package megvii.testfacepass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.bean.PicturePath;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.serialportlibrary.util.ByteStringUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.bean.DebugLogBean;
import megvii.testfacepass.independent.manage.SerialPortRequestByteManage;
import megvii.testfacepass.independent.manage.SerialPortResponseManage;
import megvii.testfacepass.independent.util.SerialPortUtil;
import megvii.testfacepass.independent.util.VendingUtil;

public class DebugActivity extends AppCompatActivity {
    private Button debug_send_btn;
    public static TextView debug_log_tv;
    private EditText target_door_number;
    private TextView stateBugTextView;
    private TextureView debug_textTueView;
    private UVCCameraProxy mUVCCamera;
    private UsbDevice mUsbDevice;
    private TextView debug_camera_textTueView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        EventBus.getDefault().register(this);

        debug_send_btn = (Button)findViewById(R.id.debug_send_btn);
        debug_log_tv = (TextView) findViewById(R.id.debug_log_tv);
        target_door_number = (EditText)findViewById(R.id.target_door_number);
        debug_textTueView = (TextureView)findViewById(R.id.debug_textTueView);
        debug_camera_textTueView = (TextView)findViewById(R.id.debug_camera_textTueView);


        mUVCCamera = new UVCCameraProxy(this);
        mUVCCamera.getConfig()
                .isDebug(true) // 是否调试
                .setPicturePath(PicturePath.APPCACHE) // 图片保存路径，保存在app缓存还是sd卡
                .setDirName("uvccamera") // 图片保存目录名称
                .setProductId(0) // 产品id，用于过滤设备，不需要可不设置 37424
                .setVendorId(0); // 供应商id，用于过滤设备，不需要可不设置 1443

        mUVCCamera.setPreviewTexture(debug_textTueView); // TextureView

        //  默认摄像头
        mUsbDevice = getUsbCameraDevice(ControlActivity.hexToInt(1));
        mUVCCamera.requestPermission(mUsbDevice);


        mUVCCamera.setConnectCallback(new ConnectCallback() {
            @Override
            public void onAttached(UsbDevice usbDevice) {


                mUVCCamera.requestPermission(mUsbDevice); // USB设备授权
            }

            @Override
            public void onGranted(UsbDevice usbDevice, boolean granted) {
                mUVCCamera.connectDevice(mUsbDevice);
                // 外置摄像头是/dev/bus/usb/001/021
            }

            @Override
            public void onConnected(UsbDevice usbDevice) {
                mUVCCamera.openCamera(); // 打开相机
            }

            @Override
            public void onCameraOpened() {
                //  拍出来的1图片大小
                mUVCCamera.setPreviewSize(640, 480); // 设置预览尺寸
                mUVCCamera.startPreview(); // 开始预览
            }

            @Override
            public void onDetached(UsbDevice usbDevice) {
                mUVCCamera.closeCamera(); // 关闭相机
            }
        });


        target_door_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(TextUtils.isEmpty(s.toString())){
                    return;
                }

