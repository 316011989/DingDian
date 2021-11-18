package cn.video.star.base;

import android.content.Context;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdSdk;


/**
 * 用单例来保存TTAdManager实例，在需要初始化sdk的时候调用
 */
public class TTAdManagerHolder {

    private static boolean sInit;

    public static TTAdManager get(Context context) {
        init(context);
        return TTAdSdk.getAdManager();
    }

    public static void init(Context context) {
        if (!sInit) {
            TTAdSdk.init(context, buildConfig(), new TTAdSdk.InitCallback() {

                @Override
                public void success() {
                    sInit = true;
                }

                @Override
                public void fail(int i, String s) {
                    sInit = false;
                }
            });
        }
    }


    private static TTAdConfig buildConfig() {
        return new TTAdConfig.Builder()
                .appId("5155646")
                .useTextureView(true) //使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
                .appName("顶点视频")
                .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_LIGHT)
                .allowShowNotify(true) //是否允许sdk展示通知栏提示
                .allowShowPageWhenScreenLock(true) //是否在锁屏场景支持展示广告落地页
                .debug(true) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI, TTAdConstant.NETWORK_STATE_3G) //允许直接下载的网络状态集合
                .supportMultiProcess(false)//是否支持多进程
//                //.httpStack(new MyOkStack3())//自定义网络库，demo中给出了okhttp3版本的样例，其余请自行开发或者咨询工作人员。
                .build();
    }

}
