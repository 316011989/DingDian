package cn.video.star.ui.adapter

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.*
import cn.junechiu.junecore.widget.alertdialog.CustomAlertDialog
import  cn.video.star.R
import cn.video.star.base.GlideRequests
import  cn.video.star.data.local.db.entity.DownloadEpisodeEntity
import  cn.video.star.data.remote.model.Detail
import  cn.video.star.data.remote.model.VideoData
import cn.video.star.data.remote.model.VideoPlay
import  cn.video.star.download.DownloadEntity
import  cn.video.star.download.DownloadFeature
import  cn.video.star.download.DownloadFileUtil
import cn.video.star.download.SingleDownloadRunnable
import cn.video.star.ui.activity.SettingActivity
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.SPUtils
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.runOnUiThread

class CachingAdapter(
    val glide: GlideRequests,
    data: MutableList<DownloadEpisodeEntity>
) : BaseQuickAdapter<DownloadEpisodeEntity, BaseViewHolder>(R.layout.item_downloading, data) {
    var selectable: Boolean = false
    var chooseIds: MutableList<Long> = mutableListOf()


    override fun convert(helper: BaseViewHolder?, item: DownloadEpisodeEntity?) {
        if (helper == null || item == null)
            return
        glide.asDrawable().load(item.img)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)//缓存保存数据为解码后数据;All.14>RESOURCE.18;>NONE仍然是18
            .placeholder(R.mipmap.default_cover)
            .fallback(R.mipmap.default_cover)
            .centerCrop()
            .transform(RoundedCorners(10))
            .into(helper.getView(R.id.item_download_imageview))
        //剧名
        if (!item.seriesName.isNullOrEmpty())
            helper.getView<TextView>(R.id.item_download_moviename).text = item.seriesName
        else
            helper.getView<TextView>(R.id.item_download_moviename).visibility = View.GONE
        //来源
        helper.getView<TextView>(R.id.item_download_source).text = "来源${item.source}"
        //集名
        if (!item.episodeName.isNullOrEmpty())
            helper.getView<TextView>(R.id.item_download_episode).text = item.episodeName
        else
            helper.getView<TextView>(R.id.item_download_episode).visibility = View.GONE
        //下载状态
        if (item.downloadStatus != null && item.downloadPrograss != null) {
            var state = ""
            when (item.downloadStatus) {
                DownloadEntity.state_prepare -> {
                    state = "排队下载"
                    helper.setTextColor(
                        R.id.item_download_downloadinfo,
                        Color.parseColor("#999999")
                    )
                }
                DownloadEntity.state_start -> {
                    state = "连接资源"
                    helper.setTextColor(
                        R.id.item_download_downloadinfo,
                        Color.parseColor("#999999")
                    )
                }
                DownloadEntity.state_prograss -> {
                    state = DownloadFileUtil.byteToString(item.speed ?: 0)
                    helper.setTextColor(
                        R.id.item_download_downloadinfo,
                        Color.parseColor("#0086E5")
                    )
                }
                DownloadEntity.state_pause -> {
                    state = "下载暂停"
                    helper.setTextColor(
                        R.id.item_download_downloadinfo,
                        Color.parseColor("#999999")
                    )
                }
                DownloadEntity.state_success -> {
                    state = "下载成功"
                    helper.setTextColor(
                        R.id.item_download_downloadinfo,
                        Color.parseColor("#FF613C")
                    )
                }
                DownloadEntity.state_fail -> {
                    state = "下载失败"
                    helper.setTextColor(
                        R.id.item_download_downloadinfo,
                        Color.parseColor("#FF613C")
                    )
                }
            }
            val prograss =
                ((item.successTsCount ?: 0).toFloat() / (item.totalTsCount
                    ?: 0).toFloat() * 100_00F).toInt()
            helper.getView<TextView>(R.id.item_download_downloadinfo).text =
                "$state  进度${prograss / 100F}%"
            helper.getView<ProgressBar>(R.id.item_download_prograss).progress = prograss

        }

        helper.getView<RelativeLayout>(R.id.item_changestate_layout).setOnClickListener {
            //等待下载 开始下载 下载中 三种状态转换为暂停状态
            if (item.downloadStatus == 0 || item.downloadStatus == 1 || item.downloadStatus == 2) {
                resumeOrPauseDownload(item, DownloadEntity.state_pause)
            } else {
                if (NetworkUtils.isWifiConnected() || SPUtils.getInstance()
                        .getBoolean("OpenMobileData")
                ) {
                    resumeOrPauseDownload(item, DownloadEntity.state_start)
                } else {
                    val dialog = CustomAlertDialog(
                        mContext, "当前设置仅在WiFi下缓存,如仍需缓存可以到[设置]里开启", "", "去设置"
                    ) {
                        mContext.startActivity(Intent(mContext, SettingActivity::class.java))
                    }
                    dialog.show()
                }
            }
        }

        val cb_layout = helper.getView<LinearLayout>(R.id.item_download_chooselayout)
        val cb = helper.getView<CheckBox>(R.id.item_download_checkbox)
        //可编辑状态
        if (selectable) {
            cb.visibility = View.VISIBLE
        } else {
            cb.visibility = View.GONE
        }
        cb_layout.setOnClickListener {
            chooseOrNot(cb, item)
        }
        cb.setOnClickListener {
            chooseOrNot(cb, item)
        }
        cb.isSelected = false
        if (item.episodeId in chooseIds) {
            cb.isSelected = true
        }
    }

    private fun chooseOrNot(cb: CheckBox, item: DownloadEpisodeEntity) {
        cb.isSelected = !cb.isSelected
        if (chooseIds.contains(item.episodeId))
            chooseIds.remove(item.episodeId)
        else
            chooseIds.add(item.episodeId ?: 0)
    }

    private fun resumeOrPauseDownload(task: DownloadEpisodeEntity, targetState: Int) {
        //发送通知更新UI
        val d = DownloadEntity()
        d.downloadState = targetState
        d.taskId = task.episodeId.toString()
        d.downloadSpeed = task.speed ?: 0
        d.tsSuccessCount = task.successTsCount ?: 0
        d.tsTotalCount = task.totalTsCount ?: 0
        task.downloadStatus = targetState
        //先修改数据库状态
        DownloadFeature.updateEpisodeDownloadStatus(
            task.episodeId ?: 0, targetState, task.speed ?: 0,
            task.successTsCount ?: 0, task.totalTsCount ?: 0
        ) {
            mContext.runOnUiThread {
                if (it != null && it > 0) {//数据库更新成功
                    //目标是暂停该任务
                    if (targetState == DownloadEntity.state_pause) {
                        //移除线程池本集所有线程
                        SingleDownloadRunnable.instance.cancelM3u8Download(task.episodeId.toString())
                        EventBus.getDefault().post(d)
                    } else {//重新创建任务
                        //下边两个对象,仅提供某些必要属性供插库和解析使用,不做UI等功能
                        val videoPlay = VideoPlay()
                        videoPlay.source = task.source ?: 0
                        videoPlay.playUrl = task.playUrl ?: ""
                        videoPlay.sourceIsVip = task.sourceIsVip ?: 0
                        videoPlay.rate = task.rate ?: ""
                        videoPlay.id = task.episodeId ?: 0
                        videoPlay.headers = null
                        videoPlay.oldPlayUrl = task.playUrl
                        videoPlay.episode = task.episode ?: 0
                        val videoData = VideoData(
                            task.seriesId ?: 0, task.vType ?: 0, task.source ?: 0,
                            task.seriesName ?: "", "", task.img ?: "",
                            "", "", "", task.sourceIsVip ?: 0,
                            0, "", 0, "", 0,
                            0, 0, task.playUrl ?: "", 0,
                            "", 0, mutableListOf(), Detail(
                                "", "",
                                "", "", task.episode ?: 0, "", "",
                                0, "", ""
                            ), "", "", ""
                        )
                        DownloadFeature.addDownloadTask2DB(videoData, videoPlay)
                    }
                }
            }
        }

    }
}