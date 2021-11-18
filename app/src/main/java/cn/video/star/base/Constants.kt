package cn.video.star.base

import cn.video.star.download.DownloadFileUtil

class Constants {
    companion object {
        val APK_PATH = DownloadFileUtil.getCacheDir() + "/dingdian.apk"

        //友盟
        const val UMengKey = "60f63aaa2a1a2a58e7ddcd92"
    }
}