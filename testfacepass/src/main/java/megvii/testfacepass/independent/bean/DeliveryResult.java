package megvii.testfacepass.independent.bean;

/**
 * 投递结果，投递后回调
 * */
public class DeliveryResult {
    //  用户 id
    long userId;
    //  门号
    int doorNumber;
    //  重量差
    double weightDiff;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getDoorNumber() {
        return doorNumber;
    }

    public void setDoorNumber(int doorNumber) {
        this.doorNumber = doorNumber;
    }

    public double getWeightDiff() {
        return weightDiff;
    }

    public void setWeightDiff(double weightDiff) {
        this.weightDiff = weightDiff;
    }
}
