package  cn.video.star.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Environment
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.text.format.DateFormat
import com.blankj.utilcode.util.RegexUtils
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPInputStream

/**
 * Created by android on 2017/11/13.
 */
class CommonUtil {

    companion object {

        fun getSpeeds(): List<String> {
            val list = mutableListOf<String>()
            list.add("0.75X 0.75")
            list.add("1.0X 1.0")
            list.add("1.25X 1.25")
            list.add("1.5X 1.5")
            list.add("2.0X 2.0")
            return list
        }

        fun getRatios(): List<String> {
            val list = mutableListOf<String>()
            list.add("满屏")
            list.add("100%")
            list.add("75%")
            return list
        }

        /**
         * 为字符串加颜色
         */
        fun getColorSpanned(color: String, string: String): Spanned {
            return Html.fromHtml(String.format("<font color=\"$color\">%s</font>", string))
        }

        fun getColorText(color: String, string: String): String {
            return String.format("<font color=\"$color\">%s</font>", string)
        }

        @SuppressLint("SimpleDateFormat")
        fun getWeek(pTime: String): String {
            var Week = ""
            val format = SimpleDateFormat("yyyy-MM-dd")
            val c = Calendar.getInstance()
            try {
                c.time = format.parse(pTime)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                Week += "周日"
            }
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                Week += "周一"
            }
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
                Week += "周二"
            }
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
                Week += "周三"
            }
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
                Week += "周四"
            }
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                Week += "周五"
            }
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                Week += "周六"
            }
            return Week
        }

        //yyyy-MM-dd hh:mm:ss
        fun getHourM(date: String): String {
            date.let {
                var arrString = date.split(" ")[1].split(":")
                return arrString[0] + ":" + arrString[1]
            }
        }

        // 时间戳转日期
        fun timeStamp2DateStr(timeStamp: Long, pattern: String): String {
            val sdf = SimpleDateFormat(pattern, Locale.CHINA)
            return sdf.format(Date(java.lang.Long.parseLong(timeStamp.toString())))
        }


        fun getPhotoFile(): File {
            val format = DateFormat.format(
                "yyyyMMdd_hhmmss",
                Calendar.getInstance(Locale.CHINA)
            )
            val name = format.toString() + ".jpg"
            val dir =
                File(Environment.getExternalStorageDirectory().toString() + "/football/image/")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return File(dir.absolutePath + name)
        }

        /**
         * 给用户名加密
         */
        fun userNameEncryption(userName: String, startIndex: Int): String {
            val surname = userName[0]
            val substring: String = userName.substring(startIndex)
//            val replace = substring.replace("[\\s\\S]* ".toRegex(), "*")
            val replaceAll = RegexUtils.getReplaceAll(substring, "[\\s\\S]", "*")
            return surname + replaceAll
        }

        fun userCardEncryption(userName: String, startIndex: Int): String {
            userName.replace("\"", "")
            val surname = userName.substring(startIndex)
            val substring: String = userName.substring(0, startIndex)
//            val replace = substring.replace("[\\s\\S]* ".toRegex(), "*")
            val replaceAll = RegexUtils.getReplaceAll(substring, "[\\s\\S]", "*")
            return replaceAll + surname
        }

        fun isWifiConnected(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mWiFiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            return mWiFiNetworkInfo?.isConnected ?: false
        }

        private fun getMetaData(context: Context, key: String): String {
            try {
                val ai = context.packageManager.getApplicationInfo(
                    context.packageName, PackageManager.GET_META_DATA
                )
                val value = ai.metaData.get(key)
                if (value != null) {
                    return value.toString()
                }
            } catch (e: Exception) {
            }
            return ""
        }

        fun appendFile(text: String, destFile: String) {
            val f = File(destFile)
            if (!f.exists()) {
                f.createNewFile()
            }
            f.appendText(text, Charset.defaultCharset())
        }

        //保存m3u8文件
//        fun saveM3u8File(url: String, fileName: String): File {
//            var file = File(AppFileUtils.getMovieDir() + fileName)
//            val requestUrl = URL(url)
//            file.writeBytes(requestUrl.readBytes())
//            return file
//        }

        //获取m3u8 url列表
        fun getTsUrlList(file: File): MutableList<String> {
            val urlList = mutableListOf<String>()
            file.readLines().forEach { lineText ->
                if (!lineText.startsWith("#")) {
                    urlList.add(lineText)
                }
            }
            return urlList
        }

        //合并文件
        fun mergeFiles(fpaths: MutableList<String>, resultPath: String): Boolean {
            if (fpaths.isEmpty() || TextUtils.isEmpty(resultPath)) {
                return false
            }
            if (fpaths.size == 1) {
                return File(fpaths[0]).renameTo(File(resultPath))
            }
            val files = mutableListOf<File>()
            fpaths.forEach { path ->
                files.add(File(path))
            }
            val resultFile = File(resultPath)
            try {
                val resultFileChannel = FileOutputStream(resultFile, true).channel
                files.forEach { file ->
                    val blk = FileInputStream(file).channel
                    resultFileChannel.transferFrom(blk, resultFileChannel.size(), blk.size())
                    blk.close()
                }
                resultFileChannel.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return false
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            return true
        }

//        }

        fun callUrl(path: String, callback: (content: String) -> Unit) {
            var conn: HttpURLConnection? = null
            var ism: InputStream? = null
            try {
                conn = URL(path).openConnection() as HttpURLConnection
                conn?.requestMethod = "GET"
                conn.setRequestProperty("accept", "*/*")
                conn.setRequestProperty("Accept-Encoding", "gzip")
                conn?.instanceFollowRedirects = false
                conn?.connectTimeout = 15000
                conn?.connect()
                if (conn!!.responseCode == HttpURLConnection.HTTP_OK) {
                    ism = conn.inputStream
                    if (conn.contentEncoding.contains("gzip")) {
                        ism = GZIPInputStream(conn.inputStream)
                    }
                    if (ism != null) {
                        val response = ism.readTextAndClose()
                        callback(response)
                    }
                } else {
                    callback("")
                }
            } catch (e: IOException) {
                callback("")
            } finally {
                if (ism != null) {
                    ism.close()
                }
                conn?.disconnect()
            }
        }

        fun InputStream.readTextAndClose(charset: Charset = Charsets.UTF_8): String {
            return this.bufferedReader(charset).use { it.readText() }
        }

        /**
         * 获取当前进程名称
         */
        fun getProcessName(context: Context?): String {
            if (context == null) return ""
            val manager: ActivityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (processInfo in manager.runningAppProcesses) {
                if (processInfo.pid == android.os.Process.myPid()) {
                    return processInfo.processName
                }
            }
            return ""
        }
    }
}