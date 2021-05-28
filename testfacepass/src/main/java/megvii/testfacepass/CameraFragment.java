package megvii.testfacepass;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.serenegiant.common.BaseFragment;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.usbcameracommon.UvcCameraDataCallBack;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import megvii.testfacepass.independent.ServerAddress;
import megvii.testfacepass.independent.UploadImageService;
import megvii.testfacepass.independent.bean.DeliveryRecord;
import megvii.testfacepass.independent.bean.DeliveryRecordDao;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.bean.UploadImageServiceBean;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.NetWorkUtil;
import megvii.testfacepass.utils.LogUtil;
import okhttp3.Call;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

/**
 * 显示多路摄像头
 */
public class CameraFragment extends BaseFragment implements CameraDialog.CameraDialogParent {
    private static final boolean DEBUG = true;
    private static final String TAG = "多路摄像头调试";

    private static final float[] BANDWIDTH_FACTORS = {0.5f, 0.5f};

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;

    private UVCCameraHandler mHandlerFirst;
    private CameraViewInterface mUVCCameraViewFirst;
    private ImageButton mCaptureButtonFirst;
    private Surface mFirstPreviewSurface;

    private UVCCameraHandler mHandlerSecond;
    private CameraViewInterface mUVCCameraViewSecond;
    private ImageButton mCaptureButtonSecond;
    private Surface mSecondPreviewSurface;

    private UVCCameraHandler mHandlerThird;
    private CameraViewInterface mUVCCameraViewThird;
    private ImageButton mCaptureButtonThird;
    private Surface mThirdPreviewSurface;

    private UVCCameraHandler mHandlerFourth;
    private CameraViewInterface mUVCCameraViewFourth;
    private ImageButton mCaptureButtonFourth;
    private Surface mFourthPreviewSurface;

    private UVCCameraHandler mHandlerFifth;
    private CameraViewInterface mUVCCameraViewFifth;
    private ImageButton mCaptureButtonFifth;
    private Surface mFifthPreviewSurface;


    private UVCCameraHandler mHandlerSixth;
    private CameraViewInterface mUVCCameraViewSixth;
    private ImageButton mCaptureButtonSixth;
    private Surface mSixthPreviewSurface;


    private UVCCameraHandler mHandlerSeven;
    private CameraViewInterface mUVCCameraViewSeven;
    private ImageButton mCaptureButtonSeven;
    private Surface mSixthPreviewSeven;

    //  权限
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String PERMISSION_READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_INTERNET = Manifest.permission.INTERNET;
    private static final String PERMISSION_ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;
    private static final String PERMISSION_AUDIO = Manifest.permission.RECORD_AUDIO;
    private static final String[] Permission = new String[]{PERMISSION_AUDIO, PERMISSION_CAMERA, PERMISSION_WRITE_STORAGE, PERMISSION_READ_STORAGE, PERMISSION_INTERNET, PERMISSION_ACCESS_NETWORK_STATE, ACCESS_COARSE_LOCATION};


    //  是否已经被加载过一遍了
    private static boolean resumed = false;

    private View cameraFragmentView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //  串口通知拍照
        getActivity().registerReceiver(new MyBroadcastReceiver(), new IntentFilter("MY_BROADCAST_RECEIVER"));

