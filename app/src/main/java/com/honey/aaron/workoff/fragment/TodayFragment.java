package com.honey.aaron.workoff.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.honey.aaron.workoff.R;
import com.honey.aaron.workoff.model.WorkDay;
import com.honey.aaron.workoff.util.TimeSharedPreferences;
import com.honey.aaron.workoff.util.TimeUtil;
import com.honey.aaron.workoff.util.Util;

import java.util.Calendar;

public class TodayFragment extends BaseFragment {
    public static TodayFragment todayFragment;

    // views
    private TextView tvTodayDate;
    private TextView tvFromToTime;
    private TextView tvTotalTime;

    WorkDay today;

    public TodayFragment() {
        super();
        TAG = TodayFragment.class.getSimpleName();
    }

    public static TodayFragment newInstance() {
        if (todayFragment == null) {
            todayFragment = new TodayFragment();
        }
        return todayFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);
        tvTodayDate = (TextView) view.findViewById(R.id.tv_today_date);
        tvFromToTime = (TextView) view.findViewById(R.id.tv_from_to_time);
        tvTotalTime = (TextView) view.findViewById(R.id.tv_today_work_time);

        initLayout();
        return view;
    }

    // 오늘 데이터 생성 로직
    public void initLayout() {
        // 오늘 날짜를 가져옴
        Calendar cal = Calendar.getInstance();

        Cursor cursor = sqlHelper.select(TimeUtil.getYear(cal.getTimeInMillis()), TimeUtil.getMonth(cal.getTimeInMillis()), TimeUtil.getWeek(cal.getTimeInMillis()), TimeUtil.getDate(cal.getTimeInMillis()));
        Log.i(TAG, "cursor : " + cursor.getCount());

        if(cursor.getCount() == 0) { // 오늘 데이터가 없을 경우 빈값을 생성
            today = Util.makEmptyWorkDay(cal);
        } else {
            while(cursor.moveToNext()) {
                Log.i(TAG, "cursor not null");
                today = Util.makeTodayInstance(cursor);
            }
        }

        tvTodayDate.setText(TimeUtil.getDisplayDateFormat(cal));
        setWorkTime(today);
    }

    private void setWorkTime(final WorkDay workDay) {
        tvTotalTime.setVisibility(View.VISIBLE);
        tvFromToTime.setVisibility(View.GONE);

        tvTotalTime.setText(TimeUtil.getTotalWorkTime(workDay.getTimestamp(),
                pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false) ? System.currentTimeMillis() :
                        TimeUtil.getMillisecondsFromString(workDay.getYear(), workDay.getMonth(), workDay.getDate(), "".equals(workDay.getToTime()) || workDay.getToTime() == null ? "00:00" : workDay.getToTime())));

        tvTotalTime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setFromToWorkTime(workDay);
            }
        });
    }

    private void setFromToWorkTime(final WorkDay workDay) {
        tvTotalTime.setVisibility(View.GONE);
        tvFromToTime.setVisibility(View.VISIBLE);
        tvFromToTime.setText(String.format(getString(R.string.daily_work_time), "".equals(workDay.getFromTime()) || workDay.getFromTime() == null ? "00:00" : workDay.getFromTime(),
                pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false) ? TimeUtil.getTime(System.currentTimeMillis()) :
                        ("".equals(workDay.getToTime()) || workDay.getToTime() == null ? "00:00" : workDay.getToTime())));

        tvFromToTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWorkTime(workDay);
            }
        });
    }
}
