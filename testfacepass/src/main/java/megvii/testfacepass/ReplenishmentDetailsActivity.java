package megvii.testfacepass;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import megvii.testfacepass.independent.bean.CommodityAlternativeBean;
import megvii.testfacepass.independent.bean.CommodityAlternativeBeanDao;
import megvii.testfacepass.independent.bean.CommodityBean;
import megvii.testfacepass.independent.bean.CommodityBeanDao;
import megvii.testfacepass.independent.util.DataBaseUtil;

public class ReplenishmentDetailsActivity extends AppCompatActivity {

    CommodityBean commodityBean = new CommodityBean();

    private RecyclerView replenishment_details_recyclerView;

    private Intent intent;


    CommodityAlternativeBean commodityAlternativeBean;
    List<CommodityBean> list;

    CommodityAlternativeBean commodityAlternativeBeanTitle;


    ImageView replenishment_details_image;
    TextView replenishment_details_message;
    Button replenishment_details_add_btn;

    ReplenishmentDetailsAdapter replenishmentDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replenishment_details);


        Log.i("结果",Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

        replenishment_details_image = (ImageView) findViewById(R.id.replenishment_details_image);
        replenishment_details_message = (TextView) findViewById(R.id.replenishment_details_message);
        replenishment_details_add_btn = (Button) findViewById(R.id.replenishment_details_add_btn);


        intent = getIntent();

        commodityBean.setCupboardNumber(intent.getLongExtra("cupboardNumber",0));
        commodityBean.setTierNumber(intent.getLongExtra("tierNumber",0));
        commodityBean.setTierChildrenNumber(intent.getIntExtra("tierChildrenNumber",0));
        commodityBean.setCommodityID(intent.getLongExtra("commodityID",0));

        Log.i("结果","接受" + commodityBean.toString());


        if(commodityBean.getCommodityID() == 0){
            Glide.with(this).load(R.mipmap.chuyu).into(replenishment_details_image);
            replenishment_details_message.setText("点击指定此货道商品类型");


            list= null;
        }else{
            commodityAlternativeBeanTitle = DataBaseUtil.getInstance(this).getDaoSession().getCommodityAlternativeBeanDao().queryBuilder().where(CommodityAlternativeBeanDao.Properties.CommodityID.eq(commodityBean.getCommodityID())).build().unique();

            Glide.with(this).load(commodityAlternativeBeanTitle.getImageUrl()).into(replenishment_details_image);
            replenishment_details_message.setText(commodityAlternativeBeanTitle.getCommodityName());


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
        }


        replenishment_details_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final List<CommodityAlternativeBean> commodityAlternativeBeans = DataBaseUtil.getInstance(ReplenishmentDetailsActivity.this).getDaoSession().getCommodityAlternativeBeanDao().queryBuilder().build().list();


                final String []arr = new String[commodityAlternativeBeans.size()];
                for(int i =0 ; i<commodityAlternativeBeans.size();i++){
                    arr[i]=commodityAlternativeBeans.get(i).getCommodityName();
                }


                AlertDialog.Builder a = new AlertDialog.Builder(ReplenishmentDetailsActivity.this);
                a.setTitle("选择当页商品：");
                a.setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        commodityBean.setCommodityID(commodityAlternativeBeans.get(which).getCommodityID());
                        commodityBean.setCommodityAlternativeBean(commodityAlternativeBeans.get(which));


                        Glide.with(ReplenishmentDetailsActivity.this).load(commodityBean.getCommodityAlternativeBean().getImageUrl()).into(replenishment_details_image);
                        replenishment_details_message.setText(commodityBean.getCommodityAlternativeBean().getCommodityName());

                        //  给标题赋值
                        commodityAlternativeBeanTitle = DataBaseUtil.getInstance(ReplenishmentDetailsActivity.this).getDaoSession().getCommodityAlternativeBeanDao().queryBuilder().where(CommodityAlternativeBeanDao.Properties.CommodityID.eq(commodityAlternativeBeans.get(which).getCommodityID())).build().unique();

                        list = DataBaseUtil.getInstance(ReplenishmentDetailsActivity.this).getDaoSession().getCommodityBeanDao().queryBuilder()
                                //  商品对象不为空
                                //.where(CommodityBeanDao.Properties.CommodityAlternativeBean.isNotNull())
                                //  同柜子
                                .where(CommodityBeanDao.Properties.CupboardNumber.eq(commodityBean.getCupboardNumber()))
                                //  同层
                                .where(CommodityBeanDao.Properties.TierNumber.eq(commodityBean.getTierNumber()))
                                //  同货道
                                .where(CommodityBeanDao.Properties.TierChildrenNumber.eq(commodityBean.getTierChildrenNumber()))
                                .build().list();


                    }
                });
                a.create();
                a.show();

            }
        });


        Toast.makeText(this, commodityBean.toString(), Toast.LENGTH_SHORT).show();


        replenishment_details_recyclerView = (RecyclerView) findViewById(R.id.replenishment_details_recyclerView);
        replenishmentDetailsAdapter = new ReplenishmentDetailsAdapter(R.layout.item_replenishment_details_layout,list);

        replenishment_details_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        replenishment_details_recyclerView.setAdapter(replenishmentDetailsAdapter);


        replenishment_details_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("结果","点击");

                //  如果列表为空
                if(list == null ){
                    list = new ArrayList<>();
                }

                if(commodityBean.getCommodityAlternativeBean() == null){
                    Toast.makeText(ReplenishmentDetailsActivity.this, "请先选择商品类型", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i("结果","开始循环");



                for(int i =0 ; i<list.size();i++){

                    CommodityBean data = list.get(i);

                    if(data.getCommodityID() ==0){
                        //  设置添加时间
                        data.setAddTime(System.currentTimeMillis());
                        //  设置商品id
                        data.setCommodityID(commodityBean.getCommodityID());
                        //  设置商品备选
                        data.setCommodityAlternativeBean(commodityAlternativeBeanTitle);
                        //  设置生产日期
                        data.setDateInProduced(System.currentTimeMillis());
                        //  设置柜号
                        data.setCupboardNumber(commodityBean.getCupboardNumber());
                        //  设置层号
                        data.setTierNumber(commodityBean.getTierNumber());
                        //  设置层 - 货道 号
                        data.setTierChildrenNumber(commodityBean.getTierChildrenNumber());

                        data.setTierChildrenCommodityNumber(i + 1);


                        replenishmentDetailsAdapter.addData(data);

                        list.set(i,data);

                        return;
                    }


                }

                //DataBaseUtil.getInstance(ReplenishmentDetailsActivity.this).getDaoSession().getCommodityBeanDao().updateInTx(data);

            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();



        /*for(CommodityBean commodityBean : list){
            Log.i("保存","已有配置：" + commodityBean.toString());
        }*/


        DataBaseUtil.getInstance(ReplenishmentDetailsActivity.this).getDaoSession().getCommodityBeanDao().saveInTx(list);


    }

    public class ReplenishmentDetailsAdapter extends BaseQuickAdapter<CommodityBean, BaseViewHolder> {

        public ReplenishmentDetailsAdapter(int layoutResId, @Nullable List<CommodityBean> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, CommodityBean commodityBean) {
            if(commodityBean.getCommodityID() != 0){
                if(commodityBean.getCommodityAlternativeBean() == null){
                    helper.setAlpha(R.id.item_replenishment_details_layout,0.6f);
                    helper.setText(R.id.item_replenishment_details_layout_number, commodityBean.getCommodityAlternativeBean().getCommodityName());
                }else{
                    helper.setAlpha(R.id.item_replenishment_details_layout,1f);
                    helper.setText(R.id.item_replenishment_details_layout_number, commodityBean.getCommodityAlternativeBean().getCommodityName());
                }
            }else{
                helper.setAlpha(R.id.item_replenishment_details_layout,0.2f);
                helper.setText(R.id.item_replenishment_details_layout_number, "请先指定商品");
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