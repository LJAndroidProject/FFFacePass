package megvii.testfacepass.independent.bean;

public class AdminLoginResult {

    /**
     * code : 1
     * msg : 登录成功
     * time : 1603330836
     * data : {"type":"equipment_login","login_status":1}
     */

    private int code;
    private String msg;
    private String time;
    private DataBean data;

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

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * type : equipment_login
         * login_status : 1
         */

        private String type;
        private int login_status;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getLogin_status() {
            return login_status;
        }

        public void setLogin_status(int login_status) {
            this.login_status = login_status;
        }
    }
}
