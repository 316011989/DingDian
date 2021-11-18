package  cn.video.star.data.remote.model

import android.view.View
import com.bytedance.sdk.openadsdk.TTFeedAd
import com.bytedance.sdk.openadsdk.TTNativeAd
import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 * Created by android on 2018/5/2.
 */
data class HomeTopic(
    val code: Int, //1000
    val message: String, //ok
    val data: HomeData
) : Serializable

data class HomeData(
    val topics: MutableList<Topic>,
    val banner: Banner
) : Serializable

data class Banner(
    val id: Int,
    val isDelete: Int,
    val name: String,
    val adList: MutableList<BannerInfo>
) : Serializable

data class BannerInfo(
    val id: Int,
    val isDelete: Int,
    val orders: Int,
    val beginTime: String,
    val endTime: String,
    val img: String,
    val title: String,
    val type: Int, //1.web页 2.app内部跳转 3.系统浏览器 101:头条广告 102:广点通广告
    val url: String,
    val adPositionId: Int,
    val maxVersion: String,
    val stores: String,
    @Transient val ttNativeAd: TTNativeAd?,//广告view
    @Transient val adView: View?
) : Serializable

data class Topic(
    val id: Int, //1
    val createUserId: Int, //1
    val topicId: Int, //1
    val isHome: Int, //0
    val ordersHome: Int, //0
    val isDiscover: Int, //1
    val ordersDiscover: Int, //0
    val showType: Int, //1:播单 2:6图 101:广告
    val img: String,
    val title: String, //测试播单
    val info: String, //播单哈哈哈
    val summary: String, //山东开发和科技啥都可减肥
    val isDelete: Any, //null
    val videoList: MutableList<Video>,
    @Transient val ttFeedAd: TTFeedAd?,//穿山甲Feed类广告
    @Transient val adView: View?
) : Serializable, MultiItemEntity {

    companion object {

        const val FEED_PLAYLIST = 1

        const val FEED_SIXLIST = 2

        const val FEED_AD = 101
    }

    override fun getItemType(): Int {
        return showType
    }
}

data class Video(
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