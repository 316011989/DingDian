package  cn.yumi.daka.data.remote.model

import java.io.Serializable

class ADControl(
    var splash: String,//开屏广告源
    var fullscreenvideo: String?,//播放页全屏视频广告
    var fullscreenvideoTimes: Int = 3//播放页全屏视频广告次数限制
) : Serializable


/**
 * 开屏广告规则
 *
 */
class SplashRule(
    var splashInPlaying: Boolean = false,//播放页进后台回前台时不显示
    var splashTimesLimit: Int = 60,//60秒之内回前台不显示
    var splashCountsLimit: Int = 20//总共显示次数
) : Serializable

/**
 * 穿山甲
 */
class Union(
    var appid: String,//应用广告功能id
    var splash: String,//开屏广告id
    var fullscreenvideo: String?,//播放页全屏视频广告,竖屏
    var fullscreenvideoH: String?,//播放页全屏视频广告,竖屏
) : Serializable