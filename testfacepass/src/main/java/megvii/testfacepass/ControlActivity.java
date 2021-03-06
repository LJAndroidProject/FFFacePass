package megvii.testfacepass;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
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
import android.view.KeyEvent;
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
import megvii.testfacepass.network.DustBinRecordRequestParams;
import megvii.testfacepass.utils.LogUtil;
import okhttp3.Call;

public class ControlActivity extends AppCompatActivity {
    private static final String TAG = "ControlActivity";
    private Intent intent;

    //  ??????id ??????????????????????????????id?????????????????????????????????
    public static long userId;

    private TextView control_welcome_textView;
    private ImageView control_image;

    //  ?????????
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

    //  ????????????
    public static EXIT_MODE exit_mode = EXIT_MODE.TIME_TASK;

    //  ????????????????????????
    public boolean cameraOpened = false;
    //  ??????????????????????????????

    //  ????????????
    public enum EXIT_MODE {
        //  ????????????
        CLOSE_ITERATION,
        //  ????????????
        TIME_TASK
    }


    //  ??????????????????
    public List<DustbinStateBean> beforeDustbinStateBeans;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i("take picture", "take picture ??????");


        APP.controlActivityIsRun = false;


        //  ???????????????
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

        //  ???????????????
        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeTheDisinfection(1));

        if (isTaskRoot()) {
            Log.i("isTaskRoot", "true");
            finish();
        } else {
            Log.i("isTaskRoot", "false");
        }

        setContentView(R.layout.activity_control);


        //  ?????????????????????
        //initCameraSurfaceView();

        handler = new Handler(Looper.getMainLooper());

        hasMan();


        //  ???????????????????????????
        if (exit_mode == EXIT_MODE.CLOSE_ITERATION) {
            EventBus.getDefault().register(this);

            SerialPortResponseManage.getInstance().setCloseListener(new SerialPortResponseManage.CloseListener() {
                @Override
                public void closeCall(DustbinStateBean dustbinStateBean) {
                    closeDoorCall(dustbinStateBean);
                }
            });

            //setDustbinCallListener();
        } else {
            //  ???????????? ??????
        }


        //  ?????????????????????
        beforeDustbinStateBeans = APP.dustbinBeanList;

        //  ???????????????????????????
        getGoodsPos();

        control_exit_btn = (TextView) findViewById(R.id.control_exit_btn);
        textTueView = (TextureView) findViewById(R.id.textTueView);
        control_image = (ImageView) findViewById(R.id.control_image);
        control_recyclerview = (RecyclerView) findViewById(R.id.control_recyclerview);
        textView = (TextView) findViewById(R.id.textView);
        control_welcome_textView = (TextView) findViewById(R.id.control_welcome_textView);

        /*if(APP.controlImagePreview){
            control_image.setAlpha(0.6f);
        }else{
            control_image.setAlpha(1f);
        }*/


        //  ???????????????
        /*
         * ???????????????????????? ??? ???????????????
         * */
        //  ??????????????????
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


        //  6 ???????????????????????????
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                control_exit_btn.setVisibility(View.VISIBLE);

                //  ?????? 6 s?????????????????????????????????????????????????????????
                /*if(!cameraOpened){
                    Log.i("onCameraOpened","onCameraOpened ??????????????????,?????????????????????");

                }

                if(mCamera != null){
                    //  ????????????
                    takePicture();
                }*/
            }
        }, 6000);


        //  ?????????????????? 5 ??? ????????????????????????
        control_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                long curTime = System.currentTimeMillis();
                long durTime = curTime - mLastClickTime;
                mLastClickTime = curTime;
                if (durTime < CLICK_INTERVAL) {
                    ++mSecretNumber;
                    if (mSecretNumber == 5) {
                        if(APP.phoneCodeVerifyBean != null){
                            phoneCodeVerifyBean = APP.phoneCodeVerifyBean;
                        }
                        //  ???????????????????????? ?????????????????????????????????????????????????????? ???????????????????????????
                        if (phoneCodeVerifyBean != null) {
                            showAdminManage(phoneCodeVerifyBean);
                            return;
                        }

                        //  ??????
                        adminLoginDialog = new AdminLoginDialog(ControlActivity.this);
                        adminLoginDialog.setLoginListener(new AdminLoginDialog.LoginListener() {
                            @Override
                            public void callBack(final String editStr, final String password, android.app.AlertDialog alertDialog) {

                                //  ????????????
                                APP.hasManTime = System.currentTimeMillis();
                                hasManIsRun = true;


                                //  ?????????????????? ??? ?????? ????????????
                                Map<String, String> map = new HashMap<>();
                                map.put("phone", editStr);
                                map.put("pwd", password);
                                NetWorkUtil.getInstance().doPost(ServerAddress.ADMIN_LOGIN, map, new NetWorkUtil.NetWorkListener() {
                                    @Override
                                    public void success(String response) {
                                        //  ????????????
                                        AdminLoginResult adminLoginResult = new Gson().fromJson(response, AdminLoginResult.class);

                                        //  ???????????? + ?????? ????????????
                                        if (adminLoginResult.getCode() == 1) {
                                            Toast.makeText(ControlActivity.this, "???????????????????????????????????????", Toast.LENGTH_SHORT).show();

                                            //  ??????????????????????????????
                                            Map<String, String> m = new HashMap<>();
                                            m.put("phone", editStr);
                                            m.put("type", "2");
                                            NetWorkUtil.getInstance().doPost(ServerAddress.SEND_SMS, m, new NetWorkUtil.NetWorkListener() {
                                                @Override
                                                public void success(String response) {

                                                    //  ?????????????????????
                                                    GeneralBean generalBean = new Gson().fromJson(response, GeneralBean.class);
                                                    if (generalBean.getCode() == 1) {
                                                        //  ?????????????????????
                                                        Toast.makeText(ControlActivity.this, "??????????????????????????????", Toast.LENGTH_SHORT).show();
                                                        //  ??????????????? ???????????????
                                                        adminLoginDialog.verifyState(new AdminLoginDialog.VerifyListener() {
                                                            @Override
                                                            public void verifyCallBack(String adminPhone, String verifyCode, final AlertDialog alertDialog) {
                                                                //  ??????????????? ?????? ???????????????????????? ?????? ???????????????????????????????????????

                                                                Map<String, String> ma = new HashMap<>();
                                                                ma.put("phone", adminPhone);
                                                                ma.put("pwd", password);
                                                                ma.put("code", verifyCode);
                                                                NetWorkUtil.getInstance().doPost(ServerAddress.PHONE_CODE_VERIFY, ma, new NetWorkUtil.NetWorkListener() {
                                                                    @Override
                                                                    public void success(String response) {
                                                                        APP.phoneCodeVerifyBean = new Gson().fromJson(response, PhoneCodeVerifyBean.class);

                                                                        phoneCodeVerifyBean = APP.phoneCodeVerifyBean;
                                                                        if (phoneCodeVerifyBean.getCode() == 1) {


                                                                            alertDialog.dismiss();
                                                                            showAdminManage(phoneCodeVerifyBean);
                                                                        } else {
                                                                            Toast.makeText(ControlActivity.this, "???????????????", Toast.LENGTH_SHORT).show();
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
                                                    } else {
                                                        Toast.makeText(ControlActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
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


                                        } else {
                                            //  ???????????? + ????????? ????????????
                                            Toast.makeText(ControlActivity.this, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
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
        userId = intent.getLongExtra("userId", 1);

        if (userId == 0) {
            Toast.makeText(ControlActivity.this, "????????????", Toast.LENGTH_LONG).show();
            //finish();
        } else {
            control_welcome_textView.setText("???????????? " + APP.userId + " ??????????????????");
        }


        //  ???????????????????????????
        final List<DustbinStateBean> dustbinStateBeans = DataBaseUtil.getInstance(this).getDaoSession().getDustbinStateBeanDao().queryBuilder().list();


        //  ???????????? ?????????????????????
        if (DataBaseUtil.getInstance(this).getDaoSession().getDustbinConfigDao().queryBuilder().unique().getHasVendingMachine()) {
            DustbinStateBean dustbinStateBean = new DustbinStateBean();
            dustbinStateBean.setDustbinBoxType("???????????????");
            dustbinStateBeans.add(dustbinStateBean);
        }

        //  ?????????
        final ControlItemAdapter controlItemAdapter = new ControlItemAdapter(R.layout.control_item_layout, removeDuplicateUser(dustbinStateBeans));
        controlItemAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(final BaseQuickAdapter adapter, View view, final int position) {
                if (view.getId() == R.id.control_item_iv) {

                    final DustbinStateBean data = controlItemAdapter.getData().get(position);

                    if ("???????????????".equals(data.getDustbinBoxType())) {
                        startActivity(new Intent(ControlActivity.this, VendingMachineActivity.class));
                    } else {

                        //  ??????????????????????????????
                        DustbinStateBean dustbinStateBean = openDoorByType(data.getDustbinBoxType());
                        if (dustbinStateBean == null) {
                            Toast.makeText(ControlActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openLight(dustbinStateBean.getDoorNumber()));


                        //  ??????????????????????????????
                        addNeedCloseDustbin(dustbinStateBean);
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openDoor(dustbinStateBean.getDoorNumber()));


                        //  ???????????????
                        //  SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openDoor(dustbinStateBean.getDoorNumber()));
                        //  ????????????????????????
                        // SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeTheDisinfection(dustbinStateBean.getDoorNumber()));
                    }


                }
            }
        });

        //  ????????????
        controlItemAdapter.setOnItemChildLongClickListener(new BaseQuickAdapter.OnItemChildLongClickListener() {
            @Override
            public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.control_item_iv) {

                    final DustbinStateBean data = controlItemAdapter.getData().get(position);

                    if ("???????????????".equals(data.getDustbinBoxType())) {

                    } else {

                        Toast.makeText(ControlActivity.this, "??????" + data.getDustbinBoxType() + "?????????", Toast.LENGTH_LONG).show();

                        //  ?????????????????????????????????
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (DustbinStateBean target : APP.dustbinBeanList) {
                                    //  0   ????????????
                                    if (target.getDustbinBoxType().equals(data.getDustbinBoxType()) && target.getDoorNumber() != 0) {
                                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDoor(target.getDoorNumber()));
                                        try {
                                            Thread.sleep(1000);
                                        } catch (Exception e) {
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
        control_recyclerview.setLayoutManager(new GridLayoutManager(this, 3));
        control_recyclerview.setAdapter(controlItemAdapter);


    }


    //  ???????????????
    FrameLayout cameraFrame;
    private Camera mCamera;

    public void initCameraSurfaceView() {
        cameraFrame = (FrameLayout) findViewById(R.id.control_camera_preview);

        int numberOfCameras = Camera.getNumberOfCameras();
        //  ?????????????????????
        for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraId == 0 && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                // ???????????????
                mCamera = Camera.open(cameraId);//  ???????????????
            }
        }


        CameraPreview mPreview = new CameraPreview(this, mCamera);
        cameraFrame.addView(mPreview);
    }


    public void takePicture() {
        //????????????????????????
        Camera.Parameters parameters = mCamera.getParameters();
        //???????????????
        parameters.setPictureFormat(ImageFormat.JPEG);
        //????????????????????????
        parameters.setPreviewSize(800, 400);
        //?????????????????????????????????
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        //??????????????????????????????
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                mCamera.takePicture(null, null, mPictureCallback);
            }
        });
    }


    //  ??????????????????????????????
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            FileOutputStream fos = null;
            String mFilePath = getFilesDir() + File.separator + System.currentTimeMillis() + ".png";
            //  ??????
            File tempFile = new File(mFilePath);


            //  ??????????????????
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            //  ??????????????????????????????????????????
            if (bitmap.getWidth() > bitmap.getHeight()) {
                bitmap = adjustPhotoRotation(bitmap, 90);
            }

            try {
                fos = new FileOutputStream(tempFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();

                NetWorkUtil.getInstance().fileUploadAutoDelete(tempFile, new NetWorkUtil.FileUploadListener() {
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

                //  ??????????????????????????????
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

    /**
     * @param bm                ??????????????????
     * @param orientationDegree ????????????
     *                          ????????????
     */
    public Bitmap adjustPhotoRotation(Bitmap bm, int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);

        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);

    }


    //  ????????????
    public final static String DEBUG_TAG_TASK = "??????????????????";

    public void exit_time_task() {

        Thread exitThread = new Thread() {
            @Override
            public void run() {
                super.run();

                Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG, "?????????????????????????????????" + APP.dustbinBeanList.toString());
                for (final DustbinStateBean dustbinStateBean : needCloseDustbin /* ??????????????????????????????????????? APP.dustbinBeanList*/) {
                    Log.i(DEBUG_TAG_TASK, "??????????????????" + dustbinStateBean.getDoorNumber() + "," + dustbinStateBean.getDoorIsOpen());


                    //  ?????????????????????????????????????????????
                    if (!dustbinStateBean.getDoorIsOpen()) {
                        //  ????????????
                        int i = DustbinUtil.getLeftOrRight(dustbinStateBean.getDoorNumber());
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openElectromagnetism(i));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openElectromagnetism(dustbinStateBean.getDoorNumber()));

                    }

                    //  ??????????????????
                    if (true /* ??????????????????????????????????????? dustbinStateBean.getDoorIsOpen()*/) {
                        Log.i(DEBUG_TAG_TASK, dustbinStateBean.getDoorNumber() + "?????????");

                        //  ??????
                        final long time = System.currentTimeMillis() / 1000;
                        //  ????????????
                        final String imageName = APP.getDeviceId() + "_" + dustbinStateBean.getDoorNumber() + "_" + APP.userId + "_" + time + "_" + dustbinStateBean.getId() + ".jpg";

                        Log.i(DEBUG_TAG_TASK, dustbinStateBean.getDoorNumber() + "????????????");

                        //  ??????
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDoor(dustbinStateBean.getDoorNumber()));

                        try {
                            Thread.sleep(400);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        //  ????????????
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openLight(dustbinStateBean.getDoorNumber()));

                        Log.i(DEBUG_TAG_TASK, dustbinStateBean.getDoorNumber() + "????????????????????????");

                        //  ??????????????????
                        addRecord(dustbinStateBean, time);

                        //  ??????
                        Intent intent = new Intent("MY_BROADCAST_RECEIVER");
                        intent.putExtra("type", "broadcast_camera_type");
                        intent.putExtra("data", "");

                        intent.putExtra("doorNumber", dustbinStateBean.getDoorNumber());
                        intent.putExtra("time", time);
                        intent.putExtra("bin_id", dustbinStateBean.getId());
                        //??????time??????????????? ???????????? ???????????????????????????
                        Log.i("????????????", "???????????????bin_id:" + dustbinStateBean.getId() + ", time:" + time);
                        sendBroadcast(intent);
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }

                Log.i(DEBUG_TAG_TASK, "take picture ????????????");
                //  1??????????????? ??????????????????
                exitEnd(1);

            }
        };

        exitThread.start();
    }


    //  ??????????????????????????????
    private List<DustbinStateBean> needCloseDustbin = new ArrayList<>();

    public void addNeedCloseDustbin(DustbinStateBean dustbinStateBean) {
        //  ????????? 0 ???????????????
        if (needCloseDustbin.size() == 0) {
            needCloseDustbin.add(dustbinStateBean);
        } else {

            //  ????????????????????????????????????????????????????????????????????????
            for (DustbinStateBean dustbinStateBeanChild : needCloseDustbin) {
                if (dustbinStateBeanChild.getDoorNumber() == dustbinStateBean.getDoorNumber()) {
                    return;
                }
            }

            //  ??????????????????????????????????????????????????????????????????????????????
            needCloseDustbin.add(dustbinStateBean);


            new Thread(new Runnable() {
                @Override
                public void run() {

                    int i = DustbinUtil.getLeftOrRight(dustbinStateBean.getDoorNumber());

                    SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeElectromagnetism(i));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeElectromagnetism(dustbinStateBean.getDoorNumber()));


                }
            }).start();

        }
    }


    /**
     * ?????????????????? ???????????????????????????
     * ?????? ?????? ????????????????????????
     */
    private int defaultCamera = 1;
    private final static String CAMERA_TAG = "???????????????";

    private int openDefaultDoor() {

        /*
         * ?????????
         * */
        for (DustbinStateBean dustbinStateBean : APP.dustbinBeanList) {
            if (dustbinStateBean.getDustbinBoxType().equals(DustbinENUM.OTHER.toString())) {

                //  ??????????????? 0 ??? ???0 ??????????????????
                if (!dustbinStateBean.getIsFull() && dustbinStateBean.getDoorNumber() != 0) {
                    //  ??????????????????????????????
                    addNeedCloseDustbin(dustbinStateBean);

                    //  ??????
                    SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openLight(dustbinStateBean.getDoorNumber()));

                    byte[] result = SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openDoor(dustbinStateBean.getDoorNumber()));

                    if (result != null) {
                        Log.i("????????????1", ByteStringUtil.byteArrayToHexStr(result));
                    }

                    defaultCamera = dustbinStateBean.getDoorNumber();
                    break;
                } else {
                    Toast.makeText(this, dustbinStateBean.getDustbinBoxType() + "???????????????", Toast.LENGTH_SHORT).show();
                }
            }
        }

        /*
         *
         * ?????????
         * */
        for (final DustbinStateBean dustbinStateBean : APP.dustbinBeanList) {
            if (dustbinStateBean.getDustbinBoxType().equals(DustbinENUM.KITCHEN.toString())) {

                if (!dustbinStateBean.getIsFull()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //  ??????????????????????????????
                            addNeedCloseDustbin(dustbinStateBean);

                            //  ??????
                            SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openLight(dustbinStateBean.getDoorNumber()));

                            byte[] result = SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openDoor(dustbinStateBean.getDoorNumber()));
                            if (result != null) {
                                Log.i("????????????2", ByteStringUtil.byteArrayToHexStr(result));
                            }

                            if (defaultCamera != 0) {
                                defaultCamera = dustbinStateBean.getDoorNumber();
                            }
                        }
                    }, 500);

                    break;
                } else {
                    Toast.makeText(ControlActivity.this, dustbinStateBean.getDustbinBoxType() + "???????????????", Toast.LENGTH_SHORT).show();
                }

            }
        }

        Log.i(CAMERA_TAG, "???????????????" + defaultCamera);
        return defaultCamera;
    }


    // ???????????????
    String[] finalStrings = new String[]{"???????????????", "??????????????????", "???????????????"};
    String[] strings = new String[]{"???????????????", "??????????????????", "???????????????"};
    /**
     * 1???????????????2????????????3???????????????99??????????????????
     */

    private PhoneCodeVerifyBean phoneCodeVerifyBean;

    private void showAdminManage(PhoneCodeVerifyBean phoneCodeVerifyBean) {
        //  ???????????????????????????????????????????????? ????????????
        if (!phoneCodeVerifyBean.getData().getAdmin_types().contains("99")) {
            String typeStr = phoneCodeVerifyBean.getData().getAdmin_types().replace("1", finalStrings[0]);
            typeStr = typeStr.replace("2", finalStrings[1]);
            typeStr = typeStr.replace("3", finalStrings[2]);

            strings = typeStr.split(",");
        }


        AlertDialog.Builder alert = new AlertDialog.Builder(ControlActivity.this, R.style.SingleChoiceItemsDialogStyle);
        alert.setTitle("????????????????????????:");
        alert.setSingleChoiceItems(strings, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String item = strings[which];

                if (finalStrings[0].equals(item)) {
                    Intent intent = new Intent(ControlActivity.this, RecyclerAdminActivity.class);
                    startActivity(intent);
                } else if (finalStrings[1].equals(item)) {
                    Intent intent = new Intent(ControlActivity.this, DustbinManageActivity.class);
                    startActivity(intent);
                } else if (finalStrings[2].equals(item)) {
                    Intent intent = new Intent(ControlActivity.this, ReplenishmentActivity.class);
                    startActivity(intent);
                }
            }
        });
        alert.setPositiveButton("??????", new DialogInterface.OnClickListener() {
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
     * ????????????????????????
     */
    private DustbinStateBean openDoorByType(String type) {
        if (APP.dustbinBeanList != null && APP.dustbinBeanList.size() > 0) {
            Log.i("????????????????????????", APP.dustbinBeanList.toString());
            for (DustbinStateBean dustbinStateBean : APP.dustbinBeanList) {
                if (dustbinStateBean.getDustbinBoxType().equals(type)) {

                    if (!dustbinStateBean.getIsFull()) {
                        return dustbinStateBean;
                    } else {
                        Toast.makeText(this, "???????????????", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }
            }
        } else {
            Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show();
        }

        return null;
    }


    /**
     * ??????????????????pid
     */
    public static int doorNumberToPid(int numb) {

        int target = numb * 1111;

        String string = new BigInteger(String.valueOf(target), 16).toString();


        return Integer.parseInt(string);
    }


    /**
     * ?????? pid ??? ??????
     */
    public static int pidToDoorNumber(int pid) {

        Integer x = pid;

        String hex = x.toHexString(x);

        int i = Integer.parseInt(hex);

        return i / 1111;
    }


    //  ?????? pid ?????? UVC ???????????????
    public UsbDevice getUsbCameraDevice(int pid) {
        UsbManager mUsbManager = (UsbManager) getSystemService(USB_SERVICE);

        HashMap<String, UsbDevice> deviceMap = mUsbManager.getDeviceList();


        //  ???????????????????????????
        if (deviceMap != null) {
            for (UsbDevice usbDevice : deviceMap.values()) {
                Integer integer = usbDevice.getProductId();
                Log.i(DEBUG_TAG_TASK, "????????????????????????" + usbDevice.getProductId() + "??????????????????" + integer.toHexString(integer));
            }
        }


        if (deviceMap != null) {
            textView.append("???????????????:" + deviceMap.size() + "\n");
            for (UsbDevice usbDevice : deviceMap.values()) {
                textView.append("???????????????:" + usbDevice.getDeviceName() + "\n");
                textView.append("?????????ProductId:" + usbDevice.getProductId() + "\n");
                textView.append("?????????DeviceId:" + usbDevice.getDeviceId() + "\n");
                textView.append("?????????VendorId:" + usbDevice.getVendorId() + "\n");
                textView.append("\n");
                if (usbDevice.getProductId() == pid) {
                    return usbDevice;
                }
            }
        }
        return null;
    }


    /**
     * ??????????????? ??????
     */
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


    //  ?????????
    public static class ControlItemAdapter extends BaseQuickAdapter<DustbinStateBean, BaseViewHolder> implements View.OnTouchListener {

        public ControlItemAdapter(int layoutResId, @Nullable List<DustbinStateBean> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, DustbinStateBean data) {

            //  ????????????
            helper.getView(R.id.control_item_iv).setOnTouchListener(this);

            //  ????????????
            int image_resource = 0;

            //  ??????
            if (DustbinENUM.BOTTLE.toString().equals(data.getDustbinBoxType())) {
                image_resource = R.mipmap.pinzi;
            } else if (DustbinENUM.WASTE_PAPER.toString().equals(data.getDustbinBoxType())) {
                image_resource = R.mipmap.zhipi;
            } else if (DustbinENUM.RECYCLABLES.toString().equals(data.getDustbinBoxType())) {
                image_resource = R.mipmap.kehuishou;
            } else if (DustbinENUM.KITCHEN.toString().equals(data.getDustbinBoxType())) {
                image_resource = R.mipmap.chuyu;
            } else if (DustbinENUM.HARMFUL.toString().equals(data.getDustbinBoxType())) {
                image_resource = R.mipmap.youhai;
            } else if (DustbinENUM.OTHER.toString().equals(data.getDustbinBoxType())) {
                image_resource = R.mipmap.qita;
            } else if ("???????????????".equals(data.getDustbinBoxType())) {
                image_resource = R.mipmap.shouhuo;
            }

            //  ????????????
            Glide.with(mContext).load(image_resource).into((ImageView) helper.getView(R.id.control_item_iv));
            helper.addOnClickListener(R.id.control_item_iv);
            helper.addOnLongClickListener(R.id.control_item_iv);

        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {

            //  ??????????????????????????????
            switch (event.getAction()) {
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
     * ????????????????????????????????????????????? ??????????????????????????????
     */
    private void getGoodsPos() {
        NetWorkUtil.getInstance().doGetThread(ServerAddress.GET_GOODS_POS, null, new NetWorkUtil.NetWorkListener() {
            @Override
            public void success(String response) {
                GetServerGoods getServerGoods = new Gson().fromJson(response, GetServerGoods.class);
                //  ??????????????????
                List<GetServerGoods.DataBean.ListBean> listBeans = getServerGoods.getData().getList();


                List<CommodityAlternativeBean> commodityAlternativeBeans = new ArrayList<>();
                for (GetServerGoods.DataBean.ListBean listBean : listBeans) {
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

                //  ????????????
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
     * ????????????????????????????????????????????????????????????????????????????????????
     *
     * @param commodityAlternativeBeans ???????????????????????????
     */
    private void updateCommodity(List<CommodityAlternativeBean> commodityAlternativeBeans) {

        //  ???????????????????????????
        DataBaseUtil.getInstance(this).getDaoSession().getCommodityAlternativeBeanDao().saveInTx(commodityAlternativeBeans);

        //  ?????????????????????
        for (CommodityAlternativeBean commodityAlternativeBean : commodityAlternativeBeans) {
            //  ??????????????????id?????????
            List<CommodityBean> com = DataBaseUtil.getInstance(ControlActivity.this).getDaoSession().getCommodityBeanDao().queryBuilder().where(CommodityBeanDao.Properties.CommodityID.eq(commodityAlternativeBean.getCommodityID())).list();

            //  ???id???????????? ??????????????????
            for (CommodityBean c : com) {
                c.setCommodityID(commodityAlternativeBean.getCommodityID());
                c.setCommodityAlternativeBean(commodityAlternativeBean);
            }

            //  ????????????????????????
            DataBaseUtil.getInstance(this).getDaoSession().getCommodityBeanDao().saveInTx(com);
        }
    }


    /**
     * @deprecated ??????????????????
     */
    public void exit(View view) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("???????????????????????????...");
        progressDialog.create();
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    //  ???????????????????????????
                    List<DustbinStateBean> dustbinBeanList = APP.dustbinBeanList;
                    for (final DustbinStateBean dustbinStateBean : dustbinBeanList) {

                        //  ???????????????????????????
                        if (dustbinStateBean.getDoorIsOpen()) {
                            //  ?????????
                            SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDoor(dustbinStateBean.getDoorNumber()));

                            //  ???????????? 3s,???????????????
                            Thread.sleep(4000);
                        }
                    }


                    //  ????????????
                    Thread.sleep(4000);

                    //  ??????id?????????0
                    APP.userId = 0;
                    //  ??????????????????????????? 0
                    APP.userType = 0;
                    //
                    bottleNumber = 0;
                    //  ????????????
                    APP.UserPhoto = null;


                    //  ???????????????
                    phoneCodeVerifyBean = null;


                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });

                    //startActivity(new Intent(ControlActivity.this,MainActivity.class));
                    finish();
                } catch (InterruptedException e) {
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
     * 30 s????????????????????????
     */
    TimerTask hasManTask;
    Timer hasManTimer = new Timer();
    private boolean hasManIsRun = true;
    private final static int AUTO_EXIT_TIME = 30;

    private void hasMan() {
        hasManTask = new TimerTask() {
            @Override
            public void run() {

                Log.i("??????", (System.currentTimeMillis() / 1000) + "," + (APP.hasManTime / 1000));

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if ((AUTO_EXIT_TIME - ((System.currentTimeMillis() - APP.hasManTime) / 1000)) == 0) {
                            if (timer != null) {
                                timer.cancel();
                            }

                            if (timerTask != null) {
                                timerTask.cancel();
                            }
                            exitEnd(1);
                        }

                        control_exit_btn.setText("?????? ( " + (AUTO_EXIT_TIME - ((System.currentTimeMillis() - APP.hasManTime) / 1000)) + "s )");
                    }
                });

                //  ?????? 10s ????????????
                if (hasManIsRun && (AUTO_EXIT_TIME - (System.currentTimeMillis() - APP.hasManTime) / 1000) == 10) {
                    VoiceUtil.getInstance().openAssetMusics(ControlActivity.this, "exit_alert_voice.aac");
                }

                //  30 s????????????
                if (hasManIsRun && System.currentTimeMillis() - APP.hasManTime > (AUTO_EXIT_TIME * 1000)) {

                    hasManTask.cancel();
                    hasManTimer.cancel();

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(ControlActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                            //  ????????????
                            exitControl(null);
                        }
                    });
                }


            }
        };

        hasManTimer = new Timer();
        hasManTimer.schedule(hasManTask, 1, 1000);
    }


    /**
     * ????????????
     */
    //  ??????????????????
    private ProgressDialog exitProgressDialog;
    //  ??????????????????
    private long beginExitTime;
    private TimerTask timerTask;
    private Timer timer;
    private Handler handler;

    public void exitControl(View view) {
        VoiceUtil.getInstance().openAssetMusics(ControlActivity.this, "exit_alert_voice.aac");

        exitProgressDialog = new ProgressDialog(this);
        exitProgressDialog.setCancelable(false);
        exitProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        exitProgressDialog.setTitle("??????");
        exitProgressDialog.setMessage("???????????????????????????...");
        exitProgressDialog.create();
        exitProgressDialog.show();


        beginExitTime = System.currentTimeMillis();

        timerTask = new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        long timeDiff = (System.currentTimeMillis() - beginExitTime) / 1000;

                        if (exitProgressDialog != null) {
                            exitProgressDialog.setTitle("????????? ( " + timeDiff + "s )");

                            if (exit_mode == EXIT_MODE.CLOSE_ITERATION) {
                                //  ??????????????? 6-7 s?????????????????????????????????????????? * 8
                                if (timeDiff > APP.dustbinBeanList.size() * 9) {
                                    timer.cancel();
                                    timerTask.cancel();
                                    Toast.makeText(ControlActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                                    exitEnd(1);
                                }
                            }
                        }
                    }
                });

            }
        };

        timer = new Timer();
        timer.schedule(timerTask, 1, 1000);

        //  ??????
        if (exit_mode == EXIT_MODE.CLOSE_ITERATION) {
            //  ????????????????????????,????????????
            closeOpenedDoor();
        } else {
            //  ????????????
            exit_time_task();
        }
    }


    /**
     * ??????????????????????????????????????????
     */
    private DustbinStateBean getBeforeDustbin(int doorNumber) {

        for (DustbinStateBean dustbinStateBean : beforeDustbinStateBeans) {
            if (dustbinStateBean.getDoorNumber() == doorNumber) {
                return dustbinStateBean;
            }
        }

        return null;
    }


    /**
     * ?????????????????? ?????????????????????????????? ??????
     */
    public final static String DEBUG_TAG = "????????????";

    public void closeOpenedDoor() {
        /*//  ????????????????????????
        int hasMatchCondition = 0;

        Log.i(DEBUG_TAG,"????????????????????????");
        List<DustbinStateBean> dustbinStateBeans = APP.dustbinBeanList;
        for(DustbinStateBean dustbinStateBean:dustbinStateBeans){
            Log.i(DEBUG_TAG,"????????????????????????" + dustbinStateBean.getDoorNumber() + "??????" +dustbinStateBean.getDoorIsOpen());
        }*/


        DustbinStateBean dustbinStateBean = getOpenedDoor();


        if (dustbinStateBean == null) {
            Log.i(DEBUG_TAG, "????????????");
            exitEnd(0);
        } else {

            Log.i(DEBUG_TAG, "?????????????????????" + dustbinStateBean.getDoorNumber() + "," + dustbinStateBean.getDoorIsOpen());

            //  ????????????????????? null
            if (mUsbDevice != null) {

                //  ??????????????????????????????????????????????????????
                if (pidToDoorNumber(mUsbDevice.getProductId()) == dustbinStateBean.getDoorNumber()) {
                    Log.i(DEBUG_TAG, "?????????????????????");
                } else {
                    Log.i(DEBUG_TAG, "??????????????????" + dustbinStateBean.getDoorNumber());
                }

            }

            Log.i(DEBUG_TAG, "????????????,???" + dustbinStateBean.getDoorNumber() + "??????");

            //  ??????
            SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDoor(dustbinStateBean.getDoorNumber()));
        }


    }

    /**
     * ????????????,????????????????????????
     *
     * @param exitCode 1??????????????? ??? 0 ???????????????
     */
    private void exitEnd(int exitCode) {

        //  ????????????????????? 4s ??????
        int millis = 4000 / APP.dustbinBeanList.size();

        //  ?????????????????????
        /*for(DustbinStateBean dustbinStateBean : APP.dustbinBeanList){
            //  ????????????
            SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeLight(dustbinStateBean.getDoorNumber()));
            try {
                Thread.sleep(millis);
            }catch (Exception e){
                e.printStackTrace();
            }
        }*/

        Log.i("take picture", "take picture ????????????");
        //  ???????????????????????????
        if (dustbinCallListenerThread != null && !dustbinCallListenerThread.isInterrupted()) {
            dustbinCallListener = false;
            dustbinCallListenerThread.interrupt();
        }

        //  ??????id?????????0
        //  APP.userId = 0;
        //  ??????????????????????????? 0
        bottleNumber = 0;
        //  ??????????????? ???????????????????????? null
        phoneCodeVerifyBean = null;

        //  ?????????????????????
        for (DustbinStateBean dustbinStateBean : APP.dustbinBeanList) {
            dustbinStateBean.setCloseFailNumber(0);
            APP.setDustbinState(ControlActivity.this, dustbinStateBean);
        }

        //  ????????????????????????????????????
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (exitProgressDialog != null) {
                    exitProgressDialog.dismiss();
                }
            }
        });

        //startActivity(new Intent(ControlActivity.this,MainActivity.class));
        Intent intent = new Intent(ControlActivity.this, MainActivity.class);
        intent.putExtra("exitCode", exitCode);
        setResult(MainActivity.CONTROL_RESULT_CODE, intent);
        finish();
    }

    /**
     * ???????????????????????????????????????????????????????????????
     */
    private long lastExecuteTime;

    @Subscribe(threadMode = ThreadMode.POSTING, sticky = true, priority = 99)
    public void closeDoorCall(final DustbinStateBean dustbinStateBean) {

        //  ???????????????????????????
        if (exit_mode == EXIT_MODE.TIME_TASK) {
            return;
        }

        if (System.currentTimeMillis() - lastExecuteTime < 1000) {
            return;
        }
        lastExecuteTime = System.currentTimeMillis();

        Log.i(DEBUG_TAG, "??????closeDoorCall()");
        //  ??????
        long time = System.currentTimeMillis() / 1000;
        //  ????????????
        final String imageName = APP.getDeviceId() + "_" + dustbinStateBean.getDoorNumber() + "_" + APP.userId + "_" + time + "_" + dustbinStateBean.getId() + ".jpg";
        //  ???????????????
        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openLight(dustbinStateBean.getDoorNumber()));
        //  ??????????????????
        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openTheDisinfection(dustbinStateBean.getDoorNumber()));
        //  ??????


        Log.i(DEBUG_TAG, "????????????????????????????????????????????????");

        Log.i(DEBUG_TAG, "???????????????????????????????????????????????????" + dustbinStateBean.toString());


        //  ????????????????????????????????????
        addRecord(dustbinStateBean, time);

        //  ??????????????????????????????
        closeOpenedDoor();

    }


    /**
     * ??????????????????
     */
    private void addRecord(final DustbinStateBean dustbinStateBean, long time) {
        //  ??????????????????????????????
        DeliveryRecord deliveryRecord = new DeliveryRecord(null, dustbinStateBean.getDoorNumber(), APP.userId, time, dustbinStateBean.getDustbinWeight(), null);
        long id = DataBaseUtil.getInstance(this).getDaoSession().getDeliveryRecordDao().insert(deliveryRecord);

        Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG, "??????????????????" + id);

        //  ????????????????????? ???????????????
        //  ??????????????????????????????, ???????????????
        if (!dustbinStateBean.getArtificialDoor()) {

            Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG, "?????????????????????");

            DeliveryRecord nowDeliveryRecord = new DeliveryRecord();
            nowDeliveryRecord.setDeliveryTime(System.currentTimeMillis());
            nowDeliveryRecord.setDoorNumber(dustbinStateBean.getDoorNumber());
            nowDeliveryRecord.setUserId(APP.userId);
            nowDeliveryRecord.setWeight(dustbinStateBean.getDustbinWeight());

            //  ?????????????????????????????????????????????????????????????????????????????????
            DataBaseUtil.getInstance(this).getDaoSession().getDeliveryRecordDao().insert(nowDeliveryRecord);


            //  ????????????????????????????????????????????????????????????
            QueryBuilder<DeliveryRecord> queryBuilder = DataBaseUtil.getInstance(this).getDaoSession().getDeliveryRecordDao().queryBuilder();
            queryBuilder.where(DeliveryRecordDao.Properties.DoorNumber.eq(dustbinStateBean.getDoorNumber()));
            queryBuilder.orderDesc(DeliveryRecordDao.Properties.Id);
            queryBuilder.limit(2);
            List<DeliveryRecord> result = queryBuilder.list();

            //  ?????????
            double diff = 0;

            //  ??????????????????????????????
            DustbinStateBean beforeDustbin = getBeforeDustbin(dustbinStateBean.getDoorNumber());
            if (beforeDustbin != null) {
                diff = dustbinStateBean.getDustbinWeight() - beforeDustbin.getDustbinWeight();
            }



                /*user_id	???	int	??????ID
                device_id	???	string	??????ID
                bin_id	???	int	?????????ID
                bin_type	???	string	??????????????? ABCDEF
                post_weight	???	float	????????????
                former_weight	???	float	???????????????
                now_weight	???	float	???????????????
                plastic_bottle_num	???	int	???????????????
                rubbish_image	???	string	????????????
                timestamp	???	string	???????????????*/


            //  ??????????????????
            Map<String, String> map = new HashMap<>();
            map.put("user_id", String.valueOf(APP.userId));
            map.put("bin_id", String.valueOf(dustbinStateBean.getId()));
            map.put("bin_type", dustbinStateBean.getDustbinBoxNumber());
            map.put("post_weight", String.valueOf(diff));
            map.put("former_weight", String.valueOf(dustbinStateBean.getDustbinWeight() - diff));
            map.put("now_weight", String.valueOf(dustbinStateBean.getDustbinWeight()));
            map.put("plastic_bottle_num", String.valueOf(bottleNumber));
            map.put("err_code", dustbinStateBean.getCloseFailNumber() == 0 ? "0" : "1");  //  0???????????????????????????????????? 1???2???3 ????????????err_msg
            map.put("err_msg", dustbinStateBean.getCloseFailNumber() == 0 ? "?????????" : "???????????????????????????");//    ????????????
            map.put("time", String.valueOf(time));
            map.put("rubbish_image", " ");
            if (APP.UserPhoto != null) {
                map.put("user_pictrue", APP.UserPhoto);
            }

            Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG, "????????????????????????" + map.toString());
            DustBinRecordRequestParams recordRequestParams = new DustBinRecordRequestParams();
            recordRequestParams.setRequestMap(map);
            EventBus.getDefault().post(recordRequestParams);
//            NetWorkUtil.getInstance().doPost(ServerAddress.DUSTBIN_RECORD, map, new NetWorkUtil.NetWorkListener() {
//                @Override
//                public void success(String response) {
//                    LogUtil.d(TAG,"?????????????????????????????????: "+response);
//                    LogUtil.writeBusinessLog("?????????????????????????????????: "+response);
//                    Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG, response);
//                    //  ?????????????????????,???????????????
//                    if (dustbinStateBean.getDustbinBoxType().equals(DustbinENUM.BOTTLE.toString())) {
//                        bottleNumber = 0;
//                    }
//                }
//
//                @Override
//                public void fail(Call call, IOException e) {
//                    LogUtil.writeBusinessLog("???????????? "+e.getMessage());
//                    Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG, e.getMessage());
//                }
//
//                @Override
//                public void error(Exception e) {
//                    LogUtil.writeBusinessLog("???????????? "+e.getMessage());
//                    Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG, e.getMessage());
//                }
//            });
        } else {
            LogUtil.writeBusinessLog("?????????????????????????????????");
            Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG, "?????????????????????????????????");
        }
    }

    /**
     * ?????????
     */
    private DustbinStateBean getDustbinByDoorNumber(List<DustbinStateBean> dustbinStateBeans, int doorNumber) {
        for (DustbinStateBean dustbinStateBean : dustbinStateBeans) {
            if (dustbinStateBean.getDoorNumber() == doorNumber) {
                return dustbinStateBean;
            }
        }

        return null;
    }

    //  ??????????????????
    /**
     * ??????????????????????????????????????????????????????????????????
     */
    private volatile boolean dustbinCallListener = true;
    private Thread dustbinCallListenerThread;

    private void setDustbinCallListener() {
        dustbinCallListenerThread = new Thread() {
            @Override
            public void run() {
                super.run();

                while (dustbinCallListener) {
                    DustbinStateBean dustbinStateBean = SerialPortResponseManage.getInstance().getDustbinStateBean();
                    Log.i(DEBUG_TAG, "????????????" + (dustbinStateBean == null ? "???????????????" : ",???????????????"));
                    if (dustbinStateBean != null) {
                        //  ???????????? ??? ????????? null
                        SerialPortResponseManage.getInstance().setDustbinStateBean(null);
                        closeDoorCall(dustbinStateBean);
                    }

                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        dustbinCallListenerThread.start();
    }

    /**
     * ????????????????????????
     */
    private DustbinStateBean getOpenedDoor() {
        Log.i(DEBUG_TAG, "????????????????????????????????????" + APP.dustbinBeanList.toString());
        for (DustbinStateBean dustbinStateBean : APP.dustbinBeanList) {
            if (dustbinStateBean.getDoorIsOpen() && dustbinStateBean.getCloseFailNumber() == 0) {
                Log.i(DEBUG_TAG, "??????????????????" + dustbinStateBean.getDoorNumber() + "," + dustbinStateBean.getDoorIsOpen() + "," + dustbinStateBean.getCloseFailNumber());
                return dustbinStateBean;
            }
        }

        return null;
    }


}

