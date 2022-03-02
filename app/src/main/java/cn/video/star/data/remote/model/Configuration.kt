package  cn.video.star.data.remote.model

import java.io.Serializable

data class Configuration(
    var configurations: Configurator
) : Serializable {
    data class Configurator(
        val appstate: String,//数据接口域名和解析接口域名
        val splashRule: String,//开屏广告不显示的规则
        var resolutionRule: String,
        var resolution: String,
        val contactWay: String,//联系方式
        val parseNgKey: String,//南瓜解析的key
    ) : Serializable
}


data class ContactWay(
    val qqGroup: String,
    val webSite: String,
    val email: String,
    val qrcodePicUrl: String
) : Serializable