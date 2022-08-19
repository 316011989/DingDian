package cn.yumi.daka.ui.activity

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import cn.junechiu.junecore.utils.DeviceUtils
import cn.yumi.daka.R
import cn.yumi.daka.base.Api
import cn.yumi.daka.base.BaseActivity
import cn.yumi.daka.base.GlideApp
import cn.yumi.daka.utils.ConfigCenter
import cn.yumi.daka.utils.TCAgentUtil
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_share.*
import kotlinx.android.synthetic.main.include_title.*
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.net.URL

class ShareActivity : BaseActivity(), View.OnClickListener {

    private var QRCODE_URL = "https://files.ybliy.com/ymShareErWeiMa.png"

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_share
    }

    override fun initData(savedInstanceState: Bundle?) {
        left_lay.visibility = View.VISIBLE
        left_img.setImageResource(R.mipmap.back_arrow_icon30)
        toolbar_center_title.text = getString(R.string.share)
        toolbar_center_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        left_lay.setOnClickListener {
            finish()
        }
        setStatusBar(ContextCompat.getColor(this, R.color.colorPrimary), false)
        apptitlebar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        val params = apptitlebar.layoutParams as RelativeLayout.LayoutParams
        params.setMargins(0, getStatusBarHeight(), 0, 0)

        commend_saveqr.setOnClickListener(this)
        commend_share2other.setOnClickListener(this)
        share_website.setOnClickListener(this)
        share_website.text = "官网：${ConfigCenter.contactWay?.webSite ?: Api.SHARE_BASE_URL}"
        GlideApp.with(this)
            .load(ConfigCenter.contactWay?.qrcodePicUrl ?: QRCODE_URL)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(shere_qrcode)
    }

    override fun onClick(v: View?) {
        TCAgentUtil.appShare()
        when (v?.id) {
            //保存二维码
            R.id.commend_saveqr -> {
                saveImage(ConfigCenter.contactWay?.qrcodePicUrl ?: QRCODE_URL)
            }
            //分享给他人
            R.id.commend_share2other -> {
                shareApp()
            }
            //点击官网
            R.id.share_website -> {
                openWebsite()
            }
        }
    }

    private fun openWebsite() {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(ConfigCenter.contactWay?.webSite ?: Api.SHARE_BASE_URL)
        startActivity(intent)
    }

    private fun shareApp() {
        DeviceUtils.showSystemShareOption(
            this,
            "追剧，有玉米电影APP就够了",
            ConfigCenter.contactWay?.webSite ?: Api.SHARE_BASE_URL
        )
    }

    private fun saveImage(url: String) {
        if (!TextUtils.isEmpty(url)) {
            val IMG_PATH =
                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/ymdyDownloads/ymdyQR.png"
            Observable.create(ObservableOnSubscribe<String> { e ->
                try {
                    var file = File(IMG_PATH)
                    if (!file.parentFile.exists()) {
                        file.parentFile.mkdirs()
                    }
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    if (!TextUtils.isEmpty(url)) {
                        val requestUrl = URL(url)
                        file.writeBytes(requestUrl.readBytes())
                    }
                    e.onNext(IMG_PATH)

                    val values = ContentValues()
                    val saveFile = File(IMG_PATH)
                    val timeMillis = System.currentTimeMillis()
                    values.put(MediaStore.MediaColumns.TITLE, saveFile.name)
                    values.put(
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        saveFile.name
                    )
                    values.put(MediaStore.MediaColumns.DATE_MODIFIED, timeMillis)
                    values.put(MediaStore.MediaColumns.DATE_ADDED, timeMillis)
                    values.put(
                        MediaStore.MediaColumns.DATA,
                        saveFile.absolutePath
                    )
                    values.put(MediaStore.MediaColumns.SIZE, saveFile.length())
                    values.put(MediaStore.Images.Media.DESCRIPTION, "ymdy二维码")
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") // 图片格式
                    // 插入到数据库
                    applicationContext.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    toast("保存成功至$IMG_PATH")
                }
        }
    }
}