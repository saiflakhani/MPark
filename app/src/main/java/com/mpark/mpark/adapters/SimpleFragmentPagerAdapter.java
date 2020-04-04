package com.mpark.mpark.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mpark.mpark.fragments.LoginFragment;
import com.mpark.mpark.fragments.RegisterFragment;

/**
 * Created by saif on 2018-01-12.
 */

public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;

    public SimpleFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new RegisterFragment();
        } else if (position == 1){
            return new LoginFragment();
        }
        return new RegisterFragment();
    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return 2;
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return "Register";
            case 1:
                return "Login";
            default:
                return null;
        }
    }

}
