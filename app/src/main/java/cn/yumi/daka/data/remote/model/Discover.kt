package  cn.yumi.daka.data.remote.model

import com.bytedance.sdk.openadsdk.TTFeedAd
import java.io.Serializable

/**
 * Created by android on 2018/5/2.
 */

data class Discover(
    val code: Int, //1000
    val message: String, //ok
    val data: DiscoverData
) : Serializable

data class DiscoverData(
    val names: MutableList<Header>,
    val topics: MutableList<DiscoverFeed>
) : Serializable

data class DiscoverFeed(
    val id: Int, //1
    val createUserId: Int, //1
    val topicId: Int, //1
    val isHome: Int, //0
    val ordersHome: Int, //0
    val isDiscover: Int, //1
    val ordersDiscover: Int, //0
    val showType: Int, //1
    val img: String, //http://wx.qlogo.cn/mmopen/vi_32/hbl8y4dicvmWcUkCjEDhY1qmictDE2UVG6aQauA4egb6vCK5FX5zlxzQFLufnpdrajSFX8K5qLgbgAB1hSibdZbKA/132
    val title: String, //测试播单7
    val info: String, //播单哈哈哈
    val summary: String, //山东开发和科技啥都可减肥
    val isDelete: Any, //null
    val cover: String,
    val videoList: MutableList<DiscoverVideo>,
    val ttFeedAd: TTFeedAd//穿山甲Feed类广告
) : Serializable

data class DiscoverVideo(
    val id: Long, //26
    val videoTopicId: Int, //1
    val type: Int, //1
    val source: Int, //1
    val name: String, //英雄祭
    val info: String,
    val img: String, //http://r1.ykimg.com/0516000059F30758859B5C048E0A12B1
    val area: String,
    val year: String,
    val sourceScore: String, //<em>7</em>.4
    val sourceIsVip: Int, //0
    val sourcePlaySum: Long, //370000
    val sourcePlaySumText: String, //3.7万次播放
    val isVip: Int, //0
    val updateText: String, //40集全
    val playSum: Int, //0
    val isTrailer: Int, //0
    val sourceOrderHot: Int, //22
    val sourceUrl: String, //http://v.youku.com/v_show/id_XMTI5MDg2NzA2OA==.html
    val isHas: Int, //0
    val season: String,
    val isOver: Int, //0
    val orders: Int, //1
    val adType: Int, //广告处理类型 0.默认不处理 1.web页 2.app内部跳转 3.系统浏览器
    val adUrl: String //广告跳转地址
) : Serializable

data class Header(
    val img: String,
    val title: String,
    val id: Int
) : Serializable