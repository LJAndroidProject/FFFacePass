package megvii.testfacepass.independent.util;


import android.util.Log;

import com.serialportlibrary.util.ByteStringUtil;

import java.util.ArrayList;
import java.util.List;

import megvii.testfacepass.MainActivity;
import megvii.testfacepass.independent.bean.OrderMessage;

/**
 * 对指令进行修改或者解析类
 * */
public class OrderUtil {

    //  版本号
    public final static String VERSION_CODE = "0001";

    //  上位机到控制电路模块头
    public final static String ANDROID_TO_HARDWARE_HEAD = "F11F";

    //  上位机到控制电路模块尾
    public final static String ANDROID_TO_HARDWARE_END = "F22F";

    //  控制模块到上位机头
    public final static String HARDWARE_TO_ANDROID_HEAD = "F33F";

    //  控制模块到上位机尾
    public final static String HARDWARE_TO_ANDROID_END = "F44F";

    //  数据长度
    public final static String  DATA_LENGTH = "01";

    //  占位符
    public final static String PLACEHOLDER = " ";


    //  开门
    public final static String DOOR = "01";

    //  红外感应上报
    /**
     * @deprecated
     * 红外感应变成了 60 ms 轮询
     * */
    public final static String INFRARED_SENSE = "02";


    //  称重上报
    /**
     * @deprecated
     * 红外感应变成了 60 ms 轮询
     * */
    public final static String WEIGHING = "03";

    //  测距、测满
    /**
     * @deprecated
     * 红外感应变成了 60 ms 轮询
     * */
    public final static String RANGING = "04";

    //  杀菌、消毒
    public final static String STERILIZE = "05";

    //  照明灯
    public final static String LIGHT = "06";

    //  排气扇
    public final static String EXHAUST_FAN = "07";

    //  电磁开关
    public final static String ELECTROMAGNETIC_SWITCH = "08";

    //  加热
    public final static String WARM = "09";

    //  搅拌机
    public final static String BLENDER = "0A";

    //  电磁投料口
    public final static String DOG_HOUSE= "0C";

    /**
     * @param oldString 旧的字符串
     * @param newString 某个位置需要替换成的字符串
     * @param index 第几个
     *
     * 比如将 F1 1F 00 01 01 01 01 11 00 F2 2F 改成 F1 1F 00 01 01 03 01 11 00 F2 2F
     *
     * replace("F1 1F 00 01 01 01 01 11 00 F2 2F",5,"03")
     *
     * */
    public String replace(String oldString , int index , String newString){
        //  字符串转数组
        String [] strArray = oldString.split(" ");

        //  修改 index 位置上的字符串
        strArray[index] = newString ;

        //  字符串拼接
        StringBuilder stringBuilder = new StringBuilder();
        for(String str : strArray){
            stringBuilder.append(str);
            stringBuilder.append(" ");
        }

        //  删除最后一个空格
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        return stringBuilder.toString();
    }


    /**
     * 生成指令
     * @param function 功能
     * @param targetDoor 第几扇门
     * @param parameter 参数
     * */
    public static String generateOrder(String function,int targetDoor,String parameter){

        String targetDoorStr ;

        //  如果目标门是个位数 则补0
        if(targetDoor < 10){
            targetDoorStr = "0" + targetDoor;
        }else{
            targetDoorStr = String.valueOf(targetDoor);
        }

        String centerStr =  VERSION_CODE + function + targetDoorStr + DATA_LENGTH + parameter;

        String sum = centerStrToSumStr(centerStr);

        StringBuilder stringBuilder = new StringBuilder();
        //  添加指令头
        stringBuilder.append(ANDROID_TO_HARDWARE_HEAD);
        //  添加中间
        stringBuilder.append(centerStr);
        //  添加校验和
        stringBuilder.append(sum);
        //  添加指令尾
        stringBuilder.append(ANDROID_TO_HARDWARE_END);

        return stringBuilder.toString();
    }

    /**
     * 截取指定位置的指令
     * */
    /*public static String cutOrderByIndex(String orderString,int index){
        String[] orderArray = orderString.split(" ");

        return orderArray[index];
    }*/



    /**
     * 截取指定位置的指令
     * */
    public static String cutOrderByIndex(String orderString,int index){

        StringBuilder s1 = new StringBuilder(orderString);
        for (int i = 2; i < s1.length(); i += 3) {
            s1.insert(i, ' ');
        }

        String[] orderArray = s1.toString().split(" ");


        for(String orderStr : orderArray){

            Log.i(MainActivity.MY_ORDER,"指令数组添加： " + orderStr);
        }

        return orderArray[index];
    }


