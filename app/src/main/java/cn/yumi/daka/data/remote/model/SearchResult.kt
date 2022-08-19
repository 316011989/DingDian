package cn.yumi.daka.data.remote.model

import java.io.Serializable

/**
 * Created by junzhao on 2018/3/17.
 */
data class SearchResultEntity(
    val code: Int,
    val message: String,
    val data: MutableList<SearchResult>
) : Serializable

data class SearchResult(
    val id: Long, //44
    val version: Int, //2
    val type: Int, //1
    val source: Int, //1
    val name: String, //少年四大名捕
    val info: String,
    val img: String, //http://r1.ykimg.com/0516000051C3B77E670C4A550E01E7E2
    val area: String,
    val year: String,
    val sourceScore: String,
    val sourceIsVip: Int, //1
    val sourcePlaySum: Long, //21000
    val sourcePlaySumText: String, //2.1万次播放
    val isVip: Int, //0
    val updateText: String, //25集全
    val playSum: Int, //0
    val createUserId: Int, //0
    val isTrailer: Int, //0
    val sourceOrderHot: Int, //22
    val sourceUrl: String, //http://v.youku.com/v_show/id_XMjU0MzU3MDg0.html
    val isHas: Int, //0
    val season: String,
    val isOver: Int, //0
    val isDelete: Int,
    val plays: MutableList<VideoPlayD>,
    val detail: DetailData,
    val sourceSort: Int,
    val adType: Int, //广告处理类型 0.默认不处理 1.web页 2.app内部跳转 3.系统浏览器
    val adUrl: String //广告跳转地址
) : Serializable

data class VideoPlayD(
    val id: Int, //154604
    val videoId: Int, //14168
    val source: Int, //2
    val type: Int, //2
    val name: String, //雷神3：诸神黄昏(原声版)
    val title: String, //雷神3：诸神黄昏(原声版)
    val imgText: String, //02:05:33
    val img: String, //http://i.gtimg.cn/qqlive/images/20150608/pic_h.png
    val season: String,
    val episode: Int, //1
    val playUrl: String, //http://v.qq.com/x/cover/9ikkuz4o8ejavdc/k0026phfnm2.html
    val clarity: Int, //-10
    val isPrevue: Int, //0
    val sourceIsVip: Int, //1
    val sourcePlaySum: Int, //22026000
    val sourcePlaySumText: String, //2202.6万
    var isPlaying: Int
) : Serializable

data class DetailData(
    val videoId: Int,
    val alias: String,
    val director: String,
    val actor: String,
    val host: String,
    val guest: String,
    val episodeNum: Int,
    val episodeText: String,
    val summary: String,
    val superId: Int,
    val tagText: String,
    val actorText: String
) : Serializable