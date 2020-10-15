package megvii.testfacepass.independent.bean;

import java.util.List;

/**
 *  获取垃圾箱配置返回bean
 * */
public class GetDustbinConfig {


    /**
     * code : 1
     * msg : 设备激活成功
     * time : 1602732239
     * data : {"count":8,"has_amat":1,"device_name":"长洲岛-幸福小区智能垃圾箱设备","list":[{"id":1,"device_id":"EQ123456","bin_type":"A","bin_code":"A01"},{"id":2,"device_id":"EQ123456","bin_type":"A","bin_code":"A02"},{"id":3,"device_id":"EQ123456","bin_type":"B","bin_code":"B01"},{"id":4,"device_id":"EQ123456","bin_type":"C","bin_code":"C01"},{"id":5,"device_id":"EQ123456","bin_type":"C","bin_code":"C02"},{"id":6,"device_id":"EQ123456","bin_type":"D","bin_code":"D01"},{"id":7,"device_id":"EQ123456","bin_type":"D","bin_code":"D02"},{"id":8,"device_id":"EQ123456","bin_type":"D","bin_code":"D03"}]}
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
         * count : 8
         * has_amat : 1
         * device_name : 长洲岛-幸福小区智能垃圾箱设备
         * list : [{"id":1,"device_id":"EQ123456","bin_type":"A","bin_code":"A01"},{"id":2,"device_id":"EQ123456","bin_type":"A","bin_code":"A02"},{"id":3,"device_id":"EQ123456","bin_type":"B","bin_code":"B01"},{"id":4,"device_id":"EQ123456","bin_type":"C","bin_code":"C01"},{"id":5,"device_id":"EQ123456","bin_type":"C","bin_code":"C02"},{"id":6,"device_id":"EQ123456","bin_type":"D","bin_code":"D01"},{"id":7,"device_id":"EQ123456","bin_type":"D","bin_code":"D02"},{"id":8,"device_id":"EQ123456","bin_type":"D","bin_code":"D03"}]
         */

        private int count;
        private int has_amat;
        private String device_name;
        private List<ListBean> list;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getHas_amat() {
            return has_amat;
        }

        public void setHas_amat(int has_amat) {
            this.has_amat = has_amat;
        }

        public String getDevice_name() {
            return device_name;
        }

        public void setDevice_name(String device_name) {
            this.device_name = device_name;
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
             * device_id : EQ123456
             * bin_type : A
             * bin_code : A01
             */

            private int id;
            private String device_id;
            private String bin_type;
            private String bin_code;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getDevice_id() {
                return device_id;
            }

            public void setDevice_id(String device_id) {
                this.device_id = device_id;
            }

            public String getBin_type() {
                return bin_type;
            }

            public void setBin_type(String bin_type) {
                this.bin_type = bin_type;
            }

            public String getBin_code() {
                return bin_code;
            }

            public void setBin_code(String bin_code) {
                this.bin_code = bin_code;
            }
        }
    }

    @Override
    public String toString() {
        return "GetDustbinConfig{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", time='" + time + '\'' +
                ", data=" + data +
                '}';
    }
}
