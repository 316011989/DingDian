package  cn.video.star.data.remote.model

import java.io.Serializable

class AppStates(
    var versions: List<V>
) : Serializable {
    class V(
        var version: String,
        var channel: String,
        var baseUrl: String,
        var needChangeIp: Boolean = true
    ) : Serializable
}