                int doorNumber = Integer.parseInt(s.toString());
                if(doorNumber >= 1 && doorNumber <= 4){
                    mUVCCamera.closeCamera();

                    //  切换摄像头
                    mUsbDevice = getUsbCameraDevice(ControlActivity.hexToInt(doorNumber));
                    mUVCCamera.requestPermission(mUsbDevice);

                    Toast.makeText(DebugActivity.this, "切换摄像头" + doorNumber, Toast.LENGTH_SHORT).show();
                }
            }
        });


        stateBugTextView = new TextView(this);
        int padding = 20;
        stateBugTextView.setPadding(padding,padding,padding,padding);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("垃圾箱状态");
        alert.setView(stateBugTextView);
        alertState = alert.create();

        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        if(APP.dustbinBeanList != null && APP.dustbinBeanList.size() > 0){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    stateBugTextView.setText(null);
                                    stateBugTextView.append("\n");
                                    stateBugTextView.append(String.valueOf(System.currentTimeMillis()));
                                    stateBugTextView.append("\n");
                                }
                            });
                            for(final DustbinStateBean dustbinStateBean : APP.dustbinBeanList){
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        stateBugTextView.append(dustbinStateBean.toString());
                                        stateBugTextView.append("\n");
                                    }
                                });
                            }
                        }

                        Thread.sleep(1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }).start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(APP.dustbinBeanList != null && APP.dustbinBeanList.size() > 0){
                    APP.dustbinBeanList.get(0).setTemperature(110);
                }



                String str = "F3 3F 00 01 02 02 05 00 00 01 00 40 86 F4 4F".replaceAll(" ","");
                SerialPortResponseManage.inOrderString(DebugActivity.this,ByteStringUtil.hexStrToByteArray(str));
            }
        },3000);


        // 如果垃圾箱为 null 则创建
        if(APP.dustbinBeanList == null || APP.dustbinBeanList .size() == 0){
            List<DustbinStateBean> dustbinStateBeans = new ArrayList<>();
            for(int i = 1 ;i <= 4 ;i++){
                DustbinStateBean dustbinStateBean = new DustbinStateBean();
                dustbinStateBean.setDoorNumber(i);
                dustbinStateBeans.add(dustbinStateBean);
            }

            APP.dustbinBeanList = dustbinStateBeans;
        }


        debug_log_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debug_log_tv.setText(null);
                Toast.makeText(DebugActivity.this, "已清空", Toast.LENGTH_SHORT).show();
            }
        });

        debug_send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(target_door_number.getText())){
                    Toast.makeText(DebugActivity.this, "门号不能为空。", Toast.LENGTH_SHORT).show();
                    return;
                }

                int door = Integer.parseInt(target_door_number.getText().toString());

                final byte[][] array = new byte[][]{
                        SerialPortRequestByteManage.getInstance().openDoor(door),
                        SerialPortRequestByteManage.getInstance().closeDoor(door),

                        SerialPortRequestByteManage.getInstance().getDate(door),

                        SerialPortRequestByteManage.getInstance().openTheDisinfection(door),
                        SerialPortRequestByteManage.getInstance().closeTheDisinfection(door),

                        SerialPortRequestByteManage.getInstance().openLight(door),
                        SerialPortRequestByteManage.getInstance().closeLight(door),

                        SerialPortRequestByteManage.getInstance().openExhaustFan(door),
                        SerialPortRequestByteManage.getInstance().closeExhaustFan(door),

                        SerialPortRequestByteManage.getInstance().openElectromagnetism(door),
                        SerialPortRequestByteManage.getInstance().closeElectromagnetism(door),

                        SerialPortRequestByteManage.getInstance().openTheHeating(door),
                        SerialPortRequestByteManage.getInstance().closeTheHeating(door),

                        SerialPortRequestByteManage.getInstance().openBlender(door),
                        SerialPortRequestByteManage.getInstance().closeBlender(door),

                        SerialPortRequestByteManage.getInstance().openDogHouse(door),
                        SerialPortRequestByteManage.getInstance().closeDogHouse(door),


                        VendingUtil.transmitJoint(VendingUtil.getDeliveryByte(1),door),

                        VendingUtil.transmitJoint(VendingUtil.getDeliveryByte(20),door),

                        VendingUtil.transmitJoint(VendingUtil.getDeliveryByte(46),door),

                        //  进入校准
                        SerialPortRequestByteManage.getInstance().weightCalibration_1(door),


                        //  校准 1
                        SerialPortRequestByteManage.getInstance().weightCalibration_2(door,0),
                        //  校准 2
                        SerialPortRequestByteManage.getInstance().weightCalibration_2(door,3),
                        //  校准 3
                        SerialPortRequestByteManage.getInstance().weightCalibration_2(door,18),
                        //  校准 4
                        SerialPortRequestByteManage.getInstance().weightCalibration_2(door,69),

                        VendingUtil.getDeliveryByte(1)
                };


                final String[] array2 = new String[]{
                        "开门",
                        "关门",
                        "获取数据",
                        "开消毒",
                        "关消毒",
                        "开灯",
                        "关灯",
                        "开排气扇",
                        "关排气扇",
                        "开电磁",
                        "关电磁",
                        "开加热",
                        "关加热",
                        "开搅拌",
                        "关搅拌",
                        "开投料",
                        "关投料",
                        "售卖机的第一个货道",
                        "售卖机的第二十个货道",
                        "售卖机的第四十六个货道",
                        "进入校准模式",
                        "0g 校准",
                        "3 g校准",
                        "18 g校准",
                        "69g 校准",
                        "第一台售卖机的第一个货道,不经协议"
                };


                AlertDialog.Builder alert = new AlertDialog.Builder(DebugActivity.this);
                alert.setItems(array2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SerialPortUtil.getInstance().sendData(array[which]);
                        Log.i(APP.TAG,array2[which] + ByteStringUtil.byteArrayToHexStr(array[which]));
                    }
                });
                alert.create();
                alert.show();



            }
        });

    }


    public UsbDevice getUsbCameraDevice(int pid) {
        debug_camera_textTueView.setText(null);
        UsbManager mUsbManager = (UsbManager) getSystemService(USB_SERVICE);

        HashMap<String, UsbDevice> deviceMap = mUsbManager.getDeviceList();
        if (deviceMap != null) {
            debug_camera_textTueView.append("摄像头数量:" + deviceMap.size() + "\n");
            for (UsbDevice usbDevice : deviceMap.values()) {
                debug_camera_textTueView.append("摄像头名称:" + usbDevice.getDeviceName() + "\n");
                debug_camera_textTueView.append("摄像头ProductId:" + usbDevice.getProductId() );
                debug_camera_textTueView.append("摄像头DeviceId:" + usbDevice.getDeviceId());
                debug_camera_textTueView.append("摄像头VendorId:" + usbDevice.getVendorId());
                debug_camera_textTueView.append("\n");
                if (usbDevice.getProductId() == pid) {
                    return usbDevice;
                }
            }
        }
        return null;
    }



    @Subscribe(threadMode = ThreadMode.POSTING)
    public void addDebugLog(final DebugLogBean de){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(debug_log_tv != null){
                    debug_log_tv.append(de.getString());
                    debug_log_tv.append("\n");
                }
            }
        });
    }


    /**
     * 查看垃圾箱状态弹窗
     * */
    private AlertDialog alertState ;
    public void debug_get_state(View view){
        if(alertState != null && !alertState.isShowing()){
            alertState.show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }
}