package cn.video.star.download

import android.net.Uri
import android.util.Log
import cn.video.star.base.App
import cn.video.star.data.local.db.AppDatabaseManager
import cn.video.star.data.local.db.entity.DownloadEpisodeEntity
import cn.video.star.data.remote.model.VideoData
import cn.video.star.data.remote.model.VideoPlay
import cn.video.star.ui.activity.PlayerHelper
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject
import java.io.File

class DownloadFeature {

    companion object {

        var videoPlay: VideoPlay? = null
        var ffChannel = MethodChannel(
            App.INSTANCE.flutterEngine?.dartExecutor?.binaryMessenger, "com.example.message/mm1"
        )

        /**
         * 数据库
         * 创建下载任务
         * videoData 剧
         * videoInfo 集
         */
        fun addDownloadTask2DB(videoData: VideoData?, videoInfo: VideoPlay) {
            if (videoData != null) {
                videoPlay = videoInfo
                //数据库插入剧任务
                val entity = DownloadEpisodeEntity()
                entity.img = videoData.img
                entity.seriesId = videoData.id//剧id
                entity.episodeId = videoInfo.id//集id
                entity.episodeName = "第${videoInfo.episode}集"//当前集的名字
                entity.seriesName = videoData.name//当前剧的名字
                entity.playUrl = videoInfo.playUrl
                entity.downloadPrograss = 0
                entity.downloadStatus = DownloadEntity.state_prepare
                //
                entity.source = videoInfo.source
                entity.sourceIsVip = videoInfo.sourceIsVip
                entity.playId = videoInfo.id
                entity.videoId = videoInfo.id
                entity.rate = videoInfo.rate
                entity.vType = videoData.type
                entity.episode = videoInfo.episode
                //需要新增6个字段插入数据库,下载中页面resume时需要重新解析
                AppDatabaseManager.dbManager.insertDownloadEpisode(entity)
                parsePlayUrl(videoData, videoInfo)
            }
        }

        /**
         * 解析
         */
        private fun parsePlayUrl(videoData: VideoData, videoPlay: VideoPlay) {
            initFlutterPlugin(videoPlay)
            if (videoPlay.source == PlayerHelper.SOURCE_CC) {
                val playerHelper = PlayerHelper(videoData)
                playerHelper.playOnline(videoPlay) {
                    if (videoPlay.rate.contains(PlayerHelper.CACHE_CLARITY)) {
                        playerHelper.changeRateUrl(videoPlay, PlayerHelper.CACHE_CLARITY) {
                            videoPlay.playUrl = it.playUrl
                        }
                    }
                    val args =
                        "${videoPlay.id} ${videoPlay.playUrl} ${videoPlay.source} ${App.INSTANCE.versionName} download"
                    ffChannel.invokeMethod("play", args)
                }
            } else {
                val args =
                    "${videoPlay.id} ${videoPlay.playUrl} ${videoPlay.source} ${App.INSTANCE.versionName} download"
                ffChannel.invokeMethod("play", args)
            }

        }

        private fun initFlutterPlugin(videoPlay: VideoPlay) {
            App.INSTANCE.flutterEngine?.navigationChannel?.setInitialRoute("download")
            App.INSTANCE.flutterEngine?.dartExecutor?.executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
            )
            ffChannel.setMethodCallHandler { methodCall, result ->
                when (methodCall.method) {
                    "jx_download" -> {
                        if (methodCall.arguments != null) {
                            val playUrl = methodCall.argument<String>("playUrl")
                            val headers = methodCall.argument<String>("headers")
                            videoPlay.playUrl = playUrl!!
                            videoPlay.headers = null
                            if (!headers.isNullOrEmpty()) {
                                val json = JSONObject(headers)
                                videoPlay.headers = hashMapOf()
                                json.keys().forEach { key ->
                                    videoPlay.headers!![key] = json.get(key).toString()
                                }
                            }
                            startDownloadCenter(videoPlay)
                        }
                    }
                    else -> result.notImplemented()
                }
            }

        }


