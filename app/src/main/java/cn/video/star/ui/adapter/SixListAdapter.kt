package cn.video.star.ui.adapter

import android.text.TextUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import cn.video.star.R
import cn.video.star.base.Api
import cn.video.star.base.GlideRequests
import cn.video.star.data.remote.model.Video
import cn.video.star.ui.activity.MainActivity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.lang.ref.WeakReference

class SixListAdapter(
    val glide: GlideRequests,
    data: MutableList<Video>,
    private val itemWidth: Int,
    private val itemHeight: Int,
) : BaseQuickAdapter<Video, BaseViewHolder>(R.layout.item_six_list, data) {

    private val option = RequestOptions()
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .placeholder(R.mipmap.default_cover)
        .fallback(R.mipmap.default_cover)
        .override(itemWidth / 2, itemHeight / 2)
        .transforms(CenterCrop(), RoundedCorners(10))

    override fun convert(helper: BaseViewHolder, item: Video) {
        //封面宽高比 0.75
        val coverImg = helper.getView<ImageView>(R.id.movie_cover)
        coverImg.layoutParams = RelativeLayout.LayoutParams(itemWidth, itemHeight)
        val imageViewWeakReference = WeakReference(coverImg)
        if (!TextUtils.isEmpty(item.img)) {
            glide.asDrawable()
                .load(item.img)
                .apply(option)
                .into(imageViewWeakReference.get()!!)
        } else {
            glide.load(R.mipmap.default_cover).into(imageViewWeakReference.get()!!)
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
            (mContext as MainActivity).whereGo(item.adType, item.adUrl, item.id)
        }
    }
}