package megvii.testfacepass;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import megvii.testfacepass.independent.ServerAddress;
import megvii.testfacepass.independent.bean.CommodityAlternativeBean;
import megvii.testfacepass.independent.bean.DustbinBean;
import megvii.testfacepass.independent.bean.DustbinENUM;
import megvii.testfacepass.independent.bean.GetServerGoods;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.NetWorkUtil;
import okhttp3.Call;

public class InitConfig extends AppCompatActivity {

    private EditText edit_dustbin_query;
    private Button btn_getDustbinConfig,btn_getGoodsPos;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_config);

        edit_dustbin_query = (EditText)findViewById(R.id.edit_dustbin_query);
        btn_getDustbinConfig = (Button)findViewById(R.id.btn_getDustbinConfig);
        btn_getGoodsPos = (Button)findViewById(R.id.btn_getGoodsPos);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("正在加载中...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.create();


        //  获取垃圾箱配置
        btn_getDustbinConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("正在查询垃圾箱配置...");
                progressDialog.show();

                NetWorkUtil.getInstance().doGet(ServerAddress.GET_DUSTBIN_CONFIG, null, new NetWorkUtil.NetWorkListener() {
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



        btn_getGoodsPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("正在获取售货机备选商品...");
                progressDialog.show();

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
}