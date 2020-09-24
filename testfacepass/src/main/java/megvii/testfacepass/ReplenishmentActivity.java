package megvii.testfacepass;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import megvii.testfacepass.independent.bean.CommodityAlternativeBean;
import megvii.testfacepass.independent.bean.CommodityBean;
import megvii.testfacepass.independent.bean.CommodityBeanDao;
import megvii.testfacepass.independent.util.DataBaseUtil;

public class ReplenishmentActivity extends AppCompatActivity {

    RecyclerView replenishment_recyclerview;

    private String TAG = "结果";

    //  查询货道列表
    List<CommodityBean> result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replenishment);









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
        ReplenishmentAdapter replenishmentAdapter = new ReplenishmentAdapter(R.layout.item_replenishment,result);


        replenishmentAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(ReplenishmentActivity.this,ReplenishmentDetailsActivity.class);
                intent.putExtra("cupboardNumber",result.get(position).getCupboardNumber());
                intent.putExtra("tierNumber",result.get(position).getTierNumber());
                intent.putExtra("tierChildrenNumber",result.get(position).getTierChildrenNumber());

                Log.i("结果","传递" + result.get(position).toString());

                startActivity(intent);
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



    }


    /**
     *
     * 初始化 售货机货道 配置
     * */
    private void initReplenishment(){
        //  创建货道列表
        List<CommodityBean> commodityBeanList = new ArrayList<>();

        for(int h = 1 ; h <= 6 ; h++){
            for(int w = 1 ; w <= 10 ; w++){
                for(int z = 1 ; z <= 10 ; z++){
                    commodityBeanList.add(new CommodityBean(0,null,1,w,h,z,0,0));
                    Log.i("结果",commodityBeanList.get((commodityBeanList.size()-1)).toString());
                }
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

    public class ReplenishmentAdapter extends BaseQuickAdapter<CommodityBean,BaseViewHolder>{

        public ReplenishmentAdapter(int layoutResId, @Nullable List<CommodityBean> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, CommodityBean commodityBean) {
            helper.setText(R.id.replenishment_item_tv, commodityBean.getCupboardNumber() + "-" + commodityBean.getTierNumber() + "-" +commodityBean.getTierChildrenNumber());

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
                helper.setText(R.id.replenishment_item_name_tv,commodityBean.getCommodityAlternativeBean().getCommodityName());
            }else{
                helper.setText(R.id.replenishment_item_name_tv,"缺货状态");
            }


        }
    }

}