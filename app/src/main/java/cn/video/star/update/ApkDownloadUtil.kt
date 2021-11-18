package cn.video.star.update

import android.content.Context
import android.media.MediaScannerConnection
import android.os.AsyncTask
import android.util.Log
import cn.junechiu.junecore.rxevent.RxBus
import cn.video.star.base.Constants.Companion.APK_PATH
import cn.video.star.ui.widget.TextProgressBar
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile

class ApkDownloadUtil(private val mContext: Context) : AsyncTask<String, Int, Int>() {
    private val tag = "ApkDownloadUtil"



    private var lastProgress = -1
    private var contentLength = 0L//文件总长度
    var event: UpdateEvent = UpdateEvent(TextProgressBar.STATE_DEFAULT, 0)



    override fun doInBackground(vararg params: String?): Int {
        var inputStream: InputStream? = null
        var savedFile: RandomAccessFile? = null
        val file: File?
        try {
            var downloadedLength: Long = 0//已下载长度
            val downloadUrl = params[0]
            Log.d(tag, "下载地址: $downloadUrl ")
            if (downloadUrl != null) {
                file = File(APK_PATH)
                if (file.exists()) {
                    downloadedLength = file.length()
                    Log.d(tag, "已存在文件长度: $downloadedLength ")
                } else {
                    if (!file.parentFile.exists()) {
                        file.parentFile.mkdirs()
                    }
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                }
                contentLength = getContentLength(downloadUrl)
                if (contentLength == 0L) {
                    return TextProgressBar.STATE_FAIL
                } else if (contentLength == downloadedLength) {
                    return TextProgressBar.STATE_SUCCESS
                }
                Log.d(tag, "下载地址文件长度: $contentLength ")
                val client = OkHttpClient()
                val request = Request.Builder()
                    .addHeader("RANGE", "bytes=$downloadedLength-")
                    .url(downloadUrl)
                    .build()
                val response = client.newCall(request).execute()
                if (response != null) {
                    inputStream = response.body()!!.byteStream()
                    savedFile = RandomAccessFile(file, "rw")
                    savedFile.seek(downloadedLength)
                    val b = ByteArray(1024)
                    var total = 0
                    var len = inputStream.read(b)
                    while (len != -1) {
                        total += len
                        savedFile.write(b, 0, len)
                        val progress = ((total + downloadedLength) * 10000 / contentLength).toInt()
                        publishProgress(progress)
                        len = inputStream!!.read(b)
                    }
                    //下载完成调用系统文件扫描机制,否则电脑连接显示不了文件
                    MediaScannerConnection.scanFile(
                        mContext,
                        arrayOf(file.absolutePath),
                        null,
                        null
                    )
                    return TextProgressBar.STATE_SUCCESS
                }
            }
        } catch (e: Exception) {
            Log.d(tag, "下载失败")
            e.printStackTrace()
            return TextProgressBar.STATE_FAIL
        } finally {
            try {
                inputStream?.close()
                savedFile?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return TextProgressBar.STATE_FAIL
    }

    override fun onPostExecute(result: Int?) {
        super.onPostExecute(result)
        event.state = result!!
        RxBus.getInstance().post(
            UpdateEvent.APKSTATUS,
            event
        )
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        val progress = values[0]
        Log.d(tag, "下载进度: $progress ")
        event.state = TextProgressBar.STATE_DOWNLOADING
        if (progress!! > lastProgress) {
            event.progress=progress
            RxBus.getInstance().post(UpdateEvent.APKSTATUS, event)
            lastProgress = progress
        }

    }

    private fun getContentLength(downloadUrl: String): Long {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(downloadUrl)
            .build()
        val response = client.newCall(request).execute()
        if (response != null && response.isSuccessful) {
            val contentLength = response.body()?.contentLength()
            response.body()?.close()
            return contentLength!!
        }
        return 0
    }


}