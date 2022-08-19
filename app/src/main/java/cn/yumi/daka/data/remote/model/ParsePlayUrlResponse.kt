package cn.yumi.daka.data.remote.model

import java.io.Serializable

data class ParsePlayUrlResponse(
    val code: Int, //1000
    val message: String, //ok
    val data: String
) : Serializable

data class ParsePlayUrlData(
    val parseSuccess: Boolean,//true
    val sourceUrl: String,//"https://api.rr.tv/video/findM3u8ByEpisodeSidAuth#isDLError=0&quality=high&episodeSid=132863&seasonId=13967&urlId=&playerType=ijk&t=%s"//原地址
    val source: Int,//9,来源
    val items: MutableList<ParsePlayUrlItem>?
) : Serializable

data class ParsePlayUrlItem(
    val url: String,//"http://aliyunvideo.rr.tv/4af20c84af5a4f079b3a519adcaebd9b/2c696399b38e47e79e413899189992e8-f38fb66ef863717930dffd95bb03c567-ld.mp4?auth_key=1551171947-ae062f9fe50b492aa0c23fdafb3371e9-0-6dc738d57ab49cc367339ef46ce9bebf"//解析后地址
    val resolution: String,//"HD" 清晰度
    val headers: MutableList<String>?,//B 站动漫数据添加解析结果使用headers进行播放
    val playHeaders: HashMap<String, String>?
) : Serializable

data class ParseNanguaPlayUrlData(
    val headers: HashMap<String, String>,//
    val url: String//
) : Serializable

data class NanguaData(
    val message: String, //"获取成功",
    val content: NanguaContentData,
    val error_code: Int,
    val international_code: Int,
    val error_info: String,
    val timestamp: String,
    val date: String
) : Serializable {
    data class NanguaContentData(
        val movie_id: Int,
        val movie_url_list: MutableList<MovieUrlListItem>?,
        val movie_url_dot: MutableList<MovieUrlDotItem>?,
        val movie_status_int: Int,
        val need_seed_number_str: String,
        val need_seed_desc_str: String,
        val seed_movie_status_int: Int,
        val exchange_status_int: Int,
        val effect_time_desc: String,
        val movie_watch_count: Int,
        val p_client_ip: String,
        val trailler_id: String
    ) : Serializable {
        data class MovieUrlListItem(
            val media_url: String,
            val movie_id: String,
            val media_resolution: String,
            val default_rate: String,
            val media_name: String,
            val media_thumbnail: String,
            val media_size: String
        ) : Serializable

        data class MovieUrlDotItem(
            val media_url: String,
            val movie_id: String,
            val media_resolution: String,
            val default_rate: String,
            val media_name: String,
            val media_thumbnail: String,
            val media_size: String
        ) : Serializable

    }
}