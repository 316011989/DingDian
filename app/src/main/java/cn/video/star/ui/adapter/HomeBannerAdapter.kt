package  cn.video.star.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import cn.junechiu.junecore.utils.ScreenUtil
import cn.video.star.R
import cn.video.star.base.GlideRequests
import cn.video.star.data.remote.model.BannerInfo
import cn.video.star.ui.activity.MainActivity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class HomeBannerAdapter(val list: MutableList<BannerInfo>, val glide: GlideRequests) :
    PagerAdapter() {

    private val imageParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT)
    private val option = RequestOptions()
        .skipMemoryCache(true)
        .placeholder(R.mipmap.default_cover_w)
        .fallback(R.mipmap.default_cover_w)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .transforms(CenterCrop(), RoundedCorners(ScreenUtil.dp2px(8.5f)))

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        //当viewpager选中第3页时,可以清除第0页
        container.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (list == null || list.size <= 0)
            return ImageView(container.context)
        val index = position % list.size
        val item = list[index]

        if (item.type == 102) {
            container.addView(item.adView)
            return item.adView!!
        } else {
            val imageView = ImageView(container.context)

            glide.asDrawable().load(item.img)
                .apply(option)
                .into(imageView)
            if (item.ttNativeAd != null) {
                item.ttNativeAd.registerViewForInteraction(container, imageView, null)
            } else {
                imageView.setOnClickListener {
                    (container.context as MainActivity).whereGo(item.type,
                        item.url, item.id.toLong())
                }
            }
            container.addView(imageView)
            return imageView
        }
    }

    override fun getCount(): Int {
        return Int.MAX_VALUE
    }

    override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
        return arg0 === arg1
    }
}
