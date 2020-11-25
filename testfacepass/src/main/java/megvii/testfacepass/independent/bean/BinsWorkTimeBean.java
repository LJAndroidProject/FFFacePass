package megvii.testfacepass.independent.bean;

public class BinsWorkTimeBean {
    /**
     * code : 1
     * msg : 获取成功
     * time : 1606185206
     * data : {"am_start_time":"07:00:00","am_end_time":"09:00:00","pm_start_time":"18:00:00","pm_end_time":"21:00:00"}
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
         * am_start_time : 07:00:00
         * am_end_time : 09:00:00
         * pm_start_time : 18:00:00
         * pm_end_time : 21:00:00
         */

        private String am_start_time;
        private String am_end_time;
        private String pm_start_time;
        private String pm_end_time;

        public String getAm_start_time() {
            return am_start_time;
        }

        public void setAm_start_time(String am_start_time) {
            this.am_start_time = am_start_time;
        }

        public String getAm_end_time() {
            return am_end_time;
        }

        public void setAm_end_time(String am_end_time) {
            this.am_end_time = am_end_time;
        }

        public String getPm_start_time() {
            return pm_start_time;
        }

        public void setPm_start_time(String pm_start_time) {
            this.pm_start_time = pm_start_time;
        }

        public String getPm_end_time() {
            return pm_end_time;
        }

        public void setPm_end_time(String pm_end_time) {
            this.pm_end_time = pm_end_time;
        }
    }
}
