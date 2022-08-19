package cn.yumi.daka.data.remote.model

import java.io.Serializable

data class VideoSuggest(
    val code: Int,
    val data: MutableList<Suggest>,
    val message: String
) : Serializable

data class Suggest(
    val area: String,
    val bannerDate: String,
    val bannerText: String,
    val createdTime: Long,
    val detail: SuggestDetail,
    val esKws: String,
    val esTags: String,
    val id: Long,
    val img: String,
    val info: String,
    val isDelete: Int,
    val isHas: Int,
    val isOver: Int,
    val isTrailer: Int,
    val isVip: Int,
    val lastModifiedTime: Long,
    val name: String,
    val playSum: Int,
    val season: String,
    val source: Int,
    val sourceIsVip: Int,
    val sourceOrderHot: Int,
    val sourcePlaySum: Int,
    val sourcePlaySumText: String,
    val sourceScore: String,
    val sourceSort: Int,
    val sourceUrl: String,
    val type: Int,
    val updateText: String,
    val version: Int,
    val year: String
) : Serializable

data class SuggestDetail(
    val actor: String,
    val actorText: String,
    val alias: String,
    val director: String,
    val episodeNum: Int,
    val episodeText: String,
    val guest: String,
    val host: String,
    val summary: String,
    val superId: Int,
    val tagText: String,
    val videoId: Int
) : Serializable