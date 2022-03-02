package  cn.video.star.ui.activity

import android.os.AsyncTask
import android.text.TextUtils
import cn.video.star.base.Api
import cn.video.star.base.App
import cn.video.star.data.local.db.AppDatabaseManager
import cn.video.star.data.local.db.entity.CollectEntity
import cn.video.star.data.local.db.entity.MovieHistoryEntity
import cn.video.star.data.remote.model.ClarityModel
import cn.video.star.data.remote.model.ClarityRuleModel
import cn.video.star.data.remote.model.VideoData
import cn.video.star.data.remote.model.VideoPlay
import cn.video.star.download.DownloadEntity
import cn.video.star.download.DownloadFeature
import cn.video.star.download.DownloadFileUtil
import cn.video.star.utils.ConfigCenter
import com.blankj.utilcode.util.TimeUtils
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class PlayerHelper(private val videoData: VideoData) {
    companion object {
        var PIC_RATIO = 1//宽高比

        //        const val WX_FRIEND = 1//分享好友
        //        const val WX_CIRCLE = 2//分享朋友圈
        const val MESSAGE_TYPE_PLAY = 1//播放tag,handler使用

        //        const val HALF_SHARE = -1//分享
        const val SOURCE_CC = 0//CC
        const val SOURCE_YOUKU = 1//优酷
        const val SOURCE_QQ = 2//腾讯
        const val SOURCE_IQIYI = 3//爱奇艺
        const val SOURCE_SOHU = 4//搜狐
        const val SOURCE_MGTV = 5//MG

        //        const val SOURCE_HANJUTV = 6 //审核
        const val SOURCE_NANGUA = 7//南瓜

        //        const val SOURCE_CUSTOM = 8  //手动添加
        const val SOURCE_RENREN = 9 //人人

        //        const val SOURCE_MEIJU = 10  //美剧
        const val SOURCE_BILIBILI = 11  //B站

        //        const val SOURCE_DAQIAN = 12  //大千
        const val SOURCE_NANGUAYINGSHI = 13  //南瓜影视
        const val SOURCE_ABORD = 14  //外剧

        //        const val RATE_1080 = "1"//清晰度中的1080P
        const val RATE_720 = "2"//清晰度中的720P,超清
        const val RATE_480 = "3"//清晰度中的480P,高清
        var PLAY_CLARITY = RATE_480 //当前播放清晰度,默认高清480P
        var CACHE_CLARITY = RATE_480 //当前缓存清晰度 ,默认高清480P
        var CAST_SCREEN = 0 //当前投屏状态,0非投屏,1投屏
        var LOCK_SCREEN = 0 //当前锁屏状态
        var PLAY_SPEED = 1 //当前倍速播放状态 默认1.0倍
        var FLOATWINDOW = 0 //1小窗播放 0非小窗播放

        var CHOOSEN_SOURCE_INDEX = 0 //当前播放第几来源
        var CHOOSEN_EPISODE_INDEX = 0 //当前播放第几集
        var SHOW_SPEED = 0 //0倍速  1 1.0x
    }

    var clarityList = mutableListOf<ClarityModel.Clarity>()//当前播放视频的可选清晰度
    var clarityCacheList = mutableListOf<ClarityModel.Clarity>()//当前整部剧集的可选清晰度,用于缓存


    /**
     *   PlayerWindowActivity videoPlay
     */
    fun setPlayUrl(
        videoPlay: VideoPlay,
        callback: (videoPlay: VideoPlay) -> Unit,
        playLocal: (localUrl: String) -> Unit,
    ) {
        videoPlay.oldPlayUrl = videoPlay.playUrl
        DownloadFeature.queryEpisodeByEpisodeId(videoPlay.id) {
            if (it != null && it.downloadStatus == DownloadEntity.state_success) {
                val m3u8File = DownloadFileUtil.getM3u8FileById("${videoPlay.id}")
                if (m3u8File != null && m3u8File.exists()) {
                    val serverUrl = App.INSTANCE.m3u8Server!!.createLocalHttpUrl(m3u8File.path)
                    playLocal(serverUrl)
                } else {
                    playOnline(videoPlay, callback)
                }
            } else {
                playOnline(videoPlay, callback)
            }
        }
    }

    /**
     * videoPlay取清晰度
     * 改变url为带清晰度url
     */
    fun playOnline(videoPlay: VideoPlay, callback: (videoPlay: VideoPlay) -> Unit) {
        //非0来源的不进行分辨率判断
        if (videoPlay.source != SOURCE_CC) {
            callback(videoPlay)
            return
        }
        getVideoRate(videoPlay)
        if (clarityList.size > 0) {
            searchDefaultRate {
                changeRateUrl(videoPlay, it, callback)
            }
        } else {
            changeRateUrl(videoPlay, "0", callback)
        }
    }

    /**
     *   返回的当前播放的视频的所有可选清晰度列表
     */
    private fun getVideoRate(videoPlay: VideoPlay) {
        clarityList.clear()
        if (!TextUtils.isEmpty(videoPlay.rate)) {
            val arr = videoPlay.rate?.split("|")
            if (arr != null && arr.isNotEmpty() && ConfigCenter.mapClarity.isNotEmpty()) {
                arr.forEach {
                    if (!TextUtils.isEmpty(it) && ConfigCenter.mapClarity[it] != null) {
                        clarityList.add(ConfigCenter.mapClarity[it]!!)
                    }
                }
                clarityList = (clarityList.sortedBy(ClarityModel.Clarity::order)).toMutableList()
            }
        }
    }

    /**
     * 查询默认分辨率
     */
    private fun searchDefaultRate(callback: (rateId: String) -> Unit) {
        val sdf = SimpleDateFormat("HHmm", Locale.getDefault())
        val time = sdf.format(Date()).toInt()
        if (ConfigCenter.clarityRuleModel != null) {
            val clarityRuleModel = ConfigCenter.clarityRuleModel
            if (clarityRuleModel?.rule != null && clarityRuleModel.rule.size > 0) {
                for (i in 0 until clarityRuleModel.rule.size) {
                    val rule = clarityRuleModel.rule[i]
                    if (null == rule) {
                        continue
                    } else {
                        if (!TextUtils.isEmpty(rule.type) && rule.type.contains(videoData.type.toString())) {
                            //条件视频类型不为空并且包含视频类型
                            if (filterTime(rule, time, true, callback)) {
                                break
                            }
                        } else { //条件视频类型为空检查时间
                            if (filterTime(rule, time, false, callback)) {
                                break
                            }
                        }
                    }
                }
            } else {
                filterRate(clarityRuleModel!!.defaultId, callback)
            }
        } else {
            if (videoData.type == Api.TYPE_ESP || videoData.type == Api.TYPE_ZY || videoData.type == Api.TYPE_DM) {
                filterRate(RATE_480, callback)
            } else {
                filterRate(RATE_720, callback)
            }
        }
    }

    /**
     * 通过时间筛选可用清晰度
     */
    private fun filterTime(
        rule: ClarityRuleModel.Rule, time: Int, isContainType: Boolean,
        callback: (rateId: String) -> Unit,
    ): Boolean {
        if (rule.max != null && rule.max.toInt() > 0 && rule.min != null && rule.min.toInt() > 0) {   //time条件
            return if (time in rule.min.toInt()..rule.max.toInt()) { //在时间区间内
                filterRate(rule.clarityId, callback)
                true
            } else { //不在时间区间内
                if (isContainType) {
                    filterRate(rule.clarityId, callback)
                    true
                } else {
                    false
                }
            }
        } else { //只有type条件 没有time条件
            return if (isContainType) {
                filterRate(rule.clarityId, callback)
                true
            } else {
                false
            }
        }
    }


    /**
     * 记录选择的清晰度
     */
    private fun filterRate(rateId: String, callback: (rateId: String) -> Unit) {
        val defaultList = clarityList.filter { it.id == rateId } //如果返回的清晰度列表包含默认值
        PLAY_CLARITY = if (defaultList != null && defaultList.isNotEmpty()) {
            rateId
        } else {
            clarityList.last().id //返回最小清晰度
        }
        callback(PLAY_CLARITY)
    }


    /**
     * 修改分辨率之后视频变更
     */
    fun changeRateUrl(videoPlay: VideoPlay, id: String, callback: (videoPlay: VideoPlay) -> Unit) {
        try {
            if (clarityList != null && clarityList.size > 0) {
                val rate = clarityList.find { id == it.id }
                if (rate != null && !TextUtils.isEmpty(rate.pre) &&
                    !TextUtils.isEmpty(videoPlay.playUrl)
                ) {
                    if (videoPlay.playUrl != null)
                        if (videoPlay.playUrl.contains("_")) {
                            val lastIndex_ = videoPlay.playUrl.lastIndexOf("_")
                            val lastDotIndex = videoPlay.playUrl.lastIndexOf(".")
                            videoPlay.playUrl =
                                videoPlay.playUrl.replaceRange(
                                    lastIndex_,
                                    lastDotIndex,
                                    rate.pre
                                )
                        } else {
                            val dotLastIndex = videoPlay.playUrl.lastIndexOf(".")
                            val str1 = videoPlay.playUrl.substring(0, dotLastIndex)
                            videoPlay.playUrl = "$str1${rate.pre}.m3u8"
                        }
                    callback(videoPlay)
                }
            } else callback(videoPlay)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 缓存分辨率字符串获取对应缓存分辨率信息
     */
    fun addCacheRate() {
        val clarityAll = StringBuilder()
        //循环所有集
        for (i in 0 until videoData.plays!!.size) {
            //汇总本集清晰度信息
            if (!TextUtils.isEmpty(videoData.plays!![i].rate)) {
                val clarityItems = videoData.plays!![i].rate.split("|")
                for (j in clarityItems.indices)
                    if (!clarityAll.contains(clarityItems[j])) {
                        if (clarityAll.isNotEmpty())
                            clarityAll.append("|")
                        clarityAll.append(clarityItems[j])
                    }
            }
        }
        if (!TextUtils.isEmpty(clarityAll.toString())) {
            val arr = clarityAll.toString().split("|")
            if (arr != null && arr.isNotEmpty() && ConfigCenter.mapClarity.isNotEmpty()) {
                arr.forEach {
                    if (!TextUtils.isEmpty(it) && ConfigCenter.mapClarity[it] != null) {
                        clarityCacheList.add(ConfigCenter.mapClarity[it]!!)
                    }
                }
            }
            clarityCacheList =
                (clarityCacheList.sortedBy(ClarityModel.Clarity::order)).toMutableList()
        }
    }


    /**
     *  添加进播放历史数据库
     */
    fun insertMovieHistory(playPosition: Long, playPercent: Int) {
        if (videoData != null) {
            try {
                val movie = MovieHistoryEntity()
                movie.cover = videoData.img
                movie.datetime =
                    TimeUtils.getNowString(SimpleDateFormat("yyyy-MM-dd", Locale.CHINA))
                movie.movidId = videoData.id
                movie.name = videoData.name
                movie.selected = 0
                movie.source = CHOOSEN_SOURCE_INDEX//第几个来源
                movie.playIndex = CHOOSEN_EPISODE_INDEX //第几集(从0)
                if (videoData.type == Api.TYPE_ESP || videoData.type == Api.TYPE_DM) {
                    movie.esp = "${videoData.name}   第${CHOOSEN_EPISODE_INDEX + 1}集"
                } else if (videoData.type == Api.TYPE_ZY) {
                    movie.esp = "${videoData.name}   第${CHOOSEN_EPISODE_INDEX + 1}期"
                } else {
                    movie.esp = videoData.name
                }
                movie.position = playPosition
                movie.percent = playPercent //获取播放进度百分比
                AppDatabaseManager.dbManager.insertHistoryMovie(movie)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 添加收藏
     */
    fun insertCollect(playPosition: Long, playPercent: Int) {
        if (videoData != null) {
            try {
                val movie = CollectEntity()
                movie.cover = videoData.img
                movie.datetime =
                    TimeUtils.getNowString(SimpleDateFormat("yyyy-MM-dd", Locale.CHINA))
                movie.movieId = videoData.id
                movie.name = videoData.name
                movie.selected = 0
                movie.source = CHOOSEN_SOURCE_INDEX
                movie.playIndex = CHOOSEN_EPISODE_INDEX
                if (videoData.type == Api.TYPE_ESP || videoData.type == Api.TYPE_DM) {
                    movie.esp = "${videoData.name}   第${CHOOSEN_EPISODE_INDEX + 1}集"
                } else if (videoData.type == Api.TYPE_ZY) {
                    movie.esp = "${videoData.name}   第${CHOOSEN_EPISODE_INDEX + 1}期"
                } else {
                    movie.esp = videoData.name
                }
                movie.position = playPosition
                movie.percent = playPercent //获取播放进度百分比
                AppDatabaseManager.dbManager.insertCollect(movie)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     * 检查播放url
     */
    fun checkPlayUrl(
        playUrl: String,
        callback: (url: String) -> Unit,
    ) {
        object : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg voids: Void): String? {
                var urlNew = ""
                try {
                    trustAllHosts()
                    val url = URL(playUrl)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connect()
                    if (conn.responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                        conn.headerFields.keys.forEach { key ->
                            if (!TextUtils.isEmpty(key) && key.toLowerCase() == "location") {
                                urlNew = conn.getHeaderField("location")
                            }
                        }
                    } else {
                        urlNew = playUrl
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return urlNew
                }
                return urlNew
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                callback(result)
            }
        }.execute()
    }

    /**
     * 信任所有证书
     */
    private fun trustAllHosts() {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(
                chain: Array<out java.security.cert.X509Certificate>?,
                authType: String?,
            ) {
            }

            override fun checkServerTrusted(
                chain: Array<out java.security.cert.X509Certificate>?,
                authType: String?,
            ) {
            }

            override fun getAcceptedIssuers(): Array<out java.security.cert.X509Certificate>? {
                return arrayOf()
            }
        })
        // Install the all-trusting trust manager
        try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, java.security.SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}