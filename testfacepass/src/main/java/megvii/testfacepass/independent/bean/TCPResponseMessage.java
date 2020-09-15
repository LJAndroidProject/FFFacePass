package megvii.testfacepass.independent.bean;


/**
 * 来自 TCP 传输过来的数据
 * */
public class TCPResponseMessage {
    private String tcpResponseStr;

    public TCPResponseMessage(String tcpResponseStr) {
        this.tcpResponseStr = tcpResponseStr;
    }

    public String getTcpResponseStr() {
        return tcpResponseStr;
    }
}
