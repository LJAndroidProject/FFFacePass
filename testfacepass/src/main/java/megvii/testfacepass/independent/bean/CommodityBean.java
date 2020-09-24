package megvii.testfacepass.independent.bean;

import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.converter.PropertyConverter;
import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {
        @Index(value = "cupboardNumber,tierNumber,tierChildrenNumber,tierChildrenCommodityNumber", unique = true)
})
public class CommodityBean {
    //  商品id ，和 CommodityAlternativeBean 中的 commodityID 一样，主要是为了方便查询
    private long commodityID;

    @Property
    @Convert(converter = CatConverter.class, columnType = String.class)
    private CommodityAlternativeBean commodityAlternativeBean;

    //  柜号
    private long cupboardNumber;

    //  柜 - 层编号
    private long tierNumber;

    //  柜 - 层编号 - 货道
    private int tierChildrenNumber;

    //  柜 - 层编号 - 货道 - 商品
    private int tierChildrenCommodityNumber;

    //  生产日期
    private long dateInProduced;

    //  补货日期
    private long addTime;








    @Generated(hash = 1149085640)
    public CommodityBean(long commodityID, CommodityAlternativeBean commodityAlternativeBean,
            long cupboardNumber, long tierNumber, int tierChildrenNumber,
            int tierChildrenCommodityNumber, long dateInProduced, long addTime) {
        this.commodityID = commodityID;
        this.commodityAlternativeBean = commodityAlternativeBean;
        this.cupboardNumber = cupboardNumber;
        this.tierNumber = tierNumber;
        this.tierChildrenNumber = tierChildrenNumber;
        this.tierChildrenCommodityNumber = tierChildrenCommodityNumber;
        this.dateInProduced = dateInProduced;
        this.addTime = addTime;
    }








    @Generated(hash = 1829956951)
    public CommodityBean() {
    }








    public long getCommodityID() {
        return this.commodityID;
    }








    public void setCommodityID(long commodityID) {
        this.commodityID = commodityID;
    }








    public CommodityAlternativeBean getCommodityAlternativeBean() {
        return this.commodityAlternativeBean;
    }








    public void setCommodityAlternativeBean(CommodityAlternativeBean commodityAlternativeBean) {
        this.commodityAlternativeBean = commodityAlternativeBean;
    }








    public long getCupboardNumber() {
        return this.cupboardNumber;
    }








    public void setCupboardNumber(long cupboardNumber) {
        this.cupboardNumber = cupboardNumber;
    }








    public long getTierNumber() {
        return this.tierNumber;
    }








    public void setTierNumber(long tierNumber) {
        this.tierNumber = tierNumber;
    }








    public int getTierChildrenNumber() {
        return this.tierChildrenNumber;
    }








    public void setTierChildrenNumber(int tierChildrenNumber) {
        this.tierChildrenNumber = tierChildrenNumber;
    }








    public int getTierChildrenCommodityNumber() {
        return this.tierChildrenCommodityNumber;
    }








    public void setTierChildrenCommodityNumber(int tierChildrenCommodityNumber) {
        this.tierChildrenCommodityNumber = tierChildrenCommodityNumber;
    }








    public long getDateInProduced() {
        return this.dateInProduced;
    }








    public void setDateInProduced(long dateInProduced) {
        this.dateInProduced = dateInProduced;
    }








    public long getAddTime() {
        return this.addTime;
    }








    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }


    @Override
    public String toString() {
        return "CommodityBean{" +
                "commodityID=" + commodityID +
                ", commodityAlternativeBean=" + (commodityAlternativeBean != null ? commodityAlternativeBean.toString() : "" ) +
                ", cupboardNumber=" + cupboardNumber +
                ", tierNumber=" + tierNumber +
                ", tierChildrenNumber=" + tierChildrenNumber +
                ", tierChildrenCommodityNumber=" + tierChildrenCommodityNumber +
                ", dateInProduced=" + dateInProduced +
                ", addTime=" + addTime +
                '}';
    }

    public static class CatConverter implements PropertyConverter<CommodityAlternativeBean, String> {
        @Override
        public CommodityAlternativeBean convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return null;
            }
            return new Gson().fromJson(databaseValue, CommodityAlternativeBean.class);
        }

        @Override
        public String convertToDatabaseValue(CommodityAlternativeBean entityProperty) {
            if (entityProperty == null) {
                return null;
            }
            return new Gson().toJson(entityProperty);
        }
    }



}
