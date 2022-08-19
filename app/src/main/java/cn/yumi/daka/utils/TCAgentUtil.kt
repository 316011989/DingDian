package cn.yumi.daka.utils

import android.content.Context
import cn.yumi.daka.base.App
import cn.yumi.daka.base.Constants
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.umeng.commonsdk.statistics.common.DeviceConfig


/**
 * talkingdata
 * 统计数据工具类
 */
class TCAgentUtil {


    companion object {
        fun init(context: App, channel: String) {
            // 必须在调用任何统计SDK接口之前调用初始化函数
            UMConfigure.init(
                context,
                Constants.UMengKey,
                channel,
                UMConfigure.DEVICE_TYPE_PHONE,
                null
            )
            // 选用AUTO页面采集模式
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)
            UMConfigure.setLogEnabled(context.isApkInDebug())

        }

        /**
         * 获取设备id和mac
         */
        fun getTestDeviceInfo(context: Context?): Array<String?>? {
            val deviceInfo = arrayOfNulls<String>(2)
            try {
                if (context != null) {
                    deviceInfo[0] = DeviceConfig.getDeviceIdForGeneral(context)
                    deviceInfo[1] = DeviceConfig.getMac(context)
                }
            } catch (e: Exception) {
            }
            return deviceInfo
        }

        /**
         * 首页banner点击事件
         */
        fun homeBannerClick(url: String) {
            MobclickAgent.onEvent(App.INSTANCE, "banner_ad", mapOf("url" to url))
        }

        /**
         * 视频解析
         */
        fun videoParse(url: String, source: String) {
            MobclickAgent.onEvent(
                App.INSTANCE,
                "video_parse",
                mapOf("url" to url, "source" to source)
            )
        }

        /**
         * 视频详情点击事件
         */
        fun videoClick(videoId: String, topicId: String, from: String) {
            MobclickAgent.onEvent(
                App.INSTANCE,
                "video_click",
                mapOf("videoId" to videoId, "topicId" to topicId, "from" to from)
            )
        }

        /**
         * 播单点击事件
         */
        fun topicClick(topicId: String) {
            MobclickAgent.onEvent(App.INSTANCE, "topic_click", mapOf("topicId" to topicId))
        }

        /**
         * 分类点击
         */
        fun videoCategoryClick(typeId: String) {
            MobclickAgent.onEvent(App.INSTANCE, "video_category_click", mapOf("typeId" to typeId))
        }

        /**
         * 搜索事件
         */
        fun search(keyword: String) {
            if (keyword.contains("1+1="))
                MobclickAgent.onEvent(App.INSTANCE, "superSearch", mapOf("keyword" to keyword))
            else
                MobclickAgent.onEvent(App.INSTANCE, "video_search", mapOf("keyword" to keyword))
        }

        /**
         * 视频播放失败
         */
        fun videoPlayFail(url: String, source: String) {
            MobclickAgent.onEvent(
                App.INSTANCE,
                "video_play_fail",
                mapOf("url" to url, "source" to source)
            )
        }

        /**
         * tabbar点击
         */
        fun tabbarClick(tabNname: String) {
            MobclickAgent.onEvent(App.INSTANCE, "tabbar_click", mapOf("index" to tabNname))
        }


        //解析播放失败
        fun videoParseError(errorId: String, videoId: String) {
            MobclickAgent.onEvent(
                App.INSTANCE,
                "video_parse_error",
                mapOf("videoId" to videoId, "errorId" to errorId)
            )
        }

        //视频详情分享事件
        fun videoDetailShare(id: String) {
            MobclickAgent.onEvent(App.INSTANCE, "video_detail_share", mapOf("videoId" to id))
        }

        //播单详情分享事件
        fun topicDetailShare(id: String) {
            MobclickAgent.onEvent(App.INSTANCE, "topic_detail_share", mapOf("topicId" to id))
        }

        //应用分享事件
        fun appShare() {
            MobclickAgent.onEvent(App.INSTANCE, "app_share")
        }

        //投屏点击事件
        fun airplayClick(id: String) {
            MobclickAgent.onEvent(App.INSTANCE, "airplay_click", mapOf("videoId" to id))
        }

        //投屏成功事件
        fun airplayOk(id: String) {
            MobclickAgent.onEvent(App.INSTANCE, "airplay_ok", mapOf("videoId" to id))
        }

    }

}