package com.honey.aaron.workoff.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.honey.aaron.workoff.R;
import com.honey.aaron.workoff.adapter.WeeklyWorkTimeListAdapter;
import com.honey.aaron.workoff.model.WorkDay;
import com.honey.aaron.workoff.util.TimeSharedPreferences;
import com.honey.aaron.workoff.util.TimeUtil;
import com.honey.aaron.workoff.util.Util;

import java.util.ArrayList;
import java.util.Calendar;

public class WeeklyFragment extends BaseFragment {
    static WeeklyFragment weeklyFragment;

    // list of the work day
    public ArrayList<WorkDay> mList;
    WeeklyWorkTimeListAdapter mAdapter;
    Calendar cal;

    // view
    TextView tvWeeklyPeriod;
    TextView tvWeeklyWorkTime;
    ListView listWeeklyWork;


    public WeeklyFragment() {
        super();
    }

    public static WeeklyFragment newInstance() {
        if (weeklyFragment == null) {
            weeklyFragment = new WeeklyFragment();
        }
        return weeklyFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = WeeklyFragment.class.getSimpleName();
        mList = new ArrayList<>();

        cal = Calendar.getInstance();
        Cursor cursor = sqlHelper.select(TimeUtil.getYear(cal.getTimeInMillis()), TimeUtil.getMonth(cal.getTimeInMillis()), TimeUtil.getWeek(cal.getTimeInMillis()), null);
        while(cursor.moveToNext()) {
            Log.i(TAG, "cursor not null");
            mList.add(Util.makeTodayInstance(cursor));
        }
        makeEmptyDataAtDayOff();
        mAdapter = new WeeklyWorkTimeListAdapter(getActivity(), mList, pref);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weekly, container, false);

        tvWeeklyPeriod = (TextView) view.findViewById(R.id.tv_weekly_period);
        tvWeeklyWorkTime = (TextView) view.findViewById(R.id.tv_weekly_work_time);
        listWeeklyWork = (ListView) view.findViewById(R.id.list_weekly_work);

        tvWeeklyPeriod.setText(TimeUtil.getDatePeriodForThisWeek());
        tvWeeklyWorkTime.setText(getWeeklyWorkTime());

        listWeeklyWork.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                // TODO 다이얼로그 팝업으로 시간 조정가능, 휴가 체크
                return false;
            }
        });
        listWeeklyWork.setAdapter(mAdapter);

        return view;
    }

    private String getWeeklyWorkTime() {
        long totalWorkTime = 0;
        for(WorkDay day : mList) {
            if(day.getTimestamp() == 0) continue;
            totalWorkTime += (TimeUtil.isToday(day.getTimestamp()) && pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false) ? System.currentTimeMillis() :
                    TimeUtil.getMillisecondsFromString(day.getYear(), day.getMonth(), day.getDate(), day.getToTime())) - day.getTimestamp();
        }

        return TimeUtil.getTotalWorkTime(totalWorkTime);
    }

    private void makeEmptyDataAtDayOff() {
        if(mList.size() == 7) return;

        cal = Calendar.getInstance();
        for(int i = 0 ; i < 7 ; i++) {
            // 토요일 부터 데이터가 있는지 확인
            if(i < mList.size()) {
                cal.setTimeInMillis(mList.get(i).getTimestamp());
                if (cal.get(Calendar.DAY_OF_WEEK) != 7 - i) {
                    cal.set(Calendar.DAY_OF_WEEK, 7 - i);
                    mList.add(i, Util.makEmptyWorkDay(cal));
                    Log.d(TAG, "size : " + mList.size());
                }
            } else {
                cal.set(Calendar.DAY_OF_WEEK, 7 - i);
                mList.add(i, Util.makEmptyWorkDay(cal));
                Log.d(TAG, "size : " + mList.size());
            }
        }
    }
}
