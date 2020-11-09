package megvii.testfacepass;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import megvii.testfacepass.independent.manage.SerialPortRequestByteManage;
import megvii.testfacepass.independent.util.SerialPortUtil;


/**
 *
 * 回收桶管理
 * */
public class RecyclerAdminActivity extends AppCompatActivity {
    private EditText ard_doorNumber_edit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler__admin);


        ard_doorNumber_edit = (EditText)findViewById(R.id.ard_doorNumber_edit);

    }


    /**
     * 开启回收桶
     * */
    public void openRecyclerDoor(View view){
        if(TextUtils.isEmpty(ard_doorNumber_edit.getText())){
            Toast.makeText(this, "请输入桶号", Toast.LENGTH_SHORT).show();
            return;
        }

        int doorNumber = Integer.parseInt(ard_doorNumber_edit.getText().toString());

        SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openElectromagnetism(doorNumber));
    }
}