package cn.video.star.data.remote.model

import java.io.Serializable

/**
 * Created by zhangyl on 2018/5/3.
 */

data class VideoEpisodes(
    val code: Int, //1000
    val message: String, //ok
    val data: MutableList<VideoPlay>
) : Serializable
