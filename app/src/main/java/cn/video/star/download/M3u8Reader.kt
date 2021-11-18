package cn.video.star.download

import android.net.Uri
import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import cn.video.star.ui.activity.PlayerHelper
import cn.video.star.utils.AESCoder
import com.kk.taurus.playerbase.config.PlayerLibrary
import org.greenrobot.eventbus.EventBus
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.StringReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

/**
 * m3u8下载
 * 读取ts片段,创建SingleTask任务
 * 提交给下载中心线程池
 */
class M3u8Reader(var task: DownloadEntity) {


    fun readM3u8() {
        task.downloadState = DownloadEntity.state_prepare
        EventBus.getDefault().post(task)
        ConnectM3u8File(task.downloadUrl).execute()
    }

    /**
     * 读取m3u8内容,生成m3u8下载任务实体类
     */
    private fun readBuffered(reader: BufferedReader) {
        val basepath1 = task.downloadUrl.substring(0, task.downloadUrl.lastIndexOf("/") + 1)
        val basepath2 =
            Uri.parse(task.downloadUrl).scheme + "://" + Uri.parse(task.downloadUrl).host
        task.tsList = mutableListOf()
        var ts = DownloadEntity.TS()
        reader.forEachLine {
            var line = it
            Log.e("readBuffered", line)
            if (line.startsWith("#")) {
                if (line.startsWith("#EXTINF:") && line.endsWith(",")) {
                    ts = DownloadEntity.TS()
                    line = line.substring(8, line.length - 1)
                    ts.tsDuration = line
                } else if (line.startsWith("#EXT-X-KEY")) {
                    if (line.contains("AES-128")) {
                        line = line.substring(line.indexOfFirst { x -> x == '\"' } + 1,
                            line.indexOfLast { x -> x == '\"' })
                        if (line.startsWith("http")) {
                            task.aes128keyUrl = line
                        } else if (!line.startsWith("/")) {
                            task.aes128keyUrl = basepath1 + line
                        } else {
                            task.aes128keyUrl = basepath2 + line
                        }
                    }
                }
            } else {
                if (line.startsWith("http")) {
                    ts.tsUrl = line
                } else if (!line.startsWith("/")) {
                    ts.tsUrl = basepath1 + line
                } else {
                    ts.tsUrl = basepath2 + line
                }
                ts.tsIndex = task.tsList.size
                ts.tempPath = task.saveDir + File.separator + ts.fileName() + "tmp"
                ts.savePath = task.saveDir + File.separator + ts.fileName()
                task.tsList.add(ts)
            }
        }
        reader.close()
    }

    /**
     * 请求m3u8文件
     */
    inner class ConnectM3u8File(private var url: String) : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            try {
                var conn = createConnection(url)

                //如果重定向
                val map = conn.headerFields
                // 遍历所有的响应头字段
                map.keys.forEach {
                    if ("Location" == it) {
                        //获取新地址
                        conn = createConnection(map[it]!![0])
                        return@forEach
                    }
                }

                conn.connect()
                return if (conn.responseCode == 200) {
                    if (task.source == PlayerHelper.SOURCE_NANGUAYINGSHI) {
                        val ssarr =
                            AESCoder.decrypt2(conn.inputStream.readBytes(), PlayerLibrary.playKey)
                        readBuffered(BufferedReader(StringReader(ssarr)))
                    } else {
                        readBuffered(BufferedReader(InputStreamReader(conn.inputStream)))
                    }
                    true
                } else {
                    Log.e("M3u8Reader", "m3u8下载失败,此处处理失败回调")
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            if (result) {
                task.downloadState = DownloadEntity.state_start
                EventBus.getDefault().post(task)//任务开始
                //如果有加密key,先下载key
                if (task.aes128keyUrl.isNotEmpty()) {
                    task.type = DownloadEntity.typeM3u8Key
                    SingleDownloadRunnable.instance.downloadKey(task)
                }
                task.tsTotalCount = task.tsList.size.toLong()
                //进行分片下载
                task.lastTime = System.currentTimeMillis()
                for (ts in task.tsList) {
                    if (!Uri.parse(ts.tsUrl).path.isNullOrEmpty()) {
                        if (Uri.parse(ts.tsUrl).path!!.endsWith("m3u8")) {
                            task.downloadUrl = ts.tsUrl
                            ConnectM3u8File(ts.tsUrl).execute()
                            return
                        } else {
                            if (File(ts.savePath).exists()) {
                                task.downloadState = DownloadEntity.state_prograss
                                task.tsSuccessCount++
                                if (task.tsTotalCount <= task.tsSuccessCount) {
                                    task.downloadState = DownloadEntity.state_success
                                    DownloadFileUtil.createLocalM3U8File(task)
                                }
                                EventBus.getDefault().post(task)
                                Log.e(
                                    "SingleDownloadRunnable",
                                    "成功数量=${task.tsSuccessCount};url=${ts.tsUrl}"
                                )
                            } else {
                                task.type = DownloadEntity.typeM3u8
                                SingleDownloadRunnable.instance.downloadTs(task, ts.tsIndex)
                            }
                        }
                    }
                }
                //创建.nomedia文件在文件夹中,使ts片段对于相册等资源管理器不可见
                DownloadFileUtil.createNoMediaFile(task.taskId)
            } else {
                task.downloadState = DownloadEntity.state_fail
                EventBus.getDefault().post(task)//任务失败
            }
        }

    }

    fun createConnection(url: String): HttpURLConnection {
        val conn = URL(url).openConnection() as HttpURLConnection
        if (!TextUtils.isEmpty(task.headers)) {
            val headersList = task.headers.split("&")
            for (h in headersList) {
                conn.addRequestProperty(
                    h.substring(0, h.indexOf(":")),
                    h.substring(h.indexOf(":") + 1)
                )
            }
        }
        return conn
    }

}