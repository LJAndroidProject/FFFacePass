package megvii.testfacepass.independent.bean;


/**
 *
 * 购买完毕通知服务器端
 * */
public class BuySuccessToServer {

    /**
     * type : product_complete_msg
     * data : {"order_id":"order_id"}
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
         * order_id : order_id
         */

        private String order_id;

        public String getOrder_id() {
            return order_id;
        }

        public void setOrder_id(String order_id) {
            this.order_id = order_id;
        }
    }
}