        /**
         * 启动下载任务
         */
        private fun startDownloadCenter(videoInfo: VideoPlay) {
            //启动下载任务线程
            val downloadTask1 = DownloadEntity()
            downloadTask1.taskId = videoInfo.id.toString()
            downloadTask1.downloadUrl = videoInfo.playUrl
            downloadTask1.source = videoInfo.source


            if (!videoInfo.headers.isNullOrEmpty()) {
                val map = videoInfo.headers
                val keys = map!!.keys
                var isFirst = true//map第一个
                keys.forEach {
                    if (!isFirst) {//非第一个
                        downloadTask1.headers += "&"
                    }
                    downloadTask1.headers += it + ":" + map[it]
                    isFirst = false
                }
            }
            //下载任务文件保存路径设置为id转为的md5命名的文件夹
            downloadTask1.saveDir =
                DownloadFileUtil.getCacheDir() + DownloadFileUtil.getCacheName(downloadTask1.taskId)
            if (!File(downloadTask1.saveDir).exists())
                File(downloadTask1.saveDir).mkdir()
            if (Uri.parse(downloadTask1.downloadUrl).path!!.contains(".m3u8")) {
                //下载任务类型设置为m3u8
                downloadTask1.type = DownloadEntity.typeM3u8
                //读取解析m3u8文件,获取ts信息
                M3u8Reader(downloadTask1).readM3u8()
            } else {
                //下载任务类型设置为mp4
                downloadTask1.type = DownloadEntity.typeMp4
                downloadTask1.tempName = downloadTask1.taskId + ".tmp"
                downloadTask1.fileName = downloadTask1.taskId + ".mp4"
                SingleDownloadRunnable.instance.downloadMp4(downloadTask1)
            }
        }


        /**
         * 数据库
         * 更新某集的下载状态和进度
         * 非m3u8多线程任务
         */
        fun updateEpisodeDownloadStatus(
            episodeId: Long, state: Int, speed: Long,
            successTsCount: Long, totalTsCount: Long, callback: (Int) -> Unit,
        ) {
            AppDatabaseManager.dbManager.updateEpisodeDownloadStatus(
                episodeId, state, speed, successTsCount, totalTsCount
            ) {
                callback(it)
            }
        }

        /**
         * 删除某集
         */
        fun deleteEpisode(episodeId: String, callback: (Int) -> Unit) {
            Thread {
                //shutdown该任务线程池,并移除线程池本集对应的所有线程
                SingleDownloadRunnable.instance.cancelM3u8Download(episodeId)
                //删除本地已下载文件
                val m3u8Files = DownloadFileUtil.getM3u8FilesById(episodeId)
                if (m3u8Files != null && m3u8Files.exists()) {
                    if (m3u8Files.listFiles() != null) {
                        for (f in m3u8Files.listFiles()!!) {
                            f.delete()
                        }
                    }
                    m3u8Files.delete()
                }
            }.start()
            AppDatabaseManager.dbManager.deleteEpisodeById(episodeId.toLong()) {
                callback(it)
            }
        }

        /**
         * 查询某剧下所有集
         */
        fun queryAllEpisodesBySeriesId(
            seriesId: Long?,
            callback: (movieList: MutableList<DownloadEpisodeEntity>?) -> Unit,
        ) {
            return AppDatabaseManager.dbManager.queryAllEpisodesBySeriesId(seriesId, callback)
        }

        /**
         * 暂停所有缓存任务
         */
        fun pauseAllDownloadTask() {
            //设置所有下载中任务为暂停状态
            AppDatabaseManager.dbManager.setDownloading2Pause {
                Log.e("setDownloading2Pause", "暂停数量$it")
            }
        }


        /**
         *  检索所有下载中的集,按剧分组
         */
        fun queryEpisodeGroupbySeriesid(callback: (movieList: MutableList<DownloadEpisodeEntity>?) -> Unit) {
            AppDatabaseManager.dbManager.queryEpisodeGroupbySeriesid {
                callback(it)
            }
        }

        /**
         * 检索此剧所有缓存完成的集
         */
        fun queryDownloadedEpisodeBySeriesid(
            seriesId: Long,
            callback: (movieList: MutableList<DownloadEpisodeEntity>) -> Unit,
        ) {
            //查询某剧下所有已完成集,循环删除
            AppDatabaseManager.dbManager.queryDownloadedEpisodeBySeriesid(seriesId) {
                if (it != null)
                    callback(it)
                else
                    callback(mutableListOf())
            }
        }

        /**
         * 检索所有未下载完成的集
         */
        fun queryNotFinishedEpisode(callback: (movieList: MutableList<DownloadEpisodeEntity>) -> Unit) {
            AppDatabaseManager.dbManager.queryNotFinishedEpisode {
                if (it != null)
                    callback(it)
                else
                    callback(mutableListOf())
            }
        }

        /**
         * 通过id查询某集
         */
        fun queryEpisodeByEpisodeId(episodeId: Long, callback: (movie: DownloadEpisodeEntity?) -> Unit) {
            AppDatabaseManager.dbManager.queryEpisodeById(episodeId) {
                callback(it)
            }
        }
    }
}
