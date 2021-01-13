package megvii.testfacepass.utils;

import android.app.AlertDialog;
import android.content.Context;

public class RootDialog extends AlertDialog {


    protected RootDialog(Context context) {
        super(context);
    }

    @Override
    public void create() {
        super.create();

        setTitle("离线超级管理员");
        setCancelable(false);

    }



}
