package com.mpark.mpark.dbOperations;

/**
 * Created by saif on 2018-01-15.
 */

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mpark.mpark.pojo.ParkingLot;


public class DatabaseHelper extends DataSource {
    public static final String TABLE_NAME = "test";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_MAC = "mac_addr";
    // Database creation sql statement
    public static final String CREATE_COMMAND = "CREATE TABLE test(id integer primary key, name text not null, mac_addr text not null);";
    public DatabaseHelper(SQLiteDatabase database) {
        super(database);
    }
    @Override
    public boolean insert(ParkingLot entity) {
        if (entity == null) {
            return false;
        }
        long result = mDatabase.insert(TABLE_NAME, null,
                generateContentValuesFromObject(entity));
        return result != -1;
    }
    @Override
    public boolean delete(ParkingLot entity) {
        if (entity == null) {
            return false;
        }
        int result = mDatabase.delete(TABLE_NAME,
                COLUMN_ID + " = " + entity.getId(), null);
        return result != 0;
    }
    @Override
    public boolean update(ParkingLot entity) {
        if (entity == null) {
            return false;
        }
        int result = mDatabase.update(TABLE_NAME,
                generateContentValuesFromObject(entity), COLUMN_ID + " = "
                        + entity.getId(), null);
        return result != 0;
    }
    @Override
    public List read() {
        Cursor cursor = mDatabase.query(TABLE_NAME, getAllColumns(), null,
                null, null, null, null);
        List tests = new ArrayList();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                tests.add(generateObjectFromCursor(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return tests;
    }
    @Override
    public List read(String selection, String[] selectionArgs,
                     String groupBy, String having, String orderBy) {
        Cursor cursor = mDatabase.query(TABLE_NAME, getAllColumns(), selection, selectionArgs, groupBy, having, orderBy);
        List tests = new ArrayList();
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                tests.add(generateObjectFromCursor(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return tests;
    }
    public String[] getAllColumns() {
        return new String[] { COLUMN_ID, COLUMN_NAME, COLUMN_MAC };
    }
    public ParkingLot generateObjectFromCursor(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        ParkingLot test = new ParkingLot();
        test.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex(COLUMN_ID))));
        test.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
        test.setMacAddress(cursor.getString(cursor.getColumnIndex(COLUMN_MAC)));
        return test;
    }
    public ContentValues generateContentValuesFromObject(ParkingLot entity) {
        if (entity == null) {
            return null;
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID,entity.getId());
        values.put(COLUMN_NAME, entity.getName());
        values.put(COLUMN_MAC,entity.getMacAddress());
        return values;
    }
}