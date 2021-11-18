package cn.video.star.ui.adapter

import android.widget.FrameLayout
import android.widget.LinearLayout
import cn.junechiu.junecore.utils.ScreenUtil
import cn.video.star.R
import cn.video.star.data.remote.model.VideoPlayD
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class SearchResultEpisodeAdapter(data: MutableList<VideoPlayD>) :
        BaseQuickAdapter<VideoPlayD, BaseViewHolder>(R.layout.item_episode_search_result, data) {

    var itemWidth = 0

    init {
        itemWidth = (ScreenUtil.widthPixels - 5 * ScreenUtil.dp2px(9f) - 2 * ScreenUtil.dp2px(15f)) / 6
    }

    var itemCallback = fun(position: Int) {}

    var popupCallback = fun() {}

    override fun convert(helper: BaseViewHolder, item: VideoPlayD) {
        val frameLayout = helper.getView<FrameLayout>(R.id.item_view)
        val params = LinearLayout.LayoutParams(itemWidth, itemWidth)
        frameLayout.layoutParams = params
        if (data.size >= 6 && helper.adapterPosition == data.size - 1) {
            helper.setText(R.id.episode_num, "更多")
            helper.itemView.setOnClickListener {
                popupCallback()
            }
        } else {
            helper.setText(R.id.episode_num, (helper.adapterPosition + 1).toString())
            helper.itemView.setOnClickListener {
                itemCallback(helper.adapterPosition)
            }
        }
    }
}