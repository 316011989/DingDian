package cn.video.star.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.video.star.R
import cn.video.star.base.App
import cn.video.star.base.GlideApp
import cn.video.star.data.remote.model.DiscoverFeed
import cn.video.star.ui.activity.MainActivity
import cn.video.star.ui.activity.SearchActivity
import cn.video.star.ui.adapter.DiscoverAdapter
import cn.video.star.viewmodel.DiscoverViewModel
import cn.video.star.viewmodel.HomeFragmentViewModel
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.fragment_discover.*
import kotlinx.android.synthetic.main.fragment_home.recyclerView
import kotlinx.android.synthetic.main.fragment_home.swipeRefreshLayout
import org.jetbrains.anko.startActivity

class DiscoverFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    BaseQuickAdapter.RequestLoadMoreListener {
    lateinit var adapter: DiscoverAdapter
    private var pageIndex = 1
    private val pageSize = 5

    var feed: MutableList<DiscoverFeed> = mutableListOf()
    var model: HomeFragmentViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).setStatusBar(ContextCompat.getColor(requireContext(), R.color.colorPrimary), false)
        discover_search.setOnClickListener {
            activity?.startActivity<SearchActivity>()
        }

        adapter = DiscoverAdapter(GlideApp.with(this), feed)
        adapter.setEnableLoadMore(true)
        adapter.setOnLoadMoreListener(this, recyclerView)
        adapter.disableLoadMoreIfNotFullPage() //默认第一次加载会进入回调，如果不需要可以配置
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN)
        recyclerView.layoutManager = LinearLayoutManager(
            activity, RecyclerView.VERTICAL, false
        )
        recyclerView.adapter = adapter
        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setColorSchemeResources(R.color.main)
        subscribeData()
    }

    private fun subscribeData() {
        val factory = DiscoverViewModel.Factory(App.INSTANCE)
        val model = ViewModelProviders.of(this, factory).get(DiscoverViewModel::class.java)
        model.getDiscoverFeed(pageIndex, pageSize)?.observe(viewLifecycleOwner, {
            Log.e("DiscoverFragment", "subscribeData$pageIndex")
            swipeRefreshLayout.isRefreshing = false
            adapter.setEnableLoadMore(true)
            if (it != null) {
                if (pageIndex == 1) {
                    adapter.replaceData(it.data.topics)
                } else {
                    adapter.addData(it.data.topics)
                    adapter.loadMoreComplete()
                }
            } else {
                adapter.loadMoreEnd()
            }

        })
    }

    /**
     * 下拉刷新
     */
    override fun onRefresh() {
        adapter.setEnableLoadMore(false)
        swipeRefreshLayout.isRefreshing = true
        pageIndex = 1
        subscribeData()
    }

    /**
     * 上拉请求更多
     */
    override fun onLoadMoreRequested() {
        adapter.setEnableLoadMore(false)
        pageIndex++
        subscribeData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            (activity as MainActivity).setStatusBar(ContextCompat.getColor(requireContext(), R.color.colorPrimary), false)
        }
    }
}