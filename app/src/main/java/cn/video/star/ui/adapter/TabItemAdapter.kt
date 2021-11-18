package cn.video.star.ui.adapter

import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import cn.video.star.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder


class TabItemAdapter(data: MutableList<String>) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_tab, data) {

    var itemCallback = fun(_: Int) {}

    var selected = 0

    override fun convert(helper: BaseViewHolder, item: String) {
        val textView = helper.getView<TextView>(R.id.tab_text)
        textView.text = item
        if (selected == helper.adapterPosition) {
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.white))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0f)
            val paint = textView.paint
            paint.isFakeBoldText = true
            textView.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                null,
                mContext.resources.getDrawable(R.drawable.fill_1c73f2_2round, null)
            )
        } else {
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.cF2F2F2))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f)
            val paint = textView.paint
            paint.isFakeBoldText = false
            textView.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                null,
                null
            )
        }
        helper.itemView.setOnClickListener {
            itemCallback(helper.adapterPosition)
        }
    }
}