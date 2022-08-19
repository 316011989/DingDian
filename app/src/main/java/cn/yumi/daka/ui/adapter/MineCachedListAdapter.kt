package cn.yumi.daka.ui.adapter

import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import cn.junechiu.junecore.utils.ScreenUtil
import cn.yumi.daka.R
import cn.yumi.daka.base.GlideApp
import cn.yumi.daka.data.local.db.entity.DownloadEpisodeEntity
import cn.yumi.daka.ui.activity.CacheFinishActivity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.jetbrains.anko.startActivity

class MineCachedListAdapter(data: MutableList<DownloadEpisodeEntity>) :
    BaseQuickAdapter<DownloadEpisodeEntity, BaseViewHolder>(R.layout.item_mine_history_item, data) {

    companion object {
        private val option = RequestOptions()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .placeholder(R.mipmap.default_cover)
            .fallback(R.mipmap.default_cover)
            .transforms(CenterCrop(), RoundedCorners(10))
    }

    override fun convert(helper: BaseViewHolder, item: DownloadEpisodeEntity) {
        val title = helper.getView<TextView>(R.id.movie_title)
        title.isSelected = true
        title.text = item.episodeName
        val imageView = helper.getView<ImageView>(R.id.movie_cover)

        val itemW = ScreenUtil.widthPixels / 3
        val itemH = (itemW * 1.5).toInt()
        imageView.layoutParams = FrameLayout.LayoutParams(itemW - 2, itemH)
        helper.itemView.layoutParams.width = itemW

        GlideApp.with(mContext).asDrawable()
            .load(item.img)
            .apply(option)
            .into(imageView)

        helper.itemView.setOnClickListener {
            mContext.startActivity<CacheFinishActivity>(
                "id" to item.episodeId, "title" to item.seriesName, "src" to (item.source ?: 0),
            )
        }
    }
}