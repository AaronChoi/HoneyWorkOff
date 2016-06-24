package com.honey.aaron.workoff.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.honey.aaron.workoff.MyApplication;
import com.honey.aaron.workoff.db.WorkTimeSQLiteHelper;
import com.honey.aaron.workoff.util.TimeSharedPreferences;


public abstract class BaseFragment extends Fragment {
    public static String TAG;
    public static WorkTimeSQLiteHelper sqlHelper;
    public static TimeSharedPreferences pref;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        pref = new TimeSharedPreferences(MyApplication.getInstance());
        sqlHelper = new WorkTimeSQLiteHelper(MyApplication.getInstance(), WorkTimeSQLiteHelper.DB_NAME, null, 1);
    }
}
