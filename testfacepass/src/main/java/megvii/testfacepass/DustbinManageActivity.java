package megvii.testfacepass;

import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import megvii.testfacepass.independent.bean.CommodityBean;
import megvii.testfacepass.independent.bean.CommodityBeanDao;
import megvii.testfacepass.independent.bean.DeliveryRecord;
import megvii.testfacepass.independent.util.DataBaseUtil;

/**
 *
 * 垃圾箱管理界面
 *
 * 投递记录
 * 错误记录
 *
 * 清空APP数据
 * 退出APP
 * 检查更新
 *
 * */
public class DustbinManageActivity extends AppCompatActivity {
    TabLayout tabLayout ;
    RecyclerView dustbin_manage_recyclerView;
    DustbinManageRecordAdapter dustbinManageRecordAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dustbin_manage);

        Toolbar mToolbarTb = (Toolbar) findViewById(R.id.tb_toolbar);
        setSupportActionBar(mToolbarTb);

        mToolbarTb.setNavigationOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                finish();
            }
        });


        tabLayout = (TabLayout) findViewById(R.id.dustbin_manage_tabLayout);
        dustbin_manage_recyclerView = (RecyclerView)findViewById(R.id.dustbin_manage_recyclerView);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getText().equals("错误记录")){

                }else if(tab.getText().equals("投递记录")){
                    List<DeliveryRecord> deliveryRecords = DataBaseUtil.getInstance(DustbinManageActivity.this).getDaoSession().getDeliveryRecordDao().queryBuilder().list();

                    dustbin_manage_recyclerView.setLayoutManager(new LinearLayoutManager(DustbinManageActivity.this));
                    dustbinManageRecordAdapter = new DustbinManageRecordAdapter(R.layout.item_record_layout,deliveryRecords);
                    dustbin_manage_recyclerView.setAdapter(dustbinManageRecordAdapter);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.addTab(tabLayout.newTab().setText("投递记录"));
        tabLayout.addTab(tabLayout.newTab().setText("错误记录"));

        List<DeliveryRecord> deliveryRecords = DataBaseUtil.getInstance(this).getDaoSession().getDeliveryRecordDao().queryBuilder().list();

        dustbin_manage_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dustbinManageRecordAdapter = new DustbinManageRecordAdapter(R.layout.item_record_layout,deliveryRecords);
        dustbin_manage_recyclerView.setAdapter(dustbinManageRecordAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dustbin_manage_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.dustbin_menu_check:
                break;
            case R.id.dustbin_menu_clear:
                DataBaseUtil.getInstance(this).getDaoSession().getDustbinConfigDao().deleteAll();

                break;
            case R.id.dustbin_menu_exit:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class DustbinManageRecordAdapter extends BaseQuickAdapter<DeliveryRecord, BaseViewHolder> {

        public DustbinManageRecordAdapter(int layoutResId, @Nullable List<DeliveryRecord> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, DeliveryRecord deliveryRecord) {
            helper.setText(R.id.user_id, String.valueOf(deliveryRecord.getUserId()));
            helper.setText(R.id.record_time, String.valueOf(deliveryRecord.getDeliveryTime()));
            helper.setText(R.id.record_door, String.valueOf(deliveryRecord.getDoorNumber()));
            helper.setText(R.id.record_dustbin_weight,  String.valueOf(deliveryRecord.getWeight()));
        }
    }

}