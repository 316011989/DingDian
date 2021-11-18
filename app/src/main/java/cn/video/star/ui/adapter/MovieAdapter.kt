package cn.video.star.ui.adapter

import android.text.TextUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import cn.video.star.R
import cn.video.star.base.Api
import cn.video.star.base.GlideApp
import cn.video.star.data.remote.model.TopicVideoData
import cn.video.star.ui.activity.BillbordActivity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.lang.ref.WeakReference

class MovieAdapter(data: MutableList<TopicVideoData>) :
    BaseQuickAdapter<TopicVideoData, BaseViewHolder>(R.layout.item_six_list, data) {

    private val option = RequestOptions()
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .placeholder(R.mipmap.default_cover)
        .transforms(CenterCrop(), RoundedCorners(10))

    override fun convert(helper: BaseViewHolder, item: TopicVideoData) {
        val coverImg = helper.getView<ImageView>(R.id.movie_cover)
        coverImg.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        val imageViewWeakReference = WeakReference(coverImg)
        if (!TextUtils.isEmpty(item.img)) {
            GlideApp.with(mContext).asDrawable()
                .load(item.img)
                .apply(option)
                .into(imageViewWeakReference.get()!!)
        } else {
            GlideApp.with(mContext).load(R.mipmap.default_cover)
                .into(imageViewWeakReference.get()!!)
        }
        if (item.type == Api.TYPE_ESP || item.type == Api.TYPE_ZY || item.type == Api.TYPE_DM) { //剧集显示
            helper.setText(
                R.id.movie_update,
                if (TextUtils.isEmpty(item.updateText)) "" else item.updateText
            )
            helper.setGone(R.id.cover_bg, true)
        } else {
            helper.setGone(R.id.cover_bg, false)
        }
        helper.setText(R.id.movie_title, item.name)

        //进入播放页面
        helper.itemView.setOnClickListener {
            (mContext as BillbordActivity).whereGo(item.adType, item.adUrl, item.id)
        }
    }

}