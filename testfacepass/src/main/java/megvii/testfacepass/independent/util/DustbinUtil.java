package megvii.testfacepass.independent.util;

import java.util.List;

import megvii.testfacepass.APP;
import megvii.testfacepass.independent.bean.DustbinStateBean;

/**
 * 垃圾箱管理工具类
 * */
public class DustbinUtil {
    public static DustbinStateBean getDustbinState(int number){
        List<DustbinStateBean> dustbinBeanList = APP.dustbinBeanList;
        for(DustbinStateBean dustbinStateBean:dustbinBeanList){
            if(dustbinStateBean.getDoorNumber() == number){
                return dustbinStateBean;
            }
        }
        //  抛出异常
        return null;
    }
}
