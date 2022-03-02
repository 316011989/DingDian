package  cn.video.star.ui.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.database.ContentObserver
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import cn.junechiu.junecore.utils.ALogger
import cn.junechiu.junecore.utils.DeviceUtils
import cn.video.star.R
import cn.video.star.base.Api
import cn.video.star.base.App
import cn.video.star.base.BaseActivity
import cn.video.star.base.DataInter
import cn.video.star.base.DataInter.Key.*
import cn.video.star.base.DataInter.ReceiverKey.*
import cn.video.star.data.DataRepository
import cn.video.star.data.local.db.AppDatabaseManager
import cn.video.star.data.remote.model.*
import cn.video.star.download.DownloadFeature
import cn.video.star.download.DownloadFileUtil
import cn.video.star.ui.widget.*
import cn.video.star.update.NetworkStatusReceiver
import cn.video.star.utils.ConfigCenter
import cn.video.star.viewmodel.PlayerViewModel
import com.blankj.utilcode.util.LogUtils
import com.kk.taurus.playerbase.assist.AssistPlay
import com.kk.taurus.playerbase.assist.InterEvent
import com.kk.taurus.playerbase.assist.OnAssistPlayEventHandler
import com.kk.taurus.playerbase.assist.RelationAssist
import com.kk.taurus.playerbase.entity.DataSource
import com.kk.taurus.playerbase.event.OnErrorEventListener
import com.kk.taurus.playerbase.event.OnPlayerEventListener
import com.kk.taurus.playerbase.lebo.LeCast
import com.kk.taurus.playerbase.player.IPlayer
import com.kk.taurus.playerbase.receiver.ReceiverGroup
import com.kk.taurus.playerbase.render.AspectRatio
import com.kk.taurus.playerbase.utils.VideoUtil
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import kotlinx.android.synthetic.main.activity_player_window.*
import kotlinx.android.synthetic.main.player_cache_layout.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class PlayerWindowActivity : BaseActivity(), OnPlayerEventListener, OnErrorEventListener {
    private var ffChannel: MethodChannel? = null//flutter 播放解析插件
    private var playStart = 0L //播放跳过
    private var movieId: Long = 0//要播放的movieId
    private var episodeId = 0L //本地播放videoId

    var model: PlayerViewModel? = null

    var playerHelper: PlayerHelper? = null
    var playerUI: PlayerUI? = null
    var videoData: VideoData? = null//剧
    var videoPlay: VideoPlay? = null//集
    private var suggests: MutableList<Suggest>? = null

    private var userPause = false
    var isLandscape = false
    var mReceiverGroup: ReceiverGroup? = null
    var dataSource: DataSource? = null
    private var rotationObserver: ContentObserver? = null

    var mAssist: RelationAssist? = null
    private var enterTime = 0L//进入播放页计时
    private var networkChangedReceiver: NetworkStatusReceiver? = null

    private var interactionShowing: Boolean = false//广告显示状态

    var handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                PlayerHelper.MESSAGE_TYPE_PLAY -> {
                    mReceiverGroup?.groupValue!!.putBoolean(KEY_SHOW_LOADING, false)
                    val url = msg.obj.toString()
                    dataSource?.data = url
                    dataSource?.clarities?.clear()
                    //清晰度处理
                    if (playerHelper?.clarityList!!.size > 0)
                        playerHelper?.clarityList!!.forEach { rate ->
                            dataSource?.clarities?.add("${rate.id}&${rate.text}&${rate.pre}")
                        }
                    mAssist?.setDataSource(dataSource)
                    startPlayVideo()
                    reCast()
                }
            }
        }
    }


    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_player_window
    }

    override fun initData(savedInstanceState: Bundle?) {
        initFlutterPlugin()

        enterTime = System.currentTimeMillis()//启动播放页时间,用于进入后台计时1分钟以上显示广告
        LeCast.getInstance().browse()//搜索
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)  //屏幕常亮
        setStatusBar(Color.BLACK, false)
        App.INSTANCE.inPlayerActivity = true//进入播放页面,用于记录后台进入前台时的页面,播放页不启动开屏广告

        //进入播放页传递的数据,需要播放的集数等
        movieId = intent.extras?.get("id") as Long
        //先查询播放历史记录
        AppDatabaseManager.dbManager.queryHistoryMovieById(movieId) { movie ->
            //存在历史记录
            if (movie != null) {
                PlayerHelper.CHOOSEN_SOURCE_INDEX = movie.source
                PlayerHelper.CHOOSEN_EPISODE_INDEX = movie.playIndex
                playStart = movie.position
            } else {
                PlayerHelper.CHOOSEN_SOURCE_INDEX = 0
                PlayerHelper.CHOOSEN_EPISODE_INDEX = 0
                playStart = 0
            }
            //指定播放集数
            if (intent.hasExtra("esp"))
                PlayerHelper.CHOOSEN_EPISODE_INDEX = intent.extras?.get("esp") as Int//指定集
            //指定集id(本地缓存的视频才有)
            if (intent.getLongExtra("episodeId", 0) != 0L)
                episodeId = intent.getLongExtra("episodeId", 0)

            initPlayerUI()//播放器UI,非播放器UI
            initAssistAndCover()//播放内容和播放器蒙层
            subscribeUi(movieId)//请求数据
            listenNetwork()
        }
    }


    private fun initFlutterPlugin() {
        App.INSTANCE.flutterEngine?.navigationChannel?.setInitialRoute("play")
        App.INSTANCE.flutterEngine?.dartExecutor?.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )

        ffChannel = MethodChannel(
            App.INSTANCE.flutterEngine?.dartExecutor?.binaryMessenger, "com.example.message/mm1"
        )

        ffChannel?.setMethodCallHandler { methodCall, result ->
            when (methodCall.method) {
                "jx" -> {
                    if (methodCall.arguments != null) {
                        val playUrl = methodCall.argument<String>("playUrl")
                        val headers = methodCall.argument<String>("headers")
                        dataSource?.data = playUrl
                        dataSource?.extra?.clear()
                        if (!headers.isNullOrEmpty()) {
                            val json = JSONObject(headers)
                            json.keys().forEach { key ->
                                dataSource?.extra?.put(key, json.get(key).toString())
                            }
                        }
                        sendUrlToPlayer(playUrl!!)
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    /**
     *  初始化数据源和播放器布局控件等
     */
    private fun initAssistAndCover() {
        updateVideoLayout()
        dataSource = DataSource()
        dataSource?.extra = HashMap()
        dataSource?.espList = ArrayList()
        mReceiverGroup = ReceiverGroup(null)
        addNeedCover()//默认添加半屏cover
        mAssist = RelationAssist(this)
        mAssist?.superContainer?.setBackgroundColor(Color.BLACK)
        mAssist?.setEventAssistHandler(onVideoViewEventHandler)
        mAssist?.receiverGroup = mReceiverGroup
        mAssist?.setOnPlayerEventListener(this)
        mAssist?.setOnErrorEventListener(this)
        mAssist?.attachContainer(videoContainer)
        if (episodeId != 0L)
            checkLocalFile(episodeId)
    }


    /**
     * 本地有数据直接播放
     */
    private fun checkLocalFile(epsId: Long) {
        DownloadFeature.queryEpisodeByEpisodeId(epsId) { video ->
            if (video != null)
                runOnUiThread {
                    var localFile = DownloadFileUtil.getM3u8FileById(epsId.toString())
                    if (localFile == null || !localFile.exists())
                        localFile = DownloadFileUtil.getMp4FileById(epsId.toString())
                    if (localFile != null && localFile.exists()) {
                        //播放本地视频不加loading状态ui
                        mReceiverGroup?.groupValue!!.putBoolean(KEY_NETWORK_RESOURCE, false)
                        progressView.visibility = View.INVISIBLE
                        mReceiverGroup?.removeReceiver(KEY_ERROR_COVER)
                        mReceiverGroup?.removeReceiver(KEY_LOADING_COVER)

                        movieTitle.text = "${video.seriesName} ${video.episodeName}"
                        dataSource?.id = video.seriesId ?: 0 //存储videoId
                        dataSource?.title = video.seriesName + video.episodeName
                        dataSource?.data = App.INSTANCE.createWifiUrl(LeCast.getInstance().wifiIp,
                            localFile.path)
                        dataSource?.from = video.source!!
                        mAssist?.setDataSource(dataSource)
                        mAssist?.play()
                    }
                }
        }
    }

    private fun updateVideoLayout() {
        val layoutParams = videoContainer.layoutParams as RelativeLayout.LayoutParams
        if (isLandscape) {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT//以宽为基准,宽度占满屏幕
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.setMargins(0, 0, 0, 0)
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT//以宽为基准,宽度占满屏幕
            layoutParams.height = VideoUtil.getScreenWidthPix() * 9 / 16 + 25
            layoutParams.setMargins(0, getStatusBarHeight(), 0, 0)
        }
        videoContainer.layoutParams = layoutParams
        if (mAssist != null) {
            val ratioSource = mAssist!!.mVideoWidth.toDouble() / mAssist!!.mVideoHeight.toDouble()
            Log.i("updateVideoLayout", "原视频宽高比$ratioSource")
            val ratioLayout: Double
            if (isLandscape) {
                ratioLayout =
                    VideoUtil.getScreenWidthPix().toDouble() / VideoUtil.getScreenHeightPix()
                        .toDouble()
                Log.i("updateVideoLayout", "全屏屏幕宽高比$ratioLayout")
            } else {
                ratioLayout = 16.toDouble() / 9.toDouble()
                Log.i("updateVideoLayout", "16:9宽高比$ratioLayout")
            }
            if (ratioSource > ratioLayout) {
                mAssist!!.setAspectRatio(AspectRatio.AspectRatio_FILL_WIDTH)
            } else {
                mAssist!!.setAspectRatio(AspectRatio.AspectRatio_FILL_HEIGHT)
            }
        }
    }

    /**
     * 播放页activity的辅助类,解析辅助和UI辅助
     */
    private fun initPlayerUI() {
        playerUI = PlayerUI(this)
        playerUI?.init()
        setListeners()//非播放器UI
    }


    /**
     * 播放器上的UI控件
     */
    private fun setListeners() {
        playerContent.visibility = View.GONE
        //屏蔽view层事件传递
        playerContent.setOnTouchListener { _, _ -> true }
        movieToInfo.setOnClickListener { playerUI?.showInfoView() } //简介
        movieInfo.setOnClickListener { playerUI?.showInfoView() } //简介
        episodeLay.setOnClickListener { playerUI?.showEpisodeListView() } //选集
        likeButton.setOnClickListener { setFollow() }//喜欢
        AppDatabaseManager.dbManager.queryCollectByMovieId(movieId) {
            runOnUiThread {
                if (it != null && it.size > 0)
                    likeButtonIcon.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(this, R.mipmap.liked_img),
                        null, null, null
                    )
                else
                    likeButtonIcon.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(this, R.mipmap.like_movie_icon),
                        null, null, null
                    )
            }
        }
        downButton.setOnClickListener { playerUI?.showCacheView { pauseVideo() } }//缓存
        //半屏分享
        shareButton.setOnClickListener {
            DeviceUtils.showSystemShareOption(
                this,
                "追剧，有玉米电影APP就够了",
                ConfigCenter.contactWay?.webSite ?: Api.SHARE_BASE_URL
            )
        }
    }

    private fun subscribeUi(id: Long) {
        progressView.visibility = View.VISIBLE
        val factory = PlayerViewModel.Factory(id, App.INSTANCE)
        model = ViewModelProviders.of(this, factory).get(PlayerViewModel::class.java)
        model?.getVideoDetail()?.observe(this) { videoDetail ->
            model?.getVideoSourcePlays()?.observe(this) { videoSources ->
                setDetailData(videoDetail.data)
                setSourcesData(videoSources?.data)
                progressView.visibility = View.GONE
            }
        }
        model?.getVideoSuggest()?.observe(this) { videoSuggest ->
            setVideoSuggest(videoSuggest.data)
        }
    }

    fun checkOrientation() {
        val flag =
            Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0)
        if (flag == 1 && PlayerHelper.LOCK_SCREEN != 1) { //自动旋转 并且没有锁屏
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        } else {
            if (isLandscape) {
                setScreenLandscape()
            } else {
                setScreenPortrait()
            }
        }
    }

    //设置详情数据
    private fun setDetailData(data: VideoData?) {
        //网络请求到videodata,先处理UI
        if (data != null) {
            videoData = data
            playerUI?.setVideoData(videoData)
            //剧集显示隐藏
            dataSource?.type = data.type
            dataSource?.from = data.source
            if (data.type == Api.TYPE_MOVIE) {
                episodeLayout.visibility = View.GONE
            } else {
                episodeLayout.visibility = View.VISIBLE
            }
            playerUI?.initPullInfoView(data)//影片信息popwindow
        }
        playerUI?.scrollTop()
    }

    /**
     * 播放源,每个源对应可选集
     */
    private fun setSourcesData(sourcesData: List<VideoSources>?) {
        if (sourcesData != null && sourcesData.isNotEmpty()) {
            //指定播放源(指定的源的名称,转换为源的下标)
            if (intent.hasExtra("src")) {
                sourcesData.forEachIndexed { index, videoSources ->
                    if (videoSources.source == intent.extras?.get("src").toString()) {
                        PlayerHelper.CHOOSEN_SOURCE_INDEX = index
                    }
                }
            }

            //获取目标videoplay
            videoPlay =
                sourcesData[PlayerHelper.CHOOSEN_SOURCE_INDEX].plays[PlayerHelper.CHOOSEN_EPISODE_INDEX]
            //目标videoplay不存在,设置默认的videoplay
            if (videoPlay == null) videoPlay = sourcesData[0].plays[0]
            playerHelper = PlayerHelper(videoData!!)
            //playerHelper更新cover的可选清晰度
            playerHelper?.addCacheRate()
            playerUI?.setVideoSources(sourcesData)
            dataSource?.espList?.addAll(videoData!!.plays!!)
            if (episodeId == 0L)//非本地缓存视频
                checkLocalFile(videoPlay!!.videoId)
        } else if (videoData!!.plays != null && videoData!!.plays!!.isNotEmpty()) {
            //获取目标videoplay
            videoPlay = videoData!!.plays!![PlayerHelper.CHOOSEN_EPISODE_INDEX]
            //目标videoplay不存在,设置默认的videoplay
            if (videoPlay == null) videoPlay = videoData!!.plays!![0]
            playerHelper = PlayerHelper(videoData!!)
            //playerHelper更新cover的可选清晰度
            playerHelper?.addCacheRate()
            dataSource?.espList?.addAll(videoData!!.plays!!)
            if (episodeId == 0L)//非本地缓存视频,指定播放集
                checkLocalFile(videoPlay!!.videoId)
        } else if (!TextUtils.isEmpty(videoData!!.sourceUrl)) {
            val play = VideoPlay()
            play.playUrl = videoData!!.sourceUrl
            play.source = videoData!!.source
            play.sourceIsVip = videoData!!.sourceIsVip
            play.rate = videoData!!.rate
            videoPlay = play
        } else {
            checkLocalFile(videoPlay!!.videoId)
        }

        when {
            episodeId != 0L ->//本地有数据,已经在subscribeUi之前启动播放了
                return
            videoPlay != null ->  //本地没有数据,网络有数据
            { //请求广告
                requestAllAd()
                setPlayUrl()
            }
            else ->  //本地无数据并且网络请求失败,提示错误
                showResourceState(true, getString(R.string.video_adding))
        }
    }

    /**
     * 推荐其他影视剧
     */
    private fun setVideoSuggest(suggests: MutableList<Suggest>) {
        if (suggests.size > 0) {
            this.suggests = suggests
            playerUI?.initRecommendListView(suggests) { position ->
                mAssist?.stop()
                bottomScrollLay.visibility = View.GONE
                model?.setVideoId(suggests[position].id)
            }
        }
    }


    //是否找到视频资源
    private fun showResourceState(show: Boolean, error: String) {
        if (show) {
            mReceiverGroup?.groupValue!!.putBoolean(KEY_SHOW_LOADING, false) //隐藏loading状态
            noEspText.text = error
            noEspText.visibility = View.VISIBLE
        } else {
            noEspText.visibility = View.GONE
        }
    }

    //获取play url
    private fun setPlayUrl() {
        mReceiverGroup?.groupValue!!.putBoolean(KEY_SHOW_LOADING, true) //loading状态
        dataSource?.from =
            if (videoPlay?.source == null) videoData!!.source else videoPlay?.source!!
        mAssist?.setSpeed(1.0f)
        //如果是剧集显示第几集
        if (videoData!!.type == Api.TYPE_ESP || videoData!!.type == Api.TYPE_DM) {
            dataSource?.title = "${videoData!!.name}   第${videoPlay!!.episode}集"
        } else if (videoData!!.type == Api.TYPE_ZY) {
            dataSource?.title = "${videoData!!.name}   第${videoPlay!!.episode}期"
        } else {
            dataSource?.title = videoData!!.name
        }
        dataSource?.id = videoPlay!!.id //存储videoId
        playerHelper?.setPlayUrl(videoPlay!!, {
            val args = "${it.id} ${it.playUrl} ${it.source} ${App.INSTANCE.versionName}"
            runOnUiThread { ffChannel?.invokeMethod("play", args) }
        }, {
            sendUrlToPlayer(it)
        })
    }


    //发送播放url到播放器
    private fun sendUrlToPlayer(url: String) {
        val message = Message()
        message.what = PlayerHelper.MESSAGE_TYPE_PLAY
        if (videoData!!.source == PlayerHelper.SOURCE_BILIBILI) {
            message.obj = url
            handler.sendMessage(message)
            playCount(videoData!!.id, videoPlay!!.id)//接口调用错误,20190426参数互换位置,20210127又换回来
        } else
            playerHelper?.checkPlayUrl(url) { newUrl ->
                if (!TextUtils.isEmpty(newUrl)) {
                    message.obj = newUrl
                } else {
                    message.obj = url
                }
                handler.sendMessage(message)
                playCount(videoData!!.id, videoPlay!!.id)//接口调用错误,20190426参数互换位置,20210127又换回来
            }
    }

    //seek到新的进度
    private fun startPlayVideo() {
        if (playStart != 0L && playStart > 0) {
            mAssist?.rePlay(playStart.toInt())
        } else {
            mAssist?.play()
        }
    }


    //关注结果
    private fun setFollow() {
        AppDatabaseManager.dbManager.queryCollectByMovieId(movieId) {
            if (it != null && it.size > 0) {
                //收藏过,删除
                AppDatabaseManager.dbManager.deleteCollectByMovieId(movieId) {
                    runOnUiThread {
                        //删除成功
                        likeButtonIcon.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(this, R.mipmap.like_movie_icon),
                            null, null, null
                        )
                    }
                }
            } else {
                //未收藏,添加
                playerHelper?.insertCollect(
                    mAssist?.currentPosition!!.toLong(),
                    getPlayPercent()
                )
                runOnUiThread {
                    likeButtonIcon.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(this, R.mipmap.liked_img),
                        null, null, null
                    )
                }
            }
        }
    }


    /**
     * 播放下一集
     */
    private fun playNext() {
        if (videoData!!.type == Api.TYPE_MOVIE) { //如果是电影类型 播放推荐
            if (suggests != null && suggests!!.size > 0) {
                bottomScrollLay.visibility = View.GONE
                val video = suggests!![Random().nextInt(suggests!!.size)]
                model?.setVideoId(video.id)
            }
        } else {
            var nextPosition = PlayerHelper.CHOOSEN_EPISODE_INDEX + 1
            if (videoData!!.plays != null && videoData!!.plays!!.size <= nextPosition) {
                nextPosition = 0
            }
            changeEsp(nextPosition)
        }
    }

    /**
     * 切换剧集操作
     */
    fun changeResource() {
        mAssist!!.pause()
        when {
            videoData?.plays == null -> return
            videoData?.plays?.get(PlayerHelper.CHOOSEN_EPISODE_INDEX) != null -> {
                videoPlay = videoData?.plays?.get(PlayerHelper.CHOOSEN_EPISODE_INDEX)
            }
            else -> {
                PlayerHelper.CHOOSEN_EPISODE_INDEX = 0
                videoPlay = videoData?.plays?.get(PlayerHelper.CHOOSEN_EPISODE_INDEX)
            }
        }
        setPlayUrl()
    }

    /**
     * 切换剧集操作
     */
    fun changeEsp(position: Int) {
        requestAllAd()
        mAssist!!.pause()
        val oldPostion = playerUI?.episodeAdapter?.selected
        playerUI?.episodeAdapter?.selected = position
        episodeListView.scrollToPosition(position)
        playerUI?.episodeAdapter?.notifyItemChanged(position)
        playerUI?.episodeAdapter?.notifyItemChanged(oldPostion!!)

        videoPlay = videoData!!.plays?.get(position)!!
        PlayerHelper.CHOOSEN_EPISODE_INDEX = position
        playStart = 0
        setPlayUrl()
    }

    /**
     * 重新投屏
     */
    private fun reCast() {
        //投屏状态,更新投屏集数
        if (mReceiverGroup?.getReceiver<PlayCastCoverPortrait>(KEY_CAST_COVER_PORTRAIT) != null) {
            //上次投屏选择的设备
            val index =
                mReceiverGroup?.getReceiver<PlayCastCoverPortrait>(KEY_CAST_COVER_PORTRAIT)?.index
                    ?: 0
            removePortraitCastCover()//移除投屏cover
            addPortraitCastCover(index)//重新投屏
            handler.postDelayed({ mAssist?.pause() }, 3000)
        } else if (mReceiverGroup?.getReceiver<PlayCastCoverLandscape>(KEY_CAST_COVER) != null) {
            val index =
                mReceiverGroup?.getReceiver<PlayCastCoverLandscape>(KEY_CAST_COVER)?.index
                    ?: 0//上次投屏选择的设备
            removeCastCover()//移除投屏cover
            addCastCover(index)//重新投屏
            handler.postDelayed({ mAssist?.pause() }, 3000)
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        playerUI?.scrollTop()
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true
            hideSystemUI()
        } else {
            isLandscape = false
            showSystemUIDelay()
            showNaviga()
        }
        checkOrientation()
        updateVideoLayout()
        addNeedCover()
    }

    private fun showSystemUIDelay() {
        videoContainer.postDelayed({
            showSystemUI()
            setStatusBar(Color.BLACK, false)
        }, 200)
    }


    fun setScreenLandscape() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        updateVideoLayout()
    }


    fun setScreenPortrait() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        updateVideoLayout()
    }

    private fun playCount(videoId: Long, videoPlayId: Long) {
        DataRepository.instance.playCount(videoId, videoPlayId)
    }

    override fun onResume() {
        super.onResume()
        playerUI?.normalPlay()
        updateVideoLayout()
        if (playerUI?.cacheAdapter != null)
            playerUI?.cacheAdapter?.notifyDataSetChanged()

        if (mAssist?.state == IPlayer.STATE_PLAYBACK_COMPLETE)
            return
        if (mAssist?.isInPlaybackState != null
            && mAssist?.isInPlaybackState!! && PlayerHelper.CAST_SCREEN != 1
        ) {
            mAssist?.resume()
        }
    }

    override fun onPause() {
        if (PlayerHelper.FLOATWINDOW == 0) { //不是点击小窗到桌面的则暂停播放
            pauseVideo()
        } else {
            mAssist?.resume()
        }
        super.onPause()
    }

    private fun pauseVideo() {
        val state = mAssist?.state
        if (state == IPlayer.STATE_PLAYBACK_COMPLETE)
            return
        if (mAssist?.isInPlaybackState!!) {
            mAssist?.pause()
        } else {
            mAssist?.stop()
        }
    }

    override fun onBackPressed() {  //返回键
        if (cacheInfoLayout.visibility == View.VISIBLE) {
            cacheInfoLayout.visibility = View.GONE
            playerUI?.scrollViewToTop()
            mAssist?.resume()
            return
        }
        if (isLandscape) {
            setScreenPortrait()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        try {
            handler.removeMessages(PlayerHelper.MESSAGE_TYPE_PLAY)
            PlayerHelper.SHOW_SPEED = 0
            ALogger.d("clear--", "clearclear")
            contentResolver.unregisterContentObserver(rotationObserver!!)
            LeCast.getInstance().stopBrowse()
            releasePlayer()
            App.INSTANCE.inPlayerActivity = false
            //注销网络监听
            if (networkChangedReceiver != null)
                unregisterReceiver(networkChangedReceiver)
        } catch (e: Exception) {
            releasePlayer()
            e.printStackTrace()
        }
        super.onDestroy()
    }

    private fun releasePlayer() {
        playerHelper?.insertMovieHistory(
            mAssist?.currentPosition!!.toLong(),
            getPlayPercent()
        )
        mAssist?.destroy()
    }

    private fun getPlayPercent(): Int {
        return (mAssist?.currentPosition!! * 100 / if (mAssist?.duration == 0) 1 else mAssist?.duration!!)
    }


    /**
     * 添加必须的cover
     * landscape 横屏
     */
    fun addNeedCover() {
        mReceiverGroup?.clearReceivers()
        mReceiverGroup?.addReceiver(KEY_GESTURE_COVER, PlayGestureCover(this))
        mReceiverGroup?.addReceiver(KEY_ERROR_COVER, PlayErrorCover(this))
        mReceiverGroup?.addReceiver(KEY_LOADING_COVER, PlayLoadingCover(this))
        mReceiverGroup?.groupValue!!.putBoolean(KEY_CONTROLLER_TOP_ENABLE, true) //显示top布局
        if (isLandscape) {//横屏
            mReceiverGroup?.addReceiver(
                KEY_CONTROLLER_COVER,
                PlayLandscapeControllerCover(this)
            )
            checkHasNavigationBar {
                mReceiverGroup?.groupValue!!.putInt(KEY_NAVIGATIONBARHEIGHT, it)
            }
            playerwindow_banner_ad1.visibility = View.GONE
        } else {
            mReceiverGroup?.addReceiver(KEY_HALF_CONTROLLER_COVER,
                PlayPortraitControllerCover(this)
            )
            playerwindow_banner_ad1.visibility = View.VISIBLE
        }

    }

    //添加投屏cover
    fun addCastCover(index: Int) {
        mReceiverGroup?.addReceiver(KEY_CAST_COVER, PlayCastCoverLandscape(this, index))
    }

    //添加竖屏投屏cover
    fun addPortraitCastCover(index: Int) {
        mReceiverGroup?.addReceiver(KEY_CAST_COVER_PORTRAIT, PlayCastCoverPortrait(this, index))
        mReceiverGroup?.removeReceiver(KEY_HALF_CONTROLLER_COVER)
    }

    //移除横屏投屏cover
    fun removeCastCover() {
        mReceiverGroup?.removeReceiver(KEY_CAST_COVER)
    }

    //移除竖屏投屏cover
    fun removePortraitCastCover() {
        mReceiverGroup?.removeReceiver(KEY_CAST_COVER_PORTRAIT)
        mReceiverGroup?.addReceiver(
            KEY_HALF_CONTROLLER_COVER,
            PlayPortraitControllerCover(this)
        )
    }

    fun removeGestureCover() {
        mReceiverGroup?.removeReceiver(KEY_GESTURE_COVER)
    }

    fun addGestureCover() {
        mReceiverGroup?.addReceiver(KEY_GESTURE_COVER, PlayGestureCover(this))
    }

    //接受cover传过来的事件
    private val onVideoViewEventHandler = object : OnAssistPlayEventHandler() {
        override fun onAssistHandle(assist: AssistPlay, eventCode: Int, bundle: Bundle?) {
            super.onAssistHandle(assist, eventCode, bundle)
            when (eventCode) {
                InterEvent.CODE_REQUEST_PAUSE -> {  //暂停
                    userPause = true
                }
                DataInter.Event.EVENT_CODE_REQUEST_BACK -> { //点击返回按钮
                    if (PlayerHelper.FLOATWINDOW == 1) {
                        PlayerHelper.FLOATWINDOW = 0
                        playerUI?.normalPlay()
                    } else {
                        if (isLandscape)
                            setScreenPortrait()
                        else
                            finish()
                    }

                }
                DataInter.Event.EVENT_CODE_REQUEST_TOGGLE_SCREEN -> { //切换横竖屏
                    if (isLandscape) {
                        setScreenPortrait()
                    } else {
                        setScreenLandscape()
                    }
                }
                DataInter.Event.EVENT_CODE_ERROR_SHOW -> { //错误
                    mAssist?.stop()
                }
                DataInter.Event.EVENT_CODE_NEXT_ESP -> { //下一集
                    playNext()
                }
                DataInter.Event.EVENT_CODE_CHANGE_SPEED -> {//改变播放速度
                    mAssist?.setSpeed(bundle?.getFloat(KEY_PLAY_SPEED)!!)
                }
                DataInter.Event.EVENT_CODE_CHANGE_ESP -> {//改变剧集换集
                    changeEsp(bundle?.getInt(KEY_PLAY_ESP)!!)
                }
                DataInter.Event.EVENT_CODE_SHARE_WX -> {//微信分享
                    DeviceUtils.showSystemShareOption(
                        this@PlayerWindowActivity,
                        "追剧，有玉米电影APP就够了",
                        ConfigCenter.contactWay?.webSite ?: Api.SHARE_BASE_URL
                    )
                }
                DataInter.Event.EVENT_CODE_SHARE_CIRCLE -> { //朋友圈分享
                    DeviceUtils.showSystemShareOption(
                        this@PlayerWindowActivity,
                        "追剧，有玉米电影APP就够了",
                        ConfigCenter.contactWay?.webSite ?: Api.SHARE_BASE_URL
                    )
                }
                DataInter.Event.EVENT_CODE_LIKE_VIDEO -> {
                    setFollow()
                }
                DataInter.Event.EVENT_SELECT_CAST_DEVICE -> {//投屏
                    mAssist?.pause()
                    addCastCover(bundle!!.getInt(KEY_CASTDEVICE_INDEX))
                }

                DataInter.Event.EVENT_CODE_CAST_PROTRAIT -> {//竖屏点击投屏事件
                    playerUI!!.showCastLayout { index ->
                        //投屏设备选定事件
                        mAssist?.pause()
                        addPortraitCastCover(index)
                    }
                }
                DataInter.Event.EVENT_SELECT_REMOVE_CAST -> {
                    //移除横屏投屏
                    if (bundle != null) {
                        if (bundle.get(KEY_CAST_ACTION).toString().toInt() == 1) {
                            //换设备 弹出设备列表
                            mReceiverGroup?.groupValue!!.putInt(KEY_CHANGE_CAST, 1)
                        }
                    }
                    mAssist?.resume()
                    removeCastCover()
                }
                DataInter.Event.EVENT_SELECT_REMOVE_CAST_PROTRAIT -> {
                    //移除竖屏投屏
                    mAssist?.resume()
                    removePortraitCastCover()
                }
                DataInter.Event.EVENT_SELECT_REMOVE_CAST_PROTRAIT_LIST -> {
                    //移除竖屏投屏设备列表
                    playerUI?.removeCastDeviceList()
                }
                DataInter.Event.EVENT_SELECT_LOCK_SCREEN -> { //锁屏
                    if (bundle != null) {
                        if (bundle.get(KEY_LOCK_SCREEN).toString().toInt() == 1) {
                            removeGestureCover()
                        } else {
                            addGestureCover()
                        }
                    }
                    checkOrientation()
                }
                DataInter.Event.EVENT_CODE_PLAY_WINDOW -> { //小窗播放
                    PlayerHelper.FLOATWINDOW = 1
                    playerUI?.windowPlay()
                }
                DataInter.Event.EVENT_CODE_CLOSE_WINDOW -> { //关闭小窗
                    PlayerHelper.FLOATWINDOW = 0
                    playerUI?.normalPlay()
                }
                DataInter.Event.EVENT_CODE_RETURN_PLAY -> { //返回全屏
                    PlayerHelper.FLOATWINDOW = 0
                    if (isActivityTop()) {
                        val intent = Intent()
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        intent.component = ComponentName(
                            "cn.yumi.daka",
                            "cn.yumi.daka.ui.activity.PlayerWindowActivity"
                        )
                        startActivity(intent)
                    }
                }
                DataInter.Event.EVENT_CODE_CHANGE_PIC_RATIO -> {//画面比例
                    when (bundle?.getInt(KEY_PICTURE_RATIO)) {
                        0 -> mAssist?.setAspectRatio(AspectRatio.AspectRatio_FILL_PARENT)
                        1 -> mAssist?.setAspectRatio(AspectRatio.AspectRatio_MATCH_PARENT)
                        2 -> mAssist?.setAspectRatio(AspectRatio.AspectRatio_ORIGIN)
                    }
                }
                DataInter.Event.KEY_CHANGE_UI -> {
                    val show = bundle?.getBoolean(KEY_SHOW_HIDE_UI) as Boolean
                    if (show) {
                        showNaviga()
                    } else {
                        hideSystemUI()
                    }
                }
                DataInter.Event.EVENT_CODE_CHANGE_CLARITY -> {//切换清晰度
                    val id = bundle?.getString(KEY_PLAY_CLARITY)
                    if (!TextUtils.isEmpty(id)) {
                        playerHelper?.changeRateUrl(videoPlay!!, id!!) {
                            val args =
                                "${it.id} ${it.playUrl} ${it.source} ${App.INSTANCE.versionName}"
                            runOnUiThread { ffChannel?.invokeMethod("play", args) }
                        }
                    }
                }
            }
        }
    }

    override fun onPlayerEvent(eventCode: Int, bundle: Bundle?) {
        when (eventCode) {
            OnPlayerEventListener.PLAYER_EVENT_ON_PREPARED -> { //视频开始渲染时
                Log.d("PlayerWindowActivity", "视频准备播放,广告展示状态$interactionShowing")
                if (interactionShowing) {
                    mAssist?.pause()
                } else if (mReceiverGroup!!.getReceiver<PlayPortraitControllerCover>(
                        KEY_HALF_CONTROLLER_COVER) != null
                )
                    mReceiverGroup!!.getReceiver<PlayPortraitControllerCover>(
                        KEY_HALF_CONTROLLER_COVER
                    ).setControllerState(true)
            }
            OnPlayerEventListener.PLAYER_EVENT_ON_PLAY_COMPLETE -> {//播放完成继续下一集
                playNext()
            }
            OnPlayerEventListener.PLAYER_EVENT_ON_DATA_RESPONSE_CODE -> { //ts response code not eq 200
            }
        }
    }

    override fun onErrorEvent(eventCode: Int, bundle: Bundle?) {
        LogUtils.d("onErrorEvent--${eventCode}")
        when (eventCode) {
            OnErrorEventListener.ERROR_EVENT_UNKNOWN -> {
                mAssist?.switchDecoder(App.PLAN_ID_IJK)
                sendUrlToPlayer(dataSource?.data!!)
            }
            OnErrorEventListener.ERROR_EVENT_TIME_OUT -> { //time out
                //重新获取ip,并播放
                val args =
                    "${videoPlay?.id} ${videoPlay?.playUrl} ${videoPlay?.source} ${App.INSTANCE.versionName}"
                runOnUiThread { ffChannel?.invokeMethod("changeip", args) }
            }
            OnErrorEventListener.ERROR_EVENT_TS_NOTFOUND -> {
                mAssist?.switchDecoder(App.PLAN_ID_IJK)
                sendUrlToPlayer(dataSource?.data!!)
            }
        }
    }

    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun isActivityTop(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = manager.appTasks
        tasks.forEach { task ->
            if (PlayerWindowActivity::class.java.name == task.taskInfo.topActivity!!.className) {
                return true
            }
        }
        return false
    }

    /**
     * 请求所有广告位，刷新banner类型，load插屏类型
     */
    private fun requestAllAd() {
        Log.d("PlayerWindowActivity", "请求广告")
        if (App.INSTANCE.adFullscreenVideoTool.mttFullVideoAd != null
            && mReceiverGroup?.getReceiver<PlayCastCoverPortrait>(KEY_CAST_COVER_PORTRAIT) == null
            && mReceiverGroup?.getReceiver<PlayCastCoverLandscape>(KEY_CAST_COVER) == null//投屏状态不展示广告
        ) {
            App.INSTANCE.adFullscreenVideoTool.mttFullVideoAd?.showFullScreenVideoAd(this)
            mAssist?.pause()
            interactionShowing = true

            Log.d("PlayerWindowActivity", "全屏视频广告")
            App.INSTANCE.adFullscreenVideoTool.setCallBack {
                if (interactionShowing) {
                    if (mAssist != null && mAssist!!.state == IPlayer.STATE_PAUSED)
                        mAssist?.resume()
                    else
                        startPlayVideo()
                }
                interactionShowing = false
            }
        }
    }


    //网络监听
    private fun listenNetwork() {
        if (networkChangedReceiver == null)
            networkChangedReceiver = NetworkStatusReceiver()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangedReceiver, intentFilter)
        App.playerNetStateData.observe(this) { result ->
            //恢复播放
            if (result == 2 && mReceiverGroup?.groupValue!!.getBoolean(KEY_ERROR_SHOW)) {
                mReceiverGroup?.groupValue!!.putBoolean(KEY_NETWORK_RESUME, true)
            }
        }

        //电池电量变化
        App.batteryPercent.observe(this) { percent ->
            mReceiverGroup?.groupValue!!.putInt(KEY_BATTERY_PERCENT, percent!!)
        }

        checkOrientation()
        val setting = Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION)
        rotationObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                checkOrientation()
            }

            override fun deliverSelfNotifications(): Boolean {
                return true
            }
        }
        contentResolver.registerContentObserver(setting, false, rotationObserver!!)
    }

    fun onNetWorkChange() {
        if (videoPlay != null) {
            playerHelper?.setPlayUrl(videoPlay!!, {
                val args = "${it.id} ${it.playUrl} ${it.source} ${App.INSTANCE.versionName}"
                runOnUiThread { ffChannel?.invokeMethod("play", args) }
            }, {
                sendUrlToPlayer(it)
            })
        }
    }
}