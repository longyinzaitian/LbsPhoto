package com.lbsphoto.app.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.lbsphoto.app.R;
import com.lbsphoto.app.widget.CameraPreview;

public class CameraActivity extends BaseActivity {

    private CameraPreview cameraPreview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cameraPreview = findViewById(R.id.cameraView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraPreview.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraPreview.onPause();
    }

    public void takePic(View view) {
        cameraPreview.takePicture();
    }
}
