package megvii.testfacepass.independent.manage;

import megvii.testfacepass.independent.iml.ImlSerialPortRequest;
import megvii.testfacepass.independent.util.OrderUtil;

public class SerialPortRequestManage implements ImlSerialPortRequest {

    private static SerialPortRequestManage serialPortRequestManage;

    private SerialPortRequestManage(){

    }

    public static SerialPortRequestManage getInstance(){
        if(serialPortRequestManage == null){
            synchronized (SerialPortRequestManage.class){
                if(serialPortRequestManage == null){
                    serialPortRequestManage = new SerialPortRequestManage();
                }
            }
        }

        return serialPortRequestManage;
    }

    @Override
    public String openDoor(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.DOOR,doorNumber,"11");
    }

    @Override
    public String closeDoor(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.DOOR,doorNumber,"00");
    }

    @Override
    public String measureTheDistance(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.RANGING,doorNumber,"01");
    }

    @Override
    public String measureTheWeight(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.RANGING,doorNumber,"00");
    }

    @Override
    public String openTheDisinfection(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.STERILIZE,doorNumber,"11");
    }

    @Override
    public String closeTheDisinfection(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.STERILIZE,doorNumber,"00");
    }

    @Override
    public String openExhaustFan(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.EXHAUST_FAN,doorNumber,"11");
    }

    @Override
    public String closeExhaustFan(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.EXHAUST_FAN,doorNumber,"00");
    }

    @Override
    public String openElectromagnetism(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.ELECTROMAGNETIC_SWITCH,doorNumber,"11");
    }

    @Override
    public String closeElectromagnetism(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.ELECTROMAGNETIC_SWITCH,doorNumber,"00");
    }

    @Override
    public String openTheHeating(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.WARM,doorNumber,"11");
    }

    @Override
    public String closeTheHeating(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.WARM,doorNumber,"00");
    }

    @Override
    public String openBlender(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.BLENDER,doorNumber,"11");
    }

    @Override
    public String closeBlender(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.BLENDER,doorNumber,"00");
    }
}
