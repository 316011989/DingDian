package  cn.yumi.daka.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.junechiu.junecore.utils.ALogger
import cn.yumi.daka.R
import cn.yumi.daka.base.Api.Companion.RESPONSE_OK
import cn.yumi.daka.base.App
import cn.yumi.daka.base.GlideApp
import cn.yumi.daka.data.remote.model.*
import cn.yumi.daka.ui.activity.MainActivity
import cn.yumi.daka.ui.adapter.HomeAdapter
import cn.yumi.daka.viewmodel.HomeFragmentViewModel
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * 首页中第一个
 * 推荐
 */
class HomeFragmentItemType1 : Fragment(),
    SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.RequestLoadMoreListener {

    var adapter: HomeAdapter? = null

    var data: MutableList<RecommendType> = mutableListOf()

    private var refresh: Int = 0 // 0刷新1加载

    var model: HomeFragmentViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        recyclerView.layoutManager = LinearLayoutManager(
            activity, RecyclerView.VERTICAL, false
        )

        adapter = HomeAdapter(activity as Context, GlideApp.with(this), data)
        data.add(RecommendType(RecommendType.BANNER))//添加banner类型
        data.add(RecommendType(RecommendType.FEED)) //添加feed类型
        recyclerView.adapter = adapter
        adapter?.setEnableLoadMore(true)
        adapter?.setOnLoadMoreListener(this, recyclerView)
        adapter?.disableLoadMoreIfNotFullPage() //默认第一次加载会进入回调，如果不需要可以配置
        adapter?.openLoadAnimation(BaseQuickAdapter.ALPHAIN)
        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setColorSchemeResources(R.color.main)

        subscribeData()//加载banner数据和feed数据并绘制UI
    }


    //加载数据
    private fun subscribeData() {
        if (!isAdded) { //是否添加到activity中
            return
        }
        val factory = HomeFragmentViewModel.Factory(App.INSTANCE)
        model = ViewModelProviders.of(this, factory).get(HomeFragmentViewModel::class.java)
        model?.getHomeFeed()?.observe(viewLifecycleOwner) {
            setFeedList(it?.data?.topics)//包含广告的feed绘制交给adapter
        }
        model?.getHomeBanner()?.observe(viewLifecycleOwner) {
            if (it != null && it.code == RESPONSE_OK && it.data.adList != null && it.data.adList.size > 0) {
                setBanners(it.data.adList)
            }
        }

        refreshData()
    }


    //设置数据
    /**
     *
     */
    private fun setBanners(bannerList: MutableList<BannerInfo>) {
        adapter?.data!![RecommendType.BANNER].adList = bannerList
        adapter?.notifyItemChanged(RecommendType.BANNER)
    }

    private fun setFeedList(topics: MutableList<Topic>?) {
        finishRefresh()
        //分页加载
        if (topics != null && topics.size > 0) {
            adapter?.addFeedData(topics)
            adapter?.setEnableLoadMore(true)
        } else {
            adapter?.loadMoreEnd()
            adapter?.setEnableLoadMore(false)
        }
    }

    override fun onLoadMoreRequested() {
        refresh = 1
        swipeRefreshLayout.isEnabled = false
        swipeRefreshLayout.postDelayed({ model?.loadNextPageData() }, 1000)
    }

    //完成加载刷新
    private fun finishRefresh() {
        if (refresh == 0) {
            swipeRefreshLayout?.isRefreshing = false
        } else {
            swipeRefreshLayout?.isEnabled = true
            adapter?.loadMoreComplete()
        }
    }

    override fun onRefresh() {
        refreshData()
    }

    private fun refreshData() {
        ALogger.d("HomeFragment", "refreshData: " + "推荐")
        refresh = 0
        adapter?.setEnableLoadMore(false)
        swipeRefreshLayout.isRefreshing = true
        adapter?.clearFeedData() //清除feed数据
        model?.refreshHomeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        finishRefresh()
        adapter?.handler = null
        ALogger.d("onDestroyView", "onDestroyView: " + "推荐")
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            (activity as MainActivity).setStatusBar(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                ), false
            )
    }
}