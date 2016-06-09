package com.honey.aaron.workoff.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.honey.aaron.workoff.fragment.SettingFragment;
import com.honey.aaron.workoff.fragment.TodayFragment;
import com.honey.aaron.workoff.fragment.WeeklyFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 3;


    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: // Fragment # 0 - This will show TodayFragment
                return TodayFragment.newInstance();
            case 1: // Fragment # 1 - This will show WeeklyFragment
                return WeeklyFragment.newInstance();
            case 2: // Fragment # 2 - This will show SettingFragment
                return SettingFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }
}
