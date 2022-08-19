package cn.yumi.daka.data.remote.model

import java.io.Serializable
import java.util.*

/**
 * Created by android on 2018/5/11.
 */

class VideoPlay : Serializable {
    var id: Long = 0//剧id
    val videoId: Long = 0//集id
    var source: Int = 0 //2
    var episode: Int = 0 //2
    var playUrl: String = ""
    var sourceIsVip: Int? = null
    val start: Long = 0
    val end: Long = 0
    var rate: String = ""//清晰度选项

    @JvmField
    var isPlaying = 0 //是否播放

    var oldPlayUrl: String? = null //记录原始播放链接

    var headers: HashMap<String, String>? = null //CC,南瓜,B站和外剧添加headers
}