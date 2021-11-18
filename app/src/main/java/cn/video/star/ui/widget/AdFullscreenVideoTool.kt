package cn.video.star.ui.widget

import android.content.Context
import android.util.Log
import cn.video.star.base.TTAdManagerHolder
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd

class AdFullscreenVideoTool(private val context: Context) {
    //穿山甲全屏广告
    private var mTTAdNative: TTAdNative? = null
    private var adSlot: AdSlot? = null
    var mttFullVideoAd: TTFullScreenVideoAd? = null

    var ready = false

    /**
     *初始化穿山甲
     */
    private fun initTT() {
        if (mTTAdNative == null) {
            //初始化穿山甲,TTAdManager接口中的方法，context可以是Activity或Application
            val ttAdManager = TTAdManagerHolder.get(context)
            //step2:创建TTAdNative对象
            mTTAdNative = ttAdManager.createAdNative(context)
            //step3step4:创建广告请求参数AdSlot,具体参数含义参考文档
            val builder = AdSlot.Builder()
            builder.setCodeId("945964382")
            builder.setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .setOrientation(TTAdConstant.VERTICAL)//必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
            adSlot = builder.build()
        }
    }

    fun requestTTVideo() {
        ready = false//默认广告视频没有加载完成
        initTT()
        //step5:请求广告
        mTTAdNative!!.loadFullScreenVideoAd(
            adSlot, object : TTAdNative.FullScreenVideoAdListener {

                override fun onError(code: Int, message: String) {
                    Log.d("AdFullscreenVideoTool", message)
                    dissmissCallback()
                }

                override fun onFullScreenVideoAdLoad(ad: TTFullScreenVideoAd) {
                    Log.d("AdFullscreenVideoTool", "FullVideoAd loaded")
                    mttFullVideoAd = ad
                    mttFullVideoAd!!.setFullScreenVideoAdInteractionListener(object :
                        TTFullScreenVideoAd.FullScreenVideoAdInteractionListener {

                        override fun onAdShow() {
                            Log.d("AdFullscreenVideoTool", "FullVideoAd show")
                        }

                        override fun onAdVideoBarClick() {
                            Log.d("AdFullscreenVideoTool", "FullVideoAd bar click")
                        }

                        override fun onAdClose() {
                            mttFullVideoAd = null
                            requestTTVideo()
                            dissmissCallback()
                            Log.d("AdFullscreenVideoTool", "FullVideoAd close")
                        }

                        override fun onVideoComplete() {
                            Log.d("AdFullscreenVideoTool", "FullVideoAd complete")
                        }

                        override fun onSkippedVideo() {
                            Log.d("AdFullscreenVideoTool", "FullVideoAd skipped")
                        }

                    })
                }

                override fun onFullScreenVideoCached() {
                    ready = true
                    Log.d("AdFullscreenVideoTool", "FullVideoAd  video cached")
                }

                override fun onFullScreenVideoCached(p0: TTFullScreenVideoAd?) {
                    ready = true
                    Log.d("AdFullscreenVideoTool", "FullVideoAd  video cached")
                }
            })
    }

    var dissmissCallback: () -> Unit? = {}

    fun setCallBack(dissmiss: () -> Unit) {
        dissmissCallback = dissmiss
    }
}