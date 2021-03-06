package megvii.testfacepass;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.support.design.widget.TabLayout;
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
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
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
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.littlegreens.netty.client.listener.NettyClientListener;
import com.serialportlibrary.util.ByteStringUtil;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import mcv.facepass.FacePassException;
import mcv.facepass.FacePassHandler;
import mcv.facepass.types.FacePassAddFaceResult;
import mcv.facepass.types.FacePassAgeGenderResult;
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
import mcv.facepass.types.FacePassRecognitionResultType;
import mcv.facepass.types.FacePassSyncResult;
import megvii.testfacepass.adapter.FaceTokenAdapter;
import megvii.testfacepass.adapter.GroupNameAdapter;
import megvii.testfacepass.camera.CameraManager;
import megvii.testfacepass.camera.CameraPreview;
import megvii.testfacepass.camera.CameraPreviewData;
import megvii.testfacepass.independent.ResidentService;
import megvii.testfacepass.independent.ServerAddress;
import megvii.testfacepass.independent.UploadImageService;
import megvii.testfacepass.independent.bean.BinsWorkTimeBean;
import megvii.testfacepass.independent.bean.BuySuccessMsg;
import megvii.testfacepass.independent.bean.DaoMaster;
import megvii.testfacepass.independent.bean.DaoSession;
import megvii.testfacepass.independent.bean.DustbinConfig;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.bean.GQrReturnBean;
import megvii.testfacepass.independent.bean.GetDustbinConfig;
import megvii.testfacepass.independent.bean.GetNfcUserBean;
import megvii.testfacepass.independent.bean.ICCard;
import megvii.testfacepass.independent.bean.ImageUploadResult;
import megvii.testfacepass.independent.bean.NearByFeatrueSendBean;
import megvii.testfacepass.independent.bean.NfcActivityBean;
import megvii.testfacepass.independent.bean.PhoneLoginBean;
import megvii.testfacepass.independent.bean.ResultMould;
import megvii.testfacepass.independent.bean.ScanLoginBean;
import megvii.testfacepass.independent.bean.TCPVerify;
import megvii.testfacepass.independent.bean.TCPVerifyResponse;
import megvii.testfacepass.independent.bean.UserMessage;
import megvii.testfacepass.independent.bean.UserMessageDao;
import megvii.testfacepass.independent.bean.VXLoginCall;
import megvii.testfacepass.independent.manage.SerialPortRequestByteManage;
import megvii.testfacepass.independent.util.BinsWorkTimeUntil;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.DustbinUtil;
import megvii.testfacepass.independent.util.NetWorkUtil;
import megvii.testfacepass.independent.util.QRCodeUtil;
import megvii.testfacepass.independent.util.SerialPortUtil;
import megvii.testfacepass.independent.util.TCPConnectUtil;
import megvii.testfacepass.independent.util.VoiceUtil;
import megvii.testfacepass.independent.view.AdminLoginDialog;
import megvii.testfacepass.independent.view.CustomNumKeyView;
import megvii.testfacepass.independent.view.PhoneLoginDialog;
import megvii.testfacepass.network.ByteRequest;
import megvii.testfacepass.utils.AndroidDeviceSDK;
import megvii.testfacepass.utils.DataCleanManager;
import megvii.testfacepass.utils.DownloadUtil;
import megvii.testfacepass.utils.FileUtil;
import megvii.testfacepass.utils.LogUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;


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
    private String[] Permission = new String[]{PERMISSION_CAMERA, PERMISSION_WRITE_STORAGE, PERMISSION_READ_STORAGE, PERMISSION_INTERNET, PERMISSION_ACCESS_NETWORK_STATE,ACCESS_COARSE_LOCATION};


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
    private UserMessageDao userMessageDao;
    private AdminLoginDialog adminLoginDialog;

    //  调试模式
    private boolean debug = false;
    private APP app ;
    private Handler mainHandler;
    private Gson gson = new Gson();
    //  从控制台返回的值
    public final static int CONTROL_RESULT_CODE = 300;

    private ImageView float_qrcode_image;

    BinsWorkTimeBean binsWorkTimeBean;

    /**
     * 与 IC 卡相关
     *
     * 事件总线经常无效，所以使用广播发送与接收
     * */
    //  超级管理员卡2
    public final static String ROOT_CARD2 = "A8000000000500C6D4912BADA9";
    //  超级管理员卡1
    public final static String ROOT_CARD = "A80000000005003030A741E3A9";
    private AlertDialog offLinAdminAlertDialog;
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                String icCardContent = intent.getStringExtra("content");
                if(icCardContent != null){

                    //  特殊的管理员卡
                    if(icCardContent.equals(ROOT_CARD) || icCardContent.equals(ROOT_CARD2)){

                        if(offLinAdminAlertDialog != null && offLinAdminAlertDialog.isShowing()){
                            return;
                        }

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                String[] functionArray = new String[]{
                                        "进入投递界面 ( 占桶员 ) ",
                                        "重新建立TCP连接",
                                        "进入调试界面",
                                        "重启设备",
                                        "显示状态栏",
                                        "关闭前台监听",
                                        "更新垃圾箱配置",
                                        "清除设备在服务器的人脸绑定",
                                        "重新校准"
                                };
                                AlertDialog.Builder offLinDialog = new AlertDialog.Builder(MainActivity.this);
                                offLinDialog.setTitle(APP.getDeviceId() + " - 离线管理员");
                                offLinDialog.setCancelable(false);
                                offLinDialog.setItems(functionArray, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case 0:
                                                APP.hasManTime = System.currentTimeMillis();
                                                startActivity(new Intent(MainActivity.this,ControlActivity.class));
                                                break;
                                            case 1:
                                                TCPConnectUtil.getInstance().disconnect();
                                                TCPConnectUtil.getInstance().connect();
                                                break;
                                            case 2:
                                                startActivity(new Intent(MainActivity.this,DebugActivity.class));
                                                break;
                                            case 3:
                                                AndroidDeviceSDK.reBoot(MainActivity.this);
                                                break;
                                            case 4:
                                                AndroidDeviceSDK.hideStatus(MainActivity.this,false);
                                                break;
                                            case 5:
                                                AndroidDeviceSDK.checkForeground(MainActivity.this,false);
                                                break;
                                            case 6:
                                                updateConfig();
                                                break;
                                            case 7:
                                                clearServerFace();
                                                break;
                                            case 8:
                                                startActivity(new Intent(MainActivity.this,WeightCalibrationActivity.class));
                                                break;
                                        }
                                    }
                                });
                                offLinDialog.setPositiveButton("关闭弹窗", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                offLinDialog. create();
                                offLinAdminAlertDialog = offLinDialog.show();
                            }
                        });
                    }else{
                        ICCard icCard = new ICCard(0,icCardContent);
                        icCard(icCard);
                    }
                }
            }
        }
    }


    //  清空服务器下这个设备的人脸绑定
    private void clearServerFace(){
        Map<String,String> hasMap = new HashMap<>();
        hasMap.put("device_id",APP.getDeviceId());
        NetWorkUtil.getInstance().doGet(ServerAddress.CLEAR_DEVICE_FACE_BING, hasMap, new NetWorkUtil.NetWorkListener() {
            @Override
            public void success(String response) {
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();

                DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getUserMessageDao().deleteAll();

                try {
                    mFacePassHandler.clearAllGroupsAndFaces();

                    finish();
                } catch (FacePassException e) {
                    Toast.makeText(MainActivity.this, "本地人脸底库删除失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void fail(Call call, IOException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void error(Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //  更新配置
    private void updateConfig(){

        Map<String,String> map = new HashMap<>();
        map.put("device_id",APP.getDeviceId());
        map.put("mange_code","KDSU9E");
        NetWorkUtil.getInstance().doPost(ServerAddress.GET_DUSTBIN_CONFIG, map, new NetWorkUtil.NetWorkListener() {
            @Override
            public void success(String response) {

                Log.i("配置获取结果",response);

                GetDustbinConfig getDustbinConfig = new Gson().fromJson(response,GetDustbinConfig.class);

                if(getDustbinConfig.getCode() == 1){
                    List<DustbinStateBean> list = new ArrayList<>();

                    List<GetDustbinConfig.DataBean.ListBean> listBeans = getDustbinConfig.getData().getList();
                    for(GetDustbinConfig.DataBean.ListBean listBean : listBeans){

                        //  垃圾箱id   服务器分配
                        long id = listBean.getId();
                        //  门板编号    也就是第几个垃圾箱
                        int number = Integer.parseInt(listBean.getBin_code());
                        //  垃圾箱类型 例如 可回收垃圾、有害垃圾、厨余垃圾
                        String typeString = DustbinUtil.getDustbinType(listBean.getBin_type());
                        //  垃圾箱类型 例如A1 A2 B3 B5 C5 D6 D7 D8
                        String typeNumber = listBean.getBin_type();


                        list.add(new DustbinStateBean(id,number,typeString,typeNumber,0,0,0,0,false,false,false,false,false,false,false,false));
                    }

                    //  保存箱体配置
                    DataBaseUtil.getInstance(MainActivity.this).setDustBinStateConfig(list);


                    /*
                     * 保存垃圾箱配置
                     * */
                    DustbinConfig dustbinConfig = new DustbinConfig();
                    dustbinConfig.setDustbinDeviceId(listBeans.get(0).getDevice_id());  //  deviceID
                    dustbinConfig.setDustbinDeviceName(getDustbinConfig.getData().getDevice_name());    //  deviceName 部署在哪一个小区
                    dustbinConfig.setHasVendingMachine(getDustbinConfig.getData().getHas_amat() == 1);  //  是否有售卖机
                    //  如果存在售卖机则创建售卖机货道
                    if(getDustbinConfig.getData().getHas_amat() == 1){
                        //  不需要创建售卖机
                        //  initReplenishment();
                    }
                    DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getDustbinConfigDao().insertOrReplace(dustbinConfig);    //  保存配置


                    Toast.makeText(MainActivity.this, "更新成功", Toast.LENGTH_SHORT).show();

                    finish();
                }

            }

            @Override
            public void fail(Call call, IOException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void error(Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


    /**
     * 凌晨重启
     * */
    class TimeChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_TIME_TICK:
                    //  每过一分钟 触发
                    Calendar now = Calendar.getInstance();
                    String timeString = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
                    //获取年
                    int year = now.get(Calendar.YEAR);
                    //获取月份，0表示1月份
                    int month = now.get(Calendar.MONTH) + 1;
                    //获取当前天数
                    int day = now.get(Calendar.DAY_OF_MONTH);
                    //获取当前小时
                    int hour = now.get(Calendar.HOUR_OF_DAY);
                    if(timeString.equals("1:10")){
                        //  1 分钟后重启
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AndroidDeviceSDK.reBoot(MainActivity.this);
                            }
                        },1000 * 60);
                    }
                    if(BinsWorkTimeUntil.getBinsWorkTime(binsWorkTimeBean)){
                        Log.i(TAG,"投放时间");
                        for(DustbinStateBean dustbinStateBean: APP.dustbinBeanList){
                            //投放时间，电磁锁开锁
                            //方法名为关闭，是让电磁锁断电开锁
                                Log.i(TAG,"投放时间电子锁未开锁，打开"+dustbinStateBean.getDoorNumber()+"门电子");
                                SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDogHouse(dustbinStateBean.getDoorNumber()));
                        }
                    }else{
                        Log.i(TAG,"非投放时间");
                        //关机前处理操作
                        for(DustbinStateBean dustbinStateBean: APP.dustbinBeanList){
                            SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDoor(dustbinStateBean.getDoorNumber()));
                            //非投放时间，电磁锁上锁
                            LogUtil.d(TAG,dustbinStateBean.getDoorNumber()+"门电子锁上锁");
                            //方法名为开启，是让电磁锁通电上锁
                                SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openDogHouse(dustbinStateBean.getDoorNumber()));
                                LogUtil.writeBusinessLog(dustbinStateBean.getDoorNumber()+"门已上锁");
                        }
                    }

                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageCache = new FaceImageCache();
        mDetectResultQueue = new ArrayBlockingQueue<byte[]>(5);
        mFeedFrameQueue = new ArrayBlockingQueue<CameraPreviewData>(1);
        initAndroidHandler();

        app = (APP) getApplication();
        mainHandler = new Handler(Looper.getMainLooper());



        //  时间变化监听广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);    //  每分钟变化
        registerReceiver(new TimeChangeReceiver(),intentFilter);


        //  注册IC卡广播
        registerReceiver(new MyBroadcastReceiver(),new IntentFilter("icCard"));


        //  请求开放时间
        NetWorkUtil.getInstance().doGetThread(ServerAddress.GET_BINS_WORK_TIME, null, new NetWorkUtil.NetWorkListener() {
            @Override
            public void success(String response) {
                Log.i("投放时间",response);
                binsWorkTimeBean  = gson.fromJson(response,BinsWorkTimeBean.class);
            }

            @Override
            public void fail(Call call, IOException e) {

            }

            @Override
            public void error(Exception e) {

            }
        });



        //  启动APP默认关闭所有门
        closeAllDoor();

        //  设置垃圾箱配置
        DustbinConfig dustbinConfig = DataBaseUtil.getInstance(this).getDaoSession().getDustbinConfigDao().queryBuilder().unique();
        app.setDustbinConfig(dustbinConfig);
        //  代表 全局 垃圾桶 list 对象
        app.setDustbinBeanList(DataBaseUtil.getInstance(MainActivity.this).getDustbinByType(null));

        //  开启读取数据服务，定时器
        startService(new Intent(this,ResidentService.class));
        //  开启图片上传服务
        startService(new Intent(this, UploadImageService.class));

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("加载中...。。");
        progressDialog.create();

        //  开启友盟推送
        //PushAgent.getInstance(this).onAppStart();

        //  注册事件总线
        EventBus.getDefault().register(this);
        //  隐藏状态栏，也就是 app 打开后不能退出
        AndroidDeviceSDK.hideStatus(MainActivity.this,true);
        //  检查是否在前台
        AndroidDeviceSDK.checkForeground(MainActivity.this,true);
        //  凌晨关机重启
        AndroidDeviceSDK.autoReBoot(this,true);
        //  必须在第一次语音播报前 先初始化对象，否则可能出现第一次语音播报无声音的情况
        VoiceUtil.getInstance();

        //  初始化 greenDao 数据库，以及数据库操作对象
        userMessageDao = DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getUserMessageDao();

        //  初始化 TCP 连接，与服务器进行 TCP 通信
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

        initLocationOption();

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


    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        char c = (char) event.getUnicodeChar();
        Log.i("扫码结果","onKeyDown 接收字符：" + c  + "," + event.getUnicodeChar());

        return super.onKeyDown(keyCode, event);
    }

    private static StringBuilder stringBuilder = new StringBuilder();
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        /*InputDevice inputDevice = event.getDevice();
        inputDevice.getMotionRanges();
        //  SM SM-2D PRODUCT HID KBW
        Log.i("扫码抢",inputDevice.getName());*/
        if(event.getUnicodeChar() != 0){
            char c = (char) event.getUnicodeChar();
            Log.i("扫码结果","接收字符：" + c  + "," + event.getUnicodeChar());
        }

        //  重复删除
        /*if(stringBuilder!=null && stringBuilder.length() > 0){
            if(String.valueOf(c).equalsIgnoreCase(stringBuilder.substring(stringBuilder.length()-1))){
                return true;
            }
        }*/




        //  如果手机号输入弹窗存在则不进行处理
        if(phoneLoginDialog != null && phoneLoginDialog.isShowing()){
            return true;
        }else{
            if(event.getAction() == KeyEvent.ACTION_DOWN){
                char pressedKey = (char) event.getUnicodeChar();

                //  数字或者字母才添加
                if(Character.isLetterOrDigit(pressedKey)){
                    stringBuilder.append(pressedKey);
                    Log.i("扫码结果",stringBuilder.toString());
                }
            }

            //  长度 20 才进行下一步判断
            if(stringBuilder.length() == 10){
                String result = stringBuilder.toString();
                if(result.startsWith("a1") && result.endsWith("1a")){

                    Map<String,String> hasMap = new HashMap<>();
                    hasMap.put("user_code",result);
                    NetWorkUtil.getInstance().doPost(ServerAddress.QUERY_QR_CODE, hasMap, new NetWorkUtil.NetWorkListener() {
                        @Override
                        public void success(String response) {
                            ScanLoginBean scanLoginBean = gson.fromJson(response,ScanLoginBean.class);
                            if(scanLoginBean != null && scanLoginBean.getCode() == 1){
                                APP.userId = scanLoginBean.getData().getUser_id();
                                APP.userType = scanLoginBean.getData().getUser_type();
                                goControlActivity();
                            }else{
                                Toast.makeText(MainActivity.this, "没有找到该用户", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void fail(Call call, IOException e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void error(Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    //  清空
                    stringBuilder.delete(0,stringBuilder.length());
                }else{
                    Toast.makeText(this,"数据结构不符合规范",Toast.LENGTH_LONG).show();

                    stringBuilder.delete(0,stringBuilder.length());
                }
            }else{
                //Toast.makeText(this,"数据不符合规范",Toast.LENGTH_LONG).show();

                //stringBuilder.delete(0,stringBuilder.length());
            }

        }


        return super.dispatchKeyEvent(event);
    }



    //  关闭所有门
    private void closeAllDoor(){
        //  启动APP就关闭所有门
        new Thread(){
            @Override
            public void run() {
                super.run();
                for(DustbinStateBean dustbinStateBean:APP.dustbinBeanList){
                    SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDoor(dustbinStateBean.getDoorNumber()));
                    try {
                        Thread.sleep(250);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    //  开排气扇
                    SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openExhaustFan(dustbinStateBean.getDoorNumber()));
                    try {
                        Thread.sleep(250);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
        }.start();
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


    //  友盟推送 ，从服务器传过来的,不过用了 TCP 后就不用了
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void ServerToAndroid(UMessage msg){


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
                APP.userType = vxLoginCall.getInfo().getUser_type();
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

                        goControlActivity();


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




    /**
     *
     * 有空放 Service 里面
     * */
    private String tcp_client_id;   //  服务器分配的连接 id
    private String cache;   //  字符串缓存，当从服务器传输过来的内容太多可能会被分段发送，所以这里用一个缓存字符串拼接原本 分段的内容
    private String tcpResponse; //  实际经过拼接处理后的 服务器字符串
    private long lastBindTime; // 上一次TCP绑定时间
    private long QRReturnTime; // 上一次扫码二维码返回时间
    private final static String TCP_DEBUG = "TCP调试";
    private void initTCP(){
        TCPConnectUtil.getInstance().connect();
        TCPConnectUtil.getInstance().setListener(new NettyClientListener() {
            @Override
            public void onMessageResponseClient(byte[] bytes, int i) {
                //  来自服务器的响应
                tcpResponse = new String(bytes, StandardCharsets.UTF_8);

                if(tcpResponse != null && tcpResponse.length() == 9 && "error msg".equals(tcpResponse)){
                    return;
                }


                Log.i(TCP_DEBUG,"服务器推送内容长度：" + tcpResponse.length() + "，TCP 当前状态：" + i);


                //  这一步解决一个返回特征值分段问题    =====================================================
                //  以扫码推送特征值开头
                if(tcpResponse.startsWith("{\"type\":\"QrReturn\",") && !tcpResponse.endsWith("}")){
                    cache = tcpResponse;
                    return;
                }

                if(tcpResponse.startsWith("{\"type\":\"GQrReturn\",") && !tcpResponse.endsWith("}")){
                    cache = tcpResponse;
                    return;
                }


                //  这一步解决一个返回特征值分段问题    =====================================================
                //  以其它设备推送特征值开头
                if(tcpResponse.startsWith("{\"type\":\"nearByFeatrueSend\",") && !tcpResponse.endsWith("}")){
                    cache = tcpResponse;
                    return;
                }

                //  如果 分 3段，为中间那段

                if(!tcpResponse.startsWith("{") /*&& tcpResponse.length() > 200*/ && !tcpResponse.endsWith("}")){
                    cache = cache + tcpResponse;
                    return;
                }


                //  改成 60 即可
                if(!tcpResponse.startsWith("{") && /*tcpResponse.length() > 200 &&*/ tcpResponse.endsWith("}")){
                    tcpResponse = cache + tcpResponse;
                }

                //  =======================================================================================

                //  分段异常
                /*if(tcpResponse.endsWith("\"megvii_android.util.Base64\"}}}") && tcpResponse.length() < 200){
                    tcpResponse = cache + tcpResponse;
                    *//*mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"出错了，请重新扫码二维码",Toast.LENGTH_LONG).show();
                            showToast("出错了，请重新扫描二维码",Toast.LENGTH_LONG,false,null);
                        }
                    });*//*
                }*/


                Log.i(TCP_DEBUG, "服务器推送过来的内容拼接结果：" + tcpResponse + ",长度:" + tcpResponse.length() + "，状态:" + i);

                //  首先判定 响应中是否存在 type 和 data 是否以 { 开头
                if(tcpResponse.startsWith("{") && tcpResponse.contains("type") && tcpResponse.contains("data") && tcpResponse.endsWith("}")){
                    try {
                        JSONObject jsonObject = new JSONObject(tcpResponse);
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

                                            //NetWorkUtil.getInstance().errorUpload("TCP 认证 connect_rz_msg" + response);

                                            Log.i(TCP_DEBUG,"TCP绑定结果:" + response);

                                            if(response.contains("设备已绑定tcp连接")){

                                            }else if(response.contains("连接池未找到改连接ID")){

                                            }

                                        }

                                        @Override
                                        public void fail(Call call, IOException e) {
                                            Log.i(TCP_DEBUG,"TCP绑定发生错误" + e.getMessage());
                                            //NetWorkUtil.getInstance().errorUpload("TCP 认证 connect_rz_msg" + e.getMessage());
                                        }

                                        @Override
                                        public void error(Exception e) {
                                            Log.i(TCP_DEBUG,"TCP绑定发生错误" + e.getMessage());
                                            //NetWorkUtil.getInstance().errorUpload("TCP 认证 connect_rz_msg" + e.getMessage());
                                        }
                                    });

                                }
                            });
                        }else if(type.equals("client_connect_msgect_msg")){
                            //  连接成功注册 与 绑定

                            //NetWorkUtil.getInstance().errorUpload("TCP 绑定 client_connect_msgect_msg");

                            //  TCP 发两个bug
                            if(System.currentTimeMillis() - lastBindTime < 1000){
                                Log.i(TCP_DEBUG,"TCP触发重复绑定，1s内多次接收到服务器传递过来的绑定信号，抛弃");
                                return;
                            }

                            lastBindTime = System.currentTimeMillis() / 1000;
                            TCPVerify verify = new TCPVerify();
                            verify.setType("login");
                            TCPVerify.DataBean dataBean = new TCPVerify.DataBean();
                            dataBean.setSign(md5(lastBindTime + key).toUpperCase());
                            /*dataBean.setTimestamp(String.valueOf(lastBindTime-(60*1000*10)));*/
                            dataBean.setTimestamp(String.valueOf(lastBindTime));
                            verify.setData(dataBean);

                            Log.i(TCP_DEBUG,"发送TCP认证信息" + gson.toJson(verify));

                            TCPConnectUtil.getInstance().sendData(gson.toJson(verify));


                            TCPVerifyResponse tcpVerify = gson.fromJson(data, TCPVerifyResponse.class);
                            tcp_client_id = tcpVerify.getClient_id();

                        }else if(type.equals("buy_success_msg")){
                            //  购买 成功反馈
                            BuySuccessMsg buySuccessMsg = gson.fromJson(data,BuySuccessMsg.class);

                            EventBus.getDefault().post(buySuccessMsg);
                        }else if(type.equals("QrReturn")){

                            //  扫码推送特征值太快则抛弃。
                            if(System.currentTimeMillis() - QRReturnTime < 1000){
//                                return;
                            }

                            VXLoginCall vxLoginCall = gson.fromJson(data,VXLoginCall.class);
                            //  修改当前设置的用户id
                            APP.userId = vxLoginCall.getInfo().getUser_id();
                            //  修改当前用户类型
                            APP.userType = vxLoginCall.getInfo().getUser_type();


                            //  隐藏二维码扫码
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                                        qrCodeDialog.dismiss();
                                    }
                                }
                            });

                            //  云端有该人的人脸特征，则将特征保存到本地
                            if(vxLoginCall.isFeatrue_state() && vxLoginCall.isFace_image_state()){

                                UserMessage userMessage = DataBaseUtil.getInstance(MainActivity.this)
                                        .getDaoSession().getUserMessageDao().queryBuilder()
                                        .where(UserMessageDao.Properties.UserId.eq(vxLoginCall.getInfo().getUser_id()))
                                        .unique();
                                //  本地已经有这个人脸特征了，则删除掉原有的人脸特征，添加新的人脸特征
                                if(userMessage != null){
                                    //  人脸库中删除这个人脸特征
                                    mFacePassHandler.deleteFace(userMessage.getFaceToken().getBytes());
                                    //  本地数据库中删除这个用户
                                    DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getUserMessageDao().delete(userMessage);
                                    Log.i(NOW_TAG,"删除旧的人脸特征与注册信息");
                                }else{
                                    Log.i(NOW_TAG,"不存在该用户，可以添加");
                                }


                                Log.i(NOW_TAG,"当前用户 含有人脸特征值 和 人脸图片");
                                //  获取来自服务器人脸特征 ( 字符串之前是 Base64 形式 )
                                byte[] feature = Base64.decode(vxLoginCall.getInfo().getFeatrue(),Base64.DEFAULT);

                                Log.i("特征值长度","结果:" + feature.length);

                                FacePassFeatureAppendInfo facePassFeatureAppendInfo = new FacePassFeatureAppendInfo();
                                //  插入人脸特征值，返回faceToken ，如果特征值不可用会抛出异常
                                String faceToken = mFacePassHandler.insertFeature(feature,facePassFeatureAppendInfo);
                                //  faceToken 绑定底库
                                boolean bindResult = mFacePassHandler.bindGroup(group_name, faceToken.getBytes());
                                //  绑定成功就可以 将 faceToken 和 id 进行绑定了

                                if(bindResult){

                                    //  更新QRReturn时间
                                    QRReturnTime = System.currentTimeMillis();

                                    Log.i(NOW_TAG,"绑定成功，将跳转控制台");
                                    //  faceToken 和用户id 绑定
                                    DataBaseUtil.getInstance(MainActivity.this).insertUserIdAndFaceToken(app.getUserId(),faceToken);

                                    //  跳转到垃圾箱控制台
                                    goControlActivity();

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

                        }else if(type.equals("GQrReturn")){
                            //  回收桶二维码登录
                            GQrReturnBean gQrReturnBean = gson.fromJson(data,GQrReturnBean.class);
                            APP.userId = gQrReturnBean.getInfo().getUser_id();
                            APP.userType = gQrReturnBean.getInfo().getUser_type();
                            goControlActivity();
                        }else if(type.equals("nfcActivity")){
                            NfcActivityBean nfcActivityBean = gson.fromJson(data,NfcActivityBean.class);
                            //  nfc 绑定成功
                            if(nfcActivityBean.getData().getCode() == 1){
                                //  设置用户id
                                APP.setUserId(nfcActivityBean.getData().getInfo().getUser_id());
                                APP.userType = nfcActivityBean.getData().getInfo().getUser_type();

                                //  跳转到垃圾箱控制台
                                goControlActivity();

                            }
                        }else if(type.equals("nearByFeatrueSend")){
                            NearByFeatrueSendBean nearByFeatrueSendBean = gson.fromJson(data,NearByFeatrueSendBean.class);



                            if(nearByFeatrueSendBean != null && nearByFeatrueSendBean.getFeatrue() != null){
                                Log.i("响应结果",nearByFeatrueSendBean.toString());
                                //  获取来自服务器人脸特征 ( 字符串之前是 Base64 形式 )
                                byte[] feature = Base64.decode(nearByFeatrueSendBean.getFeatrue(),Base64.DEFAULT);

                                FacePassFeatureAppendInfo facePassFeatureAppendInfo = new FacePassFeatureAppendInfo();
                                //  插入人脸特征值，返回faceToken ，如果特征值不可用会抛出异常
                                String faceToken = mFacePassHandler.insertFeature(feature,facePassFeatureAppendInfo);
                                //  facetoken 绑定底库
                                boolean bindResult = mFacePassHandler.bindGroup(group_name, faceToken.getBytes());
                                //  绑定成功就可以 将 facetoken 和 id 进行绑定了

                                if(bindResult) {
                                    //  facetoken 和用户id 绑定
                                    DataBaseUtil.getInstance(MainActivity.this).insertUserIdAndFaceToken(nearByFeatrueSendBean.getUser_id(), faceToken);
                                }

                            }else{
                            }
                        }else if(type.equals("deleteAllFace")){
                            //  删除所有用户信息
                            DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getUserMessageDao().deleteAll();
                            //  删除人脸库
                            mFacePassHandler.clearAllGroupsAndFaces();
                            //  重启
                            AndroidDeviceSDK.reBoot(MainActivity.this);
                        }else if(type.equals("updateAllUserType0")){
                            //  修改所有用户类型 为 0
                            List<UserMessage> userMessageList = DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getUserMessageDao().queryBuilder().list();

                            for(UserMessage userMessage : userMessageList){
                                userMessage.setUserType(0);
                            }

                            DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getUserMessageDao().updateInTx(userMessageList);

                            NetWorkUtil.getInstance().errorUpload("用户类型已全部修改为 0 ");

                            Log.i("updateAllUserType0","修改之前");
                        }else if(type.equals("reboot")){
                            AndroidDeviceSDK.reBoot(MainActivity.this);
          
                        }else if(/*type.equals("addFaceImage")*/false){  //  添加人脸，人脸在服务器注册

                            Log.i("addFaceImage","进入添加人脸");

                            JsonObject addFaceImageJsonObject = JsonParser.parseString(data).getAsJsonObject();


                            int userId = addFaceImageJsonObject.get("userId").getAsInt();
                            int userType = addFaceImageJsonObject.get("userType").getAsInt();
                            String imageUrl = addFaceImageJsonObject.get("imageUrl").getAsString();

                            APP.userId = userId;
                            APP.userType = userType;

                            Log.i("addFaceImage",addFaceImageJsonObject.toString());

                            Log.i("addFaceImage",userId+","+userType+","+imageUrl);


                            //  查询是否已经有这个人的特征值
                            UserMessage userMessage = DataBaseUtil.getInstance(MainActivity.this)
                                    .getDaoSession().getUserMessageDao().queryBuilder()
                                    .where(UserMessageDao.Properties.UserId.eq(userId))
                                    .unique();
                            //  本地已经有这个人脸特征了，则删除掉原有的人脸特征，添加新的人脸特征
                            if(userMessage != null){
                                //  人脸库中删除这个人脸特征
                                mFacePassHandler.deleteFace(userMessage.getFaceToken().getBytes());
                                //  本地数据库中删除这个用户
                                DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getUserMessageDao().delete(userMessage);
                                Log.i("addFaceImage","删除旧的人脸特征与注册信息" + userMessage.getUserId());
                            }else{
                                Log.i("addFaceImage","不存在该用户，可以添加" + userId);
                            }


                            downloadFaceImage(imageUrl,userId, userType);
                        }

                    }catch (Exception e){
                        e.printStackTrace();

                        Log.i(TCP_DEBUG, "发生异常：" + e.getMessage());

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showToast("出错了，请重新扫码", Toast.LENGTH_SHORT,false,null);
                            }
                        });
                    }
                }else{
                    Log.i(TCP_DEBUG,"接收到不符合规范的 TCP 推送值" + tcpResponse);
                }

            }

            @Override
            public void onClientStatusConnectChanged(int i, int i1) {

                //  断开连接   0，0
                //  连接成功为 1 , 0

                //  设备突然离线是 -1 , 0   NettyClientHandler: exceptionCaught
                Log.i(TCP_DEBUG,"TCP状态改变 i : " + i + " , " + i1);
                if(i == 1){
                    //NetWorkUtil.getInstance().errorUpload("TCP连接成功");
                    Log.i(TCP_DEBUG,"连接成功");
                }else{
                    //  重新连接
                    /*TCPConnectUtil.getInstance().disconnect();
                    TCPConnectUtil.getInstance().connect();*/

                    //TCPConnectUtil.getInstance().reconnect();

                    //Log.i("Netty","重新连接");
                }

                //  设备离线 断开重连接
                /*if(i == 0 || i == -1){

                    NetWorkUtil.getInstance().errorUpload("设备断开TCP连接,时间: " + System.currentTimeMillis() + "，状态 i :" + i + ", i1："+ i1 );

                    VoiceUtil.getInstance().openAssetMusics(MainActivity.this,"no_work_time.aac");

                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"触发设备离线",Toast.LENGTH_LONG).show();

                            TCPConnectUtil.getInstance().disconnect();
                            TCPConnectUtil.getInstance().connect();
                        }
                    },3000);
                }*/
            }
        });

    }



    /**
     * 下载人脸图片
     * */
    private synchronized void downloadFaceImage(String imageUrl,final long userId,long userType) throws Exception{


        Log.i("addFaceImage","开始下载人脸图片");
        //  开始下载图片
        DownloadUtil.get().download(imageUrl, Environment.getExternalStorageDirectory().toString(),
                System.currentTimeMillis() + ".jpg", new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(File file) {

                        Log.i("addFaceImage","下载完毕");
                        try {



                            //  提取图片特征值
                            FacePassExtractFeatureResult facePassExtractFeatureResult =
                                    mFacePassHandler.extractFeature(BitmapFactory.decodeFile(file.getAbsolutePath()));
                            //  如果特征值合格
                            if(facePassExtractFeatureResult.result == 0){

                                FacePassFeatureAppendInfo facePassFeatureAppendInfo = new FacePassFeatureAppendInfo();

                                //  创建 faceToken
                                String faceToken = mFacePassHandler.insertFeature(facePassExtractFeatureResult.featureData,facePassFeatureAppendInfo);

                                nowFaceToken = faceToken;

                                boolean bindResult = mFacePassHandler.bindGroup(group_name, faceToken.getBytes());

                                //  绑定结果
                                if(bindResult){
                                    Log.i("addFaceImage",userId + ",绑定成功" + faceToken);
                                    //  本地实现
                                    DataBaseUtil.getInstance(MainActivity.this).insertUserIdAndFaceTokenThread(userId,userType,nowFaceToken);


                                    Map<String,String> hasMap = new HashMap<>();
                                    hasMap.put("user_id",String.valueOf(userId));
                                    hasMap.put("device_id",APP.getDeviceId());
                                    //  通知人脸合格
                                    NetWorkUtil.getInstance().doPost(ServerAddress.POST_FACE_REGISTER_SUCCESS_LOG, hasMap, new NetWorkUtil.NetWorkListener() {
                                        @Override
                                        public void success(String response) {
                                            Log.i("addFaceImage","通知服务器" + response);
                                        }

                                        @Override
                                        public void fail(Call call, IOException e) {

                                        }

                                        @Override
                                        public void error(Exception e) {

                                        }
                                    });


                                }else{
                                    mFacePassHandler.deleteFace(faceToken.getBytes());
                                    Log.i("addFaceImage",userId + ",绑定失败");
                                }
                            }else{
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "人脸不合格", Toast.LENGTH_SHORT).show();
                                        file.delete();
                                    }
                                });
                            }
                        }catch (Exception e){
                            Log.i("addFaceImage",e.getMessage());
                        }finally {
                            //  删除图片
                            //  file.delete();
                        }



                    }

                    @Override
                    public void onDownloading(int progress) {

                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        Log.i("addFaceImage","下载失败"+e.getMessage());
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
                            Log.i(DEBUG_TAG,"异常：" + e.getMessage());
                            //  人脸库初始化失败，上报服务器
                            if(faceCreateResult){
                                NetWorkUtil.getInstance().errorUpload("检测到人脸识别库创建失败");
                                faceCreateResult = false;
                            }
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

    private boolean faceCreateResult = true;

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


    /**
     * 跳转控制台投递界面
     * */
    public final static String TAG = "投递时间";
    private void goControlActivity(){
        //  如果为 null ，重新请求投递时间
        if(binsWorkTimeBean == null){
            Log.i(TAG,"投递时间 binsWorkTimeBean 为 null");
            NetWorkUtil.getInstance().doGetThread(ServerAddress.GET_BINS_WORK_TIME, null, new NetWorkUtil.NetWorkListener() {
                @Override
                public void success(String response) {
                    Log.i(TAG,"为 null" + response);
                    binsWorkTimeBean  = gson.fromJson(response,BinsWorkTimeBean.class);
                }

                @Override
                public void fail(Call call, IOException e) {

                }

                @Override
                public void error(Exception e) {

                }
            });
        }



        APP.hasManTime = System.currentTimeMillis();

        //  是特殊用户 userType 为 1 ， 普通用户则为 0
        Log.i(CARD_DEBUG,String.valueOf(APP.userType));
        //  特殊用户就直接跳转了
        if(APP.userType == 1){
            Log.i(TAG,"用户类型为 1");

            //  跳转到垃圾箱控制台
            Intent intent = new Intent(MainActivity.this,ControlActivity.class);
            intent.putExtra("userId",app.getUserId());
            startActivityForResult(intent,CONTROL_RESULT_CODE);
        }else{
            Log.i(TAG,"用户类型不为 1");
            //  非特殊用户
            if(BinsWorkTimeUntil.getBinsWorkTime(binsWorkTimeBean)){
                Log.i(TAG,"非特殊用户 投放时间");
                //  是投放时间，跳转到垃圾箱控制台
                Intent intent = new Intent(MainActivity.this,ControlActivity.class);
                intent.putExtra("userId",app.getUserId());
                startActivityForResult(intent,CONTROL_RESULT_CODE);
            }else{
                Log.i(TAG,"非特殊用户 投放时间");
                //  showToast("验证成功，但非投放时间", Toast.LENGTH_SHORT, false, null);
                Toast.makeText(MainActivity.this,"非投放时间",Toast.LENGTH_LONG).show();
                stringBuilder.delete(0,stringBuilder.length());
                //  非投放时间
                VoiceUtil.getInstance().openAssetMusics(MainActivity.this,"no_work_time.aac");
            }
        }
    }

    AlertDialog icAlert;
    public long icCardTime;
    private final static String CARD_DEBUG = "ic卡调试";
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void icCard(final ICCard icCard){

        Log.i(CARD_DEBUG,"进入刷卡");

        if(System.currentTimeMillis() - icCardTime < 2000){
            Toast.makeText(MainActivity.this, "刷卡太过频繁", Toast.LENGTH_SHORT).show();
            return;
        }else{
            icCardTime = System.currentTimeMillis();
        }

        Log.i(CARD_DEBUG,icCard.toString());

        Map<String,String> map = new HashMap<>();
        map.put("card_code",icCard.getCardCode());
        NetWorkUtil.getInstance().doPost(ServerAddress.IC_GetNfcUserBean, map, new NetWorkUtil.NetWorkListener() {
            @Override
            public void success(String response) {
                Log.i(CARD_DEBUG,"刷卡接收" + response);
                GetNfcUserBean getNfcUserBean = gson.fromJson(response,GetNfcUserBean.class);
                if(getNfcUserBean.getCode() == 1){
                    if(getNfcUserBean.getData().getState() == 1){

                        if(!APP.controlActivityIsRun){
                            //  已经绑定好了
                            //  设置用户id
                            APP.setUserId(getNfcUserBean.getData().getUser_id());
                            APP.userType = getNfcUserBean.getData().getUser_type();

                            //  跳转到垃圾箱控制台
                            goControlActivity();
                        }else{
                            Toast.makeText(MainActivity.this, "请不要重复刷卡！", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        VoiceUtil.getInstance().openAssetMusics(MainActivity.this,"bind_ic_voice.aac");
                        //  卡片未激活，弹出绑定窗口
                        //  二维码弹窗
                        if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                            qrCodeDialog.dismiss();
                        }


                        //  如果已经存在 绑定 ic卡的二维码
                        if(icAlert != null && icAlert.isShowing()){
                            icAlert.dismiss();
                        }

                        ImageView imageView = new ImageView(MainActivity.this);
                        int padding = 50;
                        imageView.setPadding(padding,padding,padding,padding);
                        imageView.setImageBitmap(QRCodeUtil.getICLogin(ServerAddress.IC_BING + "?device_id=" + APP.getDeviceId() + "&card_code=" + icCard.getCardCode()));


                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setCancelable(true);
                        alert.setTitle("请使用微信扫描二维码绑定 IC 卡 .");
                        alert.setView(imageView);
                        alert.create();
                        icAlert = alert.show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "卡片不存在" + icCard.getCardCode(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void fail(Call call, IOException e) {
                Log.i(CARD_DEBUG,e.getMessage());
            }

            @Override
            public void error(Exception e) {
                Log.i(CARD_DEBUG,e.getMessage());
            }
        });


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
                    Log.e(MY_TAG,"人脸帧invalid失败");
                    continue;
                }

                /* 将每一帧FacePassImage 送入SDK算法， 并得到返回结果 */
                FacePassDetectionResult detectionResult = null;
                try {

                    detectionResult = mFacePassHandler.feedFrame(image);
                } catch (FacePassException e) {
                    Log.e(MY_TAG,"人脸脸框异常");
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
                                        Log.e(MY_TAG,"联机人脸数据解码失败");
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
                //  暂时不要用
                /*if(autoIntentAdvertising & endTime - lastFaceTime > (300 * 1000)){
                    autoIntentAdvertising = false;

                    Intent intent = new Intent(MainActivity.this,AdvertisingActivity.class);
                    startActivityForResult(intent,INTENT_ADVERTISING_CODE); //  这里是携带返回值的，如果到时候从广告界面返回，就又要启动 30 s自动跳转到广告界面

                }*/
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

                                Log.i("addFaceImage","faceToken 离线状态下的人脸识别"  + faceToken);


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
                } catch (InterruptedException | FacePassException e) {
                    Log.e(MY_TAG,"人脸getFaceImageByFaceToken异常，RecognizeThread.run");
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


        TabLayout tabLayout = findViewById(R.id.login_type_tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("扫码使用"));
        tabLayout.addTab(tabLayout.newTab().setText("注册人脸"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if(float_qrcode_image == null){
                    float_qrcode_image = findViewById(R.id.float_qrcode_image);
                }

                if(tab.getPosition() == 0 ){
                    float_qrcode_image.setImageBitmap(QRCodeUtil.getAppletLoginCode(ServerAddress.LOGIN + APP.getDeviceId() + "&device_type=2"));
                }else{
                    float_qrcode_image.setImageBitmap(QRCodeUtil.getAppletLoginCode(ServerAddress.LOGIN + APP.getDeviceId()));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



        EditText phoneNumberText = findViewById(R.id.phoneNumberText);
        CustomNumKeyView customNumKeyView = findViewById(R.id.keyboardview);
        customNumKeyView.setOnCallBack(new CustomNumKeyView.CallBack() {
            @Override
            public void clickNum(String num) {
                if("手机登录".equals(num)){

                    if(phoneNumberText.getText().length() != 11){
                        toast("手机号码格式不符合规范");
                        return;
                    }

                    Map<String,String> hasMap = new HashMap<>();
                    hasMap.put("phone",phoneNumberText.getText().toString());
                    NetWorkUtil.getInstance().doPost(ServerAddress.PHONE_LOGIN, hasMap, new NetWorkUtil.NetWorkListener() {
                        @Override
                        public void success(String response) {

                            //  手机号码登录返回结果
                            PhoneLoginBean phoneLoginBean = gson.fromJson(response,PhoneLoginBean.class);
                            if(phoneLoginBean.getCode() == 1){
                                APP.userId = phoneLoginBean.getData().getUser_id();
                                APP.userType = phoneLoginBean.getData().getUser_type();

                                if(phoneLoginDialog != null && phoneLoginDialog.isShowing()){
                                    phoneLoginDialog.dismiss();
                                }

                                phoneNumberText.setText(null);

                                toast("登录成功");
                                goControlActivity();
                            }else{
                                toast("请先使用微信扫描二维码绑定手机号码");
                            }


                        }

                        @Override
                        public void fail(Call call, IOException e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void error(Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    phoneNumberText.append(num);
                }
            }

            @Override
            public void deleteNum() {
                Toast.makeText(MainActivity.this, "清空成功", Toast.LENGTH_SHORT).show();
                phoneNumberText.setText("");
            }
        });


        //  添加按钮以及设置监听器
        mSyncGroupBtn = (ImageView) findViewById(R.id.btn_group_name);
        mSyncGroupBtn.setOnClickListener(this);

        mFaceOperationBtn = (ImageView) findViewById(R.id.btn_face_operation);
        mFaceOperationBtn.setOnClickListener(this);

        //  直接登录
        float_qrcode_image = (ImageView) findViewById(R.id.float_qrcode_image);
        float_qrcode_image.setImageBitmap(QRCodeUtil.getAppletLoginCode(ServerAddress.LOGIN + APP.getDeviceId() + "&device_type=2"));



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

                        Log.i("Netty","主动断开");
                        //  先断开，在连接
                        //TCPConnectUtil.getInstance().disconnect();
                        Log.i("Netty","主动连接");
                        //TCPConnectUtil.getInstance().connect();
                        Toast.makeText(MainActivity.this,"重连",Toast.LENGTH_LONG).show();



                        final String number = getrandom();
                        //  开始上报
                        //NetWorkUtil.getInstance().errorUpload("故障，重启授权码:" + number);


                        adminLoginDialog = new AdminLoginDialog(MainActivity.this);
                        adminLoginDialog.setLoginListener(new AdminLoginDialog.LoginListener() {
                            @Override
                            public void callBack(String editStr,String password,AlertDialog alertDialog) {

                                if(editStr.equals(number)){
                                    if(password.equals("123456")){
                                        AndroidDeviceSDK.reBoot(MainActivity.this);
                                    }else if(password.equals("99")){

                                        Log.i("Netty","主动断开");
                                        //  先断开，在连接
                                        //TCPConnectUtil.getInstance().disconnect();
                                        Log.i("Netty","主动连接");
                                        //TCPConnectUtil.getInstance().connect();

                                    }else if(password.equals("1")){
                                         AndroidDeviceSDK.hideStatus(MainActivity.this,false);
                                    }else if(password.equals("2")){
                                        int i = 0 / 1;
                                    }else if(password.equals("3")){
                                        Toast.makeText(MainActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
                                        TCPConnectUtil.getInstance().disconnect();
                                    }else{
                                        alertDialog.dismiss();
                                    }
                                }else{
                                    alertDialog.dismiss();
//                                    startActivity(new Intent(MainActivity.this,SettingActivity.class));
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


        //  长按隐藏状态栏 和 进行后台监听
        cameraView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                showQRCodeDialog();

                //  隐藏状态栏，也就是 app 打开后不能退出
                AndroidDeviceSDK.hideStatus(MainActivity.this,true);
                //  检查是否在前台
                AndroidDeviceSDK.checkForeground(MainActivity.this,true);


                APP.controlImagePreview = true;

                return false;
            }
        });

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


    public static String getrandom(){
        String code = "";
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int r = random.nextInt(10); //每次随机出一个数字（0-9）
            code = code + r;  //把每次随机出的数字拼在一起
        }
        return code;

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



        TCPConnectUtil.getInstance().disconnect();

        AndroidDeviceSDK.hideStatus(this,false);



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
        //  idTextView.setText(text);

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
            case CONTROL_RESULT_CODE:

                APP.userId = 0;
                APP.userType = 0;

                if(data != null){
                    int exitCode = data.getIntExtra("exitCode",0);
                    //  结算超时
                    if(exitCode != 0){
                        if(exitCode == 1){
                            closeAllDoor();
                        }
                    }
                }


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(DustbinStateBean dustbinStateBean : APP.dustbinBeanList){
                            //  关补光灯
                            SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeLight(dustbinStateBean.getDoorNumber()));
                            try {
                                Thread.sleep(50);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

                break;
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

                //  如果图片是颠倒的，则旋转过来
                if(bitmap.getWidth() > bitmap.getHeight()){
                    bitmap = adjustPhotoRotation(bitmap,90);
                }

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

                break;
            case INTENT_ADVERTISING_CODE:
                //  从广告界面返回来，重新30s自动跳转
                lastFaceTime = System.currentTimeMillis();

                autoIntentAdvertising = true;

                break;
        }
    }



    /**
     *
     * @param bm 原有位图对象
     * @param orientationDegree 旋转角度
     * 图片旋转
     * */
    public Bitmap adjustPhotoRotation(Bitmap bm, int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);

        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);

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
    private PhoneLoginDialog phoneLoginDialog;
    private void showQRCodeDialog(){
        if(true){
            return;
        }


        if(qrCodeDialog != null && qrCodeDialog.isShowing()){
            return;
        }


        if(isHasShowDialog()){
            return;
        }


        VoiceUtil.getInstance().openAssetMusics(MainActivity.this,"bind_face_voice.aac");

        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        //  alert.setCancelable(false);
        View view = View.inflate(MainActivity.this,R.layout.qr_code_layout,null);
        ImageView qr_code_login = (ImageView)view.findViewById(R.id.iv_qr_code_login);
        ImageView iv_qr_code_vxLogin = (ImageView)view.findViewById(R.id.iv_qr_code_vxLogin);

        iv_qr_code_vxLogin.setImageBitmap(QRCodeUtil.getAppletLoginCode(ServerAddress.LOGIN + APP.getDeviceId() + "&device_type=2"));


        //  获取安卓设备唯一标识符
        String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //  添加时间戳

        Log.i(NOW_TAG,"二维码显示的内容 服务器地址 : " + ServerAddress.LOGIN );

        Log.i(NOW_TAG,"二维码显示的内容 设备ID : " + APP.getDeviceId());


        //  拼接地址 传递 token
        qr_code_login.setImageBitmap(QRCodeUtil.getAppletLoginCode(ServerAddress.LOGIN + APP.getDeviceId()));


        //  暂时添加一个点击事件，模拟扫码成功，并通过TCP连接返回了用户id和token
        alert.setPositiveButton("手机号登录 ( 需先扫描登录二维码绑定 )", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                phoneLoginDialog = new PhoneLoginDialog(MainActivity.this);
                phoneLoginDialog.setLoginListener(new PhoneLoginDialog.LoginListener() {
                    @Override
                    public void callBack(String editStr, AlertDialog alertDialog) {

                        Map<String,String> hasMap = new HashMap<>();
                        hasMap.put("phone",editStr);
                        NetWorkUtil.getInstance().doPost(ServerAddress.PHONE_LOGIN, hasMap, new NetWorkUtil.NetWorkListener() {
                            @Override
                            public void success(String response) {

                                //  手机号码登录返回结果
                                PhoneLoginBean phoneLoginBean = gson.fromJson(response,PhoneLoginBean.class);
                                if(phoneLoginBean.getCode() == 1){
                                    APP.userId = phoneLoginBean.getData().getUser_id();
                                    APP.userType = phoneLoginBean.getData().getUser_type();

                                    phoneLoginDialog.dismiss();

                                    toast("登录成功");
                                    goControlActivity();
                                }else{
                                    toast("请先使用微信扫描二维码绑定手机号码");
                                }


                            }

                            @Override
                            public void fail(Call call, IOException e) {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void error(Exception e) {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
                phoneLoginDialog.create();
                phoneLoginDialog.show();
            }
        });
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


                                goControlActivity();


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

            }
        });
    }


    ProgressDialog progressDialog ;


    /**
     * 查询当前人脸的faceToken 在数据库中是否对应某个从服务器传过来的 userId，如果对应，则不需要微信扫码登陆，直接进入控制台界面
     * @param faceToken 人脸识别扫描回调的 faceToken
     * */
    //  上一次人脸识别成功时间
    private long lastPassTime;
    private void queryFaceToken(String faceToken){


        //  避免重复进入 控制台界面，2s
        if(System.currentTimeMillis() - lastPassTime < 2000){
            Log.i(MY_TAG,"重复进入");
            return;
        }




        UserMessage userMessage = userMessageDao.queryBuilder().where(UserMessageDao.Properties.FaceToken.eq(faceToken)).build().unique();

        //  如果人脸底库中存在该人脸，且有登陆记录，则直接进入垃圾控制台
        if(userMessage != null){

            Log.i(MY_TAG,"用户信息不为 null");

            //  关闭二维码登陆弹窗
            if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                qrCodeDialog.dismiss();
            }

            app.setUserId(userMessage.getUserId());
            APP.userType = userMessage.getUserType();

            if(!isHasShowDialog()){
                lastPassTime = System.currentTimeMillis();
                //  跳转到垃圾箱控制台
                goControlActivity();

            }

        }else{
            Log.i("addFaceImage","找不到" + faceToken + "对应的用户");

            Log.i(MY_TAG,"人脸底库中存在该人脸，但是没有使用微信登陆过，则显示 扫描二维码弹窗");
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

        if(phoneLoginDialog != null && phoneLoginDialog.isShowing()){
            return true;
        }

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

        if(icAlert != null && icAlert.isShowing()){
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

                String path = Environment.getExternalStorageDirectory().toString();
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
                APP.userId = 0;
                APP.userType = 0;

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
                Log.e(MY_TAG,"人脸getFaceImageByFaceToken异常，mFacePassHandler.getFaceImage");
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


    /**
     * 初始化定位参数配置
     */
    private LocationClient locationClient;
    private static double latitude ,longitude;
    //获取经度信息;
    private void initLocationOption() {

        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        locationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类实例并配置定位参数
        LocationClientOption locationOption = new LocationClientOption();
        MyLocationListener myLocationListener = new MyLocationListener();
        //注册监听函数
        locationClient.registerLocationListener(myLocationListener);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locationOption.setCoorType("gcj02");
        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(3000);
        //可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(true);
        //可选，设置是否需要地址描述
        locationOption.setIsNeedLocationDescribe(true);
        //可选，设置是否需要设备方向结果
        locationOption.setNeedDeviceDirect(false);
        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationOption.setLocationNotify(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locationOption.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationOption.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(false);
        //可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
        //可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        locationOption.setIsNeedAltitude(false);
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        locationOption.setOpenAutoNotifyMode();
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        locationOption.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        locationClient.setLocOption(locationOption);
        //开始定位
        locationClient.start();
    }


    /**
     * 实现定位回调
     */
    public static class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            Log.i("经纬度","触发定位" + location.getLongitude() + "," + location.getLatitude());

            if(location.getLatitude() == 0.0 || location.getLongitude() == 0.0){

                return;
            }


            if ("4.9E-324".equals(String.valueOf(location.getLatitude())) || "4.9E-324".equals(String.valueOf(location.getLongitude()))) {
                return;
            }

            //  获取纬度信息
            latitude = location.getLatitude();
            //  获取经度信息
            longitude = location.getLongitude();

            Log.i("经纬度3","经纬度 " + latitude + "," + longitude);

            Map<String,String> map = new HashMap<>();
            map.put("longitude",String.valueOf(longitude));
            map.put("latitude",String.valueOf(latitude));
            NetWorkUtil.getInstance().doPost(ServerAddress.UPDATE_DEVICE_LOCATION, map, new NetWorkUtil.NetWorkListener() {
                @Override
                public void success(String response) {
                    Log.i("经纬度","维度更新结果：" + response);
                }

                @Override
                public void fail(Call call, IOException e) {

                }

                @Override
                public void error(Exception e) {

                }
            });

        }

    }




}
