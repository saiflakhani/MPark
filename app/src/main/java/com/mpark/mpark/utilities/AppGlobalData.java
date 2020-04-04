package com.mpark.mpark.utilities;

import com.google.firebase.database.FirebaseDatabase;
import com.mpark.mpark.pojo.ParkingLot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by saif on 2018-01-10.
 */

public class AppGlobalData {

    public static UUID currentUUID;
    public static List<ParkingLot> parkingsList = new ArrayList<ParkingLot>();
    private static FirebaseDatabase mDatabase;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }
}
