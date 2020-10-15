package megvii.testfacepass;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengCallback;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import megvii.testfacepass.independent.ServerAddress;
import megvii.testfacepass.independent.bean.DustbinBean;
import megvii.testfacepass.independent.bean.DustbinConfig;
import megvii.testfacepass.independent.util.NetWorkUtil;
import okhttp3.Call;

public class APP extends Application {

    private long userId;

    private String deviceToken;

    private static List<DustbinBean> dustbinBeanList;

    private static DustbinConfig dustbinConfig;

    @Override
    public void onCreate() {
        super.onCreate();


        //  友盟推送

        // 在此处调用基础组件包提供的初始化函数 相应信息可在应用管理 -> 应用信息 中找到 http://message.umeng.com/list/apps
        // 参数一：当前上下文context；
        // 参数二：应用申请的Appkey（需替换）；
        // 参数三：渠道名称；
        // 参数四：设备类型，必须参数，传参数为UMConfigure.DEVICE_TYPE_PHONE则表示手机；传参数为UMConfigure.DEVICE_TYPE_BOX则表示盒子；默认为手机；
        // 参数五：Push推送业务的secret 填充Umeng Message Secret对应信息（需替换）
        UMConfigure.init(this, "5f59f33da4ae0a7f7d02d29a", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "e3a46cec8019c99194fd1054607e94a8");


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



                /*SharedPreferences.Editor editor = getSharedPreferences("appConfig", MODE_PRIVATE).edit();
                editor.putString("deviceToken",deviceToken);
                editor.apply();*/
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
        mPushAgent.setMessageHandler(messageHandler);

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

    public void setDustbinConfig(DustbinConfig dustbinConfig) {
        this.dustbinConfig = dustbinConfig;
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

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public List<DustbinBean> getDustbinBeanList() {
        return dustbinBeanList;
    }

    public void setDustbinBeanList(List<DustbinBean> dustbinBeanListPara) {
        dustbinBeanList = dustbinBeanListPara;
    }

    public static String getDeviceId(){
        if(dustbinConfig == null){
            return null;
        }

        return dustbinConfig.getDustbinDeviceId();
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
