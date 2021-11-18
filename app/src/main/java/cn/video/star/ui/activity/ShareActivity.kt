package cn.video.star.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import cn.junechiu.junecore.utils.DeviceUtils
import cn.video.star.R
import cn.video.star.base.Api
import cn.video.star.base.BaseActivity
import cn.video.star.base.GlideApp
import com.blankj.utilcode.util.PathUtils
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

    private var QRCODE_URL = "https://www.dingdian.vip/download/commendapp_qr.png"

    private val IMG_PATH = PathUtils.getExternalAppDcimPath() + "/DCIM/dingdian.png"

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
        share_website.text = "官网：https://www.dingdian.vip"
        GlideApp.with(this)
            .load(QRCODE_URL)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(shere_qrcode)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            //保存二维码
            R.id.commend_saveqr -> {
                saveImage(QRCODE_URL)
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
        intent.data = Uri.parse(Api.SHARE_BASE_URL)
        startActivity(intent)
    }

    private fun shareApp() {
        DeviceUtils.showSystemShareOption(
            this,
            "追剧，有顶点视频APP就够了",
            Api.SHARE_BASE_URL
        )
    }

    private fun saveImage(url: String) {
        if (!TextUtils.isEmpty(url)) {
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