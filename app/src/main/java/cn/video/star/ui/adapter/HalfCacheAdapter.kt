package cn.video.star.ui.adapter

import android.content.Intent
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import cn.junechiu.junecore.utils.ScreenUtil
import cn.junechiu.junecore.widget.alertdialog.CustomAlertDialog
import cn.video.star.R
import cn.video.star.data.remote.model.VideoData
import cn.video.star.data.remote.model.VideoPlay
import cn.video.star.download.DownloadFeature
import cn.video.star.ui.activity.SettingActivity
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.SPUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class HalfCacheAdapter(
    private val videoData: VideoData,
    private val downloadingId: MutableList<String>,
    private val downloadedId: MutableList<String>,
    private val cacheNum: TextView
) :
    BaseQuickAdapter<VideoPlay, BaseViewHolder>(R.layout.item_half_cache, videoData.plays) {

    var itemWidth = 0

    var isEdit = false

    init {
        itemWidth = (ScreenUtil.widthPixels - 5 *
                ScreenUtil.dp2px(5f) - 2 * ScreenUtil.dp2px(10f)) / 6
    }

    override fun convert(helper: BaseViewHolder, item: VideoPlay) {
        val params = LinearLayout.LayoutParams(itemWidth, itemWidth)
        val frameLayout = helper.getView<FrameLayout>(R.id.item_view)
        frameLayout.layoutParams = params
        helper.setText(R.id.episode_num, item.episode.toString())

        helper.setGone(R.id.cache_img, false)
        if (downloadingId.contains(item.id.toString())) {
            helper.setGone(R.id.cache_img, true)
            helper.setImageResource(R.id.cache_img, R.mipmap.half_cache_ing)
        } else if (downloadedId.contains(item.id.toString())) {
            helper.setGone(R.id.cache_img, true)
            helper.setImageResource(R.id.cache_img, R.mipmap.half_cache_finish)
        }

        helper.itemView.setOnClickListener {
            //缓存中,删除
            if (downloadingId.contains(item.id.toString())) {
                val dialog = CustomAlertDialog(
                    mContext, "该视频正在缓存中,是否删除?", "", "删除"
                ) {
                    downloadingId.remove(item.id.toString())
                    notifyDataSetChanged()
                    addOrRemoveTask(item, false)
                }
                dialog.show()
            }
            //已缓存,删除
            else if (downloadedId.contains(item.id.toString())) {
                val dialog = CustomAlertDialog(
                    mContext, "该视频已缓存成功,是否删除视频文件?", "", "删除"
                ) {
                    downloadingId.remove(item.id.toString())
                    notifyDataSetChanged()
                    addOrRemoveTask(item, false)
                }
                dialog.show()
            }
            //没有,添加
            else {
                //判断网络状态和数据流量是否可缓存
                if (NetworkUtils.isWifiConnected()//wifi状态
                    || SPUtils.getInstance().getBoolean("OpenMobileData")//OpenMobileData为true
                ) {
                    downloadingId.add(item.id.toString())
                    notifyDataSetChanged()
                    addOrRemoveTask(item, true)
                } else {//非wifi状态,并且流量不可缓存
                    val dialog = CustomAlertDialog(
                        mContext, "当前设置仅在WiFi下缓存,如仍需缓存可以到[设置]里开启", "", "去设置"
                    ) {
                        mContext.startActivity(Intent(mContext, SettingActivity::class.java))
                    }
                    dialog.show()
                }

            }

        }
    }

    private fun addOrRemoveTask(video: VideoPlay, isAdd: Boolean) {
        if (isAdd) {//添加任务
            //插数据库.并启动任务
            DownloadFeature.addDownloadTask2DB(videoData, video)
        } else {//删除任务
            //先修改数据库状态
            DownloadFeature.deleteEpisode(video.id.toString()) {}
        }
        cacheNum.text = downloadingId.size.toString()
    }
}

