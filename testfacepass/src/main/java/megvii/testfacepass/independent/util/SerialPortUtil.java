package megvii.testfacepass.independent.util;


import android.util.Log;

import com.serialportlibrary.service.impl.SerialPortBuilder;
import com.serialportlibrary.service.impl.SerialPortService;

/**
 * 串口读写
 * */
public class SerialPortUtil {

    private static SerialPortUtil serialPortUtil;

    private static SerialPortService serialPortService;

    private SerialPortUtil(){

    }

    /**
     * 获取单例模式
     * */
    public static SerialPortUtil getInstance(){
        if(serialPortUtil == null){
            synchronized (SerialPortUtil.class){
                if(serialPortUtil == null){
                    serialPortUtil = new SerialPortUtil();

                    serialPortService = new SerialPortBuilder()
                            .setTimeOut(100L)
                            .setBaudrate(115200)
                            .setDevicePath("dev/ttyS2")
                            .createService();
                    serialPortService.isOutputLog(true);
                }
            }
        }

        return serialPortUtil;
    }



    /**
     * 发送数据
     * */
    public byte[] sendData(String data){
        //  如果存在空格字符，则删除空字符
        if(data.contains(" ")){
            data = data.replace(" ","");
        }

        Log.i("结果","发送：" + data);
        return serialPortService.sendData(data);
    }



    public void sendData(byte[] data){
        serialPortService.sendData(data);
    }



    /**
     * 添加监听
     * */
    public void receiveListener(SerialPortService.SerialResponseListener serialResponseListener){
        //  串口监听
        serialPortService.receiveThread(serialResponseListener);
    }


}