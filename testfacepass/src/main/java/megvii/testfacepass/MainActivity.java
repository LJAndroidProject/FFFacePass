package megvii.testfacepass;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TypefaceSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.littlegreens.netty.client.listener.NettyClientListener;
import com.serialportlibrary.service.impl.SerialPortService;
import com.serialportlibrary.util.ByteStringUtil;
import com.umeng.message.PushAgent;
import com.umeng.message.entity.UMessage;


import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.CharsetUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import mcv.facepass.FacePassException;
import mcv.facepass.FacePassHandler;
import mcv.facepass.types.FacePassAddFaceResult;
import mcv.facepass.types.FacePassConfig;
import mcv.facepass.types.FacePassDetectionFeedback;
import mcv.facepass.types.FacePassDetectionResult;
import mcv.facepass.types.FacePassExtractFeatureResult;
import mcv.facepass.types.FacePassFace;
import mcv.facepass.types.FacePassFeatureAppendInfo;
import mcv.facepass.types.FacePassGroupSyncDetail;
import mcv.facepass.types.FacePassImage;
import mcv.facepass.types.FacePassImageRotation;
import mcv.facepass.types.FacePassImageType;
import mcv.facepass.types.FacePassModel;
import mcv.facepass.types.FacePassPose;
import mcv.facepass.types.FacePassRecognitionResult;
import mcv.facepass.types.FacePassAgeGenderResult;
import mcv.facepass.types.FacePassRecognitionResultType;
import mcv.facepass.types.FacePassSyncResult;
import megvii.testfacepass.adapter.FaceTokenAdapter;
import megvii.testfacepass.adapter.GroupNameAdapter;
import megvii.testfacepass.camera.CameraManager;
import megvii.testfacepass.camera.CameraPreview;
import megvii.testfacepass.camera.CameraPreviewData;
import megvii.testfacepass.independent.bean.BuySuccessMsg;
import megvii.testfacepass.independent.bean.DustbinConfig;
import megvii.testfacepass.independent.bean.ImageUploadResult;
import megvii.testfacepass.independent.bean.ResultMould;
import megvii.testfacepass.independent.bean.TCPVerify;
import megvii.testfacepass.independent.bean.TCPVerifyResponse;
import megvii.testfacepass.independent.bean.VXLoginCall;
import megvii.testfacepass.independent.bean.VXLoginResult;
import megvii.testfacepass.independent.manage.SerialPortResponseManage;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.NetWorkUtil;
import megvii.testfacepass.independent.util.VoiceUtil;
import megvii.testfacepass.independent.view.AdminLoginDialog;
import megvii.testfacepass.independent.util.QRCodeUtil;
import megvii.testfacepass.independent.util.SerialPortUtil;
import megvii.testfacepass.independent.ServerAddress;
import megvii.testfacepass.independent.util.TCPConnectUtil;
import megvii.testfacepass.independent.bean.DaoMaster;
import megvii.testfacepass.independent.bean.DaoSession;
import megvii.testfacepass.independent.bean.UserMessage;
import megvii.testfacepass.independent.bean.UserMessageDao;
import megvii.testfacepass.network.ByteRequest;
import megvii.testfacepass.utils.DownloadUtil;
import megvii.testfacepass.utils.FileUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;


public class MainActivity extends Activity implements CameraManager.CameraListener, View.OnClickListener {

    //  人脸识别模式
    private enum FacePassSDKMode {
        MODE_ONLINE,
        MODE_OFFLINE
    };

    //  默认是无线人脸识别
    private static FacePassSDKMode SDK_MODE = FacePassSDKMode.MODE_OFFLINE;


    private static final String DEBUG_TAG = "FacePassDemo";

    public static final String MY_TAG = "人脸识别调试";

    public static final String MY_ORDER = "指令调试";

    public static final String PUSH = "推送调试";

    /* 识别服务器IP */

    private static final String serverIP_offline = "10.104.44.50";//offline

    private static final String serverIP_online = "10.199.1.14";

    private static String serverIP;

    private static final String authIP = "https://api-cn.faceplusplus.com";
    private static final String apiKey = "N1CuZs4I1YuuCN5lO1ZmWyGiYG4ysH2k";
    private static final String apiSecret = "F0YG7KNSixDCOJnxmyPg3dToTkrTmiw-";

    private static String recognize_url;

    /* 人脸识别Group */
    private static final String group_name = "facepass";

    /* 程序所需权限 ：相机 文件存储 网络访问 */
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String PERMISSION_READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_INTERNET = Manifest.permission.INTERNET;
    private static final String PERMISSION_ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;
    private String[] Permission = new String[]{PERMISSION_CAMERA, PERMISSION_WRITE_STORAGE, PERMISSION_READ_STORAGE, PERMISSION_INTERNET, PERMISSION_ACCESS_NETWORK_STATE};


    /* SDK 实例对象 */
    FacePassHandler mFacePassHandler;

    /* 相机实例 */
    private CameraManager manager;

    /* 显示人脸位置角度信息 */
    private TextView faceBeginTextView;


    /* 显示faceId */
    private TextView faceEndTextView;

    /* 相机预览界面 */
    private CameraPreview cameraView;

    private boolean isLocalGroupExist = false;

    /* 在预览界面圈出人脸 */
    private FaceView faceView;

    private ScrollView scrollView;

    /* 相机是否使用前置摄像头 */
    private static boolean cameraFacingFront = true;
    /* 相机图片旋转角度，请根据实际情况来设置
     * 对于标准设备，可以如下计算旋转角度rotation
     * int windowRotation = ((WindowManager)(getApplicationContext().getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getRotation() * 90;
     * Camera.CameraInfo info = new Camera.CameraInfo();
     * Camera.getCameraInfo(cameraFacingFront ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK, info);
     * int cameraOrientation = info.orientation;
     * int rotation;
     * if (cameraFacingFront) {
     *     rotation = (720 - cameraOrientation - windowRotation) % 360;
     * } else {
     *     rotation = (windowRotation - cameraOrientation + 360) % 360;
     * }
     */

    private int cameraRotation;

    private static final int cameraWidth = 1280;
    private static final int cameraHeight = 720;

    private int mSecretNumber = 0;
    private static final long CLICK_INTERVAL = 600;
    private long mLastClickTime;

    private int heightPixels;
    private int widthPixels;

    int screenState = 0;    // 0 横 1 竖

    /* 网络请求队列 */
    RequestQueue requestQueue;

    Button visible;
    LinearLayout ll;
    FrameLayout frameLayout;
    private int buttonFlag = 0;
    private Button settingButton;
    private boolean ageGenderEnabledGlobal;

    /*Toast*/
    private  Toast mRecoToast;

    /*DetectResult queue*/
    ArrayBlockingQueue<byte[]> mDetectResultQueue;
    ArrayBlockingQueue<CameraPreviewData> mFeedFrameQueue;

    /*recognize thread*/
    RecognizeThread mRecognizeThread;
    FeedFrameThread mFeedFrameThread;


    /*底库同步*/
    private ImageView mSyncGroupBtn;
    private AlertDialog mSyncGroupDialog;

    private ImageView mFaceOperationBtn;
    /*图片缓存*/
    private FaceImageCache mImageCache;

    private Handler mAndroidHandler;

    private CameraPreviewData mCurrentImage;


    private Button mSDKModeBtn;
    int mId = 0;

    private String loginToken;

    private UserMessageDao userMessageDao;

    private AdminLoginDialog adminLoginDialog;

    //  调试模式
    private boolean debug = false;

    //  是否隐藏状态栏
    private boolean hide_status_bar = false;

    private APP app ;


    private Handler mainHandler;


    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageCache = new FaceImageCache();
        mDetectResultQueue = new ArrayBlockingQueue<byte[]>(5);
        mFeedFrameQueue = new ArrayBlockingQueue<CameraPreviewData>(1);
        initAndroidHandler();

        app = (APP) getApplication();
        mainHandler = new Handler(Looper.getMainLooper());


        //                                   |
        //String order = "F3 3F 00 01 02 01 01 11 00 F4 4F".replace(" ","");
        String order = "F3 3F 00 01 02 01 04 32 1A 1B 1C 00 F4 4F".replace(" ","");
        SerialPortResponseManage.inOrderString(this, ByteStringUtil.hexStrToByteArray(order));

        //  设置垃圾箱配置
        DustbinConfig dustbinConfig = DataBaseUtil.getInstance(this).getDaoSession().getDustbinConfigDao().queryBuilder().unique();
        app.setDustbinConfig(dustbinConfig);
        //  代表 全局 垃圾桶 list 对象
        app.setDustbinBeanList(DataBaseUtil.getInstance(MainActivity.this).getDustbinByType(null));

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("加载中...");
        progressDialog.create();

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                long nowTime = System.currentTimeMillis() / 1000 ;

                String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                APP app = (APP) getApplication();
                Map<String,String> map = new HashMap<>();
                map.put("device_id", androidID);
                map.put("device_token",app.getDeviceToken());
                map.put("sign",md5(androidID + nowTime + key).toUpperCase());
                map.put("timestamp",String.valueOf(nowTime));

