package megvii.testfacepass;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.serialportlibrary.service.impl.SerialPortService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import megvii.testfacepass.independent.ServerAddress;
import megvii.testfacepass.independent.bean.CommodityAlternativeBean;
import megvii.testfacepass.independent.bean.CommodityBean;
import megvii.testfacepass.independent.bean.CommodityBeanDao;
import megvii.testfacepass.independent.bean.DeliveryResult;
import megvii.testfacepass.independent.bean.DustbinBean;
import megvii.testfacepass.independent.bean.DustbinBeanDao;
import megvii.testfacepass.independent.bean.DustbinENUM;
import megvii.testfacepass.independent.manage.SerialPortRequestManage;
import megvii.testfacepass.independent.manage.SerialPortResponseManage;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.NetWorkUtil;
import megvii.testfacepass.independent.util.SerialPortUtil;
import okhttp3.Call;

public class ControlActivity extends AppCompatActivity implements View.OnClickListener,View.OnTouchListener{
    private Button btn_kitchen_garbage,btn_other_rubbish,btn_harmful_waste,btn_leatheroid,btn_vending_machine,btn_bottle;
    private Intent intent;

    //  这个id 是服务器传过来的用户id，绑定接下来的所有操作
    public static long userId;

    //  关门失败次数，5次失败上报错误到服务器
    private int closeDoorFailNumber = 0;

