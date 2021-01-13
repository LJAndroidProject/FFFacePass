package megvii.testfacepass.utils;

import android.content.Context;
import android.content.Intent;

/**
 * 安卓设备SDK ，方便切换厂商，使用一些常用接口，例如 隐藏状态栏，检测是否在前台
 * */
public class AndroidDeviceSDK {
    //  检测是否在前台
    public static void checkForeground(Context context,boolean checkForeground){
        //  监听 app 是否在前台
        Intent intent2 = new Intent("android.q_zheng.action.APPMONITOR");
        intent2.putExtra("package_name","megvii.testfacepass"); //设置所监控应用的包名为 com.xxx.yyy
        intent2.putExtra("self_starting", true); //设置开机自启动
        intent2.putExtra("period", checkForeground ? 15 : 0); //设置监控应有的周期，秒为单位，最小值为 15 秒，如果不设置
        //或者为 0，表示不需要系统对应用是否在前台进行监控
        context.sendBroadcast(intent2);

    }

    //  隐藏状态栏
    public static void hideStatus(Context context,boolean hideStatus){
        //  隐藏状态栏，也就是 app 打开后不能退出
        Intent intent = new Intent("android.q_zheng.action.statusbar");
        intent.putExtra("forbidden",hideStatus);
        intent.putExtra("status_bar",hideStatus);
        intent.putExtra("navigation_bar",hideStatus);
        context.sendBroadcast(intent);
    }

    /**
     * 重启
     * */
    public static void reBoot(Context context){
        Intent intent = new Intent("android.q_zheng.action.REBOOT");
        context.sendBroadcast(intent);
    }


    /**
     * 自动重启
     * */
    public static void autoReBoot(Context context,boolean enable){
        Intent intent = new Intent("android.q_zheng.action.POWERONOFF");
        int[] poweroff = {0,1}; //    即在每天 0:1 关机,小时取值 0-23,分钟取值 0-59
        int[] poweron = {0,3}; //  即在每天 0:3 开机,小时取值 0-23,分钟取值 0-59
        intent.putExtra("timeon", poweron);
        intent.putExtra("timeoff", poweroff);
        intent.putExtra("type", 2); //类型 2 代表设置每天开关机时间
        intent.putExtra("enable",enable); //使能开关机功能，设为 false,则为关闭,缺省为 true
        context.sendBroadcast(intent);
    }
}
