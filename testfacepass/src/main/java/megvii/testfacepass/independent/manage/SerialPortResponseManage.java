package megvii.testfacepass.independent.manage;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.serialport.SerialPort;
import android.util.Log;
import android.widget.Toast;

import com.serialportlibrary.util.ByteStringUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

import megvii.testfacepass.APP;
import megvii.testfacepass.ControlActivity;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.bean.ErrorReportBean;
import megvii.testfacepass.independent.bean.ICCard;
import megvii.testfacepass.independent.bean.OrderMessage;
import megvii.testfacepass.independent.bean.WeightCalibrationCall;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.DustbinUtil;
import megvii.testfacepass.independent.util.NetWorkUtil;
import megvii.testfacepass.independent.util.OrderUtil;
import megvii.testfacepass.independent.util.SerialPortUtil;

import static megvii.testfacepass.MainActivity.MY_ORDER;

/**
 * 端口响应的数据交给此类处理，目前主要是用来实时监听相关，请求响应形式不由此类处理
 * */
public class SerialPortResponseManage {

    private DustbinStateBean dustbinStateBean;

    private final static byte ORDER_SUCCESS = 0x10;
    private final static byte ORDER_FAIL = 0x11;

    public static SerialPortResponseManage serialPortResponseManage;

    private SerialPortResponseManage(){

    }

    public static SerialPortResponseManage getInstance(){
        if(serialPortResponseManage == null){
            synchronized (SerialPortResponseManage.class){
                if(serialPortResponseManage == null){
                    serialPortResponseManage = new SerialPortResponseManage();
                    return serialPortResponseManage;
                }
            }
        }
        return serialPortResponseManage;
    }


    public DustbinStateBean getDustbinStateBean() {
        return dustbinStateBean;
    }

    public void setDustbinStateBean(DustbinStateBean dustbinStateBean) {
        this.dustbinStateBean = dustbinStateBean;
    }

