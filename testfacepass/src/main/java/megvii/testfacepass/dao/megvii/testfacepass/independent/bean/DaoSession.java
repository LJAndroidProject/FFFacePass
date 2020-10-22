package megvii.testfacepass.independent.bean;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import megvii.testfacepass.independent.bean.DeliveryRecord;
import megvii.testfacepass.independent.bean.DustbinBean;
import megvii.testfacepass.independent.bean.UserMessage;
import megvii.testfacepass.independent.bean.CommodityAlternativeBean;
import megvii.testfacepass.independent.bean.CommodityBean;
import megvii.testfacepass.independent.bean.DustbinConfig;
import megvii.testfacepass.independent.bean.ErrorMessage;
import megvii.testfacepass.independent.bean.DustbinStateBean;
import megvii.testfacepass.independent.bean.ErrorReportBean;

import megvii.testfacepass.independent.bean.DeliveryRecordDao;
import megvii.testfacepass.independent.bean.DustbinBeanDao;
import megvii.testfacepass.independent.bean.UserMessageDao;
import megvii.testfacepass.independent.bean.CommodityAlternativeBeanDao;
import megvii.testfacepass.independent.bean.CommodityBeanDao;
import megvii.testfacepass.independent.bean.DustbinConfigDao;
import megvii.testfacepass.independent.bean.ErrorMessageDao;
import megvii.testfacepass.independent.bean.DustbinStateBeanDao;
import megvii.testfacepass.independent.bean.ErrorReportBeanDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig deliveryRecordDaoConfig;
    private final DaoConfig dustbinBeanDaoConfig;
    private final DaoConfig userMessageDaoConfig;
    private final DaoConfig commodityAlternativeBeanDaoConfig;
    private final DaoConfig commodityBeanDaoConfig;
    private final DaoConfig dustbinConfigDaoConfig;
    private final DaoConfig errorMessageDaoConfig;
    private final DaoConfig dustbinStateBeanDaoConfig;
    private final DaoConfig errorReportBeanDaoConfig;

    private final DeliveryRecordDao deliveryRecordDao;
    private final DustbinBeanDao dustbinBeanDao;
    private final UserMessageDao userMessageDao;
    private final CommodityAlternativeBeanDao commodityAlternativeBeanDao;
    private final CommodityBeanDao commodityBeanDao;
    private final DustbinConfigDao dustbinConfigDao;
    private final ErrorMessageDao errorMessageDao;
    private final DustbinStateBeanDao dustbinStateBeanDao;
    private final ErrorReportBeanDao errorReportBeanDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        deliveryRecordDaoConfig = daoConfigMap.get(DeliveryRecordDao.class).clone();
        deliveryRecordDaoConfig.initIdentityScope(type);

        dustbinBeanDaoConfig = daoConfigMap.get(DustbinBeanDao.class).clone();
        dustbinBeanDaoConfig.initIdentityScope(type);

        userMessageDaoConfig = daoConfigMap.get(UserMessageDao.class).clone();
        userMessageDaoConfig.initIdentityScope(type);

        commodityAlternativeBeanDaoConfig = daoConfigMap.get(CommodityAlternativeBeanDao.class).clone();
        commodityAlternativeBeanDaoConfig.initIdentityScope(type);

        commodityBeanDaoConfig = daoConfigMap.get(CommodityBeanDao.class).clone();
        commodityBeanDaoConfig.initIdentityScope(type);

        dustbinConfigDaoConfig = daoConfigMap.get(DustbinConfigDao.class).clone();
        dustbinConfigDaoConfig.initIdentityScope(type);

        errorMessageDaoConfig = daoConfigMap.get(ErrorMessageDao.class).clone();
        errorMessageDaoConfig.initIdentityScope(type);

        dustbinStateBeanDaoConfig = daoConfigMap.get(DustbinStateBeanDao.class).clone();
        dustbinStateBeanDaoConfig.initIdentityScope(type);

        errorReportBeanDaoConfig = daoConfigMap.get(ErrorReportBeanDao.class).clone();
        errorReportBeanDaoConfig.initIdentityScope(type);

        deliveryRecordDao = new DeliveryRecordDao(deliveryRecordDaoConfig, this);
        dustbinBeanDao = new DustbinBeanDao(dustbinBeanDaoConfig, this);
        userMessageDao = new UserMessageDao(userMessageDaoConfig, this);
        commodityAlternativeBeanDao = new CommodityAlternativeBeanDao(commodityAlternativeBeanDaoConfig, this);
        commodityBeanDao = new CommodityBeanDao(commodityBeanDaoConfig, this);
        dustbinConfigDao = new DustbinConfigDao(dustbinConfigDaoConfig, this);
        errorMessageDao = new ErrorMessageDao(errorMessageDaoConfig, this);
        dustbinStateBeanDao = new DustbinStateBeanDao(dustbinStateBeanDaoConfig, this);
        errorReportBeanDao = new ErrorReportBeanDao(errorReportBeanDaoConfig, this);

        registerDao(DeliveryRecord.class, deliveryRecordDao);
        registerDao(DustbinBean.class, dustbinBeanDao);
        registerDao(UserMessage.class, userMessageDao);
        registerDao(CommodityAlternativeBean.class, commodityAlternativeBeanDao);
        registerDao(CommodityBean.class, commodityBeanDao);
        registerDao(DustbinConfig.class, dustbinConfigDao);
        registerDao(ErrorMessage.class, errorMessageDao);
        registerDao(DustbinStateBean.class, dustbinStateBeanDao);
        registerDao(ErrorReportBean.class, errorReportBeanDao);
    }
    
    public void clear() {
        deliveryRecordDaoConfig.clearIdentityScope();
        dustbinBeanDaoConfig.clearIdentityScope();
        userMessageDaoConfig.clearIdentityScope();
        commodityAlternativeBeanDaoConfig.clearIdentityScope();
        commodityBeanDaoConfig.clearIdentityScope();
        dustbinConfigDaoConfig.clearIdentityScope();
        errorMessageDaoConfig.clearIdentityScope();
        dustbinStateBeanDaoConfig.clearIdentityScope();
        errorReportBeanDaoConfig.clearIdentityScope();
    }

    public DeliveryRecordDao getDeliveryRecordDao() {
        return deliveryRecordDao;
    }

    public DustbinBeanDao getDustbinBeanDao() {
        return dustbinBeanDao;
    }

    public UserMessageDao getUserMessageDao() {
        return userMessageDao;
    }

    public CommodityAlternativeBeanDao getCommodityAlternativeBeanDao() {
        return commodityAlternativeBeanDao;
    }

    public CommodityBeanDao getCommodityBeanDao() {
        return commodityBeanDao;
    }

    public DustbinConfigDao getDustbinConfigDao() {
        return dustbinConfigDao;
    }

    public ErrorMessageDao getErrorMessageDao() {
        return errorMessageDao;
    }

    public DustbinStateBeanDao getDustbinStateBeanDao() {
        return dustbinStateBeanDao;
    }

    public ErrorReportBeanDao getErrorReportBeanDao() {
        return errorReportBeanDao;
    }

}
