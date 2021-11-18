package cn.video.star.data.remote.model

import java.io.Serializable

/**
 * Created by z on 2018/3/17.
 */
data class SearchSuggestEntity(
    val code: Int,
    val message: String,
    val data: MutableList<String>
) : Serializable
