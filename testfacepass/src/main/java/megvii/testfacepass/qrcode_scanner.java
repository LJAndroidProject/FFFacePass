package megvii.testfacepass;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zbar.ZBarView;
import megvii.testfacepass.independent.view.QRCodeView2;

public class qrcode_scanner extends AppCompatActivity {
    private QRCodeView mQRCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //  全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_qrcode_scanner);

        mQRCodeView = (ZBarView) findViewById(R.id.zbarview);
        mQRCodeView.setDelegate(new QRCodeView.Delegate() {
            @Override
            public void onScanQRCodeSuccess(String result) {
                //  扫描成功触发震动

                //  Toast.makeText(qrcode_scanner.this,result,Toast.LENGTH_SHORT).show();
                Log.i("结果",result);
                rest();
            }

            @Override
            public void onCameraAmbientBrightnessChanged(boolean isDark) {

            }

            @Override
            public void onScanQRCodeOpenCameraError() {
                Toast.makeText(qrcode_scanner.this,"打开相机失败",Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        //  打开相机
        mQRCodeView.startCamera(0);
        //  显示扫描框
        mQRCodeView.showScanRect();
        //  开始识别二维码
        mQRCodeView.startSpot();

        //  开灯
        //mQRCodeView.openFlashlight();
        //  关灯
        //mQRCodeView.closeFlashlight();
    }

    public void rest(){
        /*mQRCodeView.stopCamera();


        mQRCodeView.startCamera(0);
        //  显示扫描框
        mQRCodeView.showScanRect();
        //  开始识别二维码
        mQRCodeView.startSpot();*/

        mQRCodeView.stopSpot();

        mQRCodeView.startSpot();
    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        super.onDestroy();
    }

    /**
     * 扫描二维码触发震动
     * */
    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        if(vibrator != null){
            vibrator.vibrate(200);
        }

    }
}