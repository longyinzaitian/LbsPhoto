package com.lbsphoto.app.camera.manager.listener;


import com.lbsphoto.app.camera.utils.Size;

public interface CameraOpenListener<CameraId, SurfaceListener> {
    void onCameraOpened(CameraId openedCameraId, Size previewSize, SurfaceListener surfaceListener);

    void onCameraOpenError();
}
