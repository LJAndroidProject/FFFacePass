package megvii.testfacepass;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import megvii.testfacepass.independent.ServerAddress;
import megvii.testfacepass.independent.bean.CommodityAlternativeBean;
import megvii.testfacepass.independent.bean.CommodityBean;
import megvii.testfacepass.independent.bean.DustbinConfig;
import megvii.testfacepass.independent.bean.DustbinENUM;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.bean.GetDustbinConfig;
import megvii.testfacepass.independent.bean.GetServerGoods;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.NetWorkUtil;
import megvii.testfacepass.utils.DownloadUtil;
import okhttp3.Call;

public class InitConfig extends AppCompatActivity {

    private EditText edit_dustbin_query;
    private Button btn_getDustbinConfig;
    private ProgressDialog progressDialog;
    private EditText edit_dustbin_authorizationCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //  查看是否存在配置，有配置则直接跳刀人脸识别界面
        if(DataBaseUtil.getInstance(InitConfig.this).hasDustBinConfig()){

            goMainActivity();

            return;
        }

        setContentView(R.layout.activity_init_config);

        

        //  初始化布局
        edit_dustbin_query = (EditText)findViewById(R.id.edit_dustbin_query);
        btn_getDustbinConfig = (Button)findViewById(R.id.btn_getDustbinConfig);
        edit_dustbin_authorizationCode = (EditText)findViewById(R.id.edit_dustbin_authorizationCode);


