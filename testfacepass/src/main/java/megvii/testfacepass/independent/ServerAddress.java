package megvii.testfacepass.independent;


/**
 * 服务器地址
 * */
public class ServerAddress {
    //  ip 地址
    public final static String IP = "http://47.106.33.158:8080";
    //  微信扫码登陆
    public final static String LOGIN = IP + "/vxLogin?token=";

    //  人脸图片上传到云服务器
    public final static String FACE_AND_USER_ID_UPLOAD = IP + "";

    //  信息上报
    public final static String MESSAGE_UPLOAD = IP + "";

    //  用户积分增长
    public final static String ADD_USER_SCORE = IP + "";
}
