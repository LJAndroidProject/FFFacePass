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

public class AdminLoginDialog extends AlertDialog {
    private Context context;

    private TextView admin_login_title;
    private EditText admin_login_edit;
    private EditText admin_login_password;
    private Button admin_login_button;
    private ImageView admin_login_close;

    private EditText admin_verify_code;
    private Button admin_verify_button;

    private LoginListener loginListener;

    private VerifyListener verifyListener;

    public AdminLoginDialog(Context context) {
        super(context);
        this.context = context;
    }


    @Override
    public void create() {
        super.create();

        //View view = View.inflate(context, R.layout.admin_login_dialog,null);

        setContentView(R.layout.admin_login_dialog);

        admin_login_title = (TextView)findViewById(R.id.admin_login_title);
        admin_login_edit = (EditText) findViewById(R.id.admin_login_edit);
        admin_login_password = (EditText) findViewById(R.id.admin_login_password);
        admin_login_button = (Button) findViewById(R.id.admin_login_button);
        admin_login_close = (ImageView)findViewById(R.id.admin_login_close);
        admin_verify_code = (EditText)findViewById(R.id.admin_verify_code);
        admin_verify_button = (Button)findViewById(R.id.admin_verify_button);

        if(loginListener != null){
            admin_login_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String editStr = admin_login_edit.getText().toString();
                    String password = admin_login_password.getText().toString();

                    if(TextUtils.isEmpty(editStr)){
                        Toast.makeText(context,"你还没有输入密码呢",Toast.LENGTH_LONG).show();
                    }else{
                        loginListener.callBack(editStr,password,AdminLoginDialog.this);
                    }
                }
            });
        }

        admin_login_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        //  否则会不出现软键盘
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        admin_login_edit.requestFocus();

        setCancelable(false);

    }


    public void verifyState(final VerifyListener verifyListener){
        admin_login_edit.setVisibility(View.GONE);
        admin_login_password.setVisibility(View.GONE);
        admin_login_button.setVisibility(View.GONE);

        admin_verify_code.setVisibility(View.VISIBLE);
        admin_verify_button.setVisibility(View.VISIBLE);
        this.verifyListener = verifyListener;

        admin_login_title.setText("请输入验证码");
        admin_verify_code.setHint("请输入手机收到的验证码");

        admin_verify_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String adminPhone = admin_login_edit.getText().toString();
                String adminVerifyCode = admin_verify_code.getText().toString();

                if(TextUtils.isEmpty(adminVerifyCode)){
                    Toast.makeText(context,"请输入验证码",Toast.LENGTH_LONG).show();
                }else{
                    verifyListener.verifyCallBack(adminPhone,adminVerifyCode,AdminLoginDialog.this);
                }

            }
        });
    }

    public void setLoginListener( LoginListener loginListener) {
        this.loginListener = loginListener;
    }

    public interface LoginListener{
        void callBack(String editStr,String password,AlertDialog alertDialog);
    }


    public interface VerifyListener{
        void verifyCallBack(String adminPhone,String verifyCode,AlertDialog alertDialog);
    }
}
