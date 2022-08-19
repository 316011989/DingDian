package cn.yumi.daka.ui.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.yumi.daka.R
import cn.yumi.daka.data.remote.model.TypeCate
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class MovieFilterRowAdapter(data: MutableList<TypeCate>) :
    BaseQuickAdapter<TypeCate, BaseViewHolder>(R.layout.item_filter_row, data) {

    var itemCallback = fun(_: Int, _: Int, _: Int) {}

    override fun convert(helper: BaseViewHolder, item: TypeCate) {
        val listview = helper.getView<RecyclerView>(R.id.cate_listview)
        listview.layoutManager = LinearLayoutManager(
            mContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        if (item.values != null && item.values.size > 0) {
            val adapter = MovieFilterValueAdapter(item.values)
            listview.adapter = adapter
            //点击回调
            adapter.itemCallback = { typeId, categoryId, valueId ->
                itemCallback(typeId, categoryId, valueId)
            }
        }
    }
}