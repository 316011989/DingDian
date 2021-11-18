package cn.video.star.ui.adapter

import android.content.Intent
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import cn.video.star.R
import cn.video.star.base.GlideRequests
import cn.video.star.data.local.db.entity.DownloadEpisodeEntity
import cn.video.star.ui.activity.PlayerWindowActivity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.jetbrains.anko.startActivity

class CacheFinishAdapter(
    val glide: GlideRequests,
    data: MutableList<DownloadEpisodeEntity>
) : BaseQuickAdapter<DownloadEpisodeEntity, BaseViewHolder>(R.layout.item_downloading, data) {
    var selectable: Boolean = false
    var chooseIds: MutableList<Long> = mutableListOf()

    override fun convert(helper: BaseViewHolder?, item: DownloadEpisodeEntity?) {
        if (helper == null || item == null)
            return
        glide.load(item.img)
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
            helper.getView<TextView>(R.id.item_download_downloadinfo).text =
                "下载完成 点击播放"
            helper.getView<ProgressBar>(R.id.item_download_prograss).visibility = View.INVISIBLE
        }

        helper.itemView.setOnClickListener {
            mContext.startActivity<PlayerWindowActivity>(
                "id" to item.seriesId,
                "episodeId" to item.episodeId,
                "esp" to (item.episode ?: 1) - 1,
                "src" to item.source)
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
}