package megvii.testfacepass.independent.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;

/**
 * 语音播报类
 * */
public class VoiceUtil {
    private static TextToSpeech textToSpeech;
    private static VoiceUtil voiceUtil;

    private VoiceUtil(){ }

    public static VoiceUtil getInstance(Context context){
        if(voiceUtil == null){
            synchronized (VoiceUtil.class){
                if(voiceUtil == null){
                    voiceUtil = new VoiceUtil();

                    textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {

                        }
                    });


                    //  设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                    textToSpeech.setPitch(1.0f);
                    //  设置语速
                    textToSpeech.setSpeechRate(1.0f);


                }
            }
        }

        return voiceUtil;
    }




    /**
     * 开始语音播报
     * @param data 需要读的文字
     * */
    public void startAuto(String data) {
        //  输入中文，若不支持的设备则不会读出来
        textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(System.currentTimeMillis()));

    }


    /**
     * 关闭语音播报
     * */
    public void stop(){
        textToSpeech.stop(); // 不管是否正在朗读TTS都被打断
    }

    /**
     * 关闭语音播报，并释放资源
     * */
    public void close(){
        textToSpeech.stop(); // 不管是否正在朗读TTS都被打断
        textToSpeech.shutdown(); // 关闭，释放资源
    }
}
