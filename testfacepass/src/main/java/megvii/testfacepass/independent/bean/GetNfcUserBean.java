package megvii.testfacepass.independent.bean;

/**
 * 查询 nfc中的数据
 * */
public class GetNfcUserBean {

    /**
     * code : 1
     * msg : 获取成功
     * time : 1603852140
     * data : {"state":1,"user_id":63}
     */

    //

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
         * state : 1
         * user_id : 63
         */

        private int state;
        private int user_id;
        private int user_type;

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getUser_id() {
            return user_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public int getUser_type() {
            return user_type;
        }

        public void setUser_type(int user_type) {
            this.user_type = user_type;
        }
    }
}
