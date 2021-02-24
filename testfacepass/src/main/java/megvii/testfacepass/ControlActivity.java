package megvii.testfacepass;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.Gson;

import com.serialportlibrary.util.ByteStringUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;


import megvii.testfacepass.independent.ServerAddress;
import megvii.testfacepass.independent.bean.AdminLoginResult;
import megvii.testfacepass.independent.bean.CommodityAlternativeBean;
import megvii.testfacepass.independent.bean.CommodityBean;
import megvii.testfacepass.independent.bean.CommodityBeanDao;
import megvii.testfacepass.independent.bean.DeliveryRecord;
import megvii.testfacepass.independent.bean.DeliveryRecordDao;
import megvii.testfacepass.independent.bean.DustbinENUM;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.bean.GeneralBean;
import megvii.testfacepass.independent.bean.GetServerGoods;
import megvii.testfacepass.independent.bean.PhoneCodeVerifyBean;
import megvii.testfacepass.independent.bean.UploadImageServiceBean;
import megvii.testfacepass.independent.manage.SerialPortRequestByteManage;
import megvii.testfacepass.independent.manage.SerialPortResponseManage;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.DustbinUtil;
import megvii.testfacepass.independent.util.NetWorkUtil;
import megvii.testfacepass.independent.util.SerialPortUtil;
import megvii.testfacepass.independent.util.VoiceUtil;
import megvii.testfacepass.independent.view.AdminLoginDialog;
import megvii.testfacepass.independent.view.CameraPreview;
import okhttp3.Call;

public class ControlActivity extends AppCompatActivity{
    private Intent intent;

    //  这个id 是服务器传过来的用户id，绑定接下来的所有操作
    public static long userId;

    private TextView control_welcome_textView;
    private ImageView control_image;

    //  摄像头
    private TextureView textTueView;
    private UsbDevice mUsbDevice;

    private int mSecretNumber = 0;
    private static final long CLICK_INTERVAL = 600;
    private long mLastClickTime;

    private AdminLoginDialog adminLoginDialog;

    private RecyclerView control_recyclerview;

    private TextView textView;

    public int bottleNumber = 0;

    public TextView control_exit_btn;

    //  结算模式
    public static EXIT_MODE exit_mode = EXIT_MODE.TIME_TASK;

    //  相机是否开启成功
    public boolean cameraOpened = false;
    //  摄像头默认显示的画面

    //  结算模式
    public enum EXIT_MODE{
        //  关门迭代
        CLOSE_ITERATION,
        //  定时延迟
        TIME_TASK
    }

    //  投递之前记录
    public List<DustbinStateBean> beforeDustbinStateBeans;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i("take picture","take picture 销毁");


        APP.controlActivityIsRun = false;


