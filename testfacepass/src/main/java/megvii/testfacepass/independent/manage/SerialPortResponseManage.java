package megvii.testfacepass.independent.manage;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import megvii.testfacepass.APP;
import megvii.testfacepass.AdvertisingActivity;
import megvii.testfacepass.ControlActivity;
import megvii.testfacepass.MainActivity;
import megvii.testfacepass.independent.bean.DeliveryRecord;
import megvii.testfacepass.independent.bean.DeliveryRecordDao;
import megvii.testfacepass.independent.bean.DeliveryResult;
import megvii.testfacepass.independent.bean.DustbinBean;
import megvii.testfacepass.independent.bean.DustbinBeanDao;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.OrderUtil;
import megvii.testfacepass.independent.util.SerialPortUtil;

import static megvii.testfacepass.MainActivity.MY_ORDER;

/**
 * 端口响应的数据交给此类处理，目前主要是用来实时监听相关，请求响应形式不由此类处理
 * */
public class SerialPortResponseManage {
    /**
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

            String data = OrderUtil.cutOrderByIndex(order,7);

            String doorNumber = OrderUtil.cutOrderByIndex(order,5);


            Log.i(MY_ORDER,"orderCutString ： " + order + ",order :" + data);

            if(orderCutString.equals(OrderUtil.DOOR)){
                //  与开关门有关的指令

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
                    SerialPortUtil.getInstance().sendData(SerialPortRequestManage.getInstance().openTheDisinfection(1));

                }else if("01".equals(data)){
                    toast(context,"第 " + doorNumber + "扇门，关门失败。");
                }


            }else if(orderCutString.equals(OrderUtil.INFRARED_SENSE)){
                //  红外感应上报

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
                //  称重上报,关门后自动上报

                //  因为数据位长度为 2  ( 一般的数据位长度为 1 ) 所以还需要拼接后面两个字符，建议以实体类的形式解析指令，而不是 每两个 拼接
                data += OrderUtil.cutOrderByIndex(order,8);


                toast(context,"第 " + doorNumber + "扇门，当前重量为 ( 16 进制 )：" + data);

            }else if(orderCutString.equals(OrderUtil.RANGING)){
                //  测距


                if(data.equals("00")){
                    //  关门后的当前重量


                    //  如果是小于或等于 0，说明非普通用户，可能是通过NFC 和其它途径进入设置界面的 ，则不做投递记录
                    if(ControlActivity.userId > 0){
                        double deliveryRecordWeight = 0.0;

                        DeliveryRecord deliveryRecord = new DeliveryRecord();
                        deliveryRecord.setDeliveryTime(System.currentTimeMillis());
                        deliveryRecord.setDoorNumber(Integer.parseInt(doorNumber));
                        deliveryRecord.setUserId(ControlActivity.userId);
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
            }else{
                Log.i(MY_ORDER,"未知功能");
            }


            //  每一次得到控制电路的响应，都要更新数据库中的数据，然后再更新 application 中的全局list变量
            //  代表 全局 垃圾桶 list 对象
            APP.setDustbinBeanList(DataBaseUtil.getInstance(context).getDustbinByType(null));

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


}
