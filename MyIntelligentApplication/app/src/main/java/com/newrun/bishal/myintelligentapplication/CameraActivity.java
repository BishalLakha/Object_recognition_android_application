package com.newrun.bishal.myintelligentapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.newrun.myintelligentapplication.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraActivity extends AppCompatActivity {

    @BindView(R.id.layoutCameraView)
    RelativeLayout layoutCameraView;

    private Camera mCamera;
    private CameraView mCameraView;

    private int cameraId = CameraInfo.CAMERA_FACING_FRONT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 0);
        }

        switchAndStartCamera();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void switchAndStartCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }

        if (cameraId == CameraInfo.CAMERA_FACING_BACK) {
            cameraId = getCamera(CameraInfo.CAMERA_FACING_FRONT);
        } else {
            cameraId = getCamera(CameraInfo.CAMERA_FACING_BACK);
        }

        try {
            mCamera = Camera.open(cameraId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            mCamera.setParameters(params);

            mCameraView = new CameraView(this, mCamera);//create a SurfaceView to show camera data
            layoutCameraView.addView(mCameraView);//add the SurfaceView to the layout
        }
    }

    private int getCamera(int cameraFacing) {

        int cameraId = -1;

        int noOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < noOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);

            if (info.facing == cameraFacing) {
                cameraId = i;
                break;
            }
        }

        return cameraId;
    }

    @Override
    protected void onPause() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        super.onPause();
    }

    @OnClick(R.id.btnCaptureAndPredict)
    public void onBtnCaptureAndPredictClicked() {

    }

    @OnClick(R.id.btnSwitchCamera)
    public void onBtnSwitchCameraClicked() {
        switchAndStartCamera();
    }
}
