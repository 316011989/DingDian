package cn.video.star.ui.adapter

import android.widget.TextView
import cn.video.star.R
import cn.video.star.ui.activity.SearchActivity
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class SearchSuggestAdapter(data: MutableList<String>) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.search_suggest_layout, data) {

    override fun convert(helper: BaseViewHolder?, item: String?) {
        helper?.getView<TextView>(R.id.suggest_tv)?.text = item
        helper?.getView<TextView>(R.id.suggest_tv)?.setOnClickListener {
            (mContext as SearchActivity).search(item!!)
        }
    }
}