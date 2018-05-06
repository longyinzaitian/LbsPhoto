package com.lbsphoto.app.application;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.lbsphoto.app.bean.User;
import com.lbsphoto.app.util.ToastUtils;

/**
 * @author husyin
 * @date 2018/5/2
 */
public class LbsPhotoApplication extends Application {
    private static LbsPhotoApplication mContext;
    public static User user;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        SDKInitializer.initialize(getApplicationContext());
        ToastUtils.init(this);
    }

    public static LbsPhotoApplication getAppContext() {
        return mContext;
    }
}
