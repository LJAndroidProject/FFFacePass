package megvii.testfacepass.independent.manage;

import android.serialport.SerialPort;

import megvii.testfacepass.independent.iml.ImlSerialPortRequest;
import megvii.testfacepass.independent.util.OrderUtil;
import megvii.testfacepass.independent.util.SerialPortUtil;

public class SerialPortRequestByteManage implements ImlSerialPortRequest.ByteHEX {

    private static SerialPortRequestByteManage serialPortRequestManage;

    public static byte[] OPEN_PARAMETER = new byte[]{0x11};

    public static byte[] CLOSE_PARAMETER = new byte[]{0x00};

    private SerialPortRequestByteManage(){

    }

    public static SerialPortRequestByteManage getInstance(){
        if(serialPortRequestManage == null){
            synchronized (SerialPortRequestByteManage.class){
                if(serialPortRequestManage == null){
                    serialPortRequestManage = new SerialPortRequestByteManage();
                }
            }
        }

        return serialPortRequestManage;
    }

    @Override
    public byte[] openDoor(int doorNumber) {
        //  开启投料口锁
        //  SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().openDogHouse(doorNumber));

        return OrderUtil.generateOrder(OrderUtil.DOOR_BYTE,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public byte[] closeDoor(int doorNumber) {
        //  关闭投料口
        //`SerialPortUtil.getInstance().sendData(SerialPortRequestByteManage.getInstance().closeDogHouse(doorNumber));

        return OrderUtil.generateOrder(OrderUtil.DOOR_BYTE,doorNumber,CLOSE_PARAMETER);
    }

    /**
     * 此方法已弃用
     * @deprecated
     * */
    @Override
    public byte[] measureTheDistance(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.WEIGHING_2_BYTE,doorNumber,new byte[]{0x01});
    }

    /**
     * 此方法已弃用
     * @deprecated
     * */
    @Override
    public byte[] measureTheWeight(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.WEIGHING_2_BYTE,doorNumber,CLOSE_PARAMETER);
    }

    @Override
    public byte[] openTheDisinfection(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.STERILIZE_BYTE,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public byte[] closeTheDisinfection(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.STERILIZE_BYTE,doorNumber,CLOSE_PARAMETER);
    }

    @Override
    public byte[] openExhaustFan(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.EXHAUST_FAN_BYTE,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public byte[] closeExhaustFan(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.EXHAUST_FAN_BYTE,doorNumber,CLOSE_PARAMETER);
    }

    @Override
    public byte[] openElectromagnetism(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.ELECTROMAGNETIC_SWITCH_BYTE,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public byte[] closeElectromagnetism(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.ELECTROMAGNETIC_SWITCH_BYTE,doorNumber,CLOSE_PARAMETER);
    }

    @Override
    public byte[] openTheHeating(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.WARM_BYTE,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public byte[] closeTheHeating(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.WARM_BYTE,doorNumber,CLOSE_PARAMETER);
    }

    @Override
    public byte[] openBlender(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.BLENDER_BYTE,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public byte[] closeBlender(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.BLENDER_BYTE,doorNumber,CLOSE_PARAMETER);
    }


    @Override
    public byte[] openDogHouse(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.DOG_HOUSE_BYTE,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public byte[] closeDogHouse(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.DOG_HOUSE_BYTE,doorNumber,CLOSE_PARAMETER);
    }

    @Override
    public byte[] openLight(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.LIGHT_BYTE,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public byte[] closeLight(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.LIGHT_BYTE,doorNumber,CLOSE_PARAMETER);
    }


    @Override
    public byte[] weightCalibration_1(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.WEIGHING_BYTE,doorNumber,new byte[]{0x01});
    }

    @Override
    public byte[] exitWeightCalibrationMode(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.WEIGHING_BYTE,doorNumber,new byte[]{0x00});
    }

    @Override
    public byte[] weightCalibration_2(int doorNumber,int weight) {
        return OrderUtil.generateOrder(OrderUtil.WEIGHING_2_BYTE,doorNumber,new byte[]{toLH(weight)[1],toLH(weight)[0]});
    }

    @Override
    public byte[] getDate(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.GET_DATA_BYTE,doorNumber,CLOSE_PARAMETER);
    }


    public static byte[] toLH(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }
}
