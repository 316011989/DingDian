package cn.yumi.daka.download

import android.text.TextUtils
import cn.yumi.daka.base.App
import com.blankj.utilcode.util.LogUtils
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.text.DecimalFormat

class DownloadFileUtil {

    companion object {
        private val phoneDir =  App.INSTANCE.externalCacheDir.toString()//缓存文件夹
        private const val appFileDir = "/.YumiVideo"//应用文件夹
        private const val cacheDir = "/Cache/"//缓存文件夹

        /**
         * 缓存文件夹
         * 应用文件路径+缓存目标文件夹
         */
        fun getCacheDir(): String {
            return mkdirs(getAppDir() + cacheDir)
        }

        /**
         * 应用文件夹
         * 手机内存路径+应用目标文件夹
         */
        private fun getAppDir(): String {
            return mkdirs(phoneDir + appFileDir)
        }

        /**
         * 生成md5文件夹
         */
        fun getCacheName(name: String): String? {
            return md5Encode(name)
        }

        //创建文件夹方法
        private fun mkdirs(dir: String): String {
            val file = File(dir)
            if (!file.exists()) {
                file.mkdirs()
            }
            return dir
        }

        //md5方法
        fun md5Encode(str: String): String {
            try {
                val md = MessageDigest.getInstance("MD5")
                md.update(str.toByteArray())
                return BigInteger(1, md.digest()).toString(16)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return str
        }

        /**
         * 创建.nomedia文件在文件夹中,使ts片段对于相册等资源管理器不可见
         */
        fun createNoMediaFile(name: String) {
            val nomediaFile = File(getCacheDir() + getCacheName(name), ".nomedia")
            if (!nomediaFile.exists()) {
                nomediaFile.createNewFile()
            }
        }


        /**
         *  按ts切片的url保存 生成本地m3u8索引文件，ts切片和m3u8文件放在相同目录下即可
         */
        @Throws(IOException::class)
        fun createLocalM3U8File(m3U8: DownloadEntity): File {
            val m3u8File = File(m3U8.saveDir, "local.m3u8")
            if (!m3u8File.exists())
                m3u8File.createNewFile()
            val bfw = BufferedWriter(FileWriter(m3u8File, false))
            bfw.write("#EXTM3U\n")
            bfw.write("#EXT-X-VERSION:3\n")
            bfw.write("#EXT-X-MEDIA-SEQUENCE:0\n")
            bfw.write("#EXT-X-TARGETDURATION:13\n")
            //如果m3u8解析时记录拥有aes类型的key
            if (!TextUtils.isEmpty(m3U8.aes128keyUrl)) {
                bfw.write("#EXT-X-KEY:METHOD=AES-128,URI=\"key.key\"\n")
            }
            for (m3U8Ts in m3U8.tsList) {
                bfw.write("#EXTINF:" + m3U8Ts.tsDuration + ",\n")
                bfw.write(m3U8Ts.fileName())
                bfw.newLine()
            }
            bfw.write("#EXT-X-ENDLIST")
            bfw.flush()
            bfw.close()
            return m3u8File
        }

        /**
         * 获取某集的m3u8和ts所在文件夹,用于删除
         */
        fun getM3u8FilesById(taskId: String): File? {
            try {
                return File(getCacheDir() + getCacheName(taskId))
            } catch (e: Exception) {
                LogUtils.e(e.message)
            }
            return null
        }

        /**
         * 获取某集的mp4文件,用于播放
         */
        fun getMp4FileById(taskId: String): File? {
            try {
                return File(getCacheDir() + getCacheName(taskId), "${taskId}.mp4")
            } catch (e: Exception) {
                LogUtils.e(e.message)
            }
            return null
        }

        /**
         * 获取某集的m3u8文件,用于播放
         */
        fun getM3u8FileById(taskId: String): File? {
            try {
                return File(getCacheDir() + getCacheName(taskId), "local.m3u8")
            } catch (e: Exception) {
                LogUtils.e(e.message)
            }
            return null
        }

        /**
         * 字节格式化
         */
        fun byteToString(size: Long): String {
            val mb: Long = 1024//定义MB的计算常量
            val df = DecimalFormat("0.00")//格式化小数
            return when {
                size / mb >= 1 -> //如果当前Byte的值大于等于1MB
                    df.format(size / mb.toFloat()) + " MB/S   "
                else -> "$size KB/S   "
            }
        }
    }

}