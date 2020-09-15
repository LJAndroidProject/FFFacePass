package megvii.testfacepass.independent.util;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import megvii.testfacepass.APP;
import megvii.testfacepass.ControlActivity;
import megvii.testfacepass.MainActivity;
import megvii.testfacepass.independent.bean.DaoMaster;
import megvii.testfacepass.independent.bean.DaoSession;
import megvii.testfacepass.independent.bean.DustbinBean;
import megvii.testfacepass.independent.bean.DustbinBeanDao;
import megvii.testfacepass.independent.bean.DustbinENUM;
import megvii.testfacepass.independent.bean.UserMessageDao;

/**
 * 一些数据库操作
 * */
public class DataBaseUtil {

    private static DataBaseUtil dataBaseUtil;

    private static DaoMaster.DevOpenHelper helper;

    private static SQLiteDatabase database;

    private static DaoMaster daoMaster;

    private static DaoSession mDaoSession;

    private DataBaseUtil(){

    }

    public static DataBaseUtil getInstance(Context context){
        if(dataBaseUtil == null){
            synchronized (DataBaseUtil.class){
                if(dataBaseUtil == null){
                    dataBaseUtil = new DataBaseUtil();


                    //  创建数据库database.db
                    helper = new DaoMaster.DevOpenHelper(context,"database.db");
                    //  获取可写数据库
                    database = helper.getWritableDatabase();
                    //  获取数据库对象
                    daoMaster = new DaoMaster(database);
                    //  获取Dao对象管理者
                    mDaoSession = daoMaster.newSession();

                }
            }
        }
        return dataBaseUtil;
    }



    public DaoSession getDaoSession(){
        return mDaoSession;
    }


    /**
     * 数据库内是否有垃圾箱配置
     * @return 如果有 返回 true ， 没有 返回 false
     * */
    public boolean hasDustBinConfig(){
        //  查询数据库中是否有垃圾箱配置
        DustbinBeanDao dustbinBeanDao = getDaoSession().getDustbinBeanDao();
        List<DustbinBean> dustbinBeanList = dustbinBeanDao.queryBuilder().build().list();

        if(dustbinBeanList == null || dustbinBeanList.size() == 0){
            return false;
        }


        return true;
    }

    /**
     * 设置垃圾箱配置
     * @param list 垃圾箱配置
     * */
    public void setDustBinConfig(List<DustbinBean> list){
        DustbinBeanDao dustbinBeanDao = getDaoSession().getDustbinBeanDao();

        //  删除之前所有配置信息
        dustbinBeanDao.deleteAll();

        //  插入新的配置
        dustbinBeanDao.insertOrReplaceInTx(list);
    }


    /**
     * 传入需要的垃圾箱类型，返回垃圾类型对应的 门板 列表   如果为 null 则 返回所有
     * @param dustbinENUM 传入垃圾箱类型
     * */
    public List<DustbinBean> getDustbinByType(DustbinENUM dustbinENUM){

        DustbinBeanDao dustbinBeanDao = getDaoSession().getDustbinBeanDao();

        if(dustbinENUM == null){
            return dustbinBeanDao.queryBuilder().build().list();
        }else{
            return dustbinBeanDao.queryBuilder().where(DustbinBeanDao.Properties.DustbinBoxType.eq(dustbinENUM)).build().list();
        }
    }



    /**
     * @param number 获取门板为 number 的对象
     * */
    public DustbinBean getDustbinByNumber(int number){

        DustbinBeanDao dustbinBeanDao = getDaoSession().getDustbinBeanDao();

        return dustbinBeanDao.queryBuilder().where(DustbinBeanDao.Properties.DoorNumber.eq(number)).build().unique();
    }


    /**
     * @param dustbinBean 门板对象
     * */
    public void setDustbinByNumber(DustbinBean dustbinBean){

        DustbinBeanDao dustbinBeanDao = getDaoSession().getDustbinBeanDao();

        dustbinBeanDao.insertOrReplaceInTx(dustbinBean);
    }






}