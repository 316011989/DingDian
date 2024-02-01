package cn.yumi.daka.base

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDex
import cn.junechiu.junecore.app.June
import cn.junechiu.junecore.net.interceptors.BasicParamsInterceptor
import cn.junechiu.junecore.net.interceptors.ProxyInterceptor
import cn.junechiu.junecore.utils.DeviceUtils
import cn.yumi.daka.data.DataRepository
import cn.yumi.daka.data.remote.RemoteDataSource
import cn.yumi.daka.download.DownloadEntity
import cn.yumi.daka.download.DownloadFeature
import cn.yumi.daka.ui.activity.MainActivity
import cn.yumi.daka.ui.activity.SplashActivity
import cn.yumi.daka.ui.widget.AdFullscreenVideoTool
import cn.yumi.daka.update.BatteryStatusReceiver
import cn.yumi.daka.update.NetworkStatusReceiver
import cn.yumi.daka.utils.CommonUtil
import cn.yumi.daka.utils.ConfigCenter
import cn.yumi.daka.utils.TCAgentUtil
import com.bumptech.glide.Glide
import com.cnc.p2p.sdk.P2PManager
import com.kk.taurus.playerbase.config.PlayerConfig
import com.kk.taurus.playerbase.config.PlayerLibrary
import com.kk.taurus.playerbase.entity.DecoderPlan
import com.kk.taurus.playerbase.lebo.LeCast
import com.kk.taurus.playerbase.m3u8.M3U8HttpServer
import com.kk.taurus.playerbase.player.ExoMediaPlayer
import com.kk.taurus.playerbase.player.IjkPlayer
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class App : Application(), Application.ActivityLifecycleCallbacks {

    var m3u8Server: M3U8HttpServer? = null

    private var networkChangedReceiver: NetworkStatusReceiver? = null

    private var battertReceiver: BatteryStatusReceiver? = null

    var channelStr = "ymdy"
    var versionName: String = ""


    // 标记程序是否已进入后台(依据onStop回调)
    var flag: Boolean = false

    // 标记程序是否已进入后台(依据onTrimMemory回调)
    var background: Boolean = false

    // 从前台进入后台的时间
    private var frontToBackTime: Long = 0

    // 从后台返回前台的时间
    private var backToFrontTime: Long = 0

    //splash次数
    var splashCounts = 0

    //进入后台是播放页
    var inPlayerActivity = false

    //全屏广告,在application中预加载
    var adFullscreenVideoTool: AdFullscreenVideoTool? = null



    override fun onCreate() {
        super.onCreate()
        initWebviewDataDir()//webview判断进程设置对应存储路径,为支持Androidx添加的,在使用cookie时偶尔会崩溃,(20191023有CSDN帖子说要在其他的SDK等等初始化之前就要调用，否则会报其他的错误)
        INSTANCE = this
        June.init(INSTANCE)//初始化最大lib
        initDataRepository()//初始化数据源
        initChannel()//初始化app所属渠道
        ConfigCenter(this).readConfig(null) {
            initNetConfig()
        }

        TCAgentUtil.init(this, channelStr) //友盟
        initPlayer()//初始化exo和ijk播放器
        initLocalServer()//本地播放服务
        listenNetwork()//监听网络
        listenBattery()//监听电量

        initFullscreenAD()//初始化穿山甲广告平台
        initEventBus()
        stopDownloadTask()
    }



    /**
     * app启动时所有任务因上次退出app停止了下载但是状态还是下载中,所以执行这个方法,设置为暂停状态
     */
    private fun stopDownloadTask() {
        DownloadFeature.pauseAllDownloadTask()//暂停任务
    }


    /**
     * 缓存进度通知使用eventbus
     */
    private fun initEventBus() {
        //缓存视频的进度状态通知
        EventBus.getDefault().register(INSTANCE)
    }


    /**
     * webview存储路径判断进程
     */
    private fun initWebviewDataDir() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = CommonUtil.getProcessName(this)
            if (packageName != processName && processName != "") {//判断不等于默认进程名称
                WebView.setDataDirectorySuffix(processName)
            }
        }
    }

    /**
     * 初始化本地服务,投屏播放使用
     */
    private fun initLocalServer() {
        m3u8Server = M3U8HttpServer()
        m3u8Server?.execute()
    }

    /**
     * 初始化数据来源,分接口和db
     */
    private fun initDataRepository() {
        DataRepository.instance.init(
            INSTANCE.applicationContext,
            RemoteDataSource.instance
        )
    }


    /**
     * 初始化穿山甲
     */
    private fun initFullscreenAD() {
        TTAdManagerHolder.init(this)//初始化穿山甲
        adFullscreenVideoTool = AdFullscreenVideoTool(this)
        adFullscreenVideoTool!!.requestVideo()//预加载播放页需要的全屏广告
    }

    private fun initPlayer() {
        PlayerConfig.addDecoderPlan(
            DecoderPlan(
                PLAN_ID_IJK,
                IjkPlayer::class.java.name,
                "IjkPlayer"
            )
        )
        PlayerConfig.addDecoderPlan(
            DecoderPlan(
                PLAN_ID_EXO,
                ExoMediaPlayer::class.java.name,
                "ExoPlayer"
            )
        )
        PlayerConfig.setDefaultPlanId(PLAN_ID_EXO)
        PlayerLibrary.init(INSTANCE)
    }

    //网络监听
    private fun listenNetwork() {
        networkChangedReceiver = NetworkStatusReceiver()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangedReceiver, intentFilter)
    }

    //电量监听
    private fun listenBattery() {
        battertReceiver = BatteryStatusReceiver()
        registerReceiver(battertReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onTerminate() {
        super.onTerminate()
        m3u8Server?.finish()
        try {
            LeCast.getInstance().release()
            unregisterReceiver(networkChangedReceiver)
            unregisterReceiver(battertReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN || level == TRIM_MEMORY_MODERATE || level == TRIM_MEMORY_BACKGROUND) {
            Glide.get(this).clearMemory()
            background = true//记录应用进入后台运行
        } else if (level == TRIM_MEMORY_COMPLETE) {
            Glide.get(this).clearMemory()
            background = !DeviceUtils.isCurAppTop(this)//任务管理器释放内存后
        }
        Glide.get(this).trimMemory(level)

        //应用进入后台时进行参数记录
        if (background) {
            frontToBackTime = System.currentTimeMillis()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Glide.get(this).clearMemory()
    }

    private fun initChannel() {
        try {
            val ai = this.packageManager.getApplicationInfo(
                this.packageName, PackageManager.GET_META_DATA
            )
            val value = ai.metaData.get("Channel")
            if (value != null) {
                channelStr = value.toString()
            }
        } catch (e: Exception) {
            channelStr = "formal"
            e.printStackTrace()
        }

        versionName = packageManager.getPackageInfo(packageName, 0).versionName
    }

    fun createWifiUrl(wifiIp: String, cacheM3u8: String): String {
        val url = m3u8Server?.createWifiHttpUrl(wifiIp, cacheM3u8)
        return if (!TextUtils.isEmpty(url)) url!! else ""
    }


    companion object {
        lateinit var INSTANCE: App
        val PLAN_ID_IJK = 1
        val PLAN_ID_EXO = 2
        val REFRESH_ME_HISTORY = 555
        val refreshData: MutableLiveData<Int> = MutableLiveData()
        val playerNetStateData: MutableLiveData<Int> = MutableLiveData() //1保存进度 2重试播放
        val refreshCacheData: MutableLiveData<String> = MutableLiveData()//"1"缓存完成  "暂停"\"开始"刷新状态
        val batteryPercent: MutableLiveData<Int> = MutableLiveData()     //电池电量

        const val EXTRA_BUNDLE = "launchBundle"
    }


    override fun onActivityResumed(activity: Activity) {
        if (background || flag) {
            background = false
            flag = false
            backToFrontTime = System.currentTimeMillis()
            Log.i("ikicker-app", "onResume: STATE_BACK_TO_FRONT")
            //进入后台时间超过规定时间，并广告位数据不为空,canshowad判断进入后台时间,2.1.2开始不做时间判断,全部展示开屏
            if (ConfigCenter.adControl != null && ConfigCenter.adControl!!.splash != "" ) {
                Log.i("ikicker-app", "显示开屏广告")
                val intent = Intent(activity, SplashActivity::class.java)
                intent.putExtra("ActivityResumed", true)
                activity.startActivity(intent)
            }
        }
    }


    private fun initNetConfig() {
        val basicParamsInterceptor = BasicParamsInterceptor.Builder()
            .addQueryParam("store", INSTANCE.channelStr)
            .addQueryParam("t", (System.currentTimeMillis() / 1000).toString())
            .addQueryParam(
                "version", packageManager.getPackageInfo(packageName, 0).versionName
            ).build()
        June.getConfigurator()
            .withInterceptor(basicParamsInterceptor)
            .withInterceptor(ProxyInterceptor(this))

        June.getConfigurator().withApiHost(Api.BASE_URL).configure()

    }

    override fun onActivityStopped(activity: Activity) {
        //判断当前activity是否处于前台
        if (!DeviceUtils.isCurAppTop(activity)) {
            frontToBackTime = System.currentTimeMillis()
            flag = true
            Log.i("ikicker-app", "onStop: " + "STATE_FRONT_TO_BACK")
        }
    }

    /**
     * 进入后台间隔1分钟以后可以再次显示广告
     *
     * @return 是否能显示广告
     */
    private fun canShowAd(): Boolean {
        //inPlayerActivity=true进入后台时在播放页,splashInPlaying=false播放页进后台回到前台时不显示开屏
        return if (inPlayerActivity && !ConfigCenter.splashRule!!.splashInPlaying) {
            false
        } else
            backToFrontTime - frontToBackTime > 2 * ConfigCenter.splashRule!!.splashTimesLimit * 1000//进入后台时间超过splashTimesLimit秒
                    && splashCounts < ConfigCenter.splashRule!!.splashCountsLimit//启动页展示小于splashCountsLimit次
    }


    @Subscribe(priority = 3)
    fun onMessageEvent(task: DownloadEntity) {
        Log.e(
            "onMessageEvent", "任务ID:${task.taskId}-状态:${task.downloadState}" +
                    "-速度${task.downloadSpeed}-完成ts:${task.tsSuccessCount}个-ts共:${task.tsTotalCount}个"
        )
        if (task.type == DownloadEntity.typeM3u8)
            DownloadFeature.updateEpisodeDownloadStatus(
                task.taskId.toLong(), task.downloadState,
                task.downloadSpeed, task.tsSuccessCount, task.tsTotalCount
            ) {}
        else
            DownloadFeature.updateEpisodeDownloadStatus(
                task.taskId.toLong(), task.downloadState,
                task.downloadSpeed, task.loadedSize, task.fileSize
            ) {}
    }


    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity != null && activity is MainActivity) {
            DownloadFeature.pauseAllDownloadTask()//暂停任务,app启动时所有任务因上次退出app停止了下载但是状态还是下载中,所以执行这个方法,设置为暂停状态
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    /**
     * 判断当前应用是否是debug状态
     */
    fun isApkInDebug(): Boolean {
        return try {
            val info: ApplicationInfo = applicationInfo
            info.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        } catch (e: java.lang.Exception) {
            false
        }
    }
}