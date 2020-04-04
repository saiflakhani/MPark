package com.mpark.mpark.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mpark.mpark.R;
import com.mpark.mpark.utilities.AppGlobalData;

import java.sql.Time;

import static android.content.ContentValues.TAG;

public class TimerRunning extends Activity {

    Chronometer chronometer;
    FirebaseAuth mAuth;
    FirebaseDatabase fbDb = AppGlobalData.getDatabase();
    DatabaseReference users = fbDb.getReference("users");
    DatabaseReference parkings;
    public String activeStatus = "";
    String startTime;
    String keyofCurrentParking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_running);
        chronometer = findViewById(R.id.chronometer);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference currentUser = users.child(user.getUid());
        parkings = currentUser.child("parkings");
        parkings.addValueEventListener(dataListener);
    }

    private ValueEventListener dataListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            DataSnapshot postSnapShot=null;
            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                postSnapShot = postSnapshot;
            }
            activeStatus = String.valueOf(postSnapShot.child("status").getValue());
            startTime = String.valueOf(postSnapShot.child("startTime").getValue());
            keyofCurrentParking = postSnapShot.getKey();
            if(keyofCurrentParking!=null){
                Button btnStopParking = findViewById(R.id.btnPay);
                btnStopParking.setOnClickListener(payListener);
            }
            System.out.println("Start Time  " + startTime);
            startTimer();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private View.OnClickListener payListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //TODO Add Payment gateway here.
            parkings.child(keyofCurrentParking).child("endTime").setValue(System.currentTimeMillis());
            parkings.child(keyofCurrentParking).child("paid").setValue("YES").addOnSuccessListener(TimerRunning.this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            }).addOnCompleteListener(TimerRunning.this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG,"Data pushed to server.");
                }
            });
            parkings.child(keyofCurrentParking).child("paid").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Intent i = new Intent(TimerRunning.this,StopParkingActivity.class);
                    chronometer.stop();
                    startActivity(i);
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    };

    private void startTimer() {
        if (activeStatus.equalsIgnoreCase("Active")&&!startTime.equals("null")) {
            long time = Long.parseLong(startTime);
            long elapsed = System.currentTimeMillis()-time;
            System.out.println(elapsed);
            chronometer.setBase(SystemClock.elapsedRealtime() - elapsed);
            chronometer.start();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        parkings.removeEventListener(dataListener);
    }
}

