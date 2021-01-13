package megvii.testfacepass.independent.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.AttributeSet;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.core.ScanResult;

public class QRCodeView2/* extends QRCodeView */{
/*
    public QRCodeView2(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public QRCodeView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void setupReader() {

    }



    @Override
    protected ScanResult processBitmapData(Bitmap bitmap) {
        return null;
    }

    public void startCamera() {
        if (mCamera != null) {
            return;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraId == 0 ) {
                startCameraById(cameraId);
                break;
            }
        }


        *//*int numberOfCameras  = Camera.getNumberOfCameras();
        for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraId == 0 && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                // 前置摄像头
                mCamera = Camera.open(cameraId);//  打开摄像头
                mPreview.setCamera(mCamera);
            }
        }*//*
    }

    private void startCameraById(int cameraId) {
        try {
            mCamera = Camera.open(cameraId);
            mPreview.setCamera(mCamera);
        } catch (Exception e) {
            if (mDelegate != null) {
                mDelegate.onScanQRCodeOpenCameraError();
            }
        }
    }

    @Override
    protected ScanResult processData(byte[] data, int width, int height, boolean isRetry) {
        return null;
    }*/

}
