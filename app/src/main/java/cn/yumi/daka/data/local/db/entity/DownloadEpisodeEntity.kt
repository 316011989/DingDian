package cn.yumi.daka.data.local.db.entity

import androidx.room.*
import java.io.Serializable

/**
 * 下载功能 单集
 */
@Entity(
    tableName = "download_tv_episode"
)
data class DownloadEpisodeEntity(
    @PrimaryKey
    var episodeId: Long? = null,//集 id
    var episodeName: String? = null,//集 名

    var seriesId: Long? = null,//剧id
    var seriesName: String? = null,//剧 名

    var img: String? = null,//集 图片(单集没有图片,使用剧图片)
    var downloadPrograss: Int? = null,//集 下载进度
    var downloadStatus: Int? = 0,//集 下载状态 0初始 1开始下载 2下载中 3下载暂停 4下载成功 5下载失败(与下载中心DownloadEntity)
    var playUrl: String? = "",//
    var headers: String? = "",

    @Ignore var count: Int = 0,//该剧下有几集

    //下载任务需要暂停重启,重启时若解析必须用到videoinfo和videodata中几个属性,所以必须添加到数据库中
    var source: Int? = null,
    var sourceIsVip: Int? = null,
    var playId: Long? = null,
    var videoId: Long? = null,
    var rate: String? = "1",
    var vType: Int? = null,
    var episode: Int? = null,

    var speed: Long? = null,//下载速度
    var successTsCount: Long? = null,//所有ts数量,用于计算完成百分比
    var totalTsCount: Long? = null//成功ts数量,用于计算百分比

) : Serializable
