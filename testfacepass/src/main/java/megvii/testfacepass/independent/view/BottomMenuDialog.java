package megvii.testfacepass.independent.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import megvii.testfacepass.R;

import static android.view.View.OVER_SCROLL_NEVER;

public class BottomMenuDialog extends BottomSheetDialog implements DialogInterface.OnKeyListener {
    private Context mContext;

    private ListView listView;

    private String [] data = new String[]{"你还没有输入值"};

    private MenuSelectListener menuSelectListener;

    private ArrayAdapter<String> adapter;

    private TextView titleTextView;

    private String title;

    private boolean backExit = true;

    public BottomMenuDialog(@NonNull Context context) {
        super(context);

        this.mContext = context;
    }


    public void setData(String[] data) {
        this.data = data;

        //  如果已经存在数据则更新
        if(adapter != null && listView != null){
            updateData(data);
        }

    }

    /**
     * 更新显示的列表
     * @param data  数据数组
     * */
    public void updateData(String[] data){
        this.data = data;

        adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * 创建一个弹窗主题内容
     * */
    public void create(){

        int padding = dip2px(mContext,20);

        setOnKeyListener(this);

        setCancelable(false);

        //  列表适配器
         adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, data);

        LinearLayout.LayoutParams listParams ;

        //  如果列表item 数量小于6个高度自适应
        if(data.length <= 8){
            listParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        }else{
            listParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dip2px(mContext,300));
        }


        listView = new ListView(mContext);
        listView.setLayoutParams(listParams);

        if(menuSelectListener != null){
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(position > 0){
                        dismiss();

                        menuSelectListener.selectItem(data[position - 1],position - 1);
                    }
                }
            });
        }

        listView.setDividerHeight(0);
        listView.setPadding(padding,padding / 2,padding,padding);
        listView.setVerticalScrollBarEnabled(false);
        listView.setOverScrollMode(OVER_SCROLL_NEVER);
        listView.setBackgroundResource(R.drawable.control_btn_shape);
        listView.setAdapter(adapter);


        //  添加标题头
        addTitle(padding);

        //  设置主题内容
        setContentView(listView);


        //  圆角
        if(getWindow() != null ){
            getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        }
    }

    @Override
    public void setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
    }

    /**
     * 设置标题
     * @param title 标题
     * */
    public void setTitle(@NonNull String title){
        this.title = title;

        if(titleTextView != null){
            titleTextView.setText(title);
        }

    }


    /**
     * 添加 ListView 头布局也就是标题
     * */
    private void addTitle(int padding){

        //  顶部标题
        titleTextView = new TextView(mContext);
        titleTextView.setBackgroundColor(Color.BLACK);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        titleTextView.setTextColor(Color.BLACK);
        titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
        titleTextView.setPadding(padding / 2,padding,padding,padding);


        Drawable drawableLeft = mContext.getResources().getDrawable(R.drawable.ic_baseline_close_24,null);
        drawableLeft.setTint(Color.BLACK);

        titleTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableLeft, null);
        titleTextView.setCompoundDrawablePadding(4);


        if(title != null){
            titleTextView.setText(title);
        }

        listView.addHeaderView(titleTextView);
    }


    public void setBackExit(boolean backExit) {
        this.backExit = backExit;
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            dismiss();

            if(backExit){
                ((Activity)mContext).finish();
            }
        }

        return false;
    }



    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    /**
     * 设置点击监听器
     * @param menuSelectListener 监听器
     * */
    public void setMenuSelectListener(MenuSelectListener menuSelectListener) {
        this.menuSelectListener = menuSelectListener;

        addMenuSelectListener();
    }

    public void addMenuSelectListener(){
        if(listView != null){
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(position > 0){
                        dismiss();

                        menuSelectListener.selectItem(data[position - 1],position - 1);
                    }
                }
            });
        }
    }

    /**
     * 接口
     * */
    public interface MenuSelectListener{
        void selectItem(String itemTitle, int position);
    }

}
