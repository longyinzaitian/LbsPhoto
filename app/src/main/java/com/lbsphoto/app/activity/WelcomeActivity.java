package com.lbsphoto.app.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lbsphoto.app.R;
import com.lbsphoto.app.application.LbsPhotoApplication;
import com.lbsphoto.app.util.LogUtils;
import com.lbsphoto.app.util.PermissionReq;

/**
 * @author husyin
 * @date 2018/5/2
 */
public class WelcomeActivity extends BaseActivity {
    private static final String TAG = "WelcomeActivity";
    private ImageView mWelcomePic;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mWelcomePic = findViewById(R.id.welcome_image);
        permissionNeed();
        Glide.with(LbsPhotoApplication.getAppContext())
                .load("http://img1.sc115.com/uploads/sc/jpg/HD/2/122.jpg")
                .into(mWelcomePic);
    }

    void permissionNeed() {
        PermissionReq.with(WelcomeActivity.this)
                .permissions(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .result(new PermissionReq.Result() {
                    @Override
                    public void onGranted() {
                        LogUtils.i(TAG, "onGranted()");
                        mWelcomePic.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                                finish();
                            }
                        }, 2000);
                    }

                    @Override
                    public void onDenied() {
                        LogUtils.i(TAG, "onDenied()");
                        finish();
                    }
                }).request();
    }
}
