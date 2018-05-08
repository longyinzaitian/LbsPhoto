package com.lbsphoto.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.lbsphoto.app.R;
import com.lbsphoto.app.util.FileUtils;
import com.lbsphoto.app.widget.CameraPreview;

import java.io.File;

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
        final File tmpCameraFile = new File(FileUtils.getExtFilePath(), FileUtils.getExtFileName()+".jpg");
        cameraPreview.setOutPutDir(tmpCameraFile);
        cameraPreview.setImpCaptureEnd(new CameraPreview.imlCaptureEnd() {
            @Override
            public void getCaptureEnd() {
                Intent intent = new Intent();
                intent.putExtra("file", tmpCameraFile);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        cameraPreview.takePicture();
    }
}
