package  cn.yumi.daka.data.remote.model

import java.io.Serializable

data class AppStateResponse(

    val code: Int,

    val message: String,

    val data: TData

) : Serializable {
    data class TData(val t: Boolean, val start: ArrayList<Any>) : Serializable
}

