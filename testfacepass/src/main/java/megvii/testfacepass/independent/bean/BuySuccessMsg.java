package megvii.testfacepass.independent.bean;

public class BuySuccessMsg {

    /**
     * timestamp : 1602815591
     * goods_id : 6
     * code : 1
     * device_id : TB123456
     * out_trade_no : AMAT_20201016761605f8906580fe68
     */

    private int timestamp;
    private int goods_id;
    private int code;
    private String device_id;
    private String out_trade_no;

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(int goods_id) {
        this.goods_id = goods_id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    @Override
    public String toString() {
        return "BuySuccessMsg{" +
                "timestamp=" + timestamp +
                ", goods_id=" + goods_id +
                ", code=" + code +
                ", device_id='" + device_id + '\'' +
                ", out_trade_no='" + out_trade_no + '\'' +
                '}';
    }
}
