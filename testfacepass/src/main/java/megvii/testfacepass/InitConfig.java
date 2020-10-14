package megvii.testfacepass;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import megvii.testfacepass.independent.bean.DustbinBean;
import megvii.testfacepass.independent.bean.DustbinConfig;
import megvii.testfacepass.independent.bean.DustbinENUM;
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
            Intent intent = new Intent(InitConfig.this,MainActivity.class);
            startActivity(intent);

            this.finish();

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
                map.put("deviceId",edit_dustbin_query.getText().toString());
                map.put("deviceCode",edit_dustbin_authorizationCode.getText().toString());
                NetWorkUtil.getInstance().doGet(ServerAddress.GET_DUSTBIN_CONFIG, map, new NetWorkUtil.NetWorkListener() {
                    @Override
                    public void success(String response) {



                        List<DustbinBean> list = new ArrayList<>();
                        list.add(new DustbinBean(1, DustbinENUM.KITCHEN.toString(),false,0));
                        list.add(new DustbinBean(2,DustbinENUM.KITCHEN.toString(),false,0));
                        list.add(new DustbinBean(3,DustbinENUM.HARMFUL.toString(),false,0));
                        list.add(new DustbinBean(4,DustbinENUM.OTHER.toString(),false,0));
                        list.add(new DustbinBean(5,DustbinENUM.OTHER.toString(),false,0));
                        list.add(new DustbinBean(6,DustbinENUM.OTHER.toString(),false,0));
                        list.add(new DustbinBean(7,DustbinENUM.WASTE_PAPER.toString(),false,0));
                        list.add(new DustbinBean(8,DustbinENUM.BOTTLE.toString(),false,0));
                        DataBaseUtil.getInstance(InitConfig.this).setDustBinConfig(list);


                        //alertMessage();

                        //  有售货机则显示获取售货机配置
                        if(true){
                            btn_getGoodsPos.setVisibility(View.VISIBLE);
                        }

                        Toast.makeText(InitConfig.this, "获取垃圾箱配置成功", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
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
    private void alertMessage(String positionName,final List<DustbinBean> list){
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

}