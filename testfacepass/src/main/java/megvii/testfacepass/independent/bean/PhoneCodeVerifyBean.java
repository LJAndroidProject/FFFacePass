package megvii.testfacepass.independent.bean;

public class PhoneCodeVerifyBean {

    /**
     * code : 1
     * msg : 获取成功
     * time : 1603331566
     * data : {"admin_types":"3","user_id":63,"name":"欧阳","is_supadmin":0}
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
         * admin_types : 3
         * user_id : 63
         * name : 欧阳
         * is_supadmin : 0
         */

        private String admin_types;
        private int user_id;
        private String name;
        private int is_supadmin;

        public String getAdmin_types() {
            return admin_types;
        }

        public void setAdmin_types(String admin_types) {
            this.admin_types = admin_types;
        }

        public int getUser_id() {
            return user_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getIs_supadmin() {
            return is_supadmin;
        }

        public void setIs_supadmin(int is_supadmin) {
            this.is_supadmin = is_supadmin;
        }
    }

    @Override
    public String toString() {
        return "PhoneCodeVerifyBean{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", time='" + time + '\'' +
                ", data=" + data.toString() +
                '}';
    }
}
