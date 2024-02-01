package cn.yumi.daka.base

import cn.yumi.daka.download.DownloadFileUtil

class Constants {
    companion object {
        val APK_PATH = DownloadFileUtil.getCacheDir() + "/dingdian.apk"
        //微信分享
        const val wxAppid = "wx2b993cb00528f4b1"
        const val wxAppSecret = ""

        //腾讯bugly
        const val buglyAppid = "c288160c0f"
        const val buglyAppkey = "3362e03e-7b0c-4506-b412-f27861412fe1"

        //凤飞
        const val fullscreenId = "829"
        const val appId = "42"
        const val splashId = "400"

        //乐播
        const val leboAppkey = "11833"
        const val leboAppSecret = "8c5ad9def1e9bc26b1f7cf49b7916274"


        //P2P CDNbye token
        const val CDNbyeToken = "NTv1Me5WR"

        //友盟
        const val UMengKey = "65bb9fb7a7208a5af1a74672"
    }
}