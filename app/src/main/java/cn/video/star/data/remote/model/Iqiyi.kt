package cn.video.star.data.remote.model

data class Iqiyi(
    val code: String,
    val `data`: DataIQ,
    val timestamp: String
)

data class DataIQ(
    val acf: String,
    val ad: Int,
    val adDuration: Int,
    val aid: Int,
    val audio: List<Audio>,
    val bossStatus: Int,
    val cacheTime: Long,
    val cid: Int,
    val clientIp: String,
    val code: Int,
    val dr: Int,
    val ds: String,
    val du: String,
    val duration: Int,
    val exclusive: Int,
    val ff: String,
    val fsc: Int,
    val hdcp: Int,
    val head: Int,
    val isProduced: Int,
    val isdol: Boolean,
    val lgh: List<Any>,
    val lgt: Int,
    val m3u: String,
    val m3utx: String,
    val pano: Pano,
    val previewType: String,
    val prv: String,
    val rTime: String,
    val rp: Int,
    val screenSize: String,
    val tail: Int,
    val thdt: Int,
    val tipType: String,
    val tvid: Int,
    val ugc: Int,
    val um: Int,
    val vd: Int,
    val vid: String,
    val vidl: List<Vidl>,
    val vipTypes: List<Int>,
    val wmarkPos: Int
)

data class Audio(
    val bit: BitIQ,
    val ispre: Int,
    val lid: Int,
    val name: String
)

data class BitIQ(
    val id: Int
)

data class Pano(
    val rType: Int,
    val type: Int
)

data class Vidl(
    val code: Int,
    val dr: Int,
    val drmType: Int,
    val fileFormat: String,
    val lgt: Int,
    val m3u: String,
    val m3utx: String,
    val screenSize: String,
    val unencryptedDuration: Int,
    val vd: Int,
    val vid: String
)