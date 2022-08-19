package com.kk.taurus.playerbase.player;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件工具类
 */
public class AppFileUtils {

    public static String LOCAL = "local";

    private static String getAppDir() {
        return Environment.getExternalStorageDirectory() + "/.YumiVideo";
    }

    public static String getCacheDir() {
        String dir = getAppDir() + "/Cache/";
        return mkdirs(dir);
    }

    public static String getMovieDir() {
        String dir = getAppDir() + "/Movie/";
        return mkdirs(dir);
    }

    public static String getLogDir() {
        String dir = getAppDir() + "/Log/";
        return mkdirs(dir);
    }

    public static String getSimpleCacheDir() {
        String dir = getAppDir() + "/SimpleCache/";
        return mkdirs(dir);
    }

    public static String getCacheMovieDir(String movieName) {
        String dir = getCacheDir() + movieName + "/";
        return mkdirs(dir);
    }

    public static String getCacheVideoDir(String movieName, String videoName) {
        String dir = getCacheDir() + movieName + "/" + videoName + "/";
        return mkdirs(dir);
    }

    public static String getSplashDir(Context context) {
        String dir = context.getFilesDir() + "/splash/";
        return mkdirs(dir);
    }

    public static String getCorpImagePath(Context context) {
        return context.getExternalCacheDir() + "/corp.jpg";
    }

    private static String mkdirs(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return dir;
    }

    private static boolean exists(String path) {
        File file = new File(path);
        return file.exists();
    }

    /**
     * 过滤特殊字符(\/:*?"<>|)
     */
    private static String stringFilter(String str) {
        if (str == null) {
            return null;
        }
        String regEx = "[\\/:*?\"<>|]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    public static float b2mb(int b) {
        String mb = String.format(Locale.getDefault(), "%.2f", (float) b / 1024 / 1024);
        return Float.valueOf(mb);
    }

    public static void saveFile(String path, String content) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path));
            bw.write(content);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
