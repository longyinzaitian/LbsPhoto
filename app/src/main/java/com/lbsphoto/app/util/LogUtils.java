package com.lbsphoto.app.util;

import android.util.Log;

import com.lbsphoto.app.BuildConfig;

/**
 * @author husyin
 * @date 2018/5/2
 */
public class LogUtils {
    public static void i(String tag, String log) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, log);
        }
    }

    public static void e(String tag, String log) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, log);
        }
    }
}
