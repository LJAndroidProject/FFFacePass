package megvii.testfacepass.independent.bean;

public class VXLoginResult {


    /**
     * userId : 329
     * QRCode : 1599448330HAsafg412ASD124gdsfka1243SADFASF45123AS76F
     * faceImage : true
     * faceImageUrl :
     * feature : true
     * featureData :
     */

    private long userId;
    private String QRCode;
    private boolean faceImage;
    private String faceImageUrl;
    private boolean feature;
    private String featureData;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getQRCode() {
        return QRCode;
    }

    public void setQRCode(String QRCode) {
        this.QRCode = QRCode;
    }

    public boolean isFaceImage() {
        return faceImage;
    }

    public void setFaceImage(boolean faceImage) {
        this.faceImage = faceImage;
    }

    public String getFaceImageUrl() {
        return faceImageUrl;
    }

    public void setFaceImageUrl(String faceImageUrl) {
        this.faceImageUrl = faceImageUrl;
    }

    public boolean isFeature() {
        return feature;
    }

    public void setFeature(boolean feature) {
        this.feature = feature;
    }

    public String getFeatureData() {
        return featureData;
    }

    public void setFeatureData(String featureData) {
        this.featureData = featureData;
    }

    @Override
    public String toString() {
        return "VXLoginResult{" +
                "userId=" + userId +
                ", QRCode='" + QRCode + '\'' +
                ", faceImage=" + faceImage +
                ", faceImageUrl='" + faceImageUrl + '\'' +
                ", feature=" + feature +
                ", featureData='" + featureData + '\'' +
                '}';
    }
}
