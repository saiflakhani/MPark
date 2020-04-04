package com.mpark.mpark.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mpark.mpark.R;
import com.mpark.mpark.utilities.AppGlobalData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Ticket extends Activity {
    TextView inTime, outTime, parkerName, cost, hoursSpent, tVDatePark;
    FirebaseAuth mAuth;
    FirebaseDatabase fbDb = AppGlobalData.getDatabase();
    DatabaseReference users = fbDb.getReference("users");
    DatabaseReference parkings;
    public String activeStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);
        inTime = findViewById(R.id.tv_intime);
        outTime = findViewById(R.id.tv_outtime);
        parkerName = findViewById(R.id.parkerName);
        tVDatePark = findViewById(R.id.tvDatePark);
        cost = findViewById(R.id.cost);
        hoursSpent = findViewById(R.id.hoursSpent);
        mAuth = FirebaseAuth.getInstance();
    }


    private void setTextViews() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            parkings.addValueEventListener(listener);
        }
    }


    private ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            DataSnapshot postSnapShot = null;
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                postSnapShot = postSnapshot;
            }
            activeStatus = String.valueOf(postSnapShot.child("status").getValue());
            if (activeStatus.equalsIgnoreCase("Active")) {

            } else {
                try {
                    parkerName.setText(mAuth.getCurrentUser().getDisplayName().split(",")[0].split(" ")[0]);
                }catch (Exception e)
                {
                    parkerName.setText("");
                }
                String startTime = formatTime(String.valueOf(postSnapShot.child("startTime").getValue()));
                String stopTime = formatTime(String.valueOf(postSnapShot.child("endTime").getValue().toString()));
                //long elapsedTime = Long.parseLong(postSnapShot.child("startTime").getValue().toString())-Long.parseLong(postSnapShot.child("endTime").getValue().toString());
                //String elapsed = formatTime(String.valueOf(elapsedTime))

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd MMM yyyy",Locale.ENGLISH);

                Date in_time = formatDate(String.valueOf(postSnapShot.child("startTime").getValue()));
                Date out_time = formatDate(String.valueOf(postSnapShot.child("endTime").getValue()));

                inTime.setText(simpleDateFormat.format(in_time));
                outTime.setText(simpleDateFormat.format(out_time));
                tVDatePark.setText(simpleDateFormat1.format(in_time));
                //hoursSpent.setText(elapsed);
                cost.setText("10");
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    public static String formatTime(String millis1)
    {
        long millis = Long.parseLong(millis1);
        return DateFormat.getDateTimeInstance().format(new Date(millis));
    }

    public static Date formatDate(String millis1)
    {
        long millis = Long.parseLong(millis1);
        return new Date(millis);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        //fbDb.setPersistenceEnabled(true);
        DatabaseReference currentUser = users.child(user.getUid());
        parkings = currentUser.child("parkings");
        setTextViews();
    }

    @Override
    protected void onStop() {
        super.onStop();
        parkings.removeEventListener(listener);
    }
}
