package megvii.testfacepass.independent.iml;

/**
 * 通过串口发送指令请求
 * */
public interface ImlSerialPortRequest {
    /**
     * 电机开门
     * @param doorNumber 第几扇门
     * */
    String openDoor(int doorNumber);


    /**
     * 电机关门
     * @param doorNumber 第几扇门
     * */
    String closeDoor(int doorNumber);


    /**
     * 测距
     * @param doorNumber 第几扇门
     * */
    String measureTheDistance(int doorNumber);


    /**
     * 测重量
     * @param doorNumber 第几扇门
     * */
    String measureTheWeight(int doorNumber);


    /**
     * 开启消毒
     * @param doorNumber 第几扇门
     * */
    String openTheDisinfection(int doorNumber);


    /**
     * 关闭消毒
     * @param doorNumber 第几扇门
     * */
    String closeTheDisinfection(int doorNumber);


    /**
     * 开启排气扇
     * @param doorNumber 第几扇门
     * */
    String openExhaustFan(int doorNumber);



    /**
     * 关闭排气扇
     * @param doorNumber 第几扇门
     * */
    String closeExhaustFan(int doorNumber);


    /**
     * 电磁开启
     * @param doorNumber 第几扇门
     * */
    String openElectromagnetism(int doorNumber);

    /**
     * 电磁关闭
     * @param doorNumber 第几扇门
     * */
    String closeElectromagnetism(int doorNumber);


    /**
     * 开启加热
     * @param doorNumber 第几扇门
     * */
    String openTheHeating(int doorNumber);


    /**
     * 关闭加热
     * @param doorNumber 第几扇门
     * */
    String closeTheHeating(int doorNumber);


    /**
     * 开启搅拌
     * @param doorNumber 第几扇门
     * */
    String openBlender(int doorNumber);


    /**
     * 关闭搅拌
     * @param doorNumber 第几扇门
     * */
    String closeBlender(int doorNumber);
}
