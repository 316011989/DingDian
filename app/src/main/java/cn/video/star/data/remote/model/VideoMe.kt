package cn.video.star.data.remote.model

import java.io.Serializable

data class VideoMe(
    val code: Int,
    val data: VideoMeData,
    val message: String
) : Serializable

data class VideoMeData(
    val sourceSort: Int,
    var subscribe: Int  // 0 未关注 1关注
) : Serializable