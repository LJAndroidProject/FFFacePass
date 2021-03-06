package megvii.testfacepass.utils;

import android.content.Context;
import android.content.Intent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * 安卓设备SDK ，方便切换厂商，使用一些常用接口，例如 隐藏状态栏，检测是否在前台
 */
public class AndroidDeviceSDK {
    private final static String TAG = "AndroidDeviceSDK";

    //  检测是否在前台
    public static void checkForeground(Context context, boolean checkForeground) {
        //  监听 app 是否在前台
        Intent intent2 = new Intent("android.q_zheng.action.APPMONITOR");
        intent2.putExtra("package_name", "megvii.testfacepass"); //设置所监控应用的包名为 com.xxx.yyy
        intent2.putExtra("self_starting", true); //设置开机自启动
        intent2.putExtra("period", checkForeground ? 15 : 0); //设置监控应有的周期，秒为单位，最小值为 15 秒，如果不设置
        //或者为 0，表示不需要系统对应用是否在前台进行监控
        context.sendBroadcast(intent2);

    }

    //  隐藏状态栏
    public static void hideStatus(Context context, boolean hideStatus) {
//        if(true){
//            hideStatus = false;
//        }
        //  隐藏状态栏，也就是 app 打开后不能退出
        Intent intent = new Intent("android.q_zheng.action.statusbar");
        intent.putExtra("forbidden", hideStatus);
        intent.putExtra("status_bar", hideStatus);
        intent.putExtra("navigation_bar", hideStatus);
        context.sendBroadcast(intent);
    }

    /**
     * 重启
     */
    public static void reBoot(Context context) {
        Intent intent = new Intent("android.q_zheng.action.REBOOT");
        context.sendBroadcast(intent);
    }


    /**
     * 自动重启
     */
    public static void autoReBoot(Context context, boolean enable) {
        Intent intent = new Intent("android.q_zheng.action.POWERONOFF");
        /*int[] poweroff = {0,1}; //    即在每天 0:1 关机,小时取值 0-23,分钟取值 0-59
        int[] poweron = {0,3}; //  即在每天 0:3 开机,小时取值 0-23,分钟取值 0-59*/
        int[] poweroff = {9, 30}; //    即在每天 9:30 关机,小时取值 0-23,分钟取值 0-59
        int[] poweron = {17, 30}; //  即在每天 17:30 开机,小时取值 0-23,分钟取值 0-59
        intent.putExtra("timeon", poweron);
        intent.putExtra("timeoff", poweroff);
        intent.putExtra("type", 2); //类型 2 代表设置每天开关机时间
        intent.putExtra("enable", enable); //使能开关机功能，设为 false,则为关闭,缺省为 true
        context.sendBroadcast(intent);
    }

    /**
     * 超过投放时间，关闭设备
     * @param context
     */
    public static void shutDownBoot(Context context){
        Intent intent = new Intent("android.q_zheng.action.SHUTDOWN");
        intent.putExtra("confirm", false);
        intent.putExtra("reason：", "超过投放时间"); //类型 2 代表设置每天开关机时间
        intent.putExtra("wait", false); //使能开关机功能，设为 false,则为关闭,缺省为 true
       context.sendBroadcast(intent);
    }

    /**
     * 早上6.30自动重启
     */
    public static void autoReBootForAM(Context context, boolean enable) {
        Intent intent = new Intent("android.q_zheng.action.POWERONOFF");
        int[] poweron = {6, 45};
//        int[] poweron = {16, 8};
        intent.putExtra("timeon", poweron);
        intent.putExtra("type", 2); //类型 2 代表设置每天开关机时间
        intent.putExtra("enable", enable); //使能开关机功能，设为 false,则为关闭,缺省为 true
        context.sendBroadcast(intent);
    }

    /**
     * 根据传入年月日控制开关机
     * 指定时间开关机
     */
    public static void nextAutoReBoot(Context context,int year, int month,int day){
        int onYear = year;
        int onMonth = month;
//        int onDay = day+1;
        int onDay = day;
        //每年最后一天第二天时间是 1月1
        if(month == 12 && day==31){
            onYear++;
            onMonth = 1;
            onDay = 1;
        }
        Intent intent = new Intent("android.q_zheng.action.POWERONOFF");
        int[] poweroff = {year,month,day, 11,50};//小时取值 0-23,分钟取值 0-59
        int[] poweron = {onYear,onMonth,onDay,  11,55}; //小时取值 0-23,分钟取值 0-59
        LogUtil.writeBusinessLog("当前设置开机时间："+ Arrays.toString(poweron)+",  当前设置关机时间"+Arrays.toString(poweroff));
        LogUtil.d("当前设置开机时间："+ Arrays.toString(poweron)+",  当前设置关机时间"+Arrays.toString(poweroff));
        intent.putExtra("timeon", poweron);
        intent.putExtra("timeoff", poweroff);
        intent.putExtra("type", 1); //类型 1 代表设置一次
        intent.putExtra("enable",true); //使能开关机功能为 true，false 为关闭,缺省为 true
        context.sendBroadcast(intent);
    }

    /**
     * 早上6.30自动重启
     */
    public static void auto643ReBootForAM(Context context, boolean enable) {
        Intent intent = new Intent("android.q_zheng.action.POWERONOFF");
        /*int[] poweroff = {0,1}; //    即在每天 0:1 关机,小时取值 0-23,分钟取值 0-59
        int[] poweron = {0,3}; //  即在每天 0:3 开机,小时取值 0-23,分钟取值 0-59*/
        int[] poweroff = {11, 44}; //    即在每天 9:30 关机,小时取值 0-23,分钟取值 0-59
        int[] poweron = {11, 45}; //  即在每天 17:30 开机,小时取值 0-23,分钟取值 0-59
        intent.putExtra("timeon", poweron);
        intent.putExtra("timeoff", poweroff);
        intent.putExtra("type", 2); //类型 2 代表设置每天开关机时间
        intent.putExtra("enable", enable); //使能开关机功能，设为 false,则为关闭,缺省为 true
        context.sendBroadcast(intent);
    }
}
