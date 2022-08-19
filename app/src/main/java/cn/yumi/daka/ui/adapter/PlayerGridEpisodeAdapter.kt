package cn.yumi.daka.ui.adapter

import androidx.core.content.ContextCompat
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import  cn.yumi.daka.R
import  cn.yumi.daka.data.remote.model.VideoPlay
import cn.junechiu.junecore.utils.ScreenUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class PlayerGridEpisodeAdapter(plays: MutableList<VideoPlay>, selected: Int) :
    BaseQuickAdapter<VideoPlay, BaseViewHolder>(R.layout.item_player_episode, plays) {

    var itemCallback = fun(_: Int) {}

    var selected = 0

    var itemWidth = 0

    init {
        this.selected = selected
        itemWidth = (ScreenUtil.widthPixels - 5 * ScreenUtil.dp2px(5f) - 2 * ScreenUtil.dp2px(10f)) / 6
    }

    override fun convert(helper: BaseViewHolder, item: VideoPlay) {
        val episodeText = helper.getView<TextView>(R.id.episode_num)
        val frameLayout = helper.getView<FrameLayout>(R.id.item_view)
        val params = LinearLayout.LayoutParams(itemWidth, itemWidth)
        frameLayout.layoutParams = params
        if (selected == helper.adapterPosition) {
            item.isPlaying = 1
            episodeText.text = item.episode.toString()
            episodeText.setTextColor(ContextCompat.getColor(mContext, R.color.c7b50db))
        } else {
            item.isPlaying = 0
            episodeText.text = item.episode.toString()
            episodeText.setTextColor(ContextCompat.getColor(mContext, R.color.c444444))
        }
        helper.itemView.setOnClickListener {
            itemCallback(helper.adapterPosition)
        }

    }
}