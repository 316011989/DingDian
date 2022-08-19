package  cn.yumi.daka.ui.adapter

import  cn.yumi.daka.R
import  cn.yumi.daka.data.local.db.entity.SearchWordEntity
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class SearchHistoryAdapter(data: MutableList<SearchWordEntity>?) :
    BaseQuickAdapter<SearchWordEntity, BaseViewHolder>(R.layout.item_search_history, data) {

    var itemCallback = fun(_: String) {}

    override fun convert(helper: BaseViewHolder, item: SearchWordEntity) {
        helper.setText(R.id.search_word, item.word)

        helper.itemView.setOnClickListener {
            itemCallback(item.word)
        }
    }
}