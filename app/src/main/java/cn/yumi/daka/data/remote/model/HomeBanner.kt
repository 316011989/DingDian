package  cn.yumi.daka.data.remote.model

import java.io.Serializable

data class HomeBanner(
    val code: Int,
    val data: Banner,
    val message: String
) : Serializable
