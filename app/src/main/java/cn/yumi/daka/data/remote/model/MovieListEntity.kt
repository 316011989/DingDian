package cn.yumi.daka.data.remote.model

import android.view.View
import com.bytedance.sdk.openadsdk.TTFeedAd
import java.io.Serializable

/**
 * Created by android on 2018/4/19.
 */
data class MovieListEntity(
    val code: Int,
    val message: String,
    val data: MutableList<MovieEntity>
) : Serializable

data class MovieEntity(
    val id: Long, //1
    val createdTime: String, //2018-04-19T08:22:14.000+0000
    val lastModifiedTime: String, //2018-05-03T08:15:52.000+0000
    val version: Int, //2
    val isDelete: Int, //0
    val type: Int, // 2 电影 1 剧集 3综艺 4动漫 -1广告
    val source: Int, //1
    val name: String, //何所冬暖，何所夏凉 TV版
    val info: String,
    val img: String, //http://r1.ykimg.com/0516000059A64E7CADBC0904A00D2577
    val area: String,
    val year: String,
    val sourceScore: String,
    val sourceIsVip: Int, //0
    val sourcePlaySum: Long, //100000
    val sourcePlaySumText: String, //10.0万次播放
    val isVip: Int, //0
    val updateText: String, //50集全
    val playSum: Int, //0
    val createUserId: Int, //0
    val isTrailer: Int, //0
    val sourceOrderHot: Int, //15
    val sourceUrl: String, //http://v.youku.com/v_show/id_XMzAxNjM0NTY0MA==.html
    val isHas: Int, //0
    val season: String,
    val isOver: Int, //0
    val checkStatus: Int, //1
    val adType: Int, //广告处理类型 0.默认不处理 1.web页 2.app内部跳转 3.系统浏览器
    val adUrl: String, //广告跳转地址
    @Transient var ttFeedAd: TTFeedAd?,//穿山甲Feed类广告
    @Transient val adView: View?//广点通原生广告view
) : Serializable