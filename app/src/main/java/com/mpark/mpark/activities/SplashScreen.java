package com.mpark.mpark.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mpark.mpark.R;
import com.mpark.mpark.asynctasks.MacAddressAsyncTask;
import com.mpark.mpark.dbOperations.DatabaseHelper;
import com.mpark.mpark.dbOperations.DbHelper;
import com.mpark.mpark.loaders.SQLiteTestDataLoader;
import com.mpark.mpark.pojo.ParkingLot;
import com.mpark.mpark.utilities.AppGlobalData;
import com.mpark.mpark.utilities.TOTP;

import java.net.InetAddress;
import java.util.List;

public class SplashScreen extends Activity{
    FirebaseAuth mAuth;
    FirebaseDatabase fbDb = AppGlobalData.getDatabase();
    DatabaseReference users = fbDb.getReference("users");
    DatabaseReference currentUser;
    DatabaseReference parkings;
    public String activeStatus = "";
    DatabaseHelper mDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();

        //System.out.println("TOTP--->"+TOTP.generateTOTP512("6D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F77","1515587658648","6"));

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null) {
            currentUser = users.child(user.getUid());
            parkings = currentUser.child("parkings");
        }
        new Handler().postDelayed(
                new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                checkOurLittleMacAddressDatabase();
            }
        }, 1000);
    }


    public void redirect(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            parkings.addValueEventListener(dataChangeListener);
        }else{
            Intent i = new Intent(SplashScreen.this,RegisterActivity.class);
            startActivity(i);
            finish();
        }
    }



    ValueEventListener dataChangeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            DataSnapshot postSnapShot=null;
            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                postSnapShot = postSnapshot;
            }
            if(postSnapShot!=null)
            activeStatus = String.valueOf(postSnapShot.child("status").getValue());
            if(activeStatus.equalsIgnoreCase("Active")){
                if(String.valueOf(postSnapShot.child("paid").getValue()).equalsIgnoreCase("NO"))
                {
                    Intent i = new Intent(SplashScreen.this, TimerRunning.class);
                    startActivity(i);
                    finish();
                }else{
                    Intent i = new Intent(SplashScreen.this,StopParkingActivity.class);
                    startActivity(i);
                    finish();
                }
            }else{
                Intent i = new Intent(SplashScreen.this,StartParkingActivity.class);
                startActivity(i);
                finish();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void checkOurLittleMacAddressDatabase()
    {
        if(isInternetAvailable()) {
            new MacAddressAsyncTask(this).execute();
            return;
        }

        DbHelper helper = new DbHelper(this);
        FirebaseUser user;

        SQLiteDatabase database = helper.getWritableDatabase();
        mDataSource = new DatabaseHelper(database);
        List macAddresses = mDataSource.read();
        if(macAddresses.size()==0||macAddresses==null)
        {
            if(!isInternetAvailable()){
                showDialog();
            }else{
                System.out.println("THE MAC ADDRESSES ARE NULL. LOADING FROM WEBSERVICE");
                new MacAddressAsyncTask(this).execute();
            }
            //CHECK IF INTERNET AVAILABLE OTHERWISE SHOW REFRESH BUTTON
        }else {
            System.out.println("THE MAC ADDRESSES ARE NOT NULL. LOADING FROM DATABASE");
            AppGlobalData.parkingsList.clear();
            AppGlobalData.parkingsList = mDataSource.read(null,null,null,null,null);
            System.out.println("Parkings from database are : ");
            for(Object park:AppGlobalData.parkingsList){
                ParkingLot parking = (ParkingLot)park;
                //AppGlobalData.parkingsList.add(parking);
                System.out.println(parking.getMacAddress());
            }
            redirect();

            //getLoaderManager().restartLoader(1,null,this);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(parkings!=null)
        parkings.removeEventListener(dataChangeListener);
    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private void showDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You need an internet connection to connect to our servers")
                .setCancelable(false)
                .setPositiveButton("Connect to WIFI", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNeutralButton("Connect to Data", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}


