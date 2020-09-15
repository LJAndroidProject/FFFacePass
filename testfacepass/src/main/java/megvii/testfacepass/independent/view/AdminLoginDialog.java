package megvii.testfacepass.independent.view;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import megvii.testfacepass.R;

public class AdminLoginDialog extends AlertDialog {
    private Context context;

    private EditText admin_login_edit;
    private Button admin_login_button;
    private ImageView admin_login_close;

    private LoginListener loginListener;

    public AdminLoginDialog(Context context) {
        super(context);
        this.context = context;
    }


    @Override
    public void create() {
        super.create();

        //View view = View.inflate(context, R.layout.admin_login_dialog,null);

        setContentView(R.layout.admin_login_dialog);

        admin_login_edit = (EditText) findViewById(R.id.admin_login_edit);
        admin_login_button = (Button) findViewById(R.id.admin_login_button);
        admin_login_close = (ImageView)findViewById(R.id.admin_login_close);

        if(loginListener != null){
            admin_login_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String editStr = admin_login_edit.getText().toString();

                    if(TextUtils.isEmpty(editStr)){
                        Toast.makeText(context,"你还没有输入密码呢",Toast.LENGTH_LONG).show();
                    }else{
                        loginListener.callBack(editStr,AdminLoginDialog.this);
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

    public void setLoginListener( LoginListener loginListener) {
        this.loginListener = loginListener;
    }

    public interface LoginListener{
        void callBack(String editStr,AlertDialog alertDialog);
    }
}
