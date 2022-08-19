package cn.yumi.daka.ui.widget

import android.app.Activity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import cn.yumi.daka.R
import cn.yumi.daka.data.remote.model.VideoPlayD
import cn.yumi.daka.ui.adapter.SearchGridEpisodeAdapter
import cn.junechiu.junecore.utils.ScreenUtil
import cn.junechiu.junecore.widget.popup.BasePopupWindow

/**
 * 选择剧集弹窗
 */
class SelectEpisodePopupWindow(private val instance: Activity) : BasePopupWindow(instance) {

    var episodeListView: RecyclerView? = null

    var movieName: TextView? = null

    var adapter: SearchGridEpisodeAdapter? = null

    var plays = mutableListOf<VideoPlayD>()

    var itemCallback = fun(id: Long, position: Int) {}

    init {
        setPopupAnimaStyle(R.style.PopupAnimation)
    }

    override fun onCreatePopupView(): View {
        return createPopupById(R.layout.select_episode_popup)
    }

    fun initSelfView() {
        movieName = findViewById(R.id.movie_name) as TextView
        episodeListView = findViewById(R.id.episode_list) as RecyclerView
        episodeListView!!.layoutManager = GridLayoutManager(instance, 6)
        episodeListView!!.addItemDecoration(GridSpacingItemDecoration.newBuilder()
                .includeEdge(false).horizontalSpacing(ScreenUtil.dp2px(5f))
                .verticalSpacing(ScreenUtil.dp2px(5f))
                .build())
        adapter = SearchGridEpisodeAdapter(plays)
        episodeListView!!.adapter = adapter
    }

    fun initData(id: Long, videoName: String, plays: MutableList<VideoPlayD>) {
        this.plays.clear()
        this.plays.addAll(plays)
        adapter!!.notifyDataSetChanged()
        movieName!!.text = videoName

        findViewById(R.id.closeBtn).setOnClickListener {
            dismiss()
        }

        adapter!!.itemCallback = { position ->
            itemCallback(id, position)
            dismiss()
        }
    }

    override fun initAnimaView(): View? {
        return null
    }

    override fun initShowAnimation(): Animation? {
        return null
    }

    override fun getClickToDismissView(): View {
        return popupWindowView
    }

    override fun showPopupWindow(v: View) {
        offsetX = (v.width - width) / 2
        offsetY = 0
        super.showPopupWindow(v)
    }

}