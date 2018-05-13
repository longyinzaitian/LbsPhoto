package com.lbsphoto.app.camera.manager.listener;


import com.lbsphoto.app.camera.listeners.CameraFragmentResultListener;

import java.io.File;

public interface CameraPhotoListener {
    void onPhotoTaken(byte[] bytes, File photoFile, CameraFragmentResultListener callback);

    void onPhotoTakeError();
}
