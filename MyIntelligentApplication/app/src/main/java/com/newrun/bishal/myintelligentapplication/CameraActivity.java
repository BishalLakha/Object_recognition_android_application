package com.newrun.bishal.myintelligentapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.newrun.myintelligentapplication.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraActivity extends AppCompatActivity {

    @BindView(R.id.layoutCameraView)
    FrameLayout layoutCameraView;
    @BindView(R.id.ivCapturedImage)
    ImageView ivCapturedImage;
    @BindView(R.id.tvResult)
    TextView tvResult;

    private Camera mCamera;
    private CameraView mCameraView;

    private int cameraId = CameraInfo.CAMERA_FACING_FRONT;

    private Recognizer recognizer = null;

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

        recognizer = new Recognizer(CameraActivity.this);

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

    private void captureAndPredict() {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {

                camera.startPreview();

                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap rotatedBitmap = null;
                if (bmp == null) {
                    return;
                }
                if (cameraId == CameraInfo.CAMERA_FACING_FRONT) {
                    rotatedBitmap = ImageUtils.rotateBitmap(bmp, -90);
                } else {
                    rotatedBitmap = ImageUtils.rotateBitmap(bmp, 90);
                }

                if (rotatedBitmap != null) {
                    ivCapturedImage.setImageBitmap(rotatedBitmap);

                    //Predict
                    try {
                        recognizer.predict(rotatedBitmap, new Recognizer.OnPredictedCallback() {
                            @Override
                            public void onPredictionComplete(final String confidence, final String label) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvResult.setText(label + " : " + confidence + "%");
                                    }
                                });
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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
        captureAndPredict();
    }

    @OnClick(R.id.btnSwitchCamera)
    public void onBtnSwitchCameraClicked() {
        switchAndStartCamera();
    }
}
