package  cn.video.star.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.junechiu.junecore.utils.ALogger
import cn.video.star.R
import cn.video.star.base.Api
import cn.video.star.base.App
import cn.video.star.base.GlideApp
import cn.video.star.data.remote.model.MovieEntity
import cn.video.star.data.remote.model.MovieType
import cn.video.star.data.remote.model.TypeCate
import cn.video.star.ui.adapter.FilterTextAdapter
import cn.video.star.ui.adapter.MovieFilterRowAdapter
import cn.video.star.ui.adapter.MovieTypeAdapter
import cn.video.star.viewmodel.ChannelFragmentViewModel
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.fragment_channel.*
import kotlinx.android.synthetic.main.movie_filter_view.*
import java.util.*
import kotlin.math.min


/**
 * 首页中第一个之后的fragment
 * 各种类别
 */
class HomeFragmentItemType2 : Fragment(),
    SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.RequestLoadMoreListener {

    private var adapterMovieType: MovieTypeAdapter? = null

    var data = mutableListOf<MovieType>()

    private var adapterFilterText: FilterTextAdapter? = null

    private var fragmentName: String = ""

    private var refresh = 0 // 0刷新1加载

    var filterViewheight = 0

    private var filterMap = WeakHashMap<Int, String>()

    private var filterDataList = mutableListOf<String>()

    var hasFilterData = 0

    var callback = fun() {}  //fragment初始化完成时调用此方法

    var model: ChannelFragmentViewModel? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_channel, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
    }

    private fun initData() {
        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        data.add(MovieType(MovieType.FILTER))
        data.add(MovieType(MovieType.MOVIE_LIST)) //添加列表类型
        adapterMovieType = MovieTypeAdapter(GlideApp.with(this), data)
        adapterMovieType?.setEnableLoadMore(true)
        adapterMovieType?.setOnLoadMoreListener(this, recyclerView)
        adapterMovieType?.openLoadAnimation(BaseQuickAdapter.ALPHAIN)
        adapterMovieType?.disableLoadMoreIfNotFullPage() //默认第一次加载会进入回调，如果不需要可以配置
        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setColorSchemeResources(R.color.c7b50db)
        recyclerView.adapter = adapterMovieType
        callback()
    }

    fun setCategoryData(categories: MutableList<TypeCate>, name: String, typeId: Int) {
        //获取筛选分类
        fragmentName = name
        //最后加载
        if (categories != null && categories.size > 0) {
            data[MovieType.FILTER].filterlist = categories
            categories.forEach { item ->
                if (item.values != null && item.values.size > 0) {
                    //初始化时 全部设置为未选中
                    item.values.forEach { it.isSelected = false }
                    if (item.values[0].type == 0) {  //默认第一个选中
                        item.values[0].isSelected = true
                    }
                }
            }
            //初始化filterview2
            initFilterView2(categories)
            hasFilterData = 1
        } else {
            hasFilterData = 0
        }
        //筛选header
        adapterMovieType?.notifyItemChanged(MovieType.FILTER)
        subscribeUI(typeId)
        filterListview()
        scrollListen()
    }

    //订阅
    private fun subscribeUI(typeId: Int) {
        val factory = ChannelFragmentViewModel.Factory(typeId, "", App.INSTANCE)
        model = ViewModelProviders.of(this, factory)
            .get("typeId-$typeId", ChannelFragmentViewModel::class.java)
        model?.getMovieListData()?.observe(this, { movieListEntity ->
            if (movieListEntity != null && movieListEntity.code == Api.RESPONSE_OK) {
                setMovieList(movieListEntity.data)
            } else {
                finishRefresh()
            }
        })
        model?.refreshData()
    }


    //
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.d("ChannelFragment $fragmentName", "setUserVisibleHint: $isVisibleToUser")
        if (isVisibleToUser && isAdded) {
            if (hasFilterData == 1) {
                filterScrollTop()
            } else {
                tipFilter.visibility = View.GONE
            }
        }

    }

    private fun scrollListen() {
        if (recyclerView != null) {
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int,
                ) {
                    super.onScrollStateChanged(recyclerView, newState)
                    hideFilterView2() //滑动隐藏filterview2
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val position = layoutManager.findFirstVisibleItemPosition()
                    if (position == 0) {
                        val firstVisiableChildView = layoutManager.findViewByPosition(position)!!
                        val itemHeight = firstVisiableChildView.height
                        val distance = position * itemHeight - firstVisiableChildView.top
                        // 1,1/130 1,2/130
                        val alpha = min(1f, distance.toFloat() / filterViewheight)
                        if (alpha < 0.84) {
                            tipFilter.visibility = View.GONE
                            filterResultLay.visibility = View.GONE
                        }
                    }
                    //向上滑动
                    if (position > 0) {
                        if (dy > 0 && hasFilterData == 1) {
                            filterResultLay.visibility = View.VISIBLE
                            tipFilter.visibility = View.GONE
                        }
                    } else {
                        filterResultLay.visibility = View.GONE
                        tipFilter.visibility = View.GONE
                    }

                }
            })
        }
    }

    //初始化筛选
    private fun filterListview() {
        filterView2.visibility = View.GONE //筛选view2
        tipFilter.text = String.format(getString(R.string.filter_cate), fragmentName)
        filterResultLay.background.alpha = 229
        filterTextlistView.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        adapterFilterText = FilterTextAdapter(filterDataList)
        filterTextlistView.adapter = adapterFilterText

        //点击显示筛选view2
        filterTextlistView.setOnTouchListener { _, _ ->
            showFilterView2()
            true
        }
        filterResultLay.setOnTouchListener { _, _ ->
            showFilterView2()
            true
        }

        updateFilterData()
        //筛选列表数据
        adapterMovieType?.itemCallback = { _, categoryId, valueId ->
            updateFilterData()
            filterData(categoryId, valueId)
        }
    }

    //筛选view2
    private fun initFilterView2(filterlist: MutableList<TypeCate>) {
        if (filterlist != null && filterlist.size > 0) {
            filterListview.layoutManager = LinearLayoutManager(
                activity,
                RecyclerView.VERTICAL,
                false
            )
            val adapter = MovieFilterRowAdapter(filterlist)
            filterListview.adapter = adapter
            //点击筛选 进行数据筛选
            adapter.itemCallback = { typeId, categoryId, valueId ->
                adapterMovieType?.refreshFilterAdapter()
                adapterMovieType?.clearMovie() //清除movie列表数据
                updateFilterData()
                filterData(categoryId, valueId)
                ALogger.d("setFilter2", "typeId: $typeId categoryId: $categoryId valueId:$valueId")
            }
        }
    }

    //显示筛选view2
    private fun showFilterView2() {
        filterResultLay.visibility = View.GONE
        filterView2.visibility = View.VISIBLE
    }

    //隐藏筛选view2
    fun hideFilterView2() {
        filterView2.visibility = View.GONE
    }

    //更新筛选关键字
    private fun updateFilterData() {
        filterDataList.clear()
        var count = 0
        if (data[MovieType.FILTER].filterlist != null) {
            data[MovieType.FILTER].filterlist!!.forEach { cate ->
                cate.values.forEach { value ->
                    if (value.isSelected) {
                        if (value.type != 0) {
                            count += 1
                            filterDataList.add(value.name)
                        }
                    }
                }
            }
        }
        //筛选值是否全部 为0全部
        if (count <= 0) {
            filterDataList.add("全部")
        }
        adapterFilterText?.notifyDataSetChanged()
    }

    //进行筛选数据 //筛选滚动
    private fun filterData(categoryId: Int, valueId: Int) {
        recyclerView.postDelayed({ recyclerView.scrollToPosition(0) }, 200)
        filterMap[categoryId] = valueId.toString()//类别id valueid
        val filterBuffer = StringBuilder()
        if (filterMap.size > 0) {
            filterMap.forEach { item ->
                if (item.value.toInt() != -1) {
                    filterBuffer.append(item.value).append(",")
                }
            }
        }
        if (filterBuffer.isNotEmpty()) {
            val values = filterBuffer.dropLast(1).toString()
            model?.setValueIdsData(values)
            ALogger.d("values", values)
        } else {
            model?.setValueIdsData("")
        }
        refreshData()
    }

    //筛选列表上滑高度
    private fun filterScrollTop() {
        if (adapterMovieType != null) {
            if (recyclerView != null) {
                recyclerView.scrollToPosition(1)
                tipFilter.visibility = View.VISIBLE
                filterResultLay.visibility = View.GONE
            }
        }
    }

    //设置列表数据
    private fun setMovieList(movieList: MutableList<MovieEntity>) {
        if (movieList != null && movieList.size > 0) { //判断是否还有数据
            if (refresh == 0)
                adapterMovieType?.setNewMovie(movieList)
            else
                adapterMovieType?.addNewMovie(movieList)
            adapterMovieType?.setEnableLoadMore(true)
            if (adapterMovieType?.getMovieData()?.size == movieList.size)
                filterScrollTop()
            finishRefresh()
        } else {
            adapterMovieType?.loadMoreEnd()
            adapterMovieType?.setEnableLoadMore(false)
        }
        if (movieList != null) {
            showNoDataView(movieList.size)
        }
    }

    private fun showNoDataView(size: Int) {
        ALogger.d("showNoDataView", "size: $size")
        if (model?.getCurrentPage()!! > 1) {
            noDataView.visibility = View.GONE
        } else if (size <= 0) {
            tipFilter.visibility = View.GONE
            noDataView.visibility = View.VISIBLE
        }

    }

    //加载更多数据
    override fun onLoadMoreRequested() {
        refresh = 1
        swipeRefreshLayout.isEnabled = false
        swipeRefreshLayout.postDelayed({ model?.loadNextPageData() }, 1000)
    }

    //刷新数据
    override fun onRefresh() {
        refreshData()
    }

    //完成加载刷新
    private fun finishRefresh() {
        swipeRefreshLayout?.isRefreshing = false
        swipeRefreshLayout?.isEnabled = true
        adapterMovieType?.loadMoreComplete()
    }

    //刷新数据
    private fun refreshData() {
        adapterMovieType?.setEnableLoadMore(false)
        swipeRefreshLayout.isRefreshing = true
        refresh = 0
        adapterMovieType?.clearMovie()
        model?.refreshData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        finishRefresh()
        ALogger.d("onDestroyView", "onDestroyView: $fragmentName")
    }

}