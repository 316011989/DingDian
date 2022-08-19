package cn.yumi.daka.ui.adapter

import androidx.core.content.ContextCompat
import android.text.TextUtils
import android.widget.TextView
import cn.yumi.daka.R
import cn.yumi.daka.data.remote.model.TypeValue
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.util.regex.Pattern


class MovieFilterValueAdapter(data: MutableList<TypeValue>) :
    BaseQuickAdapter<TypeValue, BaseViewHolder>(R.layout.item_filter_text, data) {

    var itemCallback = fun(_: Int, _: Int, _: Int) {}

    override fun convert(helper: BaseViewHolder, item: TypeValue) {
        val filterWord = helper?.getView<TextView>(R.id.filter_word)
        if (!TextUtils.isEmpty(item.name)) {
            val p = Pattern.compile("\\s*|\t|\r|\n")
            val m = p.matcher(item.name)
            filterWord.text = m.replaceAll("")
        }

        if (item.isSelected) {
            filterWord.setBackgroundResource(R.drawable.fill_f78507_3round)
            filterWord.setTextColor(ContextCompat.getColor(mContext, R.color.white))
        } else {
            filterWord.setBackgroundResource(R.drawable.fill_transparent_3round)
            filterWord.setTextColor(ContextCompat.getColor(mContext, R.color.c444444))
        }

        //点击帅选item
        helper.itemView.setOnClickListener {
            data.forEachIndexed { index, typeValue ->
                typeValue.isSelected = index == helper.adapterPosition
            }
            notifyDataSetChanged()
            itemCallback(item.typeId, item.categoryId, item.id)
        }
    }
}