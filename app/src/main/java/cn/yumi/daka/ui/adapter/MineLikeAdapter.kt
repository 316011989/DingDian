package cn.yumi.daka.ui.adapter

import cn.yumi.daka.R
import cn.yumi.daka.data.remote.model.LikeData
import cn.yumi.daka.base.Api
import cn.yumi.daka.base.GlideApp
import cn.yumi.daka.ui.activity.PlayerWindowActivity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.jetbrains.anko.startActivity

class MineLikeAdapter(data: MutableList<LikeData>) :
    BaseQuickAdapter<LikeData, BaseViewHolder>(R.layout.item_mine_like_item, data) {

    override fun convert(helper: BaseViewHolder, item: LikeData) {
        helper.setText(R.id.movie_title, item.name)
        if (item.type == Api.TYPE_ESP || item.type == Api.TYPE_ZY || item.type == Api.TYPE_DM) { //剧集显示
            helper.setText(R.id.movie_update, item.updateText)
            helper.setGone(R.id.movie_update, true)
        } else {
            helper.setGone(R.id.movie_update, false)
        }
        GlideApp.with(mContext)
            .load(item.img)
            .placeholder(R.mipmap.default_cover)
            .fallback(R.mipmap.default_cover)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(helper.getView(R.id.movie_cover))

        helper.itemView.setOnClickListener {
            mContext.startActivity<PlayerWindowActivity>("id" to item.id)
        }
    }
}