package megvii.testfacepass.independent.manage;


/**
 * TCP 响应管理，TCP传递给客户端的数据交由此类处理
 * */
public class TCPResponseManage {

    private CallBack callBack;


    //  TCP 连接中 onMessageResponseClient 回调的内容
    public void execute(byte[] bytes){

    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface  CallBack{
        //  用户微信小程序扫码成功，跳转到控制垃圾箱的界面
        void goControlActivity();

        //  改变广告轮播图内容
        void changeAdvertisingContent();
    }
}
