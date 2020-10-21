package megvii.testfacepass.independent.bean;

/**
 * 指令实体类
 * */
public class OrderMessage{

    private byte[] head;

    private byte[] version;

    private byte[] order;

    private byte[] dataLength;

    private byte[] dataContent;

    private byte[] checksum;

    private byte[] end;

    public byte[] getHead() {
        return head;
    }

    public void setHead(byte[] head) {
        this.head = head;
    }

    public byte[] getVersion() {
        return version;
    }

    public void setVersion(byte[] version) {
        this.version = version;
    }

    public byte[] getOrder() {
        return order;
    }

    public void setOrder(byte[] order) {
        this.order = order;
    }

    public byte[] getDataLength() {
        return dataLength;
    }

    public void setDataLength(byte[] dataLength) {
        this.dataLength = dataLength;
    }

    public byte[] getDataContent() {
        return dataContent;
    }

    public void setDataContent(byte[] dataContent) {
        this.dataContent = dataContent;
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public void setChecksum(byte[] checksum) {
        this.checksum = checksum;
    }

    public byte[] getEnd() {
        return end;
    }

    public void setEnd(byte[] end) {
        this.end = end;
    }
}
