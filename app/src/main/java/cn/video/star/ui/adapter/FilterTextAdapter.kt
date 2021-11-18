package cn.video.star.ui.adapter

import cn.video.star.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class FilterTextAdapter(data: MutableList<String>?) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_filter_text_dot, data) {

    var itemCallback = fun(position: Int, type: Int) {}

    override fun convert(helper: BaseViewHolder?, item: String?) {
        helper?.setText(R.id.filter_word, item)
        if (helper?.adapterPosition == data.size - 1) {
            helper?.setVisible(R.id.dot_img, false)
        } else {
            helper?.setVisible(R.id.dot_img, true)
        }
    }
}