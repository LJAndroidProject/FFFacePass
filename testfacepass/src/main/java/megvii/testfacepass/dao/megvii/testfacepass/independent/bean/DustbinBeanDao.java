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
 * DAO for table "DUSTBIN_BEAN".
*/
public class DustbinBeanDao extends AbstractDao<DustbinBean, Void> {

    public static final String TABLENAME = "DUSTBIN_BEAN";

    /**
     * Properties of entity DustbinBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property DoorNumber = new Property(0, int.class, "doorNumber", false, "DOOR_NUMBER");
        public final static Property DustbinBoxType = new Property(1, String.class, "dustbinBoxType", false, "DUSTBIN_BOX_TYPE");
        public final static Property DustbinEnabled = new Property(2, boolean.class, "dustbinEnabled", false, "DUSTBIN_ENABLED");
        public final static Property DustbinWeight = new Property(3, double.class, "dustbinWeight", false, "DUSTBIN_WEIGHT");
    }


    public DustbinBeanDao(DaoConfig config) {
        super(config);
    }
    
    public DustbinBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DUSTBIN_BEAN\" (" + //
                "\"DOOR_NUMBER\" INTEGER NOT NULL UNIQUE ," + // 0: doorNumber
                "\"DUSTBIN_BOX_TYPE\" TEXT," + // 1: dustbinBoxType
                "\"DUSTBIN_ENABLED\" INTEGER NOT NULL ," + // 2: dustbinEnabled
                "\"DUSTBIN_WEIGHT\" REAL NOT NULL );"); // 3: dustbinWeight
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DUSTBIN_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DustbinBean entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getDoorNumber());
 
        String dustbinBoxType = entity.getDustbinBoxType();
        if (dustbinBoxType != null) {
            stmt.bindString(2, dustbinBoxType);
        }
        stmt.bindLong(3, entity.getDustbinEnabled() ? 1L: 0L);
        stmt.bindDouble(4, entity.getDustbinWeight());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DustbinBean entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getDoorNumber());
 
        String dustbinBoxType = entity.getDustbinBoxType();
        if (dustbinBoxType != null) {
            stmt.bindString(2, dustbinBoxType);
        }
        stmt.bindLong(3, entity.getDustbinEnabled() ? 1L: 0L);
        stmt.bindDouble(4, entity.getDustbinWeight());
    }

    @Override
    public Void readKey(Cursor cursor, int offset) {
        return null;
    }    

    @Override
    public DustbinBean readEntity(Cursor cursor, int offset) {
        DustbinBean entity = new DustbinBean( //
            cursor.getInt(offset + 0), // doorNumber
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // dustbinBoxType
            cursor.getShort(offset + 2) != 0, // dustbinEnabled
            cursor.getDouble(offset + 3) // dustbinWeight
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DustbinBean entity, int offset) {
        entity.setDoorNumber(cursor.getInt(offset + 0));
        entity.setDustbinBoxType(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setDustbinEnabled(cursor.getShort(offset + 2) != 0);
        entity.setDustbinWeight(cursor.getDouble(offset + 3));
     }
    
    @Override
    protected final Void updateKeyAfterInsert(DustbinBean entity, long rowId) {
        // Unsupported or missing PK type
        return null;
    }
    
    @Override
    public Void getKey(DustbinBean entity) {
        return null;
    }

    @Override
    public boolean hasKey(DustbinBean entity) {
        // TODO
        return false;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