        //  开启消毒灯
        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openTheDisinfection(1));

        SerialPortResponseManage.getInstance().setCloseListener(null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        APP.controlActivityIsRun = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  关闭消毒灯
        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeTheDisinfection(1));

        if(isTaskRoot()){
            Log.i("isTaskRoot","true");
            finish();
        }else{
            Log.i("isTaskRoot","false");
        }

        setContentView(R.layout.activity_control);


        //  初始化自拍相机
        initCameraSurfaceView();

        handler = new Handler(Looper.getMainLooper());

        hasMan();




        //  结算模式为关门迭代
        if(exit_mode == EXIT_MODE.CLOSE_ITERATION){
            EventBus.getDefault().register(this);

            SerialPortResponseManage.getInstance().setCloseListener(new SerialPortResponseManage.CloseListener() {
                @Override
                public void closeCall(DustbinStateBean dustbinStateBean) {
                    closeDoorCall(dustbinStateBean);
                }
            });

            //setDustbinCallListener();
        }else{
            //  结算定时 定时
        }


        //  投递之前的数据
        beforeDustbinStateBeans = APP.dustbinBeanList;

        //  更新售货机商品列表
        getGoodsPos();

        control_exit_btn= (TextView) findViewById(R.id.control_exit_btn);
        textTueView = (TextureView) findViewById(R.id.textTueView);
        control_image = (ImageView) findViewById(R.id.control_image);
        control_recyclerview = (RecyclerView)findViewById(R.id.control_recyclerview);
        textView = (TextView) findViewById(R.id.textView);
        control_welcome_textView = (TextView) findViewById(R.id.control_welcome_textView);

        /*if(APP.controlImagePreview){
            control_image.setAlpha(0.6f);
        }else{
            control_image.setAlpha(1f);
        }*/



        //  默认摄像头
        /*
         * 默认应该开启厨余 和 其它垃圾箱
         * */
        //  餐厨默认没有
        mUsbDevice = getUsbCameraDevice(doorNumberToPid(openDefaultDoor()));

        //mUsbDevice = getUsbCameraDevice(ControlActivity.doorNumberToPid(1));
        //mUVCCamera.closeCamera();



        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mUVCCamera.closeCamera();
                mUVCCamera.requestPermission(mUsbDevice);
            }
        },3000);*/


        //  6 秒后才显示退出按钮
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                control_exit_btn.setVisibility(View.VISIBLE);

                //  如果 6 s后还没有显示画面就尝试再开启一下摄像头
                if(!cameraOpened){
                    Log.i("onCameraOpened","onCameraOpened 没有成功开启,再次请求摄像头");

                }

                if(mCamera != null){
                    //  来张自拍
                    takePicture();
                }
            }
        },6000);


        //  快速连续点击 5 次 ，出现管理员登录
        control_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                long curTime = System.currentTimeMillis();
                long durTime = curTime - mLastClickTime;
                mLastClickTime = curTime;
                if (durTime < CLICK_INTERVAL) {
                    ++mSecretNumber;
                    if (mSecretNumber == 5) {

                        //  如果管理员登陆过 还没有退出，则可以直接复用，登陆信息 省的每次都要验证码
                        if(phoneCodeVerifyBean != null){
                            showAdminManage(phoneCodeVerifyBean);
                            return;
                        }

                        //  开始
                        adminLoginDialog = new AdminLoginDialog(ControlActivity.this);
                        adminLoginDialog.setLoginListener(new AdminLoginDialog.LoginListener() {
                            @Override
                            public void callBack(final String editStr, final String password, android.app.AlertDialog alertDialog) {

                                //  更新有人
                                APP.hasManTime = System.currentTimeMillis();
                                hasManIsRun = true;


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
                                                                        phoneCodeVerifyBean = new Gson().fromJson(response,PhoneCodeVerifyBean.class);
                                                                        if(phoneCodeVerifyBean.getCode() == 1){
                                                                            Log.i("结果",phoneCodeVerifyBean.toString());

                                                                            alertDialog.dismiss();
                                                                            showAdminManage(phoneCodeVerifyBean);
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
            control_welcome_textView.setText("欢迎用户 " + APP.userId + " 进入操作界面");
        }


        //  获取所有垃圾箱配置
        final List<DustbinStateBean> dustbinStateBeans = DataBaseUtil.getInstance(this).getDaoSession().getDustbinStateBeanDao().queryBuilder().list();


        //  投递界面 添加自动售卖机
        if(DataBaseUtil.getInstance(this).getDaoSession().getDustbinConfigDao().queryBuilder().unique().getHasVendingMachine()){
            DustbinStateBean dustbinStateBean = new DustbinStateBean();
            dustbinStateBean.setDustbinBoxType("自动售卖机");
            dustbinStateBeans.add(dustbinStateBean);
        }

        //  适配器
        final ControlItemAdapter controlItemAdapter = new ControlItemAdapter(R.layout.control_item_layout,removeDuplicateUser(dustbinStateBeans));
        controlItemAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(final BaseQuickAdapter adapter, View view, final int position) {
                if(view.getId() == R.id.control_item_iv){

                    final DustbinStateBean data = controlItemAdapter.getData().get(position);

                    if("自动售卖机".equals(data.getDustbinBoxType())){
                        startActivity(new Intent(ControlActivity.this,VendingMachineActivity.class));
                    }else{

                        //  获取合适的垃圾箱类型
                        DustbinStateBean dustbinStateBean = openDoorByType(data.getDustbinBoxType());
                        if(dustbinStateBean == null){
                            Toast.makeText(ControlActivity.this, "没有合适的垃圾箱", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //  添加需要关闭的垃圾箱
                        addNeedCloseDustbin(dustbinStateBean);
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openDoor(dustbinStateBean.getDoorNumber()));

                        //  开启垃圾箱
                        //  SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openDoor(dustbinStateBean.getDoorNumber()));
                        //  关闭消毒紫外线灯
                        // SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeTheDisinfection(dustbinStateBean.getDoorNumber()));
                    }


                }
            }
        });

        //  长按关闭
        controlItemAdapter.setOnItemChildLongClickListener(new BaseQuickAdapter.OnItemChildLongClickListener() {
            @Override
            public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {
                if(view.getId() == R.id.control_item_iv){

                    final DustbinStateBean data = controlItemAdapter.getData().get(position);

                    if("自动售卖机".equals(data.getDustbinBoxType())){

                    }else{

                        Toast.makeText(ControlActivity.this,"关闭" + data.getDustbinBoxType() + "垃圾箱",Toast.LENGTH_LONG).show();

                        //  关闭所有该类型的垃圾箱
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for(DustbinStateBean target : APP.dustbinBeanList){
                                    //  0   号桶作废
                                    if(target.getDustbinBoxType().equals(data.getDustbinBoxType()) && target.getDoorNumber() != 0 ){
                                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDoor(target.getDoorNumber()));
                                        try {
                                            Thread.sleep(500);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }).start();

                    }


                }

                return true;
            }
        });
        control_recyclerview.setLayoutManager(new GridLayoutManager(this,3));
        control_recyclerview.setAdapter(controlItemAdapter);






    }



    //  初始化相机
    FrameLayout cameraFrame;
    private Camera mCamera;
    public void initCameraSurfaceView() {
        cameraFrame = (FrameLayout)findViewById(R.id.control_camera_preview);

        int numberOfCameras  = Camera.getNumberOfCameras();
        //  遍历摄像头信息
        for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraId == 0 && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                // 前置摄像头
                mCamera = Camera.open(cameraId);//  打开摄像头
            }
        }

        if(mCamera == null){
            Log.i("结果","相机为空");
        }

        CameraPreview mPreview = new CameraPreview(this, mCamera);
        cameraFrame.addView(mPreview);
    }


    public void takePicture(){
        //得到照相机的参数
        Camera.Parameters parameters = mCamera.getParameters();
        //图片的格式
        parameters.setPictureFormat(ImageFormat.JPEG);
        //预览的大小是多少
        parameters.setPreviewSize(800, 400);
        //设置对焦模式，自动对焦
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        //对焦成功后，自动拍照
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                mCamera.takePicture(null, null, mPictureCallback);
            }
        });
    }


    //  获取照片中的接口回调
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            FileOutputStream fos = null;
            String mFilePath = getFilesDir()  + File.separator + System.currentTimeMillis() +".png";
            //  文件
            File tempFile = new File(mFilePath);
            try {
                fos = new FileOutputStream(tempFile);
                fos.write(data);

                NetWorkUtil.getInstance().fileUpload(tempFile, new NetWorkUtil.FileUploadListener() {
                    @Override
                    public void success(String fileUrl) {
                        APP.UserPhoto = fileUrl;
                    }

                    @Override
                    public void error(Exception e) {

                    }
                });


            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                //  实现连续拍多张的效果
                mCamera.startPreview();
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    };


    //  延迟结算
    public final static String DEBUG_TAG_TASK = "定时延迟结算";
    public void exit_time_task(){

        Thread exitThread = new Thread(){
            @Override
            public void run() {
                super.run();

                Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG,"关门之前垃圾箱的状态：" + APP.dustbinBeanList.toString());
                for(final DustbinStateBean dustbinStateBean : needCloseDustbin /* 之前是关闭当前状态为开的门 APP.dustbinBeanList*/){
                    Log.i(DEBUG_TAG_TASK,"正在处理的门" + dustbinStateBean.getDoorNumber() + "," + dustbinStateBean.getDoorIsOpen());

                    //  如果是开门的
                    if(true /* 之前是关闭当前状态为开的门 dustbinStateBean.getDoorIsOpen()*/){
                        Log.i(DEBUG_TAG_TASK,dustbinStateBean.getDoorNumber() + "是开的");

                        //  时间
                        final long time = System.currentTimeMillis() / 1000;
                        //  文件名称
                        final String imageName = APP.getDeviceId() + "_" + dustbinStateBean.getDoorNumber() + "_" + APP.userId + "_" + time + "_" +  dustbinStateBean.getId() + ".jpg";

                        Log.i(DEBUG_TAG_TASK,dustbinStateBean.getDoorNumber() + "开始关门");

                        //  关门
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDoor(dustbinStateBean.getDoorNumber()));

                        try {
                            Thread.sleep(500);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        //  开补光灯
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openLight(dustbinStateBean.getDoorNumber()));

                        Log.i(DEBUG_TAG_TASK,dustbinStateBean.getDoorNumber() + "开始添加投递记录");

                        //  添加投递记录
                        addRecord(dustbinStateBean,time);

                        //  拍照
                        Intent intent = new Intent("MY_BROADCAST_RECEIVER");
                        intent.putExtra("type","broadcast_camera_type");
                        intent.putExtra("data","");

                        intent.putExtra("doorNumber",dustbinStateBean.getDoorNumber());
                        intent.putExtra("time",time);
                        intent.putExtra("bin_id", dustbinStateBean.getId());

                        Log.i("拍照调试","实际传输的bin_id:" + dustbinStateBean.getId());


                        sendBroadcast(intent);

                        try {
                            Thread.sleep(500);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }

                Log.i(DEBUG_TAG_TASK,"take picture 进入关闭");
                //  1为结算超时 会关闭所有门
                exitEnd(1);

            }
        };

        exitThread.start();
    }


    //  需要关闭的垃圾箱列表
    private List<DustbinStateBean> needCloseDustbin = new ArrayList<>();
    public void addNeedCloseDustbin(DustbinStateBean dustbinStateBean){
        //  如果为 0 则直接添加
        if(needCloseDustbin.size() == 0 ){
            needCloseDustbin.add(dustbinStateBean);
        }else{

            //  查找该垃圾箱是否已经被添加进去，如果有则直接返回
            for(DustbinStateBean dustbinStateBeanChild:needCloseDustbin){
                if(dustbinStateBeanChild.getDoorNumber() == dustbinStateBean.getDoorNumber()){
                    return;
                }
            }

            //  如果能执行到这里说明还没有被加入进去，则添加该垃圾箱
            needCloseDustbin.add(dustbinStateBean);

        }
    }



    /**
     * 默认开启的门 默认开启餐厨和其它
     * 同时 返回 默认开启的摄像头
     * */
    private int defaultCamera = 1;
    private final static String CAMERA_TAG = "摄像头调试";
    private int openDefaultDoor(){

        /*
        * 开其它
        * */
        for(DustbinStateBean dustbinStateBean : APP.dustbinBeanList){
            if(dustbinStateBean.getDustbinBoxType().equals(DustbinENUM.OTHER.toString())){

                //  为什么不为 0 呢 ，0 号桶位置作废
                if(!dustbinStateBean.getIsFull() && dustbinStateBean.getDoorNumber() != 0){
                    //  添加需要关闭的垃圾箱
                    addNeedCloseDustbin(dustbinStateBean);
                    byte[] result = SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openDoor(dustbinStateBean.getDoorNumber()));

                    if(result != null){
                        Log.i("开门返回1", ByteStringUtil.byteArrayToHexStr(result));
                    }

                    defaultCamera = dustbinStateBean.getDoorNumber();
                    break;
                }else{
                    Toast.makeText(this, dustbinStateBean.getDustbinBoxType() + "垃圾箱已满", Toast.LENGTH_SHORT).show();
                }
            }
        }

        /*
        *
        * 开餐厨
        * */
        for(final DustbinStateBean dustbinStateBean : APP.dustbinBeanList){
            if(dustbinStateBean.getDustbinBoxType().equals(DustbinENUM.KITCHEN.toString())){

                if(!dustbinStateBean.getIsFull()){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //  添加需要关闭的垃圾箱
                            addNeedCloseDustbin(dustbinStateBean);
                            byte[] result = SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openDoor(dustbinStateBean.getDoorNumber()));
                            if(result != null){
                                Log.i("开门返回2", ByteStringUtil.byteArrayToHexStr(result));
                            }

                            if(defaultCamera != 0){
                                defaultCamera = dustbinStateBean.getDoorNumber();
                            }
                        }
                    },500);

                    break;
                }else{
                    Toast.makeText(ControlActivity.this, dustbinStateBean.getDustbinBoxType() + "垃圾箱已满", Toast.LENGTH_SHORT).show();
                }

            }
        }

        Log.i(CAMERA_TAG,"默认摄像头" + defaultCamera);
        return defaultCamera;
    }





    // 显示管理员
    String [] finalStrings = new String[]{"回收箱管理","故障维修管理","售卖机补货"};
    String [] strings = new String[]{"回收箱管理","故障维修管理","售卖机补货"};
    /**
     *
     * 1：回收员，2维修员，3：补货员，99：超级管理员
     * */

    private PhoneCodeVerifyBean phoneCodeVerifyBean;
    private void showAdminManage(PhoneCodeVerifyBean phoneCodeVerifyBean){
        //  如果不是超级管理员，把编号转换成 字符身份
        if(!phoneCodeVerifyBean.getData().getAdmin_types().contains("99")){
            String typeStr = phoneCodeVerifyBean.getData().getAdmin_types().replace("1", finalStrings[0]);
            typeStr = typeStr.replace("2", finalStrings[1]);
            typeStr = typeStr.replace("3", finalStrings[2]);

            strings = typeStr.split(",");
        }


        AlertDialog.Builder alert = new AlertDialog.Builder(ControlActivity.this,R.style.SingleChoiceItemsDialogStyle);
        alert.setTitle("选择你要做的操作:");
        alert.setSingleChoiceItems(strings,-1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String item = strings[which];

                if(finalStrings[0].equals(item)){
                    Intent intent = new Intent(ControlActivity.this, RecyclerAdminActivity.class);
                    startActivity(intent);
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
        if(APP.dustbinBeanList != null && APP.dustbinBeanList.size() > 0 ){
            Log.i("筛选合适大垃圾箱",APP.dustbinBeanList.toString());
            for(DustbinStateBean dustbinStateBean : APP.dustbinBeanList){
                if(dustbinStateBean.getDustbinBoxType().equals(type)){

                    if(!dustbinStateBean.getIsFull()){
                        return dustbinStateBean;
                    }else{
                        Toast.makeText(this, "垃圾箱已满", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }
            }
        }else{
            Toast.makeText(this, "垃圾箱配置为空", Toast.LENGTH_SHORT).show();
        }

        return null;
    }


    /**
     * 根据桶位获取pid
     * */
    public static int doorNumberToPid(int numb){

        int target = numb * 1111;

        String string = new BigInteger(String.valueOf(target), 16).toString();


        Log.i("结果",string);

        return Integer.parseInt(string);
    }



    /**
     * 根据 pid 转 桶位
     * */
    public static int pidToDoorNumber(int pid){

        Integer x = pid;

        String hex = x.toHexString(x);

        int i = Integer.parseInt(hex);

        return i / 1111;
    }



    //  根据 pid 获取 UVC 摄像头设备
    public UsbDevice getUsbCameraDevice(int pid) {
        UsbManager mUsbManager = (UsbManager) getSystemService(USB_SERVICE);

        HashMap<String, UsbDevice> deviceMap = mUsbManager.getDeviceList();


        //  打印扫描到的摄像头
        if (deviceMap != null) {
            for (UsbDevice usbDevice : deviceMap.values()) {
                Integer integer = usbDevice.getProductId();
                Log.i(DEBUG_TAG_TASK,"扫描到的摄像头：" + usbDevice.getProductId() + "，进制转换：" + integer.toHexString(integer));
            }
        }


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


    /**
     * 垃圾箱类型 去重
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


    //  适配器
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
                image_resource = R.mipmap.pinzi;
            }else if(DustbinENUM.WASTE_PAPER.toString().equals(data.getDustbinBoxType())){
                image_resource = R.mipmap.zhipi;
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
            helper.addOnLongClickListener(R.id.control_item_iv);

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




    /**
     * 获取商品列表，不管有没有售卖机 都将备选列表下载下来
     * */
    private void getGoodsPos(){
        NetWorkUtil.getInstance().doGetThread(ServerAddress.GET_GOODS_POS, null, new NetWorkUtil.NetWorkListener() {
            @Override
            public void success(String response) {
                GetServerGoods getServerGoods = new Gson().fromJson(response,GetServerGoods.class);
                //  获取商品列表
                List<GetServerGoods.DataBean.ListBean> listBeans = getServerGoods.getData().getList();


                List<CommodityAlternativeBean> commodityAlternativeBeans = new ArrayList<>();
                for(GetServerGoods.DataBean.ListBean listBean : listBeans){
                    CommodityAlternativeBean commodityBean = new CommodityAlternativeBean();
                    commodityBean.setCommodityName(listBean.getGoods_name());
                    commodityBean.setCanUserIntegral(listBean.getScore_pay() == 1);
                    commodityBean.setCommodityID((long) listBean.getId());
                    commodityBean.setCommodityMoney(listBean.getGoods_price());
                    commodityBean.setExpirationDate(listBean.getGoods_wonderful_days());
                    commodityBean.setImageUrl(listBean.getGoods_image());
                    commodityBean.setIntegralNumber(listBean.getScore_pay());
                    commodityBean.setShelvesOf(listBean.getStatus() == 1);

                    commodityAlternativeBeans.add(commodityBean);
                }


                DataBaseUtil.getInstance(ControlActivity.this).getDaoSession().getCommodityAlternativeBeanDao().insertOrReplaceInTx(commodityAlternativeBeans);

                //  挨个更新
                updateCommodity(commodityAlternativeBeans);
            }

            @Override
            public void fail(Call call, IOException e) {

            }

            @Override
            public void error(Exception e) {

            }
        });
    }


    /**
     * 传入需要更新的商品备选，更新商品备选列表，并更新列表商品
     * @param commodityAlternativeBeans 修改当前商品的备选
     * */
    private void updateCommodity(List<CommodityAlternativeBean> commodityAlternativeBeans){

        //  修改备选商品数据库
        DataBaseUtil.getInstance(this).getDaoSession().getCommodityAlternativeBeanDao().saveInTx(commodityAlternativeBeans);

        //  遍历变化的商品
        for(CommodityAlternativeBean commodityAlternativeBean : commodityAlternativeBeans){
            //  查询所有这个id的商品
            List<CommodityBean> com = DataBaseUtil.getInstance(ControlActivity.this).getDaoSession().getCommodityBeanDao().queryBuilder().where(CommodityBeanDao.Properties.CommodityID.eq(commodityAlternativeBean.getCommodityID())).list();

            //  该id下的商品 挨个进行赋值
            for(CommodityBean c :com){
                c.setCommodityID(commodityAlternativeBean.getCommodityID());
                c.setCommodityAlternativeBean(commodityAlternativeBean);
            }

            //  修改应用到数据库
            DataBaseUtil.getInstance(this).getDaoSession().getCommodityBeanDao().saveInTx(com);
        }
    }



    /**
     * @deprecated
     * 退出进行结算
     * */
    public void exit(View view){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在退出与结算积分...");
        progressDialog.create();
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    //  查询所有开启的门板
                    List<DustbinStateBean> dustbinBeanList = APP.dustbinBeanList;
                    for(final DustbinStateBean dustbinStateBean:dustbinBeanList){

                        //  扫描门板开启的箱体
                        if(dustbinStateBean.getDoorIsOpen()){
                            //  关闭门
                            SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDoor(dustbinStateBean.getDoorNumber()));

                            //  线程休眠 3s,给时间拍照
                            Thread.sleep(4000);
                        }
                    }


                    //  最后一张
                    Thread.sleep(4000);

                    //  用户id设置为0
                    APP.userId = 0;
                    //  投递瓶子数量设置为 0
                    APP.userType = 0;
                    //
                    bottleNumber = 0;
                    //  用户头像
                    APP.UserPhoto = null;


                    //  管理员登陆
                    phoneCodeVerifyBean = null;



                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });

                    //startActivity(new Intent(ControlActivity.this,MainActivity.class));
                    finish();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

            }
        }).start();

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        APP.hasManTime = System.currentTimeMillis();

        hasManIsRun = true;

        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        APP.hasManTime = System.currentTimeMillis();

        hasManIsRun = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        hasManIsRun = false;
    }

    /**
     * 30 s内没有人自动退出
     * */
    TimerTask hasManTask;
    Timer hasManTimer = new Timer();
    private boolean hasManIsRun = true;
    private final static int AUTO_EXIT_TIME = 30;

    private void hasMan(){
        hasManTask = new TimerTask() {
            @Override
            public void run() {

                Log.i("定时",(System.currentTimeMillis() / 1000) + "," + (APP.hasManTime / 1000));

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        control_exit_btn.setText("退出 ( " + (AUTO_EXIT_TIME - ((System.currentTimeMillis() - APP.hasManTime) / 1000)) + "s )");
                    }
                });

                //  剩余 10s 提醒语音
                if(hasManIsRun && (AUTO_EXIT_TIME - (System.currentTimeMillis() - APP.hasManTime) / 1000) == 10){
                    VoiceUtil.getInstance().openAssetMusics(ControlActivity.this,"exit_alert_voice.aac");
                }

                //  30 s内没有人
                if(hasManIsRun && System.currentTimeMillis() - APP.hasManTime > (AUTO_EXIT_TIME * 1000)){

                    hasManTask.cancel();
                    hasManTimer.cancel();

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(ControlActivity.this, "无人，自动结算", Toast.LENGTH_SHORT).show();
                            //  开始结算
                            exitControl(null);
                        }
                    });
                }


            }
        };

        hasManTimer = new Timer();
        hasManTimer.schedule(hasManTask,1,1000);
    }




    /**
     * 退出投递
     * */
    //  退出投递弹窗
    private ProgressDialog exitProgressDialog;
    //  开始退出时间
    private long beginExitTime;
    private TimerTask timerTask;
    private Timer timer;
    private Handler handler;
    public void exitControl(View view){
        VoiceUtil.getInstance().openAssetMusics(ControlActivity.this,"exit_alert_voice.aac");

        exitProgressDialog = new ProgressDialog(this);
        exitProgressDialog.setCancelable(false);
        exitProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        exitProgressDialog.setTitle("提示");
        exitProgressDialog.setMessage("正在退出与结算积分...");
        exitProgressDialog.create();
        exitProgressDialog.show();


        beginExitTime = System.currentTimeMillis();

        timerTask = new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        long timeDiff = (System.currentTimeMillis()  - beginExitTime) / 1000;

                        if(exitProgressDialog != null){
                            exitProgressDialog.setTitle("结算中 ( " + timeDiff + "s )");

                            if(exit_mode == EXIT_MODE.CLOSE_ITERATION){
                                //  一般一个桶 6-7 s就可以关闭了，所有桶位的数量 * 8
                                if(timeDiff > APP.dustbinBeanList.size() * 9){
                                    timer.cancel();
                                    timerTask.cancel();
                                    Toast.makeText(ControlActivity.this, "结算超时", Toast.LENGTH_SHORT).show();
                                    exitEnd(1);
                                }
                            }
                        }
                    }
                });

            }
        };

        timer = new Timer();
        timer.schedule(timerTask,1,1000);

        //  迭代
        if(exit_mode == EXIT_MODE.CLOSE_ITERATION){
            //  寻找需要关闭的门,迭代关闭
            closeOpenedDoor();
        }else{
            //  定时退出
            exit_time_task();
        }
    }


    /**
     * 获取投递之前的某个垃圾桶参数
     * */
    private DustbinStateBean getBeforeDustbin(int doorNumber){

        for(DustbinStateBean dustbinStateBean : beforeDustbinStateBeans){
            if(dustbinStateBean.getDoorNumber() == doorNumber){
                return dustbinStateBean;
            }
        }

        return null;
    }


    /**
     * 关闭已经开启 且没有关闭失败记录过 的门
     * */
    public final static String DEBUG_TAG = "结算调试";
    public void closeOpenedDoor(){
        /*//  计算符合条件的门
        int hasMatchCondition = 0;

        Log.i(DEBUG_TAG,"开始寻找开启的门");
        List<DustbinStateBean> dustbinStateBeans = APP.dustbinBeanList;
        for(DustbinStateBean dustbinStateBean:dustbinStateBeans){
            Log.i(DEBUG_TAG,"当前垃圾箱状态：" + dustbinStateBean.getDoorNumber() + "门：" +dustbinStateBean.getDoorIsOpen());
        }*/


        DustbinStateBean dustbinStateBean = getOpenedDoor();


        if(dustbinStateBean == null){
            Log.i(DEBUG_TAG,"结算完毕");
            exitEnd(0);
        }else{

            Log.i(DEBUG_TAG,"寻找到合适的门" + dustbinStateBean.getDoorNumber() + "," + dustbinStateBean.getDoorIsOpen());

            //  首先设备不能为 null
            if(mUsbDevice != null){

                //  如果摄像头就是正在关闭的门就不用切换
                if(pidToDoorNumber(mUsbDevice.getProductId()) == dustbinStateBean.getDoorNumber()){
                    Log.i(DEBUG_TAG,"不用切换摄像头");
                }else{
                    Log.i(DEBUG_TAG,"切换摄像头为" + dustbinStateBean.getDoorNumber());
                }

            }

            Log.i(DEBUG_TAG,"发送指令,关" + dustbinStateBean.getDoorNumber() + "号门");

            //  关门
            SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDoor(dustbinStateBean.getDoorNumber()));
        }


    }

    /**
     * 退出清算,将一些数据置为空
     * @param exitCode 1为结算超时 ， 0 为结算正常
     * */
    private void exitEnd(int exitCode){

        //  反正要延迟大概 4s 左右
        int millis = 4000 / APP.dustbinBeanList.size();

        //  依次关闭补光灯
        for(DustbinStateBean dustbinStateBean : APP.dustbinBeanList){
            //  关补光灯
            SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeLight(dustbinStateBean.getDoorNumber()));
            try {
                Thread.sleep(millis);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        Log.i("take picture","take picture 休眠结束");
        //  中断监听与销毁线程
        if(dustbinCallListenerThread != null && !dustbinCallListenerThread.isInterrupted()){
            dustbinCallListener = false;
            dustbinCallListenerThread.interrupt();
        }

        //  用户id设置为0
        //  APP.userId = 0;
        //  投递瓶子数量设置为 0
        bottleNumber = 0;
        //  管理员登陆 保存的信息设置为 null
        phoneCodeVerifyBean = null;

        //  将关门失败清空
        for(DustbinStateBean dustbinStateBean : APP.dustbinBeanList){
            dustbinStateBean.setCloseFailNumber(0);
            APP.setDustbinState(ControlActivity.this,dustbinStateBean);
        }

        //  因为可能在子线程中被调用
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(exitProgressDialog != null){
                    exitProgressDialog.dismiss();
                }
            }
        });

        //startActivity(new Intent(ControlActivity.this,MainActivity.class));
        Intent intent = new Intent(ControlActivity.this,MainActivity.class);
        intent.putExtra("exitCode",exitCode);
        setResult(MainActivity.CONTROL_RESULT_CODE,intent);
        finish();
    }

    /**
     * 垃圾箱关闭回调，关闭成功或关闭失败都会回调
     * */
    private long lastExecuteTime;
    @Subscribe(threadMode = ThreadMode.POSTING,sticky = true,priority = 99)
    public void closeDoorCall(final DustbinStateBean dustbinStateBean){

        //  定时结算则不做处理
        if(exit_mode == EXIT_MODE.TIME_TASK){
            return;
        }

        if(System.currentTimeMillis() - lastExecuteTime < 1000){
            return;
        }
        lastExecuteTime = System.currentTimeMillis();

        Log.i(DEBUG_TAG,"进入closeDoorCall()");
        //  时间
        long time = System.currentTimeMillis() / 1000;
        //  文件名称
        final String imageName = APP.getDeviceId() + "_" + dustbinStateBean.getDoorNumber() + "_" + APP.userId + "_" + time + "_" +  dustbinStateBean.getId() + ".jpg";
        //  开启补光灯
        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openLight(dustbinStateBean.getDoorNumber()));
        //  开启杀菌消毒
        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openTheDisinfection(dustbinStateBean.getDoorNumber()));
        //  拍照


        Log.i(DEBUG_TAG,"收到关门回调，添加投递记录和拍照");

        Log.i(DEBUG_TAG,"关闭反馈，打印事件总线传过来的对象" + dustbinStateBean.toString());


        //  本地和服务器添加投递记录
        addRecord(dustbinStateBean,time);

        //  寻找并关闭已开启的门
        closeOpenedDoor();

    }



    /**
     * 添加投递记录
     * */
    private void addRecord(final DustbinStateBean dustbinStateBean,long time){
        //  添加一条用户投递记录
        DeliveryRecord deliveryRecord = new DeliveryRecord(null,dustbinStateBean.getDoorNumber(),APP.userId,time,dustbinStateBean.getDustbinWeight(),null);
        long id = DataBaseUtil.getInstance(this).getDaoSession().getDeliveryRecordDao().insert(deliveryRecord);

        Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG,"添加投递记录" + id);

        //  计算投递重量差 ，兑换积分
        //  如果人工门不是开启的, 才计算重量
        if(!dustbinStateBean.getArtificialDoor()){

            Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG,"没有开启人工门");

            DeliveryRecord nowDeliveryRecord = new DeliveryRecord();
            nowDeliveryRecord.setDeliveryTime(System.currentTimeMillis());
            nowDeliveryRecord.setDoorNumber(dustbinStateBean.getDoorNumber());
            nowDeliveryRecord.setUserId(APP.userId);
            nowDeliveryRecord.setWeight(dustbinStateBean.getDustbinWeight());

            //  增加投递记录，之后通知计算该用户与上一次投递后的结果差
            DataBaseUtil.getInstance(this).getDaoSession().getDeliveryRecordDao().insert(nowDeliveryRecord);


            //  查询同一个桶最近两次投递记录，算出重量差
            QueryBuilder<DeliveryRecord> queryBuilder =  DataBaseUtil.getInstance(this).getDaoSession().getDeliveryRecordDao().queryBuilder();
            queryBuilder.where(DeliveryRecordDao.Properties.DoorNumber.eq(dustbinStateBean.getDoorNumber()));
            queryBuilder.orderDesc(DeliveryRecordDao.Properties.Id);
            queryBuilder.limit(2);
            List<DeliveryRecord> result = queryBuilder.list();

            //  重量差
            double diff = 0;

            //  获取垃圾箱之前的状态
            DustbinStateBean beforeDustbin = getBeforeDustbin(dustbinStateBean.getDoorNumber());
            if(beforeDustbin != null ){
                diff = dustbinStateBean.getDustbinWeight() - beforeDustbin.getDustbinWeight();
            }



                /*user_id	是	int	用户ID
                device_id	是	string	设备ID
                bin_id	是	int	垃圾箱ID
                bin_type	是	string	垃圾箱分类 ABCDEF
                post_weight	否	float	投放重量
                former_weight	否	float	原来的重量
                now_weight	否	float	现在的重量
                plastic_bottle_num	否	int	瓶子的个数
                rubbish_image	否	string	垃圾图片
                timestamp	否	string	当前时间戳*/


            //  上传投递记录
            Map<String,String> map = new HashMap<>();
            map.put("user_id",String.valueOf(APP.userId));
            map.put("bin_id",String.valueOf(dustbinStateBean.getId()));
            map.put("bin_type",dustbinStateBean.getDustbinBoxNumber());
            map.put("post_weight",String.valueOf(diff));
            map.put("former_weight",String.valueOf(dustbinStateBean.getDustbinWeight() - diff));
            map.put("now_weight",String.valueOf(dustbinStateBean.getDustbinWeight()));
            map.put("plastic_bottle_num",String.valueOf(bottleNumber));
            map.put("err_code",dustbinStateBean.getCloseFailNumber() == 0 ? "0" : "1");  //  0是正常的，其它是不正常的 1、2、3 对应一个err_msg
            map.put("err_msg",dustbinStateBean.getCloseFailNumber() == 0 ? "无描述" : "关门失败，结算异常");//    异常描述
            map.put("time",String.valueOf(time));
            map.put("rubbish_image"," ");
            if(APP.UserPhoto != null){
                map.put("user_pictrue",APP.UserPhoto);
            }

            Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG,"即将添加投递记录" +  map.toString());
            NetWorkUtil.getInstance().doPost(ServerAddress.DUSTBIN_RECORD, map, new NetWorkUtil.NetWorkListener() {
                @Override
                public void success(String response) {

                    Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG,response);
                    //  如果是瓶子类型,则清空瓶子
                    if(dustbinStateBean.getDustbinBoxType().equals(DustbinENUM.BOTTLE.toString())){
                        bottleNumber = 0;
                    }
                }

                @Override
                public void fail(Call call, IOException e) {
                    Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG,e.getMessage());
                }

                @Override
                public void error(Exception e) {
                    Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG,e.getMessage());
                }
            });
        }else{
            Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG,"人工门开启不做投递记录");
        }
    }

    /**
     * 获取门
     * */
    private DustbinStateBean getDustbinByDoorNumber(List<DustbinStateBean> dustbinStateBeans , int doorNumber){
        for(DustbinStateBean dustbinStateBean:dustbinStateBeans){
            if(dustbinStateBean.getDoorNumber() == doorNumber){
                return dustbinStateBean;
            }
        }

        return null;
    }

    //  设置关门监听
    /**
     * 因为事件总线经常无效，所有现在用死循环取数据
     * */
    private volatile boolean dustbinCallListener = true;
    private Thread dustbinCallListenerThread;
    private void setDustbinCallListener(){
        dustbinCallListenerThread = new Thread(){
            @Override
            public void run() {
                super.run();

                while (dustbinCallListener){
                    DustbinStateBean dustbinStateBean = SerialPortResponseManage.getInstance().getDustbinStateBean();
                    Log.i(DEBUG_TAG,"关门监听" + (dustbinStateBean == null ? "，没有关门" : ",监听到关门"));
                    if(dustbinStateBean != null){
                        //  用完了就 ， 设置为 null
                        SerialPortResponseManage.getInstance().setDustbinStateBean(null);
                        closeDoorCall(dustbinStateBean);
                    }

                    try {
                        Thread.sleep(100);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
        };

        dustbinCallListenerThread.start();
    }

    /**
     * 获取还在开着的门
     * */
    private DustbinStateBean getOpenedDoor(){
        Log.i(DEBUG_TAG,"扫描之前打印所有的门状态" + APP.dustbinBeanList.toString());
        for(DustbinStateBean dustbinStateBean:APP.dustbinBeanList){
            if(dustbinStateBean.getDoorIsOpen() && dustbinStateBean.getCloseFailNumber() == 0 ){
                Log.i(DEBUG_TAG,"找到的合适门" + dustbinStateBean.getDoorNumber() + "," + dustbinStateBean.getDoorIsOpen() + "," + dustbinStateBean.getCloseFailNumber());
                return dustbinStateBean;
            }
        }

        return null;
    }



}

