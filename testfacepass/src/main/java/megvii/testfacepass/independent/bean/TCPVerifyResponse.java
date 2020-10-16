package megvii.testfacepass.independent.bean;

public class TCPVerifyResponse {


    /**
     * msg : Hello 7f0000010b5600000001 连接成功,请在10秒内发送认证，否则连接将会关闭！！！
     * client_id : 7f0000010b5600000001
     * code : 1
     */

    private String msg;
    private String client_id;
    private int code;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
