package com.lbsphoto.app.camera.manager.listener;


import com.lbsphoto.app.camera.listeners.CameraFragmentResultListener;
import com.lbsphoto.app.camera.utils.Size;

import java.io.File;


public interface CameraVideoListener {
    void onVideoRecordStarted(Size videoSize);

    void onVideoRecordStopped(File videoFile, CameraFragmentResultListener callback);

    void onVideoRecordError();
}
