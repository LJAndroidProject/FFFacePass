package megvii.testfacepass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.Gson;
import com.lgh.uvccamera.UVCCameraProxy;
import com.lgh.uvccamera.bean.PicturePath;
import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PictureCallback;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import megvii.testfacepass.independent.ServerAddress;
import megvii.testfacepass.independent.bean.AdminLoginResult;
import megvii.testfacepass.independent.bean.DeliveryResult;
import megvii.testfacepass.independent.bean.DustbinENUM;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.bean.GeneralBean;
import megvii.testfacepass.independent.bean.PhoneCodeVerifyBean;
import megvii.testfacepass.independent.manage.SerialPortRequestByteManage;
import megvii.testfacepass.independent.manage.SerialPortRequestManage;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.DustbinUtil;
import megvii.testfacepass.independent.util.NetWorkUtil;
import megvii.testfacepass.independent.util.SerialPortUtil;
import megvii.testfacepass.independent.view.AdminLoginDialog;
import okhttp3.Call;

public class ControlActivity extends AppCompatActivity{
    private Intent intent;

    //  这个id 是服务器传过来的用户id，绑定接下来的所有操作
    public static long userId;

    private TextView control_welcome_textView;
    private ImageView control_image;

    //  摄像头
    private TextureView textTueView;
    private UVCCameraProxy mUVCCamera;
    private UsbDevice mUsbDevice;

    private int mSecretNumber = 0;
    private static final long CLICK_INTERVAL = 600;
    private long mLastClickTime;

    private AdminLoginDialog adminLoginDialog;

