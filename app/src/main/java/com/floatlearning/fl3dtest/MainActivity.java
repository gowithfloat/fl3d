package com.floatlearning.fl3dtest;

import android.app.Activity;

import com.floatlearning.glcameraview.GLCameraView;

public class MainActivity extends Activity {
    private GLCameraView cameraView;

    @Override
    protected void onResume() {
        super.onResume();

        // create a simple FL3D scene
        cameraView = new GLCameraView(this);
        setContentView(cameraView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // tear down our scene
        cameraView.dispose();
    }
}
