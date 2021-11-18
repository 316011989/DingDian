package cn.video.star.ui.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.video.star.R
import cn.video.star.data.remote.model.HistoryWatchType
import cn.video.star.data.local.db.entity.MovieHistoryEntity
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * Created by android on 2018/4/20.
 */
class HistoryTypeAdapter(data: MutableList<HistoryWatchType>) :
    BaseMultiItemQuickAdapter<HistoryWatchType, BaseViewHolder>(data) {

    var delCallback = fun(_: MovieHistoryEntity) {}

    var isEdit = false

    //点选item回调
    var clickItem = fun() {}

    init {
        addItemType(HistoryWatchType.DATE, R.layout.item_history_date)
        addItemType(HistoryWatchType.LIST, R.layout.item_movie_type_movie)
    }

    override fun convert(helper: BaseViewHolder, item: HistoryWatchType) {
        when (helper.itemViewType) {
            HistoryWatchType.DATE -> {
                setDate(helper, item)
            }
            HistoryWatchType.LIST -> {
                setList(helper, item)
            }
        }
    }

    private fun setDate(helper: BaseViewHolder, item: HistoryWatchType) {
        helper.setText(R.id.date_text, item.date)
    }

    private fun setList(helper: BaseViewHolder, item: HistoryWatchType) {
        val movielistView = helper.getView<RecyclerView>(R.id.movielistView)
        movielistView.layoutManager = LinearLayoutManager(
            mContext,
            RecyclerView.VERTICAL,
            false
        )
        val adapter = HistoryAdapter(item.historyList!!)
        movielistView.adapter = adapter
        //长按事件
        adapter.delCallback = {
            delCallback(it)
        }
        adapter.clickItem = clickItem
        adapter.isEdit = isEdit
    }
}