    private TextView control_welcome_textView,replenishment_tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);



        control_welcome_textView = (TextView) findViewById(R.id.control_welcome_textView);
        replenishment_tv = (TextView)findViewById(R.id.replenishment_tv);
        replenishment_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ControlActivity.this,ReplenishmentActivity.class);
                startActivity(intent);
            }
        });

         /*int i = 0x00 + 0x01 + 0x01 + 0x01 + 0x01 + 0x11;
        Log.i("结果",  String.valueOf(i));*/

        intent = getIntent();
        userId = intent.getLongExtra("userId",1);

        if(userId == 0){
            Toast.makeText(ControlActivity.this,"特殊用户",Toast.LENGTH_LONG).show();
            finish();
        }else{
            control_welcome_textView.setText("欢迎用户 " + userId + " 进入操作界面");
        }

        initView();

        EventBus.getDefault().register(this);

        //  如果没有垃圾箱配置信息，则应该弹出弹窗 并将情况上报
        if(!DataBaseUtil.getInstance(ControlActivity.this).hasDustBinConfig()){
            alertDialog = alertNoDustBinConfig();
            alertDialog.show();
        }



        //  进入范围，打开其它和厨余垃圾
        List<DustbinBean> list = DataBaseUtil.getInstance(this).getDustbinByType(DustbinENUM.OTHER);
        list.addAll(DataBaseUtil.getInstance(this).getDustbinByType(DustbinENUM.KITCHEN));
        //openDoor(list);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initView(){
        btn_kitchen_garbage = (Button)findViewById(R.id.btn_kitchen_garbage);
        btn_other_rubbish = (Button)findViewById(R.id.btn_other_rubbish);
        btn_harmful_waste = (Button)findViewById(R.id.btn_harmful_waste);
        btn_leatheroid = (Button)findViewById(R.id.btn_leatheroid);
        btn_vending_machine = (Button)findViewById(R.id.btn_vending_machine);   //  自动售货机
        btn_bottle = (Button)findViewById(R.id.btn_bottle);

        btn_kitchen_garbage.setOnClickListener(this);
        btn_other_rubbish.setOnClickListener(this);
        btn_harmful_waste.setOnClickListener(this);
        btn_leatheroid.setOnClickListener(this);
        btn_vending_machine.setOnClickListener(this);
        btn_bottle.setOnClickListener(this);

        btn_kitchen_garbage.setOnTouchListener(this);
        btn_other_rubbish.setOnTouchListener(this);
        btn_harmful_waste.setOnTouchListener(this);
        btn_leatheroid.setOnTouchListener(this);
        btn_bottle.setOnTouchListener(this);
    }


    /**
     *
     * 显示无垃圾箱配置的警告弹窗
     * */
    private AlertDialog alertDialog;
    private AlertDialog alertNoDustBinConfig(){
        AlertDialog.Builder alert = new AlertDialog.Builder(ControlActivity.this);
        alert.setCancelable(false);
        alert.setTitle("警告");
        alert.setMessage("获取垃圾箱配置失败，无法进行操作。");
        alert.setPositiveButton("退出界面", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.setNegativeButton("创建配置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<DustbinBean> list = new ArrayList<>();
                list.add(new DustbinBean(1,DustbinENUM.KITCHEN.toString(),false,0));
                list.add(new DustbinBean(2,DustbinENUM.KITCHEN.toString(),false,0));
                list.add(new DustbinBean(3,DustbinENUM.HARMFUL.toString(),false,0));
                list.add(new DustbinBean(4,DustbinENUM.OTHER.toString(),false,0));
                list.add(new DustbinBean(5,DustbinENUM.OTHER.toString(),false,0));
                list.add(new DustbinBean(6,DustbinENUM.OTHER.toString(),false,0));
                list.add(new DustbinBean(7,DustbinENUM.WASTE_PAPER.toString(),false,0));
                list.add(new DustbinBean(8,DustbinENUM.BOTTLE.toString(),false,0));

                Log.i("结果","添加配置" + DustbinENUM.KITCHEN.toString());

                DataBaseUtil.getInstance(ControlActivity.this).setDustBinConfig(list);

                finish();
            }
        });
        return alert.create();
    }

    @Override
    public void onClick(View view) {
        List<DustbinBean> list = null ;
        switch (view.getId()){
            case R.id.btn_kitchen_garbage:
                list = DataBaseUtil.getInstance(ControlActivity.this).getDustbinByType(DustbinENUM.KITCHEN);
                break;
            case R.id.btn_leatheroid:
                list = DataBaseUtil.getInstance(ControlActivity.this).getDustbinByType(DustbinENUM.WASTE_PAPER);
                break;
            case R.id.btn_bottle:
                list = DataBaseUtil.getInstance(ControlActivity.this).getDustbinByType(DustbinENUM.BOTTLE);
                break;
            case R.id.btn_other_rubbish:
                list = DataBaseUtil.getInstance(ControlActivity.this).getDustbinByType(DustbinENUM.OTHER);
                break;
            case R.id.btn_harmful_waste:
                list = DataBaseUtil.getInstance(ControlActivity.this).getDustbinByType(DustbinENUM.HARMFUL);
                break;
        }


        if(list != null && list.size() > 0){

            Log.i("结果",list.toString());

            DustbinBean result = openDoor(list);

            if(result == null){
                Toast.makeText(ControlActivity.this,"没有合适的垃圾箱",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(ControlActivity.this,"已为你开启" +  result.getDoorNumber() + "号垃圾箱",Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(ControlActivity.this,"此垃圾箱不支持当前类型",Toast.LENGTH_LONG).show();
        }


    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        //  点击效果，调整透明度
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                v.setAlpha(0.8f);
                break;
            case MotionEvent.ACTION_UP:
                v.setAlpha(1f);
                break;
        }

        return false;
    }

    /**
     * 将遍历各个门，直到匹配,可用为止
     * @param dustbinTypeChildList 传入需要开启的门编号
     * @return 返回打开成功的门
     * */
    private DustbinBean openDoor(List<DustbinBean> dustbinTypeChildList){

        if(dustbinTypeChildList == null){
            return null;
        }

        /*for(DustbinBean dustbinBean : dustbinTypeChildList){
            byte[] responseByte = SerialPortUtil.getInstance().sendData(SerialPortRequestManage.getInstance().openDoor(dustbinBean.getDoorNumber()));
            if(responseByte != null && responseByte[7] == 0x10){
                *//*String responseString = ByteStringUtil.byteArrayToHexStr(responseByte);

                String door = OrderUtil.cutOrderByIndex(responseString,5);

                return Integer.parseInt(door);*//*

                //  打开门板成功后关闭消毒灯，关闭门板后再打开消毒灯
                SerialPortUtil.getInstance().sendData(SerialPortRequestManage.getInstance().openTheDisinfection(1));

                return dustbinBean;
            }
        }*/

        SerialPortUtil.getInstance().sendData(SerialPortRequestManage.getInstance().openDoor(2));


        return null;
    }


    /**
     * 上传用户投递的 id 和 重量差 ，以及箱体类型
     * 投递 --> 关门 --> 计算当前重量 --> 计算重量差 (同一箱体的当前重量 减去 上一次重量) --> 投递结果
     *
     * */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void addUserScore(DeliveryResult deliveryResult){

        //  传入门板号，获取垃圾箱类型
        DustbinBean dustbinBean = DataBaseUtil.getInstance(ControlActivity.this).getDustbinByNumber(deliveryResult.getDoorNumber());
        String dustbinBoxType = dustbinBean.getDustbinBoxType();

        Map<String,String> map = new HashMap<>();
        map.put("userId",String.valueOf(userId));   //  用户  ID
        map.put("weightDiff",String.valueOf(deliveryResult.getWeightDiff()));   //  垃圾箱重量差
        map.put("dustbinBoxType",dustbinBoxType);   //  垃圾箱类型

        NetWorkUtil.getInstance().doPost(ServerAddress.ADD_USER_SCORE, map, new NetWorkUtil.NetWorkListener() {
            @Override
            public void success(String response) {

            }

            @Override
            public void fail(Call call, IOException e) {

            }

            @Override
            public void error(Exception e) {

            }
        });


    }


}

