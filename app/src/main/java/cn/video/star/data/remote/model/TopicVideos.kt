package cn.video.star.data.remote.model

import java.io.Serializable

data class TopicVideos(
    val code: Int,
    val data: MutableList<TopicVideoData>?,
    val message: String
) : Serializable

data class TopicVideoData(
    val id: Long,
    val img: String,
    val name: String,
    val type: Int,
    val updateText: String,
    val adType: Int, //广告处理类型 0.默认不处理 1.web页 2.app内部跳转 3.系统浏览器
    val adUrl: String //广告跳转地址
) : Serializable