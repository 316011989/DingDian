package cn.video.star.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.junechiu.junecore.anim.viewanimator.ViewAnimator
import cn.junechiu.junecore.utils.SDCardUtils
import cn.junechiu.junecore.utils.ScreenUtil
import cn.video.star.R
import cn.video.star.base.DataInter
import cn.video.star.data.DataRepository
import cn.video.star.data.remote.model.Suggest
import cn.video.star.data.remote.model.VideoData
import cn.video.star.data.remote.model.VideoPlay
import cn.video.star.data.remote.model.VideoSources
import cn.video.star.download.DownloadEntity
import cn.video.star.download.DownloadFeature
import cn.video.star.ui.adapter.*
import cn.video.star.ui.widget.GridSpacingItemDecoration
import cn.video.star.ui.widget.PlayWindowControllerCover
import cn.video.star.ui.widget.SelectRetioPopupWindow
import com.kk.taurus.playerbase.lebo.LeCast
import com.kk.taurus.playerbase.window.FloatWindow
import com.kk.taurus.playerbase.window.FloatWindowParams
import kotlinx.android.synthetic.main.activity_player_window.*
import kotlinx.android.synthetic.main.player_cache_layout.*
import kotlinx.android.synthetic.main.player_cover_layout_cast_list_portrait.*
import org.jetbrains.anko.startActivity


class PlayerUI(private val context: PlayerWindowActivity) {

    private var pullInfoView: View? = null

    private var pullEpisodeView: View? = null

    private var espAdapter: PlayerGridEpisodeAdapter? = null

    var cacheAdapter: HalfCacheAdapter? = null

    var sourcesAdapter: PlayerSourcesAdapter? = null
    var episodeAdapter: PlayerEpisodeAdapter? = null

    var mFloatWindow: FloatWindow? = null

    private var mWindowVideoContainer: FrameLayout? = null


    fun init() {
        initPlayWindow()
    }

    private fun initPlayWindow() {
        val type: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0+
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val width = (ScreenUtil.widthPixels * 0.8f).toInt()
        mWindowVideoContainer = FrameLayout(context)
        val windowParams = FloatWindowParams()
            .setWindowType(type)
            .setX(100)
            .setY(400)
            .setWidth(width)
            .setHeight(width * 9 / 16)
        mFloatWindow = FloatWindow(
            context, mWindowVideoContainer,
            windowParams
        )
        mFloatWindow?.setBackgroundColor(Color.BLACK)
    }

