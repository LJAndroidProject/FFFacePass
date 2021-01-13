package megvii.testfacepass.independent.util;


import android.util.Log;

import com.serialportlibrary.service.impl.SerialPortBuilder;
import com.serialportlibrary.service.impl.SerialPortService;
import com.serialportlibrary.util.ByteStringUtil;

import megvii.testfacepass.APP;

/**
 * 串口读写
 * */
public class SerialPortICUtil {

    private static SerialPortICUtil serialPortUtil;

    private static SerialPortService serialPortService;

    private SerialPortICUtil(){

    }

    /**
     * 获取单例模式
     * */
    public static SerialPortICUtil getInstance(){
        if(serialPortUtil == null){
            synchronized (SerialPortICUtil.class){
                if(serialPortUtil == null){
                    serialPortUtil = new SerialPortICUtil();

                    serialPortService = new SerialPortBuilder()
                            .setTimeOut(100L)
                            .setBaudrate(115200)
                            .setDevicePath("dev/ttyS1") //  售卖机的 232是 ttyS1 、 垃圾箱的ttl 是 ttyS2  、 大屏用ttyS3
                            .createService();

                    if(serialPortService != null){
                        serialPortService.isOutputLog(true);
                    }
                }
            }
        }

        return serialPortUtil;
    }






    /**
     * 添加监听2
     * */
    public void receiveICListener(SerialPortService.SerialResponseICListener serialResponseICListener){

        if(serialResponseICListener == null){
            return;
        }

        if(serialPortService == null){
            return;
        }

        //  串口监听
        serialPortService.receiveICThread(serialResponseICListener);
    }


}
