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
        byte[] order = new byte[]{0x28,0x00,0x60,0x00,(byte) (number & 0xff),0x05,0x03,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x30,0x31,0x32,0x33,0x34,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
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
     * 转发拼接
     * @param defaultBytes 原有的售卖机指令
     * @param vendingNumber 第几台售卖机
     * @return 返回经过转发协议处理过后的指令
     * */
    public static byte[] transmitJoint(byte[] defaultBytes,int vendingNumber){
        //  主板指令拼接
        byte[] head = new byte[]{(byte)(0xf1),(byte)(0x1f)};

        byte[] version = new byte[]{0x00,0x01};
        byte[] order = new byte[]{0x0B,(byte) (vendingNumber & 0xff)};
        byte[] dataLength = new byte[]{(byte) (defaultBytes.length & 0xff)};

        //  中间数据
        byte[] newBytes = new byte[version.length + order.length + dataLength.length + defaultBytes.length];
        System.arraycopy(version,0,newBytes,0,version.length);
        System.arraycopy(order,0,newBytes,version.length,order.length);
        System.arraycopy(dataLength,0,newBytes,version.length + order.length,dataLength.length);
        System.arraycopy(defaultBytes,0,newBytes,version.length + order.length + dataLength.length,defaultBytes.length);


        int result = 0;
        for(byte b : newBytes){
            result += b;
        }
        //  得到中间数据的校验位
        byte[] sum = new byte[]{(byte)(result & 0xff)};
        byte[] end = new byte[]{(byte)(0xf2),(byte)(0x2f)};

        //  拼接最后售卖机转发的指令
        byte[] bytes = new byte[head.length+newBytes.length+sum.length+end.length];
        System.arraycopy(head,0,bytes,0,head.length);
        System.arraycopy(newBytes,0,bytes,head.length,newBytes.length);
        System.arraycopy(sum,0,bytes,head.length + newBytes.length,dataLength.length);
        System.arraycopy(end,0,bytes,head.length + newBytes.length + dataLength.length,end.length);

        return bytes;
    }


    /**
     * 支付成功与失败
     * */
    public enum VENDING_RESULT{
        SUCCESS("product_complete_msg"),FAIL("product_fail_msg");

        private String type;

        VENDING_RESULT(String dustbinType) {
            this.type = dustbinType;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    /**
     * 订单回调 出货结果 服务器
     * @param vending_result 出货结果 成功或者失败
     * @param Out_trade_no 订单编号
     * */
    public static void theOrderCall(String Out_trade_no,VENDING_RESULT vending_result){
        BuySuccessToServer.DataBean dataBean = new BuySuccessToServer.DataBean();
        dataBean.setOrder_id(Out_trade_no);
        BuySuccessToServer buySuccessToServer = new BuySuccessToServer();
        buySuccessToServer.setData(dataBean);
        buySuccessToServer.setType(vending_result.toString());

        //  出货失败  setType
        /*product_fail_msg*/
        Log.i("结果","订单完毕:" + new Gson().toJson(buySuccessToServer));
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