                Log.i(MY_TAG,map.toString());
                NetWorkUtil.getInstance().doPost(ServerAddress.DEVICE_REGISTER, map, new NetWorkUtil.NetWorkListener() {
                    @Override
                    public void success(String response) {
                        Log.i(MY_TAG, "success: " + response);
                    }

                    @Override
                    public void fail(Call call, IOException e) {
                        Log.i(MY_TAG, "fail: " + e.getMessage());
                    }

                    @Override
                    public void error(Exception e) {
                        Log.i(MY_TAG, "error: " + e.getMessage());
                    }
                });


            }
        },2000);*/



        //  开启友盟推送
        PushAgent.getInstance(this).onAppStart();

        EventBus.getDefault().register(this);

        //  隐藏状态栏，也就是 app 打开后不能退出
        Intent intent = new Intent("android.q_zheng.action.statusbar");
        intent.putExtra("forbidden",hide_status_bar);
        intent.putExtra("status_bar",hide_status_bar);
        intent.putExtra("navigation_bar",hide_status_bar);
        sendBroadcast(intent);

        //  监听 app 是否在前台
        Intent intent2 = new Intent("android.q_zheng.action.APPMONITOR");
        intent2.putExtra("package_name","megvii.testfacepass"); //设置所监控应用的包名为 com.xxx.yyy
        intent2.putExtra("self_starting", true); //设置开机自启动
        intent2.putExtra("period", 0); //设置监控应有的周期，秒为单位，最小值为 15 秒，如果不设置
        //或者为 0，表示不需要系统对应用是否在前台进行监控
        sendBroadcast(intent2);


        //  必须在第一次语音播报前 先初始化对象，否则可能出现第一次语音播报无声音的情况
        VoiceUtil.getInstance(MainActivity.this);

        //  初始化 greenDao 数据库，以及数据库操作对象
        userMessageDao = DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getUserMessageDao();

        //  注册串口监听,与硬件进行通信
        SerialPortUtil.getInstance().receiveListener(new SerialPortService.SerialResponseListener() {
            @Override
            public void response(String response) {
                //  通过事件总线发送出去
                Log.i(MY_TAG,"串口接收" + response);

                SerialPortResponseManage.inOrderString(MainActivity.this,response);
            }
        });


        //  初始化 TCP 连接，与服务器进行通信
        initTCP();

        if (SDK_MODE == FacePassSDKMode.MODE_ONLINE) {
            //  http://10.199.1.14:8080/api/service/recognize/v1
            recognize_url = "http://" + serverIP_online + ":8080/api/service/recognize/v1";
            serverIP = serverIP_online;
        } else {
            serverIP = serverIP_offline;
        }

        /* 初始化界面 */
        initView();
        /* 申请程序所需权限 */
        if (!hasPermission()) {
            requestPermission();
        } else {
            initFacePassSDK();
        }

        initFaceHandler();
        /* 初始化网络请求库 */
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        mRecognizeThread = new RecognizeThread();
        mRecognizeThread.start();
        mFeedFrameThread = new FeedFrameThread();
        mFeedFrameThread.start();

        //  如果是调试模式，则显示调试面板
        if(debug){
            mFaceOperationBtn.setVisibility(View.VISIBLE);
            mSyncGroupBtn.setVisibility(View.VISIBLE);
            visible.setVisibility(View.VISIBLE);
            mSDKModeBtn.setVisibility(View.VISIBLE);
        }else{
            mFaceOperationBtn.setVisibility(View.GONE);
            mSyncGroupBtn.setVisibility(View.GONE);
            visible.setVisibility(View.GONE);
            mSDKModeBtn.setVisibility(View.GONE);
        }

        startActivity(new Intent(MainActivity.this,VendingMachineActivity.class));
    }


    private String key = "e0e9061d403f1898a501b8d7a840b949";
    @NonNull
    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void initAndroidHandler() {

        mAndroidHandler = new Handler();

    }

    private void initFacePassSDK() {
        //  获取认证方式
        FacePassHandler.getAuth(authIP, apiKey, apiSecret);

        FacePassHandler.initSDK(getApplicationContext());

        Log.d("FacePassDemo", FacePassHandler.getVersion());
    }


    //  从服务器传过来的内容
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void ServerToAndroid(UMessage msg){
        Log.i(PUSH,"服务器传过来的结果" + msg.custom);


        Log.i(NOW_TAG,"接收到友盟推送的内容" + msg.custom);

        Gson gson = new Gson();

        try{

            JSONObject jsonObject = new JSONObject(msg.custom);
            String action = jsonObject.getString("action");
            String list = jsonObject.getString("list");

            if("QrReturn".equals(action)){
                VXLoginCall vxLoginCall = gson.fromJson(list,VXLoginCall.class);
                Log.i(NOW_TAG,"list 内容 : " + vxLoginCall.toString());
                Log.i(NOW_TAG,"设置用户 id 之前的用户 id " + app.getUserId());
                //  修改当前设置的用户id
                app.setUserId(vxLoginCall.getInfo().getUser_id());
                Log.i(NOW_TAG,"设置用户 id 之后 的用户 id" + app.getUserId());

                //  隐藏二维码扫码
                if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                    Log.i(NOW_TAG,"隐藏二维码弹窗");
                    qrCodeDialog.dismiss();
                }

                //  云端有该人的人脸特征，则将特征保存到本地
                if(vxLoginCall.isFeatrue_state() && vxLoginCall.isFace_image_state()){
                    Log.i(NOW_TAG,"当前用户 含有人脸特征值 和 人脸图片");
                    //  获取来自服务器人脸特征 ( 字符串之前是 Base64 形式 )
                    byte[] feature = Base64.decode(vxLoginCall.getInfo().getFeatrue(),Base64.DEFAULT);

                    FacePassFeatureAppendInfo facePassFeatureAppendInfo = new FacePassFeatureAppendInfo();
                    //  插入人脸特征值，返回faceToken ，如果特征值不可用会抛出异常
                    String faceToken = mFacePassHandler.insertFeature(feature,facePassFeatureAppendInfo);
                    //  facetoken 绑定底库
                    boolean bindResult = mFacePassHandler.bindGroup(group_name, faceToken.getBytes());
                    //  绑定成功就可以 将 facetoken 和 id 进行绑定了

                    if(bindResult){
                        Log.i(NOW_TAG,"绑定成功，将跳转控制台");
                        //  facetoken 和用户id 绑定
                        DataBaseUtil.getInstance(this).insertUserIdAndFaceToken(app.getUserId(),faceToken);

                        //  跳转到垃圾箱控制台
                        Intent intent = new Intent(MainActivity.this,ControlActivity.class);
                        intent.putExtra("userId",app.getUserId());
                        startActivity(intent);


                        //  将用户id和特征、图片上传至服务器   ===========================================


                    }else{
                        Log.i(NOW_TAG,"绑定失败，删除人脸");
                        //  如果没有则删除之前的绑定
                        mFacePassHandler.deleteFace(faceToken.getBytes());
                    }
                }else{
                    //   云端 没有该用户的 人脸 特征值，则提示需要人脸注册
                    Log.i(NOW_TAG,"云端没有该人脸图片和特征值，显示人脸注册");


                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showVerifyFail();
                        }
                    });

                }




            }

        }catch (Exception e){
            e.printStackTrace();
        }

        /*Gson gson = new Gson();
        JsonMould jsonMould = gson.fromJson(msg.custom,JsonMould.class);

        Log.i(PUSH,"接收：" + jsonMould.toString());
        if("QrReturn".equals(jsonMould.getAction())){
            VXLoginCall vxLoginCall = gson.fromJson(jsonMould.getAction(),VXLoginCall.class);

            if(vxLoginCall.isFace_image_state() && vxLoginCall.isFeatrue_state()){





            }else{

                Log.i(PUSH,"无人脸");


                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showVerifyFail();
                    }
                });
            }
        }*/

        /*VXLoginResult vxLoginResult = new Gson().fromJson(msg.custom,VXLoginResult.class);

        Log.i(PUSH,vxLoginResult.toString());
        vxLogin(qrCodeDialog,vxLoginResult.getUserId() , vxLoginResult.getQRCode(),nowFaceToken);*/

        //msg.custom;

        /*String custom =  msg.custom;
        JSONObject jsonObject = new JSONObject(custom);*/

        if(true){
            return;
        }

        try {


            //  微信登陆回调
            VXLoginResult vxLoginResult = new Gson().fromJson(msg.custom,VXLoginResult.class);


            //  如果 二维码token 不为空 ， 如果推送过来的消息不为空 ，如果二维码token 等于推送过来的消息中的 qrcode
            if(loginToken != null && vxLoginResult != null && loginToken.equals(vxLoginResult.getQRCode())){


                Log.i(PUSH,"设置用户id" + app.getUserId());
                //  修改当前设置的用户id
                app.setUserId(vxLoginResult.getUserId());
                Log.i(PUSH,"设置用户id" + app.getUserId());



                //  隐藏二维码扫码
                if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                    Log.i(PUSH,"隐藏二维码弹窗");
                    qrCodeDialog.dismiss();
                }

                //  云端有该人的人脸特征，则将特征保存到本地
                if(vxLoginResult.isFeature()){
                    Log.i(PUSH,"含有人脸特征值");
                    //  获取来自服务器人脸特征 ( 字符串之前是 Base64 形式 )
                    byte[] feature = Base64.decode(vxLoginResult.getFeatureData(),Base64.DEFAULT);

                    FacePassFeatureAppendInfo facePassFeatureAppendInfo = new FacePassFeatureAppendInfo();
                    //  插入人脸特征值，返回faceToken ，如果特征值不可用会抛出异常
                    String faceToken = mFacePassHandler.insertFeature(feature,facePassFeatureAppendInfo);
                    //  facetoken 绑定底库
                    boolean bindResult = mFacePassHandler.bindGroup(group_name, faceToken.getBytes());
                    //  绑定成功就可以 将 facetoken 和 id 进行绑定了

                    if(bindResult){
                        Log.i(PUSH,"绑定成功，将跳转控制台");
                        //  facetoken 和用户id 绑定
                        DataBaseUtil.getInstance(this).insertUserIdAndFaceToken(app.getUserId(),faceToken);

                        //  跳转到垃圾箱控制台
                        Intent intent = new Intent(MainActivity.this,ControlActivity.class);
                        intent.putExtra("userId",app.getUserId());
                        startActivity(intent);


                        //  将用户id和特征、图片上传至服务器   ===========================================


                    }else{
                        Log.i(PUSH,"绑定失败，删除人脸");
                        //  如果没有则删除之前的绑定
                        mFacePassHandler.deleteFace(faceToken.getBytes());
                    }
                }else{
                    //   云端 没有该用户的 人脸 特征值，则提示需要人脸注册
                    Log.i(PUSH,"显示人脸录入");


                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showVerifyFail();
                        }
                    });



                }
            }

        }catch (Exception e){
            toast(e.getMessage());
        }


    }


    private String tcp_client_id;

    private void initTCP(){
        TCPConnectUtil.getInstance().connect();
        TCPConnectUtil.getInstance().setListener(new NettyClientListener() {
            @Override
            public void onMessageResponseClient(byte[] bytes, int i) {
                //  来自服务器的响应
                final String response = new String(bytes, StandardCharsets.UTF_8);

                //  首先判定 响应中是否存在 type 和 data
                if(response.contains("type") && response.contains("data")){
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String type = jsonObject.getString("type");
                        final String data = jsonObject.getString("data");

                        //  连接认证
                        if(type.equals("connect_rz_msg")){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    
                                    Map<String,String> map = new HashMap<>();
                                    map.put("tcp_client_id",tcp_client_id);
                                    NetWorkUtil.getInstance().doPost(ServerAddress.REGISTER_TCP, map, new NetWorkUtil.NetWorkListener() {
                                        @Override
                                        public void success(String response) {
                                            Log.i("结果","绑定:" + response);
                                            if(response.contains("设备已绑定tcp连接")){
                                                //AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                            }else if(response.contains("连接池未找到改连接ID")){

                                            }

                                        }

                                        @Override
                                        public void fail(Call call, IOException e) {

                                        }

                                        @Override
                                        public void error(Exception e) {

                                        }
                                    });

                                }
                            });
                        }else if(type.equals("client_connect_msgect_msg")){
                            //  连接成功注册 与 绑定

                            long nowTime = System.currentTimeMillis() / 1000;
                            TCPVerify verify = new TCPVerify();
                            verify.setType("login");
                            TCPVerify.DataBean dataBean = new TCPVerify.DataBean();
                            dataBean.setSign(md5(nowTime + key).toUpperCase());
                            dataBean.setTimestamp(String.valueOf(nowTime));
                            verify.setData(dataBean);

                            Log.i("结果","发送" + gson.toJson(verify));

                            TCPConnectUtil.getInstance().sendData(gson.toJson(verify));


                            TCPVerifyResponse tcpVerify = gson.fromJson(data, TCPVerifyResponse.class);
                            tcp_client_id = tcpVerify.getClient_id();

                            //  延迟执行 ，避免 连接池未找到改连接ID,或者 在 connect_rz_msg 中执行
                            /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    TCPVerifyResponse tcpVerify = gson.fromJson(data, TCPVerifyResponse.class);
                                    Map<String,String> map = new HashMap<>();
                                    map.put("tcp_client_id",tcpVerify.getClient_id());
                                    NetWorkUtil.getInstance().doPost(ServerAddress.REGISTER_TCP, map, new NetWorkUtil.NetWorkListener() {
                                        @Override
                                        public void success(String response) {
                                            Log.i("结果","绑定:" + response);
                                            if(response.contains("设备已绑定tcp连接")){
                                                //AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                            }else if(response.contains("连接池未找到改连接ID")){

                                            }

                                        }

                                        @Override
                                        public void fail(Call call, IOException e) {

                                        }

                                        @Override
                                        public void error(Exception e) {

                                        }
                                    });

                                }
                            },1000);*/

                        }else if(type.equals("buy_success_msg")){
                            //  购买 成功反馈
                            BuySuccessMsg buySuccessMsg = gson.fromJson(data,BuySuccessMsg.class);
                            Log.i("结果",buySuccessMsg.toString());

                            EventBus.getDefault().post(buySuccessMsg);
                        }else if(type.equals("QrReturn")){

                            VXLoginCall vxLoginCall = gson.fromJson(data,VXLoginCall.class);
                            Log.i(NOW_TAG,"list 内容 : " + vxLoginCall.toString());
                            Log.i(NOW_TAG,"设置用户 id 之前的用户 id " + app.getUserId());
                            //  修改当前设置的用户id
                            app.setUserId(vxLoginCall.getInfo().getUser_id());
                            Log.i(NOW_TAG,"设置用户 id 之后 的用户 id" + app.getUserId());


                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //  隐藏二维码扫码
                                    if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                                        Log.i(NOW_TAG,"隐藏二维码弹窗");
                                        qrCodeDialog.dismiss();
                                    }
                                }
                            });

                            //  云端有该人的人脸特征，则将特征保存到本地
                            if(vxLoginCall.isFeatrue_state() && vxLoginCall.isFace_image_state()){
                                Log.i(NOW_TAG,"当前用户 含有人脸特征值 和 人脸图片");
                                //  获取来自服务器人脸特征 ( 字符串之前是 Base64 形式 )
                                byte[] feature = Base64.decode(vxLoginCall.getInfo().getFeatrue(),Base64.DEFAULT);

                                FacePassFeatureAppendInfo facePassFeatureAppendInfo = new FacePassFeatureAppendInfo();
                                //  插入人脸特征值，返回faceToken ，如果特征值不可用会抛出异常
                                String faceToken = mFacePassHandler.insertFeature(feature,facePassFeatureAppendInfo);
                                //  facetoken 绑定底库
                                boolean bindResult = mFacePassHandler.bindGroup(group_name, faceToken.getBytes());
                                //  绑定成功就可以 将 facetoken 和 id 进行绑定了

                                if(bindResult){
                                    Log.i(NOW_TAG,"绑定成功，将跳转控制台");
                                    //  facetoken 和用户id 绑定
                                    DataBaseUtil.getInstance(MainActivity.this).insertUserIdAndFaceToken(app.getUserId(),faceToken);

                                    //  跳转到垃圾箱控制台
                                    Intent intent = new Intent(MainActivity.this,ControlActivity.class);
                                    intent.putExtra("userId",app.getUserId());
                                    startActivity(intent);


                                    //  将用户id和特征、图片上传至服务器   ===========================================


                                }else{
                                    Log.i(NOW_TAG,"绑定失败，删除人脸");
                                    //  如果没有则删除之前的绑定
                                    mFacePassHandler.deleteFace(faceToken.getBytes());
                                }
                            }else{
                                //   云端 没有该用户的 人脸 特征值，则提示需要人脸注册
                                Log.i(NOW_TAG,"云端没有该人脸图片和特征值，显示人脸注册");


                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showVerifyFail();
                                    }
                                });

                            }

                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }



                Log.i("响应结果", response + "，状态:" + i);

                if(response.contains("client_id")){
                    long nowTime = System.currentTimeMillis() / 1000;
                    TCPVerify verify = new TCPVerify();
                    verify.setType("login");
                    TCPVerify.DataBean dataBean = new TCPVerify.DataBean();
                    dataBean.setSign(md5(nowTime + key).toUpperCase());
                    dataBean.setTimestamp(String.valueOf(nowTime));
                    verify.setData(dataBean);

                    Log.i("结果","发送" + gson.toJson(verify));

                    TCPConnectUtil.getInstance().sendData(gson.toJson(verify));


                    /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            TCPVerifyResponse tcpVerify = gson.fromJson(response, TCPVerifyResponse.class);
                            Map<String,String> map = new HashMap<>();
                            map.put("tcp_client_id",tcpVerify.getClient_id());
                            NetWorkUtil.getInstance().doPost(ServerAddress.REGISTER_TCP, map, new NetWorkUtil.NetWorkListener() {
                                @Override
                                public void success(String response) {
                                    Log.i("结果","绑定:"+response);
                                }

                                @Override
                                public void fail(Call call, IOException e) {

                                }

                                @Override
                                public void error(Exception e) {

                                }
                            });

                        }
                    },2000);*/

                }

                /*String response = new String(bytes, StandardCharsets.UTF_8);

                Log.i("响应结果","bytes:" + response + "\n i:" + i);

                if(response.contains("client_id")) {

                    TCPVerifyResponse tcpVerify = gson.fromJson(new String(bytes), TCPVerifyResponse.class);

                    if(tcpVerify.getClient_id() != null){
                        long nowTime = System.currentTimeMillis() / 1000;
                        TCPVerify verify = new TCPVerify();
                        verify.setType("login");
                        TCPVerify.DataBean dataBean = new TCPVerify.DataBean();
                        dataBean.setSign(md5(nowTime + key).toUpperCase());
                        dataBean.setTimestamp(String.valueOf(nowTime));
                        verify.setData(dataBean);

                        //  发送
                        TCPConnectUtil.getInstance().sendData(gson.toJson(verify));

                        Log.i("结果","发送" + gson.toJson(verify));



                        Map<String,String> map = new HashMap<>();
                        map.put("tcp_client_id",tcpVerify.getClient_id());
                        NetWorkUtil.getInstance().doPost(ServerAddress.REGISTER_TCP, map, new NetWorkUtil.NetWorkListener() {
                            @Override
                            public void success(String response) {
                                Log.i("结果","绑定:"+response);
                            }

                            @Override
                            public void fail(Call call, IOException e) {

                            }

                            @Override
                            public void error(Exception e) {

                            }
                        });
                    }

                }*/

            }

            @Override
            public void onClientStatusConnectChanged(int i, int i1) {

                Log.i("Netty","i : " + i + " , " + i1);
                if(i == 1){
                    Log.i("Netty","连接成功");
                }else{
                    //  重新连接
                    TCPConnectUtil.getInstance().reconnect();
                    Log.i("Netty","重新连接");
                }
            }
        });

    }

    //  初始化人脸识别库
    private void initFaceHandler() {

        new Thread() {
            @Override
            public void run() {
                while (true && !isFinishing()) {
                    while (FacePassHandler.isAvailable()) {
                        Log.d(DEBUG_TAG, "start to build FacePassHandler");
                        FacePassConfig config;
                        try {
                            /* 填入所需要的配置 */
                            config = new FacePassConfig();
                            config.poseBlurModel = FacePassModel.initModel(getApplicationContext().getAssets(), "attr.pose_blur.align.av200.190630.bin");

                            //单目使用CPU rgb活体模型
                            config.livenessModel = FacePassModel.initModel(getApplicationContext().getAssets(), "liveness.CPU.rgb.int8.D.bin");
                            //双目使用CPU rgbir活体模型
                            config.rgbIrLivenessModel = FacePassModel.initModel(getApplicationContext().getAssets(), "liveness.CPU.rgbir.int8.D.bin");
                            //当单目或者双目有一个使用GPU活体模型时，请设置livenessGPUCache
                            config.livenessGPUCache = FacePassModel.initModel(getApplicationContext().getAssets(), "liveness.GPU.AlgoPolicy.D.cache");

                            config.searchModel = FacePassModel.initModel(getApplicationContext().getAssets(), "feat2.arm.G.v1.0_1core.bin");
                            config.detectModel = FacePassModel.initModel(getApplicationContext().getAssets(), "detector.arm.D.bin");
                            config.detectRectModel = FacePassModel.initModel(getApplicationContext().getAssets(), "detector_rect.arm.D.bin");
                            config.landmarkModel = FacePassModel.initModel(getApplicationContext().getAssets(), "pf.lmk.arm.D.bin");

                            config.mouthOccAttributeModel = FacePassModel.initModel(getApplicationContext().getAssets(), "attribute.mouth.occ.gray.12M.190930.bin");
                            config.smileModel = FacePassModel.initModel(getApplicationContext().getAssets(), "attr.smile.mgf29.0.1.1.181229.bin");
                            config.ageGenderModel = FacePassModel.initModel(getApplicationContext().getAssets(), "attr.age_gender.surveillance.nnie.av200.0.1.0.190630.bin");
                            config.occlusionFilterModel = FacePassModel.initModel(getApplicationContext().getAssets(), "occlusion.all_attr_configurable.occ.190816.bin");
                            //如果不需要表情和年龄性别功能，smileModel和ageGenderModel可以为null
                            config.smileModel = null;
                            config.ageGenderModel = null;

                            //config.occlusionFilterEnabled = true;
                            config.mouthOccAttributeEnabled = true;
                            config.searchThreshold = 71f;    //未带口罩时，识别使用的阈值
                            config.searchExtThreshold = 64.71f; //带口罩时，识别使用的阈值
                            config.livenessThreshold = 60f;
                            config.livenessEnabled = true;
                            config.rgbIrLivenessEnabled = false;

                            ageGenderEnabledGlobal = (config.ageGenderModel != null);
                            config.faceMinThreshold = 100;
                            config.poseThreshold = new FacePassPose(30f, 30f, 30f);
                            config.blurThreshold = 0.8f;
                            config.lowBrightnessThreshold = 70f;
                            config.highBrightnessThreshold = 210f;
                            config.brightnessSTDThreshold = 80f;
                            config.retryCount = 10;
                            config.smileEnabled = false;
                            config.maxFaceEnabled = true;

                            config.rotation = cameraRotation;
                            config.fileRootPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

                            /* 创建SDK实例 */
                            mFacePassHandler = new FacePassHandler(config);

                            FacePassConfig addFaceConfig = mFacePassHandler.getAddFaceConfig();
                            addFaceConfig.blurThreshold = 0.8f;
                            addFaceConfig.faceMinThreshold = 100;
                            mFacePassHandler.setAddFaceConfig(addFaceConfig);

                            checkGroup();
                        } catch (FacePassException e) {
                            e.printStackTrace();
                            Log.d(DEBUG_TAG, "FacePassHandler is null");
                            return;
                        }
                        return;
                    }
                    try {
                        /* 如果SDK初始化未完成则需等待 */
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    protected void onResume() {


        checkGroup();

        initToast();

        /* 打开相机 */
        if (hasPermission()) {
            manager.open(getWindowManager(), false, cameraWidth, cameraHeight);
        }

        adaptFrameLayout();
        super.onResume();
    }


    //  检查底库创建情况
    private void checkGroup() {
        if (mFacePassHandler == null) {
            return;
        }
        //  获取所有底库
        String[] localGroups = mFacePassHandler.getLocalGroups();
        isLocalGroupExist = false;

        //  如果为 null 直接提示
        if (localGroups == null || localGroups.length == 0) {

            //  如果底库不存在，默认创建底库
            try {
                mFacePassHandler.createLocalGroup(group_name);
                isLocalGroupExist = true;
            }catch (Exception e){
                e.printStackTrace();
            }

            return;
        }

        //  如果没有找到名称符合规则的
        for (String group : localGroups) {
            if (group_name.equals(group)) {
                isLocalGroupExist = true;
            }
        }
        if (!isLocalGroupExist) {

            //  如果底库不存在，默认创建底库
            try {
                mFacePassHandler.createLocalGroup(group_name);
                isLocalGroupExist = true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    /* 相机回调函数 */
    @Override
    public void onPictureTaken(CameraPreviewData cameraPreviewData) {
        mFeedFrameQueue.offer(cameraPreviewData);
        //  Log.i(DEBUG_TAG, "feedframe");
    }

    //  这里是处理人脸识别的线程
    private class FeedFrameThread extends Thread {
        boolean isInterrupt;
        @Override
        public void run() {
            //  是否中断
            while (!isInterrupt) {
                CameraPreviewData cameraPreviewData = null;
                try {
                    cameraPreviewData = mFeedFrameQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                if (mFacePassHandler == null) {
                    continue;
                }
                /* 将相机预览帧转成SDK算法所需帧的格式 FacePassImage */
                long startTime = System.currentTimeMillis(); //起始时间

                FacePassImage image;
                try {
                    image = new FacePassImage(cameraPreviewData.nv21Data, cameraPreviewData.width, cameraPreviewData.height, cameraRotation, FacePassImageType.NV21);
                } catch (FacePassException e) {
                    e.printStackTrace();
                    continue;
                }

                /* 将每一帧FacePassImage 送入SDK算法， 并得到返回结果 */
                FacePassDetectionResult detectionResult = null;
                try {

                    detectionResult = mFacePassHandler.feedFrame(image);
                } catch (FacePassException e) {
                    e.printStackTrace();
                }

                if (detectionResult == null || detectionResult.faceList.length == 0) {
                    /* 当前帧没有检出人脸 */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            faceView.clear();
                            faceView.invalidate();
                        }
                    });
                } else {
                    /* 将识别到的人脸在预览界面中圈出，并在上方显示人脸位置及角度信息 */
                    final FacePassFace[] bufferFaceList = detectionResult.faceList;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showFacePassFace(bufferFaceList);
                        }
                    });
                }

                //  如果是联机的状态，就传递 detectionResult 对象给服务器进行处理，返回的 FacePassRecognitionResult 对象带有 faceToken ，faceToken（人脸唯一id）
                if (SDK_MODE == FacePassSDKMode.MODE_ONLINE) {
                    /*抓拍版模式*/
                    if (detectionResult != null && detectionResult.message.length != 0) {
                        /* 构建http请求 */
                        FacePassRequest request = new FacePassRequest(recognize_url, detectionResult, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(DEBUG_TAG, String.format("%s", response));
                                try {
                                    JSONObject jsresponse = new JSONObject(response);
                                    int code = jsresponse.getInt("code");
                                    if (code != 0) {
                                        Log.e(DEBUG_TAG, String.format("error code: %d", code));
                                        return;
                                    }
                                    /* 将服务器返回的结果交回SDK进行处理来获得识别结果 */
                                    FacePassRecognitionResult[] result = null;
                                    try {
                                        Log.i("lengthlength", "length is " + jsresponse.getString("data").getBytes().length);
                                        result = mFacePassHandler.decodeResponse(jsresponse.getString("data").getBytes());
                                    } catch (FacePassException e) {
                                        e.printStackTrace();
                                        return;
                                    }
                                    if (result == null || result.length == 0) {
                                        return;
                                    }

                                    for (FacePassRecognitionResult res : result) {
                                        String faceToken = new String(res.faceToken);

                                        Log.i(MY_TAG, "onResponse faceToken : " + faceToken);

                                        if (FacePassRecognitionResultType.RECOG_OK == res.facePassRecognitionResultType) {
                                            getFaceImageByFaceToken(res.trackId, faceToken);
                                        }
                                        showRecognizeResult(res.trackId, res.detail.searchScore, res.detail.livenessScore, FacePassRecognitionResultType.RECOG_OK == res.facePassRecognitionResultType);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //  请求失败

                                if(error.getMessage() != null){
                                    Log.i(MY_TAG, "网络请求失败 ：" + error.getMessage());
                                }

                                Log.e(DEBUG_TAG, "volley error response");
                                if (error.networkResponse != null) {
                                    faceEndTextView.append(String.format("network error %d", error.networkResponse.statusCode));
                                } else {
                                    String errorMessage = error.getClass().getSimpleName();
                                    faceEndTextView.append("network error" + errorMessage);
                                }
                                faceEndTextView.append("\n");
                            }
                        });
                        request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        Log.d(DEBUG_TAG, "request add");
                        request.setTag("upload_detect_result_tag");
                        requestQueue.add(request);
                    }
                } else {
                    /*离线模式，将识别到人脸的，message不为空的result添加到处理队列中*/
                    if (detectionResult != null && detectionResult.message.length != 0) {
                        Log.d(DEBUG_TAG, "mDetectResultQueue.offer");

                        //  FacePassRecognitionResult


                        Log.i(MY_TAG,"触发离线识别");

                        //  为空
                        //  Log.i(MY_TAG,detectionResult.feedback[0].trackId+"");




                        FacePassDetectionFeedback[] facePassDetectionResult = detectionResult.feedback;

                        if(facePassDetectionResult != null && facePassDetectionResult.length != 0){
                            for(FacePassDetectionFeedback f : facePassDetectionResult){

                            }
                        }else{
                            Log.i(MY_TAG,"facePassDetectionResult == null && facePassDetectionResult.length == 0");
                        }



                        /*String faceToken = new String(detectionResult.message);
                        Log.i(MY_TAG, "无线状态下 detectionResult.message " + faceToken);*/




                        mDetectResultQueue.offer(detectionResult.message);
                    }
                }
                long endTime = System.currentTimeMillis(); //结束时间
                long runTime = endTime - startTime;
                //  Log.i("]time", String.format("feedfream %d ms", runTime));


                //  如果 30 s内没有出现人脸则 跳转到广告页面
                if(autoIntentAdvertising & endTime - lastFaceTime > (300 * 1000)){
                    autoIntentAdvertising = false;

                    Intent intent = new Intent(MainActivity.this,AdvertisingActivity.class);
                    startActivityForResult(intent,INTENT_ADVERTISING_CODE); //  这里是携带返回值的，如果到时候从广告界面返回，就又要启动 30 s自动跳转到广告界面

                }
            }
        }
        @Override
        public void interrupt() {
            isInterrupt = true;
            super.interrupt();
        }
    }



    //  根据 trackId 查下性别年龄结果
    int findidx(FacePassAgeGenderResult[] results, long trackId) {
        int result = -1;
        if (results == null) {
            return result;
        }
        for (int i = 0; i < results.length; ++i) {
            if (results[i].trackId == trackId) {
                return i;
            }
        }
        return result;
    }

    //  识别结果处理线程
    private class RecognizeThread extends Thread {

        boolean isInterrupt;

        @Override
        public void run() {
            while (!isInterrupt) {
                try {
                    byte[] detectionResult = mDetectResultQueue.take();

                    //  年龄性别结果。
                    FacePassAgeGenderResult[] ageGenderResult = null;
                    //if (ageGenderEnabledGlobal) {
                    //    ageGenderResult = mFacePassHandler.getAgeGender(detectionResult);
                    //    for (FacePassAgeGenderResult t : ageGenderResult) {
                    //        Log.e("FacePassAgeGenderResult", "id " + t.trackId + " age " + t.age + " gender " + t.gender);
                    //    }
                    //}

                    Log.i(MY_TAG,"触发无线人脸识别处理线程");

                    Log.d(DEBUG_TAG, "mDetectResultQueue.isLocalGroupExist");

                    //  是否存在底库
                    if (isLocalGroupExist) {
                        Log.d(DEBUG_TAG, "mDetectResultQueue.recognize");

                        Log.i(MY_TAG,"isLocalGroupExist 为 true ， 底库存在。");

                        //  获取 返回识别结果（FacePassRecognitionResult）的数组，每一项对应一张图的识别结果。
                        FacePassRecognitionResult[] recognizeResult = mFacePassHandler.recognize(group_name, detectionResult);



                        //  判定返回识别结果是不是为空
                        if (recognizeResult != null && recognizeResult.length > 0) {
                            Log.i(MY_TAG,"人脸识别正常");

                            for (FacePassRecognitionResult result : recognizeResult) {

                                String faceToken = new String(result.faceToken);


                                nowFaceToken = faceToken;


                                Log.i(MY_TAG,"faceToken 离线状态下的人脸识别"  + faceToken);


                                //  查询faceToken 是否对应某一个从服务器传过来 userId，如果存在则直接进入垃圾箱控制台

                                if (FacePassRecognitionResultType.RECOG_OK == result.facePassRecognitionResultType) {
                                    getFaceImageByFaceToken(result.trackId, faceToken);
                                }

                                int idx = findidx(ageGenderResult, result.trackId);
                                //  -1就是没有找到的意思， 也就是 ageGenderResult (年龄性别结果) 为 null
                                if (idx == -1) {

                                    //  没有性别的 Toast
                                    showRecognizeResult(result.trackId, result.detail.searchScore, result.detail.livenessScore, !TextUtils.isEmpty(faceToken));
                                } else {
                                    //  有性别的 Toast
                                    showRecognizeResult(result.trackId, result.detail.searchScore, result.detail.livenessScore, !TextUtils.isEmpty(faceToken), ageGenderResult[idx].age, ageGenderResult[idx].gender);
                                }
                            }
                        }else{
                            //  底库中没有人脸,显示人脸验证失败，注册人脸
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(NOW_TAG, "验证失败，安卓板底库中没有该人脸");
                                    showQRCodeDialog();
                                    //  showVerifyFail();
                                }
                            });
                            Log.i(MY_TAG,"人脸识别为空 recognizeResult != null && recognizeResult.length > 0) 为 false");
                        }
                    }else{
                        Log.i(MY_TAG,"isLocalGroupExist 为 false ，底库不存在。");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (FacePassException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void interrupt() {
            isInterrupt = true;
            super.interrupt();
        }
    }

    //  debug 界面添加 日志，并打印到界面上
    private void showRecognizeResult(final long trackId, final float searchScore, final float livenessScore, final boolean isRecognizeOK) {
        mAndroidHandler.post(new Runnable() {
            @Override
            public void run() {
                if (searchScore < 64) {
                    //showToast("ID = " + String.valueOf(trackId), Toast.LENGTH_SHORT, false, null);    原先处理方式
                    Log.i(MY_TAG,"searchScore : " + searchScore + ",livenessScore : " + livenessScore + "验证不通过" );
                    //  显示验证识别并录脸
                    Log.i(NOW_TAG, "验证失败，人脸分小于 64 显示二维码登陆弹窗");
                    showQRCodeDialog();
                    //  showVerifyFail();
                }else{
                    showToast("ID = " + nowFaceToken, Toast.LENGTH_SHORT, true, null);
                    Log.i(MY_TAG,"searchScore : " + searchScore + ",livenessScore : " + livenessScore + "验证通过" );

                    queryFaceToken(nowFaceToken);
                }

                faceEndTextView.append("ID = " + trackId + (isRecognizeOK ? "识别成功" : "识别失败") + "\n");
                faceEndTextView.append("识别分 = " + searchScore + "\n");
                faceEndTextView.append("活体分 = " + livenessScore + "\n");
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }



    //  debug 界面添加 日志，并打印到界面上
    private void showRecognizeResult(final long trackId, final float searchScore, final float livenessScore, final boolean isRecognizeOK, final float age, final int gender) {
        mAndroidHandler.post(new Runnable() {
            @Override
            public void run() {

                faceEndTextView.append("ID = " + trackId + (isRecognizeOK ? "识别成功" : "识别失败") + "\n");
                faceEndTextView.append("识别分 = " + searchScore + "\n");
                faceEndTextView.append("活体分 = " + livenessScore + "\n");
                faceEndTextView.append("年龄 = " + age + "\n");


                if (gender == 0) {
                    faceEndTextView.append("性别 = " + "男" + "\n");
                } else if (gender == 1) {
                    faceEndTextView.append("性别 = " + "女" + "\n");
                } else {
                    faceEndTextView.append("性别 = " + "unknown" + "\n");
                }
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    /* 判断程序是否有所需权限 android22以上需要自申请权限 */
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_READ_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_WRITE_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_INTERNET) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    /* 请求程序所需权限 */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(Permission, PERMISSIONS_REQUEST);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED)
                    granted = false;
            }
            if (!granted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    if (!shouldShowRequestPermissionRationale(PERMISSION_CAMERA)
                            || !shouldShowRequestPermissionRationale(PERMISSION_READ_STORAGE)
                            || !shouldShowRequestPermissionRationale(PERMISSION_WRITE_STORAGE)
                            || !shouldShowRequestPermissionRationale(PERMISSION_INTERNET)
                            || !shouldShowRequestPermissionRationale(PERMISSION_ACCESS_NETWORK_STATE)) {
                        Toast.makeText(getApplicationContext(), "需要开启摄像头网络文件存储权限", Toast.LENGTH_SHORT).show();
                    }
            } else {
                initFacePassSDK();
            }
        }
    }

    private void adaptFrameLayout() {
        SettingVar.isButtonInvisible = false;
        SettingVar.iscameraNeedConfig = false;
    }

    private void initToast() {
        SettingVar.isButtonInvisible = false;
    }

    //  初始化布局
    private void initView() {
        //  获取窗口旋转角度
        int windowRotation = ((WindowManager) (getApplicationContext().getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getRotation() * 90;
        if (windowRotation == 0) {
            cameraRotation = FacePassImageRotation.DEG90;
        } else if (windowRotation == 90) {
            cameraRotation = FacePassImageRotation.DEG0;
        } else if (windowRotation == 270) {
            cameraRotation = FacePassImageRotation.DEG180;
        } else {
            cameraRotation = FacePassImageRotation.DEG270;
        }


        Log.i(DEBUG_TAG, "Rotation: cameraRation: " + cameraRotation);
        cameraFacingFront = true;

        //  用户资料
        SharedPreferences preferences = getSharedPreferences(SettingVar.SharedPrefrence, Context.MODE_PRIVATE);
        //  设置是否可用
        SettingVar.isSettingAvailable = preferences.getBoolean("isSettingAvailable", SettingVar.isSettingAvailable);
        //  显示屏是否交叉
        SettingVar.isCross = preferences.getBoolean("isCross", SettingVar.isCross);

        SettingVar.faceRotation = preferences.getInt("faceRotation", SettingVar.faceRotation);
        SettingVar.cameraPreviewRotation = preferences.getInt("cameraPreviewRotation", SettingVar.cameraPreviewRotation);
        SettingVar.cameraFacingFront = preferences.getBoolean("cameraFacingFront", SettingVar.cameraFacingFront);

        //  如果设置可用的话，进行赋值
        if (SettingVar.isSettingAvailable) {
            cameraRotation = SettingVar.faceRotation;
            cameraFacingFront = SettingVar.cameraFacingFront;
        }

        Log.i(DEBUG_TAG, "Rotation: screenRotation: " + String.valueOf(windowRotation));
        Log.i(DEBUG_TAG, "Rotation: faceRotation: " +  SettingVar.faceRotation);
        Log.i(DEBUG_TAG, "Rotation: new cameraRation: " + cameraRotation);

        //  获取屏幕朝向
        final int mCurrentOrientation = getResources().getConfiguration().orientation;
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            screenState = 1;
        } else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            screenState = 0;
        }


        setContentView(R.layout.activity_main);


        //  添加按钮以及设置监听器
        mSyncGroupBtn = (ImageView) findViewById(R.id.btn_group_name);
        mSyncGroupBtn.setOnClickListener(this);

        mFaceOperationBtn = (ImageView) findViewById(R.id.btn_face_operation);
        mFaceOperationBtn.setOnClickListener(this);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        heightPixels = displayMetrics.heightPixels;
        widthPixels = displayMetrics.widthPixels;
        SettingVar.mHeight = heightPixels;
        SettingVar.mWidth = widthPixels;
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        AssetManager mgr = getAssets();
        Typeface tf = Typeface.createFromAsset(mgr, "fonts/Univers LT 57 Condensed.ttf");
        /* 初始化界面 */
        faceEndTextView = (TextView) this.findViewById(R.id.tv_meg2);
        faceEndTextView.setTypeface(tf);
        faceView = (FaceView) this.findViewById(R.id.fcview);
        settingButton = (Button) this.findViewById(R.id.settingid);

        //  连续点击 5 下 设置按钮，即可打开设置界面，间隔不超过 0.6s，跳转到设置界面
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long curTime = System.currentTimeMillis();
                long durTime = curTime - mLastClickTime;
                mLastClickTime = curTime;
                if (durTime < CLICK_INTERVAL) {
                    ++mSecretNumber;
                    if (mSecretNumber == 5) {

                        adminLoginDialog = new AdminLoginDialog(MainActivity.this);
                        adminLoginDialog.setLoginListener(new AdminLoginDialog.LoginListener() {
                            @Override
                            public void callBack(String editStr,String password,AlertDialog alertDialog) {

                                if(editStr.equals("123")){
                                    alertDialog.dismiss();

                                    Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                                    startActivity(intent);

                                    //  关闭当前activity
                                    MainActivity.this.finish();
                                }else{
                                    Toast.makeText(MainActivity.this,"登陆失败，密码错误",Toast.LENGTH_LONG).show();
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
        SettingVar.cameraSettingOk = false;

        //  debug 中用来显示日志的布局
        ll = (LinearLayout) this.findViewById(R.id.ll);
        ll.getBackground().setAlpha(100);
        //  debug 按钮
        visible = (Button) this.findViewById(R.id.visible);
        visible.setBackgroundResource(R.drawable.debug);
        //  buttonFlag 就是用来切换了 显示与隐藏了
        visible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonFlag == 0) {
                    ll.setVisibility(View.VISIBLE);
                    if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        visible.setBackgroundResource(R.drawable.down);
                    } else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        visible.setBackgroundResource(R.drawable.right);
                    }
                    buttonFlag = 1;
                } else if (buttonFlag == 1) {
                    buttonFlag = 0;
                    if (SettingVar.isButtonInvisible)
                        ll.setVisibility(View.INVISIBLE);
                    else
                        ll.setVisibility(View.GONE);
                    visible.setBackgroundResource(R.drawable.debug);
                }

            }
        });


        manager = new CameraManager();
        cameraView = (CameraPreview) findViewById(R.id.preview);
        manager.setPreviewDisplay(cameraView);
        frameLayout = (FrameLayout) findViewById(R.id.frame);
        /* 注册相机回调函数 */
        manager.setListener(this);

        //  切换人脸识别模式 (有线或者无线)
        mSDKModeBtn=(Button)findViewById(R.id.btn_mode_switch);
        mSDKModeBtn.setText(SDK_MODE.toString());

        //  切换人脸识别的有线无线状态
        mSDKModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SDK_MODE == FacePassSDKMode.MODE_OFFLINE) {
                    SDK_MODE = FacePassSDKMode.MODE_ONLINE;
                    recognize_url = "http://" + serverIP_online + ":8080/api/service/recognize/v1";
                    serverIP = serverIP_online;
                    mSDKModeBtn.setText("当前已联网" +SDK_MODE.toString());
                } else {
                    SDK_MODE = FacePassSDKMode.MODE_OFFLINE;
                    serverIP = serverIP_offline;
                    mSDKModeBtn.setText("当前未联网" + SDK_MODE.toString());
                }
            }
        });

    }


    @Override
    protected void onStop() {
        SettingVar.isButtonInvisible = false;
        mDetectResultQueue.clear();
        if (manager != null) {
            manager.release();
        }


        //  跳转到其它 activity 则暂时先禁用 30 s 跳转广告
        autoIntentAdvertising = false;


        super.onStop();
    }

    @Override
    protected void onRestart() {
        lastFaceTime = System.currentTimeMillis();
        autoIntentAdvertising = true;


        faceView.clear();
        faceView.invalidate();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        mRecognizeThread.isInterrupt = true;
        mFeedFrameThread.isInterrupt = true;

        mRecognizeThread.interrupt();
        mFeedFrameThread.interrupt();
        if (requestQueue != null) {
            requestQueue.cancelAll("upload_detect_result_tag");
            requestQueue.cancelAll("handle_sync_request_tag");
            requestQueue.cancelAll("load_image_request_tag");
            requestQueue.stop();
        }

        if (manager != null) {
            manager.release();
        }
        if (mAndroidHandler != null) {
            mAndroidHandler.removeCallbacksAndMessages(null);
        }

        if (mFacePassHandler != null) {
            mFacePassHandler.release();
        }

        EventBus.getDefault().unregister(this);

        TCPConnectUtil.getInstance().disconnect();



        super.onDestroy();
    }


    //  最后检测到有人脸的时间
    private static long lastFaceTime = System.currentTimeMillis();
    //  是否启用 30 没有人脸自动跳转广告界面
    private volatile boolean autoIntentAdvertising  = true;
    //  跳转到广告界面
    public final static int INTENT_ADVERTISING_CODE = 600;

    //  将识别到的人脸在预览界面中圈出，并在上方显示人脸位置及角度信息 ,也就是那个框
    private void showFacePassFace(FacePassFace[] detectResult) {
        faceView.clear();
        for (FacePassFace face : detectResult) {
            Log.d("facefacelist", "width " + (face.rect.right - face.rect.left) + " height " + (face.rect.bottom - face.rect.top) );
            Log.d("facefacelist", "smile " + face.smile);
            boolean mirror = cameraFacingFront; /* 前摄像头时mirror为true */

            StringBuilder faceIdString = new StringBuilder();
            faceIdString.append("ID = ").append(face.trackId);
            SpannableString faceViewString = new SpannableString(faceIdString);
            faceViewString.setSpan(new TypefaceSpan("fonts/kai"), 0, faceViewString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            StringBuilder faceRollString = new StringBuilder();
            faceRollString.append("旋转: ").append((int) face.pose.roll).append("°");
            StringBuilder facePitchString = new StringBuilder();
            facePitchString.append("上下: ").append((int) face.pose.pitch).append("°");
            StringBuilder faceYawString = new StringBuilder();
            faceYawString.append("左右: ").append((int) face.pose.yaw).append("°");
            StringBuilder faceBlurString = new StringBuilder();
            faceBlurString.append("模糊: ").append(face.blur);
            StringBuilder smileString = new StringBuilder();
            smileString.append("微笑: ").append(String.format("%.6f", face.smile));
            Matrix mat = new Matrix();
            int w = cameraView.getMeasuredWidth();
            int h = cameraView.getMeasuredHeight();

            int cameraHeight = manager.getCameraheight();
            int cameraWidth = manager.getCameraWidth();

            float left = 0;
            float top = 0;
            float right = 0;
            float bottom = 0;
            switch (cameraRotation) {
                case 0:
                    left = face.rect.left;
                    top = face.rect.top;
                    right = face.rect.right;
                    bottom = face.rect.bottom;
                    mat.setScale(mirror ? -1 : 1, 1);
                    mat.postTranslate(mirror ? (float) cameraWidth : 0f, 0f);
                    mat.postScale((float) w / (float) cameraWidth, (float) h / (float) cameraHeight);
                    break;
                case 90:
                    mat.setScale(mirror ? -1 : 1, 1);
                    mat.postTranslate(mirror ? (float) cameraHeight : 0f, 0f);
                    mat.postScale((float) w / (float) cameraHeight, (float) h / (float) cameraWidth);
                    left = face.rect.top;
                    top = cameraWidth - face.rect.right;
                    right = face.rect.bottom;
                    bottom = cameraWidth - face.rect.left;
                    break;
                case 180:
                    mat.setScale(1, mirror ? -1 : 1);
                    mat.postTranslate(0f, mirror ? (float) cameraHeight : 0f);
                    mat.postScale((float) w / (float) cameraWidth, (float) h / (float) cameraHeight);
                    left = face.rect.right;
                    top = face.rect.bottom;
                    right = face.rect.left;
                    bottom = face.rect.top;
                    break;
                case 270:
                    mat.setScale(mirror ? -1 : 1, 1);
                    mat.postTranslate(mirror ? (float) cameraHeight : 0f, 0f);
                    mat.postScale((float) w / (float) cameraHeight, (float) h / (float) cameraWidth);
                    left = cameraHeight - face.rect.bottom;
                    top = face.rect.left;
                    right = cameraHeight - face.rect.top;
                    bottom = face.rect.right;
            }
            //  绘制框框
            RectF drect = new RectF();
            RectF srect = new RectF(left, top, right, bottom);

            mat.mapRect(drect, srect);
            faceView.addRect(drect);
            faceView.addId(faceIdString.toString());
            faceView.addRoll(faceRollString.toString());
            faceView.addPitch(facePitchString.toString());
            faceView.addYaw(faceYawString.toString());
            faceView.addBlur(faceBlurString.toString());
            faceView.addSmile(smileString.toString());

            //  最近一次有人脸出现的时间
            lastFaceTime = System.currentTimeMillis();
        }
        faceView.invalidate();
    }




    private String nowFaceToken;
    //  显示通过人脸图片
    public void showToast(CharSequence text, int duration, boolean isSuccess, Bitmap bitmap) {
        //  如果当前显示二维码弹窗，就不要显示验证成功了
        if(qrCodeDialog != null && qrCodeDialog.isShowing()){
            return;
        }

        LayoutInflater inflater = getLayoutInflater();
        View toastView = inflater.inflate(R.layout.toast, null);
        LinearLayout toastLLayout = (LinearLayout) toastView.findViewById(R.id.toastll);
        if (toastLLayout == null) {
            return;
        }
        ImageView imageView = (ImageView) toastView.findViewById(R.id.toastImageView);
        TextView idTextView = (TextView) toastView.findViewById(R.id.toastTextView);
        TextView stateView = (TextView) toastView.findViewById(R.id.toastState);
        SpannableString s;
        if (isSuccess) {
            s = new SpannableString("验证成功");
            imageView.setImageResource(R.drawable.ic_success);

            //  VoiceUtil.getInstance(MainActivity.this).startAuto("验证成功");

            //  如果此时人脸识别失败弹窗存在，则先关闭
            if(alertDialog!=null && alertDialog.isShowing()){
                alertDialog.dismiss();
            }


        } else {
            s = new SpannableString("验证失败");
            imageView.setImageResource(R.drawable.ic_baseline_error_outline_24);


            Log.i(NOW_TAG, "验证失败，showToast 中的 验证失败");
            showQRCodeDialog();
            //  显示验证识别并录脸
            //  showVerifyFail();

        }
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
        stateView.setText(s);


        //  faceToken 就是这里设置的
        idTextView.setText(text);

        if (mRecoToast == null) {
            mRecoToast = new Toast(getApplicationContext());
            mRecoToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        }
        mRecoToast.setDuration(duration);
        mRecoToast.setView(toastView);

        mRecoToast.show();
    }

    private static final int REQUEST_CODE_CHOOSE_PICK = 1;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_group_name:
                //  显示同步弹窗
                showSyncGroupDialog();
                break;
            case R.id.btn_face_operation:
                //  显示添加人脸弹窗
                showAddFaceDialog();
                break;
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //从相册选取照片后读取地址
            case REQUEST_CODE_CHOOSE_PICK:
                if (resultCode == RESULT_OK) {
                    String path = "";
                    Uri uri = data.getData();
                    String[] pojo = {MediaStore.Images.Media.DATA};
                    CursorLoader cursorLoader = new CursorLoader(this, uri, pojo, null, null, null);
                    Cursor cursor = cursorLoader.loadInBackground();
                    if (cursor != null) {
                        cursor.moveToFirst();
                        path = cursor.getString(cursor.getColumnIndex(pojo[0]));
                    }
                    if (!TextUtils.isEmpty(path) && "file".equalsIgnoreCase(uri.getScheme())) {
                        path = uri.getPath();
                    }
                    if (TextUtils.isEmpty(path)) {
                        try {
                            path = FileUtil.getPath(getApplicationContext(), uri);
                        } catch (Exception e) {
                        }
                    }
                    if (TextUtils.isEmpty(path)) {
                        toast("图片选取失败！");
                        return;
                    }

                    //  如果录脸弹窗 显示了的话
                    if (!TextUtils.isEmpty(path) && mFaceOperationDialog != null && mFaceOperationDialog.isShowing()) {
                        EditText imagePathEdt = (EditText) mFaceOperationDialog.findViewById(R.id.et_face_image_path);
                        imagePathEdt.setText(path);
                    }
                }
                break;
            case REQUEST_CODE_CAMERA:   //  验证失败--扫码登陆--显示人脸未注册--注册人脸

                String TAG = "特征";

                //  获取位图对象
                Bitmap bitmap = BitmapFactory.decodeFile(file.toString());

                try {
                    //  提取特征值
                    FacePassExtractFeatureResult facePassExtractFeatureResult = mFacePassHandler.extractFeature(bitmap);

                    //  如果特征值合格
                    if(facePassExtractFeatureResult.result == 0){

                        FacePassFeatureAppendInfo facePassFeatureAppendInfo = new FacePassFeatureAppendInfo();

                        //  创建 faceToken
                        String faceToken = mFacePassHandler.insertFeature(facePassExtractFeatureResult.featureData,facePassFeatureAppendInfo);

                        nowFaceToken = faceToken;

                        boolean bindResult = mFacePassHandler.bindGroup(group_name, faceToken.getBytes());

                        //  绑定结果
                        if(bindResult){
                            Log.i(TAG,"绑定成功" + faceToken);

                            //  将该facetoken 和用户id进行绑定

                            //  上传人脸图片,特征值，以及用户id


                            uploadFace(file,facePassExtractFeatureResult.featureData,app.getUserId());
                        }else{
                            mFacePassHandler.deleteFace(faceToken.getBytes());
                            Log.i(TAG,"绑定失败");
                        }
                    }else{
                        toast("人脸质量不合格");
                    }
                }catch (Exception e){
                    Log.i(TAG,e.getMessage());
                }finally {
                    //  删除图片
                    //  file.delete();
                }


                //  这时候直接用上面的mUri就可以了
                /*Bitmap bitmap = BitmapFactory.decodeFile(file.toString());

                try {
                    //  离线模式下，注册人脸后返回结果。
                    FacePassAddFaceResult result = mFacePassHandler.addFace(bitmap);

                    //  FacePassExtractFeatureResult facePassExtractFeatureResult = mFacePassHandler.extractFeature(bitmap);
                    //  质量判断 facePassExtractFeatureResult.result

                    mFacePassHandler.extractFeature()

                    if (result != null && result.faceToken != null) {

                        nowFaceToken = new String(result.faceToken);

                        if (result.result == 0) {
                            //  绑定人脸
                            boolean b = mFacePassHandler.bindGroup(group_name, result.faceToken);

                            if(b){
                                toast("添加人脸成功");

                                if(!isHasShowDialog()){
                                    showQRCodeDialog();
                                }
                            }else{
                                toast("绑定人脸失败");
                            }

                        } else if (result.result == 1) {
                            toast("没有拍到人脸");
                        } else {
                            toast("人脸照片不合格");
                        }
                    }else{
                        toast("人脸照片不合格");
                    }


                } catch (FacePassException e) {
                    e.printStackTrace();
                    toast(e.getMessage());
                }*/

                break;
            case INTENT_ADVERTISING_CODE:
                //  从广告界面返回来，重新30s自动跳转
                lastFaceTime = System.currentTimeMillis();

                autoIntentAdvertising = true;

                break;
        }
    }


    /**
     * 插入base64形式的特征值
     * */
    private void insertFeatureBaseString(byte[] featureData,long userId){


        try {

            FacePassFeatureAppendInfo facePassFeatureAppendInfo = new FacePassFeatureAppendInfo();

            //  创建 faceToken
            String faceToken = mFacePassHandler.insertFeature(featureData,facePassFeatureAppendInfo);

            boolean bindResult = mFacePassHandler.bindGroup(group_name, nowFaceToken.getBytes());


        }catch (Exception e){

        }

        //  绑定结果
        /*if(bindResult){
            vxLogin(qrCodeDialog,userId,,faceToken);
        }else{
            toast("绑定失败");
        }*/



        /*String string = Base64.encodeToString(facePassExtractFeatureResult.featureData, Base64.DEFAULT);

        Log.i(PUSH,"base64长度："+string.length());

        byte [] byteArray = Base64.decode(string, Base64.DEFAULT);

        boolean result = Arrays.equals(facePassExtractFeatureResult.featureData,byteArray);

        if(result){
            Log.i(PUSH,"相等");
        }else{
            Log.i(PUSH,"不相等");
        }*/
    }


    /**
     *
     * 初始化数据库
     * */
    private UserMessageDao initDataBase(){
        //  创建数据库loginRecord.db
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(MainActivity.this,"loginRecord.db");
        //  获取可写数据库
        SQLiteDatabase database = helper.getWritableDatabase();
        //  获取数据库对象
        DaoMaster daoMaster = new DaoMaster(database);
        //  获取Dao对象管理者
        DaoSession mDaoSession = daoMaster.newSession();

        return mDaoSession.getUserMessageDao();
    }


    /**
     * 显示扫描二维码登陆弹窗
     * */
    private AlertDialog qrCodeDialog;
    private void showQRCodeDialog(){

        if(qrCodeDialog != null && qrCodeDialog.isShowing()){
            return;
        }


        if(isHasShowDialog()){
            return;
        }


        VoiceUtil.getInstance(MainActivity.this).startAuto("请用微信扫描二维码");

        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setCancelable(false);
        View view = View.inflate(MainActivity.this,R.layout.qr_code_layout,null);
        ImageView qr_code_login = (ImageView)view.findViewById(R.id.iv_qr_code_login);

        //  获取安卓设备唯一标识符
        String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //  添加时间戳
        loginToken =  androidID;
        //  生成token 二维码


        Log.i(PUSH,loginToken);

        //  拼接地址 传递 token
        qr_code_login.setImageBitmap(QRCodeUtil.getAppletLoginCode(ServerAddress.LOGIN + APP.getDeviceId()));

        Log.i(NOW_TAG,"二维码显示的内容 : " + ServerAddress.LOGIN + androidID);


        //  暂时添加一个点击事件，模拟扫码成功，并通过TCP连接返回了用户id和token
        qr_code_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nowFaceToken != null){
                    //  vxLogin(qrCodeDialog,System.currentTimeMillis() / 1000 , loginToken,nowFaceToken);
                }
            }
        });

        alert.setView(view);


        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                alert.create();
                qrCodeDialog = alert.show();
            }
        });

    }




    /**
     *
     * 扫码后发现该账户下有人脸，但是本地没有，下载图片到本地库，并将 人脸 和 用户 id 进行绑定
     * @param url 图片所在的服务器地址地址
     * @param userId 需要绑定的用户id
     * */
    private void downloadFaceImageAndBind(String url,final int userId) {

        String path = getFilesDir() + File.separator + "images" ;

        String fileName = System.currentTimeMillis() + ".jpg";


        DownloadUtil.get().download(url, path, fileName, new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(File file) {
                        //  下载完毕添加并绑定人脸
                        addFaceAndBind(file,userId);
                    }

                    @Override
                    public void onDownloading(int progress) {

                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        toast("绑定人脸失败，请联系管理员");
                    }
                });
    }


    private final String NOW_TAG = "人脸注册";

    /**
     * 扫码后发现没有录入人脸，则将人脸上传到云服务器
     * @param userId 用户id
     * @param file 人脸图片
     * */
    public void uploadFace(final File file,final byte[] feature,final long userId){
        progressDialog.show();

        OkHttpClient client = new OkHttpClient.Builder().build();

        // 设置文件以及文件上传类型封装
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpg"), file);


        final long nowTime = System.currentTimeMillis() / 1000 ;

        final String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // 文件上传的请求体封装
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //.addFormDataPart("user_id",String.valueOf(userId))
                //.addFormDataPart("featrue", Base64.encodeToString(feature,Base64.DEFAULT))
                .addFormDataPart("file", file.getName(), requestBody)

                //.addFormDataPart("sign",md5(androidID + nowTime + key).toUpperCase())
                //.addFormDataPart("device_id",androidID)
                //.addFormDataPart("timestamp",String.valueOf(nowTime))
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(ServerAddress.FILE_UPLOAD)
                .post(multipartBody)
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Log.i(NOW_TAG,"图片上传失败：" + e.getMessage() );


                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        //  如果存在二维码弹窗，则关闭
                        if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                            qrCodeDialog.dismiss();
                        }

                        if(progressDialog != null && progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }

                        AlertDialog.Builder alertB = new AlertDialog.Builder(MainActivity.this);
                        alertB.setCancelable(false);
                        alertB.setTitle("提示");
                        alertB.setMessage("人脸注册失败");
                        alertB.setPositiveButton("重试", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                uploadFace(file,feature,userId);

                            }
                        });
                        alertB.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alertB.create();
                        alertB.show();


                    }
                });
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                ImageUploadResult imageUploadResult = new Gson().fromJson(response.body().string(),ImageUploadResult.class);

                Log.i(NOW_TAG,"图片上传结果：" + imageUploadResult.toString());


                //  图片上传成功
                if(imageUploadResult.getCode() == 1){
                    Map<String,String> map = new HashMap<>();
                    map.put("user_id",String.valueOf(userId));
                    map.put("featrue",Base64.encodeToString(feature,Base64.DEFAULT));
                    map.put("face_image",imageUploadResult.getData());
                    map.put("sign",md5(androidID + nowTime + key).toUpperCase());
                    //map.put("device_id",androidID);
                    map.put("timestamp",String.valueOf(nowTime));
                    NetWorkUtil.getInstance().doPost(ServerAddress.FACE_AND_USER_ID_UPLOAD, map, new NetWorkUtil.NetWorkListener() {
                        @Override
                        public void success(String response) {
                            Log.i(NOW_TAG,"人脸注册 json 结果："+response);


                            ResultMould resultMould = new Gson().fromJson(response,ResultMould.class);
                            //  人脸注册成功
                            if(resultMould.getCode() == 1){

                                Log.i(NOW_TAG,"开始本地 userToken 和 userID 绑定");

                                //  本地实现
                                DataBaseUtil.getInstance(MainActivity.this).insertUserIdAndFaceToken(app.getUserId(),nowFaceToken);


                                Log.i(NOW_TAG,app.getUserId() + "绑定 " + nowFaceToken);

                                file.delete();

                                //  如果二维码扫码弹窗显示 则 隐藏
                                if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                                    qrCodeDialog.dismiss();
                                }


                                if(progressDialog != null && progressDialog.isShowing()){
                                    progressDialog.dismiss();
                                }


                                Intent intent = new Intent(MainActivity.this,ControlActivity.class);
                                intent.putExtra("userId",app.getUserId());
                                startActivity(intent);


                            }
                        }

                        @Override
                        public void fail(Call call, IOException e) {
                            Log.i(NOW_TAG,"人脸注册失败 fail：" + e.getMessage());

                            if(progressDialog != null && progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                        }

                        @Override
                        public void error(Exception e) {
                            Log.i(NOW_TAG,"人脸注册失败error ：" + e.getMessage());

                            if(progressDialog != null && progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                        }
                    });
                }




                //  DataBaseUtil.getInstance(MainActivity.this).insertUserIdAndFaceToken(app.getUserId(),nowFaceToken);


                //Log.i(PUSH,"图片上传结果：" + response.body().string().substring(7000));
                //Log.i("结果",response.body().string());

            }
        });
    }



    ProgressDialog progressDialog ;

    public void userRegisterFace(){
        Map<String,String> map = new HashMap<>();
        map.put("user_id","");
        map.put("featrue","");
        map.put("face_image","");
        map.put("sign","");
        map.put("device_id","");
        map.put("timestamp","");
        NetWorkUtil.getInstance().doPost(ServerAddress.FACE_AND_USER_ID_UPLOAD, map, new NetWorkUtil.NetWorkListener() {
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




    /**
     * 添加人脸 并绑定 用户账户
     * @param imageFile 人脸图片
     * @param userId 用户 id
     * */
    private void addFaceAndBind(File imageFile,int userId){

        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.toString());

        try {
            //  离线模式下，注册人脸后返回结果。
            FacePassAddFaceResult result = mFacePassHandler.addFace(bitmap);

            if (result != null) {

                nowFaceToken = new String(result.faceToken);

                if (result.result == 0) {
                    //  绑定人脸
                    boolean bindResult = mFacePassHandler.bindGroup(group_name, result.faceToken);

                    //  绑定结果
                    if(bindResult){
                        UserMessage userMessage = new UserMessage();
                        //  对应底库中的faceToken
                        userMessage.setFaceToken(nowFaceToken);
                        //  上次使用时间
                        userMessage.setLastUsedTime(System.currentTimeMillis());
                        //  在数据库中的注册时间
                        userMessage.setRegisterTime(System.currentTimeMillis());
                        //  使用该次数
                        userMessage.setUsedNumber(1);
                        //  设置服务器传过来的用户id
                        userMessage.setUserId(userId);
                        //  插入到数据库
                        userMessageDao.insert(userMessage);
                    }else{
                        toast("绑定人脸失败");
                    }

                } else if (result.result == 1) {
                    toast("没有拍到人脸");
                } else {
                    toast("人脸照片不合格");
                }
            }


        } catch (FacePassException e) {
            e.printStackTrace();
            toast(e.getMessage());
        }

    }





    /**
     *
     * 微信小程序扫描二维码 > 小程序通知服务器用户登陆 > 登陆成功 > 服务器通过 TCP 传输json给安卓客户端 > 客户端接收 > 如果json内容属于登陆相关 > 交由此方法处理
     *
     * @param alertDialog 显示二维码登陆的那个弹窗
     * @param userId 从服务器传过来 用户id
     * @param token 从服务器传过来的 token ,也就是二维码内容
     * @param faceToken 人脸底库主键
     * */
    private void vxLogin(AlertDialog alertDialog,long userId,String token,String faceToken){

        //  如果之前生成的二维码token 不为空 ，而且和服务器传回的token匹配
        if(this.loginToken != null && this.loginToken.equals(token)){

            //  关闭 扫描二维码登陆弹窗
            if(alertDialog != null && alertDialog.isShowing()){
                alertDialog.dismiss();
            }

            //  查询数据库中是否有登陆记录
            UserMessage userMessage = userMessageDao.queryBuilder().where(UserMessageDao.Properties.UserId.eq(userId)).build().unique();

            if(userMessage != null){
                //  说明存在该记录，则使用次数添加 + 1，这一步骤主要是为了以后清理使用次数较少的人脸

                //  使用次数 + 1
                userMessage.setUsedNumber(userMessage.getUsedNumber() + 1);
                //  上次使用时间
                userMessage.setLastUsedTime(System.currentTimeMillis());
                //  插入 或者 替换 到数据库
                userMessageDao.insertOrReplace(userMessage);
            }else{
                // 在这台安卓机上用户还没有登陆过，则添加一条新的记录

                userMessage = new UserMessage();
                //  对应底库中的faceToken
                userMessage.setFaceToken(faceToken);
                //  上次使用时间
                userMessage.setLastUsedTime(System.currentTimeMillis());
                //  在数据库中的注册时间
                userMessage.setRegisterTime(System.currentTimeMillis());
                //  使用该次数
                userMessage.setUsedNumber(1);
                //  设置服务器传过来的用户id
                userMessage.setUserId(userId);
                //  插入到数据库
                userMessageDao.insert(userMessage);

            }




            //  关闭二维码登陆弹窗
            if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                qrCodeDialog.dismiss();
            }


            if(!isHasShowDialog()){
                //  跳转到垃圾箱控制台
                Intent intent = new Intent(MainActivity.this,ControlActivity.class);
                intent.putExtra("userId",userMessage.getUserId());
                startActivity(intent);
            }

        }
    }



    /**
     * 查询当前人脸的faceToken 在数据库中是否对应某个从服务器传过来的 userId，如果对应，则不需要微信扫码登陆，直接进入控制台界面
     * @param faceToken 人脸识别扫描回调的 faceToken
     * */
    private void queryFaceToken(String faceToken){
        UserMessage userMessage = userMessageDao.queryBuilder().where(UserMessageDao.Properties.FaceToken.eq(faceToken)).build().unique();

        //  如果人脸底库中存在该人脸，且有登陆记录，则直接进入垃圾控制台
        if(userMessage != null){

            //  关闭二维码登陆弹窗
            if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                qrCodeDialog.dismiss();
            }

            app.setUserId(userMessage.getUserId());

            if(!isHasShowDialog()){
                //  跳转到垃圾箱控制台
                Intent intent = new Intent(MainActivity.this,ControlActivity.class);
                intent.putExtra("userId",userMessage.getUserId());
                startActivity(intent);
            }

        }else{
            //  如果人脸底库中存在该人脸，但是没有使用微信登陆过，则显示 扫描二维码弹窗

            if(!isHasShowDialog()){
                Log.i(NOW_TAG, "验证失败，人脸底库中存在该人脸，但是没有使用微信登陆过");

                List<UserMessage> list = userMessageDao.queryBuilder().list();
                for(UserMessage um : list){
                    Log.i(NOW_TAG, "人脸id数据库：" + um.toString());
                }

                showQRCodeDialog();
            }

        }
    }


    /**
     * 如果当前界面已经有弹窗显示了
     * */
    private boolean isHasShowDialog(){

        //  人脸验证失败,显示添加人脸弹窗
        if(alertDialog!=null && alertDialog.isShowing()){
            return true;
        }

        //  demo 添加人脸弹窗
        if (mFaceOperationDialog != null && mFaceOperationDialog.isShowing()) {
            return true;
        }

        //  二维码
        if(qrCodeDialog != null && qrCodeDialog.isShowing()){
            return true;
        }

        //  管理员登陆
        if(adminLoginDialog != null && adminLoginDialog.isShowing()){
            return true;
        }


        if(progressDialog != null && progressDialog.isShowing()){
            return true;
        }

        return false;
    }


    /**
     * 验证失败,显示添加人脸弹窗
     * */
    private AlertDialog alertDialog;
    //  相机
    private final static int REQUEST_CODE_CAMERA = 500;
    //  H5 唤起相机 拍照回调此路径
    private Uri mUri;
    //  图片文件
    private File file;
    private void showVerifyFail(){
        AlertDialog.Builder alertB = new AlertDialog.Builder(MainActivity.this);
        alertB.setCancelable(false);
        alertB.setTitle("提示");
        alertB.setMessage("需要进行人脸注册");
        alertB.setPositiveButton("人脸注册", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //  打开相机拍照
                //  步骤一：创建存储照片的文件
                String path = getFilesDir() + File.separator + "images" + File.separator;
                file = new File(path, System.currentTimeMillis() + ".jpg");
                if(!file.getParentFile().exists())
                    file.getParentFile().mkdirs();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //  步骤二：Android 7.0及以上获取文件 Uri
                    mUri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", file);
                } else {
                    //  步骤三：获取文件Uri
                    mUri = Uri.fromFile(file);
                }
                //  步骤四：调取系统拍照
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);

            }
        });
        alertB.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                app.setUserId(0);

                dialog.dismiss();
            }
        });
        alertB.create();
        alertDialog = alertB.show();
    }




    /**
     *
     * 根据faceToken Toast出一个人脸
     * @param faceToken 唯一标识符
     * @param trackId 单次人脸识别的ID数
     * */
    private void getFaceImageByFaceToken(final long trackId, String faceToken) {
        if (TextUtils.isEmpty(faceToken)) {
            return;
        }

        final String faceUrl = "http://" + serverIP + ":8080/api/image/v1/query?face_token=" + faceToken;

        final Bitmap cacheBmp = mImageCache.getBitmap(faceUrl);
        if (cacheBmp != null) {
            mAndroidHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(DEBUG_TAG, "getFaceImageByFaceToken cache not null");
                    showToast("ID = " + String.valueOf(trackId), Toast.LENGTH_SHORT, true, cacheBmp);
                }
            });
            return;
        } else {
            try {
                final Bitmap bitmap = mFacePassHandler.getFaceImage(faceToken.getBytes());
                mAndroidHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(DEBUG_TAG, "getFaceImageByFaceToken cache is null");
                        showToast("ID = " + String.valueOf(trackId), Toast.LENGTH_SHORT, true, bitmap);
                    }
                });
                if (bitmap != null) {
                    return;
                }
            } catch (FacePassException e) {
                e.printStackTrace();
            }
        }

        //  缓存
        ByteRequest request = new ByteRequest(Request.Method.GET, faceUrl, new Response.Listener<byte[]>() {
            @Override
            public void onResponse(byte[] response) {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeByteArray(response, 0, response.length, options);
                mImageCache.putBitmap(faceUrl, bitmap);
                showToast("ID = " + String.valueOf(trackId), Toast.LENGTH_SHORT, true, bitmap);
                Log.i(DEBUG_TAG, "getFaceImageByFaceToken response ");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(DEBUG_TAG, "image load failed ! ");
            }
        });
        request.setTag("load_image_request_tag");
        requestQueue.add(request);
    }


    /* 显示同步底库弹窗 */
    private void showSyncGroupDialog() {

        if (mSyncGroupDialog != null && mSyncGroupDialog.isShowing()) {
            mSyncGroupDialog.hide();
            requestQueue.cancelAll("handle_sync_request_tag");
        }

        //  创建弹窗
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //  绑定布局
        View view = LayoutInflater.from(this).inflate(R.layout.layout_dialog_sync_groups, null);
        final EditText groupNameEt = (EditText) view.findViewById(R.id.et_group_name);
        final TextView syncDataTv = (TextView) view.findViewById(R.id.tv_show_sync_data);
        Button obtainGroupsBtn = (Button) view.findViewById(R.id.btn_obtain_groups);
        Button createGroupBtn = (Button) view.findViewById(R.id.btn_submit);
        ImageView closeWindowIv = (ImageView) view.findViewById(R.id.iv_close);
        final Button handleSyncDataBtn = (Button) view.findViewById(R.id.btn_handle_sync_data);
        final ListView groupNameLv = (ListView) view.findViewById(R.id.lv_group_name);
        final ScrollView syncScrollView = (ScrollView) view.findViewById(R.id.sv_handle_sync_data);

        final GroupNameAdapter groupNameAdapter = new GroupNameAdapter();
        builder.setView(view);

        //  关闭当前弹窗按钮
        closeWindowIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSyncGroupDialog.dismiss();
            }
        });

        //  获取当前所有底库
        obtainGroupsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }
                String[] groups = mFacePassHandler.getLocalGroups();
                if (groups != null && groups.length > 0) {
                    List<String> data = Arrays.asList(groups);
                    syncScrollView.setVisibility(View.GONE);
                    groupNameLv.setVisibility(View.VISIBLE);
                    groupNameAdapter.setData(data);
                    groupNameLv.setAdapter(groupNameAdapter);
                } else {
                    toast("groups is null !");
                }
            }
        });

        //  创建底库
        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }
                String groupName = groupNameEt.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    toast("please input group name ！");
                    return;
                }
                boolean isSuccess = false;
                try {
                    isSuccess = mFacePassHandler.createLocalGroup(groupName);
                } catch (FacePassException e) {
                    e.printStackTrace();
                }
                toast("create group " + isSuccess);
                if (isSuccess && group_name.equals(groupName)) {
                    isLocalGroupExist = true;
                }

            }
        });

        //  同步底库按钮，从服务器下载底库 (这个按钮默认是不显示的)
        handleSyncDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }
                String requestData = mFacePassHandler.getSyncRequestData();
                getHandleSyncGroupData(requestData);
            }

            private void getHandleSyncGroupData(final String paramsValue) {

                // TODO: 2017/12/6
                ByteRequest request = new ByteRequest(Request.Method.POST, "http://" + serverIP + ":8080/api/service/sync/v1", new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        if (mFacePassHandler == null) {

                            return;
                        }
                        FacePassSyncResult result3 = null;
                        try {
                            result3 = mFacePassHandler.handleSyncResultData(response);
                        } catch (FacePassException e) {
                            e.printStackTrace();
                        }

                        if (result3 == null || result3.facePassGroupSyncDetails == null) {
                            toast("handle sync result is failed!");
                            return;
                        }

                        StringBuilder builder = new StringBuilder();
                        for (FacePassGroupSyncDetail detail : result3.facePassGroupSyncDetails) {
                            builder.append("========" + detail.groupName + "==========" + "\r\n");
                            builder.append("groupName :" + detail.groupName + " \r\n");
                            builder.append("facetokenadded :" + detail.faceAdded + " \r\n");
                            builder.append("facetokendeleted :" + detail.faceDeleted + " \r\n");
                            builder.append("resultcode :" + detail.result + " \r\n");
                        }
                        syncDataTv.setText(builder);
                        syncScrollView.setVisibility(View.VISIBLE);
                        groupNameLv.setVisibility(View.GONE);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
                    @Override
                    public byte[] getBody() throws AuthFailureError {

                        return paramsValue.getBytes();
                    }
                };
                request.setTag("handle_sync_request_tag");
                requestQueue.add(request);
            }
        });

        //  点击删除按钮，删除底库
        groupNameAdapter.setOnItemDeleteButtonClickListener(new GroupNameAdapter.ItemDeleteButtonClickListener() {
            @Override
            public void OnItemDeleteButtonClickListener(int position) {
                List<String> groupNames = groupNameAdapter.getData();
                if (groupNames == null) {
                    return;
                }
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }
                String groupName = groupNames.get(position);
                boolean isSuccess = false;
                try {
                    isSuccess = mFacePassHandler.deleteLocalGroup(groupName);
                } catch (FacePassException e) {
                    e.printStackTrace();
                }
                if (isSuccess) {
                    String[] groups = mFacePassHandler.getLocalGroups();
                    if (group_name.equals(groupName)) {
                        isLocalGroupExist = false;
                    }
                    if (groups != null) {
                        groupNameAdapter.setData(Arrays.asList(groups));
                        groupNameAdapter.notifyDataSetChanged();
                    }
                    toast("删除成功!");
                } else {
                    toast("删除失败!");

                }
            }

        });


        //  创建并显示
        mSyncGroupDialog = builder.create();
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
        WindowManager.LayoutParams attributes = mSyncGroupDialog.getWindow().getAttributes();
        attributes.height = d.getHeight();
        attributes.width = d.getWidth();
        mSyncGroupDialog.getWindow().setAttributes(attributes);
        mSyncGroupDialog.show();

    }

    private AlertDialog mFaceOperationDialog;

    //  显示添加人脸弹窗
    private void showAddFaceDialog() {
        //  做一个简单的判空
        if (mFaceOperationDialog != null && !mFaceOperationDialog.isShowing()) {
            mFaceOperationDialog.show();
            return;
        }
        if (mFaceOperationDialog != null && mFaceOperationDialog.isShowing()) {
            return;
        }


        //  初始化弹窗
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 布局
        View view = LayoutInflater.from(this).inflate(R.layout.layout_dialog_face_operation, null);
        builder.setView(view);
        final EditText faceImagePathEt = (EditText) view.findViewById(R.id.et_face_image_path);
        final EditText faceTokenEt = (EditText) view.findViewById(R.id.et_face_token);
        final EditText groupNameEt = (EditText) view.findViewById(R.id.et_group_name);
        Button choosePictureBtn = (Button) view.findViewById(R.id.btn_choose_picture);
        Button addFaceBtn = (Button) view.findViewById(R.id.btn_add_face);
        Button getFaceImageBtn = (Button) view.findViewById(R.id.btn_get_face_image);
        Button deleteFaceBtn = (Button) view.findViewById(R.id.btn_delete_face);
        Button bindGroupFaceTokenBtn = (Button) view.findViewById(R.id.btn_bind_group);
        Button getGroupInfoBtn = (Button) view.findViewById(R.id.btn_get_group_info);
        ImageView closeIv = (ImageView) view.findViewById(R.id.iv_close);
        final ListView groupInfoLv = (ListView) view.findViewById(R.id.lv_group_info);

        //  设置适配器
        final FaceTokenAdapter faceTokenAdapter = new FaceTokenAdapter();

        //  默认底库是facepass
        groupNameEt.setText(group_name);

        //  点击 x 关闭弹窗
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFaceOperationDialog.dismiss();
            }
        });

        //  选择图片
        choosePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentFromGallery = new Intent(Intent.ACTION_GET_CONTENT);
                intentFromGallery.setType("image/*"); // 设置文件类型
                intentFromGallery.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(intentFromGallery, REQUEST_CODE_CHOOSE_PICK);
                } catch (ActivityNotFoundException e) {
                    toast("请安装相册或者文件管理器");
                }
            }
        });

        //  点击添加人脸
        addFaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }
                String imagePath = faceImagePathEt.getText().toString();
                if (TextUtils.isEmpty(imagePath)) {
                    toast("请输入正确的图片路径！");
                    return;
                }

                File imageFile = new File(imagePath);
                if (!imageFile.exists()) {
                    toast("图片不存在 ！");
                    return;
                }

                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

                try {
                    //  离线模式下，注册人脸后返回结果。
                    FacePassAddFaceResult result = mFacePassHandler.addFace(bitmap);
                    if (result != null) {
                        //  人脸图片添加成功
                        if (result.result == 0) {
                            toast("add face successfully！");
                            faceTokenEt.setText(new String(result.faceToken));

                            Log.i("结果",new String(result.faceToken));

                            //  图片里面没有扫描到人脸
                        } else if (result.result == 1) {
                            toast("no face ！");

                            //  图片质量不合格
                        } else {
                            toast("quality problem！");
                        }
                    }
                } catch (FacePassException e) {
                    e.printStackTrace();
                    toast(e.getMessage());
                }
            }
        });


        //  获取图片按钮
        getFaceImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }
                try {
                    //  根据 faceToken 获取图片
                    byte[] faceToken = faceTokenEt.getText().toString().getBytes();
                    Bitmap bmp = mFacePassHandler.getFaceImage(faceToken);
                    final ImageView iv = (ImageView) findViewById(R.id.imview);
                    iv.setImageBitmap(bmp);
                    //  显示该张图片
                    iv.setVisibility(View.VISIBLE);
                    //  显示该张图片 2s 后隐藏
                    iv.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            iv.setVisibility(View.GONE);
                            iv.setImageBitmap(null);
                        }
                    }, 2000);
                    mFaceOperationDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                    toast(e.getMessage());
                }
            }
        });

        //  根据faceToken 删除人脸
        deleteFaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }
                boolean b = false;
                try {
                    byte[] faceToken = faceTokenEt.getText().toString().getBytes();
                    b = mFacePassHandler.deleteFace(faceToken);
                    if (b) {
                        String groupName = groupNameEt.getText().toString();
                        if (TextUtils.isEmpty(groupName)) {
                            toast("group name  is null ！");
                            return;
                        }
                        //  遍历下面所以faceToken 然后重新显示到listView 列表
                        byte[][] faceTokens = mFacePassHandler.getLocalGroupInfo(groupName);
                        List<String> faceTokenList = new ArrayList<>();
                        if (faceTokens != null && faceTokens.length > 0) {
                            for (int j = 0; j < faceTokens.length; j++) {
                                if (faceTokens[j].length > 0) {
                                    faceTokenList.add(new String(faceTokens[j]));
                                }
                            }

                        }
                        faceTokenAdapter.setData(faceTokenList);
                        groupInfoLv.setAdapter(faceTokenAdapter);
                    }
                } catch (FacePassException e) {
                    e.printStackTrace();
                   toast(e.getMessage());
                }

                //  显示删除脸成功 或 失败
                String result = b ? "success " : "failed";
                toast("delete face " + result);
                Log.d(DEBUG_TAG, "delete face  " + result);

            }
        });

        //  绑定按钮
        bindGroupFaceTokenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }

                byte[] faceToken = faceTokenEt.getText().toString().getBytes();
                String groupName = groupNameEt.getText().toString();
                if (faceToken == null || faceToken.length == 0 || TextUtils.isEmpty(groupName)) {
                    toast("params error！");
                    return;
                }
                try {
                    boolean b = mFacePassHandler.bindGroup(groupName, faceToken);
                    String result = b ? "success " : "failed";
                    toast("bind  " + result);
                } catch (Exception e) {
                    e.printStackTrace();
                    toast(e.getMessage());
                }


            }
        });

        //  详细信息
        getGroupInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }
                String groupName = groupNameEt.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    toast("group name  is null ！");
                    return;
                }
                try {
                    //  遍历底库所有faceToken
                    byte[][] faceTokens = mFacePassHandler.getLocalGroupInfo(groupName);
                    List<String> faceTokenList = new ArrayList<>();
                    if (faceTokens != null && faceTokens.length > 0) {
                        for (int j = 0; j < faceTokens.length; j++) {
                            if (faceTokens[j].length > 0) {
                                faceTokenList.add(new String(faceTokens[j]));
                            }
                        }

                    }
                    faceTokenAdapter.setData(faceTokenList);
                    groupInfoLv.setAdapter(faceTokenAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                    toast("get local group info error!");
                }

            }
        });

        //  解绑
        faceTokenAdapter.setOnItemButtonClickListener(new FaceTokenAdapter.ItemButtonClickListener() {
            @Override
            public void onItemDeleteButtonClickListener(int position) {
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }

                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }
                String groupName = groupNameEt.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    toast("group name  is null ！");
                    return;
                }
                try {
                    byte[] faceToken = faceTokenAdapter.getData().get(position).getBytes();
                    //  删除某一个faceToken
                    boolean b = mFacePassHandler.deleteFace(faceToken);
                    String result = b ? "success " : "failed";
                    toast("delete face " + result);
                    if (b) {
                        byte[][] faceTokens = mFacePassHandler.getLocalGroupInfo(groupName);
                        List<String> faceTokenList = new ArrayList<>();
                        if (faceTokens != null && faceTokens.length > 0) {
                            for (int j = 0; j < faceTokens.length; j++) {
                                if (faceTokens[j].length > 0) {
                                    faceTokenList.add(new String(faceTokens[j]));
                                }
                            }

                        }
                        //  重新显示列表
                        faceTokenAdapter.setData(faceTokenList);
                        groupInfoLv.setAdapter(faceTokenAdapter);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    toast(e.getMessage());
                }

            }

            @Override
            public void onItemUnbindButtonClickListener(int position) {
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }

                String groupName = groupNameEt.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    toast("group name  is null ！");
                    return;
                }
                try {
                    byte[] faceToken = faceTokenAdapter.getData().get(position).getBytes();
                    boolean b = mFacePassHandler.unBindGroup(groupName, faceToken);
                    String result = b ? "success " : "failed";
                    toast("unbind " + result);
                    if (b) {
                        byte[][] faceTokens = mFacePassHandler.getLocalGroupInfo(groupName);
                        List<String> faceTokenList = new ArrayList<>();
                        if (faceTokens != null && faceTokens.length > 0) {
                            for (int j = 0; j < faceTokens.length; j++) {
                                if (faceTokens[j].length > 0) {
                                    faceTokenList.add(new String(faceTokens[j]));
                                }
                            }

                        }
                        faceTokenAdapter.setData(faceTokenList);
                        groupInfoLv.setAdapter(faceTokenAdapter);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    toast("unbind error!");
                }

            }
        });


        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
        mFaceOperationDialog = builder.create();
        WindowManager.LayoutParams attributes = mFaceOperationDialog.getWindow().getAttributes();
        attributes.height = d.getHeight();
        attributes.width = d.getWidth();
        mFaceOperationDialog.getWindow().setAttributes(attributes);
        mFaceOperationDialog.show();
    }





    private void toast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


    /**
     * 最近最少使用算法 容器 图片缓存
     */
    private static class FaceImageCache implements ImageLoader.ImageCache {

        private static final int CACHE_SIZE = 6 * 1024 * 1024;

        LruCache<String, Bitmap> mCache;

        public FaceImageCache() {
            mCache = new LruCache<String, Bitmap>(CACHE_SIZE) {

                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getRowBytes() * value.getHeight();
                }
            };
        }

        @Override
        public Bitmap getBitmap(String url) {
            return mCache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            mCache.put(url, bitmap);
        }
    }



    /**
     * 联机的人脸识别
     * */
    private class FacePassRequest extends Request<String> {

        HttpEntity entity;

        FacePassDetectionResult mFacePassDetectionResult;
        private Response.Listener<String> mListener;

        public FacePassRequest(String url, FacePassDetectionResult detectionResult, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(Method.POST, url, errorListener);
            mFacePassDetectionResult = detectionResult;
            mListener = listener;
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            String parsed;
            try {
                parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            } catch (UnsupportedEncodingException e) {
                parsed = new String(response.data);
            }
            return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
        }

        @Override
        protected void deliverResponse(String response) {
            mListener.onResponse(response);
        }

        @Override
        public String getBodyContentType() {
            return entity.getContentType().getValue();
        }

        @Override
        public byte[] getBody() throws AuthFailureError {
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            //        beginRecogIdArrayList.clear();

            for (FacePassImage passImage : mFacePassDetectionResult.images) {
                /* 将人脸图转成jpg格式图片用来上传 */
                YuvImage img = new YuvImage(passImage.image, ImageFormat.NV21, passImage.width, passImage.height, null);
                Rect rect = new Rect(0, 0, passImage.width, passImage.height);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                img.compressToJpeg(rect, 95, os);
                byte[] tmp = os.toByteArray();
                ByteArrayBody bab = new ByteArrayBody(tmp, String.valueOf(passImage.trackId) + ".jpg");
                //            beginRecogIdArrayList.add(passImage.trackId);
                entityBuilder.addPart("image_" + String.valueOf(passImage.trackId), bab);
            }
            StringBody sbody = null;
            try {
                sbody = new StringBody(MainActivity.group_name, ContentType.TEXT_PLAIN.withCharset(CharsetUtils.get("UTF-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            entityBuilder.addPart("group_name", sbody);
            StringBody data = null;
            try {
                data = new StringBody(new String(mFacePassDetectionResult.message), ContentType.TEXT_PLAIN.withCharset(CharsetUtils.get("UTF-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            entityBuilder.addPart("face_data", data);
            entity = entityBuilder.build();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                entity.writeTo(bos);
            } catch (IOException e) {
                VolleyLog.e("IOException writing to ByteArrayOutputStream");
            }
            byte[] result = bos.toByteArray();
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }
    }




}
