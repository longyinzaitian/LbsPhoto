package com.lbsphoto.app.application;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * @author husyin
 * @date 2018/5/2
 */
public class LbsPhotoApplication extends Application {
    private static LbsPhotoApplication mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        SDKInitializer.initialize(getApplicationContext());
    }

    public static LbsPhotoApplication getAppContext() {
        return mContext;
    }
}
