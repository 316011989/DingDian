package cn.video.star.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import cn.video.star.R
import cn.video.star.base.Api
import cn.video.star.base.App
import cn.video.star.base.App.Companion.INSTANCE
import cn.video.star.base.BaseActivity
import cn.video.star.data.remote.api.ApiManager
import cn.video.star.download.DownloadFileUtil
import cn.video.star.utils.traceroute.TraceRoute
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.include_title.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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


        //缓存目录大小
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
//            AppDatabaseManager.dbManager.deleteAllEpisode()
        }
    }
}