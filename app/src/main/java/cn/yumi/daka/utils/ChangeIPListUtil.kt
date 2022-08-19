package  cn.yumi.daka.utils

import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import cn.junechiu.junecore.utils.FileUtil
import cn.yumi.daka.base.Api
import cn.yumi.daka.base.App
import cn.yumi.daka.data.remote.model.IPData
import cn.yumi.daka.data.remote.model.ParsePlayUrlResponse
import cn.yumi.daka.download.DownloadFileUtil
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

open class ChangeIPListUtil(
    private val domain: String, private val cacheName: String,
    var callback: (String) -> Unit
) :
    AsyncTask<String, Int, String>() {
    override fun doInBackground(vararg params: String?): String {
        val result: String
        val url = "${Api.IPLIST_URL}domain=$domain&app=${App.INSTANCE.channelStr}"
        Log.d("ChangeIPListUtil", "lookup url---$url")
        val urlConn = URL(url)
        val conn = urlConn.openConnection() as HttpURLConnection
        var ism: InputStream? = null
        val str: String
        try {
            conn.requestMethod = "GET"
            conn.connectTimeout = 2 * 1000
            conn.readTimeout = 2 * 1000
            conn.connect()
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                ism = conn.inputStream
                str = ism.bufferedReader(Charset.forName("GBK")).use(BufferedReader::readText)
                Log.d("ChangeIPListUtil", "lookup result---$str")
                if (!TextUtils.isEmpty(str)) {
                    val bean = Gson().fromJson(str, ParsePlayUrlResponse::class.java)
                    if (bean != null && bean.code == Api.RESPONSE_OK) {
                        result = bean.data
                        Log.d("ChangeIPListUtil", "encrept string---${result.replace("\n", "")}")
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            ism?.close()
            conn.disconnect()
            return ""
        }
    }

    override fun onPostExecute(result: String?) {
        val ipListStr: String?
        val file = File(DownloadFileUtil.getCacheDir() + "/$cacheName")
        if (result != null && result.isNotEmpty()) {
            file.writeText(result.replace("\n", ""))
            ipListStr = ParsePlayUrlUtil.AESDecryptIpList(result)
            Log.d("ChangeIPListUtil", "after encrept---$ipListStr")
        } else {
            ipListStr = if (file.exists()) {
                Log.d("ChangeIPListUtil", "cache file---${file.readText()}")
                ParsePlayUrlUtil.AESDecryptIpList(file.readText())
            } else {
                Log.d("ChangeIPListUtil", "assets file--${FileUtil.getAssetsFile(cacheName)}")
                ParsePlayUrlUtil.AESDecryptIpList(FileUtil.getAssetsFile(cacheName))
            }

            Log.d("ChangeIPListUtil", "after encrept--$ipListStr")
        }
        val iPListData = Gson().fromJson(ipListStr, IPData::class.java)
        if (iPListData?.iplist != null && iPListData.iplist!!.size > 0) {
            callback(iPListData.iplist!![0])
        } else {
            callback("")
        }
    }
}