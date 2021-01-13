package megvii.testfacepass.independent.view;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import megvii.testfacepass.R;

public class PhoneLoginDialog extends AlertDialog {
    private Context context;

    private TextView admin_login_title;
    private EditText phone_login_phone;
    private Button phone_login_button;
    private ImageView phone_login_close;
    private LoginListener loginListener;



    public PhoneLoginDialog(Context context) {
        super(context);
        this.context = context;
    }


    @Override
    public void create() {
        super.create();


        setContentView(R.layout.phone_login_dialog);

        admin_login_title = (TextView)findViewById(R.id.phone_login_title);
        phone_login_phone = (EditText) findViewById(R.id.phone_login_phone);
        phone_login_button = (Button) findViewById(R.id.phone_login_button);
        phone_login_close = (ImageView)findViewById(R.id.phone_login_close);


        if(loginListener != null){
            phone_login_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String editStr = phone_login_phone.getText().toString();

                    if(TextUtils.isEmpty(editStr)){
                        Toast.makeText(context,"你还没有输入手机号码呢",Toast.LENGTH_LONG).show();
                    }else{
                        phone_login_phone.clearFocus();
                        loginListener.callBack(editStr, PhoneLoginDialog.this);
                    }
                }
            });
        }

        phone_login_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        //  否则会不出现软键盘
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        phone_login_phone.requestFocus();

        setCancelable(false);

    }


    public void setLoginListener( LoginListener loginListener) {
        this.loginListener = loginListener;
    }

    public interface LoginListener{
        void callBack(String editStr, AlertDialog alertDialog);
    }

}
