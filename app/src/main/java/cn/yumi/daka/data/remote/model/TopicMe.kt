package cn.yumi.daka.data.remote.model

import java.io.Serializable

data class TopicMe(
    val code: Int,
    val data: TopicMeData,
    val message: String
) : Serializable

data class TopicMeData(
    val isHeart: Int,    //是否喜欢
    var subscribe: Int,  // 0 未关注 1关注
    val heartCount: Int  //喜欢数量
) : Serializable