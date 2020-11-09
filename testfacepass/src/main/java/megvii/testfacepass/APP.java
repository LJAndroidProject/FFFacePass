package megvii.testfacepass;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.serialportlibrary.service.impl.SerialPortService;
import com.serialportlibrary.util.ByteStringUtil;

import org.greenrobot.eventbus.EventBus;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import megvii.testfacepass.independent.bean.DustbinConfig;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.bean.DebugLogBean;
import megvii.testfacepass.independent.manage.SerialPortResponseManage;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.SerialPortUtil;
import megvii.testfacepass.independent.util.TCPConnectUtil;

public class APP extends Application {

    public final static String TAG = "硬件串口对接日志";

    public static long userId;

    private String deviceToken;

    public static List<DustbinStateBean> dustbinBeanList;

    private static DustbinConfig dustbinConfig;

    private Handler handler;


    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler(Looper.getMainLooper());

        //  设置垃圾箱配置
        DustbinConfig dustbinConfig = DataBaseUtil.getInstance(this).getDaoSession().getDustbinConfigDao().queryBuilder().unique();
        setDustbinConfig(dustbinConfig);
        //  代表 全局 垃圾桶 list 对象
        setDustbinBeanList(DataBaseUtil.getInstance(this).getDustbinByType(null));

        // 如果垃圾箱为 null 则创建
        /*if(APP.dustbinBeanList == null || APP.dustbinBeanList .size() == 0){
            List<DustbinStateBean> dustbinStateBeans = new ArrayList<>();
            for(int i = 1 ;i <= 4 ;i++){
                DustbinStateBean dustbinStateBean = new DustbinStateBean();
                dustbinStateBean.setDoorNumber(i);
                dustbinStateBeans.add(dustbinStateBean);
            }

            APP.dustbinBeanList = dustbinStateBeans;
        }*/


        //  注册串口监听,与硬件进行通信
        SerialPortUtil.getInstance().receiveListener(new SerialPortService.SerialResponseByteListener() {
            @Override
            public void response(final byte[] response) {
                //  通过事件总线发送出去
                //Log.i("串口接收",ByteStringUtil.byteArrayToHexStr(response));
                /*final DebugLogBean debugLogBean = new DebugLogBean();
                debugLogBean.setString(ByteStringUtil.byteArrayToHexStr(response));
                EventBus.getDefault().post(debugLogBean);*/

                if(DebugActivity.debug_log_tv != null){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            DebugActivity.debug_log_tv.append("接收:" + ByteStringUtil.byteArrayToHexStr(response));
                            DebugActivity.debug_log_tv.append("\n");
                        }
                    });
                }

                SerialPortResponseManage.inOrderString(APP.this,response);
            }
        });


        //  友盟推送，暂时无用

        // 在此处调用基础组件包提供的初始化函数 相应信息可在应用管理 -> 应用信息 中找到 http://message.umeng.com/list/apps
        // 参数一：当前上下文context；
        // 参数二：应用申请的Appkey（需替换）；
        // 参数三：渠道名称；
        // 参数四：设备类型，必须参数，传参数为UMConfigure.DEVICE_TYPE_PHONE则表示手机；传参数为UMConfigure.DEVICE_TYPE_BOX则表示盒子；默认为手机；
        // 参数五：Push推送业务的secret 填充Umeng Message Secret对应信息（需替换）
        /*UMConfigure.init(this, "5f59f33da4ae0a7f7d02d29a", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "e3a46cec8019c99194fd1054607e94a8");


        // 获取消息推送代理示例
        PushAgent mPushAgent = PushAgent.getInstance(this);
        // 注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {

            @Override
            public void onSuccess(String deviceToken) {
                //  获取用于区分用户的 deviceToken
                Log.i("推送调试（如果收不到，看看是不是卸载了）",deviceToken);


                setDeviceToken(deviceToken);

                long nowTime = System.currentTimeMillis() / 1000 ;
                String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                Map<String,String> map = new HashMap<>();
                map.put("device_id", androidID);
                map.put("device_token",deviceToken);
                map.put("sign",md5(androidID + nowTime + key).toUpperCase());
                map.put("timestamp",String.valueOf(nowTime));

                NetWorkUtil.getInstance().doPost(ServerAddress.DEVICE_REGISTER, map, new NetWorkUtil.NetWorkListener() {
                    @Override
                    public void success(String response) {
                        Log.i("人脸注册","deviceToken 注册成功" + response);
                    }

                    @Override
                    public void fail(Call call, IOException e) {

                    }

                    @Override
                    public void error(Exception e) {

                    }
                });



                *//*SharedPreferences.Editor editor = getSharedPreferences("appConfig", MODE_PRIVATE).edit();
                editor.putString("deviceToken",deviceToken);
                editor.apply();*//*
            }

            @Override
            public void onFailure(String s, String s1) {
                //  Log.e(TAG,"注册失败：-------->  " + "s:" + s + ",s1:" + s1);
            }
        });


        UmengMessageHandler messageHandler = new UmengMessageHandler(){
            @Override
            public void dealWithCustomMessage(final Context context, final UMessage msg) {
                Log.i("推送调试",msg.custom);
                EventBus.getDefault().post(msg);
            }
        };
        mPushAgent.setMessageHandler(messageHandler);*/

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


    public DustbinConfig getDustbinConfig() {
        return dustbinConfig;
    }

    public void setDustbinConfig(DustbinConfig dustbinConfigBean) {
        dustbinConfig = dustbinConfigBean;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getUserId() {
        return userId;
    }

    public static void setUserId(long userID) {
        userId = userID;
    }

    public List<DustbinStateBean> getDustbinBeanList() {
        return dustbinBeanList;
    }

    public void setDustbinBeanList(List<DustbinStateBean> dustbinBeanListPara) {
        dustbinBeanList = dustbinBeanListPara;
    }

    public static String getDeviceId(){
        if(dustbinConfig == null){
            return null;
        }

        return dustbinConfig.getDustbinDeviceId();
    }


    /**
     * 修改垃圾箱
     * */
    public static void setDustbinState(DustbinStateBean dustbinStateBean){
        // 1. boolean hasMan = false;
        for(int i = 1 ; i < dustbinBeanList.size(); i++){
            if(dustbinBeanList.get(i).getDoorNumber() == dustbinStateBean.getDoorNumber()){

                //  之前没有设置 垃圾箱  id 导致为 null

                dustbinStateBean.setId(dustbinBeanList.get(i).getId());
                dustbinBeanList.set(i,dustbinStateBean);
            }

            // 2. 有一个桶 有人 就 true
            /*if(dustbinStateBean.getProximitySwitch()){
                hasMan = true;
            }*/
        }

        //  3.如果所有桶都没有人就把userID 设置为 0 并且退回首页
        /*if(hasMan){
            userId = 0;
        }*/
    }

    public static void getDustbinState(){

    }


    /**
     * 是否是 debug 模式
     * */
    public static boolean isDebugMode(){
        return true;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        TCPConnectUtil.getInstance().disconnect();
    }


    @Override
    public void onTrimMemory(int level) {
        // 程序在内存清理的时候执行
        //TCPConnectUtil.getInstance().disconnect();
        super.onTrimMemory(level);
    }
}
