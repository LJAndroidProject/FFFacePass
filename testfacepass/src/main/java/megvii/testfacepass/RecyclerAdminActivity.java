package megvii.testfacepass;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import megvii.testfacepass.independent.bean.DeliveryRecord;
import megvii.testfacepass.independent.bean.DeliveryRecordDao;
import megvii.testfacepass.independent.bean.DustbinENUM;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.manage.SerialPortRequestByteManage;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.SerialPortUtil;


/**
 *
 * 回收桶管理
 * */
public class RecyclerAdminActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler__admin);


        Toolbar ara_toolbar = (Toolbar)findViewById(R.id.ara_toolbar);
        ara_toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                finish();
            }
        });


    }


    /**
     * 开启回收桶
     * */
    public void openRecyclerDoor(View view){
        if(APP.dustbinBeanList != null && APP.dustbinBeanList.size() > 0){
            for(DustbinStateBean dustbinStateBean:APP.dustbinBeanList){
                if(dustbinStateBean.getDustbinBoxType().equals(DustbinENUM.BOTTLE.toString()) || dustbinStateBean.getDustbinBoxType().equals(DustbinENUM.WASTE_PAPER.toString())){
                    //  开启电磁人工门
                    SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openElectromagnetism(dustbinStateBean.getDoorNumber()));
                }
            }

            //  删除所有投递记录
            DataBaseUtil.getInstance(RecyclerAdminActivity.this).getDaoSession().getDeliveryRecordDao().deleteAll();
        }else{
            Toast.makeText(this, "垃圾箱列表为null，或数量为 0 ", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 关闭所有回收桶
     * */
    public void closeRecyclerDoor(View view){
        if(APP.dustbinBeanList != null && APP.dustbinBeanList.size() > 0){
            for(DustbinStateBean dustbinStateBean:APP.dustbinBeanList){
                if(dustbinStateBean.getDustbinBoxType().equals(DustbinENUM.BOTTLE.toString()) || dustbinStateBean.getDustbinBoxType().equals(DustbinENUM.WASTE_PAPER.toString())){
                    //  开启电磁人工门
                    SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeElectromagnetism(dustbinStateBean.getDoorNumber()));
                }
            }
            //  删除所有投递记录
            DataBaseUtil.getInstance(RecyclerAdminActivity.this).getDaoSession().getDeliveryRecordDao().deleteAll();
        }else{
            Toast.makeText(this, "垃圾箱列表为null，或数量为 0 ", Toast.LENGTH_SHORT).show();
        }
    }
}