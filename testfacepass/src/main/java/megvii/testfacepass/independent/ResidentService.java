package megvii.testfacepass.independent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import megvii.testfacepass.APP;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.bean.StateCallBean;
import megvii.testfacepass.independent.manage.SerialPortRequestByteManage;
import megvii.testfacepass.independent.manage.SerialPortRequestManage;
import megvii.testfacepass.independent.util.NetWorkUtil;
import megvii.testfacepass.independent.util.SerialPortUtil;
import megvii.testfacepass.independent.util.TCPConnectUtil;
import megvii.testfacepass.network.DustBinRecordRequestParams;
import megvii.testfacepass.utils.DownloadUtil;
import megvii.testfacepass.utils.LogUtil;
import okhttp3.Call;

/**
 * 常驻服务
 */
public class ResidentService extends Service {
    private String TAG = "常驻服务";
    private Gson gson = new Gson();

    //  下载安装包中
    private boolean downloading = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Log.i(TAG, "服务被销毁");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "服务被创建");
        EventBus.getDefault().register(this);
        /**
         * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! 暂不开启 ！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
         *
         * */

        //  设备状态上报服务器
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "上传状态");

                if (APP.dustbinBeanList != null && APP.dustbinBeanList.size() > 0) {
                    NetWorkUtil.getInstance().stateUpload(ServerAddress.STATE_UPLOAD, (int) getAppVersionCode(ResidentService.this), APP.dustbinBeanList, new NetWorkUtil.NetWorkListener() {
                        @Override
                        public void success(String response) {
                            Log.i(TAG, response);
                            StateCallBean stateCallBean = gson.fromJson(response, StateCallBean.class);
                            //  大于，并且没有已经在下载 所以要更新
                            if (stateCallBean.getData() != null && stateCallBean.getData().getVersion_code() > getAppVersionCode(ResidentService.this) && !downloading) {
                                download(stateCallBean.getData().getApk_download_url());
                            }

                            //  如果是设备已离线则重连
                            if (stateCallBean.getData() != null && stateCallBean.getData().getEq_status() == 0) {
                                //  断开连接
                                TCPConnectUtil.getInstance().disconnect();
                                //  重新连接
                                TCPConnectUtil.getInstance().connect();

                                NetWorkUtil.getInstance().errorUpload("上传状态时发现设备离线");
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
            }
        };

        Timer timer = new Timer();
        timer.schedule(timerTask, 1, 60 * 1000);


        //  获取垃圾箱状态  暂定30秒获取一次，之前1秒获取一次全部桶 浪费资源且占用串口通道传输数据
        TimerTask getDustbinState = new TimerTask() {
            @Override
            public void run() {
                if (APP.dustbinBeanList != null && APP.dustbinBeanList.size() > 0) {
                    for (DustbinStateBean dustbinBeanList : APP.dustbinBeanList) {
                        Log.i(TAG, "获取垃圾箱状态" + dustbinBeanList.getDoorNumber());
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().getDate(dustbinBeanList.getDoorNumber()));
                        try {
                            Thread.sleep(400);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        Timer timer2 = new Timer();
        timer2.schedule(getDustbinState, 1, 30 * 1000);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void uploadDustBinRecord(DustBinRecordRequestParams params) {
        LogUtil.d(TAG, "EventBus收到map：" + params.getRequestMap());
        NetWorkUtil.getInstance().doPost(ServerAddress.DUSTBIN_RECORD, params.getRequestMap(), new NetWorkUtil.NetWorkListener() {
            @Override
            public void success(String response) {
                LogUtil.d(TAG, "投递记录成功上传服务器: " + response);
                LogUtil.writeBusinessLog("投递记录成功上传服务器: " + response);
//                Log.i(exit_mode == EXIT_MODE.TIME_TASK ? DEBUG_TAG_TASK : DEBUG_TAG, response);
//                //  如果是瓶子类型,则清空瓶子
//                if (dustbinStateBean.getDustbinBoxType().equals(DustbinENUM.BOTTLE.toString())) {
//                    bottleNumber = 0;
//                }
            }

            @Override
            public void fail(Call call, IOException e) {
                LogUtil.d("投递失败 " + e.getMessage());
                LogUtil.writeBusinessLog("投递失败 " + e.getMessage());
            }

            @Override
            public void error(Exception e) {
                LogUtil.d("投递失败 " + e.getMessage());
                LogUtil.writeBusinessLog("投递失败 " + e.getMessage());
            }
        });
    }


    public static long getAppVersionCode(Context context) {
        long appVersionCode = 0;
        try {
            PackageInfo packageInfo = context.getApplicationContext().getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return appVersionCode;
    }

    /**
     * 下载安装包
     */
    public void download(final String url) {

        final String saveDir = Environment.getExternalStorageDirectory().toString();

        //  文件名称
        //final String fileName = System.currentTimeMillis() + ".apk";

        final String fileName = "newPackage.apk";

        new Thread(new Runnable() {
            @Override
            public void run() {

                downloading = true;

                DownloadUtil.get().download(url, saveDir, fileName, new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(File file) {


                        //  延迟一下比较好
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                downloading = false;
                            }
                        }, 10 * 1000);

                        installApk(file);
                    }

                    @Override
                    public void onDownloading(int progress) {

                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        downloading = false;
                    }
                });

            }
        }).start();
    }


    /**
     * 打开 安装包 开始安装
     */
    private void installApk(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 7.0+以上版本
            Uri apkUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        startActivity(intent);
    }


    /**
     * 过滤字段
     * Gson gson = new GsonBuilder().setExclusionStrategies(myExclusionStrategy).create();
     */
    ExclusionStrategy myExclusionStrategy = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes fa) {
            if ("isFull".equals(fa.getName()) || "id".equals(fa.getName()) || "dustbinWeight".equals(fa.getName()) || "temperature".equals(fa.getName())) {
                return false;
            }

            return true;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }

    };


}
