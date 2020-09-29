package megvii.testfacepass;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import megvii.testfacepass.independent.bean.CommodityAlternativeBean;
import megvii.testfacepass.independent.bean.CommodityAlternativeBeanDao;
import megvii.testfacepass.independent.bean.CommodityBean;
import megvii.testfacepass.independent.bean.CommodityBeanDao;
import megvii.testfacepass.independent.util.DataBaseUtil;

public class VendingMachineActivity extends AppCompatActivity {
    RecyclerView vendingMachineActivity_recyclerView;
    VendingMachineAdapter vendingMachineAdapter ;
    TextView vendingMachineActivity_title;
    List<CommodityBean> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vending_machine);

        vendingMachineActivity_recyclerView = (RecyclerView) findViewById(R.id.vendingMachineActivity_recyclerView);
        vendingMachineActivity_title = (TextView)findViewById(R.id.vendingMachineActivity_title);


        vendingMachineActivity_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VendingMachineActivity.this,ReplenishmentActivity.class);
                startActivity(intent);
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        vendingMachineActivity_recyclerView.setLayoutManager(gridLayoutManager);


        //  根据某个货道最后一个商品不为空来检索所有货道商品
        list = DataBaseUtil.getInstance(this).getDaoSession().getCommodityBeanDao().queryBuilder().where(CommodityBeanDao.Properties.CommodityID.notEq(0)).where(CommodityBeanDao.Properties.TierChildrenCommodityNumber.eq(1)).list();




        vendingMachineAdapter = new VendingMachineAdapter(R.layout.vending_machine_layout,removeDuplicateUser(list));
        vendingMachineActivity_recyclerView.setAdapter(vendingMachineAdapter);


        vendingMachineAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(final BaseQuickAdapter adapter, View view, final int position) {
                if(view.getId() == R.id.item_vendingMachineActivity_layout){

                    final CommodityBean commodityBean = vendingMachineAdapter.getData().get(position);



                    /**
                     * 数量清零时闪退！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
                     * */



                    //  查询该商品id数量
                    final List<CommodityBean> result = DataBaseUtil.getInstance(VendingMachineActivity.this).getDaoSession().getCommodityBeanDao().queryBuilder().where(CommodityBeanDao.Properties.CommodityID.eq(commodityBean.getCommodityID())).list();

                    //  出货之前再次确认有货
                    if(result!=null && result.size()>0){
                        AlertDialog.Builder alert = new AlertDialog.Builder(VendingMachineActivity.this);
                        alert.setTitle("购买窗口");
                        alert.setMessage("你要购买" + commodityBean.getCommodityAlternativeBean().getCommodityName() + "吗 ? 现在还剩" + result.size() + "个。");
                        alert.setPositiveButton("购买", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                //  清空货道
                                CommodityBean c = result.get(result.size()-1);
                                c.setCommodityID(0);
                                c.setCommodityAlternativeBean(null);

                                //  修改货道信息
                                DataBaseUtil.getInstance(VendingMachineActivity.this).getDaoSession().getCommodityBeanDao().save(c);



                                Toast.makeText(VendingMachineActivity.this,"货道：" +c .getTierChildrenNumber() + "," + c.getTierChildrenCommodityNumber(),Toast.LENGTH_LONG).show();


                            }
                        });
                        alert.create();
                        alert.show();
                    }else{
                        Toast.makeText(VendingMachineActivity.this,"抱歉，该商品无货",Toast.LENGTH_LONG).show();
                        vendingMachineAdapter.remove(position);
                    }

                }
            }
        });

    }




    /**
     * 去重
     * */
    private static ArrayList<CommodityBean> removeDuplicateUser(List<CommodityBean> users) {
        Set<CommodityBean> set = new TreeSet<>(new Comparator<CommodityBean>() {
            @Override
            public int compare(CommodityBean o1, CommodityBean o2) {
                //字符串,则按照asicc码升序排列
                return String.valueOf(o1.getCommodityID()).compareTo(String.valueOf(o2.getCommodityID()));
            }
        });
        set.addAll(users);
        return new ArrayList<>(set);
    }


    public class VendingMachineAdapter extends BaseQuickAdapter<CommodityBean, BaseViewHolder> {

        public VendingMachineAdapter(int layoutResId, @Nullable List<CommodityBean> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, CommodityBean commodityBean) {

            Glide.with(mContext).load(commodityBean.getCommodityAlternativeBean().getImageUrl()).into((ImageView) helper.getView(R.id.item_vendingMachineActivity_image));

            helper.setText(R.id.vendingMachineActivity_txt,commodityBean.getCommodityAlternativeBean().getCommodityName());


            //  添加点击事件
            helper.addOnClickListener(R.id.item_vendingMachineActivity_layout);
        }
    }
}