package com.lbsphoto.app.camera;

import android.support.annotation.Nullable;

import com.lbsphoto.app.camera.listeners.CameraFragmentControlsListener;
import com.lbsphoto.app.camera.listeners.CameraFragmentResultListener;
import com.lbsphoto.app.camera.listeners.CameraFragmentStateListener;
import com.lbsphoto.app.camera.listeners.CameraFragmentVideoRecordTextListener;
import com.lbsphoto.app.camera.ui.model.PhotoQualityOption;


public interface CameraFragmentApi {

    void takePhotoOrCaptureVideo(CameraFragmentResultListener resultListener, @Nullable String directoryPath, @Nullable String fileName);

    void openSettingDialog();

    PhotoQualityOption[] getPhotoQualities();

    void switchCameraTypeFrontBack();

    void switchActionPhotoVideo();

    void toggleFlashMode();

    void setStateListener(CameraFragmentStateListener cameraFragmentStateListener);

    void setTextListener(CameraFragmentVideoRecordTextListener cameraFragmentVideoRecordTextListener);

    void setControlsListener(CameraFragmentControlsListener cameraFragmentControlsListener);

    void setResultListener(CameraFragmentResultListener cameraFragmentResultListener);

}
