package megvii.testfacepass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import megvii.testfacepass.independent.bean.CommodityAlternativeBean;
import megvii.testfacepass.independent.bean.CommodityBean;
import megvii.testfacepass.independent.bean.CommodityBeanDao;
import megvii.testfacepass.independent.util.DataBaseUtil;


/**
 *
 * 如果货道有没有卖完的商品，但是过期了，则不进入商品选择列表
 *
 * 补货的时候会显示该货道没货，但是点进去选择商品的时候，会显示之前过期的商品
 *
 * 一般在后面的商品比前面的商品更慢过期
 *
 * */
public class VendingMachineActivity extends AppCompatActivity {
    private RecyclerView vendingMachineActivity_recyclerView;
    private VendingMachineAdapter vendingMachineAdapter ;
    private TextView vendingMachineActivity_title;
    private List<CommodityBean> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vending_machine);

        //  初始化组件
        vendingMachineActivity_recyclerView = (RecyclerView) findViewById(R.id.vendingMachineActivity_recyclerView);
        vendingMachineActivity_title = (TextView)findViewById(R.id.vendingMachineActivity_title);


        //  跳转到补货界面
        vendingMachineActivity_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VendingMachineActivity.this,ReplenishmentActivity.class);
                startActivity(intent);
            }
        });

        //  多行
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        vendingMachineActivity_recyclerView.setLayoutManager(gridLayoutManager);


        //  查询 所有货道，最后一个货道位 的商品 ID 不为空 (说明该货道有商品)
        list = DataBaseUtil.getInstance(this).getDaoSession().getCommodityBeanDao().queryBuilder()
                .where(CommodityBeanDao.Properties.CommodityID.notEq(0))
                .where(CommodityBeanDao.Properties.TierChildrenCommodityNumber.eq(1))
                .list();



        //  更新商品列表
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                List<CommodityAlternativeBean> commodityAlternativeBeans = new ArrayList<>();
                commodityAlternativeBeans.add(new CommodityAlternativeBean((long) 1,28,"面包",false,0,true,"https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=2383288134,2143816432&fm=15&gp=0.jpg",360));
                commodityAlternativeBeans.add(new CommodityAlternativeBean((long) 2,60,"法式小面包",false,0,true,"https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=1637694247,2029374163&fm=26&gp=0.jpg",360));
                commodityAlternativeBeans.add(new CommodityAlternativeBean((long) 3,7.5,"方便面",true,500,true,"https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=3214305998,151990978&fm=26&gp=0.jpg",180));
                commodityAlternativeBeans.add(new CommodityAlternativeBean((long) 4,10,"瑞士卷",false,0,false,"https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=2975923086,1932516814&fm=26&gp=0.jpg",90));
                commodityAlternativeBeans.add(new CommodityAlternativeBean((long) 5,8,"纯牛奶",true,400,false,"https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2336638272,2853397711&fm=26&gp=0.jpg",90));
                commodityAlternativeBeans.add(new CommodityAlternativeBean((long) 6,4,"矿泉水",true,200,false,"https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=1132615959,1940036971&fm=26&gp=0.jpg",360));


                updateCommodity(commodityAlternativeBeans);
            }
        },5000);



        //  设置适配器
        vendingMachineAdapter = new VendingMachineAdapter(R.layout.vending_machine_layout,removeDuplicateUser(list));


        //  商品为 0 的空布局
        TextView textView = new TextView(this);
        textView.setTextSize(30);
        textView.setGravity(Gravity.CENTER);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        textView.setLayoutParams(lp);
        textView.setText("商品卖完了哦");
        vendingMachineAdapter.setEmptyView(textView);


        //  设置适配器
        vendingMachineActivity_recyclerView.setAdapter(vendingMachineAdapter);

        //  点击购买
        vendingMachineAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(final BaseQuickAdapter adapter, View view, final int position) {
                if(view.getId() == R.id.item_vendingMachineActivity_layout){

                    //  获取当前商品选择页的容器集合
                    final CommodityBean commodityBean = vendingMachineAdapter.getData().get(position);

                    //  如果没货了则移除掉
                    if(commodityBean == null || commodityBean.getCommodityAlternativeBean() ==null){
                        Toast.makeText(VendingMachineActivity.this,"无货",Toast.LENGTH_LONG).show();
                        vendingMachineAdapter.remove(position);
                        return;
                    }


                    //  查询该商品id数量
                    final List<CommodityBean> result = DataBaseUtil.getInstance(VendingMachineActivity.this).getDaoSession().getCommodityBeanDao().queryBuilder().where(CommodityBeanDao.Properties.CommodityID.eq(commodityBean.getCommodityID())).list();


                    //  提取没有过期的商品数量
                    normalCommodityBean(result);


                    //  出货之前再次确认有货
                    if(result != null && result.size() > 0){
                        AlertDialog.Builder alert = new AlertDialog.Builder(VendingMachineActivity.this);
                        alert.setTitle("购买窗口");
                        alert.setMessage("你要购买" + commodityBean.getCommodityAlternativeBean().getCommodityName() + "吗 ? 现在还剩" + result.size() + "个。");
                        alert.setPositiveButton("购买", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                //  清空货道，从后向前减 商品
                                CommodityBean c = result.get(result.size()-1);

                                c.setCommodityID(0);
                                c.setCommodityAlternativeBean(null);

                                //  修改货道信息
                                DataBaseUtil.getInstance(VendingMachineActivity.this).getDaoSession().getCommodityBeanDao().save(c);



                                Toast.makeText(VendingMachineActivity.this,"货道：" +c .getTierChildrenNumber() + "," + c.getTierChildrenCommodityNumber(),Toast.LENGTH_LONG).show();


                                //  说明是最后一个了直接删除
                                if(result.size() == 1){
                                    vendingMachineAdapter.remove(position);
                                }

                            }
                        });
                        alert.create();
                        alert.show();
                    }else{

                        //  如果没货了则移除掉


                        Toast.makeText(VendingMachineActivity.this,"抱歉，该商品无货",Toast.LENGTH_LONG).show();
                        vendingMachineAdapter.remove(position);
                    }

                }
            }
        });

    }



    /**
     * 提取列表里面没有过期的商品
     * @return 没有过期的商品
     * */
    private final long DAY_TIME = 86400000; //  一天的毫秒数
    private void normalCommodityBean(List<CommodityBean> list){

        if(list == null){
            return;
        }

        Iterator<CommodityBean> iterator = list.iterator();

        while (iterator.hasNext()){

            CommodityBean commodityBean = iterator.next();

            //  过期时间 = 生产日期 + 保质期
            long outOfDay = commodityBean.getDateInProduced() + (commodityBean.getCommodityAlternativeBean().getExpirationDate() * DAY_TIME);

            //  如果过期了就不显示了
            if(outOfDay < System.currentTimeMillis()){
                //  移除该商品
                iterator.remove();
            }

        }

    }



    /**
     *
     * 根据商品 ID 去重，最后得到可购买商品
     *
     * @param list 查询到的所有商品
     *
     * @return 返回去重后的商品
     * */
    private static ArrayList<CommodityBean> removeDuplicateUser(List<CommodityBean> list) {
        Set<CommodityBean> set = new TreeSet<>(new Comparator<CommodityBean>() {
            @Override
            public int compare(CommodityBean o1, CommodityBean o2) {

                return String.valueOf(o1.getCommodityID()).compareTo(String.valueOf(o2.getCommodityID()));
            }
        });
        set.addAll(list);
        return new ArrayList<>(set);
    }


    /**
     * 传入需要更新的商品备选，更新商品备选列表，并更新列表商品
     * @param commodityAlternativeBeans 修改当前商品的备选
     * */
    private void updateCommodity(List<CommodityAlternativeBean> commodityAlternativeBeans){

        //  修改备选商品数据库
        DataBaseUtil.getInstance(this).getDaoSession().getCommodityAlternativeBeanDao().saveInTx(commodityAlternativeBeans);

        //  遍历变化的商品
        for(CommodityAlternativeBean commodityAlternativeBean : commodityAlternativeBeans){
            //  查询所有这个id的商品
            List<CommodityBean> com = DataBaseUtil.getInstance(VendingMachineActivity.this).getDaoSession().getCommodityBeanDao().queryBuilder().where(CommodityBeanDao.Properties.CommodityID.eq(commodityAlternativeBean.getCommodityID())).list();

            //  该id下的商品 挨个进行赋值
            for(CommodityBean c :com){
                c.setCommodityID(commodityAlternativeBean.getCommodityID());
                c.setCommodityAlternativeBean(commodityAlternativeBean);
            }

            //  修改应用到数据库
            DataBaseUtil.getInstance(this).getDaoSession().getCommodityBeanDao().saveInTx(com);
        }

        //  更新商品选择列表,按需更新性能更佳！！！！！！！！！！！！
        list = DataBaseUtil.getInstance(this).getDaoSession().getCommodityBeanDao().queryBuilder().where(CommodityBeanDao.Properties.CommodityID.notEq(0)).where(CommodityBeanDao.Properties.TierChildrenCommodityNumber.eq(1)).list();

        //  修改应用到视图列表
        vendingMachineAdapter.setNewData(removeDuplicateUser(list));
    }


    /**
     * 售货机选购界面 适配器
     * 下架是半透明，并且不可点击
     * 卖光了直接不显示
     * */
    public static class VendingMachineAdapter extends BaseQuickAdapter<CommodityBean, BaseViewHolder> {

        public VendingMachineAdapter(int layoutResId, @Nullable List<CommodityBean> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, CommodityBean commodityBean) {

            Glide.with(mContext).load(commodityBean.getCommodityAlternativeBean().getImageUrl()).into((ImageView) helper.getView(R.id.item_vendingMachineActivity_image));

            //  已经下架
            if(commodityBean.getCommodityAlternativeBean().getShelvesOf()){
                helper.setAlpha(R.id.item_vendingMachineActivity_layout,0.4f);
                helper.setText(R.id.vendingMachineActivity_txt,commodityBean.getCommodityAlternativeBean().getCommodityName() + " (已下架)" + "\n" + commodityBean.getCommodityAlternativeBean().getCommodityMoney());
            }else{
                //  添加点击事件
                helper.addOnClickListener(R.id.item_vendingMachineActivity_layout);
                helper.setText(R.id.vendingMachineActivity_txt,commodityBean.getCommodityAlternativeBean().getCommodityName() + "\n" + commodityBean.getCommodityAlternativeBean().getCommodityMoney());
            }

        }
    }
}