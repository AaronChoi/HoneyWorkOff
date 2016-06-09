package com.honey.aaron.workoff.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.honey.aaron.workoff.activity.MainActivity;
import com.honey.aaron.workoff.db.WorkTimeSQLiteHelper;
import com.honey.aaron.workoff.util.TimeSharedPreferences;


public abstract class BaseFragment extends Fragment {
    public static String TAG;
    public static WorkTimeSQLiteHelper sqlHelper;
    public static TimeSharedPreferences pref;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        pref = ((MainActivity)context).getSharedPreferences();
        sqlHelper = ((MainActivity)context).getSQLHelper();
    }
}
