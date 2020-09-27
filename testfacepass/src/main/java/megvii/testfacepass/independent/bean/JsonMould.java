package megvii.testfacepass.independent.bean;

public class JsonMould {


    /**
     * action : QrReturn
     * list : {}
     */

    private String action;
    private ListBean list;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public ListBean getList() {
        return list;
    }

    public void setList(ListBean list) {
        this.list = list;
    }

    public static class ListBean {
    }
}
