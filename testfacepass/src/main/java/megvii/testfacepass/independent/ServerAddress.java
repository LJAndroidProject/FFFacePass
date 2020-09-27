package megvii.testfacepass.independent;


/**
 * 服务器地址
 * */
public class ServerAddress {
    //  ip 地址
    public final static String IP = "https://ffadmin.fenfeneco.com/";
    //  微信扫码登陆
    public final static String LOGIN = IP + "index.php/index/index/weixinRegister?device_id=";

    //  人脸图片上传到云服务器
    public final static String FACE_AND_USER_ID_UPLOAD = IP + "api/other/userFaceRegister";

    //  信息上报
    public final static String MESSAGE_UPLOAD = IP + "";

    //  用户积分增长
    public final static String ADD_USER_SCORE = IP + "";

    //  设备注册
    public final static String DEVICE_REGISTER = IP + "api/Other/equipmentRegister";
}
