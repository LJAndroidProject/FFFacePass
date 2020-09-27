package megvii.testfacepass.independent.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.Map;


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

    public interface NetWorkListener{
        void success(String response);

        void fail(Call call, IOException e);

        void error(Exception e);
    }
}
