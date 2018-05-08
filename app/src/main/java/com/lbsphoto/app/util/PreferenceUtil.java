package com.lbsphoto.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.lbsphoto.app.application.LbsPhotoApplication;
import com.lbsphoto.app.application.RequestCode;

/**
 * @author 15361
 * @date 2018/5/8
 */
public class PreferenceUtil {
    private static SharedPreferences sp = LbsPhotoApplication.getAppContext().getSharedPreferences(RequestCode.FILE_PATH, Context.MODE_PRIVATE);
    public static void saveLoginUser(String name) {
        sp.edit().putString(RequestCode.USER_NAME_KEY, name).apply();
    }

    public static String getLoginUser() {
        return sp.getString(RequestCode.USER_NAME_KEY, "");
    }

    public static void saveLoginPass(String pass) {
        sp.edit().putString(RequestCode.USER_PASS_KEY, pass).apply();
    }

    public static String getLoginPass() {
        return sp.getString(RequestCode.USER_PASS_KEY, "");
    }

    public static void clearSp() {
        sp.edit().clear().apply();
    }
}
