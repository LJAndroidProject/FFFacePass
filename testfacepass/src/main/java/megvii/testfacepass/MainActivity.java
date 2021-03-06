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


    //  ??????????????????
    private enum FacePassSDKMode {
        MODE_ONLINE,
        MODE_OFFLINE
    };

    //  ???????????????????????????
    private static FacePassSDKMode SDK_MODE = FacePassSDKMode.MODE_OFFLINE;

    private static final String DEBUG_TAG = "FacePassDemo";

    public static final String MY_TAG = "??????????????????";

    public static final String MY_ORDER = "????????????";

    public static final String PUSH = "????????????";

    /* ???????????????IP */

    private static final String serverIP_offline = "10.104.44.50";//offline

    private static final String serverIP_online = "10.199.1.14";

    private static String serverIP;

    private static final String authIP = "https://api-cn.faceplusplus.com";
    private static final String apiKey = "N1CuZs4I1YuuCN5lO1ZmWyGiYG4ysH2k";
    private static final String apiSecret = "F0YG7KNSixDCOJnxmyPg3dToTkrTmiw-";

    private static String recognize_url;

    /* ????????????Group */
    private static final String group_name = "facepass";

    /* ?????????????????? ????????? ???????????? ???????????? */
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String PERMISSION_READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_INTERNET = Manifest.permission.INTERNET;
    private static final String PERMISSION_ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;
    private String[] Permission = new String[]{PERMISSION_CAMERA, PERMISSION_WRITE_STORAGE, PERMISSION_READ_STORAGE, PERMISSION_INTERNET, PERMISSION_ACCESS_NETWORK_STATE,ACCESS_COARSE_LOCATION};


    /* SDK ???????????? */
    FacePassHandler mFacePassHandler;

    /* ???????????? */
    private CameraManager manager;

    /* ?????????????????????????????? */
    private TextView faceBeginTextView;


    /* ??????faceId */
    private TextView faceEndTextView;

    /* ?????????????????? */
    private CameraPreview cameraView;

    private boolean isLocalGroupExist = false;

    /* ??????????????????????????? */
    private FaceView faceView;

    private ScrollView scrollView;

    /* ????????????????????????????????? */
    private static boolean cameraFacingFront = true;
    /* ?????????????????????????????????????????????????????????
     * ???????????????????????????????????????????????????rotation
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

    int screenState = 0;    // 0 ??? 1 ???

    /* ?????????????????? */
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


    /*????????????*/
    private ImageView mSyncGroupBtn;
    private AlertDialog mSyncGroupDialog;


    private ImageView mFaceOperationBtn;
    /*????????????*/
    private FaceImageCache mImageCache;

    private Handler mAndroidHandler;

    private CameraPreviewData mCurrentImage;

    private Button mSDKModeBtn;
    private UserMessageDao userMessageDao;
    private AdminLoginDialog adminLoginDialog;

    //  ????????????
    private boolean debug = false;
    private APP app ;
    private Handler mainHandler;
    private Gson gson = new Gson();
    //  ????????????????????????
    public final static int CONTROL_RESULT_CODE = 300;

    private ImageView float_qrcode_image;

    BinsWorkTimeBean binsWorkTimeBean;

    /**
     * ??? IC ?????????
     *
     * ????????????????????????????????????????????????????????????
     * */
    //  ??????????????????2
    public final static String ROOT_CARD2 = "A8000000000500C6D4912BADA9";
    //  ??????????????????1
    public final static String ROOT_CARD = "A80000000005003030A741E3A9";
    private AlertDialog offLinAdminAlertDialog;
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                String icCardContent = intent.getStringExtra("content");
                if(icCardContent != null){

                    //  ?????????????????????
                    if(icCardContent.equals(ROOT_CARD) || icCardContent.equals(ROOT_CARD2)){

                        if(offLinAdminAlertDialog != null && offLinAdminAlertDialog.isShowing()){
                            return;
                        }

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                String[] functionArray = new String[]{
                                        "?????????????????? ( ????????? ) ",
                                        "????????????TCP??????",
                                        "??????????????????",
                                        "????????????",
                                        "???????????????",
                                        "??????????????????",
                                        "?????????????????????",
                                        "???????????????????????????????????????",
                                        "????????????"
                                };
                                AlertDialog.Builder offLinDialog = new AlertDialog.Builder(MainActivity.this);
                                offLinDialog.setTitle(APP.getDeviceId() + " - ???????????????");
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
                                offLinDialog.setPositiveButton("????????????", new DialogInterface.OnClickListener() {
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


    //  ?????????????????????????????????????????????
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
                    Toast.makeText(MainActivity.this, "??????????????????????????????" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    //  ????????????
    private void updateConfig(){

        Map<String,String> map = new HashMap<>();
        map.put("device_id",APP.getDeviceId());
        map.put("mange_code","KDSU9E");
        NetWorkUtil.getInstance().doPost(ServerAddress.GET_DUSTBIN_CONFIG, map, new NetWorkUtil.NetWorkListener() {
            @Override
            public void success(String response) {

                Log.i("??????????????????",response);

                GetDustbinConfig getDustbinConfig = new Gson().fromJson(response,GetDustbinConfig.class);

                if(getDustbinConfig.getCode() == 1){
                    List<DustbinStateBean> list = new ArrayList<>();

                    List<GetDustbinConfig.DataBean.ListBean> listBeans = getDustbinConfig.getData().getList();
                    for(GetDustbinConfig.DataBean.ListBean listBean : listBeans){

                        //  ?????????id   ???????????????
                        long id = listBean.getId();
                        //  ????????????    ???????????????????????????
                        int number = Integer.parseInt(listBean.getBin_code());
                        //  ??????????????? ?????? ?????????????????????????????????????????????
                        String typeString = DustbinUtil.getDustbinType(listBean.getBin_type());
                        //  ??????????????? ??????A1 A2 B3 B5 C5 D6 D7 D8
                        String typeNumber = listBean.getBin_type();


                        list.add(new DustbinStateBean(id,number,typeString,typeNumber,0,0,0,0,false,false,false,false,false,false,false,false));
                    }

                    //  ??????????????????
                    DataBaseUtil.getInstance(MainActivity.this).setDustBinStateConfig(list);


                    /*
                     * ?????????????????????
                     * */
                    DustbinConfig dustbinConfig = new DustbinConfig();
                    dustbinConfig.setDustbinDeviceId(listBeans.get(0).getDevice_id());  //  deviceID
                    dustbinConfig.setDustbinDeviceName(getDustbinConfig.getData().getDevice_name());    //  deviceName ????????????????????????
                    dustbinConfig.setHasVendingMachine(getDustbinConfig.getData().getHas_amat() == 1);  //  ??????????????????
                    //  ?????????????????????????????????????????????
                    if(getDustbinConfig.getData().getHas_amat() == 1){
                        //  ????????????????????????
                        //  initReplenishment();
                    }
                    DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getDustbinConfigDao().insertOrReplace(dustbinConfig);    //  ????????????


                    Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();

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
     * ????????????
     * */
    class TimeChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_TIME_TICK:
                    //  ??????????????? ??????
                    Calendar now = Calendar.getInstance();
                    String timeString = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
                    //?????????
                    int year = now.get(Calendar.YEAR);
                    //???????????????0??????1??????
                    int month = now.get(Calendar.MONTH) + 1;
                    //??????????????????
                    int day = now.get(Calendar.DAY_OF_MONTH);
                    //??????????????????
                    int hour = now.get(Calendar.HOUR_OF_DAY);
                    if(timeString.equals("1:10")){
                        //  1 ???????????????
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AndroidDeviceSDK.reBoot(MainActivity.this);
                            }
                        },1000 * 60);
                    }
                    if(BinsWorkTimeUntil.getBinsWorkTime(binsWorkTimeBean)){
                        Log.i(TAG,"????????????");
                        for(DustbinStateBean dustbinStateBean: APP.dustbinBeanList){
                            //??????????????????????????????
                            //????????????????????????????????????????????????
                                Log.i(TAG,"???????????????????????????????????????"+dustbinStateBean.getDoorNumber()+"?????????");
                                SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDogHouse(dustbinStateBean.getDoorNumber()));
                        }
                    }else{
                        Log.i(TAG,"???????????????");
                        //?????????????????????
                        for(DustbinStateBean dustbinStateBean: APP.dustbinBeanList){
                            SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDoor(dustbinStateBean.getDoorNumber()));
                            //?????????????????????????????????
                            LogUtil.d(TAG,dustbinStateBean.getDoorNumber()+"??????????????????");
                            //????????????????????????????????????????????????
                                SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openDogHouse(dustbinStateBean.getDoorNumber()));
                                LogUtil.writeBusinessLog(dustbinStateBean.getDoorNumber()+"????????????");
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



        //  ????????????????????????
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);    //  ???????????????
        registerReceiver(new TimeChangeReceiver(),intentFilter);


        //  ??????IC?????????
        registerReceiver(new MyBroadcastReceiver(),new IntentFilter("icCard"));


        //  ??????????????????
        NetWorkUtil.getInstance().doGetThread(ServerAddress.GET_BINS_WORK_TIME, null, new NetWorkUtil.NetWorkListener() {
            @Override
            public void success(String response) {
                Log.i("????????????",response);
                binsWorkTimeBean  = gson.fromJson(response,BinsWorkTimeBean.class);
            }

            @Override
            public void fail(Call call, IOException e) {

            }

            @Override
            public void error(Exception e) {

            }
        });



        //  ??????APP?????????????????????
        closeAllDoor();

        //  ?????????????????????
        DustbinConfig dustbinConfig = DataBaseUtil.getInstance(this).getDaoSession().getDustbinConfigDao().queryBuilder().unique();
        app.setDustbinConfig(dustbinConfig);
        //  ?????? ?????? ????????? list ??????
        app.setDustbinBeanList(DataBaseUtil.getInstance(MainActivity.this).getDustbinByType(null));

        //  ????????????????????????????????????
        startService(new Intent(this,ResidentService.class));
        //  ????????????????????????
        startService(new Intent(this, UploadImageService.class));

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("?????????...??????");
        progressDialog.create();

        //  ??????????????????
        //PushAgent.getInstance(this).onAppStart();

        //  ??????????????????
        EventBus.getDefault().register(this);
        //  ??????????????????????????? app ?????????????????????
        AndroidDeviceSDK.hideStatus(MainActivity.this,true);
        //  ?????????????????????
        AndroidDeviceSDK.checkForeground(MainActivity.this,true);
        //  ??????????????????
        AndroidDeviceSDK.autoReBoot(this,true);
        //  ????????????????????????????????? ??????????????????????????????????????????????????????????????????????????????
        VoiceUtil.getInstance();

        //  ????????? greenDao ???????????????????????????????????????
        userMessageDao = DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getUserMessageDao();

        //  ????????? TCP ??????????????????????????? TCP ??????
        initTCP();

        if (SDK_MODE == FacePassSDKMode.MODE_ONLINE) {
            //  http://10.199.1.14:8080/api/service/recognize/v1
            recognize_url = "http://" + serverIP_online + ":8080/api/service/recognize/v1";
            serverIP = serverIP_online;
        } else {
            serverIP = serverIP_offline;
        }

        /* ??????????????? */
        initView();
        /* ???????????????????????? */
        if (!hasPermission()) {
            requestPermission();
        } else {
            initFacePassSDK();
        }

        initLocationOption();

        initFaceHandler();
        /* ???????????????????????? */
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        mRecognizeThread = new RecognizeThread();
        mRecognizeThread.start();
        mFeedFrameThread = new FeedFrameThread();
        mFeedFrameThread.start();

        //  ?????????????????????????????????????????????
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
        Log.i("????????????","onKeyDown ???????????????" + c  + "," + event.getUnicodeChar());

        return super.onKeyDown(keyCode, event);
    }

    private static StringBuilder stringBuilder = new StringBuilder();
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        /*InputDevice inputDevice = event.getDevice();
        inputDevice.getMotionRanges();
        //  SM SM-2D PRODUCT HID KBW
        Log.i("?????????",inputDevice.getName());*/
        if(event.getUnicodeChar() != 0){
            char c = (char) event.getUnicodeChar();
            Log.i("????????????","???????????????" + c  + "," + event.getUnicodeChar());
        }

        //  ????????????
        /*if(stringBuilder!=null && stringBuilder.length() > 0){
            if(String.valueOf(c).equalsIgnoreCase(stringBuilder.substring(stringBuilder.length()-1))){
                return true;
            }
        }*/




        //  ???????????????????????????????????????????????????
        if(phoneLoginDialog != null && phoneLoginDialog.isShowing()){
            return true;
        }else{
            if(event.getAction() == KeyEvent.ACTION_DOWN){
                char pressedKey = (char) event.getUnicodeChar();

                //  ???????????????????????????
                if(Character.isLetterOrDigit(pressedKey)){
                    stringBuilder.append(pressedKey);
                    Log.i("????????????",stringBuilder.toString());
                }
            }

            //  ?????? 20 ????????????????????????
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
                                Toast.makeText(MainActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
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

                    //  ??????
                    stringBuilder.delete(0,stringBuilder.length());
                }else{
                    Toast.makeText(this,"???????????????????????????",Toast.LENGTH_LONG).show();

                    stringBuilder.delete(0,stringBuilder.length());
                }
            }else{
                //Toast.makeText(this,"?????????????????????",Toast.LENGTH_LONG).show();

                //stringBuilder.delete(0,stringBuilder.length());
            }

        }


        return super.dispatchKeyEvent(event);
    }



    //  ???????????????
    private void closeAllDoor(){
        //  ??????APP??????????????????
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

                    //  ????????????
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
        //  ??????????????????
        FacePassHandler.getAuth(authIP, apiKey, apiSecret);

        FacePassHandler.initSDK(getApplicationContext());

        Log.d("FacePassDemo", FacePassHandler.getVersion());
    }


    //  ???????????? ???????????????????????????,???????????? TCP ???????????????
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void ServerToAndroid(UMessage msg){


        Log.i(NOW_TAG,"??????????????????????????????" + msg.custom);

        Gson gson = new Gson();

        try{

            JSONObject jsonObject = new JSONObject(msg.custom);
            String action = jsonObject.getString("action");
            String list = jsonObject.getString("list");

            if("QrReturn".equals(action)){
                VXLoginCall vxLoginCall = gson.fromJson(list,VXLoginCall.class);
                Log.i(NOW_TAG,"list ?????? : " + vxLoginCall.toString());
                Log.i(NOW_TAG,"???????????? id ??????????????? id " + app.getUserId());
                //  ???????????????????????????id
                app.setUserId(vxLoginCall.getInfo().getUser_id());
                APP.userType = vxLoginCall.getInfo().getUser_type();
                Log.i(NOW_TAG,"???????????? id ?????? ????????? id" + app.getUserId());

                //  ?????????????????????
                if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                    Log.i(NOW_TAG,"?????????????????????");
                    qrCodeDialog.dismiss();
                }

                //  ????????????????????????????????????????????????????????????
                if(vxLoginCall.isFeatrue_state() && vxLoginCall.isFace_image_state()){
                    Log.i(NOW_TAG,"???????????? ????????????????????? ??? ????????????");
                    //  ????????????????????????????????? ( ?????????????????? Base64 ?????? )
                    byte[] feature = Base64.decode(vxLoginCall.getInfo().getFeatrue(),Base64.DEFAULT);

                    FacePassFeatureAppendInfo facePassFeatureAppendInfo = new FacePassFeatureAppendInfo();
                    //  ??????????????????????????????faceToken ??????????????????????????????????????????
                    String faceToken = mFacePassHandler.insertFeature(feature,facePassFeatureAppendInfo);
                    //  facetoken ????????????
                    boolean bindResult = mFacePassHandler.bindGroup(group_name, faceToken.getBytes());
                    //  ????????????????????? ??? facetoken ??? id ???????????????

                    if(bindResult){
                        Log.i(NOW_TAG,"?????????????????????????????????");
                        //  facetoken ?????????id ??????
                        DataBaseUtil.getInstance(this).insertUserIdAndFaceToken(app.getUserId(),faceToken);

                        goControlActivity();


                        //  ?????????id????????????????????????????????????   ===========================================


                    }else{
                        Log.i(NOW_TAG,"???????????????????????????");
                        //  ????????????????????????????????????
                        mFacePassHandler.deleteFace(faceToken.getBytes());
                    }
                }else{
                    //   ?????? ?????????????????? ?????? ???????????????????????????????????????
                    Log.i(NOW_TAG,"????????????????????????????????????????????????????????????");


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
     * ????????? Service ??????
     * */
    private String tcp_client_id;   //  ???????????????????????? id
    private String cache;   //  ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? ???????????????
    private String tcpResponse; //  ?????????????????????????????? ??????????????????
    private long lastBindTime; // ?????????TCP????????????
    private long QRReturnTime; // ????????????????????????????????????
    private final static String TCP_DEBUG = "TCP??????";
    private void initTCP(){
        TCPConnectUtil.getInstance().connect();
        TCPConnectUtil.getInstance().setListener(new NettyClientListener() {
            @Override
            public void onMessageResponseClient(byte[] bytes, int i) {
                //  ????????????????????????
                tcpResponse = new String(bytes, StandardCharsets.UTF_8);

                if(tcpResponse != null && tcpResponse.length() == 9 && "error msg".equals(tcpResponse)){
                    return;
                }


                Log.i(TCP_DEBUG,"??????????????????????????????" + tcpResponse.length() + "???TCP ???????????????" + i);


                //  ????????????????????????????????????????????????    =====================================================
                //  ??????????????????????????????
                if(tcpResponse.startsWith("{\"type\":\"QrReturn\",") && !tcpResponse.endsWith("}")){
                    cache = tcpResponse;
                    return;
                }

                if(tcpResponse.startsWith("{\"type\":\"GQrReturn\",") && !tcpResponse.endsWith("}")){
                    cache = tcpResponse;
                    return;
                }


                //  ????????????????????????????????????????????????    =====================================================
                //  ????????????????????????????????????
                if(tcpResponse.startsWith("{\"type\":\"nearByFeatrueSend\",") && !tcpResponse.endsWith("}")){
                    cache = tcpResponse;
                    return;
                }

                //  ?????? ??? 3?????????????????????

                if(!tcpResponse.startsWith("{") /*&& tcpResponse.length() > 200*/ && !tcpResponse.endsWith("}")){
                    cache = cache + tcpResponse;
                    return;
                }


                //  ?????? 60 ??????
                if(!tcpResponse.startsWith("{") && /*tcpResponse.length() > 200 &&*/ tcpResponse.endsWith("}")){
                    tcpResponse = cache + tcpResponse;
                }

                //  =======================================================================================

                //  ????????????
                /*if(tcpResponse.endsWith("\"megvii_android.util.Base64\"}}}") && tcpResponse.length() < 200){
                    tcpResponse = cache + tcpResponse;
                    *//*mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"????????????????????????????????????",Toast.LENGTH_LONG).show();
                            showToast("????????????????????????????????????",Toast.LENGTH_LONG,false,null);
                        }
                    });*//*
                }*/


                Log.i(TCP_DEBUG, "?????????????????????????????????????????????" + tcpResponse + ",??????:" + tcpResponse.length() + "?????????:" + i);

                //  ???????????? ????????????????????? type ??? data ????????? { ??????
                if(tcpResponse.startsWith("{") && tcpResponse.contains("type") && tcpResponse.contains("data") && tcpResponse.endsWith("}")){
                    try {
                        JSONObject jsonObject = new JSONObject(tcpResponse);
                        String type = jsonObject.getString("type");
                        final String data = jsonObject.getString("data");

                        //  ????????????
                        if(type.equals("connect_rz_msg")){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {

                                    Map<String,String> map = new HashMap<>();
                                    map.put("tcp_client_id",tcp_client_id);
                                    NetWorkUtil.getInstance().doPost(ServerAddress.REGISTER_TCP, map, new NetWorkUtil.NetWorkListener() {
                                        @Override
                                        public void success(String response) {

                                            //NetWorkUtil.getInstance().errorUpload("TCP ?????? connect_rz_msg" + response);

                                            Log.i(TCP_DEBUG,"TCP????????????:" + response);

                                            if(response.contains("???????????????tcp??????")){

                                            }else if(response.contains("???????????????????????????ID")){

                                            }

                                        }

                                        @Override
                                        public void fail(Call call, IOException e) {
                                            Log.i(TCP_DEBUG,"TCP??????????????????" + e.getMessage());
                                            //NetWorkUtil.getInstance().errorUpload("TCP ?????? connect_rz_msg" + e.getMessage());
                                        }

                                        @Override
                                        public void error(Exception e) {
                                            Log.i(TCP_DEBUG,"TCP??????????????????" + e.getMessage());
                                            //NetWorkUtil.getInstance().errorUpload("TCP ?????? connect_rz_msg" + e.getMessage());
                                        }
                                    });

                                }
                            });
                        }else if(type.equals("client_connect_msgect_msg")){
                            //  ?????????????????? ??? ??????

                            //NetWorkUtil.getInstance().errorUpload("TCP ?????? client_connect_msgect_msg");

                            //  TCP ?????????bug
                            if(System.currentTimeMillis() - lastBindTime < 1000){
                                Log.i(TCP_DEBUG,"TCP?????????????????????1s???????????????????????????????????????????????????????????????");
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

                            Log.i(TCP_DEBUG,"??????TCP????????????" + gson.toJson(verify));

                            TCPConnectUtil.getInstance().sendData(gson.toJson(verify));


                            TCPVerifyResponse tcpVerify = gson.fromJson(data, TCPVerifyResponse.class);
                            tcp_client_id = tcpVerify.getClient_id();

                        }else if(type.equals("buy_success_msg")){
                            //  ?????? ????????????
                            BuySuccessMsg buySuccessMsg = gson.fromJson(data,BuySuccessMsg.class);

                            EventBus.getDefault().post(buySuccessMsg);
                        }else if(type.equals("QrReturn")){

                            //  ???????????????????????????????????????
                            if(System.currentTimeMillis() - QRReturnTime < 1000){
//                                return;
                            }

                            VXLoginCall vxLoginCall = gson.fromJson(data,VXLoginCall.class);
                            //  ???????????????????????????id
                            APP.userId = vxLoginCall.getInfo().getUser_id();
                            //  ????????????????????????
                            APP.userType = vxLoginCall.getInfo().getUser_type();


                            //  ?????????????????????
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                                        qrCodeDialog.dismiss();
                                    }
                                }
                            });

                            //  ????????????????????????????????????????????????????????????
                            if(vxLoginCall.isFeatrue_state() && vxLoginCall.isFace_image_state()){

                                UserMessage userMessage = DataBaseUtil.getInstance(MainActivity.this)
                                        .getDaoSession().getUserMessageDao().queryBuilder()
                                        .where(UserMessageDao.Properties.UserId.eq(vxLoginCall.getInfo().getUser_id()))
                                        .unique();
                                //  ???????????????????????????????????????????????????????????????????????????????????????????????????
                                if(userMessage != null){
                                    //  ????????????????????????????????????
                                    mFacePassHandler.deleteFace(userMessage.getFaceToken().getBytes());
                                    //  ????????????????????????????????????
                                    DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getUserMessageDao().delete(userMessage);
                                    Log.i(NOW_TAG,"???????????????????????????????????????");
                                }else{
                                    Log.i(NOW_TAG,"?????????????????????????????????");
                                }


                                Log.i(NOW_TAG,"???????????? ????????????????????? ??? ????????????");
                                //  ????????????????????????????????? ( ?????????????????? Base64 ?????? )
                                byte[] feature = Base64.decode(vxLoginCall.getInfo().getFeatrue(),Base64.DEFAULT);

                                Log.i("???????????????","??????:" + feature.length);

                                FacePassFeatureAppendInfo facePassFeatureAppendInfo = new FacePassFeatureAppendInfo();
                                //  ??????????????????????????????faceToken ??????????????????????????????????????????
                                String faceToken = mFacePassHandler.insertFeature(feature,facePassFeatureAppendInfo);
                                //  faceToken ????????????
                                boolean bindResult = mFacePassHandler.bindGroup(group_name, faceToken.getBytes());
                                //  ????????????????????? ??? faceToken ??? id ???????????????

                                if(bindResult){

                                    //  ??????QRReturn??????
                                    QRReturnTime = System.currentTimeMillis();

                                    Log.i(NOW_TAG,"?????????????????????????????????");
                                    //  faceToken ?????????id ??????
                                    DataBaseUtil.getInstance(MainActivity.this).insertUserIdAndFaceToken(app.getUserId(),faceToken);

                                    //  ???????????????????????????
                                    goControlActivity();

                                }else{
                                    Log.i(NOW_TAG,"???????????????????????????");
                                    //  ????????????????????????????????????
                                    mFacePassHandler.deleteFace(faceToken.getBytes());
                                }
                            }else{
                                //   ?????? ?????????????????? ?????? ???????????????????????????????????????
                                Log.i(NOW_TAG,"????????????????????????????????????????????????????????????");

                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showVerifyFail();
                                    }
                                });

                            }

                        }else if(type.equals("GQrReturn")){
                            //  ????????????????????????
                            GQrReturnBean gQrReturnBean = gson.fromJson(data,GQrReturnBean.class);
                            APP.userId = gQrReturnBean.getInfo().getUser_id();
                            APP.userType = gQrReturnBean.getInfo().getUser_type();
                            goControlActivity();
                        }else if(type.equals("nfcActivity")){
                            NfcActivityBean nfcActivityBean = gson.fromJson(data,NfcActivityBean.class);
                            //  nfc ????????????
                            if(nfcActivityBean.getData().getCode() == 1){
                                //  ????????????id
                                APP.setUserId(nfcActivityBean.getData().getInfo().getUser_id());
                                APP.userType = nfcActivityBean.getData().getInfo().getUser_type();

                                //  ???????????????????????????
                                goControlActivity();

                            }
                        }else if(type.equals("nearByFeatrueSend")){
                            NearByFeatrueSendBean nearByFeatrueSendBean = gson.fromJson(data,NearByFeatrueSendBean.class);



                            if(nearByFeatrueSendBean != null && nearByFeatrueSendBean.getFeatrue() != null){
                                Log.i("????????????",nearByFeatrueSendBean.toString());
                                //  ????????????????????????????????? ( ?????????????????? Base64 ?????? )
                                byte[] feature = Base64.decode(nearByFeatrueSendBean.getFeatrue(),Base64.DEFAULT);

                                FacePassFeatureAppendInfo facePassFeatureAppendInfo = new FacePassFeatureAppendInfo();
                                //  ??????????????????????????????faceToken ??????????????????????????????????????????
                                String faceToken = mFacePassHandler.insertFeature(feature,facePassFeatureAppendInfo);
                                //  facetoken ????????????
                                boolean bindResult = mFacePassHandler.bindGroup(group_name, faceToken.getBytes());
                                //  ????????????????????? ??? facetoken ??? id ???????????????

                                if(bindResult) {
                                    //  facetoken ?????????id ??????
                                    DataBaseUtil.getInstance(MainActivity.this).insertUserIdAndFaceToken(nearByFeatrueSendBean.getUser_id(), faceToken);
                                }

                            }else{
                            }
                        }else if(type.equals("deleteAllFace")){
                            //  ????????????????????????
                            DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getUserMessageDao().deleteAll();
                            //  ???????????????
                            mFacePassHandler.clearAllGroupsAndFaces();
                            //  ??????
                            AndroidDeviceSDK.reBoot(MainActivity.this);
                        }else if(type.equals("updateAllUserType0")){
                            //  ???????????????????????? ??? 0
                            List<UserMessage> userMessageList = DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getUserMessageDao().queryBuilder().list();

                            for(UserMessage userMessage : userMessageList){
                                userMessage.setUserType(0);
                            }

                            DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getUserMessageDao().updateInTx(userMessageList);

                            NetWorkUtil.getInstance().errorUpload("?????????????????????????????? 0 ");

                            Log.i("updateAllUserType0","????????????");
                        }else if(type.equals("reboot")){
                            AndroidDeviceSDK.reBoot(MainActivity.this);
          
                        }else if(/*type.equals("addFaceImage")*/false){  //  ???????????????????????????????????????

                            Log.i("addFaceImage","??????????????????");

                            JsonObject addFaceImageJsonObject = JsonParser.parseString(data).getAsJsonObject();


                            int userId = addFaceImageJsonObject.get("userId").getAsInt();
                            int userType = addFaceImageJsonObject.get("userType").getAsInt();
                            String imageUrl = addFaceImageJsonObject.get("imageUrl").getAsString();

                            APP.userId = userId;
                            APP.userType = userType;

                            Log.i("addFaceImage",addFaceImageJsonObject.toString());

                            Log.i("addFaceImage",userId+","+userType+","+imageUrl);


                            //  ??????????????????????????????????????????
                            UserMessage userMessage = DataBaseUtil.getInstance(MainActivity.this)
                                    .getDaoSession().getUserMessageDao().queryBuilder()
                                    .where(UserMessageDao.Properties.UserId.eq(userId))
                                    .unique();
                            //  ???????????????????????????????????????????????????????????????????????????????????????????????????
                            if(userMessage != null){
                                //  ????????????????????????????????????
                                mFacePassHandler.deleteFace(userMessage.getFaceToken().getBytes());
                                //  ????????????????????????????????????
                                DataBaseUtil.getInstance(MainActivity.this).getDaoSession().getUserMessageDao().delete(userMessage);
                                Log.i("addFaceImage","???????????????????????????????????????" + userMessage.getUserId());
                            }else{
                                Log.i("addFaceImage","?????????????????????????????????" + userId);
                            }


                            downloadFaceImage(imageUrl,userId, userType);
                        }

                    }catch (Exception e){
                        e.printStackTrace();

                        Log.i(TCP_DEBUG, "???????????????" + e.getMessage());

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showToast("???????????????????????????", Toast.LENGTH_SHORT,false,null);
                            }
                        });
                    }
                }else{
                    Log.i(TCP_DEBUG,"??????????????????????????? TCP ?????????" + tcpResponse);
                }

            }

            @Override
            public void onClientStatusConnectChanged(int i, int i1) {

                //  ????????????   0???0
                //  ??????????????? 1 , 0

                //  ????????????????????? -1 , 0   NettyClientHandler: exceptionCaught
                Log.i(TCP_DEBUG,"TCP???????????? i : " + i + " , " + i1);
                if(i == 1){
                    //NetWorkUtil.getInstance().errorUpload("TCP????????????");
                    Log.i(TCP_DEBUG,"????????????");
                }else{
                    //  ????????????
                    /*TCPConnectUtil.getInstance().disconnect();
                    TCPConnectUtil.getInstance().connect();*/

                    //TCPConnectUtil.getInstance().reconnect();

                    //Log.i("Netty","????????????");
                }

                //  ???????????? ???????????????
                /*if(i == 0 || i == -1){

                    NetWorkUtil.getInstance().errorUpload("????????????TCP??????,??????: " + System.currentTimeMillis() + "????????? i :" + i + ", i1???"+ i1 );

                    VoiceUtil.getInstance().openAssetMusics(MainActivity.this,"no_work_time.aac");

                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"??????????????????",Toast.LENGTH_LONG).show();

                            TCPConnectUtil.getInstance().disconnect();
                            TCPConnectUtil.getInstance().connect();
                        }
                    },3000);
                }*/
            }
        });

    }



    /**
     * ??????????????????
     * */
    private synchronized void downloadFaceImage(String imageUrl,final long userId,long userType) throws Exception{


        Log.i("addFaceImage","????????????????????????");
        //  ??????????????????
        DownloadUtil.get().download(imageUrl, Environment.getExternalStorageDirectory().toString(),
                System.currentTimeMillis() + ".jpg", new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(File file) {

                        Log.i("addFaceImage","????????????");
                        try {



                            //  ?????????????????????
                            FacePassExtractFeatureResult facePassExtractFeatureResult =
                                    mFacePassHandler.extractFeature(BitmapFactory.decodeFile(file.getAbsolutePath()));
                            //  ?????????????????????
                            if(facePassExtractFeatureResult.result == 0){

                                FacePassFeatureAppendInfo facePassFeatureAppendInfo = new FacePassFeatureAppendInfo();

                                //  ?????? faceToken
                                String faceToken = mFacePassHandler.insertFeature(facePassExtractFeatureResult.featureData,facePassFeatureAppendInfo);

                                nowFaceToken = faceToken;

                                boolean bindResult = mFacePassHandler.bindGroup(group_name, faceToken.getBytes());

                                //  ????????????
                                if(bindResult){
                                    Log.i("addFaceImage",userId + ",????????????" + faceToken);
                                    //  ????????????
                                    DataBaseUtil.getInstance(MainActivity.this).insertUserIdAndFaceTokenThread(userId,userType,nowFaceToken);


                                    Map<String,String> hasMap = new HashMap<>();
                                    hasMap.put("user_id",String.valueOf(userId));
                                    hasMap.put("device_id",APP.getDeviceId());
                                    //  ??????????????????
                                    NetWorkUtil.getInstance().doPost(ServerAddress.POST_FACE_REGISTER_SUCCESS_LOG, hasMap, new NetWorkUtil.NetWorkListener() {
                                        @Override
                                        public void success(String response) {
                                            Log.i("addFaceImage","???????????????" + response);
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
                                    Log.i("addFaceImage",userId + ",????????????");
                                }
                            }else{
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "???????????????", Toast.LENGTH_SHORT).show();
                                        file.delete();
                                    }
                                });
                            }
                        }catch (Exception e){
                            Log.i("addFaceImage",e.getMessage());
                        }finally {
                            //  ????????????
                            //  file.delete();
                        }



                    }

                    @Override
                    public void onDownloading(int progress) {

                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        Log.i("addFaceImage","????????????"+e.getMessage());
                    }
                });

    }



    //  ????????????????????????
    private void initFaceHandler() {

        new Thread() {
            @Override
            public void run() {
                while (true && !isFinishing()) {
                    while (FacePassHandler.isAvailable()) {
                        Log.d(DEBUG_TAG, "start to build FacePassHandler");
                        FacePassConfig config;
                        try {
                            /* ???????????????????????? */
                            config = new FacePassConfig();
                            config.poseBlurModel = FacePassModel.initModel(getApplicationContext().getAssets(), "attr.pose_blur.align.av200.190630.bin");

                            //????????????CPU rgb????????????
                            config.livenessModel = FacePassModel.initModel(getApplicationContext().getAssets(), "liveness.CPU.rgb.int8.D.bin");
                            //????????????CPU rgbir????????????
                            config.rgbIrLivenessModel = FacePassModel.initModel(getApplicationContext().getAssets(), "liveness.CPU.rgbir.int8.D.bin");
                            //????????????????????????????????????GPU???????????????????????????livenessGPUCache
                            config.livenessGPUCache = FacePassModel.initModel(getApplicationContext().getAssets(), "liveness.GPU.AlgoPolicy.D.cache");

                            config.searchModel = FacePassModel.initModel(getApplicationContext().getAssets(), "feat2.arm.G.v1.0_1core.bin");
                            config.detectModel = FacePassModel.initModel(getApplicationContext().getAssets(), "detector.arm.D.bin");
                            config.detectRectModel = FacePassModel.initModel(getApplicationContext().getAssets(), "detector_rect.arm.D.bin");
                            config.landmarkModel = FacePassModel.initModel(getApplicationContext().getAssets(), "pf.lmk.arm.D.bin");

                            config.mouthOccAttributeModel = FacePassModel.initModel(getApplicationContext().getAssets(), "attribute.mouth.occ.gray.12M.190930.bin");
                            config.smileModel = FacePassModel.initModel(getApplicationContext().getAssets(), "attr.smile.mgf29.0.1.1.181229.bin");
                            config.ageGenderModel = FacePassModel.initModel(getApplicationContext().getAssets(), "attr.age_gender.surveillance.nnie.av200.0.1.0.190630.bin");
                            config.occlusionFilterModel = FacePassModel.initModel(getApplicationContext().getAssets(), "occlusion.all_attr_configurable.occ.190816.bin");
                            //?????????????????????????????????????????????smileModel???ageGenderModel?????????null
                            config.smileModel = null;
                            config.ageGenderModel = null;

                            //config.occlusionFilterEnabled = true;
                            config.mouthOccAttributeEnabled = true;
                            config.searchThreshold = 71f;    //???????????????????????????????????????
                            config.searchExtThreshold = 64.71f; //????????????????????????????????????
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

                            /* ??????SDK?????? */
                            mFacePassHandler = new FacePassHandler(config);

                            FacePassConfig addFaceConfig = mFacePassHandler.getAddFaceConfig();
                            addFaceConfig.blurThreshold = 0.8f;
                            addFaceConfig.faceMinThreshold = 100;
                            mFacePassHandler.setAddFaceConfig(addFaceConfig);

                            checkGroup();
                        } catch (FacePassException e) {
                            e.printStackTrace();
                            Log.d(DEBUG_TAG, "FacePassHandler is null");
                            Log.i(DEBUG_TAG,"?????????" + e.getMessage());
                            //  ??????????????????????????????????????????
                            if(faceCreateResult){
                                NetWorkUtil.getInstance().errorUpload("????????????????????????????????????");
                                faceCreateResult = false;
                            }
                            return;
                        }
                        return;
                    }
                    try {
                        /* ??????SDK?????????????????????????????? */
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

        /* ???????????? */
        if (hasPermission()) {
            manager.open(getWindowManager(), false, cameraWidth, cameraHeight);
        }

        adaptFrameLayout();
        super.onResume();
    }


    /**
     * ???????????????????????????
     * */
    public final static String TAG = "????????????";
    private void goControlActivity(){
        //  ????????? null ???????????????????????????
        if(binsWorkTimeBean == null){
            Log.i(TAG,"???????????? binsWorkTimeBean ??? null");
            NetWorkUtil.getInstance().doGetThread(ServerAddress.GET_BINS_WORK_TIME, null, new NetWorkUtil.NetWorkListener() {
                @Override
                public void success(String response) {
                    Log.i(TAG,"??? null" + response);
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

        //  ??????????????? userType ??? 1 ??? ?????????????????? 0
        Log.i(CARD_DEBUG,String.valueOf(APP.userType));
        //  ??????????????????????????????
        if(APP.userType == 1){
            Log.i(TAG,"??????????????? 1");

            //  ???????????????????????????
            Intent intent = new Intent(MainActivity.this,ControlActivity.class);
            intent.putExtra("userId",app.getUserId());
            startActivityForResult(intent,CONTROL_RESULT_CODE);
        }else{
            Log.i(TAG,"?????????????????? 1");
            //  ???????????????
            if(BinsWorkTimeUntil.getBinsWorkTime(binsWorkTimeBean)){
                Log.i(TAG,"??????????????? ????????????");
                //  ?????????????????????????????????????????????
                Intent intent = new Intent(MainActivity.this,ControlActivity.class);
                intent.putExtra("userId",app.getUserId());
                startActivityForResult(intent,CONTROL_RESULT_CODE);
            }else{
                Log.i(TAG,"??????????????? ????????????");
                //  showToast("?????????????????????????????????", Toast.LENGTH_SHORT, false, null);
                Toast.makeText(MainActivity.this,"???????????????",Toast.LENGTH_LONG).show();
                stringBuilder.delete(0,stringBuilder.length());
                //  ???????????????
                VoiceUtil.getInstance().openAssetMusics(MainActivity.this,"no_work_time.aac");
            }
        }
    }

    AlertDialog icAlert;
    public long icCardTime;
    private final static String CARD_DEBUG = "ic?????????";
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void icCard(final ICCard icCard){

        Log.i(CARD_DEBUG,"????????????");

        if(System.currentTimeMillis() - icCardTime < 2000){
            Toast.makeText(MainActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
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
                Log.i(CARD_DEBUG,"????????????" + response);
                GetNfcUserBean getNfcUserBean = gson.fromJson(response,GetNfcUserBean.class);
                if(getNfcUserBean.getCode() == 1){
                    if(getNfcUserBean.getData().getState() == 1){

                        if(!APP.controlActivityIsRun){
                            //  ??????????????????
                            //  ????????????id
                            APP.setUserId(getNfcUserBean.getData().getUser_id());
                            APP.userType = getNfcUserBean.getData().getUser_type();

                            //  ???????????????????????????
                            goControlActivity();
                        }else{
                            Toast.makeText(MainActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        VoiceUtil.getInstance().openAssetMusics(MainActivity.this,"bind_ic_voice.aac");
                        //  ????????????????????????????????????
                        //  ???????????????
                        if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                            qrCodeDialog.dismiss();
                        }


                        //  ?????????????????? ?????? ic???????????????
                        if(icAlert != null && icAlert.isShowing()){
                            icAlert.dismiss();
                        }

                        ImageView imageView = new ImageView(MainActivity.this);
                        int padding = 50;
                        imageView.setPadding(padding,padding,padding,padding);
                        imageView.setImageBitmap(QRCodeUtil.getICLogin(ServerAddress.IC_BING + "?device_id=" + APP.getDeviceId() + "&card_code=" + icCard.getCardCode()));


                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setCancelable(true);
                        alert.setTitle("???????????????????????????????????? IC ??? .");
                        alert.setView(imageView);
                        alert.create();
                        icAlert = alert.show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "???????????????" + icCard.getCardCode(), Toast.LENGTH_SHORT).show();
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

    //  ????????????????????????
    private void checkGroup() {
        if (mFacePassHandler == null) {
            return;
        }
        //  ??????????????????
        String[] localGroups = mFacePassHandler.getLocalGroups();
        isLocalGroupExist = false;

        //  ????????? null ????????????
        if (localGroups == null || localGroups.length == 0) {

            //  ??????????????????????????????????????????
            try {
                mFacePassHandler.createLocalGroup(group_name);
                isLocalGroupExist = true;
            }catch (Exception e){
                e.printStackTrace();
            }

            return;
        }

        //  ???????????????????????????????????????
        for (String group : localGroups) {
            if (group_name.equals(group)) {
                isLocalGroupExist = true;
            }
        }
        if (!isLocalGroupExist) {

            //  ??????????????????????????????????????????
            try {
                mFacePassHandler.createLocalGroup(group_name);
                isLocalGroupExist = true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    /* ?????????????????? */
    @Override
    public void onPictureTaken(CameraPreviewData cameraPreviewData) {
        mFeedFrameQueue.offer(cameraPreviewData);


        //  Log.i(DEBUG_TAG, "feedframe");
    }

    //  ????????????????????????????????????
    private class FeedFrameThread extends Thread {
        boolean isInterrupt;
        @Override
        public void run() {
            //  ????????????
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
                /* ????????????????????????SDK???????????????????????? FacePassImage */
                long startTime = System.currentTimeMillis(); //????????????

                FacePassImage image;
                try {
                    image = new FacePassImage(cameraPreviewData.nv21Data, cameraPreviewData.width, cameraPreviewData.height, cameraRotation, FacePassImageType.NV21);
                } catch (FacePassException e) {
                    e.printStackTrace();
                    Log.e(MY_TAG,"?????????invalid??????");
                    continue;
                }

                /* ????????????FacePassImage ??????SDK????????? ????????????????????? */
                FacePassDetectionResult detectionResult = null;
                try {

                    detectionResult = mFacePassHandler.feedFrame(image);
                } catch (FacePassException e) {
                    Log.e(MY_TAG,"??????????????????");
                    e.printStackTrace();
                }

                if (detectionResult == null || detectionResult.faceList.length == 0) {
                    /* ??????????????????????????? */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            faceView.clear();
                            faceView.invalidate();
                        }
                    });
                } else {
                    /* ????????????????????????????????????????????????????????????????????????????????????????????? */
                    final FacePassFace[] bufferFaceList = detectionResult.faceList;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showFacePassFace(bufferFaceList);
                        }
                    });
                }

                //  ???????????????????????????????????? detectionResult ?????????????????????????????????????????? FacePassRecognitionResult ???????????? faceToken ???faceToken???????????????id???
                if (SDK_MODE == FacePassSDKMode.MODE_ONLINE) {
                    /*???????????????*/
                    if (detectionResult != null && detectionResult.message.length != 0) {
                        /* ??????http?????? */
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
                                    /* ?????????????????????????????????SDK????????????????????????????????? */
                                    FacePassRecognitionResult[] result = null;
                                    try {
                                        Log.i("lengthlength", "length is " + jsresponse.getString("data").getBytes().length);
                                        result = mFacePassHandler.decodeResponse(jsresponse.getString("data").getBytes());
                                    } catch (FacePassException e) {
                                        Log.e(MY_TAG,"??????????????????????????????");
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
                                //  ????????????

                                if(error.getMessage() != null){
                                    Log.i(MY_TAG, "?????????????????? ???" + error.getMessage());
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
                    /*???????????????????????????????????????message????????????result????????????????????????*/
                    if (detectionResult != null && detectionResult.message.length != 0) {
                        Log.d(DEBUG_TAG, "mDetectResultQueue.offer");

                        //  FacePassRecognitionResult


                        Log.i(MY_TAG,"??????????????????");

                        //  ??????
                        //  Log.i(MY_TAG,detectionResult.feedback[0].trackId+"");




                        FacePassDetectionFeedback[] facePassDetectionResult = detectionResult.feedback;

                        if(facePassDetectionResult != null && facePassDetectionResult.length != 0){
                            for(FacePassDetectionFeedback f : facePassDetectionResult){

                            }
                        }else{
                            Log.i(MY_TAG,"facePassDetectionResult == null && facePassDetectionResult.length == 0");
                        }



                        /*String faceToken = new String(detectionResult.message);
                        Log.i(MY_TAG, "??????????????? detectionResult.message " + faceToken);*/




                        mDetectResultQueue.offer(detectionResult.message);
                    }
                }
                long endTime = System.currentTimeMillis(); //????????????
                long runTime = endTime - startTime;
                //  Log.i("]time", String.format("feedfream %d ms", runTime));


                //  ?????? 30 s???????????????????????? ?????????????????????
                //  ???????????????
                /*if(autoIntentAdvertising & endTime - lastFaceTime > (300 * 1000)){
                    autoIntentAdvertising = false;

                    Intent intent = new Intent(MainActivity.this,AdvertisingActivity.class);
                    startActivityForResult(intent,INTENT_ADVERTISING_CODE); //  ???????????????????????????????????????????????????????????????????????????????????? 30 s???????????????????????????

                }*/
            }
        }
        @Override
        public void interrupt() {
            isInterrupt = true;
            super.interrupt();
        }
    }



    //  ?????? trackId ????????????????????????
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

    //  ????????????????????????
    private class RecognizeThread extends Thread {

        boolean isInterrupt;

        @Override
        public void run() {
            while (!isInterrupt) {
                try {
                    byte[] detectionResult = mDetectResultQueue.take();

                    //  ?????????????????????
                    FacePassAgeGenderResult[] ageGenderResult = null;
                    //if (ageGenderEnabledGlobal) {
                    //    ageGenderResult = mFacePassHandler.getAgeGender(detectionResult);
                    //    for (FacePassAgeGenderResult t : ageGenderResult) {
                    //        Log.e("FacePassAgeGenderResult", "id " + t.trackId + " age " + t.age + " gender " + t.gender);
                    //    }
                    //}

                    Log.i(MY_TAG,"????????????????????????????????????");

                    Log.d(DEBUG_TAG, "mDetectResultQueue.isLocalGroupExist");

                    //  ??????????????????
                    if (isLocalGroupExist) {
                        Log.d(DEBUG_TAG, "mDetectResultQueue.recognize");

                        Log.i(MY_TAG,"isLocalGroupExist ??? true ??? ???????????????");

                        //  ?????? ?????????????????????FacePassRecognitionResult?????????????????????????????????????????????????????????
                        FacePassRecognitionResult[] recognizeResult = mFacePassHandler.recognize(group_name, detectionResult);



                        //  ???????????????????????????????????????
                        if (recognizeResult != null && recognizeResult.length > 0) {
                            Log.i(MY_TAG,"??????????????????");

                            for (FacePassRecognitionResult result : recognizeResult) {

                                String faceToken = new String(result.faceToken);


                                nowFaceToken = faceToken;

                                Log.i("addFaceImage","faceToken ??????????????????????????????"  + faceToken);


                                //  ??????faceToken ?????????????????????????????????????????? userId????????????????????????????????????????????????

                                if (FacePassRecognitionResultType.RECOG_OK == result.facePassRecognitionResultType) {
                                    getFaceImageByFaceToken(result.trackId, faceToken);
                                }

                                int idx = findidx(ageGenderResult, result.trackId);
                                //  -1?????????????????????????????? ????????? ageGenderResult (??????????????????) ??? null
                                if (idx == -1) {

                                    //  ??????????????? Toast
                                    showRecognizeResult(result.trackId, result.detail.searchScore, result.detail.livenessScore, !TextUtils.isEmpty(faceToken));
                                } else {
                                    //  ???????????? Toast
                                    showRecognizeResult(result.trackId, result.detail.searchScore, result.detail.livenessScore, !TextUtils.isEmpty(faceToken), ageGenderResult[idx].age, ageGenderResult[idx].gender);
                                }
                            }
                        }else{
                            //  ?????????????????????,???????????????????????????????????????
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(NOW_TAG, "????????????????????????????????????????????????");
                                    showQRCodeDialog();
                                    //  showVerifyFail();
                                }
                            });
                            Log.i(MY_TAG,"?????????????????? recognizeResult != null && recognizeResult.length > 0) ??? false");
                        }
                    }else{
                        Log.i(MY_TAG,"isLocalGroupExist ??? false ?????????????????????");
                    }
                } catch (InterruptedException | FacePassException e) {
                    Log.e(MY_TAG,"??????getFaceImageByFaceToken?????????RecognizeThread.run");
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

    //  debug ???????????? ??????????????????????????????
    private void showRecognizeResult(final long trackId, final float searchScore, final float livenessScore, final boolean isRecognizeOK) {
        mAndroidHandler.post(new Runnable() {
            @Override
            public void run() {
                if (searchScore < 64) {
                    //showToast("ID = " + String.valueOf(trackId), Toast.LENGTH_SHORT, false, null);    ??????????????????
                    Log.i(MY_TAG,"searchScore : " + searchScore + ",livenessScore : " + livenessScore + "???????????????" );
                    //  ???????????????????????????
                    Log.i(NOW_TAG, "?????????????????????????????? 64 ???????????????????????????");
                    showQRCodeDialog();
                    //  showVerifyFail();
                }else{
                    showToast("ID = " + nowFaceToken, Toast.LENGTH_SHORT, true, null);
                    Log.i(MY_TAG,"searchScore : " + searchScore + ",livenessScore : " + livenessScore + "????????????" );

                    queryFaceToken(nowFaceToken);
                }

                faceEndTextView.append("ID = " + trackId + (isRecognizeOK ? "????????????" : "????????????") + "\n");
                faceEndTextView.append("????????? = " + searchScore + "\n");
                faceEndTextView.append("????????? = " + livenessScore + "\n");
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }



    //  debug ???????????? ??????????????????????????????
    private void showRecognizeResult(final long trackId, final float searchScore, final float livenessScore, final boolean isRecognizeOK, final float age, final int gender) {
        mAndroidHandler.post(new Runnable() {
            @Override
            public void run() {

                faceEndTextView.append("ID = " + trackId + (isRecognizeOK ? "????????????" : "????????????") + "\n");
                faceEndTextView.append("????????? = " + searchScore + "\n");
                faceEndTextView.append("????????? = " + livenessScore + "\n");
                faceEndTextView.append("?????? = " + age + "\n");


                if (gender == 0) {
                    faceEndTextView.append("?????? = " + "???" + "\n");
                } else if (gender == 1) {
                    faceEndTextView.append("?????? = " + "???" + "\n");
                } else {
                    faceEndTextView.append("?????? = " + "unknown" + "\n");
                }
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    /* ????????????????????????????????? android22??????????????????????????? */
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

    /* ???????????????????????? */
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
                        Toast.makeText(getApplicationContext(), "?????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
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

    //  ???????????????
    private void initView() {
        //  ????????????????????????
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

        //  ????????????
        SharedPreferences preferences = getSharedPreferences(SettingVar.SharedPrefrence, Context.MODE_PRIVATE);
        //  ??????????????????
        SettingVar.isSettingAvailable = preferences.getBoolean("isSettingAvailable", SettingVar.isSettingAvailable);
        //  ?????????????????????
        SettingVar.isCross = preferences.getBoolean("isCross", SettingVar.isCross);

        SettingVar.faceRotation = preferences.getInt("faceRotation", SettingVar.faceRotation);
        SettingVar.cameraPreviewRotation = preferences.getInt("cameraPreviewRotation", SettingVar.cameraPreviewRotation);
        SettingVar.cameraFacingFront = preferences.getBoolean("cameraFacingFront", SettingVar.cameraFacingFront);

        //  ???????????????????????????????????????
        if (SettingVar.isSettingAvailable) {
            cameraRotation = SettingVar.faceRotation;
            cameraFacingFront = SettingVar.cameraFacingFront;
        }

        Log.i(DEBUG_TAG, "Rotation: screenRotation: " + String.valueOf(windowRotation));
        Log.i(DEBUG_TAG, "Rotation: faceRotation: " +  SettingVar.faceRotation);
        Log.i(DEBUG_TAG, "Rotation: new cameraRation: " + cameraRotation);

        //  ??????????????????
        final int mCurrentOrientation = getResources().getConfiguration().orientation;
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            screenState = 1;
        } else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            screenState = 0;
        }


        setContentView(R.layout.activity_main);


        TabLayout tabLayout = findViewById(R.id.login_type_tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("????????????"));
        tabLayout.addTab(tabLayout.newTab().setText("????????????"));
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
                if("????????????".equals(num)){

                    if(phoneNumberText.getText().length() != 11){
                        toast("?????????????????????????????????");
                        return;
                    }

                    Map<String,String> hasMap = new HashMap<>();
                    hasMap.put("phone",phoneNumberText.getText().toString());
                    NetWorkUtil.getInstance().doPost(ServerAddress.PHONE_LOGIN, hasMap, new NetWorkUtil.NetWorkListener() {
                        @Override
                        public void success(String response) {

                            //  ??????????????????????????????
                            PhoneLoginBean phoneLoginBean = gson.fromJson(response,PhoneLoginBean.class);
                            if(phoneLoginBean.getCode() == 1){
                                APP.userId = phoneLoginBean.getData().getUser_id();
                                APP.userType = phoneLoginBean.getData().getUser_type();

                                if(phoneLoginDialog != null && phoneLoginDialog.isShowing()){
                                    phoneLoginDialog.dismiss();
                                }

                                phoneNumberText.setText(null);

                                toast("????????????");
                                goControlActivity();
                            }else{
                                toast("???????????????????????????????????????????????????");
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
                Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                phoneNumberText.setText("");
            }
        });


        //  ?????????????????????????????????
        mSyncGroupBtn = (ImageView) findViewById(R.id.btn_group_name);
        mSyncGroupBtn.setOnClickListener(this);

        mFaceOperationBtn = (ImageView) findViewById(R.id.btn_face_operation);
        mFaceOperationBtn.setOnClickListener(this);

        //  ????????????
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
        /* ??????????????? */
        faceEndTextView = (TextView) this.findViewById(R.id.tv_meg2);
        faceEndTextView.setTypeface(tf);
        faceView = (FaceView) this.findViewById(R.id.fcview);
        settingButton = (Button) this.findViewById(R.id.settingid);

        //  ???????????? 5 ??? ????????????????????????????????????????????????????????? 0.6s????????????????????????
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long curTime = System.currentTimeMillis();
                long durTime = curTime - mLastClickTime;
                mLastClickTime = curTime;
                if (durTime < CLICK_INTERVAL) {
                    ++mSecretNumber;
                    if (mSecretNumber == 5) {

                        Log.i("Netty","????????????");
                        //  ?????????????????????
                        //TCPConnectUtil.getInstance().disconnect();
                        Log.i("Netty","????????????");
                        //TCPConnectUtil.getInstance().connect();
                        Toast.makeText(MainActivity.this,"??????",Toast.LENGTH_LONG).show();



                        final String number = getrandom();
                        //  ????????????
                        //NetWorkUtil.getInstance().errorUpload("????????????????????????:" + number);


                        adminLoginDialog = new AdminLoginDialog(MainActivity.this);
                        adminLoginDialog.setLoginListener(new AdminLoginDialog.LoginListener() {
                            @Override
                            public void callBack(String editStr,String password,AlertDialog alertDialog) {

                                if(editStr.equals(number)){
                                    if(password.equals("123456")){
                                        AndroidDeviceSDK.reBoot(MainActivity.this);
                                    }else if(password.equals("99")){

                                        Log.i("Netty","????????????");
                                        //  ?????????????????????
                                        //TCPConnectUtil.getInstance().disconnect();
                                        Log.i("Netty","????????????");
                                        //TCPConnectUtil.getInstance().connect();

                                    }else if(password.equals("1")){
                                         AndroidDeviceSDK.hideStatus(MainActivity.this,false);
                                    }else if(password.equals("2")){
                                        int i = 0 / 1;
                                    }else if(password.equals("3")){
                                        Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();
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

        //  debug ??????????????????????????????
        ll = (LinearLayout) this.findViewById(R.id.ll);
        ll.getBackground().setAlpha(100);
        //  debug ??????
        visible = (Button) this.findViewById(R.id.visible);
        visible.setBackgroundResource(R.drawable.debug);
        //  buttonFlag ????????????????????? ??????????????????
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
        /* ???????????????????????? */
        manager.setListener(this);

        //  ???????????????????????? (??????????????????)
        mSDKModeBtn=(Button)findViewById(R.id.btn_mode_switch);
        mSDKModeBtn.setText(SDK_MODE.toString());


        //  ????????????????????? ??? ??????????????????
        cameraView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                showQRCodeDialog();

                //  ??????????????????????????? app ?????????????????????
                AndroidDeviceSDK.hideStatus(MainActivity.this,true);
                //  ?????????????????????
                AndroidDeviceSDK.checkForeground(MainActivity.this,true);


                APP.controlImagePreview = true;

                return false;
            }
        });

        //  ???????????????????????????????????????
        mSDKModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SDK_MODE == FacePassSDKMode.MODE_OFFLINE) {
                    SDK_MODE = FacePassSDKMode.MODE_ONLINE;
                    recognize_url = "http://" + serverIP_online + ":8080/api/service/recognize/v1";
                    serverIP = serverIP_online;
                    mSDKModeBtn.setText("???????????????" +SDK_MODE.toString());
                } else {
                    SDK_MODE = FacePassSDKMode.MODE_OFFLINE;
                    serverIP = serverIP_offline;
                    mSDKModeBtn.setText("???????????????" + SDK_MODE.toString());
                }
            }
        });

    }


    public static String getrandom(){
        String code = "";
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int r = random.nextInt(10); //??????????????????????????????0-9???
            code = code + r;  //???????????????????????????????????????
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


        //  ??????????????? activity ?????????????????? 30 s ????????????
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


    //  ?????????????????????????????????
    private static long lastFaceTime = System.currentTimeMillis();
    //  ???????????? 30 ????????????????????????????????????
    private volatile boolean autoIntentAdvertising  = true;
    //  ?????????????????????
    public final static int INTENT_ADVERTISING_CODE = 600;

    //  ????????????????????????????????????????????????????????????????????????????????????????????? ,??????????????????
    private void showFacePassFace(FacePassFace[] detectResult) {
        faceView.clear();
        for (FacePassFace face : detectResult) {
            Log.d("facefacelist", "width " + (face.rect.right - face.rect.left) + " height " + (face.rect.bottom - face.rect.top) );
            Log.d("facefacelist", "smile " + face.smile);
            boolean mirror = cameraFacingFront; /* ???????????????mirror???true */

            StringBuilder faceIdString = new StringBuilder();
            faceIdString.append("ID = ").append(face.trackId);
            SpannableString faceViewString = new SpannableString(faceIdString);
            faceViewString.setSpan(new TypefaceSpan("fonts/kai"), 0, faceViewString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            StringBuilder faceRollString = new StringBuilder();
            faceRollString.append("??????: ").append((int) face.pose.roll).append("??");
            StringBuilder facePitchString = new StringBuilder();
            facePitchString.append("??????: ").append((int) face.pose.pitch).append("??");
            StringBuilder faceYawString = new StringBuilder();
            faceYawString.append("??????: ").append((int) face.pose.yaw).append("??");
            StringBuilder faceBlurString = new StringBuilder();
            faceBlurString.append("??????: ").append(face.blur);
            StringBuilder smileString = new StringBuilder();
            smileString.append("??????: ").append(String.format("%.6f", face.smile));
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
            //  ????????????
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

            //  ????????????????????????????????????
            lastFaceTime = System.currentTimeMillis();
        }
        faceView.invalidate();
    }




    private String nowFaceToken;
    //  ????????????????????????
    public void showToast(CharSequence text, int duration, boolean isSuccess, Bitmap bitmap) {
        //  ??????????????????????????????????????????????????????????????????
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
            s = new SpannableString("????????????");
            imageView.setImageResource(R.drawable.ic_success);

            //  VoiceUtil.getInstance(MainActivity.this).startAuto("????????????");

            //  ?????????????????????????????????????????????????????????
            if(alertDialog!=null && alertDialog.isShowing()){
                alertDialog.dismiss();
            }


        } else {
            s = new SpannableString("????????????");
            imageView.setImageResource(R.drawable.ic_baseline_error_outline_24);


            Log.i(NOW_TAG, "???????????????showToast ?????? ????????????");
            showQRCodeDialog();
            //  ???????????????????????????
            //  showVerifyFail();

        }
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
        stateView.setText(s);


        //  faceToken ?????????????????????
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
                //  ??????????????????
                showSyncGroupDialog();
                break;
            case R.id.btn_face_operation:
                //  ????????????????????????
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
                    //  ????????????
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
                            //  ????????????
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
            //????????????????????????????????????
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
                        toast("?????????????????????");
                        return;
                    }

                    //  ?????????????????? ???????????????
                    if (!TextUtils.isEmpty(path) && mFaceOperationDialog != null && mFaceOperationDialog.isShowing()) {
                        EditText imagePathEdt = (EditText) mFaceOperationDialog.findViewById(R.id.et_face_image_path);
                        imagePathEdt.setText(path);
                    }
                }
                break;
            case REQUEST_CODE_CAMERA:   //  ????????????--????????????--?????????????????????--????????????

                String TAG = "??????";

                //  ??????????????????
                Bitmap bitmap = BitmapFactory.decodeFile(file.toString());

                //  ??????????????????????????????????????????
                if(bitmap.getWidth() > bitmap.getHeight()){
                    bitmap = adjustPhotoRotation(bitmap,90);
                }

                try {
                    //  ???????????????
                    FacePassExtractFeatureResult facePassExtractFeatureResult = mFacePassHandler.extractFeature(bitmap);

                    //  ?????????????????????
                    if(facePassExtractFeatureResult.result == 0){

                        FacePassFeatureAppendInfo facePassFeatureAppendInfo = new FacePassFeatureAppendInfo();

                        //  ?????? faceToken
                        String faceToken = mFacePassHandler.insertFeature(facePassExtractFeatureResult.featureData,facePassFeatureAppendInfo);

                        nowFaceToken = faceToken;

                        boolean bindResult = mFacePassHandler.bindGroup(group_name, faceToken.getBytes());

                        //  ????????????
                        if(bindResult){
                            Log.i(TAG,"????????????" + faceToken);

                            //  ??????facetoken ?????????id????????????

                            //  ??????????????????,????????????????????????id


                            uploadFace(file,facePassExtractFeatureResult.featureData,app.getUserId());
                        }else{
                            mFacePassHandler.deleteFace(faceToken.getBytes());
                            Log.i(TAG,"????????????");
                        }
                    }else{
                        toast("?????????????????????");
                    }
                }catch (Exception e){
                    Log.i(TAG,e.getMessage());
                }finally {
                    //  ????????????
                    //  file.delete();
                }

                break;
            case INTENT_ADVERTISING_CODE:
                //  ?????????????????????????????????30s????????????
                lastFaceTime = System.currentTimeMillis();

                autoIntentAdvertising = true;

                break;
        }
    }



    /**
     *
     * @param bm ??????????????????
     * @param orientationDegree ????????????
     * ????????????
     * */
    public Bitmap adjustPhotoRotation(Bitmap bm, int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);

        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);

    }




    /**
     *
     * ??????????????????
     * */
    private UserMessageDao initDataBase(){
        //  ???????????????loginRecord.db
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(MainActivity.this,"loginRecord.db");
        //  ?????????????????????
        SQLiteDatabase database = helper.getWritableDatabase();
        //  ?????????????????????
        DaoMaster daoMaster = new DaoMaster(database);
        //  ??????Dao???????????????
        DaoSession mDaoSession = daoMaster.newSession();

        return mDaoSession.getUserMessageDao();
    }


    /**
     * ?????????????????????????????????
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


        //  ?????????????????????????????????
        String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //  ???????????????

        Log.i(NOW_TAG,"???????????????????????? ??????????????? : " + ServerAddress.LOGIN );

        Log.i(NOW_TAG,"???????????????????????? ??????ID : " + APP.getDeviceId());


        //  ???????????? ?????? token
        qr_code_login.setImageBitmap(QRCodeUtil.getAppletLoginCode(ServerAddress.LOGIN + APP.getDeviceId()));


        //  ???????????????????????????????????????????????????????????????TCP?????????????????????id???token
        alert.setPositiveButton("??????????????? ( ????????????????????????????????? )", new DialogInterface.OnClickListener() {
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

                                //  ??????????????????????????????
                                PhoneLoginBean phoneLoginBean = gson.fromJson(response,PhoneLoginBean.class);
                                if(phoneLoginBean.getCode() == 1){
                                    APP.userId = phoneLoginBean.getData().getUser_id();
                                    APP.userType = phoneLoginBean.getData().getUser_type();

                                    phoneLoginDialog.dismiss();

                                    toast("????????????");
                                    goControlActivity();
                                }else{
                                    toast("???????????????????????????????????????????????????");
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



    private final String NOW_TAG = "????????????";

    /**
     * ?????????????????????????????????????????????????????????????????????
     * @param userId ??????id
     * @param file ????????????
     * */
    public void uploadFace(final File file,final byte[] feature,final long userId){
        progressDialog.show();

        OkHttpClient client = new OkHttpClient.Builder().build();

        // ??????????????????????????????????????????
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpg"), file);


        final long nowTime = System.currentTimeMillis() / 1000 ;

        final String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // ??????????????????????????????
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

                Log.i(NOW_TAG,"?????????????????????" + e.getMessage() );


                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        //  ???????????????????????????????????????
                        if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                            qrCodeDialog.dismiss();
                        }

                        if(progressDialog != null && progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }

                        AlertDialog.Builder alertB = new AlertDialog.Builder(MainActivity.this);
                        alertB.setCancelable(false);
                        alertB.setTitle("??????");
                        alertB.setMessage("??????????????????");
                        alertB.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                uploadFace(file,feature,userId);

                            }
                        });
                        alertB.setNegativeButton("??????", new DialogInterface.OnClickListener() {
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

                Log.i(NOW_TAG,"?????????????????????" + imageUploadResult.toString());


                //  ??????????????????
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
                            Log.i(NOW_TAG,"???????????? json ?????????"+response);


                            ResultMould resultMould = new Gson().fromJson(response,ResultMould.class);
                            //  ??????????????????
                            if(resultMould.getCode() == 1){

                                Log.i(NOW_TAG,"???????????? userToken ??? userID ??????");

                                //  ????????????
                                DataBaseUtil.getInstance(MainActivity.this).insertUserIdAndFaceToken(app.getUserId(),nowFaceToken);


                                Log.i(NOW_TAG,app.getUserId() + "?????? " + nowFaceToken);

                                file.delete();

                                //  ????????????????????????????????? ??? ??????
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
                            Log.i(NOW_TAG,"?????????????????? fail???" + e.getMessage());

                            if(progressDialog != null && progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                        }

                        @Override
                        public void error(Exception e) {
                            Log.i(NOW_TAG,"??????????????????error ???" + e.getMessage());

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
     * ?????????????????????faceToken ????????????????????????????????????????????????????????? userId??????????????????????????????????????????????????????????????????????????????
     * @param faceToken ??????????????????????????? faceToken
     * */
    //  ?????????????????????????????????
    private long lastPassTime;
    private void queryFaceToken(String faceToken){


        //  ?????????????????? ??????????????????2s
        if(System.currentTimeMillis() - lastPassTime < 2000){
            Log.i(MY_TAG,"????????????");
            return;
        }




        UserMessage userMessage = userMessageDao.queryBuilder().where(UserMessageDao.Properties.FaceToken.eq(faceToken)).build().unique();

        //  ??????????????????????????????????????????????????????????????????????????????????????????
        if(userMessage != null){

            Log.i(MY_TAG,"?????????????????? null");

            //  ???????????????????????????
            if(qrCodeDialog != null && qrCodeDialog.isShowing()){
                qrCodeDialog.dismiss();
            }

            app.setUserId(userMessage.getUserId());
            APP.userType = userMessage.getUserType();

            if(!isHasShowDialog()){
                lastPassTime = System.currentTimeMillis();
                //  ???????????????????????????
                goControlActivity();

            }

        }else{
            Log.i("addFaceImage","?????????" + faceToken + "???????????????");

            Log.i(MY_TAG,"?????????????????????????????????????????????????????????????????????????????? ?????????????????????");
            //  ???????????????????????????????????????????????????????????????????????????????????? ?????????????????????

            if(!isHasShowDialog()){
                Log.i(NOW_TAG, "?????????????????????????????????????????????????????????????????????????????????");

                List<UserMessage> list = userMessageDao.queryBuilder().list();
                for(UserMessage um : list){
                    Log.i(NOW_TAG, "??????id????????????" + um.toString());
                }

                showQRCodeDialog();
            }

        }
    }


    /**
     * ??????????????????????????????????????????
     * */
    private boolean isHasShowDialog(){

        if(phoneLoginDialog != null && phoneLoginDialog.isShowing()){
            return true;
        }

        //  ??????????????????,????????????????????????
        if(alertDialog!=null && alertDialog.isShowing()){
            return true;
        }

        //  demo ??????????????????
        if (mFaceOperationDialog != null && mFaceOperationDialog.isShowing()) {
            return true;
        }

        //  ?????????
        if(qrCodeDialog != null && qrCodeDialog.isShowing()){
            return true;
        }

        //  ???????????????
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
     * ????????????,????????????????????????
     * */
    private AlertDialog alertDialog;
    //  ??????
    private final static int REQUEST_CODE_CAMERA = 500;
    //  H5 ???????????? ?????????????????????
    private Uri mUri;
    //  ????????????
    private File file;
    private void showVerifyFail(){
        AlertDialog.Builder alertB = new AlertDialog.Builder(MainActivity.this);
        alertB.setCancelable(false);
        alertB.setTitle("??????");
        alertB.setMessage("????????????????????????");
        alertB.setPositiveButton("????????????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //  ??????????????????
                //  ???????????????????????????????????????

                String path = Environment.getExternalStorageDirectory().toString();
                file = new File(path, System.currentTimeMillis() + ".jpg");
                if(!file.getParentFile().exists())
                    file.getParentFile().mkdirs();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //  ????????????Android 7.0????????????????????? Uri
                    mUri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", file);
                } else {
                    //  ????????????????????????Uri
                    mUri = Uri.fromFile(file);
                }
                //  ??????????????????????????????
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);

            }
        });
        alertB.setNegativeButton("??????", new DialogInterface.OnClickListener() {
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
     * ??????faceToken Toast???????????????
     * @param faceToken ???????????????
     * @param trackId ?????????????????????ID???
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
                Log.e(MY_TAG,"??????getFaceImageByFaceToken?????????mFacePassHandler.getFaceImage");
                e.printStackTrace();
            }
        }

        //  ??????
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


    /* ???????????????????????? */
    private void showSyncGroupDialog() {

        if (mSyncGroupDialog != null && mSyncGroupDialog.isShowing()) {
            mSyncGroupDialog.hide();
            requestQueue.cancelAll("handle_sync_request_tag");
        }

        //  ????????????
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //  ????????????
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

        //  ????????????????????????
        closeWindowIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSyncGroupDialog.dismiss();
            }
        });

        //  ????????????????????????
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

        //  ????????????
        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }
                String groupName = groupNameEt.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    toast("please input group name ???");
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

        //  ????????????????????????????????????????????? (?????????????????????????????????)
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

        //  ?????????????????????????????????
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
                    toast("????????????!");
                } else {
                    toast("????????????!");

                }
            }

        });


        //  ???????????????
        mSyncGroupDialog = builder.create();
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();  //????????????????????????
        WindowManager.LayoutParams attributes = mSyncGroupDialog.getWindow().getAttributes();
        attributes.height = d.getHeight();
        attributes.width = d.getWidth();
        mSyncGroupDialog.getWindow().setAttributes(attributes);
        mSyncGroupDialog.show();

    }

    private AlertDialog mFaceOperationDialog;

    //  ????????????????????????
    private void showAddFaceDialog() {
        //  ????????????????????????
        if (mFaceOperationDialog != null && !mFaceOperationDialog.isShowing()) {
            mFaceOperationDialog.show();
            return;
        }
        if (mFaceOperationDialog != null && mFaceOperationDialog.isShowing()) {
            return;
        }


        //  ???????????????
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // ??????
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

        //  ???????????????
        final FaceTokenAdapter faceTokenAdapter = new FaceTokenAdapter();

        //  ???????????????facepass
        groupNameEt.setText(group_name);

        //  ?????? x ????????????
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFaceOperationDialog.dismiss();
            }
        });

        //  ????????????
        choosePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentFromGallery = new Intent(Intent.ACTION_GET_CONTENT);
                intentFromGallery.setType("image/*"); // ??????????????????
                intentFromGallery.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(intentFromGallery, REQUEST_CODE_CHOOSE_PICK);
                } catch (ActivityNotFoundException e) {
                    toast("????????????????????????????????????");
                }
            }
        });

        //  ??????????????????
        addFaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }
                String imagePath = faceImagePathEt.getText().toString();
                if (TextUtils.isEmpty(imagePath)) {
                    toast("?????????????????????????????????");
                    return;
                }

                File imageFile = new File(imagePath);
                if (!imageFile.exists()) {
                    toast("??????????????? ???");
                    return;
                }

                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

                try {
                    //  ????????????????????????????????????????????????
                    FacePassAddFaceResult result = mFacePassHandler.addFace(bitmap);
                    if (result != null) {
                        //  ????????????????????????
                        if (result.result == 0) {
                            toast("add face successfully???");
                            faceTokenEt.setText(new String(result.faceToken));

                            Log.i("??????",new String(result.faceToken));

                            //  ?????????????????????????????????
                        } else if (result.result == 1) {
                            toast("no face ???");

                            //  ?????????????????????
                        } else {
                            toast("quality problem???");
                        }
                    }
                } catch (FacePassException e) {
                    e.printStackTrace();
                    toast(e.getMessage());
                }
            }
        });


        //  ??????????????????
        getFaceImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }
                try {
                    //  ?????? faceToken ????????????
                    byte[] faceToken = faceTokenEt.getText().toString().getBytes();
                    Bitmap bmp = mFacePassHandler.getFaceImage(faceToken);
                    final ImageView iv = (ImageView) findViewById(R.id.imview);
                    iv.setImageBitmap(bmp);
                    //  ??????????????????
                    iv.setVisibility(View.VISIBLE);
                    //  ?????????????????? 2s ?????????
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

        //  ??????faceToken ????????????
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
                            toast("group name  is null ???");
                            return;
                        }
                        //  ??????????????????faceToken ?????????????????????listView ??????
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

                //  ????????????????????? ??? ??????
                String result = b ? "success " : "failed";
                toast("delete face " + result);
                Log.d(DEBUG_TAG, "delete face  " + result);

            }
        });

        //  ????????????
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
                    toast("params error???");
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

        //  ????????????
        getGroupInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFacePassHandler == null) {
                    toast("FacePassHandle is null ! ");
                    return;
                }
                String groupName = groupNameEt.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    toast("group name  is null ???");
                    return;
                }
                try {
                    //  ??????????????????faceToken
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

        //  ??????
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
                    toast("group name  is null ???");
                    return;
                }
                try {
                    byte[] faceToken = faceTokenAdapter.getData().get(position).getBytes();
                    //  ???????????????faceToken
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
                        //  ??????????????????
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
                    toast("group name  is null ???");
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
        Display d = m.getDefaultDisplay();  //????????????????????????
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
     * ???????????????????????? ?????? ????????????
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
     * ?????????????????????
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
                /* ??????????????????jpg???????????????????????? */
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
     * ???????????????????????????
     */
    private LocationClient locationClient;
    private static double latitude ,longitude;
    //??????????????????;
    private void initLocationOption() {

        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        locationClient = new LocationClient(getApplicationContext());
        //??????LocationClient??????????????????????????????
        LocationClientOption locationOption = new LocationClientOption();
        MyLocationListener myLocationListener = new MyLocationListener();
        //??????????????????
        locationClient.registerLocationListener(myLocationListener);
        //?????????????????????????????????????????????????????????????????????????????????
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //???????????????gcj02??????????????????????????????????????????????????????????????????????????????????????????bd09ll;
        locationOption.setCoorType("gcj02");
        //???????????????0?????????????????????????????????????????????????????????????????????????????????1000ms???????????????
        locationOption.setScanSpan(3000);
        //?????????????????????????????????????????????????????????
        locationOption.setIsNeedAddress(true);
        //???????????????????????????????????????
        locationOption.setIsNeedLocationDescribe(true);
        //?????????????????????????????????????????????
        locationOption.setNeedDeviceDirect(false);
        //???????????????false??????????????????gps???????????????1S1???????????????GPS??????
        locationOption.setLocationNotify(false);
        //???????????????true?????????SDK???????????????SERVICE?????????????????????????????????????????????stop?????????????????????????????????????????????
        locationOption.setIgnoreKillProcess(true);
        //???????????????false??????????????????????????????????????????????????????BDLocation.getLocationDescribe?????????????????????????????????????????????????????????
        locationOption.setIsNeedLocationDescribe(true);
        //???????????????false?????????????????????POI??????????????????BDLocation.getPoiList?????????
        locationOption.setIsNeedLocationPoiList(true);
        //???????????????false?????????????????????CRASH?????????????????????
        locationOption.SetIgnoreCacheException(false);
        //???????????????false?????????????????????Gps??????
        locationOption.setOpenGps(true);
        //???????????????false?????????????????????????????????????????????????????????????????????????????????????????????
        locationOption.setIsNeedAltitude(false);
        //??????????????????????????????????????????????????????????????????????????????SDK????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????SDK??????????????????????????????????????????????????????
        locationOption.setOpenAutoNotifyMode();
        //??????????????????????????????????????????????????????????????????????????????SDK???????????????????????????????????????????????????
        locationOption.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
        //??????????????????LocationClientOption???????????????setLocOption???????????????LocationClient????????????
        locationClient.setLocOption(locationOption);
        //????????????
        locationClient.start();
    }


    /**
     * ??????????????????
     */
    public static class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            //?????????BDLocation?????????????????????????????????????????????get??????????????????????????????????????????
            //?????????????????????????????????????????????????????????????????????
            //??????????????????????????????????????????????????????BDLocation???????????????

            Log.i("?????????","????????????" + location.getLongitude() + "," + location.getLatitude());

            if(location.getLatitude() == 0.0 || location.getLongitude() == 0.0){

                return;
            }


            if ("4.9E-324".equals(String.valueOf(location.getLatitude())) || "4.9E-324".equals(String.valueOf(location.getLongitude()))) {
                return;
            }

            //  ??????????????????
            latitude = location.getLatitude();
            //  ??????????????????
            longitude = location.getLongitude();

            Log.i("?????????3","????????? " + latitude + "," + longitude);

            Map<String,String> map = new HashMap<>();
            map.put("longitude",String.valueOf(longitude));
            map.put("latitude",String.valueOf(latitude));
            NetWorkUtil.getInstance().doPost(ServerAddress.UPDATE_DEVICE_LOCATION, map, new NetWorkUtil.NetWorkListener() {
                @Override
                public void success(String response) {
                    Log.i("?????????","?????????????????????" + response);
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
