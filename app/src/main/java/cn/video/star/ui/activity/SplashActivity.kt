package cn.video.star.ui.activity

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import cn.video.star.R
import cn.video.star.base.Api
import cn.video.star.base.App
import cn.video.star.base.BaseActivity
import cn.video.star.ui.widget.AdSplashTool
import cn.video.star.utils.ConfigCenter
import kotlinx.android.synthetic.main.activity_splash.*
import pub.devrel.easypermissions.EasyPermissions


class SplashActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {
    //广告展示完毕返回此页面,是否强制跳转到主页面
    private var mForceGoMain: Boolean = false

    private var adSplashTool: AdSplashTool? = null


    private val perms = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_splash
    }

    override fun initData(savedInstanceState: Bundle?) {
        adSplashTool = AdSplashTool(this)

        //权限申请
        requestPermission()
    }


    private fun requestPermission() {
        if (EasyPermissions.hasPermissions(this, *perms)) {
            Log.d("requestPermission", "已经拥有权限")
            ConfigCenter(this).readConfig {
                for (v in ConfigCenter.appstate!!.versions) {
                    if (App.INSTANCE.versionName == v.version && App.INSTANCE.channelStr == v.channel) {
                        Api.BASE_URL = v.baseUrl
                    }
                }
                adSplashTool!!.requestSplash({
                    App.INSTANCE.splashCounts++
                    loadingImg.removeAllViews()
                    loadingImg.addView(it)
                }, {  finish() }, { mForceGoMain = true })
            }
        } else {
            Log.d("requestPermission", "尚未拥有权限,需要申请")
            EasyPermissions.requestPermissions(
                this, getString(R.string.title_settings_rationale),
                10011, *perms
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d("onPermissionsDenied", "拒绝权限")
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d("onPermissionsDenied", "接受权限")
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("onRequestPermissions", "权限申请完成")
        finish()
    }


    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        Log.d("ikicker-app", "onStop: " + "STATE_FRONT_TO_BACK")
        //判断是否该跳转到主页面
        if (mForceGoMain) {
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        mForceGoMain = true
    }
}