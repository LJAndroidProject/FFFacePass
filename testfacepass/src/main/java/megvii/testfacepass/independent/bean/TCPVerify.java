package megvii.testfacepass.independent.bean;

public class TCPVerify {

    /**
     * type : login
     * data : {"sign":"asdasdasdasdas","timestamp":"16092023232"}
     */

    private String type;
    private DataBean data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * sign : asdasdasdasdas
         * timestamp : 16092023232
         */

        private String sign;
        private String timestamp;

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}
