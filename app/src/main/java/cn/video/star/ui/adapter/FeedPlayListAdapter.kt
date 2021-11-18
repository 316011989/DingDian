package cn.video.star.ui.adapter

import android.text.TextUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import cn.junechiu.junecore.utils.ScreenUtil
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

class FeedPlayListAdapter(
    private val glide: GlideRequests, data: MutableList<Video>,
) :
    BaseQuickAdapter<Video, BaseViewHolder>(R.layout.item_play_list, data) {


    //横向滚动播单,屏幕可显示2.5个
    private val feedWidth = (ScreenUtil.widthPixels / 2.9f).toInt()
    private val feedHeight = (feedWidth * 1.67f).toInt()

    private val option = RequestOptions()
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .placeholder(R.mipmap.default_cover)
        .fallback(R.mipmap.default_cover)
        .transforms(CenterCrop(), RoundedCorners(10))

    override fun convert(helper: BaseViewHolder, item: Video) {
        val coverImg = helper.getView<ImageView>(R.id.movie_cover)
        val imageViewWeakReference = WeakReference(coverImg)
        if (!TextUtils.isEmpty(item.img)) {
            glide.asDrawable()
                .load(item.img)
                .apply(option)
                .into(imageViewWeakReference.get()!!)
        } else {
            glide.load(R.mipmap.default_cover).into(imageViewWeakReference.get()!!)
        }
        helper.setText(R.id.movie_title, item.name)
        if (item.type == Api.TYPE_ESP || item.type == Api.TYPE_ZY || item.type == Api.TYPE_DM) { //剧集显示
            helper.setText(R.id.movie_update,
                if (TextUtils.isEmpty(item.updateText)) "" else item.updateText)
            helper.setGone(R.id.feed_cover_bg, true)
        } else {
            helper.setGone(R.id.feed_cover_bg, false)
        }

        val lp = RelativeLayout.LayoutParams(feedWidth, feedHeight)
        if (helper.adapterPosition == 0)
            lp.leftMargin = ScreenUtil.dp2px(10F)

        lp.rightMargin = ScreenUtil.dp2px(5F)
        helper.itemView.layoutParams = lp


        //进入播放页面
        helper.itemView.setOnClickListener {
            (mContext as MainActivity).whereGo(item.adType, item.adUrl, item.id)
        }
    }
}