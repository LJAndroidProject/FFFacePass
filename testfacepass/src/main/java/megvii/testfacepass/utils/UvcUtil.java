package megvii.testfacepass.utils;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.math.BigInteger;
import java.util.HashMap;

import static android.content.Context.USB_SERVICE;

public class UvcUtil {
    private final static String UVC_CAMERA = "摄像头调试";
    /**
     * 根据桶位获取pid
     * */
    public static int doorNumberToPid(int numb){

        int target = numb * 1111;

        String string = new BigInteger(String.valueOf(target), 16).toString();

        return Integer.parseInt(string);
    }



    /**
     * 根据 pid 转 桶位
     * */
    public static int pidToDoorNumber(int pid){

        Integer x = pid;

        String hex = x.toHexString(x);

        int i = Integer.parseInt(hex);

        return i / 1111;
    }

    //  根据 pid 获取 UVC 摄像头设备
    public static UsbDevice getUsbCameraDevice(Context context , int pid) {
        UsbManager mUsbManager = (UsbManager) context.getSystemService(USB_SERVICE);

        HashMap<String, UsbDevice> deviceMap = mUsbManager.getDeviceList();
        if (deviceMap != null) {
            Log.i(UVC_CAMERA,"摄像头数量:" + deviceMap.size() + "\n");
            for (UsbDevice usbDevice : deviceMap.values()) {
                Log.i(UVC_CAMERA,"摄像头名称:" + usbDevice.getDeviceName() + "\n");
                Log.i(UVC_CAMERA,"摄像头ProductId:" + usbDevice.getProductId() + "\n");
                Log.i(UVC_CAMERA,"摄像头DeviceId:" + usbDevice.getDeviceId() + "\n");
                Log.i(UVC_CAMERA,"摄像头VendorId:" + usbDevice.getVendorId() + "\n");
                if (usbDevice.getProductId() == pid) {
                    return usbDevice;
                }
            }
        }
        return null;
    }
}
