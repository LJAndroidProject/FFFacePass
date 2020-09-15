package megvii.testfacepass.independent.util;

import com.littlegreens.netty.client.NettyTcpClient;
import com.littlegreens.netty.client.listener.NettyClientListener;

/**
 * TCP 连接类
 * */
public class TCPConnectUtil {
    private static TCPConnectUtil tcpConnectUtil;

    private static NettyTcpClient mNettyTcpClient;

    private TCPConnectUtil(){

    }

    public static TCPConnectUtil getInstance(){
        if(tcpConnectUtil == null){
            synchronized (TCPConnectUtil.class){
                if(tcpConnectUtil == null){
                    tcpConnectUtil = new TCPConnectUtil();


                    mNettyTcpClient = new NettyTcpClient.Builder()
                            .setHost("47.106.33.158")    //设置服务端地址
                            .setTcpPort(9999) //设置服务端端口号
                            .setMaxReconnectTimes(-1)    //设置最大重连次数 -1时无限重连
                            .setReconnectIntervalTime(5000)    //设置重连间隔时间。单位 毫秒
                            .setSendheartBeat(true) //设置是否发送心跳
                            .setHeartBeatInterval(50) //设置心跳间隔时间。单位：秒
                            .setHeartBeatData(new byte[]{0x03, 0x0F, (byte) 0xFE, 0x05, 0x04, 0x0A}) //设置心跳数据，可以是String类型，也可以是byte[]
                            .setIndex(0)    //设置客户端标识.(因为可能存在多个tcp连接)
                            .build();

                }
            }
        }

        return tcpConnectUtil;
    }


    public void setListener(NettyClientListener nettyClientListener){
        mNettyTcpClient.setListener(nettyClientListener);
    }


    public void connect(){
        mNettyTcpClient.connect();//连接服务器
    }


    public boolean sendData(String b){
        boolean ret = mNettyTcpClient.sendMsgToServer(b.getBytes());
        return ret;
    }


}
