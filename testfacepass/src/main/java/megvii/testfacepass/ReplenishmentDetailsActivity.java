package megvii.testfacepass;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import megvii.testfacepass.independent.bean.CommodityAlternativeBean;
import megvii.testfacepass.independent.bean.CommodityAlternativeBeanDao;
import megvii.testfacepass.independent.bean.CommodityBean;
import megvii.testfacepass.independent.bean.CommodityBeanDao;
import megvii.testfacepass.independent.util.DataBaseUtil;

public class ReplenishmentDetailsActivity extends AppCompatActivity {

    //  当前货道 所处的商品类型，通过 上一个 activity intent 传递而来
    private CommodityBean commodityBean = new CommodityBean();

    private RecyclerView replenishment_details_recyclerView;

    private Intent intent;

    private CommodityAlternativeBean commodityAlternativeBean;
    private List<CommodityBean> list;
    private CommodityAlternativeBean commodityAlternativeBeanTitle;
    private ImageView replenishment_details_image;
    private TextView replenishment_details_message;
    private Button replenishment_details_add_btn;
    private ReplenishmentDetailsAdapter replenishmentDetailsAdapter;

    private final static String TAG = "补货调试";


    private int listPosition ;


    private Button replenishment_details_save_btn,replenishment_details_clear_btn;


