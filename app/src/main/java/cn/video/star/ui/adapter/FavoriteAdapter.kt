package cn.video.star.ui.adapter

import cn.video.star.R
import cn.video.star.base.GlideApp
import cn.video.star.data.local.db.entity.CollectEntity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class FavoriteAdapter(data: MutableList<CollectEntity>) :
    BaseQuickAdapter<CollectEntity, BaseViewHolder>(R.layout.item_history_item, data) {

    //长按删除监听
    var delCallback = fun(_: Long) {}

    //点选item回调
    var clickItem = fun(_: Long) {}

    var isEdit = false

    override fun convert(helper: BaseViewHolder, item: CollectEntity) {
        GlideApp.with(mContext)
            .load(item.cover)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(helper.getView(R.id.movie_cover))
        helper.setText(R.id.movie_title, item.name)
        if (isEdit) {
            helper.setGone(R.id.select_img, true)
        } else {
            helper.setGone(R.id.select_img, false)
        }

        if (item.selected == 1) {
            helper.setImageResource(R.id.select_img, R.mipmap.history_selected_icon)
        } else {
            helper.setImageResource(R.id.select_img, R.mipmap.history_select_icon)
        }

        helper.itemView.setOnClickListener {
            item.selected = if (item.selected == 1) 0 else 1
            if (item.selected == 1) {
                helper.setImageResource(R.id.select_img, R.mipmap.history_selected_icon)
            } else {
                helper.setImageResource(R.id.select_img, R.mipmap.history_select_icon)
            }
            clickItem(item.movieId!!)
        }

        //长按删除
        helper.itemView.setOnLongClickListener {
            delCallback(item.movieId!!)
            true
        }
    }
}