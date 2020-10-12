package megvii.testfacepass.independent.bean;

import java.util.List;

/**
 * 获取服务器商品备选
 * */
public class GetServerGoods {

    /**
     * code : 1
     * msg : 获取成功
     * time : 1602486516
     * data : {"list":[{"id":1,"goods_name":"矿泉水","goods_price":"3.50","status":1,"score_pay":1,"goods_image":"http://ffuserupload.oss-cn-shenzhen.aliyuncs.com/userupload/other/20200929/3ca911c5070f5449496336423d4aedd5e6511ac3.png","goods_wonderful_days":180,"update_time":null},{"id":2,"goods_name":"方便面","goods_price":"5.00","status":1,"score_pay":1,"goods_image":"http://ffuserupload.oss-cn-shenzhen.aliyuncs.com/userupload/other/20200929/3ca911c5070f5449496336423d4aedd5e6511ac3.png","goods_wonderful_days":365,"update_time":null},{"id":3,"goods_name":"瓜子","goods_price":"6.00","status":1,"score_pay":1,"goods_image":"http://ffuserupload.oss-cn-shenzhen.aliyuncs.com/userupload/other/20200929/3ca911c5070f5449496336423d4aedd5e6511ac3.png","goods_wonderful_days":180,"update_time":null},{"id":4,"goods_name":"饮料","goods_price":"3.00","status":1,"score_pay":1,"goods_image":"http://ffuserupload.oss-cn-shenzhen.aliyuncs.com/userupload/other/20200929/3ca911c5070f5449496336423d4aedd5e6511ac3.png","goods_wonderful_days":200,"update_time":null}],"count":4}
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
         * list : [{"id":1,"goods_name":"矿泉水","goods_price":"3.50","status":1,"score_pay":1,"goods_image":"http://ffuserupload.oss-cn-shenzhen.aliyuncs.com/userupload/other/20200929/3ca911c5070f5449496336423d4aedd5e6511ac3.png","goods_wonderful_days":180,"update_time":null},{"id":2,"goods_name":"方便面","goods_price":"5.00","status":1,"score_pay":1,"goods_image":"http://ffuserupload.oss-cn-shenzhen.aliyuncs.com/userupload/other/20200929/3ca911c5070f5449496336423d4aedd5e6511ac3.png","goods_wonderful_days":365,"update_time":null},{"id":3,"goods_name":"瓜子","goods_price":"6.00","status":1,"score_pay":1,"goods_image":"http://ffuserupload.oss-cn-shenzhen.aliyuncs.com/userupload/other/20200929/3ca911c5070f5449496336423d4aedd5e6511ac3.png","goods_wonderful_days":180,"update_time":null},{"id":4,"goods_name":"饮料","goods_price":"3.00","status":1,"score_pay":1,"goods_image":"http://ffuserupload.oss-cn-shenzhen.aliyuncs.com/userupload/other/20200929/3ca911c5070f5449496336423d4aedd5e6511ac3.png","goods_wonderful_days":200,"update_time":null}]
         * count : 4
         */

        private int count;
        private List<ListBean> list;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<ListBean> getList() {
            return list;
        }

        public void setList(List<ListBean> list) {
            this.list = list;
        }

        public static class ListBean {
            /**
             * id : 1
             * goods_name : 矿泉水
             * goods_price : 3.50
             * status : 1
             * score_pay : 1
             * goods_image : http://ffuserupload.oss-cn-shenzhen.aliyuncs.com/userupload/other/20200929/3ca911c5070f5449496336423d4aedd5e6511ac3.png
             * goods_wonderful_days : 180
             * update_time : null
             */

            private int id;
            private String goods_name;
            private double goods_price;
            private int status;
            private int score_pay;
            private String goods_image;
            private int goods_wonderful_days;
            private Object update_time;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getGoods_name() {
                return goods_name;
            }

            public void setGoods_name(String goods_name) {
                this.goods_name = goods_name;
            }

            public double getGoods_price() {
                return goods_price;
            }

            public void setGoods_price(double goods_price) {
                this.goods_price = goods_price;
            }

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public int getScore_pay() {
                return score_pay;
            }

            public void setScore_pay(int score_pay) {
                this.score_pay = score_pay;
            }

            public String getGoods_image() {
                return goods_image;
            }

            public void setGoods_image(String goods_image) {
                this.goods_image = goods_image;
            }

            public int getGoods_wonderful_days() {
                return goods_wonderful_days;
            }

            public void setGoods_wonderful_days(int goods_wonderful_days) {
                this.goods_wonderful_days = goods_wonderful_days;
            }

            public Object getUpdate_time() {
                return update_time;
            }

            public void setUpdate_time(Object update_time) {
                this.update_time = update_time;
            }
        }
    }
}