    Calendar calendar ;
    int dateInProducedYear;
    int dateInProducedMonth;
    int dateInProducedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replenishment_details);

        calendar = Calendar.getInstance();


         dateInProducedYear = calendar.get(Calendar.YEAR);
         dateInProducedMonth = calendar.get(Calendar.MONTH);
         dateInProducedDay = calendar.get(Calendar.DAY_OF_MONTH);

        //  绑定组件
        replenishment_details_image = (ImageView) findViewById(R.id.replenishment_details_image);
        replenishment_details_message = (TextView) findViewById(R.id.replenishment_details_message);
        replenishment_details_add_btn = (Button) findViewById(R.id.replenishment_details_add_btn);
        replenishment_details_save_btn = (Button) findViewById(R.id.replenishment_details_save_btn);
        replenishment_details_clear_btn = (Button) findViewById(R.id.replenishment_details_clear_btn);


        //  获取意图对象
        intent = getIntent();

        //  生成当前货道对象
        commodityBean.setCupboardNumber(intent.getLongExtra("cupboardNumber",0));
        commodityBean.setTierNumber(intent.getLongExtra("tierNumber",0));
        commodityBean.setTierChildrenNumber(intent.getIntExtra("tierChildrenNumber",0));
        commodityBean.setCommodityID(intent.getLongExtra("commodityID",0));

        listPosition = intent.getIntExtra("listPosition",-1);

        //  赋值 货道对象 所承载的 商品
        commodityBean.setCommodityAlternativeBean(DataBaseUtil.getInstance(this).getDaoSession().getCommodityAlternativeBeanDao().queryBuilder().where(CommodityAlternativeBeanDao.Properties.CommodityID.eq(commodityBean.getCommodityID())).build().unique());


        Log.i(TAG,"当前货道商品：" + commodityBean.toString());


        //  如果为 0 说明该货道还未指定商品
        if(commodityBean.getCommodityID() == 0){

            //  图片和文字填充
            Glide.with(this).load(R.mipmap.logo).into(replenishment_details_image);
            replenishment_details_message.setText("点击指定此货道商品类型");


            //  该货道列表将为 null
            list = null;
        }else{
            commodityBean.setCommodityAlternativeBean(DataBaseUtil.getInstance(this).getDaoSession().getCommodityAlternativeBeanDao().queryBuilder().where(CommodityAlternativeBeanDao.Properties.CommodityID.eq(commodityBean.getCommodityID())).build().unique());


            //  图片和文字填充
            Glide.with(this).load(commodityBean.getCommodityAlternativeBean().getImageUrl()).into(replenishment_details_image);


            replenishment_details_message.setText(
                    "商品名称：" +
                    commodityBean.getCommodityAlternativeBean().getCommodityName()
                    + "\n保质期：" +
                    commodityBean.getCommodityAlternativeBean().getExpirationDate()
                    + "\n商品价格：" +
                    commodityBean.getCommodityAlternativeBean().getCommodityMoney()
                    + "\n积分支付：" +
                    commodityBean.getCommodityAlternativeBean().getCanUserIntegral()
            );


            //  查询该 货道下面所有商品
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


        replenishment_details_clear_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(list != null){

                    for(CommodityBean commodityBean : list){
                        commodityBean.setCommodityID(0);
                        commodityBean.setCommodityAlternativeBean(null);
                    }


                    //  清空库存
                    DataBaseUtil.getInstance(ReplenishmentDetailsActivity.this).getDaoSession().getCommodityBeanDao().saveInTx(list);

                    //  更新列表
                    replenishmentDetailsAdapter.setNewData(list);

                }
            }
        });

        //  设置监听事件切换当前货道商品
        replenishment_details_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //  获取商品备选列表
                final List<CommodityAlternativeBean> commodityAlternativeBeans = DataBaseUtil.getInstance(ReplenishmentDetailsActivity.this).getDaoSession().getCommodityAlternativeBeanDao().queryBuilder().build().list();
                final String []arr = new String[commodityAlternativeBeans.size()];
                for(int i =0 ; i<commodityAlternativeBeans.size();i++){
                    arr[i]=commodityAlternativeBeans.get(i).getCommodityName();
                }


                //  显示选择备选商品弹窗
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
                        commodityBean.setCommodityAlternativeBean(DataBaseUtil.getInstance(ReplenishmentDetailsActivity.this).getDaoSession().getCommodityAlternativeBeanDao().queryBuilder().where(CommodityAlternativeBeanDao.Properties.CommodityID.eq(commodityAlternativeBeans.get(which).getCommodityID())).build().unique());


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


                        replenishmentDetailsAdapter.setNewData(list);

                    }
                });
                a.create();
                a.show();

            }
        });



        replenishment_details_recyclerView = (RecyclerView) findViewById(R.id.replenishment_details_recyclerView);
        replenishmentDetailsAdapter = new ReplenishmentDetailsAdapter(R.layout.item_replenishment_details_layout,list);
        replenishment_details_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        replenishment_details_recyclerView.setAdapter(replenishmentDetailsAdapter);


        replenishment_details_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(commodityBean != null && commodityBean.getCommodityAlternativeBean() != null){


                    DataBaseUtil.getInstance(ReplenishmentDetailsActivity.this).getDaoSession().getCommodityBeanDao().saveInTx(list);

                    Intent intent = new Intent(ReplenishmentDetailsActivity.this,ReplenishmentActivity.class);
                    intent.putExtra("listPosition",listPosition);
                    intent.putExtra("commodityJsonString",new Gson().toJson(list.get(0)));
                    setResult(RESULT_OK,intent);
                    finish();


                }else{
                    finish();
                }
            }
        });


        replenishmentDetailsAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(final BaseQuickAdapter adapter, View view, final int position) {
                if(view.getId() == R.id.item_replenishment_details_dateInProduced){


                    int []data = getBirthTime(list.get(position).getDateInProduced());

                    DatePickerDialog datePickerDialog = new DatePickerDialog(ReplenishmentDetailsActivity.this, new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                            dateInProducedYear = year;
                            dateInProducedMonth = monthOfYear;
                            dateInProducedDay = dayOfMonth;



                            Long time = convertTimeToLong(year + "-" + monthOfYear + "-" + dayOfMonth);

                            list.get(position).setDateInProduced(time);


                            replenishmentDetailsAdapter.setData(position,list.get(position));

                            //  Toast.makeText(ReplenishmentDetailsActivity.this, year + "-" + monthOfYear + "-" + dayOfMonth, Toast.LENGTH_SHORT).show();
                        }
                    }, dateInProducedYear, dateInProducedMonth, dateInProducedDay);
                    datePickerDialog.show();

                }
            }
        });

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

                    if(data.getCommodityID() == 0){
                        //  设置添加时间
                        data.setAddTime(System.currentTimeMillis());
                        //  设置商品id
                        data.setCommodityID(commodityBean.getCommodityID());
                        //  设置商品备选
                        data.setCommodityAlternativeBean(commodityBean.getCommodityAlternativeBean());
                        //  设置生产日期
                        data.setDateInProduced(convertTimeToLong(dateInProducedYear + "-" + dateInProducedMonth + "-" + dateInProducedDay));
                        //  设置柜号
                        data.setCupboardNumber(commodityBean.getCupboardNumber());
                        //  设置层号
                        data.setTierNumber(commodityBean.getTierNumber());
                        //  设置层 - 货道 号
                        data.setTierChildrenNumber(commodityBean.getTierChildrenNumber());

                        data.setTierChildrenCommodityNumber(i + 1);


                        replenishmentDetailsAdapter.setData(i,data);

                        list.set(i,data);

                        return;
                    }


                }

            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();



        /*for(CommodityBean commodityBean : list){
            Log.i("保存","已有配置：" + commodityBean.toString());
        }*/



    }

    public class ReplenishmentDetailsAdapter extends BaseQuickAdapter<CommodityBean, BaseViewHolder> {

        public ReplenishmentDetailsAdapter(int layoutResId, @Nullable List<CommodityBean> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, CommodityBean commodityBean) {
            if(commodityBean.getCommodityID() != 0){
                helper.setAlpha(R.id.item_replenishment_details_layout,1f);
                helper.setText(R.id.item_replenishment_details_layout_number, String.valueOf(commodityBean.getTierChildrenCommodityNumber()));
                helper.setText(R.id.item_replenishment_details_layout_name, commodityBean.getCommodityAlternativeBean().getCommodityName());
                helper.setText(R.id.item_replenishment_details_dateInProduced, "生产日期：" + stampToDate(commodityBean.getDateInProduced()));

                //  过期时间
                long outOfDay = commodityBean.getDateInProduced() + (commodityBean.getCommodityAlternativeBean().getExpirationDate() * 86400000);

                helper.setText(R.id.item_replenishment_details_layout_expirationTime,"过期时间："+stampToDate(outOfDay));

                //  过期了
                if(outOfDay < System.currentTimeMillis()){
                    helper.setTextColor(R.id.item_replenishment_details_layout_expirationTime, Color.RED);
                }


            }else{
                helper.setAlpha(R.id.item_replenishment_details_layout,0.5f);
                helper.setText(R.id.item_replenishment_details_layout_number, String.valueOf(commodityBean.getTierChildrenCommodityNumber()));
                helper.setText(R.id.item_replenishment_details_layout_name, "请先指定商品");
                helper.setText(R.id.item_replenishment_details_dateInProduced, "生产日期");
            }


            //  添加点击事件
            helper.addOnClickListener(R.id.item_replenishment_details_dateInProduced);

        }
    }


    public static String stampToDate(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String time_Date = sdf.format(new Date(time));
        return time_Date;

    }


    /**
     * 传入时间戳获取年月日
     * */
    private int[] getBirthTime(Long timeStamp) {

        int [] result = new int[3];

        Date date = new Date(timeStamp);

        calendar.setTime(date);

        result[0] = calendar.get(Calendar.YEAR);
        result[1] = calendar.get(Calendar.MONTH) + 1;
        result[2] = calendar.get(Calendar.DAY_OF_MONTH);

        return result;
    }


    public static Long convertTimeToLong(String time) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            date = sdf.parse(time);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

}