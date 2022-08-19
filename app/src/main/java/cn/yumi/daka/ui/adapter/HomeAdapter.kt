package cn.yumi.daka.ui.adapter

import android.content.Context
import android.os.Handler
import android.text.TextUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import cn.junechiu.junecore.anim.DepthPageTransformer
import cn.junechiu.junecore.anim.ViewPagerScroller
import cn.yumi.daka.R
import cn.yumi.daka.base.GlideRequests
import cn.yumi.daka.data.remote.model.RecommendType
import cn.yumi.daka.data.remote.model.Topic
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * Created by android on 2018/4/18.
 */
class HomeAdapter(context: Context, val glide: GlideRequests, data: MutableList<RecommendType>) :
    BaseMultiItemQuickAdapter<RecommendType, BaseViewHolder>(data) {

    var bannerVP: ViewPager? = null
    var handler: Handler? = null
    var delayMills = 0
    private var bannerAdapter: HomeBannerAdapter? = null

    private var feedAdapter = HomeFeedAdapter(context, glide, mutableListOf())

    init {
        addItemType(RecommendType.BANNER, R.layout.item_home_banner)
        addItemType(RecommendType.FEED, R.layout.item_home_feed)

        handler = Handler()
        handler?.postDelayed(TimerRunnable(), 1000)
    }

    override fun convert(helper: BaseViewHolder, item: RecommendType) {
        when (helper.itemViewType) {
            RecommendType.BANNER -> {
                setBannerData(helper, item)
            }
            RecommendType.FEED -> {
                setFeedData(helper, item)
            }
        }
    }

    private fun setBannerData(helper: BaseViewHolder, item: RecommendType) {
        if (item.adList != null && item.adList.size > 0) {
            bannerVP = helper.getView(R.id.view_pager)
            bannerVP?.setPageTransformer(true, DepthPageTransformer())
            val scroller = ViewPagerScroller(mContext)
            scroller.setScrollDuration(1000)//时间越长，速度越慢
            scroller.initViewPagerScroll(bannerVP)

            bannerAdapter = HomeBannerAdapter(item.adList, glide)
            bannerVP?.adapter = bannerAdapter

            bannerVP?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    delayMills = 0
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    delayMills = 0
                }

                override fun onPageSelected(position: Int) {
                    //设置标题
                    if (!TextUtils.isEmpty(item.adList[position % item.adList.size].title))
                        helper.setText(
                            R.id.banner_title,
                            item.adList[position % item.adList.size].title
                        )
                }
            })
            bannerVP?.currentItem = item.adList.size * 10//默认第一个
            helper.setText(R.id.banner_title, item.adList[0].title)
        }
    }

    private fun setFeedData(helper: BaseViewHolder, item: RecommendType) {
        val recylerView = helper.getView<RecyclerView>(R.id.feedListView)
        recylerView.layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
        feedAdapter.addData(item.feedList)
        recylerView.adapter = feedAdapter
    }


    fun addFeedData(data: Collection<Topic>) {
        if (data != null) {
            feedAdapter.addData(data)
        }
    }

    fun clearFeedData() {
        feedAdapter.data.clear()
    }


    /**
     * banner自滚动线程
     */
    internal inner class TimerRunnable : Runnable {
        override fun run() {
            delayMills++
            if (delayMills == 5) {
                val curItem = bannerVP?.currentItem
                bannerVP?.currentItem = curItem!!.plus(1)
                delayMills = 0
            }
            if (handler != null) {
                handler?.postDelayed(this, 1000)
            }
        }
    }


}
