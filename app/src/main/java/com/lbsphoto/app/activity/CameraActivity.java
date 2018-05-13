package com.lbsphoto.app.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.lbsphoto.app.R;
import com.lbsphoto.app.util.FileUtils;

import java.io.File;

public class CameraActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void finish(View view) {
        finish();
    }

    public void takePic(View view) {
        final File tmpCameraFile = new File(FileUtils.getExtFilePath(), FileUtils.getExtFileName()+".jpg");
    }
}
