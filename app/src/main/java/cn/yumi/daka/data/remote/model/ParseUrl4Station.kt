package  cn.yumi.daka.data.remote.model

import java.io.Serializable

class ParseUrl4Station(
    var source: MutableList<Int>,//开屏广告源
    var parseUrl: String,//解析url
) : Serializable