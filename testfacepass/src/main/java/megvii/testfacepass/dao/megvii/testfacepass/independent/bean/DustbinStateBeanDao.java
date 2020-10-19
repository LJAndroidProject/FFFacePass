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
 * DAO for table "DUSTBIN_STATE_BEAN".
*/
public class DustbinStateBeanDao extends AbstractDao<DustbinStateBean, Long> {

    public static final String TABLENAME = "DUSTBIN_STATE_BEAN";

    /**
     * Properties of entity DustbinStateBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property DoorNumber = new Property(1, int.class, "doorNumber", false, "DOOR_NUMBER");
        public final static Property DustbinBoxType = new Property(2, String.class, "dustbinBoxType", false, "DUSTBIN_BOX_TYPE");
        public final static Property DustbinBoxNumber = new Property(3, String.class, "dustbinBoxNumber", false, "DUSTBIN_BOX_NUMBER");
        public final static Property DustbinWeight = new Property(4, double.class, "dustbinWeight", false, "DUSTBIN_WEIGHT");
        public final static Property Temperature = new Property(5, double.class, "temperature", false, "TEMPERATURE");
        public final static Property Humidity = new Property(6, double.class, "humidity", false, "HUMIDITY");
        public final static Property ProximitySwitch = new Property(7, boolean.class, "proximitySwitch", false, "PROXIMITY_SWITCH");
        public final static Property ArtificialDoor = new Property(8, boolean.class, "artificialDoor", false, "ARTIFICIAL_DOOR");
        public final static Property IsFull = new Property(9, boolean.class, "isFull", false, "IS_FULL");
        public final static Property PushRod = new Property(10, boolean.class, "pushRod", false, "PUSH_ROD");
        public final static Property AbnormalCommunication = new Property(11, boolean.class, "abnormalCommunication", false, "ABNORMAL_COMMUNICATION");
        public final static Property DeliverLock = new Property(12, boolean.class, "deliverLock", false, "DELIVER_LOCK");
        public final static Property ArtificialDoorLock = new Property(13, boolean.class, "artificialDoorLock", false, "ARTIFICIAL_DOOR_LOCK");
    }


    public DustbinStateBeanDao(DaoConfig config) {
        super(config);
    }
    
    public DustbinStateBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DUSTBIN_STATE_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"DOOR_NUMBER\" INTEGER NOT NULL UNIQUE ," + // 1: doorNumber
                "\"DUSTBIN_BOX_TYPE\" TEXT," + // 2: dustbinBoxType
                "\"DUSTBIN_BOX_NUMBER\" TEXT," + // 3: dustbinBoxNumber
                "\"DUSTBIN_WEIGHT\" REAL NOT NULL ," + // 4: dustbinWeight
                "\"TEMPERATURE\" REAL NOT NULL ," + // 5: temperature
                "\"HUMIDITY\" REAL NOT NULL ," + // 6: humidity
                "\"PROXIMITY_SWITCH\" INTEGER NOT NULL ," + // 7: proximitySwitch
                "\"ARTIFICIAL_DOOR\" INTEGER NOT NULL ," + // 8: artificialDoor
                "\"IS_FULL\" INTEGER NOT NULL ," + // 9: isFull
                "\"PUSH_ROD\" INTEGER NOT NULL ," + // 10: pushRod
                "\"ABNORMAL_COMMUNICATION\" INTEGER NOT NULL ," + // 11: abnormalCommunication
                "\"DELIVER_LOCK\" INTEGER NOT NULL ," + // 12: deliverLock
                "\"ARTIFICIAL_DOOR_LOCK\" INTEGER NOT NULL );"); // 13: artificialDoorLock
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DUSTBIN_STATE_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DustbinStateBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getDoorNumber());
 
        String dustbinBoxType = entity.getDustbinBoxType();
        if (dustbinBoxType != null) {
            stmt.bindString(3, dustbinBoxType);
        }
 
        String dustbinBoxNumber = entity.getDustbinBoxNumber();
        if (dustbinBoxNumber != null) {
            stmt.bindString(4, dustbinBoxNumber);
        }
        stmt.bindDouble(5, entity.getDustbinWeight());
        stmt.bindDouble(6, entity.getTemperature());
        stmt.bindDouble(7, entity.getHumidity());
        stmt.bindLong(8, entity.getProximitySwitch() ? 1L: 0L);
        stmt.bindLong(9, entity.getArtificialDoor() ? 1L: 0L);
        stmt.bindLong(10, entity.getIsFull() ? 1L: 0L);
        stmt.bindLong(11, entity.getPushRod() ? 1L: 0L);
        stmt.bindLong(12, entity.getAbnormalCommunication() ? 1L: 0L);
        stmt.bindLong(13, entity.getDeliverLock() ? 1L: 0L);
        stmt.bindLong(14, entity.getArtificialDoorLock() ? 1L: 0L);
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DustbinStateBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getDoorNumber());
 
        String dustbinBoxType = entity.getDustbinBoxType();
        if (dustbinBoxType != null) {
            stmt.bindString(3, dustbinBoxType);
        }
 
        String dustbinBoxNumber = entity.getDustbinBoxNumber();
        if (dustbinBoxNumber != null) {
            stmt.bindString(4, dustbinBoxNumber);
        }
        stmt.bindDouble(5, entity.getDustbinWeight());
        stmt.bindDouble(6, entity.getTemperature());
        stmt.bindDouble(7, entity.getHumidity());
        stmt.bindLong(8, entity.getProximitySwitch() ? 1L: 0L);
        stmt.bindLong(9, entity.getArtificialDoor() ? 1L: 0L);
        stmt.bindLong(10, entity.getIsFull() ? 1L: 0L);
        stmt.bindLong(11, entity.getPushRod() ? 1L: 0L);
        stmt.bindLong(12, entity.getAbnormalCommunication() ? 1L: 0L);
        stmt.bindLong(13, entity.getDeliverLock() ? 1L: 0L);
        stmt.bindLong(14, entity.getArtificialDoorLock() ? 1L: 0L);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DustbinStateBean readEntity(Cursor cursor, int offset) {
        DustbinStateBean entity = new DustbinStateBean( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // doorNumber
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // dustbinBoxType
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // dustbinBoxNumber
            cursor.getDouble(offset + 4), // dustbinWeight
            cursor.getDouble(offset + 5), // temperature
            cursor.getDouble(offset + 6), // humidity
            cursor.getShort(offset + 7) != 0, // proximitySwitch
            cursor.getShort(offset + 8) != 0, // artificialDoor
            cursor.getShort(offset + 9) != 0, // isFull
            cursor.getShort(offset + 10) != 0, // pushRod
            cursor.getShort(offset + 11) != 0, // abnormalCommunication
            cursor.getShort(offset + 12) != 0, // deliverLock
            cursor.getShort(offset + 13) != 0 // artificialDoorLock
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DustbinStateBean entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setDoorNumber(cursor.getInt(offset + 1));
        entity.setDustbinBoxType(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setDustbinBoxNumber(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setDustbinWeight(cursor.getDouble(offset + 4));
        entity.setTemperature(cursor.getDouble(offset + 5));
        entity.setHumidity(cursor.getDouble(offset + 6));
        entity.setProximitySwitch(cursor.getShort(offset + 7) != 0);
        entity.setArtificialDoor(cursor.getShort(offset + 8) != 0);
        entity.setIsFull(cursor.getShort(offset + 9) != 0);
        entity.setPushRod(cursor.getShort(offset + 10) != 0);
        entity.setAbnormalCommunication(cursor.getShort(offset + 11) != 0);
        entity.setDeliverLock(cursor.getShort(offset + 12) != 0);
        entity.setArtificialDoorLock(cursor.getShort(offset + 13) != 0);
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DustbinStateBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DustbinStateBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DustbinStateBean entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
