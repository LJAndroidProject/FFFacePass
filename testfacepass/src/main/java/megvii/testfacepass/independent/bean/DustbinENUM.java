package megvii.testfacepass.independent.bean;

public enum DustbinENUM{
        KITCHEN("厨余垃圾"),HARMFUL("有害垃圾"),WASTE_PAPER("纸片"),BOTTLE("瓶子"),OTHER("其它垃圾"),RECYCLABLES("可回收垃圾");

        private String dustbinType;

        DustbinENUM(String dustbinType) {
            this.dustbinType = dustbinType;
        }

    @Override
    public String toString() {
        return dustbinType;
    }
}