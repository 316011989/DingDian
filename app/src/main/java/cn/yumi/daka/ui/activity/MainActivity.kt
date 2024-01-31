package cn.yumi.daka.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import cn.junechiu.junecore.utils.ALogger
import cn.junechiu.junecore.utils.MD5String
import cn.junechiu.junecore.widget.bottomtab.TabEntity
import cn.yumi.daka.R
import cn.yumi.daka.base.Api
import cn.yumi.daka.base.App
import cn.yumi.daka.base.BaseActivity
import cn.yumi.daka.base.Constants
import cn.yumi.daka.data.remote.model.VersionResponse
import cn.yumi.daka.ui.fragment.DiscoverFragment
import cn.yumi.daka.ui.fragment.HomeFragmentGroup
import cn.yumi.daka.ui.fragment.MineFragment
import cn.yumi.daka.update.UpdateDialog
import cn.yumi.daka.utils.ConfigCenter
import cn.yumi.daka.utils.TCAgentUtil
import cn.yumi.daka.viewmodel.MainViewModel
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.SPUtils
import com.cnc.p2p.sdk.P2PManager
import com.kk.taurus.playerbase.player.AppFileUtils
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import java.io.File
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {
    private var showingFragment: Fragment? = null//当前显示fragment
    var model: MainViewModel? = null//Model
    private var updateDialog: UpdateDialog? = null//升级对话框
    private var mExitTime: Long = 0//返回按键计时
    private var versionCheckFinish = true



    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_main
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (NetworkUtils.isWifiConnected() || SPUtils.getInstance().getBoolean("OpenMobileData")) {
            startDownload()
        }
        Handler().postDelayed({ checklaunchBundle() }, 800)
        deleteSimpleCache()
        startActivity(Intent(this, SplashActivity::class.java))
        ConfigCenter(this).readConfig("") {
//                            subscribeUI()
            createBottomBar()
        }
    }


    /**
     * 拦截触摸事件
     * versionCheckFinish 版本检查完成接口返回结果之前不可触摸
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (versionCheckFinish)
            super.dispatchTouchEvent(ev)
        else
            false
    }

    /**
     *  观察数据
     *  版本检测
     *  缓存任务
     */
    private fun subscribeUI() {
        versionCheckFinish = false
        model = ViewModelProviders.of(this).get(MainViewModel::class.java)
        model?.getVersionData()?.observe(this) { versionData ->
            try {
                if (versionData != null) {//有新版
                    showUpdateDialog(versionData)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            versionCheckFinish = true//触摸事件拦截解除
        }
        model?.checkVersion(packageManager.getPackageInfo(packageName, 0).versionName)
    }

    private fun showUpdateDialog(versionData: VersionResponse) {
        val apkFile = File(Constants.APK_PATH)
        if (apkFile.exists()) {//文件存在
            val md5String = versionData.MD5
            val localMd5 = MD5String.getMd5(apkFile)
            if (!TextUtils.isEmpty(md5String) && !TextUtils.isEmpty(localMd5)) {//检查md5
                ALogger.d("downloadApk", "md5String: $md5String localMd5：$localMd5")
                if (md5String == localMd5) {//md5一致,提示安装
                    updateDialog = UpdateDialog(this, versionData, true)
                    updateDialog?.show()
                } else {//md5不一致,删除文件,提示升级安装
                    apkFile.delete()
                    updateDialog = UpdateDialog(this, versionData, false)
                    updateDialog?.show()
                }
            }
        } else {//文件不存在,提示升级安装
            updateDialog = UpdateDialog(this, versionData, false)
            updateDialog?.show()
        }
    }

    /**
     * 布局
     */
    private fun createBottomBar() {
        val tabText = arrayOf(
            getString(R.string.home_first),
            getString(R.string.home_discover),
            getString(R.string.home_mine)
        ) //底部导航栏文字
        val normalIcon = intArrayOf(
            R.mipmap.home_page_icon,
            R.mipmap.home_channel_icon,
            R.mipmap.home_mine_icon
        )//底部导航栏图片(未选择)
        val selectIcon = intArrayOf(
            R.mipmap.home_page_icon_s,
            R.mipmap.home_channel_icon_s,
            R.mipmap.home_mine_icon_s
        )//底部导航栏图片(已选择)
        val fragments = arrayOf(
            HomeFragmentGroup(),
            DiscoverFragment(),
            MineFragment()
        )//tab页

        val tabEntityList = mutableListOf<TabEntity>()
        for (i in tabText.indices) {
            val item = TabEntity()
            item.text = tabText[i]
            item.normalIconId = normalIcon[i]
            item.selectIconId = selectIcon[i]
            tabEntityList.add(item)
        }
        bottomBar.setNormalTextColor(ContextCompat.getColor(this, R.color.cbbbbbb))
        bottomBar.setSelectTextColor(ContextCompat.getColor(this, R.color.c222222))
        bottomBar.setTabList(tabEntityList)
        supportFragmentManager.beginTransaction().add(R.id.container, fragments[0])
            .commitAllowingStateLoss()
        showingFragment = fragments[0]
        bottomBar.setOnItemClickListener { position ->
            //当前显示fragment与点击fragment不一致
            if (showingFragment != fragments[position]) {
                if (!supportFragmentManager.fragments.contains(fragments[position]))
                    supportFragmentManager.beginTransaction().add(
                        R.id.container,
                        fragments[position]
                    ).commit()
                else
                    supportFragmentManager.beginTransaction().show(fragments[position])
                        .commitAllowingStateLoss()
                supportFragmentManager.beginTransaction().hide(showingFragment!!)
                    .commitAllowingStateLoss()
                showingFragment = fragments[position]
                TCAgentUtil.tabbarClick(tabText[position])
            }
        }
    }


    fun whereGo(adType: Int, adUrl: String?, id: Long) {
        ALogger.d("where--adType:$adType,adUrl:$adUrl,  id:$id")
        when (adType) {
            Api.TYPE_AD_INNER -> {
                webADS(adUrl, id)
            }
            Api.TYPE_AD_PLAY -> {
                startActivity<PlayerWindowActivity>("id" to id)
            }
            Api.TYPE_AD_BROSER -> {
                if (!TextUtils.isEmpty(adUrl)) {
                    val uri = Uri.parse(adUrl)
                    val intent = Intent()
                    intent.action = "android.intent.action.VIEW"
                    intent.data = uri
                    startActivity(intent)
                }
            }
            Api.TYPE_AD_WEB -> {
                startActivity<PlayerWindowActivity>("id" to id)
//                if (!TextUtils.isEmpty(adUrl)) {
//                    startActivity<WebActivity>("url" to adUrl)
//                }
            }
        }
    }

    /**
     * 点击通知,执行相应操作
     */
    private fun checklaunchBundle() {
        if (intent.getBundleExtra(App.EXTRA_BUNDLE) != null) {
            val bundle = intent.getBundleExtra(App.EXTRA_BUNDLE)
            val url = bundle?.getString("url")
            webADS(url, 0)
        }
    }


    //vfans://video/123  app内部跳转
    //vfans://video/id/esp //esp集数
    private fun webADS(url: String?, id: Long) {
        if (!TextUtils.isEmpty(url) && url!!.startsWith("vfans")) {
            try {
                var esp = 0
                val arr = url.replace("vfans://", "").replace("\n", "").split("/")
                val cate = arr[0]
                val id = arr[1]
                if (arr.size >= 3) {
                    esp = arr[2].toInt()
                }
                if (cate == "video") {
                    if (esp > 0) {
                        startActivity<PlayerWindowActivity>("id" to id.toLong(), "esp" to esp)
                    } else {
                        startActivity<PlayerWindowActivity>("id" to id.toLong())
                    }
                } else {
                    startActivity<BillbordActivity>("id" to id.toInt())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            if (id != 0L) {
                startActivity<PlayerWindowActivity>("id" to id)
            }
        }
    }

    /**
     * 清除缓存
     */
    private fun deleteSimpleCache() {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                FileUtils.deleteAllInDir(AppFileUtils.getSimpleCacheDir())
                return null
            }
        }.execute()
    }


    /**
     * 继续缓存任务
     */
    private fun startDownload() {
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }


    /**
     * 退出app时清空资源
     */
    override fun onDestroy() {
        if (updateDialog != null) {
            updateDialog?.dismiss()
            updateDialog = null
        }
        //注销p2p
        P2PManager.getInstance().uninit()
        super.onDestroy()
    }

    /**
     * 重写返回按钮事件,返回确认退出app
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(applicationContext, "再按一次退出程序", Toast.LENGTH_SHORT).show()
                mExitTime = System.currentTimeMillis()
            } else {
                finish()
                finishAffinity()
                exitProcess(0)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}