package cn.yumi.daka.ui.adapter

import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import cn.yumi.daka.R
import cn.yumi.daka.base.Api
import cn.yumi.daka.base.GlideApp
import cn.yumi.daka.data.remote.model.Suggest
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class PlayerRecommendAdapter(data: MutableList<Suggest>) :
    BaseQuickAdapter<Suggest, BaseViewHolder>(R.layout.item_six_list, data) {

    private val option = RequestOptions()
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .placeholder(R.mipmap.default_cover)
        .fallback(R.mipmap.default_cover)
        .transforms(CenterCrop(), RoundedCorners(10))


    var itemCallback = fun(_: Int) {}

    override fun convert(helper: BaseViewHolder, item: Suggest) {
        val container = helper.getView<RelativeLayout>(R.id.item_movie_container)
        container.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        if (!TextUtils.isEmpty(item.img)) {
            GlideApp.with(mContext)
                .load(item.img)
                .apply(option)
                .into(helper.getView(R.id.movie_cover))
        } else {
            GlideApp.with(mContext).load(R.mipmap.default_cover)
                .into(helper.getView(R.id.movie_cover))
        }
        helper.setText(R.id.movie_title, item.name)

        if (item.type == Api.TYPE_ESP || item.type == Api.TYPE_ZY || item.type == Api.TYPE_DM) { //剧集显示
            helper.getView<TextView>(R.id.movie_update).visibility = View.VISIBLE
            helper.setText(R.id.movie_update, if (TextUtils.isEmpty(item.updateText)) "" else item.updateText)
            helper.setGone(R.id.cover_bg, true)
        } else {
            helper.getView<TextView>(R.id.movie_update).visibility = View.INVISIBLE
            helper.setGone(R.id.cover_bg, false)
        }

        helper.itemView.setOnClickListener {
            itemCallback(helper.adapterPosition)
        }
    }
}