    /**
     *
     * @param centerStr 为设备版本号、命令、数据长度、数据参数位
     * @return 返回完整的指令 byte[]
     * */
    public static byte[] centerStrToOrderBytes(String centerStr){

        if(centerStr.contains(" ")){
            centerStr = centerStr.replace(" ", "");
        }

        //  中间内容转为 byte[] 数组
        byte[] bytes = ByteStringUtil.hexStrToByteArray(centerStr);

        //  取 int 类型的 校验和
        int sum = CheckSum(bytes);

        // int 类型的校验和 取 低位
        byte[] sumBytes = new byte[]{intToBytes(sum)[0]};
        //  帧头
        byte[] headBytes = ByteStringUtil.hexStrToByteArray(ANDROID_TO_HARDWARE_HEAD);
        //  帧尾
        byte[] endBytes = ByteStringUtil.hexStrToByteArray(ANDROID_TO_HARDWARE_END);



        //  新的 byte[] 数组大小
        int newBytesSize = bytes.length + headBytes.length + endBytes.length + 1;
        //  创建一个新的 byte[]
        byte[] newBytes = new byte[newBytesSize];


        //  需要复制的数组、复制源的起始位置，目标数组，目标数组的起始位置，复制的长度
        //  首先添加帧头
        System.arraycopy(headBytes,0,newBytes,0,headBytes.length);
        //  添加中间
        System.arraycopy(bytes,0,newBytes,headBytes.length,bytes.length);
        //  添加校验位
        System.arraycopy(sumBytes,0,newBytes,headBytes.length + bytes.length,sumBytes.length);
        //  添加帧尾部
        System.arraycopy(endBytes,0,newBytes,headBytes.length + bytes.length + sumBytes.length,endBytes.length);


        return newBytes;
    }



    /**
     * @param centerStr 为设备版本号、命令、数据长度、数据参数位
     * 返回 hex 转 字符串类型校验和
     * */
    /*public static String centerStrToSumStr(String centerStr){
        if(centerStr.contains(" ")){
            centerStr = centerStr.replace(" ", "");
        }


        //  中间内容转为 byte[] 数组
        byte[] bytes = ByteStringUtil.hexStrToByteArray(centerStr);

        //  取 int 类型的 校验和
        int sum = CheckSum(bytes);

        // int 类型的校验和 取 低位
        byte[] sumBytes = new byte[]{intToBytes(sum)[0]};

        return ByteStringUtil.byteArrayToHexStr(sumBytes);
    }*/


    /*public static String centerStrToSumStr(String centerStr){
        if(centerStr.contains(" ")){
            centerStr = centerStr.replace(" ", "");
        }


        //  中间内容转为 byte[] 数组
        byte[] bytes = ByteStringUtil.hexStrToByteArray(centerStr);

        //  取 int 类型的 校验和
        int sum = CheckSum(bytes);

        sum = bytes[0] + bytes[1] + bytes[2] + bytes[3] + bytes[4] + bytes[5] ;


        int result = 0;
        for(byte b : bytes){
            result += b;
        }

        //Log.i("结果",sum < 10 ? ("0"+sum) : String.valueOf(sum));

        // int 类型的校验和 取 低位
        //byte[] sumBytes = new byte[]{intToBytes(sum)[0]};



        //return sum < 10 ? ("0"+sum) : String.valueOf(sum);

        return String.valueOf(result);

    }*/



    /**
     * 中间字符串转 校验和 （数据累加）
     * @param centerStr 中间字符串
     * @return 校验和字符串
     * */
    public static String centerStrToSumStr(String centerStr){
        if(centerStr.contains(" ")){
            centerStr = centerStr.replace(" ", "");
        }

        //  中间内容转为 byte[] 数组
        byte[] bytes = ByteStringUtil.hexStrToByteArray(centerStr);

        int result = 0;
        for(byte b : bytes){
            result += b;
        }


        //Log.i("结果","指令："+Integer.toHexString(result));
        return Integer.toHexString(result);
    }



    /**
     * 校验和
     * */
    public static int CheckSum(byte[] data){
        int tmp;
        int res = 0;

        for(int i = 0; i <data.length;i++){
            tmp = res << 1;
            tmp += 0xff & data[i];
            res = (tmp & 0xff) + (tmp >> 8) & 0xff;
        }

        return res;
    }

    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。 和bytesToInt（）配套使用
     * @param value 要转换的int值
     * @return byte 数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] =  (byte) ((value>>24) & 0xFF);
        src[2] =  (byte) ((value>>16) & 0xFF);
        src[1] =  (byte) ((value>>8) & 0xFF);
        src[0] =  (byte) (value & 0xFF);
        return src;
    }




}
