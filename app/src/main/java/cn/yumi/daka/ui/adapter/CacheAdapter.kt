package cn.yumi.daka.ui.adapter

import android.content.Intent
import android.widget.TextView
import cn.junechiu.junecore.widget.alertdialog.CustomAlertDialog
import cn.yumi.daka.R
import cn.yumi.daka.base.GlideRequests
import cn.yumi.daka.data.local.db.entity.DownloadEpisodeEntity
import cn.yumi.daka.ui.activity.CacheActivity
import cn.yumi.daka.ui.activity.CacheFinishActivity
import cn.yumi.daka.ui.activity.CachingActivity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class CacheAdapter(
    val glide: GlideRequests,
    data: MutableList<DownloadEpisodeEntity>,
) : BaseQuickAdapter<DownloadEpisodeEntity, BaseViewHolder>(R.layout.item_download, data) {


    companion object {
        private val option = RequestOptions()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)//缓存保存数据为解码后数据;All.14>RESOURCE.18;>NONE仍然是18
            .placeholder(R.mipmap.default_cover)
            .fallback(R.mipmap.default_cover)
            .transforms(CenterCrop(), RoundedCorners(10))
    }

    override fun convert(helper: BaseViewHolder?, item: DownloadEpisodeEntity?) {
        if (helper == null || item == null)
            return
        if (item.seriesName == "正在缓存") {
            glide.load("")
                .apply(option)
                .into(helper.getView(R.id.item_download_imageview))

            helper.getView<TextView>(R.id.item_download_moviename).text = item.seriesName
            helper.getView<TextView>(R.id.item_download_downloadinfo).text =
                "${item.seriesName}${item.episodeName}等${item.count}个视频"
            helper.itemView.setOnClickListener {
                mContext.startActivity(Intent(mContext, CachingActivity::class.java))
            }
        } else {
            glide.load(item.img)
                .apply(option)
                .into(helper.getView(R.id.item_download_imageview))

            helper.getView<TextView>(R.id.item_download_moviename).text = item.seriesName
            helper.getView<TextView>(R.id.item_download_downloadinfo).text = "共${item.count}个视频"
            helper.itemView.setOnClickListener {
                val i = Intent(mContext, CacheFinishActivity::class.java)
                i.putExtra("video", item)
                mContext.startActivity(i)
            }
            helper.itemView.setOnLongClickListener {
                if (item.seriesName != "正在缓存") {
                    val dialog = CustomAlertDialog(
                        mContext, "是否删除该缓存任务?", "", "删除"
                    ) {
                        (mContext as CacheActivity).deleteDownloadTask(item)
                    }
                    dialog.show()
                }
                false
            }
        }
    }


}