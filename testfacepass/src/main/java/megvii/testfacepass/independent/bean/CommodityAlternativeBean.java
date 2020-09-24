package megvii.testfacepass.independent.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 商品备选
 * */
@Entity
public class CommodityAlternativeBean{
    //  商品id
    @Unique
    private Long commodityID;

    //  商品价格
    private double commodityMoney;

    //  商品名称
    private String commodityName;

    //  是否可用积分支付
    private boolean canUserIntegral;

    //  需要的积分数量
    private int integralNumber;

    //  上架情况
    private boolean shelvesOf;

    //  图片地址
    private String imageUrl;

    //  保质期
    private long expirationDate;

    @Generated(hash = 1835223671)
    public CommodityAlternativeBean(Long commodityID, double commodityMoney,
            String commodityName, boolean canUserIntegral, int integralNumber,
            boolean shelvesOf, String imageUrl, long expirationDate) {
        this.commodityID = commodityID;
        this.commodityMoney = commodityMoney;
        this.commodityName = commodityName;
        this.canUserIntegral = canUserIntegral;
        this.integralNumber = integralNumber;
        this.shelvesOf = shelvesOf;
        this.imageUrl = imageUrl;
        this.expirationDate = expirationDate;
    }

    @Generated(hash = 358769380)
    public CommodityAlternativeBean() {
    }

    public Long getCommodityID() {
        return this.commodityID;
    }

    public void setCommodityID(Long commodityID) {
        this.commodityID = commodityID;
    }

    public double getCommodityMoney() {
        return this.commodityMoney;
    }

    public void setCommodityMoney(double commodityMoney) {
        this.commodityMoney = commodityMoney;
    }

    public String getCommodityName() {
        return this.commodityName;
    }

    public void setCommodityName(String commodityName) {
        this.commodityName = commodityName;
    }

    public boolean getCanUserIntegral() {
        return this.canUserIntegral;
    }

    public void setCanUserIntegral(boolean canUserIntegral) {
        this.canUserIntegral = canUserIntegral;
    }

    public int getIntegralNumber() {
        return this.integralNumber;
    }

    public void setIntegralNumber(int integralNumber) {
        this.integralNumber = integralNumber;
    }

    public boolean getShelvesOf() {
        return this.shelvesOf;
    }

    public void setShelvesOf(boolean shelvesOf) {
        this.shelvesOf = shelvesOf;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getExpirationDate() {
        return this.expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }


    @Override
    public String toString() {
        return "CommodityAlternativeBean{" +
                "commodityID=" + commodityID +
                ", commodityMoney=" + commodityMoney +
                ", commodityName='" + commodityName + '\'' +
                ", canUserIntegral=" + canUserIntegral +
                ", integralNumber=" + integralNumber +
                ", shelvesOf=" + shelvesOf +
                ", imageUrl='" + imageUrl + '\'' +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
