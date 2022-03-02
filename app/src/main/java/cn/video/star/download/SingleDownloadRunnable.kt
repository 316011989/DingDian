package cn.video.star.download

import android.text.TextUtils
import android.util.Log
import okhttp3.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.properties.Delegates

/**
 * 单个下载线程
 * 用于下载ts或mp4文件
 */
class SingleDownloadRunnable {

    companion object {
        /**
         * 双重校验锁式单例
         */
        val instance: SingleDownloadRunnable by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SingleDownloadRunnable()
        }

        private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    }

    var okhttpClient: OkHttpClient by Delegates.notNull()
    private val trustAllCerts: Array<TrustManager> = arrayOf(object : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(
            chain: Array<X509Certificate>, authType: String,
        ) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(
            chain: Array<X509Certificate>,
            authType: String,
        ) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate?> {
            return arrayOfNulls(0)
        }
    })

    init {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext?.init(null, trustAllCerts, SecureRandom())
        val allHostsValid = HostnameVerifier { _, _ -> true }
        val dispatcher = Dispatcher()
        dispatcher.maxRequests = CPU_COUNT * 2 + 1
        dispatcher.maxRequestsPerHost = (CPU_COUNT * 2 + 1) / 3
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(5 * 1000, TimeUnit.SECONDS)
            .readTimeout(5 * 10000, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true) //重试
//                .addInterceptor(logging)
            .proxy(Proxy.NO_PROXY)
            .sslSocketFactory(
                sslContext.socketFactory!!,
                trustAllCerts[0] as X509TrustManager
            )
            .hostnameVerifier(allHostsValid)
            .dispatcher(dispatcher)
        okhttpClient = builder.build()
    }


    fun downloadTs(task: DownloadEntity, index: Int) {
        val ts = task.tsList[index]
        val requestBuilder = Request.Builder()
        if (!TextUtils.isEmpty(task.headers)) {
            val headersList = task.headers.split("&")
            for (h in headersList) {
                requestBuilder.header(
                    h.substring(0, h.indexOf(":")),
                    h.substring(h.indexOf(":") + 1)
                )
            }
        }
        val request = requestBuilder
            .url(ts.tsUrl)
            .tag("id:${task.taskId};name${ts.tempPath}") //设置标记，以便取消相应videoid的任务，如暂停、失败时取消
            .build()
        okhttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e.message != null && e.message!!.contains("Canceled"))
                    task.downloadState = DownloadEntity.state_pause
                else
                    task.downloadState = DownloadEntity.state_fail
                EventBus.getDefault().post(task)
            }

            override fun onResponse(call: Call, response: Response) {
                val tmpFile = File(ts.tempPath) //临时文件
                if (tmpFile.exists())
                    tmpFile.delete()
                tmpFile.createNewFile()
                val contentLenth = response.body()!!.contentLength()
                val fos = FileOutputStream(tmpFile)
                val inputStream: InputStream = response.body()!!.byteStream()
                val buf = ByteArray(2048)
                var current = 0L
                var len: Int
                var flag = true
                while (flag) {
                    len = inputStream.read(buf)
                    flag = len != -1
                    if (flag) {
                        fos.write(buf, 0, len)
                        current += len.toLong()
                    }
                }
                fos.flush()
                inputStream.close()
                val saveFile = File(ts.savePath)
                tmpFile.renameTo(saveFile)
                task.downloadState = DownloadEntity.state_prograss
                task.tsSuccessCount++

                //全部完成通知
                if (task.tsTotalCount <= task.tsSuccessCount) {
                    task.downloadState = DownloadEntity.state_success
                    DownloadFileUtil.createLocalM3U8File(task)
                } else if (task.tsSuccessCount.toInt() % 3 == 0) {
                    val costTime = System.currentTimeMillis() - task.lastTime
                    if (costTime != 0L) {
                        task.downloadSpeed = (contentLenth * 1024 * 3) / (costTime * 1000)
                        task.lastTime = System.currentTimeMillis()
                        Log.e(
                            "SingleDownloadRunnable",
                            "total:${task.tsTotalCount};index:${task.tsSuccessCount};lengthchange:${contentLenth * 3};costTime:$costTime;successCount:${task.tsSuccessCount}"
                        )
                    }
                }
                EventBus.getDefault().post(task)
            }

        })
    }


    fun downloadMp4(task: DownloadEntity) {
        Log.e(
            "SingleDownloadRunnable",
            "task url===${task.downloadUrl}"
        )
        val requestBuilder = Request.Builder()
        if (!TextUtils.isEmpty(task.headers)) {
            val headersList = task.headers.split("&")
            for (h in headersList) {
                requestBuilder.header(
                    h.substring(0, h.indexOf(":")),
                    h.substring(h.indexOf(":") + 1)
                )
            }
        }
        val request = requestBuilder
            .url(task.downloadUrl)
            .tag("id:${task.taskId};name${task.downloadUrl}") //设置标记，以便取消相应videoid的任务，如暂停、失败时取消
            .build()
        okhttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                task.downloadState = DownloadEntity.state_fail
                EventBus.getDefault().post(task)
            }

            override fun onResponse(call: Call, response: Response) {
                task.fileSize = response.body()!!.contentLength()
                task.downloadState = DownloadEntity.state_start
                EventBus.getDefault().post(task)
                val inputStream: InputStream = response.body()!!.byteStream()
                val tmpFile = File(task.saveDir + "/" + task.tempName)
                if (!tmpFile.exists()) {
                    tmpFile.createNewFile()
                }
                val fos = FileOutputStream(tmpFile)
                val buf = ByteArray(1024 * 10)
                var current = 0L
                var len: Int
                var flag = true
                while (flag) {
                    val startTime = System.currentTimeMillis()//2048开始时间
                    len = inputStream.read(buf)
                    flag = len != -1
                    if (flag) {
                        fos.write(buf, 0, len)
                        current += len
                    }
                    task.loadedSize = current
                    task.downloadState = DownloadEntity.state_prograss
                    val costTime = System.currentTimeMillis() - startTime//2048消耗时间
                    if (costTime != 0L) {
                        task.downloadSpeed = 1024 * 10 / costTime//速度b/ms=kb/s
                        EventBus.getDefault().post(task)
                    }
                }
                fos.flush()
                inputStream.close()
                tmpFile.renameTo(File(task.saveDir + "/" + task.fileName))
                task.downloadState = DownloadEntity.state_success
                EventBus.getDefault().post(task)
            }
        })
    }


    fun downloadKey(task: DownloadEntity) {
        Log.e(
            "SingleDownloadRunnable",
            "key url===${task.aes128keyUrl}"
        )
        val requestBuilder = Request.Builder()
        if (!TextUtils.isEmpty(task.headers)) {
            val headersList = task.headers.split("&")
            for (h in headersList) {
                requestBuilder.header(
                    h.substring(0, h.indexOf(":")),
                    h.substring(h.indexOf(":") + 1)
                )
            }
        }
        val request = requestBuilder
            .url(task.aes128keyUrl)
            .tag("id:${task.taskId};name${task.aes128keyUrl}") //设置标记，以便取消相应videoid的任务，如暂停、失败时取消
            .build()
        okhttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                task.downloadState = DownloadEntity.state_fail
                EventBus.getDefault().post(task)
            }

            override fun onResponse(call: Call, response: Response) {
                val saveFile = File(task.saveDir, "key.key")
                if (!saveFile.exists()) {
                    saveFile.createNewFile()
                }
                val fos = FileOutputStream(saveFile)
                val inputStream: InputStream = response.body()!!.byteStream()
                val buf = ByteArray(2048)
                var current = 0L
                var len: Int
                var flag = true
                while (flag) {
                    len = inputStream.read(buf)
                    flag = len != -1
                    if (flag) {
                        fos.write(buf, 0, len)
                        current += len.toLong()
                    }
                }
                fos.flush()
                inputStream.close()
            }
        })
    }


    //暂停下载
    fun cancelM3u8Download(taskId: String) { //取消所有任务
        //根据tag(videoid)当前正在下载的video
        for (call in okhttpClient.dispatcher().runningCalls()) {
            if (call.request().tag().toString().contains(taskId)) {
                call.cancel() //抛出异常
            }
        }
        for (call in okhttpClient.dispatcher().queuedCalls()) {
            if (call.request().tag().toString().contains(taskId)) {
                call.cancel()  //抛出异常
            }
        }
    }

    //取消所有任务
    fun cancelAllDownload() {
        okhttpClient.dispatcher().cancelAll()
    }

    private fun createConnection(task: DownloadEntity, url: String): HttpURLConnection {
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