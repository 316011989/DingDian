package cn.junechiu.junecore.utils;

import com.orhanobut.logger.Logger;

/**
 * Created by android on 2017/12/8.
 */
public class ALogger {

    public static final int VERBOSE = 1;

    public static final int DEBUG = 2;

    public static final int INFO = 3;

    public static final int WARN = 4;

    public static final int ERROR = 5;

    public static final int NOTHING = 6;

    //控制log等级
    private static int LEVEL = VERBOSE;

    public static void setLEVEL(int level) {
        LEVEL = level;
    }

    public static void v(String tag, String message) {
        if (LEVEL <= VERBOSE) {
            Logger.t(tag).v(message);
        }
    }

    public static void d(String tag, String message) {
        if (LEVEL <= DEBUG) {
            Logger.t(tag).d(message);
        }
    }

    public static void d(String message, Object... args) {
        if (LEVEL <= DEBUG) {
            Logger.d(message, args);
        }
    }

    public static void d(String message) {
        if (LEVEL <= DEBUG) {
            Logger.d(message);
        }
    }

    public static void i(String tag, String message) {
        if (LEVEL <= INFO) {
            Logger.t(tag).i(message);
        }
    }

    public static void w(String tag, String message) {
        if (LEVEL <= WARN) {
            Logger.t(tag).w(message);
        }
    }

    public static void json(String tag, String message) {
        if (LEVEL <= WARN) {
            Logger.t(tag).json(message);
        }
    }

    public static void e(String tag, String message) {
        if (LEVEL <= ERROR) {
            Logger.t(tag).e(message);
        }
    }
}
