package megvii.testfacepass;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import megvii.testfacepass.independent.bean.WeightCalibrationCall;
import megvii.testfacepass.independent.manage.SerialPortRequestByteManage;
import megvii.testfacepass.independent.util.SerialPortUtil;


/**
 * 重量校准
 *
 * */
public class WeightCalibrationActivity extends AppCompatActivity {
    private EditText awc_weight_edit;
    private Button awc_weight_btn;

    //  校准次数
    private int calibrationNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_calibration);

        awc_weight_edit = (EditText) findViewById(R.id.awc_weight_edit);
        awc_weight_btn = (Button) findViewById(R.id.awc_weight_btn);


        EventBus.getDefault().register(this);

        //  校准按钮
        awc_weight_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(awc_weight_edit.getText())){
                    int weight = Integer.parseInt(awc_weight_edit.getText().toString());
                    if(calibrationNumber == 0){
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().weightCalibration_1(1,weight));
                    }else if(calibrationNumber == 1){
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().weightCalibration_2(1,weight));
                    }else{
                        Toast.makeText(WeightCalibrationActivity.this, "校准完毕", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(WeightCalibrationActivity.this, "请输入重量，单位 g", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 称重回调
     * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void weightCalibrationCall(WeightCalibrationCall weightCalibrationCall){
        Toast.makeText(WeightCalibrationActivity.this, weightCalibrationCall.toString(), Toast.LENGTH_SHORT).show();

        if(weightCalibrationCall.getCalibrationNumber() == 1){
            if(weightCalibrationCall.getResult() == 0x01){
                //  进入校准模式
            }else if(weightCalibrationCall.getResult() == 0x00){
                Toast.makeText(this, "第一次校准完成，请拿掉重物，开始第二次校准。", Toast.LENGTH_SHORT).show();

                awc_weight_btn.setText("开始重量校准 ( 第二次 )");

                calibrationNumber = 1;
            }else if(weightCalibrationCall.getResult() == (byte)(0xff)){
                Toast.makeText(this, "第一次校准失败！", Toast.LENGTH_SHORT).show();
            }
        }else if(weightCalibrationCall.getCalibrationNumber() == 2){
            if(weightCalibrationCall.getResult() == 0x01){
                //  进入校准模式
            }else if(weightCalibrationCall.getResult() == 0x00){
                Toast.makeText(this, "第二次校准成功，现在可以初始化其它设备参数了", Toast.LENGTH_SHORT).show();
                calibrationNumber = 2;
            }else if(weightCalibrationCall.getResult() == (byte)(0xff)){
                Toast.makeText(this, "第二次校准失败", Toast.LENGTH_SHORT).show();
            }
        }
    }


}