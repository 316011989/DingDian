package cn.video.star.ui.adapter

import cn.video.star.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class RatioTextAdapter(data: MutableList<String>?) :
        BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_ratio_text, data) {

    var itemCallback = fun(position: Int) {}

    override fun convert(helper: BaseViewHolder, item: String?) {
        helper.setText(R.id.ratio_text, item)
        helper.itemView.setOnClickListener {
            itemCallback(helper!!.adapterPosition)
        }
    }
}