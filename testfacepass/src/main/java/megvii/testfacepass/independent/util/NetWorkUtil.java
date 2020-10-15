package megvii.testfacepass.independent.util;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;


import megvii.testfacepass.APP;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetWorkUtil {
    public static NetWorkUtil netWorkUtil;
    private static OkHttpClient okHttpClient;
    private static Handler handler;

    private NetWorkUtil(){

    }

    public static NetWorkUtil getInstance(){
        if(okHttpClient == null){
            synchronized (NetWorkUtil.class){
                if(okHttpClient == null){
                    netWorkUtil = new NetWorkUtil();
                    okHttpClient = new OkHttpClient();
                    handler = new Handler(Looper.getMainLooper());
                }
            }
        }

        return netWorkUtil;
    }

    public void doPost(String url, Map<String,String> map, final NetWorkListener networkListener){

        FormBody.Builder formBody = new FormBody.Builder();

        if(map != null && map.size() > 0){
            for(Map.Entry<String,String> e : map.entrySet()){
                formBody.add(e.getKey(),e.getValue());
            }


            //  当前时间
            long nowTime = System.currentTimeMillis() / 1000 ;
            formBody.add("sign",md5(nowTime + key).toUpperCase());
            formBody.add("timestamp",String.valueOf(nowTime));


            //  如果设备id 不为 null 自动添加设备id到请求头
            if(APP.getDeviceId() != null){
                formBody.add("device_id",APP.getDeviceId());
            }
        }


        Request request = new Request.Builder().url( url).post(formBody.build()).build();
        Log.i("结果","multipartBody:"+request.toString());


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call,final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        networkListener.fail(call,e);
                    }
                });

            }

            @Override
            public void onResponse(final Call call, final Response response) {

                if(response.isSuccessful()){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                networkListener.success(response.body().string());
                            }catch (Exception e){
                                networkListener.error(e);
                            }
                        }
                    });
                }else{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            networkListener.error(new Exception("服务器异常"));
                        }
                    });
                }

            }
        });

    }


    /**
     * 注意回调是子线程
     * */
    public void doGet(String url, Map<String,String> map, final NetWorkListener networkListener){
        StringBuilder stringBuilder = new StringBuilder();

        if(map != null && map.size() > 0){
            stringBuilder.append("?");
            for(Map.Entry<String,String> e : map.entrySet()){
                stringBuilder.append(e.getKey());//e.getValue()
                stringBuilder.append("=");
                stringBuilder.append(e.getValue());
                stringBuilder.append("&");
            }
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
        }

        Request request = new Request.Builder().url(url + stringBuilder.toString()).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call,final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        networkListener.fail(call,e);
                    }
                });

            }

            @Override
            public void onResponse(final Call call, final Response response) {

                if(response.isSuccessful()){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                networkListener.success(response.body().string());
                            }catch (Exception e){
                                networkListener.error(e);
                            }
                        }
                    });
                }else{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            networkListener.error(new Exception("服务器异常"));
                        }
                    });
                }

            }
        });

    }

    private final static String key = "e0e9061d403f1898a501b8d7a840b949";
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

    public interface NetWorkListener{
        void success(String response);

        void fail(Call call, IOException e);

        void error(Exception e);
    }
}
