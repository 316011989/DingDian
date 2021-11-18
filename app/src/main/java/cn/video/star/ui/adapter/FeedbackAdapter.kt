package cn.video.star.ui.adapter

import android.widget.RelativeLayout
import android.widget.TextView
import cn.video.star.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class FeedbackAdapter(data: MutableList<String>?) :
        BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_feedback_text, data) {

    var itemCallback = fun(position: Int) {}

    var selected = 0

    override fun convert(helper: BaseViewHolder?, item: String?) {
        var textView = helper!!.getView<TextView>(R.id.feedback_text)
        textView!!.text = item

        var itemView = helper!!.getView<RelativeLayout>(R.id.item_view)
        if (selected == helper!!.adapterPosition) {
            textView.setTextColor(mContext.resources.getColor(R.color.cffffff))
            itemView.setBackgroundResource(R.drawable.oval_fill_ff6600)
        } else {
            textView.setTextColor(mContext.resources.getColor(R.color.c888888))
            itemView.setBackgroundResource(R.drawable.oval_fill_f2f2f2)
        }
        itemView!!.setOnClickListener {
            itemCallback(helper!!.adapterPosition)
        }
    }
}