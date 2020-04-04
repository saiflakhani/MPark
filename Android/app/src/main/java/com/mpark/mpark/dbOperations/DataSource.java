package com.mpark.mpark.dbOperations;

/**
 * Created by saif on 2018-01-15.
 */

        import java.util.List;

        import android.database.sqlite.SQLiteDatabase;

        import com.mpark.mpark.pojo.ParkingLot;

public abstract class DataSource {
    protected SQLiteDatabase mDatabase;
    public DataSource(SQLiteDatabase database) {
        mDatabase = database;
    }
    public abstract boolean insert(ParkingLot entity);
    public abstract boolean delete(ParkingLot entity);
    public abstract boolean update(ParkingLot entity);
    public abstract List read();
    public abstract List read(String selection, String[] selectionArgs,
                              String groupBy, String having, String orderBy);
}