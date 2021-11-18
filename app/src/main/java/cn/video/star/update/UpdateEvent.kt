package  cn.video.star.update

import java.io.Serializable

class UpdateEvent(var state: Int, var progress: Int) : Serializable {
    companion object {
        val APKSTATUS = "download_apk_state"
    }
}