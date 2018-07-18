package com.lbsphoto.app.camera.controller.impl;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;


import com.lbsphoto.app.camera.configuration.Configuration;
import com.lbsphoto.app.camera.configuration.ConfigurationProvider;
import com.lbsphoto.app.camera.controller.CameraController;
import com.lbsphoto.app.camera.controller.view.CameraView;
import com.lbsphoto.app.camera.listeners.CameraFragmentResultListener;
import com.lbsphoto.app.camera.manager.CameraManager;
import com.lbsphoto.app.camera.manager.impl.Camera2Manager;
import com.lbsphoto.app.camera.manager.listener.CameraCloseListener;
import com.lbsphoto.app.camera.manager.listener.CameraOpenListener;
import com.lbsphoto.app.camera.manager.listener.CameraPhotoListener;
import com.lbsphoto.app.camera.manager.listener.CameraVideoListener;
import com.lbsphoto.app.camera.ui.view.AutoFitTextureView;
import com.lbsphoto.app.camera.utils.CameraHelper;
import com.lbsphoto.app.camera.utils.Size;

import java.io.File;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Controller implements CameraController<String>,
        CameraOpenListener<String, TextureView.SurfaceTextureListener>,
        CameraPhotoListener, CameraVideoListener, CameraCloseListener<String> {

    private final static String TAG = "Camera2Controller";

    private final Context context;
    private String currentCameraId;
    private ConfigurationProvider configurationProvider;
    private CameraManager<String, TextureView.SurfaceTextureListener> camera2Manager;
    private CameraView cameraView;

    private File outputFile;

    public Camera2Controller(Context context, CameraView cameraView, ConfigurationProvider configurationProvider) {
        this.context = context;
        this.cameraView = cameraView;
        this.configurationProvider = configurationProvider;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        camera2Manager = new Camera2Manager();
        camera2Manager.initializeCameraManager(configurationProvider, context);
        setCurrentCameraId(camera2Manager.getFaceBackCameraId());
    }

    @Override
    public void onResume() {
        camera2Manager.openCamera(currentCameraId, this);
    }

    @Override
    public void onPause() {
        camera2Manager.closeCamera(null);
        cameraView.releaseCameraPreview();
    }

    @Override
    public void onDestroy() {
        camera2Manager.releaseCameraManager();
    }

    @Override
    public void takePhoto(CameraFragmentResultListener callback) {
        takePhoto(callback, null, null);
    }

    @Override
    public void takePhoto(CameraFragmentResultListener callback, @Nullable String direcoryPath, @Nullable String fileName) {
        outputFile = CameraHelper.getOutputMediaFile(context, Configuration.MEDIA_ACTION_PHOTO, direcoryPath, fileName);
        camera2Manager.takePhoto(outputFile, this, callback);
    }

    @Override
    public void startVideoRecord() {
        startVideoRecord(null, null);
    }

    @Override
    public void startVideoRecord(@Nullable String direcoryPath, @Nullable String fileName) {
        outputFile = CameraHelper.getOutputMediaFile(context, Configuration.MEDIA_ACTION_VIDEO, direcoryPath, fileName);
        camera2Manager.startVideoRecord(outputFile, this);
    }

    @Override
    public void stopVideoRecord(CameraFragmentResultListener callback) {
        camera2Manager.stopVideoRecord(callback);
    }

    @Override
    public boolean isVideoRecording() {
        return camera2Manager.isVideoRecording();
    }

    @Override
    public void switchCamera(final @Configuration.CameraFace int cameraFace) {
        final String currentCameraId = camera2Manager.getCurrentCameraId();
        final String faceFrontCameraId = camera2Manager.getFaceFrontCameraId();
        final String faceBackCameraId = camera2Manager.getFaceBackCameraId();

        if (cameraFace == Configuration.CAMERA_FACE_REAR && faceBackCameraId != null) {
            setCurrentCameraId(faceBackCameraId);
            camera2Manager.closeCamera(this);
        } else if (faceFrontCameraId != null) {
            setCurrentCameraId(faceFrontCameraId);
            camera2Manager.closeCamera(this);
        }

    }

    private void setCurrentCameraId(String currentCameraId){
        this.currentCameraId = currentCameraId;
        camera2Manager.setCameraId(currentCameraId);
    }

    @Override
    public void setFlashMode(@Configuration.FlashMode int flashMode) {
        camera2Manager.setFlashMode(flashMode);
    }

    @Override
    public void switchQuality() {
        camera2Manager.closeCamera(this);
    }

    @Override
    public int getNumberOfCameras() {
        return camera2Manager.getNumberOfCameras();
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
    public String getCurrentCameraId() {
        return currentCameraId;
    }

    @Override
    public void onCameraOpened(String openedCameraId, Size previewSize, TextureView.SurfaceTextureListener surfaceTextureListener) {
        cameraView.updateUiForMediaAction(Configuration.MEDIA_ACTION_UNSPECIFIED);
        cameraView.updateCameraPreview(previewSize, new AutoFitTextureView(context, surfaceTextureListener));
        cameraView.updateCameraSwitcher(camera2Manager.getNumberOfCameras());
    }

    @Override
    public void onCameraOpenError() {
        Log.e(TAG, "onCameraOpenError");
    }

    @Override
    public void onCameraClosed(String closedCameraId) {
        cameraView.releaseCameraPreview();

        camera2Manager.openCamera(currentCameraId, this);
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
    public CameraManager<String, TextureView.SurfaceTextureListener> getCameraManager() {
        return camera2Manager;
    }

    @Override
    public CharSequence[] getVideoQualityOptions() {
        return camera2Manager.getVideoQualityOptions();
    }

    @Override
    public CharSequence[] getPhotoQualityOptions() {
        return camera2Manager.getPhotoQualityOptions();
    }
}
