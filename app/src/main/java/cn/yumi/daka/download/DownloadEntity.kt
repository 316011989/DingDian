package cn.yumi.daka.download


/**
 * 下载任务实体类
 */
class DownloadEntity {
    companion object {
        const val typeM3u8 = 1//m3u8类型
        const val typeMp4 = 2//mp4类型
        const val typeM3u8Key = 3//m3u8的key
        const val state_prepare = 0//准备下载
        const val state_start = 1//开始下载
        const val state_prograss = 2//下载中
        const val state_pause = 3//下载暂停
        const val state_success = 4//下载成功
        const val state_fail = 5//下载失败
    }

    var type: Int = typeM3u8
    var source: Int = 0
    var taskId = ""
    var downloadState = state_prepare
    var downloadUrl: String = ""//下载地址
    var saveDir: String = ""//保存地址
    var headers: String = ""//header
    var lastTime: Long = 0//上次计算速度时的时间
    var downloadSpeed: Long = 0//下载速度,单位b/ms即kb/s


    //m3u8专属属性
    var tsList = mutableListOf<TS>()//分片集合
    var aes128keyUrl: String = ""//aes128解密用的key地址
    var tsSuccessCount = 0L
    var tsTotalCount = 0L
    var errorTime: Int = 0//失败ts计数

    class TS {
        var tsUrl: String = ""
        var tsDuration: String = ""
        var tsIndex: Int = 0
        var savePath: String = "" //保存路径
        var tempPath: String = "" //临时保存路径

        //保存名称
        fun fileName(): String {
            return DownloadFileUtil.md5Encode("tsfile") + tsIndex.toString() + ".ts"
        }
    }

    //mp4专属属性
    var fileSize: Long = 0//文件大小
    var tempName: String = ""//文件临时名称
    var fileName: String = ""//文件名称
    var loadedSize: Long = 0//已经下载大小
    var totalDuration: String = ""//整体时长

}