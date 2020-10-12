package megvii.testfacepass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import megvii.testfacepass.independent.ServerAddress;
import megvii.testfacepass.independent.bean.CommodityAlternativeBean;
import megvii.testfacepass.independent.bean.CommodityBean;
import megvii.testfacepass.independent.bean.CommodityBeanDao;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.NetWorkUtil;
import okhttp3.Call;

public class ReplenishmentActivity extends AppCompatActivity {

    RecyclerView replenishment_recyclerview;

    private String TAG = "结果";

    //  查询货道列表
    List<CommodityBean> result;


    public final static int UPDATE_CODE = 100;


    ReplenishmentAdapter replenishmentAdapter;

    Button replenishment_clear_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replenishment);


        /*        SharedPreferences sharedPreferences = getSharedPreferences("appConfig",MODE_PRIVATE);
        String deviceToken = sharedPreferences.getString("deviceToken",null);*/

        replenishment_clear_btn = (Button)findViewById(R.id.replenishment_clear_btn);






        //  商品备选
        List<CommodityAlternativeBean> commodityAlternativeBean = DataBaseUtil.getInstance(this).getDaoSession().getCommodityAlternativeBeanDao().queryBuilder().build().list();

        if(commodityAlternativeBean != null && commodityAlternativeBean.size() > 0 ){
            Toast.makeText(this, "存在备选商品列表", Toast.LENGTH_SHORT).show();
        }else{

            List<CommodityAlternativeBean> commodityAlternativeBeans = new ArrayList<>();
            commodityAlternativeBeans.add(new CommodityAlternativeBean((long) 1,9,"面包",false,0,true,"https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=2383288134,2143816432&fm=15&gp=0.jpg",360));
            commodityAlternativeBeans.add(new CommodityAlternativeBean((long) 2,6,"小面包",false,0,true,"https://file-cloud.yst.com.cn/website/2020/04/14/c58577d3507046b79e28564fc9f4767c.png",360));
            commodityAlternativeBeans.add(new CommodityAlternativeBean((long) 3,5.5,"方便面",true,500,true,"https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=3214305998,151990978&fm=26&gp=0.jpg",180));
            commodityAlternativeBeans.add(new CommodityAlternativeBean((long) 4,10,"瑞士卷",false,0,false,"https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=2975923086,1932516814&fm=26&gp=0.jpg",90));
            commodityAlternativeBeans.add(new CommodityAlternativeBean((long) 5,4,"纯牛奶",true,400,false,"https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2336638272,2853397711&fm=26&gp=0.jpg",90));
            commodityAlternativeBeans.add(new CommodityAlternativeBean((long) 6,2,"矿泉水",true,200,false,"https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=1132615959,1940036971&fm=26&gp=0.jpg",360));


            DataBaseUtil.getInstance(this).getDaoSession().getCommodityAlternativeBeanDao().insertInTx(commodityAlternativeBeans);
        }





        /*List<CommodityBean> commodityBeanListPara = new ArrayList<>() ;
        for(int i =0 ;i < 10 ;i ++){
            CommodityAlternativeBean commodityAlternativeBean = new CommodityAlternativeBean((long) 1,10,"康师傅方便面",false,0,true,"https://www.norkm.com",100);

            CommodityBean commodityBean = new CommodityBean();
            commodityBean.setAddTime(System.currentTimeMillis());
            commodityBean.setCupboardNumber(1);
            commodityBean.setTierNumber(2);
            commodityBean.setDateInProduced(System.currentTimeMillis());
            commodityBean.setTierChildrenNumber(i);
            commodityBean.setCommodityID(i);

            commodityBean.setCommodityAlternativeBean(commodityAlternativeBean);





            commodityBeanListPara.add(commodityBean);
        }


        DataBaseUtil.getInstance(this).getDaoSession().getCommodityBeanDao().insertOrReplaceInTx(commodityBeanListPara);*/




        //  查询商品列表
        List<CommodityBean> commodityBeanList = DataBaseUtil.getInstance(this).getDaoSession().getCommodityBeanDao().queryBuilder().list();



        if(commodityBeanList == null || commodityBeanList.size() == 0){
            initReplenishment();
        }

        result = DataBaseUtil.getInstance(this).getDaoSession().getCommodityBeanDao().queryBuilder().where(CommodityBeanDao.Properties.TierChildrenCommodityNumber.eq(1)).orderAsc(CommodityBeanDao.Properties.TierNumber,CommodityBeanDao.Properties.TierChildrenNumber).build().list();
        replenishment_recyclerview = (RecyclerView) findViewById(R.id.replenishment_recyclerview);
        replenishmentAdapter = new ReplenishmentAdapter(R.layout.item_replenishment,result);


        replenishmentAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(ReplenishmentActivity.this,ReplenishmentDetailsActivity.class);
                intent.putExtra("listPosition",position);
                intent.putExtra("cupboardNumber",result.get(position).getCupboardNumber());
                intent.putExtra("tierNumber",result.get(position).getTierNumber());
                intent.putExtra("tierChildrenNumber",result.get(position).getTierChildrenNumber());
                intent.putExtra("commodityID",result.get(position).getCommodityID());

                Log.i("结果","传递" + result.get(position).toString());

                startActivityForResult(intent,UPDATE_CODE);
            }
        });

        replenishment_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        replenishment_recyclerview.setAdapter(replenishmentAdapter);



        for(CommodityBean commodityBean : commodityBeanList){
            Log.i(TAG,"已有配置：" + commodityBean.toString());
        }

        Log.i(TAG,"全部：" + commodityBeanList.size());
        Log.i(TAG,"货道列表：" + result.size());

        /*List<CommodityBean> commodityBeanList = DataBaseUtil.getInstance(this).getDaoSession().getCommodityBeanDao().queryBuilder().where(CommodityBeanDao.Properties.CommodityID.eq(2)).build().list();
        if(commodityBeanList != null && commodityBeanList.size() > 0){
            Log.i("结果",DataBaseUtil.getInstance(this).getDaoSession().getCommodityBeanDao().queryBuilder().list().size() + "," + commodityBeanList.toString());
        }else{
            Log.i("结果","空");
        }*/


        replenishment_clear_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  清空
                DataBaseUtil.getInstance(ReplenishmentActivity.this).getDaoSession().getCommodityBeanDao().deleteAll();

                //  重新初始化
                initReplenishment();


                List<CommodityBean> newData = DataBaseUtil.getInstance(ReplenishmentActivity.this).getDaoSession().getCommodityBeanDao().queryBuilder().where(CommodityBeanDao.Properties.TierChildrenCommodityNumber.eq(1)).orderAsc(CommodityBeanDao.Properties.TierNumber,CommodityBeanDao.Properties.TierChildrenNumber).build().list();
                replenishmentAdapter.setNewData(newData);




            }
        });

    }





    /**
     *
     * 初始化 售货机货道 配置
     * */
    private void initReplenishment(){
        //  创建货道列表
        List<CommodityBean> commodityBeanList = new ArrayList<>();

        /*for(int h = 1 ; h <= 6 ; h++){
            for(int w = 1 ; w <= 10 ; w++){
                for(int z = 1 ; z <= 10 ; z++){
                    commodityBeanList.add(new CommodityBean(null,0,null,1,w,h,z,0,0));
                    Log.i("结果",commodityBeanList.get((commodityBeanList.size()-1)).toString());
                }
            }
        }*/

        //  货道数
        int number = 1;

        //  第一层 可存放8个
        for(; number <= 9; number += 2){
            for(int tierChildrenCommodityNumber = 0 ; tierChildrenCommodityNumber <= 8 ; tierChildrenCommodityNumber++){
                commodityBeanList.add(new CommodityBean(null,0,null,1,1,number,tierChildrenCommodityNumber,0,0));
            }
        }

        //  第二层 可存放3个
        for(; number <= 19; number +=2 ){
            for(int tierChildrenCommodityNumber = 0 ; tierChildrenCommodityNumber <= 3 ; tierChildrenCommodityNumber++){
                commodityBeanList.add(new CommodityBean(null,0,null,1,1,number,tierChildrenCommodityNumber,0,0));
            }
        }

        //  三 至 六层 可存放10个
        for(; number <= 60; number++){
            for(int tierChildrenCommodityNumber = 0 ; tierChildrenCommodityNumber <= 10 ; tierChildrenCommodityNumber++){
                commodityBeanList.add(new CommodityBean(null,0,null,1,1,number,tierChildrenCommodityNumber,0,0));
            }
        }


        Log.i(TAG, "创建成功 : " + commodityBeanList.size() );
        DataBaseUtil.getInstance(this).getDaoSession().getCommodityBeanDao().insertInTx(commodityBeanList);

    }


    public static void i(String tag, String msg) {  //信息太长,分段打印
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //  把4*1024的MAX字节打印长度改为2001字符数
        int max_str_length = 2001 - tag.length();
        //大于4000时
        while (msg.length() > max_str_length) {
            Log.i(tag, msg.substring(0, max_str_length));
            msg = msg.substring(max_str_length);
        }
        //剩余部分
        Log.i(tag, msg);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == UPDATE_CODE && resultCode == RESULT_OK){

            //  更新列表


            //  之前传递过去的 list 位置
            int listPosition = data.getIntExtra("listPosition",-1);
            //  返回一个货道对象
            CommodityBean commodityBean = new Gson().fromJson(data.getStringExtra("commodityJsonString"),CommodityBean.class);


            Log.i(TAG, "onActivityResult: " + commodityBean.toString());

            if(listPosition != -1){
                replenishmentAdapter.setData(listPosition,commodityBean);
            }

        }else{
            Log.i(TAG, "onActivityResult: " + requestCode + "," + resultCode);
        }
    }

    public class ReplenishmentAdapter extends BaseQuickAdapter<CommodityBean,BaseViewHolder>{

        public ReplenishmentAdapter(int layoutResId, @Nullable List<CommodityBean> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, CommodityBean commodityBean) {
                //  helper.setText(R.id.replenishment_item_tv, commodityBean.getCupboardNumber() + "-" + commodityBean.getTierNumber() + "-" +commodityBean.getTierChildrenNumber());
            helper.setText(R.id.replenishment_item_tv, "A " + commodityBean.getTierChildrenNumber());

            //  查询库存数
            int number = DataBaseUtil.getInstance(ReplenishmentActivity.this).getDaoSession().getCommodityBeanDao().queryBuilder()
                    //  商品对象不为空
                    .where(CommodityBeanDao.Properties.CommodityAlternativeBean.isNotNull())
                    //  同柜子
                    .where(CommodityBeanDao.Properties.CupboardNumber.eq(commodityBean.getCupboardNumber()))
                    //  同层
                    .where(CommodityBeanDao.Properties.TierNumber.eq(commodityBean.getTierNumber()))
                    //  同货道
                    .where(CommodityBeanDao.Properties.TierChildrenNumber.eq(commodityBean.getTierChildrenNumber()))
                    .build().list().size();
            helper.setText(R.id.replenishment_item_number_tv,"库存数：" + number);

            if(number != 0 && commodityBean.getCommodityAlternativeBean() != null){
                //  设置商品名称
                Glide.with(mContext).load(commodityBean.getCommodityAlternativeBean().getImageUrl()).into((ImageView) helper.getView(R.id.replenishment_item_image));
                helper.setText(R.id.replenishment_item_name_tv,commodityBean.getCommodityAlternativeBean().getCommodityName());
            }else{
                Glide.with(mContext).load(R.mipmap.logo).into((ImageView) helper.getView(R.id.replenishment_item_image));
                helper.setText(R.id.replenishment_item_name_tv,"未上架商品");
            }


        }
    }

}