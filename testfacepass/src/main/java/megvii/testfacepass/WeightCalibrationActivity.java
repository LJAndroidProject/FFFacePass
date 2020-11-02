package megvii.testfacepass;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
    private EditText awc_weight_edit,awc_weight_doorNumber;
    private Button awc_weight_btn;

    //  校准次数
    private int calibrationNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_calibration);

        awc_weight_doorNumber = (EditText) findViewById(R.id.awc_weight_doorNumber);
        awc_weight_edit = (EditText) findViewById(R.id.awc_weight_edit);

        awc_weight_doorNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //  文本内容变化，说明切换了门

                calibrationNumber = 0;
                awc_weight_btn.setText("开始校准");
                awc_weight_edit.setText("0");

                Toast.makeText(WeightCalibrationActivity.this, "文本改变，切换校准桶", Toast.LENGTH_SHORT).show();
            }
        });

        awc_weight_btn = (Button) findViewById(R.id.awc_weight_btn);


        EventBus.getDefault().register(this);

        //  校准按钮
        awc_weight_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(awc_weight_edit.getText()) || !TextUtils.isEmpty(awc_weight_doorNumber.getText())){
                    int weight = Integer.parseInt(awc_weight_edit.getText().toString());
                    int doorNumber = Integer.parseInt(awc_weight_doorNumber.getText().toString());
                    if(calibrationNumber == 0){
                        //  进入校准
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().weightCalibration_1(doorNumber));
                    }else if(calibrationNumber == 1){
                        //  第一次校准
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().weightCalibration_2(doorNumber,weight));
                    }else if(calibrationNumber == 2){
                        //  第二次校准
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().weightCalibration_2(doorNumber,weight));
                    }else if(calibrationNumber == 3){
                        //  第三次校准
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().weightCalibration_2(doorNumber,weight));
                    }else if(calibrationNumber == 4){
                        //  第四次校准
                        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().weightCalibration_2(doorNumber,weight));
                    }else{
                        Toast.makeText(WeightCalibrationActivity.this, "校准完毕，可以切换校准门或者退出校准了", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(WeightCalibrationActivity.this, "请输入重量或者门板号", Toast.LENGTH_SHORT).show();
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

        //  1 是进入校准成功 、 2 是校准回馈
        if(weightCalibrationCall.getCalibrationNumber() == 1){
            if(weightCalibrationCall.getResult()[0] == 0x01){
                //  进入校准模式
                Toast.makeText(this, "进入校准模式成功，可以放置重物了", Toast.LENGTH_SHORT).show();

                calibrationNumber = 1;
            }else if(weightCalibrationCall.getResult()[0] == 0x00){
                Toast.makeText(this, "校准完成", Toast.LENGTH_SHORT).show();
                awc_weight_btn.setText("校准完成");

            }else if(weightCalibrationCall.getResult()[0] == (byte)(0xff)){
                Toast.makeText(this, "进入校准模式失败！", Toast.LENGTH_SHORT).show();
            }
        }else if(weightCalibrationCall.getCalibrationNumber() == 2){
                Toast.makeText(this, "校准反馈" + bytes2Int(weightCalibrationCall.getResult()), Toast.LENGTH_SHORT).show();
        }
    }


    public static int bytes2Int(byte[] bytes) {
        int result = 0;
        //将每个byte依次搬运到int相应的位置
        result = bytes[0] & 0xff;
        result = result << 8 | bytes[1] & 0xff;
        return result;
    }


}