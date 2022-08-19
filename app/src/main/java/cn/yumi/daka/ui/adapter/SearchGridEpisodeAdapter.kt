package cn.yumi.daka.ui.adapter

import android.widget.FrameLayout
import android.widget.LinearLayout
import cn.yumi.daka.R
import cn.yumi.daka.data.remote.model.VideoPlayD
import cn.junechiu.junecore.utils.ScreenUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class SearchGridEpisodeAdapter(data: MutableList<VideoPlayD>) :
        BaseQuickAdapter<VideoPlayD, BaseViewHolder>(R.layout.item_player_episode, data) {

    var itemWidth = 0

    init {
        itemWidth = (ScreenUtil.widthPixels - 5 * ScreenUtil.dp2px(5f) - 2 * ScreenUtil.dp2px(15f)) / 6
    }

    var itemCallback = fun(position: Int) {}

    override fun convert(helper: BaseViewHolder, item: VideoPlayD) {
        var frameLayout = helper.getView<FrameLayout>(R.id.item_view)
        var params = LinearLayout.LayoutParams(itemWidth, itemWidth)
        frameLayout.layoutParams = params
        helper.setText(R.id.episode_num, (helper.adapterPosition + 1).toString())
        helper.itemView.setOnClickListener {
            itemCallback(helper.adapterPosition)
        }
    }
}