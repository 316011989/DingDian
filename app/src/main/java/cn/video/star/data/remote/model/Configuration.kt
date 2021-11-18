package  cn.video.star.data.remote.model

import java.io.Serializable

data class Configuration(
    var configurations: Configurator
) : Serializable {
    data class Configurator(
        var resolutionRule: String,
        var resolution: String,
        var parseSite: String,
        val contactWay: String,//联系方式
        val splashRule: String,//开屏广告不显示的规则
        val parseNgKey: String,//南瓜解析的key
        val appstate: String,
    ) : Serializable
}


data class ContactWay(
    val qqGroup: String,
    val webSite: String,
    val email: String,
    val qrcodePicUrl: String
) : Serializable






