package com.lbsphoto.app.camera.listeners;


public interface CameraFragmentResultListener {

    //Called when the video record is finished and saved
    void onVideoRecorded(String filePath);

    //called when the photo is taken and saved
    void onPhotoTaken(byte[] bytes, String filePath);
}
