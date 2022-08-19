package cn.yumi.daka.data.remote.model

import java.io.Serializable

/**
 * Created by android on 2018/5/3.
 */
data class VideoPlayData(
        val code: Int, //1000
        val message: String, //ok
        val data: PlayData
) : Serializable

data class PlayData(
        val streams: MutableList<Stream>,
        val lang: String
) : Serializable

data class Stream(
        val play_list: MutableList<Play>
) : Serializable

data class Play(
        val image: String, //http://screenshot.movie.vcinema.com.cn/201710/NsmIaMbOoypVMxEwIsxr/s.jpg
        val default: Int, //0
        val type: String, //m3u8
        val resolution: String, //SD
        val url: String //https://s2.cdn.vcinema.com.cn/201710/nlGJGmFD/TYepmCvRlu.m3u8?auth_key=5nJBLzF1lrR0wyVnEtmTOjsXDdUHZfZ1Oewdd+HXYdMnlLjZ79uJsyMEXPQs1tPN
) : Serializable