        //  拍照成功回调
        getActivity().registerReceiver(new UvcCameraPhotoCallback(), new IntentFilter("UVC_CAMERA_PHOTO_CALLBACK"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        cameraFragmentView = View.inflate(getActivity(), R.layout.activity_surface_view_camera2, null);


        //  发送广播
        

        /*//  串口通知拍照
        getActivity().registerReceiver(new MyBroadcastReceiver(),new IntentFilter("MY_BROADCAST_RECEIVER"));

        //  拍照成功回调
        getActivity().registerReceiver(new UvcCameraPhotoCallback(),new IntentFilter("UVC_CAMERA_PHOTO_CALLBACK"));*/

        //  请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(Permission, PERMISSIONS_REQUEST);
        }

        cameraFragmentView.findViewById(R.id.RelativeLayout1).setOnClickListener(mOnClickListener);
        mUSBMonitor = new USBMonitor(getActivity(), mOnDeviceConnectListener);
        //拿到垃圾箱配置，根据厨余类型设置需要初始化的参数
//        List<DustbinStateBean> dustbinBeanList = APP.dustbinBeanList;
//        for (DustbinStateBean dustbinStateBean : dustbinBeanList) {
//            if (null != dustbinStateBean && dustbinStateBean.getDustbinBoxType() != null && dustbinStateBean.getDustbinBoxType().equals("厨余垃圾")) {
//                switch (dustbinStateBean.getDoorNumber()) {
//                    case 1:
//                        resultFirstCamera();
//                        break;
//                    case 2:
//                        resultSecondCamera();
//                        break;
//                    case 3:
//                        resultThirdCamera();
//                        break;
//                    case 4:
//                        resultFourthCamera();
//                        break;
//                    case 5:
//                        resultFifThCamrea();
//                        break;
//                    case 6:
//                        resultSixthCamera();
//                        break;
//                    case 7:
//                        resultSevenCamera();
//                        break;
//
//                }
//            }
//        }
        resultFirstCamera();
        resultSecondCamera();
        resultThirdCamera();
        resultFourthCamera();
        resultFifThCamrea();
        resultSixthCamera();
        resultSevenCamera();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initAllCamera();
            }
        }, 3000);


        return cameraFragmentView;
    }


    //  拍照成功回调
    private class UvcCameraPhotoCallback extends BroadcastReceiver {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void onReceive(final Context context, Intent intent) {

            if (intent != null) {

                final String path = intent.getStringExtra("path");

                uploadImageFile(path);
            }
        }
    }


    //  检查图片
    private void checkImage(String[] fileArray, File file) {
        //  图片小于 10 kb ， 说明没有拍摄成功
        if (file.length() < (1024 * 10)) {

            //  门板编号
            int doorNumber = Integer.parseInt(fileArray[1]);

            Log.i("警告", doorNumber + "没有拍摄成功");

            if (1 == doorNumber) {

                mHandlerFirst.close();
                if (mUVCCameraViewFirst != null)
                    mUVCCameraViewFirst.onPause();
                mCaptureButtonFirst.setVisibility(View.INVISIBLE);

                mUSBMonitor.requestPermission(getDeviceByNumber(doorNumber));
            } else if (2 == doorNumber) {
                mHandlerSecond.close();
                if (mUVCCameraViewSecond != null)
                    mUVCCameraViewSecond.onPause();
                mCaptureButtonSecond.setVisibility(View.INVISIBLE);

                mUSBMonitor.requestPermission(getDeviceByNumber(doorNumber));
            } else if (3 == doorNumber) {
                mHandlerThird.close();
                if (mUVCCameraViewThird != null)
                    mUVCCameraViewThird.onPause();
                mCaptureButtonThird.setVisibility(View.INVISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mUSBMonitor.requestPermission(getDeviceByNumber(doorNumber));
                    }
                }, 1000);

            } else if (4 == doorNumber) {
                mHandlerFourth.close();
                if (mUVCCameraViewFourth != null)
                    mUVCCameraViewFourth.onPause();
                mCaptureButtonFourth.setVisibility(View.INVISIBLE);

                mUSBMonitor.requestPermission(getDeviceByNumber(doorNumber));
            } else if (5 == doorNumber) {
                mHandlerFifth.close();
                if (mUVCCameraViewFifth != null)
                    mUVCCameraViewFifth.onPause();
                mCaptureButtonFifth.setVisibility(View.INVISIBLE);

                mUSBMonitor.requestPermission(getDeviceByNumber(doorNumber));
            } else if (6 == doorNumber) {
                mHandlerSixth.close();
                if (mUVCCameraViewSixth != null)
                    mUVCCameraViewSixth.onPause();
                mCaptureButtonSixth.setVisibility(View.INVISIBLE);

                mUSBMonitor.requestPermission(getDeviceByNumber(doorNumber));
            } else if (7 == doorNumber) {
                mHandlerSeven.close();
                if (mUVCCameraViewSeven != null)
                    mUVCCameraViewSeven.onPause();
                mCaptureButtonSeven.setVisibility(View.INVISIBLE);

                mUSBMonitor.requestPermission(getDeviceByNumber(doorNumber));
            }

        }
    }


    public void uploadImageFile(String path) {

        if (path != null) {


            File file = new File(path);
            //  去除.jpg
            //  设备id + 门板编号 + 用户id + 时间戳 + 垃圾箱id . jpg
            String fileName = file.getName().replace(".jpg", "");

            //  解析文件名称
            final String[] fileArray = fileName.split("_");


            //  检查图片
            //checkImage(fileArray,file);


            //  首先根据时间戳查询 投递记录,
            DeliveryRecord deliveryRecord = DataBaseUtil.getInstance(getActivity()).getDaoSession().getDeliveryRecordDao().queryBuilder().where(DeliveryRecordDao.Properties.DeliveryTime.eq(fileArray[3])).unique();
            //  修改投递记录 中的拍摄图片地址 （关门时创建的投递记录是没有图片的，所以这里拍摄回调需要添加拍摄地址）
            deliveryRecord.setTakePath(file.getPath());
            //  修改信息
            DataBaseUtil.getInstance(getActivity()).getDaoSession().getDeliveryRecordDao().update(deliveryRecord);



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

            NetWorkUtil.getInstance().fileUploadAutoDelete(file, new NetWorkUtil.FileUploadListener() {
                @Override
                public void success(String fileUrl) {

                    Map<String, String> map = new HashMap<>();
                    map.put("bin_id", fileArray[4]);
                    map.put("user_id", fileArray[2]);
                    map.put("rubbish_image", fileUrl);
                    map.put("time", fileArray[3]);
                    LogUtil.d(TAG,"图片上传参数："+map.toString());
                    LogUtil.writeBusinessLog("上传拍照信息参数："+map.toString());

                    NetWorkUtil.getInstance().doPost(ServerAddress.RUBBISH_IMAGE_POST, map, new NetWorkUtil.NetWorkListener() {
                        @Override
                        public void success(String response) {
                            Log.i(ControlActivity.DEBUG_TAG_TASK, "图片绑定结果" + response);
                        }

                        @Override
                        public void fail(Call call, IOException e) {
                            Log.i(ControlActivity.DEBUG_TAG_TASK, "图片绑定结果" + e.getMessage());
                        }

                        @Override
                        public void error(Exception e) {
                            Log.i(ControlActivity.DEBUG_TAG_TASK, "图片绑定结果" + e.getMessage());
                        }
                    });
                }

                @Override
                public void error(Exception e) {
                    Log.i(ControlActivity.DEBUG_TAG_TASK, "图片绑定结果" + e.getMessage());
                }
            });
        }
    }


    /**
     * 初始化所有相机
     *
     * 目前只开启餐厨垃圾
     */
    private void initAllCamera() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                for (UsbDevice usbDevice : mUSBMonitor.getDeviceList()) {
                    String str = String.format("%x", usbDevice.getProductId());
                    //拿到垃圾桶配置，只开启厨余垃圾摄像头
                    final List<DustbinStateBean> dustbinBeanList = APP.dustbinBeanList;
                    Log.d(TAG,"当前垃圾箱配置列表："+dustbinBeanList.size());
                    for (DustbinStateBean dustbinStateBean : dustbinBeanList) {
                        if (null != dustbinStateBean && dustbinStateBean.getDustbinBoxType() != null && dustbinStateBean.getDustbinBoxType().equals("厨余垃圾")) {
//                            if(str.equals(String.valueOf(dustbinStateBean.getDoorNumber() * 1111))){
                                Log.d(TAG,"当前开启的摄像头："+str);
                                mUSBMonitor.requestPermission(usbDevice);
                                onDialogResult(false);
                                threadSleep();
//                            }
                        }
                    }
//                    if (str.equals("1111") ||
//                            str.equals("2222") ||
//                            str.equals("3333") ||
//                            str.equals("4444") ||
//                            str.equals("5555") ||
//                            str.equals("6666") ||
//                            str.equals("7777")) {
//
//                        mUSBMonitor.requestPermission(usbDevice);
//                        onDialogResult(false);
//                        threadSleep();
//                    }

                }
            }
        }).start();
    }


    //  传入 id 获取设备
    private UsbDevice getDeviceByNumber(int number) {
        String target = String.valueOf(number * 1111);
        for (UsbDevice usbDevice : mUSBMonitor.getDeviceList()) {
            String str = String.format("%x", usbDevice.getProductId());

            if (str.equals(target)) {

                return usbDevice;
            }

        }

        return null;
    }

    public final static String CAMERA_TAG = "拍照调试";

    public final static String BROADCAST_CAMERA_TYPE = "broadcast_camera_type";

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(CAMERA_TAG, "接收到拍照请求");
            if (intent != null) {
                String type = intent.getStringExtra("type");
                String data = intent.getStringExtra("data");

                int doorNumber = intent.getIntExtra("doorNumber", 0);
                long time = intent.getLongExtra("time", System.currentTimeMillis());
                long bin_id = intent.getLongExtra("bin_id", 0);

                for(UsbDevice usbDevice : getUSBMonitor().getDeviceList()){
                    String pid = String.format("%x", usbDevice.getProductId());
                    String doorPid = String.valueOf(doorNumber * 1111);

                }
                //  拍照
                if (BROADCAST_CAMERA_TYPE.equals(type)) {
                    captureByDoor(doorNumber, time, bin_id);
                }else {
                    LogUtil.d(TAG,"拍照type不匹配");
                }
            }else {
                LogUtil.d(TAG,"拍照接收意图为null,无法拍照");
            }
        }
    }


    /**
     * @param doorNumber 门板 id
     * @param time       拍摄时间
     * @param bin_id     垃圾箱 id
     */
    private void captureByDoor(int doorNumber, long time, long bin_id) {
        Log.i(CAMERA_TAG, "拍摄" + doorNumber + "桶位" + bin_id + ", time:" + time);

        if (doorNumber == 0 || bin_id == 0) {
            return;
        }
        final String imageName = APP.getDeviceId() + "_" + doorNumber + "_" + APP.userId + "_" + time + "_" + bin_id + ".jpg";

        String savePath = Environment.getExternalStorageDirectory() + File.separator + imageName;
        switch (doorNumber) {
            case 1:
                if (mHandlerFirst.isOpened()) {
                    mHandlerFirst.captureStill(savePath);
                }else {
                    LogUtil.writeBusinessLog(doorNumber+"号门摄像头未开启");
                }
                break;
            case 2:
                if (mHandlerSecond.isOpened()) {
                    mHandlerSecond.captureStill(savePath);
                }else {
                    LogUtil.writeBusinessLog(doorNumber+"号门摄像头未开启");
                }
                break;
            case 3:
                if (mHandlerThird.isOpened()) {
                    mHandlerThird.captureStill(savePath);
                }else {
                    LogUtil.writeBusinessLog(doorNumber+"号门摄像头未开启");
                }
                break;
            case 4:
                if (mHandlerFourth.isOpened()) {
                    mHandlerFourth.captureStill(savePath);
                }else {
                    LogUtil.writeBusinessLog(doorNumber+"号门摄像头未开启");
                }
                break;
            case 5:
                if (mHandlerFifth.isOpened()) {
                    LogUtil.d(TAG,"测试拍照"+savePath);
                    mHandlerFifth.captureStill(savePath);
                }else {
                    LogUtil.writeBusinessLog(doorNumber+"号门摄像头未开启");
                }
                break;
            case 6:
                if (mHandlerSixth.isOpened()) {
                    mHandlerSixth.captureStill(savePath);
                }else {
                    LogUtil.writeBusinessLog(doorNumber+"号门摄像头未开启");
                }
                break;
            case 7:
                if (mHandlerSeven.isOpened()) {
                    mHandlerSeven.captureStill(savePath);
                }else {
                    LogUtil.writeBusinessLog(doorNumber+"号门摄像头未开启");
                }
                break;
        }
    }


    private void threadSleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 带有回调数据的初始化
     */
    private void resultFirstCamera() {
        mUVCCameraViewFirst = (CameraViewInterface) cameraFragmentView.findViewById(R.id.camera_view_first);
        //设置显示宽高
        mUVCCameraViewFirst.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewFirst).setOnClickListener(mOnClickListener);
        mCaptureButtonFirst = (ImageButton) cameraFragmentView.findViewById(R.id.capture_button_first);
        mCaptureButtonFirst.setOnClickListener(mOnClickListener);
        mCaptureButtonFirst.setVisibility(View.INVISIBLE);
        mHandlerFirst = UVCCameraHandler.createHandler(getActivity(), mUVCCameraViewFirst
                , UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT
                , BANDWIDTH_FACTORS[0], firstDataCallBack);
    }

    UvcCameraDataCallBack firstDataCallBack = new UvcCameraDataCallBack() {
        @Override
        public void getData(byte[] data) {
            Log.i("视频流", "1");
        }
    };


    UvcCameraDataCallBack twoDataCallBack = new UvcCameraDataCallBack() {
        @Override
        public void getData(byte[] data) {
            Log.i("视频流", "2");
        }
    };


    UvcCameraDataCallBack threeDataCallBack = new UvcCameraDataCallBack() {
        @Override
        public void getData(byte[] data) {
            Log.i("视频流", "3");
        }
    };


    UvcCameraDataCallBack fourDataCallBack = new UvcCameraDataCallBack() {
        @Override
        public void getData(byte[] data) {
            Log.i("视频流", "4");
        }
    };


    UvcCameraDataCallBack fiveDataCallBack = new UvcCameraDataCallBack() {
        @Override
        public void getData(byte[] data) {
            Log.i("视频流", "5");
        }
    };

    UvcCameraDataCallBack sixDataCallBack = new UvcCameraDataCallBack() {
        @Override
        public void getData(byte[] data) {
            Log.i("视频流", "6");
        }
    };

    UvcCameraDataCallBack sevenDataCallBack = new UvcCameraDataCallBack() {
        @Override
        public void getData(byte[] data) {
            Log.i("视频流", "7");
        }
    };


    private void resultSecondCamera() {
        mUVCCameraViewSecond = (CameraViewInterface) cameraFragmentView.findViewById(R.id.camera_view_second);
        mUVCCameraViewSecond.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewSecond).setOnClickListener(mOnClickListener);
        mCaptureButtonSecond = (ImageButton) cameraFragmentView.findViewById(R.id.capture_button_second);
        mCaptureButtonSecond.setOnClickListener(mOnClickListener);
        mCaptureButtonSecond.setVisibility(View.INVISIBLE);
        mHandlerSecond = UVCCameraHandler.createHandler(getActivity(), mUVCCameraViewSecond, UVCCamera.DEFAULT_PREVIEW_WIDTH,
                UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[1], twoDataCallBack);
    }

    private void resultThirdCamera() {
        mUVCCameraViewThird = (CameraViewInterface) cameraFragmentView.findViewById(R.id.camera_view_third);
        mUVCCameraViewThird.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewThird).setOnClickListener(mOnClickListener);
        mCaptureButtonThird = (ImageButton) cameraFragmentView.findViewById(R.id.capture_button_third);
        mCaptureButtonThird.setOnClickListener(mOnClickListener);
        mCaptureButtonThird.setVisibility(View.INVISIBLE);
        mHandlerThird = UVCCameraHandler.createHandler(getActivity(), mUVCCameraViewThird, UVCCamera.DEFAULT_PREVIEW_WIDTH,
                UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[1], threeDataCallBack);
    }

    private void resultFourthCamera() {
        mUVCCameraViewFourth = (CameraViewInterface) cameraFragmentView.findViewById(R.id.camera_view_fourth);
        mUVCCameraViewFourth.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewFourth).setOnClickListener(mOnClickListener);
        mCaptureButtonFourth = (ImageButton) cameraFragmentView.findViewById(R.id.capture_button_fourth);
        mCaptureButtonFourth.setOnClickListener(mOnClickListener);
        mCaptureButtonFourth.setVisibility(View.INVISIBLE);
        mHandlerFourth = UVCCameraHandler.createHandler(getActivity(), mUVCCameraViewFourth, UVCCamera.DEFAULT_PREVIEW_WIDTH,
                UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[1], fourDataCallBack);
    }

    private void resultFifThCamrea() {
        mUVCCameraViewFifth = (CameraViewInterface) cameraFragmentView.findViewById(R.id.camera_view_fifth);
        mUVCCameraViewFifth.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewFifth).setOnClickListener(mOnClickListener);
        mCaptureButtonFifth = (ImageButton) cameraFragmentView.findViewById(R.id.capture_button_fifth);
        mCaptureButtonFifth.setOnClickListener(mOnClickListener);
        mCaptureButtonFifth.setVisibility(View.INVISIBLE);
        mHandlerFifth = UVCCameraHandler.createHandler(getActivity(), mUVCCameraViewFifth, UVCCamera.DEFAULT_PREVIEW_WIDTH,
                UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[1], fiveDataCallBack);
    }

    private void resultSixthCamera() {
        mUVCCameraViewSixth = (CameraViewInterface) cameraFragmentView.findViewById(R.id.camera_view_sixth);
        mUVCCameraViewSixth.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewSixth).setOnClickListener(mOnClickListener);
        mCaptureButtonSixth = (ImageButton) cameraFragmentView.findViewById(R.id.capture_button_sixth);
        mCaptureButtonSixth.setOnClickListener(mOnClickListener);
        mCaptureButtonSixth.setVisibility(View.INVISIBLE);
        mHandlerSixth = UVCCameraHandler.createHandler(getActivity(), mUVCCameraViewSixth, UVCCamera.DEFAULT_PREVIEW_WIDTH,
                UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[1], sixDataCallBack);
    }


    private void resultSevenCamera() {
        mUVCCameraViewSeven = (CameraViewInterface) cameraFragmentView.findViewById(R.id.camera_view_seven);
        mUVCCameraViewSeven.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewSeven).setOnClickListener(mOnClickListener);
        mCaptureButtonSeven = (ImageButton) cameraFragmentView.findViewById(R.id.capture_button_seven);
        mCaptureButtonSeven.setOnClickListener(mOnClickListener);
        mCaptureButtonSeven.setVisibility(View.INVISIBLE);
        mHandlerSeven = UVCCameraHandler.createHandler(getActivity(), mUVCCameraViewSeven, UVCCamera.DEFAULT_PREVIEW_WIDTH,
                UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[1], sevenDataCallBack);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (resumed) {
            return;
        }

        mUSBMonitor.register();
        if (mUVCCameraViewSecond != null)
            mUVCCameraViewSecond.onResume();
        if (mUVCCameraViewFirst != null)
            mUVCCameraViewFirst.onResume();
        if (mUVCCameraViewThird != null)
            mUVCCameraViewThird.onResume();
        if (mUVCCameraViewFourth != null)
            mUVCCameraViewFourth.onResume();
        if (mUVCCameraViewFifth != null)
            mUVCCameraViewFifth.onResume();
        if (mUVCCameraViewSixth != null)
            mUVCCameraViewSixth.onResume();
        if (mUVCCameraViewSeven != null)
            mUVCCameraViewSeven.onResume();
    }

    @Override
    public void onResume() {
        super.onResume();

        resumed = true;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (resumed) {
            return;
        }

        mHandlerFirst.close();
        if (mUVCCameraViewFirst != null)
            mUVCCameraViewFirst.onPause();
        mCaptureButtonFirst.setVisibility(View.INVISIBLE);

        mHandlerSecond.close();
        if (mUVCCameraViewSecond != null)
            mUVCCameraViewSecond.onPause();
        mCaptureButtonSecond.setVisibility(View.INVISIBLE);

        mHandlerThird.close();
        if (mUVCCameraViewThird != null)
            mUVCCameraViewThird.onPause();
        mCaptureButtonThird.setVisibility(View.INVISIBLE);

        mHandlerFourth.close();
        if (mUVCCameraViewFourth != null)
            mUVCCameraViewFourth.onPause();
        mCaptureButtonFourth.setVisibility(View.INVISIBLE);

        mHandlerFifth.close();
        if (mUVCCameraViewFifth != null)
            mUVCCameraViewFifth.onPause();
        mCaptureButtonFifth.setVisibility(View.INVISIBLE);

        mHandlerSixth.close();
        if (mUVCCameraViewSixth != null)
            mUVCCameraViewSixth.onPause();
        mCaptureButtonSixth.setVisibility(View.INVISIBLE);


        mHandlerSeven.close();
        if (mUVCCameraViewSeven != null)
            mUVCCameraViewSeven.onPause();
        mCaptureButtonSeven.setVisibility(View.INVISIBLE);

        mUSBMonitor.unregister();//usb管理器解绑
    }

    @Override
    public void onDestroy() {
        if (mHandlerFirst != null) {
            mHandlerFirst = null;
        }

        if (mHandlerSecond != null) {
            mHandlerSecond = null;
        }

        if (mHandlerThird != null) {
            mHandlerThird = null;
        }

        if (mHandlerFourth != null) {
            mHandlerFourth = null;
        }

        if (mHandlerFifth != null) {
            mHandlerFifth = null;
        }

        if (mHandlerSixth != null) {
            mHandlerSixth = null;
        }


        if (mHandlerSeven != null) {
            mHandlerSeven = null;
        }

        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }

        mUVCCameraViewFirst = null;
        mCaptureButtonFirst = null;

        mUVCCameraViewSecond = null;
        mCaptureButtonSecond = null;

        mUVCCameraViewThird = null;
        mCaptureButtonThird = null;

        mUVCCameraViewFourth = null;
        mCaptureButtonFourth = null;

        mUVCCameraViewFifth = null;
        mCaptureButtonFifth = null;

        mUVCCameraViewSixth = null;
        mCaptureButtonSixth = null;


        mUVCCameraViewSeven = null;
        mCaptureButtonSeven = null;
        super.onDestroy();
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {


            switch (view.getId()) {
                case R.id.camera_view_first:
                    if (!mHandlerFirst.isPreviewing()) {
                        mHandlerFirst.close();
                        mUSBMonitor.requestPermission(getDeviceByNumber(1));
                    } else {
                        Toast.makeText(getActivity(), "相机已开启", Toast.LENGTH_SHORT).show();

                    }
                    break;
                case R.id.camera_view_second:
                    if (!mHandlerSecond.isPreviewing()) {
                        mHandlerSecond.close();
                        mUSBMonitor.requestPermission(getDeviceByNumber(2));
                    } else {

                        Toast.makeText(getActivity(), "相机已开启", Toast.LENGTH_SHORT).show();

                    }
                    break;
                case R.id.camera_view_third:
                    if (!mHandlerThird.isPreviewing()) {
                        mHandlerThird.close();
                        mUSBMonitor.requestPermission(getDeviceByNumber(3));
                    } else {
                        Toast.makeText(getActivity(), "相机已开启", Toast.LENGTH_SHORT).show();

                        /*mHandlerThird.close();
                        if (mUVCCameraViewThird != null)
                            mUVCCameraViewThird.onPause();
                        mCaptureButtonThird.setVisibility(View.INVISIBLE);


                        mUSBMonitor.unregister();//usb管理器解绑
                        mUSBMonitor.register();
                        if (mUVCCameraViewThird != null)
                            mUVCCameraViewThird.onResume();

                        mUSBMonitor.requestPermission(getDeviceByNumber(3));
                        onDialogResult(false);*/

                    }
                    break;
                case R.id.camera_view_fourth:
                    if (!mHandlerFourth.isPreviewing()) {
                        mHandlerFourth.close();
                        mUSBMonitor.requestPermission(getDeviceByNumber(4));
                    } else {
                        Toast.makeText(getActivity(), "相机已开启", Toast.LENGTH_SHORT).show();

                    }
                    break;

                case R.id.camera_view_fifth:
                    if (!mHandlerFifth.isPreviewing()) {
                        mHandlerFifth.close();
                        mUSBMonitor.requestPermission(getDeviceByNumber(5));
                    } else {
                        Toast.makeText(getActivity(), "相机已开启", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.camera_view_sixth:
                    if (!mHandlerSixth.isPreviewing()) {
                        mHandlerSixth.close();
                        mUSBMonitor.requestPermission(getDeviceByNumber(6));
                    } else {
                        Toast.makeText(getActivity(), "相机已开启", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.camera_view_seven:
                    if (!mHandlerSeven.isPreviewing()) {
                        mHandlerSeven.close();
                        mUSBMonitor.requestPermission(getDeviceByNumber(7));
                    } else {
                        Toast.makeText(getActivity(), "相机已开启", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {

            if (DEBUG) {
                String pid = String.format("%x", device.getProductId());
                Log.v(TAG, "onAttach:  pid" + pid);
            }
            //  Toast.makeText(getActivity(), "相机连接", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {

            String pid = String.format("%x", device.getProductId());

            //设备连接成功
            if (DEBUG) Log.v(TAG, "onConnect:" + device.getProductId());

            if ("1111".equals(pid)) {
                if (!mHandlerFirst.isOpened()) {
                    LogUtil.writeBusinessLog("摄像头："+pid+"开始连接");
                    mHandlerFirst.open(ctrlBlock);
                    final SurfaceTexture st = mUVCCameraViewFirst.getSurfaceTexture();
                    mHandlerFirst.startPreview(new Surface(st));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCaptureButtonFirst.setVisibility(View.VISIBLE);
                        }
                    }, 0);
                }
            } else if ("2222".equals(pid)) {
                if (!mHandlerSecond.isOpened()) {
                    LogUtil.writeBusinessLog("摄像头："+pid+"开始连接");
                    mHandlerSecond.open(ctrlBlock);
                    final SurfaceTexture st = mUVCCameraViewSecond.getSurfaceTexture();
                    mHandlerSecond.startPreview(new Surface(st));
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCaptureButtonSecond.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } else if ("3333".equals(pid)) {
                if (!mHandlerThird.isOpened()) {
                    LogUtil.writeBusinessLog("摄像头："+pid+"开始连接");
                    mHandlerThird.open(ctrlBlock);
                    final SurfaceTexture st = mUVCCameraViewThird.getSurfaceTexture();
                    mHandlerThird.startPreview(new Surface(st));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCaptureButtonThird.setVisibility(View.VISIBLE);
                        }
                    }, 0);
                }
            } else if ("4444".equals(pid)) {
                if (!mHandlerFourth.isOpened()) {
                    LogUtil.writeBusinessLog("摄像头："+pid+"开始连接");
                    mHandlerFourth.open(ctrlBlock);
                    final SurfaceTexture st = mUVCCameraViewFourth.getSurfaceTexture();
                    mHandlerFourth.startPreview(new Surface(st));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCaptureButtonFourth.setVisibility(View.VISIBLE);
                        }
                    }, 0);
                }
            } else if ("5555".equals(pid)) {
                if (!mHandlerFifth.isOpened()) {
                    LogUtil.writeBusinessLog("摄像头："+pid+"开始连接");
                    mHandlerFifth.open(ctrlBlock);
                    final SurfaceTexture st = mUVCCameraViewFifth.getSurfaceTexture();
                    mHandlerFifth.startPreview(new Surface(st));
                    LogUtil.d(TAG,pid+"摄像头开启成功");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCaptureButtonFifth.setVisibility(View.VISIBLE);
                        }
                    }, 0);
                }
            } else if ("6666".equals(pid)) {
                if (!mHandlerSixth.isOpened()) {
                    LogUtil.writeBusinessLog("摄像头："+pid+"开始连接");
                    mHandlerSixth.open(ctrlBlock);
                    final SurfaceTexture st = mUVCCameraViewSixth.getSurfaceTexture();
                    mHandlerSixth.startPreview(new Surface(st));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCaptureButtonSixth.setVisibility(View.VISIBLE);
                        }
                    }, 0);
                }
            } else if ("7777".equals(pid)) {
                if (!mHandlerSeven.isOpened()) {
                    LogUtil.writeBusinessLog("摄像头："+pid+"开始连接");
                    mHandlerSeven.open(ctrlBlock);
                    final SurfaceTexture st = mUVCCameraViewSeven.getSurfaceTexture();
                    mHandlerSeven.startPreview(new Surface(st));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCaptureButtonSeven.setVisibility(View.VISIBLE);
                        }
                    }, 0);
                }
            }

        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:" + device.getProductId());
            if ((mHandlerFirst != null) && !mHandlerFirst.isEqual(device)) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerFirst.close();
                        if (mFirstPreviewSurface != null) {
                            mFirstPreviewSurface.release();
                            mFirstPreviewSurface = null;
                        }
                        setCameraButton();
                    }
                }, 0);
            } else if ((mHandlerSecond != null) && !mHandlerSecond.isEqual(device)) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerSecond.close();
                        if (mSecondPreviewSurface != null) {
                            mSecondPreviewSurface.release();
                            mSecondPreviewSurface = null;
                        }
                        setCameraButton();
                    }
                }, 0);
            } else if ((mHandlerThird != null) && !mHandlerThird.isEqual(device)) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerThird.close();
                        if (mThirdPreviewSurface != null) {
                            mThirdPreviewSurface.release();
                            mThirdPreviewSurface = null;
                        }
                        setCameraButton();
                    }
                }, 0);
            } else if ((mHandlerFourth != null) && !mHandlerFourth.isEqual(device)) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerFourth.close();
                        if (mFourthPreviewSurface != null) {
                            mFourthPreviewSurface.release();
                            mFourthPreviewSurface = null;
                        }
                        setCameraButton();
                    }
                }, 0);
            } else if ((mHandlerFifth != null) && !mHandlerFifth.isEqual(device)) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerFifth.close();
                        if (mFifthPreviewSurface != null) {
                            mFifthPreviewSurface.release();
                            mFifthPreviewSurface = null;
                        }
                        setCameraButton();
                    }
                }, 0);
            } else if ((mHandlerSixth != null) && !mHandlerSixth.isEqual(device)) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerSixth.close();
                        if (mSixthPreviewSurface != null) {
                            mSixthPreviewSurface.release();
                            mSixthPreviewSurface = null;
                        }
                        setCameraButton();
                    }
                }, 0);
            } else if ((mHandlerSeven != null) && !mHandlerSeven.isEqual(device)) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerSeven.close();
                        if (mSixthPreviewSeven != null) {
                            mSixthPreviewSeven.release();
                            mSixthPreviewSeven = null;
                        }
                        setCameraButton();
                    }
                }, 0);
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onDettach:" + device.getProductId());
            Toast.makeText(getActivity(), "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onCancel:" + device.getProductId());
        }
    };

    /**
     * to access from CameraDialog
     *
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setCameraButton();
                }
            }, 0);
        }
    }

    private void setCameraButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((mHandlerFirst != null) && !mHandlerFirst.isOpened() && (mCaptureButtonFirst != null)) {
                    mCaptureButtonFirst.setVisibility(View.INVISIBLE);
                }
                if ((mHandlerSecond != null) && !mHandlerSecond.isOpened() && (mCaptureButtonSecond != null)) {
                    mCaptureButtonSecond.setVisibility(View.INVISIBLE);
                }
                if ((mHandlerThird != null) && !mHandlerThird.isOpened() && (mCaptureButtonThird != null)) {
                    mCaptureButtonThird.setVisibility(View.INVISIBLE);
                }
                if ((mHandlerFourth != null) && !mHandlerFourth.isOpened() && (mCaptureButtonFourth != null)) {
                    mCaptureButtonFourth.setVisibility(View.INVISIBLE);
                }
                if ((mHandlerFifth != null) && !mHandlerFifth.isOpened() && (mCaptureButtonFifth != null)) {
                    mCaptureButtonFifth.setVisibility(View.INVISIBLE);
                }
                if ((mHandlerSixth != null) && !mHandlerSixth.isOpened() && (mCaptureButtonSixth != null)) {
                    mCaptureButtonSixth.setVisibility(View.INVISIBLE);
                }
                if ((mHandlerSeven != null) && !mHandlerSeven.isOpened() && (mCaptureButtonSeven != null)) {
                    mCaptureButtonSeven.setVisibility(View.INVISIBLE);
                }
            }
        }, 0);
    }
}
