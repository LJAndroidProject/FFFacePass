package megvii.testfacepass.independent.bean;

public class WeightCalibrationCall {
        //  校准结果
        public byte result;

        //  第几次校准
        public int calibrationNumber;

        public WeightCalibrationCall(){

        }


        public WeightCalibrationCall(byte result, int calibrationNumber) {
            this.result = result;
            this.calibrationNumber = calibrationNumber;
        }

        public void setResult(byte result) {
            this.result = result;
        }

        public void setCalibrationNumber(int calibrationNumber) {
            this.calibrationNumber = calibrationNumber;
        }

        public byte getResult() {
            return result;
        }

        public int getCalibrationNumber() {
            return calibrationNumber;
        }

    @Override
    public String toString() {
        return "WeightCalibrationCall{" +
                "result=" + result +
                ", calibrationNumber=" + calibrationNumber +
                '}';
    }
}