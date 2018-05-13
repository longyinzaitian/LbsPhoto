package com.lbsphoto.app.camera.manager;

import android.content.Context;


import com.lbsphoto.app.camera.configuration.Configuration;
import com.lbsphoto.app.camera.configuration.ConfigurationProvider;
import com.lbsphoto.app.camera.listeners.CameraFragmentResultListener;
import com.lbsphoto.app.camera.manager.listener.CameraCloseListener;
import com.lbsphoto.app.camera.manager.listener.CameraOpenListener;
import com.lbsphoto.app.camera.manager.listener.CameraPhotoListener;
import com.lbsphoto.app.camera.manager.listener.CameraVideoListener;
import com.lbsphoto.app.camera.utils.Size;

import java.io.File;

public interface CameraManager<CameraId, SurfaceListener> {

    void initializeCameraManager(ConfigurationProvider configurationProvider, Context context);

    void openCamera(CameraId cameraId, CameraOpenListener<CameraId, SurfaceListener> cameraOpenListener);

    void closeCamera(CameraCloseListener<CameraId> cameraCloseListener);

    void setFlashMode(@Configuration.FlashMode int flashMode);

    void takePhoto(File photoFile, CameraPhotoListener cameraPhotoListener, CameraFragmentResultListener callback);

    void startVideoRecord(File videoFile, CameraVideoListener cameraVideoListener);

    Size getPhotoSizeForQuality(@Configuration.MediaQuality int mediaQuality);

    void stopVideoRecord(CameraFragmentResultListener callback);

    void releaseCameraManager();


    CameraId getCurrentCameraId();

    CameraId getFaceFrontCameraId();

    CameraId getFaceBackCameraId();

    int getNumberOfCameras();

    int getFaceFrontCameraOrientation();

    int getFaceBackCameraOrientation();

    boolean isVideoRecording();

    CharSequence[] getVideoQualityOptions();

    CharSequence[] getPhotoQualityOptions();

    void setCameraId(CameraId currentCameraId);
}
