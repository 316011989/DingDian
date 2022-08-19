package cn.yumi.daka.ui.widget

import android.content.Context
import android.util.Log
import android.view.View
import androidx.annotation.MainThread
import cn.junechiu.junecore.utils.ALogger
import cn.yumi.daka.base.TTAdManagerHolder
import cn.yumi.daka.utils.ConfigCenter
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTSplashAd


/**
 * 开屏广告,支持穿山甲,趣盈,广点通
 */
class AdSplashTool(private val context: Context) {
    //穿山甲开屏广告
    private var mTTAdNative: TTAdNative? = null

    private var adSlot: AdSlot? = null

    /**
     * 请求开屏广告
     */
    fun requestSplash(onShow: (adView: View?) -> Unit, onDismiss: () -> Unit, onClick: () -> Unit) {
        //总开关开启状态
        if (ConfigCenter.adControl != null ) {
            Log.d("开屏广告平台选择", ConfigCenter.adControl!!.splash)
            if (ConfigCenter.adControl!!.splash == "TT") {//穿山甲统一平台
                initTT(onDismiss)
                requestTTSplash(onShow, onDismiss, onClick)
            } else {
                onDismiss()
            }
        }
        //总开关关闭状态,直接跳转
        else {
            onDismiss()
        }
    }

    /**
     *初始化穿山甲
     */
    private fun initTT(onDismiss: () -> Unit) {
        if (mTTAdNative == null) {
            //初始化穿山甲,TTAdManager接口中的方法，context可以是Activity或Application
            val ttAdManager = TTAdManagerHolder.get(context)
            //step2:创建TTAdNative对象
            mTTAdNative = ttAdManager.createAdNative(context)
            //step3:创建开屏广告请求参数AdSlot,具体参数含义参考文档
            adSlot = AdSlot.Builder()
                .setCodeId("887565074")
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .build()
        } else {
            onDismiss()
        }
    }

    /**
     * 加载穿山甲开屏
     */
    private fun requestTTSplash(
        onShow: (adView: View) -> Unit, onDismiss: () -> Unit, onClick: () -> Unit
    ) {
        if (ConfigCenter.union != null) {
            //step4:请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
            mTTAdNative?.loadSplashAd(adSlot, object : TTAdNative.SplashAdListener {
                @MainThread
                override fun onSplashAdLoad(ad: TTSplashAd?) {
                    Log.d("splash TT", "加载穿山甲开屏成功")
                    if (ad != null) {
                        ad.setSplashInteractionListener(object : TTSplashAd.AdInteractionListener {
                            override fun onAdClicked(p0: View?, p1: Int) {
                                onClick()
                            }

                            override fun onAdSkip() {
                                onDismiss()
                            }

                            override fun onAdShow(p0: View?, p1: Int) {
                            }

                            override fun onAdTimeOver() {
                                onDismiss()
                            }
                        })
                        onShow(ad.splashView)
                    } else {
                        onDismiss()
                    }
                }

                @MainThread
                override fun onTimeout() {
                    ALogger.d("加载开屏广告超时")
                    onDismiss()
                }

                @MainThread
                override fun onError(code: Int, message: String) {
                    ALogger.d("加载开屏广告异常,原因$message")
                    onDismiss()
                }
            }, 2000)
        } else {
            onDismiss()
        }
    }

}


