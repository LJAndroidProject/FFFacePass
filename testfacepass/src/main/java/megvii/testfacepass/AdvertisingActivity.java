package megvii.testfacepass;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.stx.xhb.xbanner.XBanner;
import com.stx.xhb.xbanner.transformers.Transformer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static megvii.testfacepass.MainActivity.INTENT_ADVERTISING_CODE;

public class AdvertisingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_advertising);




        XBanner xbanner_view = (XBanner)findViewById(R.id.xbanner_view);

        //图片集合,模拟一下数据
        final List<String> imgesUrl = new ArrayList<>();
        imgesUrl.add("http://www.pptok.com/wp-content/uploads/2012/08/xunguang-7.jpg");
        imgesUrl.add("http://imageprocess.yitos.net/images/public/20160910/99381473502384338.jpg");
        imgesUrl.add("http://imageprocess.yitos.net/images/public/20160910/77991473496077677.jpg");
        imgesUrl.add("http://imageprocess.yitos.net/images/public/20160906/1291473163104906.jpg");



        final List<String> texts = new ArrayList<>();
        texts.add("图片一");
        texts.add("图片二");
        texts.add("图片三");
        texts.add("图片四");

        //数据集合导入banner里
        xbanner_view.setData(imgesUrl,texts);

        //图片加载
        xbanner_view.loadImage(new XBanner.XBannerAdapter() {
            @Override
            public void loadBanner(XBanner banner, Object model, View view, int position) {
                //glide请求网络图片
                Glide.with(AdvertisingActivity.this).load(imgesUrl.get(position)).into((ImageView) view);
            }
        });


        //设置切换延时,单位sm，默认5000sm
        xbanner_view.setPageChangeDuration(3000);

        // 设置XBanner的页面切换特效，有多个，其他的可以到网上去查
        //xbanner_view.setPageTransformer(Transformer.Default);//横向移动

        xbanner_view.setPageTransformer(Transformer.Alpha); //渐变，效果不明显

        //设置轮播图点击监听
        xbanner_view.setOnItemClickListener(new XBanner.OnItemClickListener() {
            @Override
            public void onItemClick(XBanner banner, Object model, View view, int position) {
                Toast.makeText(AdvertisingActivity.this, "点击了"+position, Toast.LENGTH_SHORT).show();
            }
        });

        //-----------一下可以在控件里面进行设置，也可以在当前执行页面进行设置-------------------------
        xbanner_view.setAutoPlayAble(true);   //设置自动轮播

        xbanner_view.setAutoPalyTime(5000);   //图片轮播事件间隔,int类型，默认5000ms


        EventBus.getDefault().register(this);

    }

    /**
     * 从广告界面返回扫脸功能
     * */
    private void backPassFace(){
        Intent intent = new Intent();
        setResult(INTENT_ADVERTISING_CODE, intent);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void serialPortMessage(String text){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }
}