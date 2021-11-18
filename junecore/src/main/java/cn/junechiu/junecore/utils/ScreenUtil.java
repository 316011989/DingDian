
package cn.junechiu.junecore.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class ScreenUtil {

    // 目标模糊半径
    public static final float GBLUR_RADIUS_DEST = 220;

    // 屏幕宽
    public static int widthPixels;

    // 屏幕高
    public static int heightPixels;

    // 屏幕密度
    public static float density;

    // 图片在沟通中显示最大宽度
    public static int maxImageWidth;

    // 图片在沟通中显示最小宽度
    public static int minImageWidth;

    // 图片在沟通中显示最大高度
    public static int maxImageHeight;

    // 图片在沟通中显示最小高度
    public static int minImageHeight;

    public static void init(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        widthPixels = dm.widthPixels;
        heightPixels = dm.heightPixels;
        density = dm.density;
        maxImageWidth = widthPixels / 2;
        minImageWidth = widthPixels / 4;
        maxImageHeight = heightPixels / 3;
        minImageHeight = heightPixels / 8;
    }

    /**
     * dp转px
     *
     * @param dpValue dp
     * @return int px
     * @throws
     */
    public static int dp2px(float dpValue) {
        return (int) (dpValue * density + 0.5f);
    }

    /**
     * px 转 dp
     *
     * @param pxValue px
     * @return int dp
     * @throws
     */
    public static int px2dp(float pxValue) {
        return (int) (pxValue / density + 0.5f);
    }

    /**
     * 说明：根据手机的分辨率将sp转成为px
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    /**
     * 获取状态栏高度
     *
     * @return int
     * @throws
     */
    public static int getStatusBarHeight() {
        return Resources.getSystem().getDimensionPixelSize(
                Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"));
    }

    /**
     * 根据屏幕的高度计算相应的视图高度
     *
     * @param high
     * @return
     */
    public static int getViewHeight(int high) {
        return high * heightPixels / 1920;
    }

    /**
     * 根据屏幕的高度计算相应的视图高度
     *
     * @param width
     * @return
     */
    public static int getViewWidth(int width) {
        return width * widthPixels / 1080;
    }

    public static int[] getScreenInfo() {
        int info[] = {
                widthPixels, heightPixels
        };
        return info;
    }

    public static int getTextSize(int size) {
        return size * heightPixels / 1920;
    }


}
