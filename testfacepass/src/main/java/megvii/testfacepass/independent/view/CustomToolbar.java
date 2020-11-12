package megvii.testfacepass.independent.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.support.v7.widget.Toolbar;
import android.view.View;

import megvii.testfacepass.R;

/*<android.support.v7.widget.Toolbar
        app:theme="@style/MyDarkToolBarTheme"
        app:navigationIcon="?attr/homeAsUpIndicator"
        android:id="@+id/ara_toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        app:title="回收桶回收"
        app:titleTextColor="@android:color/white"
        android:background="@color/colorPrimary">*/
public class CustomToolbar extends Toolbar {
    public CustomToolbar(Context context) {
        super(context);
    }

    public CustomToolbar(final Context context, AttributeSet attrs) {
        super(context, attrs);

        setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        setTitleTextColor(Color.WHITE);

        setNavigationOnClickListener(new View.OnClickListener(){
            public void onClick(View view){

                ((Activity)view.getContext()).finish();

            }
        });

    }

    public CustomToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }




}
