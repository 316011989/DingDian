package cn.video.star.data.remote.model

import java.io.Serializable

/**
 * Created by android on 2018/5/3.
 */

data class VideoSourcePlays(
    val code: Int, //1000
    val message: String, //ok
    val data: List<VideoSources>,
) : Serializable

data class VideoSources(
    val total: Int,
    val videoId: Long,
    val source: String,
    var plays: List<VideoPlay>,
) : Serializable