    fun windowPlay() {
        if (checkWindowPermission() && !mFloatWindow?.isWindowShow!!) {
            changeCover(true)
            mFloatWindow?.setElevationShadow(20f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mFloatWindow?.setRoundRectShape(5f)
            mFloatWindow?.show()
            context.mAssist?.attachContainer(mWindowVideoContainer)
            openDesk()
        }
    }

    fun normalPlay() {
        if (mFloatWindow != null && mFloatWindow?.isWindowShow!!) {
            mFloatWindow?.close()
            changeCover(false)
            context.mAssist?.attachContainer(context.videoContainer)
            context.mAssist?.pause()
        }
    }

    private fun changeCover(window: Boolean) {
        if (window) { //小窗
            context.mReceiverGroup?.clearReceivers()
            context.mReceiverGroup?.addReceiver(
                DataInter.ReceiverKey.KEY_CLOSE_COVER,
                PlayWindowControllerCover(context)
            )
        } else {//全屏
            context.addNeedCover()
        }
    }

    private fun openDesk() {
        val i = Intent(Intent.ACTION_MAIN)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        i.addCategory(Intent.CATEGORY_HOME)
        context.startActivity(i)
    }

    private fun checkWindowPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            context.startActivityForResult(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.packageName)
                ), 0
            )
            return false
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    fun setVideoData(videoData: VideoData?) {
        if (videoData != null) {
            context.movieTitle.text = videoData.name
            context.fromText.text = "${videoData.year}  ${videoData.esTags}"
            if (!TextUtils.isEmpty(videoData.detail.summary)) {
                context.movieInfo.text = videoData.detail.summary
            } else {
                context.movieInfo.visibility = View.GONE
            }
            context.episodeCount.text = videoData.updateText
        }
    }


    /**
     * 来源listview
     */
    fun setVideoSources(sources: List<VideoSources>) {
        context.resourceListView.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )
        sourcesAdapter = PlayerSourcesAdapter(sources)
        context.resourceListView.adapter = sourcesAdapter
        sourcesAdapter?.selected = PlayerHelper.CHOOSEN_SOURCE_INDEX
        context.resourceListView.scrollToPosition(sourcesAdapter!!.selected)
        sourcesAdapter!!.itemCallback = { position ->
            if (sources[position].plays.isNotEmpty()) {
                val oldPositon = sourcesAdapter!!.selected
                sourcesAdapter?.selected = position
                sourcesAdapter?.notifyItemChanged(oldPositon)
                sourcesAdapter?.notifyItemChanged(position)
                PlayerHelper.CHOOSEN_SOURCE_INDEX = position //播放器选中来源
                moreThan100(sources[position])
                context.changeResource()
            }
        }
        moreThan100(sources[PlayerHelper.CHOOSEN_SOURCE_INDEX])
    }

    private fun moreThan100(source: VideoSources) {
        if (source.total > 100) {
            DataRepository.instance.videoEpisodes(source.videoId).observe(context) {
                context.videoData?.plays = it?.data
                initEpisodeListView(it?.data!!)
            }
        } else {
            context.videoData?.plays = source.plays.toMutableList()
            initEpisodeListView(source.plays.toMutableList())
        }
    }

    /**
     * 初始化集列表
     */
    private fun initEpisodeListView(plays: MutableList<VideoPlay>) {
        context.episodeListView.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )
        episodeAdapter = PlayerEpisodeAdapter(plays)
        context.episodeListView.adapter = episodeAdapter
        episodeAdapter?.selected = PlayerHelper.CHOOSEN_EPISODE_INDEX

        context.episodeListView.scrollToPosition(episodeAdapter!!.selected)
        episodeAdapter!!.itemCallback = { position ->
            context.changeEsp(position)
        }
        initPullEpisodeView(context.videoData!!) { position -> context.changeEsp(position) }//选集popwindow
        initPullCacheView(context.videoData!!) { context.mAssist?.resume() }//缓存popwindow
    }


    //推荐
    fun initRecommendListView(data: MutableList<Suggest>, recommendCall: (position: Int) -> Unit) {
        context.recommendListView.layoutManager = GridLayoutManager(context, 3)
        context.recommendListView.addItemDecoration(
            GridSpacingItemDecoration.newBuilder()
                .includeEdge(false).horizontalSpacing(ScreenUtil.dp2px(8.5f))
                .verticalSpacing(0)
                .build()
        )
        val adapter = PlayerRecommendAdapter(data)
        context.recommendListView.adapter = adapter
        adapter.itemCallback = { position ->
            recommendCall(position)
        }
    }

    fun initPullInfoView(videoData: VideoData) {
        pullInfoView = context.layoutInflater.inflate(R.layout.player_info_layout, null)
        if (videoData != null) {
            pullInfoView?.findViewById<TextView>(R.id.movie_title)?.text = videoData.name
            if (videoData!!.detail != null) {
                var detail = videoData!!.detail
                pullInfoView?.findViewById<TextView>(R.id.play_num)?.text =
                    "(${detail.episodeNum}全)"
                if (TextUtils.isEmpty(detail.director)) {
                    pullInfoView?.findViewById<RelativeLayout>(R.id.director_lay)?.visibility =
                        View.GONE
                } else {
                    pullInfoView?.findViewById<TextView>(R.id.directorText)?.text = detail.director
                }
                if (TextUtils.isEmpty(detail.actor)) {
                    pullInfoView?.findViewById<RelativeLayout>(R.id.mainRole_lay)?.visibility =
                        View.GONE
                } else {
                    pullInfoView?.findViewById<TextView>(R.id.mainRoleText)?.text = detail.actor
                }
                if (TextUtils.isEmpty(detail.tagText)) {
                    pullInfoView?.findViewById<RelativeLayout>(R.id.category_lay)?.visibility =
                        View.GONE
                } else {
                    pullInfoView?.findViewById<TextView>(R.id.categoryText)?.text = detail.tagText
                }
                if (!TextUtils.isEmpty(detail!!.summary)) {
                    pullInfoView?.findViewById<TextView>(R.id.infoText)?.text = detail!!.summary
                } else {
                    pullInfoView?.findViewById<TextView>(R.id.infoText)?.visibility = View.GONE
                }
            }
        }
        pullInfoView?.findViewById<RelativeLayout>(R.id.closeBtn)?.setOnClickListener {
            closePlayerContent()
        }
    }

    fun initPullEpisodeView(
        videoData: VideoData,
        changeEspCallback: (position: Int) -> Unit,
    ) {
        pullEpisodeView = context.layoutInflater.inflate(R.layout.player_episode_layout, null)
        if (videoData.plays != null && videoData.plays!!.isNotEmpty()) {
            pullEpisodeView?.findViewById<TextView>(R.id.movie_title)?.text = videoData.name
            pullEpisodeView?.findViewById<TextView>(R.id.movie_count)?.text =
                "(${videoData.plays!!.size}全)"

            val episodeListView = pullEpisodeView?.findViewById<RecyclerView>(R.id.episodeList)
            episodeListView?.layoutManager = GridLayoutManager(context, 6)
            episodeListView?.addItemDecoration(
                GridSpacingItemDecoration.newBuilder()
                    .includeEdge(false).horizontalSpacing(ScreenUtil.dp2px(5f))
                    .verticalSpacing(ScreenUtil.dp2px(5f))
                    .build()
            )
            espAdapter =
                PlayerGridEpisodeAdapter(videoData.plays!!, PlayerHelper.CHOOSEN_EPISODE_INDEX)

            //选集
            episodeListView?.adapter = espAdapter
            episodeListView?.scrollToPosition(PlayerHelper.CHOOSEN_EPISODE_INDEX)

            espAdapter?.notifyDataSetChanged()
            espAdapter?.itemCallback = { position ->
                changeEspCallback(position)
                espAdapter?.selected = position
                espAdapter?.notifyDataSetChanged()
                closePlayerContent()
            }
        }
        pullEpisodeView?.findViewById<RelativeLayout>(R.id.closeBtn)?.setOnClickListener {
            closePlayerContent()
        }
    }

    fun initPullCacheView(videoData: VideoData, resumePlay: () -> Unit) {
        //屏蔽view层事件传递
        context.cacheInfoLayout.setOnTouchListener { _, _ ->
            true
        }
        //设置距离顶部距离
        (context.cacheInfoLayout.layoutParams as RelativeLayout.LayoutParams).setMargins(
            0, context.getStatusBarHeight(), 0, 0
        )

        //缓存可选集数
        if (videoData.plays != null && videoData.plays!!.size > 0) {
            context.cacheList.layoutManager = GridLayoutManager(context, 6)
            context.cacheList.addItemDecoration(
                GridSpacingItemDecoration.newBuilder()
                    .includeEdge(false).horizontalSpacing(ScreenUtil.dp2px(5f))
                    .verticalSpacing(ScreenUtil.dp2px(5f))
                    .build()
            )
            //缓存可选清晰度
            if (context.playerHelper!!.clarityCacheList.size > 0) {
                context.resolutionLay.visibility = View.VISIBLE
                val popup = SelectRetioPopupWindow(context)
                popup.setRatio(context.playerHelper!!.clarityCacheList)
                popup.selectCallback = { clarity ->
                    //显示选中清晰度
                    context.cache_clarity.text = clarity.text
                    //记录选中缓存任务清晰度
                    PlayerHelper.CACHE_CLARITY = clarity.id
                    popup.dismiss()
                }
                context.resolutionLay.setOnClickListener {
                    popup.showPopupWindow()
                }
            }
        }
        //关闭缓存选集页
        context.closeBtn.setOnClickListener {
            val endP = ScreenUtil.heightPixels.toFloat()
            popAnim(context.cacheInfoLayout, 0f, endP, 250, context.movieHanle) {
                context.cacheInfoLayout.visibility = View.GONE
                scrollViewToTop()
                resumePlay()
            }
        }

        //缓存全部
        context.cacheAll.visibility = View.GONE //0全选  1取消全选
        context.cacheAll.setOnClickListener {
            if (context.cacheAll.tag.toString().toInt() == 0) {
                cacheAdapter?.isEdit = true
                context.cacheAll.tag = 1
                context.cacheAll.text = context.getString(R.string.cancel)
                context.cacheAll.setTextColor(ContextCompat.getColor(context, R.color.cFF6600))
                for (video in videoData.plays!!) {
                    //插数据库.并启动任务
                    DownloadFeature.addDownloadTask2DB(videoData, video)
                    //刷新adapter
                    makeEpisodeCacheAdapter(videoData)
                }
            } else {
                cacheAdapter?.isEdit = false
                context.cacheAll.tag = 0
                context.cacheAll.text = context.getString(R.string.select_all)
                context.cacheAll.setTextColor(ContextCompat.getColor(context, R.color.c444444))
            }
        }
        //查看缓存
        context.lookCacheLayout.setOnClickListener {
            context.startActivity<CacheActivity>()
        }

        makeEpisodeCacheAdapter(videoData)
    }

    private fun makeEpisodeCacheAdapter(videoData: VideoData?) {
        DownloadFeature.queryAllEpisodesBySeriesId(videoData?.id!!) {
            context.runOnUiThread {
                val downloadingIds = mutableListOf<String>()
                val downloadedIds = mutableListOf<String>()
                if (it != null)
                //可缓存集数gridview
                    for (t in it) {
                        if (t.downloadStatus == DownloadEntity.state_success)
                            downloadedIds.add(t.episodeId.toString())
                        else
                            downloadingIds.add(t.episodeId.toString())
                    }
                cacheAdapter = HalfCacheAdapter(
                    videoData, downloadingIds, downloadedIds, context.cacheNum
                )
                val cacheListView =
                    context.cacheInfoLayout.findViewById<RecyclerView>(R.id.cacheList)
                cacheListView.layoutManager = GridLayoutManager(context, 5)
                cacheListView.adapter = cacheAdapter
                context.cacheNum.text = it?.size.toString()
            }
        }
    }


    //简介
    fun showInfoView() {
        context.cacheInfoLayout.visibility = View.GONE
        context.playerContent.visibility = View.VISIBLE
        context.playerContent.removeAllViews()
        if (pullInfoView != null) {
            context.playerContent.addView(pullInfoView)
        }
        val startP = ScreenUtil.dp2px(328f).toFloat()
        closeMovieHanle(startP)
    }

    //显示选集列表
    fun showEpisodeListView() {
        context.cacheInfoLayout.visibility = View.GONE
        context.playerContent.visibility = View.VISIBLE
        context.playerContent.removeAllViews()
        if (pullEpisodeView != null) {
            espAdapter?.selected = PlayerHelper.CHOOSEN_EPISODE_INDEX
            espAdapter?.notifyDataSetChanged()
            context.playerContent.addView(pullEpisodeView)
        }
        val startP = ScreenUtil.dp2px(328f).toFloat()
        closeMovieHanle(startP)
    }

    //显示缓存列表
    fun showCacheView(pauseVideo: () -> Unit) {
        pauseVideo()
        context.diskCanUse.text =
            context.getString(R.string.can_use_space) + SDCardUtils.byte2FitMemorySize(
                SDCardUtils.getAvailaleSize()
            )
        context.cacheInfoLayout.visibility = View.VISIBLE
        val startP = ScreenUtil.heightPixels.toFloat()
        pushAnim(context.cacheInfoLayout, startP, 0f, 250) {
            scrollViewToTop()
        }
    }


    /**
     * 显示投屏设备列表
     */
    fun showCastLayout(callBack: (deviceIndex: Int) -> Unit) {
        //显示设备列表布局
        context.portraitCastLayout.visibility = View.VISIBLE
        context.portraitCastLayout.setOnClickListener {
            context.portraitCastLayout.visibility = View.GONE
        }
        context.castlist_close.setOnClickListener {
            context.portraitCastLayout.visibility = View.GONE
        }
        //动画弹出效果
        val startP = ScreenUtil.heightPixels.toFloat()
        pushAnim(context.portraitCastLayout, startP, 0f, 250) {
            scrollViewToTop()
        }
        //设备列表适配器
        context.cast_recycler_view.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        var castAdapter = PlayerCoverCastAdapter(context, LeCast.getInstance().infos, -1)
        context.cast_recycler_view.adapter = castAdapter
        castAdapter.setOnItemClicklistener { position, _ ->
            //通知播放器选中投屏设备
            callBack(position)
            //动画弹出效果
            val endP = ScreenUtil.heightPixels.toFloat()
            popAnim(context.portraitCastLayout, 0f, endP, 250, context.movieHanle) {
                context.portraitCastLayout.visibility = View.INVISIBLE
                scrollViewToTop()
            }
        }

        //刷新设备列表
        context.cast_search_tv.setOnClickListener {
            context.cast_searching_tip.visibility = View.VISIBLE
            context.cast_recycler_view.visibility = View.GONE
            val handler = Handler()
            handler.postDelayed(Runnable {
                LeCast.getInstance().browse()//搜索
                context.cast_searching_tip.visibility = View.GONE
                context.cast_recycler_view.visibility = View.VISIBLE
                castAdapter = PlayerCoverCastAdapter(context, LeCast.getInstance().infos, -1)
                context.cast_recycler_view.adapter = castAdapter
                castAdapter.setOnItemClicklistener { position, _ ->
                    //通知播放器选中投屏设备
                    callBack(position)
                    //动画弹出效果
                    val endP = ScreenUtil.heightPixels.toFloat()
                    popAnim(context.portraitCastLayout, 0f, endP, 250, context.movieHanle) {
                        context.portraitCastLayout.visibility = View.INVISIBLE
                        scrollViewToTop()
                    }
                }
            }, 3_000)
        }
        //投屏助手
        context.cast_helper.setOnClickListener {
            context.startActivity(Intent(context, CastHelperActivity::class.java))
            //动画弹出效果
            val endP = ScreenUtil.heightPixels.toFloat()
            popAnim(context.portraitCastLayout, 0f, endP, 250, context.movieHanle) {
                context.portraitCastLayout.visibility = View.INVISIBLE
                scrollViewToTop()
            }
        }
    }

    fun removeCastDeviceList() {
        if (context.portraitCastLayout.visibility == View.VISIBLE) {
            //动画弹出效果
            val endP = ScreenUtil.heightPixels.toFloat()
            popAnim(context.portraitCastLayout, 0f, endP, 250, context.movieHanle) {
                context.portraitCastLayout.visibility = View.INVISIBLE
                scrollViewToTop()
            }
        }
    }

    private fun closeMovieHanle(startP: Float) {
        pushAnim(context.playerContent, startP, 0f, 250) {
            context.movieHanle.visibility = View.GONE
            scrollViewToTop()
        }
    }

    private fun closePlayerContent() {
        val endP = ScreenUtil.dp2px(328f).toFloat()
        popAnim(context.playerContent, 0f, endP, 250, context.movieHanle) {
            context.playerContent.visibility = View.GONE
            scrollViewToTop()
        }
    }

    fun scrollViewToTop() {
        context.bottomScrollLay.postDelayed({
            context.bottomScrollLay.fling(0)
            context.bottomScrollLay.smoothScrollTo(0, 0)
        }, 250)
    }

    //滚动到顶部
    fun scrollTop() {
        context.bottomScrollLay.postDelayed({
            context.bottomScrollLay.visibility = View.VISIBLE
            context.bottomScrollLay.fling(0)
            context.bottomScrollLay.smoothScrollTo(0, 0)
        }, 368)
    }

    private fun pushAnim(
        view: View, startPos: Float, endPos: Float, duration: Long,
        stopListener: () -> Unit,
    ) {
        ViewAnimator
            .animate(view)
            .translationY(startPos, endPos)
            .duration(duration)
            .onStart({})
            .onStop(stopListener).start()
    }

    private fun popAnim(
        view: View, startPos: Float, endPos: Float, duration: Long, movieHanle: View,
        stopListener: () -> Unit,
    ) {
        movieHanle.visibility = View.VISIBLE
        ViewAnimator
            .animate(view)
            .translationY(startPos, endPos)
            .duration(duration)
            .onStart {}
            .onStop(stopListener).start()
    }

}