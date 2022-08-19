package cn.yumi.daka.data.remote.model

import java.io.Serializable

/**
 * Created by android on 2018/5/8.
 */

data class HotSearchList(
        val code: Int,
        val data: MutableList<HotSearch>,
        val message: String
) : Serializable

data class HotSearch(
        var id: Int,
        val text: String
) : Serializable
