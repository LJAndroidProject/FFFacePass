package megvii.testfacepass.independent.bean;

public class WeightCalibrationCall {
        //  校准结果
        public byte[] result;

        //  第几次校准
        public int calibrationNumber;

        //  第几号桶
        public int doorNumber;

        public WeightCalibrationCall(){

        }

    public WeightCalibrationCall(byte[] result, int calibrationNumber, int doorNumber) {
        this.result = result;
        this.calibrationNumber = calibrationNumber;
        this.doorNumber = doorNumber;
    }

    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
    }

    public int getCalibrationNumber() {
        return calibrationNumber;
    }

    public void setCalibrationNumber(int calibrationNumber) {
        this.calibrationNumber = calibrationNumber;
    }

    public int getDoorNumber() {
        return doorNumber;
    }

    public void setDoorNumber(int doorNumber) {
        this.doorNumber = doorNumber;
    }

    @Override
    public String toString() {
        return "WeightCalibrationCall{" +
                "result=" + result +
                ", calibrationNumber=" + calibrationNumber +
                ", doorNumber=" + doorNumber +
                '}';
    }
}