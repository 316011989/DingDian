package cn.video.star.update

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.Log
import android.view.View
import cn.junechiu.junecore.rxevent.RxBus
import cn.junechiu.junecore.rxevent.RxBusCallBack
import cn.junechiu.junecore.utils.ALogger
import cn.junechiu.junecore.utils.DeviceUtils
import cn.junechiu.junecore.utils.MD5String
import cn.video.star.R
import cn.video.star.base.Api
import cn.video.star.base.Constants
import cn.video.star.data.remote.model.VersionResponse
import cn.video.star.ui.widget.TextProgressBar
import cn.video.star.utils.CommonUtil
import cn.video.star.utils.ConfigCenter
import kotlinx.android.synthetic.main.widget_dialog_update.*
import java.io.File


class UpdateDialog(context: Context, var data: VersionResponse, private var hasApkFile: Boolean) :
    Dialog(context, R.style.alert_dialog),
    View.OnClickListener {

    var mContext = context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_dialog_update)
        setCancelable(false)

        update_dialog_tips?.text = fromHtml(data.content)
        update_dialog_version?.text = data.version
        Log.d("UpdateDialog", data.toString())
        if (data.k == "1") {//强制升级效果
            update_dialog_dismissdialog.visibility = View.GONE
        } else {//非强制升级效果
            update_dialog_dismissdialog.visibility = View.VISIBLE
        }
        update_dialog_progress.setState(TextProgressBar.STATE_DEFAULT)
        if (hasApkFile) {
            update_dialog_progress.progress = 100
            update_dialog_progress.setState(TextProgressBar.STATE_SUCCESS)
        }

        //按钮点击
        update_dialog_progress.setOnClickListener(this)
        update_dialog_dismissdialog.setOnClickListener(this)
        update_dialog_update2web.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.update_dialog_progress -> {
                if (hasApkFile) {//已有apk文件,并检查过apk
                    DeviceUtils.installAPK(mContext, File(Constants.APK_PATH))
                } else {
                    update_dialog_progress.progress = 0
                    update_dialog_progress.setState(TextProgressBar.STATE_PREPARE)
                    registDownloadListener()
                    CoroutineApkDowloader(mContext).download("https://www.dingdian.vip/download/dingdian.apk")
                }
            }
            R.id.update_dialog_dismissdialog -> {
                if (!hasApkFile) {
                    Log.d("UpdateDialog", "后台下载")
                    // 如果是wifi开启服务下载apk
                    if (CommonUtil.isWifiConnected(mContext)) {
//                    ApkDownloadUtil(mContext).execute(data.url)
                        CoroutineApkDowloader(mContext).download("https://www.dingdian.vip/download/dingdian.apk")
                    }
                }
                this@UpdateDialog.dismiss()
            }
            R.id.update_dialog_update2web -> {
                val uri = Uri.parse("https://www.dingdian.vip")
                val intent = Intent("android.intent.action.VIEW", uri)
                mContext.startActivity(intent)
            }
        }
    }

    /**
     * 监听下载任务状态
     */
    private fun registDownloadListener() {
        RxBus.getInstance().register(UpdateEvent.APKSTATUS, object : RxBusCallBack<UpdateEvent> {
            override fun onBusNext(t: UpdateEvent?) {
                when (t!!.state) {
                    TextProgressBar.STATE_SUCCESS -> {
                        val md5String = data.MD5
                        val localMd5 = MD5String.getMd5(File(Constants.APK_PATH))
                        if (!TextUtils.isEmpty(md5String) && !TextUtils.isEmpty(localMd5)) {
                            ALogger.d("downloadApk", "md5String: $md5String localMd5：$localMd5")
                            if (md5String == localMd5) {
                                update_dialog_progress.setState(TextProgressBar.STATE_SUCCESS)
                                hasApkFile = true
                            } else {
                                File(Constants.APK_PATH).delete()
                                update_dialog_progress.setState(-1)
                            }
                        }
                    }
                    TextProgressBar.STATE_FAIL -> {
                        update_dialog_progress.setState(-1)
                    }
                    TextProgressBar.STATE_DOWNLOADING -> {
                        update_dialog_progress.progress = t.progress
                        update_dialog_progress.setState(TextProgressBar.STATE_DOWNLOADING)
                    }
                }
            }

            override fun onBusError(throwable: Throwable?) {
                throwable?.printStackTrace()
            }

            override fun busOfType(): Class<UpdateEvent> {
                return UpdateEvent::class.java
            }
        })
    }


    @Suppress("DEPRECATION")
    fun fromHtml(source: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(source)
        }
    }

}