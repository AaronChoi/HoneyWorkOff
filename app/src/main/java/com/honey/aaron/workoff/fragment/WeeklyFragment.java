package com.honey.aaron.workoff.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.honey.aaron.workoff.adapter.WeeklyWorkTimeListAdapter;
import com.honey.aaron.workoff.model.WorkDay;
import com.honey.aaron.workoff.util.TimeSharedPreferences;
import com.honey.aaron.workoff.util.TimeUtil;
import com.honey.aaron.workoff.util.Util;
import com.sds.aaron.workoff.R;

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
        Cursor cursor = sqlHelper.select(String.valueOf(cal.get(Calendar.YEAR)), String.valueOf(cal.get(Calendar.MONTH) + 1),
                String.valueOf(cal.get(Calendar.WEEK_OF_MONTH)), null);
        while(cursor.moveToNext()) {
            Log.i(TAG, "cursor not null");
            mList.add(Util.makeTodayInstance(cursor));
        }

        mAdapter = new WeeklyWorkTimeListAdapter(getActivity(), mList, pref);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weekly, container, false);

        tvWeeklyPeriod = (TextView) view.findViewById(R.id.tv_weekly_period);
        tvWeeklyWorkTime = (TextView) view.findViewById(R.id.tv_weekly_work_time);
        listWeeklyWork = (ListView) view.findViewById(R.id.list_weekly_work);

        tvWeeklyPeriod.setText(getPeriodString());
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

    private String getPeriodString() {
        StringBuilder period = new StringBuilder();
        cal = Calendar.getInstance();
        // 월요일 날짜 계산..
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        period.append(TimeUtil.getDatePeriodForThisWeek(cal)).append(" ~ ");
        // 일요일 날짜 계산.. calendar 는 일요일부터 시작이므로 월~ 일은 7일을 더해서 다음주 일요일을 계산해야 함
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.add(Calendar.DATE, 7);
        period.append(TimeUtil.getDatePeriodForThisWeek(cal));

        return period.toString();
    }

    private String getWeeklyWorkTime() {
        long totalWorkTime = 0;
        for(WorkDay day : mList) {
            totalWorkTime += (TimeUtil.isToday(day.getTimestamp()) && pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false) ? System.currentTimeMillis() :
                    TimeUtil.getMillisecondsFromString(day.getYear(), day.getMonth(), day.getDate(), day.getToTime())) - day.getTimestamp();
        }

        return TimeUtil.getTotalWorkTime(totalWorkTime);
    }
}
