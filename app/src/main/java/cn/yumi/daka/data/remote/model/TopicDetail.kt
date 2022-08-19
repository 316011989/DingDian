package cn.yumi.daka.data.remote.model

import java.io.Serializable

/**
 * Created by android on 2018/5/2.
 */

data class TopicDetail(
    val code: Int, //1000
    val message: String, //ok
    val data: TopicDetailData
) : Serializable

data class TopicDetailData(
    val id: Int, //1
    val createUserId: Int, //1
    val title: String, //测试播单
    val info: String, //播单哈哈哈
    val summary: String, //山东开发和科技啥都可减肥
    val img: String
) : Serializable