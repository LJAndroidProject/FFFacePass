package megvii.testfacepass;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

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
import okhttp3.Call;

public class InitConfig extends AppCompatActivity {

    private EditText edit_dustbin_query;
    private Button btn_getDustbinConfig,btn_getGoodsPos;
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
        btn_getGoodsPos = (Button)findViewById(R.id.btn_getGoodsPos);
        edit_dustbin_authorizationCode = (EditText)findViewById(R.id.edit_dustbin_authorizationCode);


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
                                String typeNumber = listBean.getBin_type() + id;


                                list.add(new DustbinStateBean(id,number,typeString,typeNumber,0,0,0,false,false,false,false,false,false,false));
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


        //  获取垃圾箱配置
        btn_getGoodsPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("正在获取售货机备选商品...");
                progressDialog.show();

                getGoodsPos();
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
            return DustbinENUM.WASTE_PAPER.toString();
        }else if(text.equals("F")){
            return DustbinENUM.BOTTLE.toString();
        }else{
            return null;
        }
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


                DataBaseUtil.getInstance(InitConfig.this).getDaoSession().getCommodityAlternativeBeanDao().insertInTx(commodityAlternativeBeans);

                progressDialog.dismiss();
                Toast.makeText(InitConfig.this, "获取售货机备选成功", Toast.LENGTH_SHORT).show();
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


    /**
     * 查询到对应的垃圾箱
     * */
    /*private void alertMessage(String positionName,final List<DustbinBean> list){
        StringBuilder stringBuilder = new StringBuilder();
       for(DustbinBean dustbinBean : list){
           stringBuilder.append(dustbinBean.getDustbinBoxType());
           stringBuilder.append(",");
       }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("你确定应用此配置吗？");
        alert.setCancelable(false);
        alert.setMessage("您输入的 此 ID 对应是位于" + positionName + " 的垃圾箱，垃圾箱配置如下: \n" + stringBuilder.toString());
        alert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DataBaseUtil.getInstance(InitConfig.this).setDustBinConfig(list);


                Toast.makeText(InitConfig.this, "配置应用成功", Toast.LENGTH_SHORT).show();
            }
        });
        alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.create();
        alert.show();
    }*/

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

}