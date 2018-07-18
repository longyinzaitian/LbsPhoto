package com.lbsphoto.app.camera;

import android.Manifest;
import android.support.annotation.RequiresPermission;

import com.lbsphoto.app.camera.configuration.Configuration;
import com.lbsphoto.app.camera.ui.BaseAnncaFragment;

public class CameraFragment extends BaseAnncaFragment {

    @RequiresPermission(Manifest.permission.CAMERA)
    public static CameraFragment newInstance(Configuration configuration) {
        return (CameraFragment) BaseAnncaFragment.newInstance(new CameraFragment(), configuration);
    }
}
