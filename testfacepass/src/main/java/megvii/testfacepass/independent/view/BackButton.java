package megvii.testfacepass.independent.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import megvii.testfacepass.R;


public class BackButton extends AppCompatImageView implements View.OnClickListener{
    private Context context;

    public BackButton(Context context, AttributeSet attrs) {
        super(context, attrs, R.style.RippleWhite);

        this.context = context;

        setImageResource(R.drawable.ic_baseline_arrow_back_24);

        setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, android.R.color.darker_gray), PorterDuff.Mode.SRC_IN));

        setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        ((Activity)context).finish();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = dip2px(context,56);
        int height = dip2px(context,56);

        super.onMeasure(width, height);

    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
