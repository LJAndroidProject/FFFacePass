package megvii.testfacepass.independent;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import megvii.testfacepass.ControlActivity;
import megvii.testfacepass.independent.bean.DeliveryRecord;
import megvii.testfacepass.independent.bean.DeliveryRecordDao;
import megvii.testfacepass.independent.bean.UploadImageServiceBean;
import megvii.testfacepass.independent.util.DataBaseUtil;
import megvii.testfacepass.independent.util.NetWorkUtil;
import okhttp3.Call;

/**
 * 上传图片服务
 * */
public class UploadImageService extends Service {
    @Override
    public void onCreate() {

        EventBus.getDefault().register(this);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void updateService(UploadImageServiceBean uploadImageServiceBean){

        Log.i("take picture","take picture 事件总线收到上传任务");

        if(uploadImageServiceBean != null && uploadImageServiceBean.getPath() != null){

            Log.i("take picture","take picture 事件总线收到上传任务" + uploadImageServiceBean.toString());

            File file = new File(uploadImageServiceBean.getPath());
            //  去除.jpg
            //  设备id + 门板编号 + 用户id + 时间戳 + 垃圾箱id . jpg
            String fileName = file.getName().replace(".jpg","");

            //  解析文件名称
            final String[] fileArray = fileName.split("_");



            //  首先根据时间戳查询 投递记录,
            DeliveryRecord deliveryRecord = DataBaseUtil.getInstance(UploadImageService.this).getDaoSession().getDeliveryRecordDao().queryBuilder().where(DeliveryRecordDao.Properties.DeliveryTime.eq(fileArray[3])).unique();
            //  修改投递记录 中的拍摄图片地址 （关门时创建的投递记录是没有图片的，所以这里拍摄回调需要添加拍摄地址）
            deliveryRecord.setTakePath(file.getPath());
            //  修改信息
            DataBaseUtil.getInstance(UploadImageService.this).getDaoSession().getDeliveryRecordDao().update(deliveryRecord);



            /*
             * 这里可以做一个 判空 如果数据表中，有两个 拍摄记录为 null 的数据，说明有摄像头没有拍摄到图片，说明摄像头故障
             * */
                /*if(DataBaseUtil.getInstance(ControlActivity.this).getDaoSession().getDeliveryRecordDao().queryBuilder().where(DeliveryRecordDao.Properties.TakePath.isNull()).list().size() > 2){
                    //  错误上报
                }*/


            /*
             *
             * 图片文件上传
             * */

            NetWorkUtil.getInstance().fileUpload(file, new NetWorkUtil.FileUploadListener() {
                @Override
                public void success(String fileUrl) {

                    Map<String,String> map = new HashMap<>();
                    map.put("bin_id",fileArray[4]);
                    map.put("user_id",fileArray[2]);
                    map.put("rubbish_image",fileUrl);
                    map.put("time",fileArray[3]);



                    NetWorkUtil.getInstance().doPost(ServerAddress.RUBBISH_IMAGE_POST, map, new NetWorkUtil.NetWorkListener() {
                        @Override
                        public void success(String response) {
                            Log.i("take picture","图片绑定结果" + response);
                        }

                        @Override
                        public void fail(Call call, IOException e) {

                        }

                        @Override
                        public void error(Exception e) {

                        }
                    });
                }

                @Override
                public void error(Exception e) {

                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
