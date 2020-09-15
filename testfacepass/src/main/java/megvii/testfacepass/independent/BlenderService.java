package megvii.testfacepass.independent;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

import megvii.testfacepass.independent.manage.SerialPortRequestManage;
import megvii.testfacepass.independent.util.OrderUtil;
import megvii.testfacepass.independent.util.SerialPortUtil;


/**
 * 搅拌机服务 每小时转动 10 分钟，
 * */
public class BlenderService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                SerialPortUtil.getInstance().sendData(SerialPortRequestManage.getInstance().openBlender(1));

                //  10 分钟后关闭搅拌机
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SerialPortUtil.getInstance().sendData(SerialPortRequestManage.getInstance().closeBlender(1));
                    }
                },1000 * 60 * 10);
            }
        };

        Timer timer = new Timer();
        timer.schedule(timerTask,1000 * 60 * 60);



        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
