package megvii.testfacepass.independent.manage;

import megvii.testfacepass.independent.iml.ImlSerialPortRequest;
import megvii.testfacepass.independent.util.OrderUtil;

/**
 * @deprecated
 * */
public class SerialPortRequestManage implements ImlSerialPortRequest.StrHEX {

    private static SerialPortRequestManage serialPortRequestManage;

    private static String OPEN_PARAMETER = "11";

    private static String CLOSE_PARAMETER = "00";

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
        return OrderUtil.generateOrder(OrderUtil.DOOR,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public String closeDoor(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.DOOR,doorNumber,CLOSE_PARAMETER);
    }

    /**
     * 此方法已弃用
     * @deprecated
     * */
    @Override
    public String measureTheDistance(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.WEIGHING_2,doorNumber,"01");
    }


    /**
     * 此方法已弃用
     * @deprecated
     * */
    @Override
    public String measureTheWeight(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.WEIGHING_2,doorNumber,CLOSE_PARAMETER);
    }

    @Override
    public String openTheDisinfection(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.STERILIZE,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public String closeTheDisinfection(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.STERILIZE,doorNumber,CLOSE_PARAMETER);
    }

    @Override
    public String openExhaustFan(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.EXHAUST_FAN,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public String closeExhaustFan(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.EXHAUST_FAN,doorNumber,CLOSE_PARAMETER);
    }

    @Override
    public String openElectromagnetism(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.ELECTROMAGNETIC_SWITCH,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public String closeElectromagnetism(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.ELECTROMAGNETIC_SWITCH,doorNumber,CLOSE_PARAMETER);
    }

    @Override
    public String openTheHeating(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.WARM,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public String closeTheHeating(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.WARM,doorNumber,CLOSE_PARAMETER);
    }

    @Override
    public String openBlender(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.BLENDER,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public String closeBlender(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.BLENDER,doorNumber,CLOSE_PARAMETER);
    }


    @Override
    public String openDogHouse(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.DOG_HOUSE,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public String closeDogHouse(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.DOG_HOUSE,doorNumber,CLOSE_PARAMETER);
    }

    @Override
    public String openLight(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.LIGHT,doorNumber,OPEN_PARAMETER);
    }

    @Override
    public String closeLight(int doorNumber) {
        return OrderUtil.generateOrder(OrderUtil.LIGHT,doorNumber,CLOSE_PARAMETER);
    }
}
