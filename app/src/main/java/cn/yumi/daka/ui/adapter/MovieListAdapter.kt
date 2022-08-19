package cn.yumi.daka.ui.adapter

import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import cn.yumi.daka.R
import cn.yumi.daka.data.remote.model.MovieEntity
import cn.yumi.daka.base.Api
import cn.yumi.daka.base.GlideRequests
import cn.yumi.daka.ui.activity.MainActivity
import cn.yumi.daka.utils.TCAgentUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bytedance.sdk.openadsdk.TTNativeAd
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.lang.ref.WeakReference


class MovieListAdapter(
    val glide: GlideRequests,
    data: MutableList<MovieEntity>,
    var itemWidth: Int,
    var itemHeight: Int
) : BaseQuickAdapter<MovieEntity, BaseViewHolder>(R.layout.item_six_list, data) {

    override fun convert(helper: BaseViewHolder, item: MovieEntity) {
        val container = helper.getView<RelativeLayout>(R.id.item_movie_container)
        container.layoutParams = RelativeLayout.LayoutParams(itemWidth, itemHeight)
        //标题
        helper.setText(R.id.movie_title, item.name)
        //影视剧数据和穿山甲数据
        if (item.type != 102 && item.type != 103) {
            //穿山甲
            if (item.type == 101) {
                helper.getView<TextView>(R.id.movie_update).visibility = View.VISIBLE
                helper.setText(R.id.movie_update, if (TextUtils.isEmpty(item.updateText)) "" else item.updateText)
                helper.setGone(R.id.cover_bg, true)
                val clickViewList = mutableListOf<View>()
                clickViewList.add(helper.itemView)
                if (item.ttFeedAd != null)
                    item.ttFeedAd!!.registerViewForInteraction(
                        helper.itemView as ViewGroup,
                        clickViewList,
                        null,
                        object : TTNativeAd.AdInteractionListener {
                            override fun onAdClicked(p0: View?, ad: TTNativeAd?) {
                                if (ad != null)
                                    Log.d("TTad", "频道feed广告" + ad.title + " 被点击 ")
                            }

                            override fun onAdShow(ad: TTNativeAd?) {
                            }

                            override fun onAdCreativeClick(p0: View?, ad: TTNativeAd?) {
                                if (ad != null)
                                    Log.d("TTad", "频道feed广告" + ad.title + " 创意按钮被点击 ")
                            }
                        })
            }
            //普通影视剧数据
            else {
                if (item.type == Api.TYPE_ESP || item.type == Api.TYPE_ZY || item.type == Api.TYPE_DM) { //剧集显示
                    helper.getView<TextView>(R.id.movie_update).visibility = View.VISIBLE
                    helper.setText(R.id.movie_update, if (TextUtils.isEmpty(item.updateText)) "" else item.updateText)
                    helper.setGone(R.id.cover_bg, true)
                } else {
                    helper.getView<TextView>(R.id.movie_update).visibility = View.INVISIBLE
                    helper.setGone(R.id.cover_bg, false)
                }
                helper.itemView.setOnClickListener {
                    (mContext as MainActivity).whereGo(item.adType, item.adUrl, item.id)
                    TCAgentUtil.videoClick("${item.id}", "", "category")
                }
            }
            //穿山甲和影视剧数据均加载img图片url
            val imageView = helper.getView<ImageView>(R.id.movie_cover)
            val imageViewWeakReference = WeakReference(imageView)
            container.removeAllViews()
            container.addView(imageView)
            if (!TextUtils.isEmpty(item.img)) {
                glide.asDrawable().load(item.img)
                    .skipMemoryCache(true)//跳过缓存
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)//缓存保存数据为解码后数据;All.14>RESOURCE.18;>NONE仍然是18
                    .placeholder(R.mipmap.default_cover)
                    .fallback(R.mipmap.default_cover)
                    .centerCrop()
                    .override(itemWidth, itemHeight)
                    .into(imageViewWeakReference.get()!!)
            } else {
                glide.load(R.mipmap.default_cover).into(imageViewWeakReference.get()!!)
            }
        }
        //广告数据,102广点通,103趣盈
        else {
            helper.getView<TextView>(R.id.movie_update).visibility = View.VISIBLE
            helper.setText(R.id.movie_update, if (TextUtils.isEmpty(item.updateText)) "" else item.updateText)
//            container.removeAllViews()//如果这里进行remove,则imageview不存在,line81位置add会报null
            container.addView(item.adView)
        }
    }

}