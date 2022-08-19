package  cn.yumi.daka.data.remote.model

import java.io.Serializable

data class Configuration(
    var configurations: Configurator
) : Serializable {
    data class Configurator(
        val appstate: String,//数据接口域名和解析接口域名
        var adControl: String,//广告控制开关
        val splashRule: String,//开屏广告显示次数和时间的规则
        var union: String,//穿山甲appid和广告位id
        var resolutionRule: String,
        var resolution: String,
        val contactWay: String,//联系方式
        val parseNgKey: String?,//南瓜解析的key
        val parseUrl4Station: String?,//1235来源解析链接
    ) : Serializable
}


data class ContactWay(
    val qqGroup: String,
    val webSite: String,
    val email: String,
    val qrcodePicUrl: String
) : Serializable