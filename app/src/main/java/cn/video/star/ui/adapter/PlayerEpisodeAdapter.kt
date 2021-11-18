package cn.video.star.ui.adapter

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cn.junechiu.junecore.utils.ScreenUtil
import cn.video.star.R
import cn.video.star.data.remote.model.VideoPlay
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

class PlayerEpisodeAdapter(plays: MutableList<VideoPlay>) :
    BaseQuickAdapter<VideoPlay, BaseViewHolder>(R.layout.item_episode_text, plays) {

    var itemCallback = fun(_: Int) {}

    var selected = 0

    var itemWidth = 0

    init {
        itemWidth =
            ((ScreenUtil.widthPixels - 5 * ScreenUtil.dp2px(9f) - ScreenUtil.dp2px(10f)) / 6.5).toInt()
    }

    override fun convert(helper: BaseViewHolder, item: VideoPlay) {
        val episodeText = helper.getView<TextView>(R.id.episode_text)
        val params = RecyclerView.LayoutParams(itemWidth, itemWidth)
        params.setMargins(0, 0, ScreenUtil.dp2px(9f), 0)
        episodeText.layoutParams = params
        if (selected == helper.adapterPosition) {
            episodeText.text = item.episode.toString()
            item.isPlaying = 1
            episodeText.setTextColor(ContextCompat.getColor(mContext, R.color.main_orange))
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