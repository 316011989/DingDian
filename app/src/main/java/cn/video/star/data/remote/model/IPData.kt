package cn.video.star.data.remote.model

import java.io.Serializable


class IPData(
    var domain: String,
    var ip: String,
    var iplist: ArrayList<String>?
) : Serializable