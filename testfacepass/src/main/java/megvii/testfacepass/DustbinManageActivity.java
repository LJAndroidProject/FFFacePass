package megvii.testfacepass;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import megvii.testfacepass.independent.bean.CommodityBean;
import megvii.testfacepass.independent.bean.CommodityBeanDao;
import megvii.testfacepass.independent.bean.DeliveryRecord;
import megvii.testfacepass.independent.bean.DeliveryRecordDao;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.bean.ErrorMessage;
import megvii.testfacepass.independent.bean.ErrorMessageDao;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.utils.AndroidDeviceSDK;

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
    View errorHeadView,recordHeadView;
    DustbinManageErrorRecordAdapter dustbinManageErrorRecordAdapter;
    Switch dustbin_switch_status,dustbin_switch_foreground;


    List<DeliveryRecord> deliveryRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dustbin_manage);



        tabLayout = (TabLayout) findViewById(R.id.dustbin_manage_tabLayout);
        dustbin_switch_status = (Switch)findViewById(R.id.dustbin_switch_status);
        dustbin_switch_foreground = (Switch)findViewById(R.id.dustbin_switch_foreground);
        dustbin_manage_recyclerView = (RecyclerView)findViewById(R.id.dustbin_manage_recyclerView);

        dustbin_switch_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //  隐藏状态栏，也就是 app 打开后不能退出
                AndroidDeviceSDK.hideStatus(DustbinManageActivity.this,isChecked);


            }
        });

        dustbin_switch_foreground.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //  检查是否在前台
                AndroidDeviceSDK.checkForeground(DustbinManageActivity.this,isChecked);
            }
        });


        errorHeadView = View.inflate(DustbinManageActivity.this,R.layout.error_record_layout,null);
        recordHeadView = View.inflate(DustbinManageActivity.this,R.layout.item_record_layout,null);


        List<ErrorMessage> errorMessageList = DataBaseUtil.getInstance(DustbinManageActivity.this).getDaoSession().getErrorMessageDao().queryBuilder().limit(10).orderDesc(ErrorMessageDao.Properties.ErrorId).list();
        dustbin_manage_recyclerView.setLayoutManager(new LinearLayoutManager(DustbinManageActivity.this));
        dustbinManageErrorRecordAdapter = new DustbinManageErrorRecordAdapter(R.layout.error_record_layout,errorMessageList);
        dustbinManageErrorRecordAdapter.addHeaderView(errorHeadView);



        deliveryRecords = DataBaseUtil.getInstance(DustbinManageActivity.this).getDaoSession().getDeliveryRecordDao().queryBuilder().limit(10).orderDesc(DeliveryRecordDao.Properties.Id).list();
        dustbin_manage_recyclerView.setLayoutManager(new LinearLayoutManager(DustbinManageActivity.this));
        dustbinManageRecordAdapter = new DustbinManageRecordAdapter(R.layout.item_record_layout,deliveryRecords);
        dustbinManageRecordAdapter.addHeaderView(recordHeadView);

        Toolbar mToolbarTb = (Toolbar) findViewById(R.id.dustbin_manage_toolbar);
        setSupportActionBar(mToolbarTb);

        mToolbarTb.setNavigationOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                finish();
            }
        });


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if(tab.getText() == null){
                    return;
                }


                if(tab.getText().equals("错误记录")){

                    dustbin_manage_recyclerView.setAdapter(dustbinManageErrorRecordAdapter);

                }else if(tab.getText().equals("投递记录")){


                    dustbinManageRecordAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                        @Override
                        public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                            if(view.getId() == R.id.item_record_layout){

                                if(deliveryRecords.get(position).getTakePath() != null){
                                    Bitmap bitmap = BitmapFactory.decodeFile(deliveryRecords.get(position).getTakePath());
                                    ImageView imageView = new ImageView(DustbinManageActivity.this);
                                    imageView.setImageBitmap(bitmap);

                                    AlertDialog.Builder alert = new AlertDialog.Builder(DustbinManageActivity.this);
                                    alert.setTitle("拍摄图片");
                                    alert.setView(imageView);
                                    alert.create();
                                    alert.show();
                                }else{
                                    Toast.makeText(DustbinManageActivity.this, "没有拍摄到图片", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });



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

        tabLayout.setTabGravity(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dustbin_manage_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            /*case R.id.dustbin_menu_afresh_calibration:
                startActivity(new Intent(DustbinManageActivity.this,WeightCalibrationActivity.class));
                break;*/
            case R.id.dustbin_menu_debug:
                startActivity(new Intent(DustbinManageActivity.this,DebugActivity.class));
                break;
            case R.id.dustbin_menu_check:
                break;
            case R.id.dustbin_menu_reBoot:
                AndroidDeviceSDK.reBoot(DustbinManageActivity.this);
                break;
            case R.id.dustbin_menu_clear:
                DataBaseUtil.getInstance(this).getDaoSession().getAllDaos().clear();
                break;
            case R.id.dustbin_menu_exit:
                //APP.exit();
                //killAppProcess();

                int a = 1 / 0;
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void killAppProcess() {
        //注意：不能先杀掉主进程，否则逻辑代码无法继续执行，需先杀掉相关进程最后杀掉主进程
        ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList)
        {
            if (runningAppProcessInfo.pid != android.os.Process.myPid())
            {
                android.os.Process.killProcess(runningAppProcessInfo.pid);
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public static String stampToDate(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time_Date = sdf.format(new Date(time));
        return time_Date;

    }

    public static class DustbinManageRecordAdapter extends BaseQuickAdapter<DeliveryRecord, BaseViewHolder> {

        public DustbinManageRecordAdapter(int layoutResId, @Nullable List<DeliveryRecord> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, DeliveryRecord deliveryRecord) {
            helper.setText(R.id.user_id, String.valueOf(deliveryRecord.getUserId()));
            helper.setText(R.id.record_time, stampToDate(deliveryRecord.getDeliveryTime()));
            helper.setText(R.id.record_door, String.valueOf(deliveryRecord.getDoorNumber()));
            helper.setText(R.id.record_dustbin_weight, String.valueOf(deliveryRecord.getWeight()));

            Glide.with(mContext).load(deliveryRecord.getTakePath()).into((ImageView) helper.getView(R.id.item_record_icon));

            helper.addOnClickListener(R.id.item_record_layout);
        }
    }



    public static class DustbinManageErrorRecordAdapter extends BaseQuickAdapter<ErrorMessage, BaseViewHolder> {

        public DustbinManageErrorRecordAdapter(int layoutResId, @Nullable List<ErrorMessage> errorMessages) {
            super(layoutResId, errorMessages);
        }

        @Override
        protected void convert(BaseViewHolder helper, ErrorMessage errorMessage) {
            helper.setText(R.id.error_door, String.valueOf(errorMessage.getErrorDoor()));
            helper.setText(R.id.error_msg, String.valueOf(errorMessage.getErrorDescribe()));
            helper.setText(R.id.error_time, stampToDate(errorMessage.getErrorTime()));

        }
    }

}