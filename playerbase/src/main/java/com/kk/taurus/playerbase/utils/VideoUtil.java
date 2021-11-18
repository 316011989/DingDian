
package com.kk.taurus.playerbase.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.webkit.WebSettings;

public class VideoUtil {

    public static Context mContext;

    private VideoUtil() {
    }

    public static void init(Context context) {
        mContext = context;
    }

    /**
     * 获取屏幕的大小
     *
     * @return 屏幕尺寸对象
     */
    public static Screen getScreenPix() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return new Screen(dm.widthPixels, dm.heightPixels);
    }

    /**
     * 获取屏幕的宽
     *
     * @return 屏幕宽
     */
    public static int getScreenWidthPix() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获取屏幕的高
     *
     * @return 屏幕高
     */
    public static int getScreenHeightPix() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 屏幕信息
     *
     * @author wangyang
     */
    public static class Screen {
        // 屏幕宽
        public int widthPixels;
        // 屏幕高
        public int heightPixels;

        public Screen() {
        }

        public Screen(int widthPixels, int heightPixels) {
            this.widthPixels = widthPixels;
            this.heightPixels = heightPixels;
        }
    }

    /**
     * 获取屏幕密度
     *
     * @return float
     * @throws
     */
    public static float getDensity() {
        DisplayMetrics dm = new DisplayMetrics();
        dm = mContext.getResources().getDisplayMetrics();
        return dm.density;
    }

    /**
     * dp转px
     *
     * @param dpValue dp
     * @return int px
     * @throws
     */
    public static int dp2px(float dpValue) {
        return (int) (dpValue * getDensity() + 0.5f);
    }

    /**
     * px 转 dp
     *
     * @param pxValue px
     * @return int dp
     * @throws
     */
    public static int px2dp(float pxValue) {
        return (int) (pxValue / getDensity() + 0.5f);
    }

    /**
     * 获取状态栏高度
     *
     * @return int
     * @throws
     */
    public static int getStatusBarHeight() {
        return Resources.getSystem().getDimensionPixelSize(Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"));
    }


    public static String getUserAgent() {
        String userAgent = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(mContext);
            } catch (Exception e) {
                userAgent = System.getProperty("http.agent");
            }
        } else {
            userAgent = System.getProperty("http.agent");
        }
        StringBuffer sb = new StringBuffer();
        int i = 0;
        int length = userAgent.length();
        while (i < length) {
            char c = userAgent.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", c));
            } else {
                sb.append(c);
            }
            i++;
        }
        return sb.toString();
    }
}
