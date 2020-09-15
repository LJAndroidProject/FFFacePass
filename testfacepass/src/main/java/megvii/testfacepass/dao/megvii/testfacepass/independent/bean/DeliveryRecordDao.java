package megvii.testfacepass.independent.bean;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DELIVERY_RECORD".
*/
public class DeliveryRecordDao extends AbstractDao<DeliveryRecord, Long> {

    public static final String TABLENAME = "DELIVERY_RECORD";

    /**
     * Properties of entity DeliveryRecord.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property DoorNumber = new Property(1, int.class, "doorNumber", false, "DOOR_NUMBER");
        public final static Property UserId = new Property(2, long.class, "userId", false, "USER_ID");
        public final static Property DeliveryTime = new Property(3, long.class, "deliveryTime", false, "DELIVERY_TIME");
        public final static Property Weight = new Property(4, double.class, "weight", false, "WEIGHT");
    }


    public DeliveryRecordDao(DaoConfig config) {
        super(config);
    }
    
    public DeliveryRecordDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DELIVERY_RECORD\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE ," + // 0: id
                "\"DOOR_NUMBER\" INTEGER NOT NULL ," + // 1: doorNumber
                "\"USER_ID\" INTEGER NOT NULL ," + // 2: userId
                "\"DELIVERY_TIME\" INTEGER NOT NULL ," + // 3: deliveryTime
                "\"WEIGHT\" REAL NOT NULL );"); // 4: weight
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DELIVERY_RECORD\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DeliveryRecord entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getDoorNumber());
        stmt.bindLong(3, entity.getUserId());
        stmt.bindLong(4, entity.getDeliveryTime());
        stmt.bindDouble(5, entity.getWeight());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DeliveryRecord entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getDoorNumber());
        stmt.bindLong(3, entity.getUserId());
        stmt.bindLong(4, entity.getDeliveryTime());
        stmt.bindDouble(5, entity.getWeight());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DeliveryRecord readEntity(Cursor cursor, int offset) {
        DeliveryRecord entity = new DeliveryRecord( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // doorNumber
            cursor.getLong(offset + 2), // userId
            cursor.getLong(offset + 3), // deliveryTime
            cursor.getDouble(offset + 4) // weight
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DeliveryRecord entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setDoorNumber(cursor.getInt(offset + 1));
        entity.setUserId(cursor.getLong(offset + 2));
        entity.setDeliveryTime(cursor.getLong(offset + 3));
        entity.setWeight(cursor.getDouble(offset + 4));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DeliveryRecord entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DeliveryRecord entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DeliveryRecord entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}