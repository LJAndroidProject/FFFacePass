package megvii.testfacepass.independent.bean;

import java.util.List;

public class DustbinStateUploadBean {

    /**
     * time :
     * sign :
     * list : [{"dustbinWeight":0,"id":34,"isFull":false,"temperature":0},{"dustbinWeight":0,"id":35,"isFull":false,"temperature":0},{"dustbinWeight":0,"id":36,"isFull":false,"temperature":0},{"dustbinWeight":0,"id":37,"isFull":false,"temperature":0}]
     */

    private String timestamp;
    private String sign;
    private List<ListBean> list;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean {
        /**
         * dustbinWeight : 0
         * id : 34
         * isFull : false
         * temperature : 0
         */

        private double dustbinWeight;
        private long id;
        private boolean isFull;
        private double temperature;

        public ListBean(double dustbinWeight, long id, boolean isFull, double temperature) {
            this.dustbinWeight = dustbinWeight;
            this.id = id;
            this.isFull = isFull;
            this.temperature = temperature;
        }

        public double getDustbinWeight() {
            return dustbinWeight;
        }

        public void setDustbinWeight(double dustbinWeight) {
            this.dustbinWeight = dustbinWeight;
        }

        public long getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean isIsFull() {
            return isFull;
        }

        public void setIsFull(boolean isFull) {
            this.isFull = isFull;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
    }
}
