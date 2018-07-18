package com.lbsphoto.app.camera.controller.impl;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;

import com.lbsphoto.app.camera.configuration.Configuration;
import com.lbsphoto.app.camera.configuration.ConfigurationProvider;
import com.lbsphoto.app.camera.controller.CameraController;
import com.lbsphoto.app.camera.controller.view.CameraView;
import com.lbsphoto.app.camera.listeners.CameraFragmentResultListener;
import com.lbsphoto.app.camera.manager.CameraManager;
import com.lbsphoto.app.camera.manager.impl.Camera1Manager;
import com.lbsphoto.app.camera.manager.listener.CameraCloseListener;
import com.lbsphoto.app.camera.manager.listener.CameraOpenListener;
import com.lbsphoto.app.camera.manager.listener.CameraPhotoListener;
import com.lbsphoto.app.camera.manager.listener.CameraVideoListener;
import com.lbsphoto.app.camera.ui.view.AutoFitSurfaceView;
import com.lbsphoto.app.camera.utils.CameraHelper;
import com.lbsphoto.app.camera.utils.Size;

import java.io.File;


@SuppressWarnings("deprecation")
public class Camera1Controller implements CameraController<Integer>,
        CameraOpenListener<Integer, SurfaceHolder.Callback>, CameraPhotoListener, CameraCloseListener<Integer>, CameraVideoListener {

    private final static String TAG = "Camera1Controller";

    private final Context context;

    private Integer currentCameraId;
    private ConfigurationProvider configurationProvider;
    private CameraManager<Integer, SurfaceHolder.Callback> cameraManager;
    private CameraView cameraView;

    private File outputFile;

    public Camera1Controller(Context context, CameraView cameraView, ConfigurationProvider configurationProvider) {
        this.context = context;
        this.cameraView = cameraView;
        this.configurationProvider = configurationProvider;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        cameraManager = new Camera1Manager();
        cameraManager.initializeCameraManager(configurationProvider, context);
        setCurrentCameraId(cameraManager.getFaceBackCameraId());
    }

    private void setCurrentCameraId(Integer cameraId) {
        this.currentCameraId = cameraId;
        cameraManager.setCameraId(cameraId);
    }

    @Override
    public void onResume() {
        cameraManager.openCamera(currentCameraId, this);
    }

    @Override
    public void onPause() {
        cameraManager.closeCamera(null);
    }

    @Override
    public void onDestroy() {
        cameraManager.releaseCameraManager();
    }

    @Override
    public void takePhoto(CameraFragmentResultListener callback) {
        takePhoto(callback, null, null);
    }

    @Override
    public void takePhoto(CameraFragmentResultListener callback, @Nullable String direcoryPath, @Nullable String fileName) {
        outputFile = CameraHelper.getOutputMediaFile(context, Configuration.MEDIA_ACTION_PHOTO, direcoryPath, fileName);
        cameraManager.takePhoto(outputFile, this, callback);
    }

    @Override
    public void startVideoRecord() {
        startVideoRecord(null, null);
    }

    @Override
    public void startVideoRecord(@Nullable String direcoryPath, @Nullable String fileName) {
        outputFile = CameraHelper.getOutputMediaFile(context, Configuration.MEDIA_ACTION_VIDEO, direcoryPath, fileName);
        cameraManager.startVideoRecord(outputFile, this);
    }

    @Override
    public void stopVideoRecord(CameraFragmentResultListener callback) {
        cameraManager.stopVideoRecord(callback);
    }

    @Override
    public boolean isVideoRecording() {
        return cameraManager.isVideoRecording();
    }

    @Override
    public void switchCamera(@Configuration.CameraFace final int cameraFace) {
        final Integer backCameraId = cameraManager.getFaceBackCameraId();
        final Integer frontCameraId = cameraManager.getFaceFrontCameraId();
        final Integer currentCameraId = cameraManager.getCurrentCameraId();

        if (cameraFace == Configuration.CAMERA_FACE_REAR && backCameraId != null) {
            setCurrentCameraId(backCameraId);
            cameraManager.closeCamera(this);
        } else if (frontCameraId != null && !frontCameraId.equals(currentCameraId)) {
            setCurrentCameraId(frontCameraId);
            cameraManager.closeCamera(this);
        }
    }

    @Override
    public void setFlashMode(@Configuration.FlashMode int flashMode) {
        cameraManager.setFlashMode(flashMode);
    }

    @Override
    public void switchQuality() {
        cameraManager.closeCamera(this);
    }

    @Override
    public int getNumberOfCameras() {
        return cameraManager.getNumberOfCameras();
    }

    @Override
    public int getMediaAction() {
        return configurationProvider.getMediaAction();
    }

    @Override
    public File getOutputFile() {
        return outputFile;
    }

    @Override
    public Integer getCurrentCameraId() {
        return currentCameraId;
    }

    @Override
    public void onCameraOpened(Integer cameraId, Size previewSize, SurfaceHolder.Callback surfaceCallback) {
        cameraView.updateUiForMediaAction(configurationProvider.getMediaAction());
        cameraView.updateCameraPreview(previewSize, new AutoFitSurfaceView(context, surfaceCallback));
        cameraView.updateCameraSwitcher(getNumberOfCameras());
    }

    @Override
    public void onCameraOpenError() {
        Log.e(TAG, "onCameraOpenError");
    }

    @Override
    public void onCameraClosed(Integer closedCameraId) {
        cameraView.releaseCameraPreview();

        cameraManager.openCamera(currentCameraId, this);
    }

    @Override
    public void onPhotoTaken(byte[] bytes, File photoFile, CameraFragmentResultListener callback) {
        cameraView.onPhotoTaken(bytes, callback);
    }

    @Override
    public void onPhotoTakeError() {
    }

    @Override
    public void onVideoRecordStarted(Size videoSize) {
        cameraView.onVideoRecordStart(videoSize.getWidth(), videoSize.getHeight());
    }

    @Override
    public void onVideoRecordStopped(File videoFile, @Nullable CameraFragmentResultListener callback) {
        cameraView.onVideoRecordStop(callback);
    }

    @Override
    public void onVideoRecordError() {

    }

    @Override
    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public CharSequence[] getVideoQualityOptions() {
        return cameraManager.getVideoQualityOptions();
    }

    @Override
    public CharSequence[] getPhotoQualityOptions() {
        return cameraManager.getPhotoQualityOptions();
    }
}