        String ANDROID_ID = Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);
        Log.i("安卓ID",ANDROID_ID);
        if("d800f7d28d59445d".equals(ANDROID_ID)){
            edit_dustbin_query.setText("GD-GZ-HP-DS-JT-001");
            edit_dustbin_authorizationCode.setText("CIUUHV");
        }else{
            edit_dustbin_query.setText("GD-GZ-HP-DS-JT-002");
            edit_dustbin_authorizationCode.setText("KDSU9E");
        }


        //  加载中弹窗...
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("正在加载中...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.create();


        //  获取售卖机备选
        getGoodsPos();



        //  绑定垃圾箱配置
        btn_getDustbinConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(edit_dustbin_query.getText()) || TextUtils.isEmpty(edit_dustbin_authorizationCode.getText())){
                    Toast.makeText(InitConfig.this, "内容为空", Toast.LENGTH_SHORT).show();
                    return;
                }


                progressDialog.setMessage("正在查询垃圾箱配置...");
                progressDialog.show();

                Map<String,String> map = new HashMap<>();
                map.put("device_id",edit_dustbin_query.getText().toString());
                map.put("mange_code",edit_dustbin_authorizationCode.getText().toString());
                NetWorkUtil.getInstance().doPost(ServerAddress.GET_DUSTBIN_CONFIG, map, new NetWorkUtil.NetWorkListener() {
                    @Override
                    public void success(String response) {

                        Log.i("结果",response);

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
                                String typeString = getDustbinType(listBean.getBin_type());
                                //  垃圾箱类型 例如A1 A2 B3 B5 C5 D6 D7 D8
                                String typeNumber = listBean.getBin_type();


                                list.add(new DustbinStateBean(id,number,typeString,typeNumber,0,0,0,0,false,false,false,false,false,false,false,false));
                            }

                            //  保存箱体配置
                            DataBaseUtil.getInstance(InitConfig.this).setDustBinStateConfig(list);


                            /*
                             * 保存垃圾箱配置
                             * */
                            DustbinConfig dustbinConfig = new DustbinConfig();
                            dustbinConfig.setDustbinDeviceId(listBeans.get(0).getDevice_id());  //  deviceID
                            dustbinConfig.setDustbinDeviceName(getDustbinConfig.getData().getDevice_name());    //  deviceName 部署在哪一个小区
                            dustbinConfig.setHasVendingMachine(getDustbinConfig.getData().getHas_amat() == 1);  //  是否有售卖机
                            //  如果存在售卖机则创建售卖机货道
                            if(getDustbinConfig.getData().getHas_amat() == 1){
                                initReplenishment();
                            }
                            DataBaseUtil.getInstance(InitConfig.this).getDaoSession().getDustbinConfigDao().insertOrReplace(dustbinConfig);    //  保存配置

                            goMainActivity();
                        }


                        progressDialog.dismiss();
                        Toast.makeText(InitConfig.this, getDustbinConfig.getMsg(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void fail(Call call, IOException e) {
                        Toast.makeText(InitConfig.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void error(Exception e) {
                        Toast.makeText(InitConfig.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

            }
        });




    }


    /**
     * 跳转到首页
     * */
    private void goMainActivity(){
        Intent intent = new Intent(InitConfig.this,MainActivity.class);
        startActivity(intent);

        this.finish();
    }


    /**
     * A：厨余垃圾，B：其他垃圾，C：可回收垃圾，D：有害垃圾
     * */
    private String getDustbinType(String text){
        if(text.equals("A")){
            return DustbinENUM.KITCHEN.toString();
        }else if(text.equals("B")){
            return DustbinENUM.OTHER.toString();
        }else if(text.equals("C")){
            return DustbinENUM.RECYCLABLES.toString();
        }else if(text.equals("D")){
            return DustbinENUM.HARMFUL.toString();
        }else if(text.equals("E")){
            return DustbinENUM.BOTTLE.toString();
        }else if(text.equals("F")){
            return DustbinENUM.WASTE_PAPER.toString();
        }else{
            return null;
        }
    }


    /**
     * 检查新版本
     * */
    ProgressDialog downloadProgressDialog;
    private void checkNewVersion(){
        downloadProgressDialog = new ProgressDialog(this);
        downloadProgressDialog.setCancelable(false);
        downloadProgressDialog.setMessage("准备下载安装包...");
        downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        downloadProgressDialog.create();

        NetWorkUtil.getInstance().doPost(ServerAddress.REGISTER_TCP, null, new NetWorkUtil.NetWorkListener() {
            @Override
            public void success(String response) {
                if(true){

                    AlertDialog.Builder alert = new AlertDialog.Builder(InitConfig.this);
                    alert.setCancelable(false);
                    alert.setTitle("版本更新提示：");
                    alert.setMessage("发现新版本");
                    alert.setPositiveButton("下载", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            download("");


                            downloadProgressDialog.show();
                        }
                    });
                    alert.create();
                    alert.show();
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



    public void download(final String url){

        final String saveDir = Environment.getExternalStorageDirectory().toString();

        //  文件名称
        final String fileName = System.currentTimeMillis() + ".apk";

        new Thread(new Runnable() {
            @Override
            public void run() {

                DownloadUtil.get().download(url, saveDir, fileName, new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(File file) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                downloadProgressDialog.dismiss();
                            }
                        });
                        installApk(file);
                    }

                    @Override
                    public void onDownloading(final int progress) {
                        Log.i("结果","下载进度" + progress);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                downloadProgressDialog.setProgress(progress);
                            }
                        });
                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                downloadProgressDialog.dismiss();
                            }
                        });
                    }
                });

            }
        }).start();
    }


    /**
     * 打开 安装包 开始安装
     * */
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
     * 获取商品列表，不管有没有售卖机 都将备选列表下载下来
     * */
    private void getGoodsPos(){
        NetWorkUtil.getInstance().doGet(ServerAddress.GET_GOODS_POS, null, new NetWorkUtil.NetWorkListener() {
            @Override
            public void success(String response) {
                GetServerGoods getServerGoods = new Gson().fromJson(response,GetServerGoods.class);
                //  获取商品列表
                List<GetServerGoods.DataBean.ListBean> listBeans = getServerGoods.getData().getList();



                List<CommodityAlternativeBean> commodityAlternativeBeans = new ArrayList<>();
                for(GetServerGoods.DataBean.ListBean listBean : listBeans){
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


                DataBaseUtil.getInstance(InitConfig.this).getDaoSession().getCommodityAlternativeBeanDao().insertOrReplaceInTx(commodityAlternativeBeans);

                progressDialog.dismiss();
                Toast.makeText(InitConfig.this, "获取售货机备选商品成功！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void fail(Call call, IOException e) {
                Toast.makeText(InitConfig.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                AlertDialog.Builder alert = new AlertDialog.Builder(InitConfig.this);
                //alert.setCancelable(false);
                alert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                        finish();
                    }
                });
                alert.setMessage("没有网络，请连网后再试。");
                alert.create();
                alert.show();
                progressDialog.dismiss();
            }

            @Override
            public void error(Exception e) {
                Toast.makeText(InitConfig.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }



    /**
     * 创建售货机货道
     * */
    private void initReplenishment(){
        //  创建货道列表
        List<CommodityBean> commodityBeanList = new ArrayList<>();

        //  货道数
        int number = 1;

        //  前者为货道编号 后者为货道可容纳商品数量
        //  第一层 可存放8个
        for(; number <= 9; number += 2){
            for(int tierChildrenCommodityNumber = 1 ; tierChildrenCommodityNumber <= 8 ; tierChildrenCommodityNumber++){
                commodityBeanList.add(new CommodityBean(null,0,null,1,1,number,tierChildrenCommodityNumber,0,0));
            }
        }

        //  第二层 可存放3个
        for(; number <= 19; number +=2 ){
            for(int tierChildrenCommodityNumber = 1 ; tierChildrenCommodityNumber <= 3 ; tierChildrenCommodityNumber++){
                commodityBeanList.add(new CommodityBean(null,0,null,1,1,number,tierChildrenCommodityNumber,0,0));
            }
        }

        //  三 至 四层 可存放10个
        for(; number <= 40; number++){
            for(int tierChildrenCommodityNumber = 1 ; tierChildrenCommodityNumber <= 10 ; tierChildrenCommodityNumber++){
                commodityBeanList.add(new CommodityBean(null,0,null,1,1,number,tierChildrenCommodityNumber,0,0));
            }
        }

        for(; number <= 60; number++){
            for(int tierChildrenCommodityNumber = 1 ; tierChildrenCommodityNumber <= 4 ; tierChildrenCommodityNumber++){
                commodityBeanList.add(new CommodityBean(null,0,null,1,1,number,tierChildrenCommodityNumber,0,0));
            }
        }

        DataBaseUtil.getInstance(this).getDaoSession().getCommodityBeanDao().insertInTx(commodityBeanList);

    }

    /**
     * 跳转到校准界面
     * */
    public void goWeightCalibration(View view){
        startActivity(new Intent(InitConfig.this,WeightCalibrationActivity.class));
    }


    /**
     * 跳转 debug 界面
     * */
    public void goDebug(View view){
        startActivity(new Intent(InitConfig.this, DebugActivity.class));
    }


}