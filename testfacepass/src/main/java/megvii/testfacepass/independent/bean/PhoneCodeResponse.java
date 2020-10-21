package megvii.testfacepass.independent.bean;

/**
 * 用户发送验证码返回
 * */
public class PhoneCodeResponse {

    /**
     * code : 1
     * msg : 验证码发送成功
     * time : 1603246877
     * data : null
     */

    private int code;
    private String msg;
    private String time;
    private Object data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
