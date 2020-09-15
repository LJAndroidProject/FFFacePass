package megvii.testfacepass.independent.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class DustbinBean {
    
    //  对应的门板编号
    @Unique
    private int doorNumber;

    //  箱体类型，厨余、可回收、有害、其他
    private String dustbinBoxType;

    //  门板开启状态
    private boolean dustbinEnabled;

    //  当前重量

    private double dustbinWeight;

    @Generated(hash = 2054684576)
    public DustbinBean(int doorNumber, String dustbinBoxType,
            boolean dustbinEnabled, double dustbinWeight) {
        this.doorNumber = doorNumber;
        this.dustbinBoxType = dustbinBoxType;
        this.dustbinEnabled = dustbinEnabled;
        this.dustbinWeight = dustbinWeight;
    }

    @Generated(hash = 306016680)
    public DustbinBean() {
    }

    public int getDoorNumber() {
        return this.doorNumber;
    }

    public void setDoorNumber(int doorNumber) {
        this.doorNumber = doorNumber;
    }

    public String getDustbinBoxType() {
        return this.dustbinBoxType;
    }

    public void setDustbinBoxType(String dustbinBoxType) {
        this.dustbinBoxType = dustbinBoxType;
    }

    public boolean getDustbinEnabled() {
        return this.dustbinEnabled;
    }

    public void setDustbinEnabled(boolean dustbinEnabled) {
        this.dustbinEnabled = dustbinEnabled;
    }

    public double getDustbinWeight() {
        return this.dustbinWeight;
    }

    public void setDustbinWeight(double dustbinWeight) {
        this.dustbinWeight = dustbinWeight;
    }


    @Override
    public String toString() {
        return "DustbinBean{" +
                "doorNumber=" + doorNumber +
                ", dustbinBoxType='" + dustbinBoxType + '\'' +
                ", dustbinEnabled=" + dustbinEnabled +
                ", dustbinWeight=" + dustbinWeight +
                '}';
    }
}
