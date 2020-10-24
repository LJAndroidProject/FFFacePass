package megvii.testfacepass.independent.util;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import megvii.testfacepass.APP;
import megvii.testfacepass.independent.ServerAddress;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.bean.DustbinStateUploadBean;
import megvii.testfacepass.independent.bean.ErrorReportBean;
import megvii.testfacepass.independent.bean.GeneralBean;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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


    /**
     * 错误上报
     *
     * */
    public void errorUpload(ErrorReportBean errorReportBean){
        Log.i("错误上报结果",errorReportBean.toString());

        if(APP.getDeviceId() != null){

            long nowTime = System.currentTimeMillis() / 1000 ;

            /**
             * sign	是	string	签名
             * timestamp	是	string	当前时间戳
             * device_id	是	string	设备ID
             * msg	是	string	错误描述
             * order_number	否	string	指令编码
             * data	否	int	指令错误类型
             * order_string	否	string	原始指令
             * door_number	否	int	几号门
             * time	否	string	错误发生时间戳
             * */
            FormBody.Builder formBody = new FormBody.Builder();
            formBody.add("sign",md5(nowTime + key).toUpperCase());
            formBody.add("timestamp",String.valueOf(nowTime));
            formBody.add("device_id", APP.getDeviceId());
            formBody.add("msg",errorReportBean.getMsg());
            formBody.add("order_number",errorReportBean.getOrderNumber());
            formBody.add("data",errorReportBean.getData());
            formBody.add("order_string",errorReportBean.getOrderString());
            formBody.add("door_number",String.valueOf(errorReportBean.getDoorNumber()));
            formBody.add("time",String.valueOf(errorReportBean.getTime() / 1000));


            Request request = new Request.Builder().url(ServerAddress.ERROR_UPLOAD).post(formBody.build()).build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(final Call call,final IOException e) {

                }

                @Override
                public void onResponse(final Call call, final Response response) throws IOException {
                        Log.i("错误上报结果",response.body().string());
                }
            });
        }


    }


    public void fileUpload(File file,final FileUploadListener fileUploadListener){
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpg"), file);

        // 文件上传的请求体封装
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), requestBody)
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(ServerAddress.FILE_UPLOAD)
                .post(multipartBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                fileUploadListener.error(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    //  通用 bean
                    GeneralBean generalBean = new Gson().fromJson(response.body().string(),GeneralBean.class);
                    fileUploadListener.success(generalBean.getData());
                }else{
                    fileUploadListener.error(new Exception("状态码异常"));
                }
            }
        });
    }

    /**
     *
     * 上传垃圾箱状态
     * */
    public void stateUpload(String url, List<DustbinStateBean> dustbinStateBeans, final NetWorkListener netWorkListener){
        DustbinStateUploadBean dustbinStateUploadBean = new DustbinStateUploadBean();

        List<DustbinStateUploadBean.ListBean> listBean = new ArrayList<>();
        for(DustbinStateBean dustbinStateBean:dustbinStateBeans){
            listBean.add(new DustbinStateUploadBean.ListBean(dustbinStateBean.getDustbinWeight(),dustbinStateBean.getId(),dustbinStateBean.getIsFull(),dustbinStateBean.getTemperature()));
        }
        dustbinStateUploadBean.setList(listBean);
        long nowTime = System.currentTimeMillis() / 1000 ;
        dustbinStateUploadBean.setSign(md5(nowTime + key).toUpperCase());
        dustbinStateUploadBean.setTimestamp(String.valueOf(nowTime));

        RequestBody body = FormBody.create(MediaType.parse("application/json"), new Gson().toJson(dustbinStateUploadBean));

        Request request = new Request.Builder().url(url).post(body).build();

        Log.i("传输内容",new Gson().toJson(dustbinStateUploadBean));
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                netWorkListener.error(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                netWorkListener.success(response.body().string());
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


    public interface FileUploadListener{
        void success(String fileUrl);

        void error(Exception e);
    }
}
