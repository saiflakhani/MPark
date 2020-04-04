package com.mpark.mpark.activities;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mpark.mpark.R;
import com.mpark.mpark.adapters.SimpleFragmentPagerAdapter;

public class RegisterActivity extends FragmentActivity implements ActionBar.TabListener{
    private FirebaseAuth mAuth;
    FirebaseDatabase fbDb = FirebaseDatabase.getInstance();
    DatabaseReference users = fbDb.getReference("users");
    DatabaseReference currentUser;
    DatabaseReference parkings;
    boolean redirect = false;
    EditText phone;
    EditText email, password,name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_login);
        mAuth = FirebaseAuth.getInstance();
        //System.out.println(mAuth);
        //Button register = findViewById(R.id.btnRegister);
        //Button login = findViewById(R.id.login);
        //register.setOnClickListener(registerClickListener);
        //login.setOnClickListener(loginClickListener);
        //phone = findViewById(R.id.eTPhone);
        //email = findViewById(R.id.eTEmail);
        //password = findViewById(R.id.eTPassword);
        //name = findViewById(R.id.eTName);


        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(this, getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }




    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        //Log.d("Current User",currentUser.getDisplayName());
        //SHOULD NOT GET EXECUTED
        if(firebaseUser!=null){
            Intent i = new Intent(RegisterActivity.this,StartParkingActivity.class);
            startActivity(i);
            finish();
        }
    }


    //FOR LOGIN


    //FOR LOGIN





    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }
}
