package megvii.testfacepass;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import megvii.testfacepass.independent.bean.CommodityAlternativeBean;
import megvii.testfacepass.independent.bean.CommodityBean;
import megvii.testfacepass.independent.bean.CommodityBeanDao;
import megvii.testfacepass.independent.util.DataBaseUtil;

public class ReplenishmentDetailsActivity extends AppCompatActivity {

    CommodityBean commodityBean = new CommodityBean();

    private RecyclerView replenishment_details_recyclerView;

    private Intent intent;


    CommodityAlternativeBean commodityAlternativeBean;
    List<CommodityBean> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replenishment_details);

        intent = getIntent();

        commodityBean.setCupboardNumber(intent.getLongExtra("cupboardNumber",0));
        commodityBean.setTierNumber(intent.getLongExtra("tierNumber",0));
        commodityBean.setTierChildrenNumber(intent.getIntExtra("tierChildrenNumber",0));


        Log.i("结果","接受" + commodityBean.toString());



        list = DataBaseUtil.getInstance(this).getDaoSession().getCommodityBeanDao().queryBuilder()
                //  商品对象不为空
                //.where(CommodityBeanDao.Properties.CommodityAlternativeBean.isNotNull())
                //  同柜子
                .where(CommodityBeanDao.Properties.CupboardNumber.eq(commodityBean.getCupboardNumber()))
                //  同层
                .where(CommodityBeanDao.Properties.TierNumber.eq(commodityBean.getTierNumber()))
                //  同货道
                .where(CommodityBeanDao.Properties.TierChildrenNumber.eq(commodityBean.getTierChildrenNumber()))
                .build().list();


        Toast.makeText(this, commodityBean.toString(), Toast.LENGTH_SHORT).show();


        replenishment_details_recyclerView = (RecyclerView) findViewById(R.id.replenishment_details_recyclerView);
        ReplenishmentDetailsAdapter replenishmentDetailsAdapter = new ReplenishmentDetailsAdapter(R.layout.item_replenishment_details_layout,list);

        replenishment_details_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        replenishment_details_recyclerView.setAdapter(replenishmentDetailsAdapter);

    }



    public class ReplenishmentDetailsAdapter extends BaseQuickAdapter<CommodityBean, BaseViewHolder> {

        public ReplenishmentDetailsAdapter(int layoutResId, @Nullable List<CommodityBean> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, CommodityBean commodityBean) {
            if(commodityBean.getCommodityAlternativeBean() == null){
                helper.setText(R.id.item_replenishment_details_layout_number, "无货");
            }else{
                helper.setText(R.id.item_replenishment_details_layout_number, commodityBean.getCommodityAlternativeBean().getCommodityName());
            }

        }
    }


    public void add(View view){

        if(list == null){
            list = new ArrayList<>();
        }


        for(int i = 0; i<list.size() ; i++){
            if(commodityBean.getCommodityAlternativeBean() == null){


                if(commodityAlternativeBean == null){
                    //  提示显示选择商品
                }else{
                    list.get(i).setCommodityAlternativeBean(commodityAlternativeBean);
                }


                return;
            }
        }
    }


}