package megvii.testfacepass.independent.bean;

/**
 * ic 卡
 * */
public class ICCard {
    private int userId;
    private String cardCode;

    public ICCard(){

    }

    public ICCard(int userId, String cardCode) {
        this.userId = userId;
        this.cardCode = cardCode;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }

    @Override
    public String toString() {
        return "ICCard{" +
                "userId=" + userId +
                ", cardCode='" + cardCode + '\'' +
                '}';
    }
}
