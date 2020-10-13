package megvii.testfacepass.independent.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class DustbinConfig {
    @Id
    private Long dustbinDeviceId;

    //  设备名称
    private String dustbinDeviceName;

    //  设备备注
    private String dustbinDeviceRemark;

    //  经度
    private double longitude;

    //  纬度
    private double latitude;

    //  是否有售卖机
    private boolean hasVendingMachine;

    @Generated(hash = 1479570448)
    public DustbinConfig(Long dustbinDeviceId, String dustbinDeviceName,
            String dustbinDeviceRemark, double longitude, double latitude,
            boolean hasVendingMachine) {
        this.dustbinDeviceId = dustbinDeviceId;
        this.dustbinDeviceName = dustbinDeviceName;
        this.dustbinDeviceRemark = dustbinDeviceRemark;
        this.longitude = longitude;
        this.latitude = latitude;
        this.hasVendingMachine = hasVendingMachine;
    }

    @Generated(hash = 1195503439)
    public DustbinConfig() {
    }

    public Long getDustbinDeviceId() {
        return this.dustbinDeviceId;
    }

    public void setDustbinDeviceId(Long dustbinDeviceId) {
        this.dustbinDeviceId = dustbinDeviceId;
    }

    public String getDustbinDeviceName() {
        return this.dustbinDeviceName;
    }

    public void setDustbinDeviceName(String dustbinDeviceName) {
        this.dustbinDeviceName = dustbinDeviceName;
    }

    public String getDustbinDeviceRemark() {
        return this.dustbinDeviceRemark;
    }

    public void setDustbinDeviceRemark(String dustbinDeviceRemark) {
        this.dustbinDeviceRemark = dustbinDeviceRemark;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public boolean getHasVendingMachine() {
        return this.hasVendingMachine;
    }

    public void setHasVendingMachine(boolean hasVendingMachine) {
        this.hasVendingMachine = hasVendingMachine;
    }


    @Override
    public String toString() {
        return "DustbinConfig{" +
                "dustbinDeviceId=" + dustbinDeviceId +
                ", dustbinDeviceName='" + dustbinDeviceName + '\'' +
                ", dustbinDeviceRemark='" + dustbinDeviceRemark + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", hasVendingMachine=" + hasVendingMachine +
                '}';
    }
}
