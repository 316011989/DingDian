package cn.video.star.ui.adapter

import android.view.View
import android.widget.TextView
import cn.video.star.R
import cn.video.star.data.remote.model.HotSearch
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class HotSearchAdapter(data: MutableList<HotSearch>?) :
    BaseQuickAdapter<HotSearch, BaseViewHolder>(R.layout.item_hot_search, data) {

    var itemCallback = fun(_: String) {}

    override fun convert(helper: BaseViewHolder, item: HotSearch) {
        when (item.id) {
            1 -> {
                helper.itemView.visibility = View.VISIBLE
                helper.getView<TextView>(R.id.wordIndex).setBackgroundResource(R.drawable.fill_ff6e75_rec)
            }
            2 -> {
                helper.itemView.visibility = View.VISIBLE
                helper.getView<TextView>(R.id.wordIndex).setBackgroundResource(R.drawable.fill_ffc547_rec)
            }
            3 -> {
                helper.itemView.visibility = View.VISIBLE
                helper.getView<TextView>(R.id.wordIndex).setBackgroundResource(R.drawable.fill_6fbdff_rec)
            }
            -1 -> {
                helper.itemView.visibility = View.GONE
            }
            else -> {
                helper.itemView.visibility = View.VISIBLE
                helper.getView<TextView>(R.id.wordIndex).setBackgroundResource(R.drawable.fill_bbbbbb_rec)
            }
        }
        helper.setText(R.id.wordIndex, item.id.toString())
        helper.setText(R.id.hotWord, item.text)

        helper.itemView.setOnClickListener {
            itemCallback(item.text)
        }
    }
}