    private RecyclerView control_recyclerview;

    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);


        textTueView = (TextureView) findViewById(R.id.textTueView);
        control_image = (ImageView) findViewById(R.id.control_image);
        control_recyclerview = (RecyclerView)findViewById(R.id.control_recyclerview);
        textView = (TextView) findViewById(R.id.textView);

        mUVCCamera = new UVCCameraProxy(this);
        mUVCCamera.getConfig()
                .isDebug(true) // 是否调试
                .setPicturePath(PicturePath.APPCACHE) // 图片保存路径，保存在app缓存还是sd卡
                .setDirName("uvccamera") // 图片保存目录名称
                .setProductId(0) // 产品id，用于过滤设备，不需要可不设置 37424
                .setVendorId(0); // 供应商id，用于过滤设备，不需要可不设置 1443

        mUVCCamera.setPreviewTexture(textTueView); // TextureView

        //  默认摄像头
        mUsbDevice = getUsbCameraDevice(hexToInt(1));


        control_welcome_textView = (TextView) findViewById(R.id.control_welcome_textView);

        control_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                long curTime = System.currentTimeMillis();
                long durTime = curTime - mLastClickTime;
                mLastClickTime = curTime;
                if (durTime < CLICK_INTERVAL) {
                    ++mSecretNumber;
                    if (mSecretNumber == 5) {

                        adminLoginDialog = new AdminLoginDialog(ControlActivity.this);
                        adminLoginDialog.setLoginListener(new AdminLoginDialog.LoginListener() {
                            @Override
                            public void callBack(final String editStr, final String password, android.app.AlertDialog alertDialog) {


                                //  传参手机号码 和 密码 开始登陆
                                Map<String,String> map = new HashMap<>();
                                map.put("phone",editStr);
                                map.put("pwd",password);
                                NetWorkUtil.getInstance().doPost(ServerAddress.ADMIN_LOGIN, map, new NetWorkUtil.NetWorkListener() {
                                    @Override
                                    public void success(String response) {
                                        //  登陆结果
                                        AdminLoginResult adminLoginResult = new Gson().fromJson(response,AdminLoginResult.class);

                                        //  手机号码 + 密码 登陆成功
                                        if(adminLoginResult.getCode() == 1){
                                            Toast.makeText(ControlActivity.this, "登陆成功，正在发送验证码。", Toast.LENGTH_SHORT).show();

                                            //  登陆成功，发送验证码
                                            Map<String,String> m = new HashMap<>();
                                            m.put("phone",editStr);
                                            m.put("type","2");
                                            NetWorkUtil.getInstance().doPost(ServerAddress.SEND_SMS, m, new NetWorkUtil.NetWorkListener() {
                                                @Override
                                                public void success(String response) {

                                                    //  验证码发送回调
                                                    GeneralBean generalBean = new Gson().fromJson(response,GeneralBean.class);
                                                    if(generalBean.getCode() == 1){
                                                        //  验证码发送成功
                                                        Toast.makeText(ControlActivity.this, "已发送验证码到手机。", Toast.LENGTH_SHORT).show();
                                                        //  弹窗跳转至 验证码输入
                                                        adminLoginDialog.verifyState(new AdminLoginDialog.VerifyListener() {
                                                            @Override
                                                            public void verifyCallBack(String adminPhone, String verifyCode,final AlertDialog alertDialog) {
                                                                //  点击验证码 验证 ，传输手机号码、 密码 、以及输入的验证码进行验证

                                                                Map<String,String> ma = new HashMap<>();
                                                                ma.put("phone",adminPhone);
                                                                ma.put("pwd",password);
                                                                ma.put("code",verifyCode);
                                                                NetWorkUtil.getInstance().doPost(ServerAddress.PHONE_CODE_VERIFY, ma, new NetWorkUtil.NetWorkListener() {
                                                                    @Override
                                                                    public void success(String response) {
                                                                        PhoneCodeVerifyBean phoneCodeVerifyBean = new Gson().fromJson(response,PhoneCodeVerifyBean.class);
                                                                        if(phoneCodeVerifyBean.getCode() == 1){
                                                                            Log.i("结果",phoneCodeVerifyBean.toString());
                                                                            showAdminManage(alertDialog,phoneCodeVerifyBean);
                                                                        }else{
                                                                            Toast.makeText(ControlActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void fail(Call call, IOException e) {
                                                                        Toast.makeText(ControlActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }

                                                                    @Override
                                                                    public void error(Exception e) {
                                                                        Toast.makeText(ControlActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });


                                                            }
                                                        });
                                                    }else{
                                                        Toast.makeText(ControlActivity.this, "验证码发送失败。", Toast.LENGTH_SHORT).show();
                                                    }




                                                }

                                                @Override
                                                public void fail(Call call, IOException e) {
                                                    Toast.makeText(ControlActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void error(Exception e) {
                                                    Toast.makeText(ControlActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });



                                        }else{
                                            //  手机号码 + 验证码 登陆失败
                                            Toast.makeText(ControlActivity.this, "账户或密码错误，登陆失败", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void fail(Call call, IOException e) {
                                        Toast.makeText(ControlActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void error(Exception e) {
                                        Toast.makeText(ControlActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }
                        });
                        adminLoginDialog.create();
                        adminLoginDialog.show();


                    }
                } else {
                    mSecretNumber = 0;
                }
            }
        });


        intent = getIntent();
        userId = intent.getLongExtra("userId",1);

        if(userId == 0){
            Toast.makeText(ControlActivity.this,"特殊用户",Toast.LENGTH_LONG).show();
            //finish();
        }else{
            control_welcome_textView.setText("欢迎用户 " + userId + " 进入操作界面");
        }


        EventBus.getDefault().register(this);



        //  进入范围，打开其它和厨余垃圾
        List<DustbinStateBean> list = DataBaseUtil.getInstance(this).getDustbinByType(DustbinENUM.OTHER);
        list.addAll(DataBaseUtil.getInstance(this).getDustbinByType(DustbinENUM.KITCHEN));
        //openDoor(list);


        //  获取所有垃圾箱配置
        final List<DustbinStateBean> dustbinStateBeans = DataBaseUtil.getInstance(this).getDaoSession().getDustbinStateBeanDao().queryBuilder().list();


        if(DataBaseUtil.getInstance(this).getDaoSession().getDustbinConfigDao().queryBuilder().unique().getHasVendingMachine()){
            DustbinStateBean dustbinStateBean = new DustbinStateBean();
            dustbinStateBean.setDustbinBoxType("自动售卖机");
            dustbinStateBeans.add(dustbinStateBean);
        }

        final ControlItemAdapter controlItemAdapter = new ControlItemAdapter(R.layout.control_item_layout,removeDuplicateUser(dustbinStateBeans));
        controlItemAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(final BaseQuickAdapter adapter, View view, final int position) {
                if(view.getId() == R.id.control_item_iv){

                    final DustbinStateBean data = controlItemAdapter.getData().get(position);

                    if("自动售卖机".equals(data.getDustbinBoxType())){
                        startActivity(new Intent(ControlActivity.this,VendingMachineActivity.class));
                    }else{
                        mUVCCamera.closeCamera(); // 关闭相机


                        DustbinStateBean dustbinStateBean = openDoorByType(data.getDustbinBoxType());
                        if(dustbinStateBean == null){
                            return;
                        }
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openDoor(dustbinStateBean.getDoorNumber()));

                        /*if(DustbinENUM.BOTTLE.toString().equals(data.getDustbinBoxType())){

                        }else if(DustbinENUM.WASTE_PAPER.toString().equals(data.getDustbinBoxType())){

                        }else if(DustbinENUM.RECYCLABLES.toString().equals(data.getDustbinBoxType())){

                        }else if(DustbinENUM.KITCHEN.toString().equals(data.getDustbinBoxType())){

                        }else if(DustbinENUM.HARMFUL.toString().equals(data.getDustbinBoxType())){

                        }else if(DustbinENUM.OTHER.toString().equals(data.getDustbinBoxType())){

                        }*/

                        //  开启闪关灯
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openLight(data.getDoorNumber()));

                        mUsbDevice = getUsbCameraDevice(hexToInt(data.getDoorNumber()));
                        mUVCCamera.requestPermission(mUsbDevice);


                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ControlActivity.this, "拍照", Toast.LENGTH_SHORT).show();
                                mUVCCamera.takePicture();
                            }
                        },2000);


                        //  10 s 后 关闭闪关灯
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeLight(data.getDoorNumber()));
                            }
                        },10 * 1000);
                    }


                }
            }
        });
        control_recyclerview.setLayoutManager(new GridLayoutManager(this,3));
        control_recyclerview.setAdapter(controlItemAdapter);


        mUVCCamera.setPictureTakenCallback(new PictureCallback() {
            @Override
            public void onPictureTaken(String path) {
                textView.append("图片路径" + path);
                textView.append("\n");
            }
        });

        mUVCCamera.setConnectCallback(new ConnectCallback() {
            @Override
            public void onAttached(UsbDevice usbDevice) {


                mUVCCamera.requestPermission(mUsbDevice); // USB设备授权
            }

            @Override
            public void onGranted(UsbDevice usbDevice, boolean granted) {

                /*MainActivity3.textView.append("onGranted:"  +granted);
                MainActivity3.textView.append("\n");*/

                /*if(usbDevice.getDeviceName().endsWith("/021")){
                    mUVCCamera.connectDevice(usbDevice); // 连接USB设备
                }*/
                /*if (granted) {
                    mUVCCamera.connectDevice(usbDevice); // 连接USB设备
                }*/
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
    }


    private void camera(){

    }

    // 显示管理员
    String [] finalStrings = new String[]{"回收箱管理","故障维修管理","售卖机补货"};
    String [] strings = new String[]{"回收箱管理","故障维修管理","售卖机补货"};
    /**
     *
     * 1：回收员，2维修员，3：补货员，99：超级管理员
     * */
    private void showAdminManage(AlertDialog alertDialog,PhoneCodeVerifyBean phoneCodeVerifyBean){
        alertDialog.dismiss();


        //  如果不是超级管理员
        if(!phoneCodeVerifyBean.getData().getAdmin_types().contains("99")){
            String typeStr = phoneCodeVerifyBean.getData().getAdmin_types().replace("1", finalStrings[0]);
            typeStr = typeStr.replace("2", finalStrings[1]);
            typeStr = typeStr.replace("3", finalStrings[2]);

            strings = typeStr.split(",");
        }


        AlertDialog.Builder alert = new AlertDialog.Builder(ControlActivity.this);
        alert.setTitle("选择你要做的操作:");
        alert.setSingleChoiceItems(strings,-1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String item = strings[which];

                if(finalStrings[0].equals(item)){

                }else if(finalStrings[1].equals(item)){
                    Intent intent = new Intent(ControlActivity.this, DustbinManageActivity.class);
                    startActivity(intent);
                }else if( finalStrings[2].equals(item)){
                    Intent intent = new Intent(ControlActivity.this,ReplenishmentActivity.class);
                    startActivity(intent);
                }
            }
        });
        alert.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setCancelable(false);
        alert.create();
        alert.show();
    }


    /**
     * 筛选合适的垃圾箱
     * */
    private DustbinStateBean openDoorByType(String type){
        List<DustbinStateBean> list = ((APP)getApplication()).getDustbinBeanList();
        for(DustbinStateBean dustbinStateBean : list){
            if(dustbinStateBean.getDustbinBoxType() .equals(type)){
                
                if(dustbinStateBean.getIsFull()){
                    return dustbinStateBean;
                }else{
                    Toast.makeText(this, "垃圾箱已满或没有合适的垃圾箱", Toast.LENGTH_SHORT).show();
                    return null;
                }
            }
        }

        Toast.makeText(this, "垃圾箱已满", Toast.LENGTH_SHORT).show();
        return null;
    }


    /**
     * 根据桶位获取pid
     * */
    private int hexToInt(int numb){

        int target = numb * 1111;

        String string = new BigInteger(String.valueOf(target), 16).toString();


        Log.i("结果",string);

        return Integer.parseInt(string);
    }




    /**
     * 将遍历各个门，直到匹配,可用为止
     * @param dustbinTypeChildList 传入需要开启的门编号
     * @return 返回打开成功的门
     * */
    private DustbinStateBean openDoor(List<DustbinStateBean> dustbinTypeChildList){

        if(dustbinTypeChildList == null){
            return null;
        }

        /*for(DustbinBean dustbinBean : dustbinTypeChildList){
            byte[] responseByte = SerialPortUtil.getInstance().sendData(SerialPortRequestManage.getInstance().openDoor(dustbinBean.getDoorNumber()));
            if(responseByte != null && responseByte[7] == 0x10){
                *//*String responseString = ByteStringUtil.byteArrayToHexStr(responseByte);

                String door = OrderUtil.cutOrderByIndex(responseString,5);

                return Integer.parseInt(door);*//*

                //  打开门板成功后关闭消毒灯，关闭门板后再打开消毒灯
                SerialPortUtil.getInstance().sendData(SerialPortRequestManage.getInstance().openTheDisinfection(1));

                return dustbinBean;
            }
        }*/

        SerialPortUtil.getInstance().sendData(SerialPortRequestManage.getInstance().openDoor(2));


        return null;
    }


    /**
     * 上传用户投递的 id 和 重量差 ，以及箱体类型
     * 投递 --> 关门 --> 计算当前重量 --> 计算重量差 (同一箱体的当前重量 减去 上一次重量) --> 投递结果
     *
     * */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void addUserScore(DeliveryResult deliveryResult){

        //  传入门板号，获取垃圾箱类型
        DustbinStateBean dustbinBean = DataBaseUtil.getInstance(ControlActivity.this).getDustbinByNumber(deliveryResult.getDoorNumber());
        String dustbinBoxType = dustbinBean.getDustbinBoxType();

        Map<String,String> map = new HashMap<>();
        map.put("userId",String.valueOf(userId));   //  用户  ID
        map.put("weightDiff",String.valueOf(deliveryResult.getWeightDiff()));   //  垃圾箱重量差
        map.put("dustbinBoxType",dustbinBoxType);   //  垃圾箱类型

        NetWorkUtil.getInstance().doPost(ServerAddress.ADD_USER_SCORE, map, new NetWorkUtil.NetWorkListener() {
            @Override
            public void success(String response) {

            }

            @Override
            public void fail(Call call, IOException e) {

            }

            @Override
            public void error(Exception e) {

            }
        });


    }




    public UsbDevice getUsbCameraDevice(int pid) {
        UsbManager mUsbManager = (UsbManager) getSystemService(USB_SERVICE);

        HashMap<String, UsbDevice> deviceMap = mUsbManager.getDeviceList();
        if (deviceMap != null) {
            textView.append("摄像头数量:" + deviceMap.size() + "\n");
            for (UsbDevice usbDevice : deviceMap.values()) {
                textView.append("摄像头名称:" + usbDevice.getDeviceName() + "\n");
                textView.append("摄像头ProductId:" + usbDevice.getProductId() + "\n");
                textView.append("摄像头DeviceId:" + usbDevice.getDeviceId() + "\n");
                textView.append("摄像头VendorId:" + usbDevice.getVendorId() + "\n");
                textView.append("\n");
                if (usbDevice.getProductId() == pid) {
                    return usbDevice;
                }
            }
        }
        return null;
    }

    public boolean isUsbCamera(UsbDevice usbDevice) {
        return usbDevice != null && 239 == usbDevice.getDeviceClass() && 2 == usbDevice.getDeviceSubclass();
    }


    /**
     * 去重
     * */
    public static ArrayList<DustbinStateBean> removeDuplicateUser(List<DustbinStateBean> list) {
        Set<DustbinStateBean> set = new TreeSet<>(new Comparator<DustbinStateBean>() {
            @Override
            public int compare(DustbinStateBean o1, DustbinStateBean o2) {

                return String.valueOf(o1.getDustbinBoxType()).compareTo(String.valueOf(o2.getDustbinBoxType()));
            }
        });
        set.addAll(list);
        return new ArrayList<>(set);
    }



    public static class ControlItemAdapter extends BaseQuickAdapter<DustbinStateBean, BaseViewHolder> implements View.OnTouchListener{

        public ControlItemAdapter(int layoutResId, @Nullable List<DustbinStateBean> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, DustbinStateBean data) {

            //  点击效果
            helper.getView(R.id.control_item_iv).setOnTouchListener(this);

            //  图片资源
            int image_resource = 0;

            //  厨余
            if(DustbinENUM.BOTTLE.toString().equals(data.getDustbinBoxType())){
                image_resource = R.mipmap.chuyu;
            }else if(DustbinENUM.WASTE_PAPER.toString().equals(data.getDustbinBoxType())){
                image_resource = R.mipmap.pinzi;
            }else if(DustbinENUM.RECYCLABLES.toString().equals(data.getDustbinBoxType())){
                image_resource = R.mipmap.kehuishou;
            }else if(DustbinENUM.KITCHEN.toString().equals(data.getDustbinBoxType())){
                image_resource = R.mipmap.chuyu;
            }else if(DustbinENUM.HARMFUL.toString().equals(data.getDustbinBoxType())){
                image_resource = R.mipmap.youhai;
            }else if(DustbinENUM.OTHER.toString().equals(data.getDustbinBoxType())){
                image_resource = R.mipmap.qita;
            }else if("自动售卖机".equals(data.getDustbinBoxType())){
                image_resource = R.mipmap.shouhuo;
            }

            //  加载图片
            Glide.with(mContext).load(image_resource).into((ImageView) helper.getView(R.id.control_item_iv));
            helper.addOnClickListener(R.id.control_item_iv);

        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {

            //  点击效果，调整透明度
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.8f);
                    break;
                case MotionEvent.ACTION_UP:
                    v.setAlpha(1f);
                    break;
            }

            return false;
        }
    }


}

