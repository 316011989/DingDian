package cn.yumi.daka.ui.adapter

import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import cn.yumi.daka.R
import cn.yumi.daka.data.local.db.entity.MovieHistoryEntity
import cn.yumi.daka.ui.activity.PlayerWindowActivity
import cn.junechiu.junecore.utils.ScreenUtil
import cn.yumi.daka.base.GlideApp
import cn.yumi.daka.utils.TCAgentUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.jetbrains.anko.startActivity

class MineHistoryAdapter(data: MutableList<MovieHistoryEntity>) :
    BaseQuickAdapter<MovieHistoryEntity, BaseViewHolder>(R.layout.item_mine_history_item, data) {

    companion object {
        private val option = RequestOptions()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .placeholder(R.mipmap.default_cover)
            .fallback(R.mipmap.default_cover)
            .transforms(CenterCrop(), RoundedCorners(10))
    }

    override fun convert(helper: BaseViewHolder, item: MovieHistoryEntity) {
        val title = helper.getView<TextView>(R.id.movie_title)
        title.isSelected = true
        title.text = item.name
        val imageView = helper.getView<ImageView>(R.id.movie_cover)

        val itemW = ScreenUtil.widthPixels / 4
        val itemH = (itemW * 1.5).toInt()
        imageView.layoutParams = FrameLayout.LayoutParams(itemW - 4, itemH)
        helper.itemView.layoutParams.width = itemW

        GlideApp.with(mContext).asDrawable()
            .load(item.cover)
            .apply(option)
            .into(imageView)
        helper.getView<ProgressBar>(R.id.watch_progress).progress = item.percent

        helper.itemView.setOnClickListener {
            mContext.startActivity<PlayerWindowActivity>("id" to item.movidId)
            TCAgentUtil.videoClick("${item.id}", "", "history")
        }
    }
}