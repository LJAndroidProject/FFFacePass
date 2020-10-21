package megvii.testfacepass;

import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
import megvii.testfacepass.independent.bean.DeliveryResult;
import megvii.testfacepass.independent.bean.DustbinENUM;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.manage.SerialPortRequestManage;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.NetWorkUtil;
import megvii.testfacepass.independent.util.OrderUtil;
import megvii.testfacepass.independent.util.SerialPortUtil;
import megvii.testfacepass.independent.util.VendingUtil;
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
                            public void callBack(String editStr, String password, android.app.AlertDialog alertDialog) {

                                if(editStr.equals("123")){
                                    adminLoginDialog.verifyState(new AdminLoginDialog.VerifyListener() {
                                        @Override
                                        public void verifyCallBack(String adminPhone, String verifyCode, android.app.AlertDialog alertDialog) {

                                            alertDialog.dismiss();


                                            List<String> list = new ArrayList<>();
                                            list.add("回收箱管理");
                                            list.add("故障维修管理管理");
                                            list.add("售卖机补货");


                                            final String[] strings = new String[list.size()];
                                            for(int i = 0 ; i < list.size() ; i++){
                                                strings[i] = list.get(i);
                                            }

                                            AlertDialog.Builder alert = new AlertDialog.Builder(ControlActivity.this);
                                            alert.setTitle("选择你要做的操作:");
                                            alert.setSingleChoiceItems(strings,-1, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    String item = strings[which];

                                                    if("回收箱管理".equals(item)){

                                                    }else if("故障维修管理管理".equals(item)){
                                                        Intent intent = new Intent(ControlActivity.this, DustbinManageActivity.class);
                                                        startActivity(intent);
                                                    }else if( "售卖机补货".equals(item)){
                                                        Intent intent = new Intent(ControlActivity.this,ReplenishmentActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                            alert.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            alert.setCancelable(false);
                                            alert.create();
                                            alert.show();

                                        }
                                    });
                                }else{
                                    Toast.makeText(ControlActivity.this,"登陆失败，密码错误",Toast.LENGTH_LONG).show();
                                }
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

                        if(DustbinENUM.BOTTLE.toString().equals(data.getDustbinBoxType())){

                        }else if(DustbinENUM.WASTE_PAPER.toString().equals(data.getDustbinBoxType())){

                        }else if(DustbinENUM.RECYCLABLES.toString().equals(data.getDustbinBoxType())){

                        }else if(DustbinENUM.KITCHEN.toString().equals(data.getDustbinBoxType())){

                        }else if(DustbinENUM.HARMFUL.toString().equals(data.getDustbinBoxType())){

                        }else if(DustbinENUM.OTHER.toString().equals(data.getDustbinBoxType())){

                        }

                        //  开启闪关灯
                        SerialPortUtil.getInstance().sendData(SerialPortRequestManage.getInstance().openLight(data.getDoorNumber()));

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
                                SerialPortUtil.getInstance().sendData(SerialPortRequestManage.getInstance().closeLight(data.getDoorNumber()));
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


    /**
     * 筛选合适的垃圾箱
     * */
    private void e(){
        List<DustbinStateBean> list = ((APP)getApplication()).getDustbinBeanList();

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

