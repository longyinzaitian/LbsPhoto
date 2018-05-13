package com.lbsphoto.app.camera.listeners;


public interface CameraFragmentControlsListener {
    void lockControls();
    void unLockControls();
    void allowCameraSwitching(boolean allow);
    void allowRecord(boolean allow);
    void setMediaActionSwitchVisible(boolean visible);
}
