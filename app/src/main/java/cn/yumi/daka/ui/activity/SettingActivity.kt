package cn.yumi.daka.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import cn.yumi.daka.R
import cn.yumi.daka.base.BaseActivity
import cn.yumi.daka.data.local.db.AppDatabaseManager
import cn.yumi.daka.download.DownloadFileUtil
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.SPUtils
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.include_title.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.startActivity

class SettingActivity : BaseActivity() {

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_setting
    }

    override fun initData(savedInstanceState: Bundle?) {
        left_lay.visibility = View.VISIBLE
        toolbar_center_title.text = getString(R.string.setting)
        toolbar_center_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        left_lay.setOnClickListener {
            finish()
        }
        setlisteners()

        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)

        MainScope().launch {
            var size: String
            withContext(Dispatchers.IO) {
                size = FileUtils.getDirSize(DownloadFileUtil.getCacheDir())
            }
            cacheSize.text = size
        }
    }

    private fun setlisteners() {
        //清理缓存
        cacheLayout.setOnClickListener {
            alertDialog(getString(R.string.clear_cache), "") {
                clearCache()
            }
        }
        //意见反馈
        feedbackLay.setOnClickListener {
            startActivity<FeedbackActivity>()
        }

        //免责声明
        Disclaimer.setOnClickListener {
            startActivity<DisclaimerActivity>()
        }
        //4G 播放和下载开关
        mine_item_mobiledata.isChecked = SPUtils.getInstance().getBoolean("OpenMobileData")
        mine_item_mobiledata.setOnCheckedChangeListener { _, isChecked ->
            SPUtils.getInstance().put("OpenMobileData", isChecked)
        }
        versionText.text = packageManager.getPackageInfo(packageName, 0).versionName
    }

    private fun clearCache() {
        MainScope().launch {
            var size: String
            withContext(Dispatchers.IO) {
                FileUtils.deleteAllInDir(DownloadFileUtil.getCacheDir())
                size = FileUtils.getDirSize(DownloadFileUtil.getCacheDir())
            }
            cacheSize.text = size
            AppDatabaseManager.dbManager.deleteAllEpisode()
        }
    }

}