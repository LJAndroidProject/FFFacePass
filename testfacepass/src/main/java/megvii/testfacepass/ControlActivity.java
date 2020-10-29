package megvii.testfacepass;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import com.serialportlibrary.util.ByteStringUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
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
import megvii.testfacepass.independent.bean.DeliveryRecord;
import megvii.testfacepass.independent.bean.DeliveryRecordDao;
import megvii.testfacepass.independent.bean.DeliveryResult;
import megvii.testfacepass.independent.bean.DustbinBeanDao;
import megvii.testfacepass.independent.bean.DustbinENUM;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.bean.GeneralBean;
import megvii.testfacepass.independent.bean.PhoneCodeVerifyBean;
import megvii.testfacepass.independent.manage.SerialPortRequestByteManage;
import megvii.testfacepass.independent.manage.SerialPortRequestManage;
import megvii.testfacepass.independent.manage.SerialPortResponseManage;
import megvii.testfacepass.independent.util.DataBaseUtil;
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

    private ProgressDialog bottleDialog;
    public int bottleNumber = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        bottleDialog = new ProgressDialog(this);
        bottleDialog.setMessage("没有检测到瓶子...");
        bottleDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        bottleDialog.create();




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
                        //  瓶子
                        if(DustbinENUM.BOTTLE.toString().equals(data.getDustbinBoxType())){
                            bottleDialog.show();
                        }

                        //mUVCCamera.closeCamera(); // 关闭相机
                        /*mUsbDevice = getUsbCameraDevice(hexToInt(data.getDoorNumber()));
                        mUVCCamera.requestPermission(mUsbDevice);*/


                        //  获取合适的垃圾箱类型
                        DustbinStateBean dustbinStateBean = openDoorByType(data.getDustbinBoxType());
                        if(dustbinStateBean == null){
                            return;
                        }

                        //  开启垃圾箱
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openDoor(dustbinStateBean.getDoorNumber()));
                    }


                }
            }
        });
        control_recyclerview.setLayoutManager(new GridLayoutManager(this,3));
        control_recyclerview.setAdapter(controlItemAdapter);


        mUVCCamera.setPictureTakenCallback(new PictureCallback() {
            @Override
            public void onPictureTaken(String path) {
                //  可能为空
                if(path == null){
                    return;
                }

                File file = new File(path);
                //  去除.jpg
                //  设备id + 门板编号 + 用户id + 时间戳 + 垃圾箱id . jpg
                String fileName = file.getName().replace(".jpg","");

                //  解析文件名称
                final String[] fileArray = fileName.split("_");



                //  首先根据时间戳查询 投递记录,
                DeliveryRecord deliveryRecord = DataBaseUtil.getInstance(ControlActivity.this).getDaoSession().getDeliveryRecordDao().queryBuilder().where(DeliveryRecordDao.Properties.DeliveryTime.eq(fileArray[3])).unique();
                //  修改投递记录 中的拍摄图片地址 （关门时创建的投递记录是没有图片的，所以这里拍摄回调需要添加拍摄地址）
                deliveryRecord.setTakePath(file.getPath());
                //  修改信息
                DataBaseUtil.getInstance(ControlActivity.this).getDaoSession().getDeliveryRecordDao().update(deliveryRecord);



                /*
                 * 这里可以做一个 判空 如果数据表中，有两个 拍摄记录为 null 的数据，说明有摄像头没有拍摄到图片，说明摄像头故障
                 * */
                /*if(DataBaseUtil.getInstance(ControlActivity.this).getDaoSession().getDeliveryRecordDao().queryBuilder().where(DeliveryRecordDao.Properties.TakePath.isNull()).list().size() > 2){
                    //  错误上报
                }*/


                /*
                 *
                 * 图片文件上传
                 * */

                NetWorkUtil.getInstance().fileUpload(file, new NetWorkUtil.FileUploadListener() {
                    @Override
                    public void success(String fileUrl) {

                        Map<String,String> map = new HashMap<>();
                        map.put("bin_id",fileArray[4]);
                        map.put("user_id",fileArray[2]);
                        map.put("rubbish_image",fileUrl);
                        map.put("time",fileArray[3]);



                        NetWorkUtil.getInstance().doPost(ServerAddress.RUBBISH_IMAGE_POST, map, new NetWorkUtil.NetWorkListener() {
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

                    @Override
                    public void error(Exception e) {

                    }
                });


            }
        });

        //mUVCCamera.

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

    /**
     * 串口回调 垃圾箱被关闭
     * */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void closeDoor(final DustbinStateBean dustbinStateBean){
        long time = System.currentTimeMillis() / 1000;

        //  设备id + 门板编号 + 用户id + 时间戳 + 垃圾箱id
        //  GD-GZ-HP-HP-1_1_329_1603419106.jpg
        final String imageName = APP.getDeviceId() + "_" + dustbinStateBean.getDoorNumber() + "_" + APP.userId + "_" + time + "_" +  dustbinStateBean.getId() + ".jpg";
        //String imageName = System.currentTimeMillis() + ".jpg";

        //  开启闪关灯
        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openLight(dustbinStateBean.getDoorNumber()));



        mUVCCamera.closeCamera(); // 关闭相机
        mUVCCamera.closeDevice();


        //  切换摄像头
        mUsbDevice = getUsbCameraDevice(hexToInt(dustbinStateBean.getDoorNumber()));
        mUVCCamera.requestPermission(mUsbDevice);

        //  拍照
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ControlActivity.this, "拍照2", Toast.LENGTH_SHORT).show();
                mUVCCamera.takePicture(imageName);
            }
        },2 * 1000);

        //  10 s 后 关闭闪关灯
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeLight(dustbinStateBean.getDoorNumber()));
            }
        },10 * 1000);


        //  添加一条用户投递记录
        DeliveryRecord deliveryRecord = new DeliveryRecord(null,dustbinStateBean.getDoorNumber(),APP.userId,time,dustbinStateBean.getDustbinWeight(),null);
        DataBaseUtil.getInstance(this).getDaoSession().getDeliveryRecordDao().insert(deliveryRecord);








        //  计算投递重量差 ，兑换积分
        //  如果人工门不是开启的，并且不是瓶子 才计算重量
        if(!dustbinStateBean.getArtificialDoor()){

            //  如果为 0 说明不是通过扫码进入
            if(APP.userId > 0){

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

                //  没有投递记录，那肯定是不正常的
                if(result != null && result.size() > 0 ){
                    //  说明之前没有投递记录，第一条记录即是本次投递记录
                    if(result.size() == 1){

                        diff = result.get(0).getWeight();
                    }else if(result.size() == 2){

                        diff = result.get(0).getWeight() - result.get(1).getWeight();

                        //  不知道先后顺序，直接算出重量差即可,不能这样算，如果垃圾箱被清空！！！！！！！！！！！！！！
                        if(diff < 0 ){
                            diff = Math.abs(diff);
                        }
                    }
                }else{

                    return;
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
                map.put("time",String.valueOf(time));
                map.put("rubbish_image","");

                NetWorkUtil.getInstance().doPost(ServerAddress.DUSTBIN_RECORD, map, new NetWorkUtil.NetWorkListener() {
                    @Override
                    public void success(String response) {



                        //  如果是瓶子类型,则清空瓶子
                        if(dustbinStateBean.getDustbinBoxType().equals(DustbinENUM.BOTTLE.toString())){
                            bottleNumber = 0;
                        }
                    }

                    @Override
                    public void fail(Call call, IOException e) {
                        Log.i("投递记录结果，仅记录",e.getMessage());
                    }

                    @Override
                    public void error(Exception e) {
                        Log.i("投递记录结果，仅记录",e.getMessage());
                    }
                });

            }
        }
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
     * 退出进行结算
     * */
    public void exit(View view){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在退出与结算垃圾...");
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
                        if(!dustbinStateBean.getDoorIsOpen()){
                            //  关闭门
                            SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDoor(dustbinStateBean.getDoorNumber()));


                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    byte[] bytes = new byte[]{(byte) 0xF3 ,0x3F ,0x00 ,0x01 ,0x01 ,(byte) (dustbinStateBean.getDoorNumber() & 0xff) ,0x01 ,0x00 ,0x00 ,(byte) 0xF4 ,0x4F};

                                    SerialPortResponseManage.inOrderString(ControlActivity.this,bytes);

                                    Log.i("串口接收2", ByteStringUtil.byteArrayToHexStr(bytes));

                                        }
                            },6000);

                            //  线程休眠 3s,给时间拍照
                            Thread.sleep(6000);
                        }
                    }


                    Thread.sleep(1000);

                    //  用户id设置为0
                    APP.userId = 0;
                    //  投递瓶子数量设置为 0
                    bottleNumber = 0;


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

}

