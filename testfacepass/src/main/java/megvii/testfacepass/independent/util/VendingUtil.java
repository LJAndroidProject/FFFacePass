package megvii.testfacepass.independent.util;

import android.util.Log;

import com.google.gson.Gson;

import megvii.testfacepass.independent.bean.BuySuccessToServer;

/**
 * 售卖机指令工具类
 * */
public class VendingUtil {
    public final static String TAG = "自动售卖机调试";

    /**
     * @param number 货道
     * 售卖机出货
     * */
    public static void delivery(int number){
        byte[] headBytes = new byte[]{0x0D,0x24};
        byte[] order = new byte[]{0x28,0x00,0x60,0x00,(byte) (number & 0xff),0x05,0x03,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x30,0x31,0x32,0x33,0x34,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,};
        byte[] sumBytes = new byte[]{getXor(order)};
        byte[] endBytes = new byte[]{0x0D,0x0A};


        byte[] newBytes = new byte[headBytes.length + order.length + sumBytes.length + endBytes.length];


        //  需要复制的数组、复制源的起始位置，目标数组，目标数组的起始位置，复制的长度
        //  首先添加帧头
        System.arraycopy(headBytes,0,newBytes,0,headBytes.length);
        //  添加中间
        System.arraycopy(order,0,newBytes,headBytes.length,order.length);
        //  添加校验位
        System.arraycopy(sumBytes,0,newBytes,headBytes.length + order.length,sumBytes.length);
        //  添加帧尾部
        System.arraycopy(endBytes,0,newBytes,headBytes.length + order.length + sumBytes.length,endBytes.length);

        //  开始通知售卖机
        SerialPortUtil.getInstance().sendData(newBytes);
    }

    /**
     * 订单完毕通知服务器端
     * {"data":{"order_id":"AMAT_20201016229985f89171d3c3e9"},"type":"product_complete_msg"}
     * @param orderNumber 订单号
     * */
    private void theOrderFinished(String orderNumber){
        BuySuccessToServer.DataBean dataBean = new BuySuccessToServer.DataBean();
        dataBean.setOrder_id(orderNumber);
        BuySuccessToServer buySuccessToServer = new BuySuccessToServer();
        buySuccessToServer.setData(dataBean);
        buySuccessToServer.setType("product_complete_msg");

        Log.i(TAG,"通知服务器端出货完毕:" + new Gson().toJson(buySuccessToServer));
        TCPConnectUtil.getInstance().sendData(new Gson().toJson(buySuccessToServer));
    }


    /**
     * 拼接
     * */
    private void joint(){
        byte[] head = new byte[]{0xf,0x1,0x1,0xf};
        //OrderUtil.centerStrToSumStr();
    }

    /**
     * 售卖机校验位 异或 运算
     * */
    public static byte getXor(byte[] datas){

        byte temp=datas[0];

        for (int i = 1; i <datas.length; i++) {
            temp ^=datas[i];
        }

        return temp;
    }
}
