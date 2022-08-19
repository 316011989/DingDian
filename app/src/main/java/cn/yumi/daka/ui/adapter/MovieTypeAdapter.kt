package  cn.yumi.daka.ui.adapter

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.FrameLayout
import  cn.yumi.daka.R
import  cn.yumi.daka.data.remote.model.MovieEntity
import  cn.yumi.daka.data.remote.model.MovieType
import  cn.yumi.daka.ui.widget.GridSpacingItemDecoration
import cn.junechiu.junecore.utils.ALogger
import cn.junechiu.junecore.utils.ScreenUtil
import cn.yumi.daka.base.GlideRequests
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder


/**
 * Created by android on 2018/4/20.
 */
class MovieTypeAdapter(val glide: GlideRequests, data: MutableList<MovieType>) :
    BaseMultiItemQuickAdapter<MovieType, BaseViewHolder>(data) {

    private var movieAdapter = MovieListAdapter(glide, mutableListOf(), 0, 0)

    private var filterAdapter = MovieFilterRowAdapter(mutableListOf())


    init {
        addItemType(MovieType.FILTER, R.layout.item_movie_type_filter)
        addItemType(MovieType.MOVIE_LIST, R.layout.item_movie_type_movie)

        val itemWidth = (ScreenUtil.widthPixels - ScreenUtil.dp2px(8.5f) * 2 - ScreenUtil.dp2px(11f) * 2) / 3
        val itemHeight = itemWidth / 0.67
        movieAdapter.itemWidth = itemWidth
        movieAdapter.itemHeight = itemHeight.toInt()
    }

    var itemCallback = fun(_: Int, _: Int, _: Int) {}

    override fun convert(helper: BaseViewHolder, item: MovieType) {
        when (helper.itemViewType) {
            MovieType.FILTER -> {
                setFilter(helper, item)
            }
            MovieType.MOVIE_LIST -> {
                setMovieList(helper, item)
            }
        }
    }

    private fun setFilter(helper: BaseViewHolder, item: MovieType) {
        val filterListview = helper.getView<RecyclerView>(R.id.filter_listview)
        val params = filterListview.layoutParams as FrameLayout.LayoutParams
        if (item.filterlist != null && item.filterlist!!.size > 0) {
            filterListview.layoutManager = LinearLayoutManager(
                mContext,
                RecyclerView.VERTICAL,
                false
            )
            filterListview.adapter = filterAdapter
            filterAdapter.data.clear()
            filterAdapter.addData(item.filterlist!!)
            filterAdapter.itemCallback = { typeId, categoryId, valueId ->
                //获取筛选列表
                itemCallback(typeId, categoryId, valueId)
                ALogger.d("setFilter", "typeId: $typeId categoryId: $categoryId valueId:$valueId")
            }

            params.setMargins(ScreenUtil.dp2px(10f), 0, ScreenUtil.dp2px(10f), ScreenUtil.dp2px(20f))
        } else {
            params.setMargins(ScreenUtil.dp2px(10f), 0, ScreenUtil.dp2px(10f), 0)
        }
    }

    private fun setMovieList(helper: BaseViewHolder, item: MovieType) {
        val movielistView = helper.getView<RecyclerView>(R.id.movielistView)
        if (movielistView.tag != "1") {
            movielistView.layoutManager = GridLayoutManager(mContext, 3)
            movielistView.addItemDecoration(
                GridSpacingItemDecoration.newBuilder()
                    .includeEdge(false).horizontalSpacing(ScreenUtil.dp2px(8.5f))
                    .verticalSpacing(0)
                    .build()
            )
            movielistView.tag = "1"
        }
        movielistView.adapter = movieAdapter
    }

    //刷新筛选列表
    fun refreshFilterAdapter() {
        filterAdapter.notifyDataSetChanged()
    }

    fun addNewMovie(movielist: Collection<MovieEntity>) {
        if (movielist != null) {
            movieAdapter.addData(movielist)
        }
    }

    fun setNewMovie(movielist: List<MovieEntity>) {
        if (movielist != null) {
            movieAdapter.setNewData(movielist)
        }
    }


    fun clearMovie() {
        movieAdapter.data.clear()
        movieAdapter.notifyDataSetChanged()
    }

    fun getMovieData(): MutableList<MovieEntity> {
        return movieAdapter.data
    }
}