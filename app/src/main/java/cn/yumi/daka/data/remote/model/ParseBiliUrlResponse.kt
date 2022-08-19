package cn.yumi.daka.data.remote.model

import java.io.Serializable

data class ParseBiliUrlResponse(
    val code: Int, //200
    val message: String, //ok
    val data: MutableList<Items>
) : Serializable {
    data class Items(
        val headers: MutableList<String>,//true
        val segs: MutableList<Seg>,
        val type: String,//FLV 格式
        val quality: String//"SD"清晰度
    ) : Serializable {
        data class Seg(
            val duration: Float,// 1294.505
            val size: Int,//149185252
            val url: String//"http://upos-hz-mirrorks3u.acgvideo.com/upgcxcode/46/03/89750346/89750346-1-32.flv?e=ig8euxZM2rNcNbN37zUVhoMgnwuBhwdEto8g5X10ugNcXBlqNxHxNEVE5XREto8KqJZHUa6m5J0SqE85tZvEuENvNC8xNEVE9EKE9IMvXBvE2ENvNCImNEVEK9GVqJIwqa80WXIekXRE9IMvXBvEuENvNCImNEVEua6m2jIxux0CkF6s2JZv5x0DQJZY2F8SkXKE9IB5QK==&deadline=1557056218&gen=playurl&nbs=1&oi=795403748&os=ks3u&platform=pc&trid=b168490aa5b84ce0a0fc6eabded350d8&uipk=5&upsig=e29257a8823ac55fad821614b0878b99&uparams=e,deadline,gen,nbs,oi,os,platform,trid,uipk"
        ) : Serializable
    }
}


