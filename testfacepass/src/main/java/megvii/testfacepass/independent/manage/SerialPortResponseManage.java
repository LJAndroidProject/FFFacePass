package megvii.testfacepass.independent.manage;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.serialportlibrary.util.ByteStringUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.Arrays;
import java.util.List;

import megvii.testfacepass.APP;
import megvii.testfacepass.AdvertisingActivity;
import megvii.testfacepass.ControlActivity;
import megvii.testfacepass.MainActivity;
import megvii.testfacepass.WeightCalibrationActivity;
import megvii.testfacepass.independent.bean.DeliveryRecord;
import megvii.testfacepass.independent.bean.DeliveryRecordDao;
import megvii.testfacepass.independent.bean.DeliveryResult;
import megvii.testfacepass.independent.bean.DustbinBeanDao;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.bean.ErrorReportBean;
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
    /**
     * @deprecated
     * @param order 指令的字符串形式
     * */
    public static void inOrderString(Context context , String order){

        //  字母全部转大写
        order = order.toUpperCase();

        Log.i(MY_ORDER,"真正开始处理的指令 ： " + order);

        //  先判定指令 帧头 帧尾 是否符合标准
        if(order.startsWith(OrderUtil.HARDWARE_TO_ANDROID_HEAD) && order.endsWith(OrderUtil.HARDWARE_TO_ANDROID_END)){

            Log.i(MY_ORDER,"有效指令");

            //  获取命令类型  4 是功能，5 是第几扇门，6 是数据长度，7是数据位
            String orderCutString = OrderUtil.cutOrderByIndex(order,4);

            String doorNumber = OrderUtil.cutOrderByIndex(order,5);

            String length = OrderUtil.cutOrderByIndex(order,6);

            String data = OrderUtil.cutOrderByIndex(order,7);


            Log.i(MY_ORDER,"orderCutString ： " + order + ",order :" + data);

            if(orderCutString.equals(OrderUtil.DOOR)){
                //  与关门有关的指令

                //  第几扇门

                /*
                * 数据位
                * 11 - 未知原因失败
                * 12 - 开门失败，消毒灯开启中
                * 13 - 开门失败，电机搅拌中
                * 14 - 开门失败，垃圾箱已满
                *
                * 00 - 关门成功
                * 01 - 关门失败
                * */

                if("11".equals(data)){

                    toast(context,"第 " + doorNumber + "扇门开启失败， 原因 ：未知。");

                }else if("12".equals(data)){

                    toast(context,"第 " + doorNumber + "扇门开启失败， 原因 ：消毒灯开启。");

                }else if("13".equals(data)){

                    toast(context,"第 " + doorNumber + "扇门开启失败， 原因 ：电机搅拌中。");

                }else if("14".equals(data)){

                    toast(context,"第 " + doorNumber + "扇门开启失败， 原因 ：垃圾桶已满。");

                }else if("00".equals(data)){

                    toast(context,"第 " + doorNumber + "扇门，关门成功。");

                    //  关门之后开启消毒
                    SerialPortUtil.getInstance().sendData(SerialPortRequestManage.getInstance().openTheDisinfection(Integer.parseInt(doorNumber)));


                    /*if(APP.userId > 0){
                        double deliveryRecordWeight = 0.0;

                        DeliveryRecord deliveryRecord = new DeliveryRecord();
                        deliveryRecord.setDeliveryTime(System.currentTimeMillis());
                        deliveryRecord.setDoorNumber(Integer.parseInt(doorNumber));
                        deliveryRecord.setUserId(APP.userId);
                        deliveryRecord.setWeight(deliveryRecordWeight);

                        //  增加投递记录，之后通知计算该用户与上一次投递后的结果差
                        DataBaseUtil.getInstance(context).getDaoSession().getDeliveryRecordDao().insert(deliveryRecord);


                        QueryBuilder<DeliveryRecord> queryBuilder =  DataBaseUtil.getInstance(context).getDaoSession().getDeliveryRecordDao().queryBuilder();
                        queryBuilder.where(DustbinBeanDao.Properties.DustbinBoxType.eq(Integer.parseInt(doorNumber)));
                        queryBuilder.orderDesc(DeliveryRecordDao.Properties.Id);
                        queryBuilder.limit(2);


                        //  查询该门板 下最后两条数据
                        List<DeliveryRecord> result = queryBuilder.list();

                        //  没有投递记录，那肯定是不正常的
                        if(result != null && result.size() != 0 ){
                            DeliveryResult deliveryResult = new DeliveryResult();
                            //  说明之前没有投递记录，第一条记录即是本次投递记录
                            if(result.size() == 1){
                                EventBus.getDefault().post(deliveryRecord);
                            }else if(result.size() == 2){
                                EventBus.getDefault().post(deliveryRecord);
                            }
                        }else{
                            toast(context,"记录为0");
                        }

                    }*/


                }else if("01".equals(data)){
                    toast(context,"第 " + doorNumber + "扇门，关门失败。");
                }


            }else if(orderCutString.equals(OrderUtil.GET_DATA)){
                //  数据读取，返回

                /*
                 *数据位
                 * 01 进入范围内上报
                 * 02 离开范围上报
                 * */

                if("01".equals(data)){
                    //  进入范围进入扫脸界面
                    //  指令举例
                    //  F3 3F 00 01 02 01 01 01 00 F4 4F
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                }else if("02".equals(data)){
                    //  离开范围进入广告界面
                    //  指令举例
                    //  F3 3F 00 01 02 01 01 02 00 F4 4F
                    Intent intent = new Intent(context, AdvertisingActivity.class);
                    context.startActivity(intent);
                }


            }else if(orderCutString.equals(OrderUtil.WEIGHING)){
                //  第一次 称重校准

                //  因为数据位长度为 2  ( 一般的数据位长度为 1 ) 所以还需要拼接后面两个字符，建议以实体类的形式解析指令，而不是 每两个 拼接
                data += OrderUtil.cutOrderByIndex(order,8);


                toast(context,"第 " + doorNumber + "扇门，当前重量为 ( 16 进制 )：" + data);

            }else if(orderCutString.equals(OrderUtil.WEIGHING_2)){
                //  第二次 称重校准


                if(data.equals("00")){
                    //  关门后的当前重量


                    //  如果是小于或等于 0，说明非普通用户，可能是通过NFC 和其它途径进入设置界面的 ，则不做投递记录
                    if(APP.userId > 0){
                        double deliveryRecordWeight = 0.0;

                        DeliveryRecord deliveryRecord = new DeliveryRecord();
                        deliveryRecord.setDeliveryTime(System.currentTimeMillis());
                        deliveryRecord.setDoorNumber(Integer.parseInt(doorNumber));
                        deliveryRecord.setUserId(APP.userId);
                        deliveryRecord.setWeight(deliveryRecordWeight);

                        //  增加投递记录，之后通知计算该用户与上一次投递后的结果差
                        DataBaseUtil.getInstance(context).getDaoSession().getDeliveryRecordDao().insert(deliveryRecord);


                        QueryBuilder<DeliveryRecord> queryBuilder =  DataBaseUtil.getInstance(context).getDaoSession().getDeliveryRecordDao().queryBuilder();
                        queryBuilder.where(DustbinBeanDao.Properties.DustbinBoxType.eq(Integer.parseInt(doorNumber)));
                        queryBuilder.orderDesc(DeliveryRecordDao.Properties.Id);
                        queryBuilder.limit(2);


                        //  查询该门板 下最后两条数据
                        List<DeliveryRecord> result = queryBuilder.list();

                        //  没有投递记录，那肯定是不正常的
                        if(result != null && result.size() != 0 ){
                            DeliveryResult deliveryResult = new DeliveryResult();
                            //  说明之前没有投递记录，第一条记录即是本次投递记录
                            if(result.size() == 1){
                                EventBus.getDefault().post(deliveryRecord);
                            }else if(result.size() == 2){
                                EventBus.getDefault().post(deliveryRecord);
                            }
                        }else{
                            toast(context,"记录为0");
                        }

                    }


                    toast(context,"第 " + doorNumber + "扇门的测满、校正成功。");
                }else if(data.equals("01")){
                    toast(context,"第 " + doorNumber + "扇门的测满、校正失败。");
                }else if(data.equals("11")){
                    //  垃圾箱已满，应该禁止开启，所以将不进行投递记录

                    toast(context,"第 " + doorNumber + "扇门内的垃圾已满。");
                }else if(data.equals("10")){
                    toast(context,"第 " + doorNumber + "满后清满已上报 ( 后门维护门关闭后才上报 ) ");

                    //  满清后清空投递记录
                    DataBaseUtil.getInstance(context).getDaoSession().getDeliveryRecordDao().deleteAll();
                }


            }else if(orderCutString.equals(OrderUtil.STERILIZE)){
                //  杀菌、消毒

                if(data.equals("10")){
                    toast(context,"第 " + doorNumber + "扇门的杀菌、消毒开启成功。");
                }else if(data.equals("11")){
                    toast(context,"第 " + doorNumber + "扇门的杀菌、消毒开启失败。");
                }else if(data.equals("00")){
                    toast(context,"第 " + doorNumber + "扇门的杀菌、消毒关闭成功。");
                }else if(data.equals("01")){
                    toast(context,"第 " + doorNumber + "扇门的杀菌、消毒关闭失败。");
                }

            }else if(orderCutString.equals(OrderUtil.EXHAUST_FAN)){
                //  排气扇
                if(data.equals("10")){
                    toast(context,"第 " + doorNumber + "扇门的排气扇开启成功。");
                }else if(data.equals("11")){
                    toast(context,"第 " + doorNumber + "扇门的排气扇开启失败。");
                }else if(data.equals("00")){
                    toast(context,"第 " + doorNumber + "扇门的排气扇关闭成功。");
                }else if(data.equals("01")){
                    toast(context,"第 " + doorNumber + "扇门的排气扇关闭失败。");
                }
            }else if(orderCutString.equals(OrderUtil.ELECTROMAGNETIC_SWITCH)){
                //  电磁开关
                if(data.equals("10")){
                    toast(context,"第 " + doorNumber + "扇门的电磁开启成功。");
                }else if(data.equals("11")){
                    toast(context,"第 " + doorNumber + "扇门的电磁开启失败。");
                }else if(data.equals("00")){
                    toast(context,"第 " + doorNumber + "扇门的电磁关闭成功。");
                }else if(data.equals("01")){
                    toast(context,"第 " + doorNumber + "扇门的电磁关闭失败。");
                }

            }else if(orderCutString.equals(OrderUtil.WARM)){
                //  加热
                if(data.equals("10")){
                    toast(context,"第 " + doorNumber + "扇门的加热开启成功。");
                }else if(data.equals("11")){
                    toast(context,"第 " + doorNumber + "扇门的加热开启失败。");
                }else if(data.equals("00")){
                    toast(context,"第 " + doorNumber + "扇门的加热关闭成功。");
                }else if(data.equals("01")){
                    toast(context,"第 " + doorNumber + "扇门的加热关闭失败。");
                }
            }else if(orderCutString.equals(OrderUtil.BLENDER)){
                //  搅拌机
                if(data.equals("10")){
                    toast(context,"第 " + doorNumber + "扇门的搅拌机开启成功。");
                }else if(data.equals("11")){
                    toast(context,"第 " + doorNumber + "扇门的搅拌机开启失败。");
                }else if(data.equals("00")){
                    toast(context,"第 " + doorNumber + "扇门的搅拌机关闭成功");
                }else if(data.equals("01")){
                    toast(context,"第 " + doorNumber + "扇门的搅拌机关闭失败。");
                }
            }else if(orderCutString.equals(OrderUtil.VENDING)){
                //  售卖机指令转发
                //OrderMessage orderMessage = OrderUtil.orderAnalysis(ByteStringUtil.hexStrToByteArray(order));

            }else{
                Log.i(MY_ORDER,"未知功能");
            }


            //  每一次得到控制电路的响应，都要更新数据库中的数据，然后再更新 application 中的全局list变量
            //  代表 全局 垃圾桶 list 对象
            //.setDustbinBeanList(DataBaseUtil.getInstance(context).getDustbinByType(null));

        }else{
            Log.i(MY_ORDER,"非法指令");
        }
    }




    private final static byte ORDER_SUCCESS = 0x10;
    private final static byte ORDER_FAIL = 0x11;
    public static void inOrderString(Context context , byte[] order){

        OrderMessage orderMessage = OrderUtil.orderAnalysis(order);

        //  先判定指令 帧头 帧尾 是否符合标准
        if(Arrays.equals(orderMessage.getHead(), OrderUtil.HARDWARE_TO_ANDROID_HEAD_BYTES) && Arrays.equals(orderMessage.getEnd(), OrderUtil.HARDWARE_TO_ANDROID_END_BYTES)){
            //  门开关

            if(orderMessage.getOrder()[0] == OrderUtil.DOOR_BYTE){

                //  数据位
                if(orderMessage.getDataContent()[0] == 0x10){
                    //开成功
                    toast(context,"开成功");
                }else if(orderMessage.getDataContent()[0] == 0x11){
                    //  开失败，未知原因
                    toast(context,"开失败，未知原因");
                }else if(orderMessage.getDataContent()[0] == 0x12){
                    //  开失败，电机过载
                    toast(context,"开失败，电机过载");
                }else if(orderMessage.getDataContent()[0] == 0x00){
                    //  关成功
                    toast(context,"关成功");

                    //  获取门板号
                    DustbinStateBean dustbinStateBean = DustbinUtil.getDustbinState(orderMessage.getOrder()[1]);
                    if(dustbinStateBean != null){
                        Log.i("结果","切换的桶" + dustbinStateBean.toString());
                        //  通知开启闪关灯并拍照
                        EventBus.getDefault().post(dustbinStateBean);
                    }

                }else if(orderMessage.getDataContent()[0] == 0x01){
                    //  关失败
                    toast(context,"关失败");


                    ErrorReportBean errorReportBean = new ErrorReportBean();
                    //  错误描述
                    errorReportBean.setMsg("关门失败，原因:" + (orderMessage.getDataContent()[0] == 0x01 ? "未知原因失败" : "电机过载") );
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

                    Log.i("结果",errorReportBean.toString());

                    //  本地错误记录
                    DataBaseUtil.getInstance(context).getDaoSession().getErrorReportBeanDao().insert(errorReportBean);
                }

            }else if(orderMessage.getOrder()[0] == OrderUtil.GET_DATA_BYTE){
                //  读取数据

                DustbinStateBean dustbinStateBean = new DustbinStateBean();
                //  重量 0-25000 * 10g
                dustbinStateBean.setDustbinWeight(orderMessage.getDataContent()[0] * 10);
                //  温度0-200°C
                dustbinStateBean.setTemperature(orderMessage.getDataContent()[1]);
                //  湿度 0-100%
                dustbinStateBean.setHumidity(orderMessage.getDataContent()[2]);
                //  设置门编号
                dustbinStateBean.setDoorNumber(orderMessage.getOrder()[1]);

                //  其它
                //  1.空	2.接近开关	3.人工门开关	 4.测满	5.推杆过流	6.通信异常	7.投料锁	8.人工门锁
                byte other = orderMessage.getDataContent()[3];


                String tString = Integer.toBinaryString((other & 0xFF) + 0x100).substring(1);
                char[] chars = tString.toCharArray();

                //  挡板是否开启
                dustbinStateBean.setProximitySwitch(chars[0] == '1');
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

                Log.i("状态",tString);
                Log.i("状态",dustbinStateBean.toString());

                APP.setDustbinState(dustbinStateBean);

                //  人接近 与离开
                /*DustbinStateBean oldDustbinStateBean =  DustbinUtil.getDustbinState(dustbinStateBean.getDoorNumber());

                if(oldDustbinStateBean.getProximitySwitch()){
                    if(dustbinStateBean.getProximitySwitch()){

                    }else{

                    }
                }*/

            }else if(orderMessage.getOrder()[0] == OrderUtil.WEIGHING_BYTE){


                WeightCalibrationCall weightCalibrationCall = new WeightCalibrationCall();
                weightCalibrationCall.setCalibrationNumber(1);
                weightCalibrationCall.setResult((byte) orderMessage.getDataContent()[0]);

                EventBus.getDefault().post(weightCalibrationCall);


            }else if(orderMessage.getOrder()[0] == OrderUtil.WEIGHING_2_BYTE){

                WeightCalibrationCall weightCalibrationCall = new WeightCalibrationCall();
                weightCalibrationCall.setCalibrationNumber(2);
                weightCalibrationCall.setResult((byte) orderMessage.getDataContent()[0]);

                EventBus.getDefault().post(weightCalibrationCall);


            }else if(orderMessage.getOrder()[0] == OrderUtil.STERILIZE_BYTE){
                //  杀菌、消毒



            }else if(orderMessage.getOrder()[0] == OrderUtil.LIGHT_BYTE){
                //  照明灯

            }else if(orderMessage.getOrder()[0] == OrderUtil.EXHAUST_FAN_BYTE){


            }else if(orderMessage.getOrder()[0] == OrderUtil.ELECTROMAGNETIC_SWITCH_BYTE){
                //  加热

            }else if(orderMessage.getOrder()[0] == OrderUtil.WARM_BYTE){
                //  加热

            }else if(orderMessage.getOrder()[0] == OrderUtil.BLENDER_BYTE){


            }else if(orderMessage.getOrder()[0] == OrderUtil.DOG_HOUSE_BYTE){


            }else if(orderMessage.getOrder()[0] == OrderUtil.VENDING_BYTE){
                //  售卖机指令转发
                //OrderMessage orderMessage = OrderUtil.orderAnalysis(ByteStringUtil.hexStrToByteArray(order));

            }else{
                Log.i(MY_ORDER,"未知功能");
            }


            //  每一次得到控制电路的响应，都要更新数据库中的数据，然后再更新 application 中的全局list变量
            //  代表 全局 垃圾桶 list 对象
            //.setDustbinBeanList(DataBaseUtil.getInstance(context).getDustbinByType(null));

        }else{
            Log.i(MY_ORDER,"非法指令");
        }
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
