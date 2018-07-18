package com.lbsphoto.app.camera.manager.listener;

public interface CameraCloseListener<CameraId> {
    void onCameraClosed(CameraId closedCameraId);
}
