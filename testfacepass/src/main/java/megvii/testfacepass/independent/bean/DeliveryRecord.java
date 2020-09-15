package megvii.testfacepass.independent.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 投递记录，每一次垃圾回收时，应该清空
 * */
@Entity
public class DeliveryRecord {
    @Unique //  唯一
    @Id(autoincrement = true)
    private Long id;
    //  门编号
    int doorNumber;
    //  用户id
    long userId;
    //  投递时间
    long deliveryTime;
    //  当前重量
    double weight;
    @Generated(hash = 715919933)
    public DeliveryRecord(Long id, int doorNumber, long userId, long deliveryTime,
            double weight) {
        this.id = id;
        this.doorNumber = doorNumber;
        this.userId = userId;
        this.deliveryTime = deliveryTime;
        this.weight = weight;
    }
    @Generated(hash = 1327237091)
    public DeliveryRecord() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getDoorNumber() {
        return this.doorNumber;
    }
    public void setDoorNumber(int doorNumber) {
        this.doorNumber = doorNumber;
    }
    public long getUserId() {
        return this.userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public long getDeliveryTime() {
        return this.deliveryTime;
    }
    public void setDeliveryTime(long deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
    public double getWeight() {
        return this.weight;
    }
    public void setWeight(double weight) {
        this.weight = weight;
    }

    
}
