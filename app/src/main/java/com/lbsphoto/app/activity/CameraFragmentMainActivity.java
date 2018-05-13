package com.lbsphoto.app.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lbsphoto.app.R;
import com.lbsphoto.app.camera.CameraFragment;
import com.lbsphoto.app.camera.CameraFragmentApi;
import com.lbsphoto.app.camera.configuration.Configuration;
import com.lbsphoto.app.camera.listeners.CameraFragmentControlsAdapter;
import com.lbsphoto.app.camera.listeners.CameraFragmentResultAdapter;
import com.lbsphoto.app.camera.listeners.CameraFragmentStateAdapter;
import com.lbsphoto.app.camera.listeners.CameraFragmentVideoRecordTextAdapter;
import com.lbsphoto.app.camera.widgets.CameraSettingsView;
import com.lbsphoto.app.camera.widgets.CameraSwitchView;
import com.lbsphoto.app.camera.widgets.FlashSwitchView;
import com.lbsphoto.app.camera.widgets.MediaActionSwitchView;
import com.lbsphoto.app.camera.widgets.RecordButton;
import com.lbsphoto.app.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@SuppressLint("MissingPermission")
public class CameraFragmentMainActivity extends FragmentActivity implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "camera";
    private static final int REQUEST_CAMERA_PERMISSIONS = 931;
    private static final int REQUEST_PREVIEW_CODE = 1001;
    CameraSettingsView settingsView;
    FlashSwitchView flashSwitchView;
    CameraSwitchView cameraSwitchView;
    RecordButton recordButton;
    MediaActionSwitchView mediaActionSwitchView;

    TextView recordDurationText;
    TextView recordSizeText;

    View cameraLayout;
    View addCameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camerafragment_activity_main);
        settingsView = findViewById(R.id.settings_view);
        flashSwitchView = findViewById(R.id.flash_switch_view);
        cameraSwitchView = findViewById(R.id.front_back_camera_switcher);
        recordButton = findViewById(R.id.record_button);
        mediaActionSwitchView = findViewById(R.id.photo_video_camera_switcher);
        recordDurationText = findViewById(R.id.record_duration_text);
        recordSizeText = findViewById(R.id.record_size_mb_text);
        cameraLayout = findViewById(R.id.cameraLayout);

        addCameraButton = findViewById(R.id.addCameraButton);
        addCameraButton.setOnClickListener(this);
        settingsView.setOnClickListener(this);
        flashSwitchView.setOnClickListener(this);
        cameraSwitchView.setOnClickListener(this);
        recordButton.setOnClickListener(this);
        addCameraButton.performClick();
    }

    @Override
    public void onClick(View v) {
        final CameraFragmentApi cameraFragment = getCameraFragment();
        switch (v.getId()) {
            case R.id.flash_switch_view:
                if (cameraFragment != null) {
                    cameraFragment.toggleFlashMode();
                }
                break;

            case R.id.front_back_camera_switcher:
                if (cameraFragment != null) {
                    cameraFragment.switchCameraTypeFrontBack();
                }
                break;

            case R.id.record_button:
                if (cameraFragment != null) {
                    cameraFragment.takePhotoOrCaptureVideo(new CameraFragmentResultAdapter() {
                                                               @Override
                                                               public void onVideoRecorded(String filePath) {
                                                                   Toast.makeText(getBaseContext(), "onVideoRecorded " + filePath, Toast.LENGTH_SHORT).show();
                                                               }

                                                               @Override
                                                               public void onPhotoTaken(byte[] bytes, String filePath) {
                                                                   Toast.makeText(getBaseContext(), "onPhotoTaken " + filePath, Toast.LENGTH_SHORT).show();
                                                                   Intent intent = new Intent();
                                                                   intent.putExtra("file", new File(filePath));
                                                                   setResult(RESULT_OK, intent);
                                                                   finish();
                                                               }
                                                           },
                            FileUtils.getExtFilePath(),
                            FileUtils.getExtFileName());
                }
                break;

            case R.id.settings_view:
                if (cameraFragment != null) {
                    cameraFragment.openSettingDialog();
                }
                break;

            case R.id.photo_video_camera_switcher:
                if (cameraFragment != null) {
                    cameraFragment.switchActionPhotoVideo();
                }
                break;

            case R.id.addCameraButton:
                if (Build.VERSION.SDK_INT > 15) {
                    final String[] permissions = {
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE};

                    final List<String> permissionsToRequest = new ArrayList<>();
                    for (String permission : permissions) {
                        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                            permissionsToRequest.add(permission);
                        }
                    }
                    if (!permissionsToRequest.isEmpty()) {
                        ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[permissionsToRequest.size()]), REQUEST_CAMERA_PERMISSIONS);
                    } else {
                        addCamera();
                    }
                } else {
                    addCamera();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length != 0) {
            addCamera();
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void addCamera() {
        addCameraButton.setVisibility(View.GONE);
        cameraLayout.setVisibility(View.VISIBLE);

        final CameraFragment cameraFragment = CameraFragment.newInstance(new Configuration.Builder()
                .setCamera(Configuration.CAMERA_FACE_REAR).build());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, cameraFragment, FRAGMENT_TAG)
                .commitAllowingStateLoss();

        if (cameraFragment != null) {
            //cameraFragment.setResultListener(new CameraFragmentResultListener() {
            //    @Override
            //    public void onVideoRecorded(String filePath) {
            //        Intent intent = PreviewActivity.newIntentVideo(CameraFragmentMainActivity.this, filePath);
            //        startActivityForResult(intent, REQUEST_PREVIEW_CODE);
            //    }
//
            //    @Override
            //    public void onPhotoTaken(byte[] bytes, String filePath) {
            //        Intent intent = PreviewActivity.newIntentPhoto(CameraFragmentMainActivity.this, filePath);
            //        startActivityForResult(intent, REQUEST_PREVIEW_CODE);
            //    }
            //});

            cameraFragment.setStateListener(new CameraFragmentStateAdapter() {

                @Override
                public void onCurrentCameraBack() {
                    cameraSwitchView.displayBackCamera();
                }

                @Override
                public void onCurrentCameraFront() {
                    cameraSwitchView.displayFrontCamera();
                }

                @Override
                public void onFlashAuto() {
                    flashSwitchView.displayFlashAuto();
                }

                @Override
                public void onFlashOn() {
                    flashSwitchView.displayFlashOn();
                }

                @Override
                public void onFlashOff() {
                    flashSwitchView.displayFlashOff();
                }

                @Override
                public void onCameraSetupForPhoto() {
                    mediaActionSwitchView.displayActionWillSwitchVideo();

                    recordButton.displayPhotoState();
                    flashSwitchView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCameraSetupForVideo() {
                    mediaActionSwitchView.displayActionWillSwitchPhoto();

                    recordButton.displayVideoRecordStateReady();
                    flashSwitchView.setVisibility(View.GONE);
                }

                @Override
                public void shouldRotateControls(int degrees) {
                    ViewCompat.setRotation(cameraSwitchView, degrees);
                    ViewCompat.setRotation(mediaActionSwitchView, degrees);
                    ViewCompat.setRotation(flashSwitchView, degrees);
                    ViewCompat.setRotation(recordDurationText, degrees);
                    ViewCompat.setRotation(recordSizeText, degrees);
                }

                @Override
                public void onRecordStateVideoReadyForRecord() {
                    recordButton.displayVideoRecordStateReady();
                }

                @Override
                public void onRecordStateVideoInProgress() {
                    recordButton.displayVideoRecordStateInProgress();
                }

                @Override
                public void onRecordStatePhoto() {
                    recordButton.displayPhotoState();
                }

                @Override
                public void onStopVideoRecord() {
                    recordSizeText.setVisibility(View.GONE);
                    //cameraSwitchView.setVisibility(View.VISIBLE);
                    settingsView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onStartVideoRecord(File outputFile) {
                }
            });

            cameraFragment.setControlsListener(new CameraFragmentControlsAdapter() {
                @Override
                public void lockControls() {
                    cameraSwitchView.setEnabled(false);
                    recordButton.setEnabled(false);
                    settingsView.setEnabled(false);
                    flashSwitchView.setEnabled(false);
                }

                @Override
                public void unLockControls() {
                    cameraSwitchView.setEnabled(true);
                    recordButton.setEnabled(true);
                    settingsView.setEnabled(true);
                    flashSwitchView.setEnabled(true);
                }

                @Override
                public void allowCameraSwitching(boolean allow) {
                    cameraSwitchView.setVisibility(allow ? View.VISIBLE : View.GONE);
                }

                @Override
                public void allowRecord(boolean allow) {
                    recordButton.setEnabled(allow);
                }

                @Override
                public void setMediaActionSwitchVisible(boolean visible) {
                    mediaActionSwitchView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
                }
            });

            cameraFragment.setTextListener(new CameraFragmentVideoRecordTextAdapter() {
                @Override
                public void setRecordSizeText(long size, String text) {
                    recordSizeText.setText(text);
                }

                @Override
                public void setRecordSizeTextVisible(boolean visible) {
                    recordSizeText.setVisibility(visible ? View.VISIBLE : View.GONE);
                }

                @Override
                public void setRecordDurationText(String text) {
                    recordDurationText.setText(text);
                }

                @Override
                public void setRecordDurationTextVisible(boolean visible) {
                    recordDurationText.setVisibility(visible ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    private CameraFragmentApi getCameraFragment() {
        return (CameraFragmentApi) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }
}
