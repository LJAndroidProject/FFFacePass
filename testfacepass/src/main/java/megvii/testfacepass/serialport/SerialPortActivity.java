package megvii.testfacepass.serialport;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.serialport.SerialPortFinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.serialportlibrary.service.impl.SerialPortBuilder;
import com.serialportlibrary.service.impl.SerialPortService;

import megvii.testfacepass.R;

public class SerialPortActivity extends AppCompatActivity {

    TextView tv_hello;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_serialport_layout);


        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(SerialPortActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                //没有权限则申请权限
                ActivityCompat.requestPermissions(SerialPortActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }else {
                //有权限直接执行,docode()不用做处理
                doCode();

            }
        }else {
            //小于6.0，不用申请权限，直接执行
            doCode();
        }



    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //执行代码,这里是已经申请权限成功了,可以不用做处理
                    doCode();

                }else{

                }
                break;
        }
    }


    private void doCode(){
        tv_hello = (TextView) findViewById(R.id.tv_hello);

        //获取所有串口名字
        String[] devices = new SerialPortFinder().getDevices();
        //获取所用串口地址
        String[] devicesPath = new SerialPortFinder().getDevicesPaths();


        if(devicesPath != null && devicesPath.length != 0) {
            for (String path : devicesPath) {
                Log.e("MainActivity：", path);
            }
        }


        if(devices != null && devices.length != 0){
            for (String device : devices) {
                Log.e("MainActivity：", device);
            }
        }

        tv_hello.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                SerialPortService serialPortService = new SerialPortBuilder()
                        .setTimeOut(100L)
                        .setBaudrate(9600)
                        .setDevicePath("dev/ttyS2")
                        .createService();
                serialPortService.isOutputLog(true);

                serialPortService.sendData("F1 1F 00 01 01 01 01 11");

                //发送开门指令
                //  byte[] receiveData = serialPortService.sendData("55AA0101010002");
                //  Log.e("MainActivity：", ByteStringUtil.byteArrayToHexStr(receiveData));


                //  串口监听
                serialPortService.receiveThread(new SerialPortService.SerialResponseListener() {
                    @Override
                    public void response(String response) {

                    }
                });

                //  Toast.makeText(SerialPortActivity.this,"发送数据55AA0101010002",Toast.LENGTH_LONG).show();

            }
        });
    }



}
