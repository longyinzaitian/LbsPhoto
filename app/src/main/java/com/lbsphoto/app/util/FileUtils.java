package com.lbsphoto.app.util;

import android.content.Context;
import android.os.Environment;

import com.lbsphoto.app.application.RequestCode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 文件工具类
 */
public class FileUtils {

    public static String getAvatarImagePath(Context context) {
        return context.getExternalCacheDir() + "/avatar.jpg";
    }

    public static String getExtFilePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + RequestCode.FILE_PATH;
    }

    public static String getExtFileName() {
        return new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date());
    }
}
