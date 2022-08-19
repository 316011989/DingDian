package  cn.yumi.daka.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.junechiu.junecore.utils.NetworkUtil
import cn.yumi.daka.base.App
import cn.yumi.daka.ui.activity.PlayerWindowActivity

//监听网络状态变化
class NetworkStatusReceiver : BroadcastReceiver() {
    private var isInterruptNet = false

    override fun onReceive(context: Context, intent: Intent) {
        netState(context)
    }

    private fun netState(context: Context) {
        when (NetworkUtil.getNetWorkStates(context)) {
            NetworkUtil.TYPE_NONE -> { //断网了
                if (!isInterruptNet) {
                    App.playerNetStateData.value = 1
                    isInterruptNet = true
                }
            }
            NetworkUtil.TYPE_MOBILE -> { //移动网络
                if (isInterruptNet) {
                    App.playerNetStateData.value = 3
                    isInterruptNet = false
                }
            }
            NetworkUtil.TYPE_WIFI -> { //WIFI
                if (isInterruptNet) {
                    App.playerNetStateData.value = 2
                    isInterruptNet = false
                }
            }
        }
        changeIp(context)
    }

    private fun changeIp(context: Context) {
        if (context is PlayerWindowActivity) {//播放页,change playUrl
            context.onNetWorkChange()
        }
    }
}
