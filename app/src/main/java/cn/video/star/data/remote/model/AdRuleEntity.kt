package  cn.video.star.data.remote.model

import java.io.Serializable



/**
 * 开屏广告规则
 *
 */
class SplashRule(
    var splashInPlaying: Boolean = false,//播放页进后台回前台时不显示
    var splashTimesLimit: Int = 60,//60秒之内回前台不显示
    var splashCountsLimit: Int = 20//总共显示次数
) : Serializable

