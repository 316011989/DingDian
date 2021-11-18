package cn.video.star.ui.adapter

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.junechiu.junecore.utils.ScreenUtil
import cn.video.star.R
import cn.video.star.base.GlideRequests
import cn.video.star.data.remote.model.Topic
import cn.video.star.ui.activity.BillbordActivity
import cn.video.star.ui.activity.MainActivity
import cn.video.star.ui.widget.GridSpacingItemDecoration
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bytedance.sdk.openadsdk.TTNativeAd
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.jetbrains.anko.startActivity
import java.lang.ref.WeakReference


class HomeFeedAdapter(
    val context: Context, val glide: GlideRequests, data: MutableList<Topic>,
) :
    BaseMultiItemQuickAdapter<Topic, BaseViewHolder>(data) {

    //六宫格播单,屏幕可显示3个
    private val itemWidth = ((ScreenUtil.widthPixels - ScreenUtil.dp2px(30f)) / 3f).toInt()
    private val itemHeight = (itemWidth * 1.43f).toInt()

    init {
        //横向滚动
        addItemType(Topic.FEED_PLAYLIST, R.layout.item_recommend_feed_play)
        //6宫格
        addItemType(Topic.FEED_SIXLIST, R.layout.item_recommend_feed_six)
        //广告
        addItemType(Topic.FEED_AD, R.layout.item_recommend_feed_ad)
    }

    override fun convert(helper: BaseViewHolder, item: Topic) {
        when (helper.itemViewType) {
            Topic.FEED_PLAYLIST -> {
                setPlayListData(helper, item)
            }
            Topic.FEED_SIXLIST -> {
                setSixListData(helper, item)
            }
            Topic.FEED_AD -> {
                setADData(helper, item)
            }
        }
    }

    /**
     * 横向滚动播单
     */
    private fun setPlayListData(helper: BaseViewHolder, item: Topic) {
        helper.setText(R.id.play_list_name, item.title)
        val recylerview = helper.getView<RecyclerView>(R.id.playListView)
        recylerview.layoutManager = LinearLayoutManager(
            mContext, LinearLayoutManager.HORIZONTAL,
            false
        )
        if (item.videoList != null && item.videoList.size > 0) {
            helper.setText(R.id.plist_count, item.videoList.size.toString() + "部")
            val adapter = FeedPlayListAdapter(glide, item.videoList)
            recylerview.adapter = adapter
        }

        //播单详情
        helper.getView<LinearLayout>(R.id.play_title_layout).setOnClickListener {
            (mContext as MainActivity).startActivity<BillbordActivity>(
                "id" to item.topicId, "title" to item.title
            )
        }
    }

    /**
     * 六宫格播单
     */
    private fun setSixListData(helper: BaseViewHolder, item: Topic) {
        helper.setText(R.id.six_list_title, item.title)
        helper.setText(R.id.six_list_des, item.summary)
        val recylerview = helper.getView<RecyclerView>(R.id.sixListView)
        if (item.videoList != null && item.videoList.size > 0) {
            var list = item.videoList
            if (item.videoList.size > 6) {
                list = item.videoList.subList(0, 6)
            }
            val adapter = SixListAdapter(glide, list, itemWidth, itemHeight)
            if (recylerview.tag != "1") {
                recylerview.layoutManager = GridLayoutManager(mContext, 3)
                recylerview.addItemDecoration(
                    GridSpacingItemDecoration.newBuilder()
                        .includeEdge(false).horizontalSpacing(ScreenUtil.dp2px(5f))
                        .verticalSpacing(0)
                        .build()
                )
                recylerview.tag = "1"
            }
            recylerview.adapter = adapter
        }

        //播单详情
        helper.getView<LinearLayout>(R.id.six_title_lay).setOnClickListener {
            (mContext as MainActivity).startActivity<BillbordActivity>(
                "id" to item.topicId,
                "title" to item.title
            )
        }
    }

    private fun setADData(helper: BaseViewHolder, item: Topic) {
        //穿山甲广告不为null
        if (item.ttFeedAd != null) {
            val ttFeedAd = item.ttFeedAd
            helper.setText(R.id.feedad_des, "广告")
            helper.setText(R.id.feedad_title, ttFeedAd.description)
            val coverImg = helper.getView<ImageView>(R.id.feedad_img)
            val imageViewWeakReference = WeakReference(coverImg)
            //广告图片集合不为空,第一张不为空
            if (ttFeedAd.imageList != null && ttFeedAd.imageList.isNotEmpty() && ttFeedAd.imageList[0] != null && ttFeedAd.imageList[0].isValid) {
                val myOptions = RequestOptions()
                    .centerCrop()
                    .placeholder(R.mipmap.default_cover)
                    .fallback(R.mipmap.default_cover)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                glide.asDrawable().load(ttFeedAd.imageList[0].imageUrl).apply(myOptions)
                    .into(imageViewWeakReference.get()!!)
            } else {
                glide.asDrawable().load(R.mipmap.default_cover).into(imageViewWeakReference.get()!!)
            }

            val clickViewList = mutableListOf<View>()
            clickViewList.add(helper.getView(R.id.feedad_img))
            val creativeViewList = mutableListOf<View>()
            creativeViewList.add(helper.getView(R.id.feedad_title))
            ttFeedAd.registerViewForInteraction(
                helper.itemView as ViewGroup,
                clickViewList,
                creativeViewList,
                object : TTNativeAd.AdInteractionListener {
                    override fun onAdClicked(p0: View?, ad: TTNativeAd?) {
                        if (ad != null) {
                            Log.d("首页feed广告", ad.title + "被点击")
                        }
                    }

                    override fun onAdShow(ad: TTNativeAd?) {
                    }

                    override fun onAdCreativeClick(p0: View?, ad: TTNativeAd?) {
                        if (ad != null) {
                            Log.d("首页feed广告", ad.title + "被创意按钮被点击")
                        }
                    }

                })
        } else {
            val container = helper.getView<FrameLayout>(R.id.rec_feed_adcontainer)
            container.removeAllViews()
            container.addView(item.adView)
        }
    }

}