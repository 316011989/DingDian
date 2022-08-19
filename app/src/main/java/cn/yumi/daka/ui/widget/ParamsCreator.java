package cn.yumi.daka.ui.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Deprecated: 播放加载参数
 */
public class ParamsCreator {

    /***屏幕宽度**/
    private int screenWidth;

    /***像素密度***/
    private int densityDpi;

    /**
     * Instantiates a new Params creator.
     *
     * @param context the context
     */
    public ParamsCreator(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        densityDpi = metric.densityDpi;
    }

    /**
     * 获得默认圆的半径
     *
     * @return int default circle radius
     */
    public int getDefaultCircleRadius() {
        //1440
        if (screenWidth >= 1400) {
            return 40;
        }
        //1080
        if (screenWidth >= 1000) {
            if (densityDpi >= 480) {
                return 30;
            }
            if (densityDpi >= 320) {
                return 30;
            }
            return 30;
        }
        //720
        if (screenWidth >= 700) {
            if (densityDpi >= 320) {
                return 24;
            }
            if (densityDpi >= 240) {
                return 24;
            }
            if (densityDpi >= 160) {
                return 24;
            }
            return 24;
        }
        //540
        if (screenWidth >= 500) {
            if (densityDpi >= 320) {
                return 20;
            }
            if (densityDpi >= 240) {
                return 20;
            }
            if (densityDpi >= 160) {
                return 20;
            }
            return 20;
        }
        return 20;
    }

    /**
     * 获得默认圆的间距‘
     *
     * @return int default circle spacing
     */
    public int getDefaultCircleSpacing() {
        //1440
        if (screenWidth >= 1400) {
            return 8;
        }
        //1080
        if (screenWidth >= 1000) {
            if (densityDpi >= 480) {
                return 8;
            }
            if (densityDpi >= 320) {
                return 8;
            }
            return 8;
        }
        //720
        if (screenWidth >= 700) {
            if (densityDpi >= 320) {
                return 6;
            }
            if (densityDpi >= 240) {
                return 6;
            }
            if (densityDpi >= 160) {
                return 6;
            }
            return 6;
        }
        //540
        if (screenWidth >= 500) {
            if (densityDpi >= 320) {
                return 4;
            }
            if (densityDpi >= 240) {
                return 4;
            }
            if (densityDpi >= 160) {
                return 4;
            }
            return 4;
        }
        return 4;
    }
}
