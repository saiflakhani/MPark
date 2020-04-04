package com.mpark.mpark.loaders;

/**
 * Created by saif on 2018-01-15.
 */

import java.util.List;

import android.content.Context;

import com.mpark.mpark.dbOperations.DataSource;
import com.mpark.mpark.pojo.ParkingLot;

public class SQLiteTestDataLoader extends AbstractDataLoader {
    private DataSource mDataSource;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mGroupBy;
    private String mHaving;
    private String mOrderBy;

    public SQLiteTestDataLoader(Context context, DataSource dataSource, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        super(context);
        mDataSource = dataSource;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mGroupBy = groupBy;
        mHaving = having;
        mOrderBy = orderBy;
    }

    protected List buildList() {
        List testList = mDataSource.read(mSelection, mSelectionArgs, mGroupBy, mHaving,	mOrderBy);
        return testList;
    }
    public void insert(ParkingLot entity) {
        new InsertTask(this).execute(entity);
    }
    public void update(ParkingLot entity) {
        new UpdateTask(this).execute(entity);
    }
    public void delete(ParkingLot entity) {
        new DeleteTask(this).execute(entity);
    }
    private class InsertTask extends ContentChangingTask<ParkingLot, Void, Void> {
        InsertTask(SQLiteTestDataLoader loader) {
            super(loader);
        }
        @Override
        protected Void doInBackground(ParkingLot... params) {
            mDataSource.insert(params[0]);
            return (null);
        }
    }
    private class UpdateTask extends ContentChangingTask<ParkingLot, Void, Void> {
        UpdateTask(SQLiteTestDataLoader loader) {
            super(loader);
        }

        @Override
        protected Void doInBackground(ParkingLot... params) {
            mDataSource.update(params[0]);
            return (null);
        }
    }
    private class DeleteTask extends ContentChangingTask<ParkingLot, Void, Void> {
        DeleteTask(SQLiteTestDataLoader loader) {
            super(loader);
        }
        @Override
        protected Void doInBackground(ParkingLot... params) {
            mDataSource.delete(params[0]);
            return (null);
        }
    }
}