    public void inOrderString(Context context , byte[] order) {
        //  打印收到的指令
        Log.i(APP.TAG, "接收: " + ByteStringUtil.byteArrayToHexStr(order));

        if(order.length < 4){
            Log.i(APP.TAG,ByteStringUtil.byteArrayToHexStr(order) + "长度过小");
            return;
        }

        //  首先指令长度大于 4
        if (Arrays.equals(new byte[]{order[0], order[1]}, OrderUtil.HARDWARE_TO_ANDROID_HEAD_BYTES) && Arrays.equals(new byte[]{order[order.length - 2], order[order.length - 1]}, OrderUtil.HARDWARE_TO_ANDROID_END_BYTES)) {

            //  指令解析
            OrderMessage orderMessage = OrderUtil.orderAnalysis(order);

            Log.i("结算",ByteStringUtil.byteArrayToHexStr(order));

            //  如果是开门相关
            if (orderMessage.getOrder()[0] == OrderUtil.DOOR_BYTE) {

                Log.i("结算调试","接受串口数据:" + ByteStringUtil.byteArrayToHexStr(order));

                //  数据位
                if (orderMessage.getDataContent()[0] == 0x10) {
                    //开成功
                    toast(context, "开成功");


                    Log.i("结算调试","开成功");

                    //  开成功关闭消毒灯
                    // SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeTheDisinfection(orderMessage.getOrder()[1]));
                } else if (orderMessage.getDataContent()[0] == 0x11) {
                    //  开失败，未知原因
                    toast(context, "开失败，未知原因");


                    Log.i("结算调试","开失败，原因未知");

                } else if (orderMessage.getDataContent()[0] == 0x12) {
                    //  开失败，电机过载
                    toast(context, "开失败，电机过载");

                    Log.i("结算调试","开失败，电机过载");


                } else if (orderMessage.getDataContent()[0] == 0x00) {
                    //  关成功


                    Log.i("结算调试",orderMessage.getOrder()[1] + "门，关闭成功");

                    //  获取门板号
                    final DustbinStateBean dustbinStateBean = DustbinUtil.getDustbinState(orderMessage.getOrder()[1]);


                    if (dustbinStateBean != null) {
                        Log.i("结算调试",orderMessage.getOrder()[1] + "收到关闭成功后，门状态是否还是为开启:" + dustbinStateBean.getDoorIsOpen());

                        //  改变状态
                        dustbinStateBean.setDoorIsOpen(false);
                        //  改变状态
                        APP.setDustbinState(context,dustbinStateBean);

                        Log.i("结算调试","开始通过事件总线通知控制台关门成功");


                        //  通知开启闪关灯并拍照
                        //  事件总线经常无效
                        /*for(int i = 0 ; i < 10; i ++){
                            EventBus.getDefault().post(dustbinStateBean);
                            try {
                                Thread.sleep(10);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }*/

                        //setDustbinStateBean(dustbinStateBean);


                        /*try {
                            Thread.sleep(100);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        EventBus.getDefault().post(dustbinStateBean);*/

                        if(closeListener != null){
                            closeListener.closeCall(dustbinStateBean);
                            EventBus.getDefault().post(dustbinStateBean);
                        }

                        toast(context, "关成功");


                        //SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDoor(2));
                    }

                } else if (orderMessage.getDataContent()[0] == 0x01) {
                    //  关失败
                    toast(context, "关失败");

                    Log.i("结算调试","关闭失败");


                    //  失败次数 + 1
                    if(APP.dustbinBeanList != null && APP.dustbinBeanList.size() > 0){
                        DustbinStateBean dustbinStateBean = DustbinUtil.getDustbinState(orderMessage.getOrder()[1]);

                        if(dustbinStateBean != null){
                            dustbinStateBean.setCloseFailNumber(dustbinStateBean.getCloseFailNumber() + 1);

                            //  修改状态
                            APP.setDustbinState(context,dustbinStateBean);

                            //  关门失败照样发送
                            EventBus.getDefault().post(dustbinStateBean);
                        }
                    }


                    ErrorReportBean errorReportBean = new ErrorReportBean();
                    //  错误描述
                    errorReportBean.setMsg("关门失败，原因:" + (orderMessage.getDataContent()[0] == 0x01 ? "未知原因失败" : "电机过载"));
                    //  发生时间
                    errorReportBean.setTime(System.currentTimeMillis());
                    //  数据位
                    errorReportBean.setData(ByteStringUtil.byteArrayToHexStr(orderMessage.getDataContent()));
                    //  命令位
                    errorReportBean.setOrderNumber(String.valueOf(orderMessage.getOrder()[0]));
                    //  具体哪一个门
                    errorReportBean.setDoorNumber(orderMessage.getOrder()[1]);
                    //  设备id
                    errorReportBean.setDeviceId(APP.getDeviceId());
                    //  错误编号
                    errorReportBean.setErrorId(null);
                    //  实际指令
                    errorReportBean.setOrderString(ByteStringUtil.byteArrayToHexStr(order));
                    //  开始上报
                    NetWorkUtil.getInstance().errorUpload(errorReportBean);

                    Log.i("结果", errorReportBean.toString());

                    //  本地错误记录
                    DataBaseUtil.getInstance(context).getDaoSession().getErrorReportBeanDao().insert(errorReportBean);
                }


            } else if (orderMessage.getOrder()[0] == OrderUtil.GET_DATA_BYTE) {   //  获取数据

                Log.i(APP.TAG, "获取到数据");
                //  读取数据

                Log.i(APP.TAG, "获取到数据位:" + ByteStringUtil.byteArrayToHexStr(orderMessage.getDataContent()));

                DustbinStateBean dustbinStateBean = new DustbinStateBean();
                //  重量 0-25000 * 10g
                //dustbinStateBean.setDustbinWeight(orderMessage.getDataContent()[0] * 10);
                dustbinStateBean.setDustbinWeight(bytes2Int(new byte[]{orderMessage.getDataContent()[0], orderMessage.getDataContent()[1]}));
                //  温度0-200°C
                dustbinStateBean.setTemperature(orderMessage.getDataContent()[2]);
                //  湿度 0-100%
                dustbinStateBean.setHumidity(orderMessage.getDataContent()[3]);
                //  设置门编号
                dustbinStateBean.setDoorNumber(orderMessage.getOrder()[1]);

                //  其它
                //  1.空	2.接近开关	3.人工门开关	 4.测满	5.推杆过流	6.通信异常	7.投料锁	8.人工门锁
                byte other = orderMessage.getDataContent()[4];


                String tString = Integer.toBinaryString((other & 0xFF) + 0x100).substring(1);
                char[] chars = tString.toCharArray();

                //  挡板是否开启
                dustbinStateBean.setDoorIsOpen(chars[0] == '1');
                //  接近开关
                dustbinStateBean.setProximitySwitch(chars[1] == '1');
                //  人工门开关
                dustbinStateBean.setArtificialDoor(chars[2] == '0');
                //  侧满
                dustbinStateBean.setIsFull(chars[3] == '1');
                //  推杆过流
                dustbinStateBean.setPushRod(chars[4] == '1');
                //  通信异常
                dustbinStateBean.setAbnormalCommunication(chars[5] == '1');
                //  投料锁
                dustbinStateBean.setDeliverLock(chars[6] == '1');
                //  人工锁
                dustbinStateBean.setArtificialDoorLock(chars[7] == '1');

                Log.i(APP.TAG, "获取到数据的 二进制" + tString);
                Log.i(APP.TAG, "状态解析" + dustbinStateBean.toString());

                //  接近开关
                if(dustbinStateBean.getProximitySwitch()){
                    Log.i("定时","有人");
                    APP.hasManTime = System.currentTimeMillis();
                }

                APP.setDustbinState(context,dustbinStateBean);

                Log.i("结算调试",dustbinStateBean.getDoorNumber() + ", 门状态:" + dustbinStateBean.getDoorIsOpen());



                //  人接近 与离开
                    /*DustbinStateBean oldDustbinStateBean =  DustbinUtil.getDustbinState(dustbinStateBean.getDoorNumber());

                    if(oldDustbinStateBean.getProximitySwitch()){
                        if(dustbinStateBean.getProximitySwitch()){

                        }else{

                        }
                    }*/

            } else if (orderMessage.getOrder()[0] == OrderUtil.WEIGHING_BYTE) {


                Log.i("串口","触发校准进入");

                WeightCalibrationCall weightCalibrationCall = new WeightCalibrationCall();
                weightCalibrationCall.setCalibrationNumber(1);
                weightCalibrationCall.setResult(orderMessage.getDataContent());

                EventBus.getDefault().post(weightCalibrationCall);


            } else if (orderMessage.getOrder()[0] == OrderUtil.WEIGHING_2_BYTE) {

                Log.i("串口","触发校准过程");

                WeightCalibrationCall weightCalibrationCall = new WeightCalibrationCall();
                weightCalibrationCall.setCalibrationNumber(2);
                weightCalibrationCall.setResult(orderMessage.getDataContent());
                weightCalibrationCall.setDoorNumber(orderMessage.getOrder()[1]);

                EventBus.getDefault().post(weightCalibrationCall);


            } else if (orderMessage.getOrder()[0] == OrderUtil.STERILIZE_BYTE) {
                //  杀菌、消毒


            } else if (orderMessage.getOrder()[0] == OrderUtil.LIGHT_BYTE) {
                //  照明灯

            } else if (orderMessage.getOrder()[0] == OrderUtil.EXHAUST_FAN_BYTE) {


            } else if (orderMessage.getOrder()[0] == OrderUtil.ELECTROMAGNETIC_SWITCH_BYTE) {
                //  加热

            } else if (orderMessage.getOrder()[0] == OrderUtil.WARM_BYTE) {
                //  加热

            } else if (orderMessage.getOrder()[0] == OrderUtil.BLENDER_BYTE) {


            } else if (orderMessage.getOrder()[0] == OrderUtil.DOG_HOUSE_BYTE) {


            } else if (orderMessage.getOrder()[0] == OrderUtil.VENDING_BYTE) {
                //  售卖机指令转发
                //OrderMessage orderMessage = OrderUtil.orderAnalysis(ByteStringUtil.hexStrToByteArray(order));

            } else if (orderMessage.getOrder()[0] == OrderUtil.IC_CARD_BYTE) {
                Log.i("卡","接收到:" + ByteStringUtil.byteArrayToHexStr(orderMessage.getDataContent()));
                //  IC 卡
                /*ICCard icCard = new ICCard(0, ByteStringUtil.byteArrayToHexStr(orderMessage.getDataContent()));
                EventBus.getDefault().post(icCard);*/


                Intent intent = new Intent("icCard");
                intent.putExtra("content",ByteStringUtil.byteArrayToHexStr(orderMessage.getDataContent()));
                context.sendBroadcast(intent);

            } else {
                Log.i(MY_ORDER, "未知功能");
            }
        } else {
            //  一个是售卖机指令
            Log.i(APP.TAG,"非法指令");
        }

    }


    public CloseListener closeListener;


    public void setCloseListener(CloseListener closeListener) {
        this.closeListener = closeListener;
    }

    /**
     * 关闭监听
     * */
    public interface CloseListener{
        void closeCall(DustbinStateBean dustbinStateBean);
    }

    /**
     * byte[]转int
     * @param bytes
     * @return
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;// 往高位游
        }
        Log.i(APP.TAG,"重量" + value);
        return value;
    }


    public static int bytes2Int(byte[] bytes) {
        int result = 0;
        //将每个byte依次搬运到int相应的位置
        result = bytes[0] & 0xff;
        result = result << 8 | bytes[1] & 0xff;
        Log.i(APP.TAG,"重量" + result);
        return result;
    }




    public static void toast(final Context context,final String text){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,text,Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     * 错误上传
     * */
    private void errorUpload(){

    }


}
