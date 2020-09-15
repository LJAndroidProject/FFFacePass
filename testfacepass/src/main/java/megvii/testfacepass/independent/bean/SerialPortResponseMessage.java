package megvii.testfacepass.independent.bean;

/**
 * 来自串口传输过来的数据
 * */
public class SerialPortResponseMessage {
    private SerialPortResponseMessage serialPortResponseStr;

    public SerialPortResponseMessage(SerialPortResponseMessage serialPortResponseStr) {
        this.serialPortResponseStr = serialPortResponseStr;
    }

    public SerialPortResponseMessage getSerialPortResponseStr() {
        return serialPortResponseStr;
    }
}
