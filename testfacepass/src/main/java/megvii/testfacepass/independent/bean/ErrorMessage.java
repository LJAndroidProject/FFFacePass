package megvii.testfacepass.independent.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 错误信息上报
 * */
@Entity
public class ErrorMessage {
    @Id
    private Long errorId;
    //  错误描述
    private String errorDescribe;

    //  错误发生时间
    private long errorTime;

    //  错误类型
    private String errorType;

    //  错误发生的箱柜，非箱柜错误则为 -1
    private int errorDoor;

    @Generated(hash = 1835227612)
    public ErrorMessage(Long errorId, String errorDescribe, long errorTime,
            String errorType, int errorDoor) {
        this.errorId = errorId;
        this.errorDescribe = errorDescribe;
        this.errorTime = errorTime;
        this.errorType = errorType;
        this.errorDoor = errorDoor;
    }

    @Generated(hash = 1087851058)
    public ErrorMessage() {
    }

    public Long getErrorId() {
        return this.errorId;
    }

    public void setErrorId(Long errorId) {
        this.errorId = errorId;
    }

    public String getErrorDescribe() {
        return this.errorDescribe;
    }

    public void setErrorDescribe(String errorDescribe) {
        this.errorDescribe = errorDescribe;
    }

    public long getErrorTime() {
        return this.errorTime;
    }

    public void setErrorTime(long errorTime) {
        this.errorTime = errorTime;
    }

    public String getErrorType() {
        return this.errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public int getErrorDoor() {
        return this.errorDoor;
    }

    public void setErrorDoor(int errorDoor) {
        this.errorDoor = errorDoor;
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "errorId=" + errorId +
                ", errorDescribe='" + errorDescribe + '\'' +
                ", errorTime=" + errorTime +
                ", errorType='" + errorType + '\'' +
                ", errorDoor=" + errorDoor +
                '}';
    }
}
