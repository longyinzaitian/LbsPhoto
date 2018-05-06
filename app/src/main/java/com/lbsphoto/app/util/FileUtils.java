package com.lbsphoto.app.util;

import android.content.Context;

/**
 * 文件工具类
 */
public class FileUtils {

    public static String getAvatarImagePath(Context context) {
        return context.getExternalCacheDir() + "/avatar.jpg";
    }
}
