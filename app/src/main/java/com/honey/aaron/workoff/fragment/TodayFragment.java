package com.honey.aaron.workoff.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.honey.aaron.workoff.model.WorkDay;
import com.honey.aaron.workoff.util.TimeSharedPreferences;
import com.honey.aaron.workoff.util.TimeUtil;
import com.honey.aaron.workoff.util.Util;
import com.sds.aaron.workoff.R;

import java.util.Calendar;

public class TodayFragment extends BaseFragment {
    public static TodayFragment todayFragment;

    // views
    private LinearLayout fromToLayout;
    private TextView tvTodayDate;
    private TextView tvFromTime;
    private TextView tvToTime;
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
        fromToLayout = (LinearLayout) view.findViewById(R.id.from_to_layout);
        tvTodayDate = (TextView) view.findViewById(R.id.tv_today_date);
        tvFromTime = (TextView) view.findViewById(R.id.tv_from_time);
        tvToTime = (TextView) view.findViewById(R.id.tv_to_time);
        tvTotalTime = (TextView) view.findViewById(R.id.tv_work_time);

        initLayout();
        return view;
    }

    // 오늘 데이터 생성 로직
    public void initLayout() {
        // 오늘 날짜를 가져옴
        Calendar cal = Calendar.getInstance();

        Cursor cursor = sqlHelper.select(String.valueOf(cal.get(Calendar.YEAR)), String.valueOf(cal.get(Calendar.MONTH) + 1),
                String.valueOf(cal.get(Calendar.WEEK_OF_MONTH)), String.valueOf(cal.get(Calendar.DATE)));
        Log.i(TAG, "cursor : " + cursor.getCount());

        if(cursor.getCount() == 0) { // 오늘 데이터가 없을 경우 날짜만 셋팅
            today = new WorkDay();
            today.setYear(String.valueOf(cal.get(Calendar.YEAR)));
            today.setMonth(String.valueOf(cal.get(Calendar.MONTH) + 1));
            today.setWeek(String.valueOf(cal.get(Calendar.WEEK_OF_MONTH)));
            today.setDay(String.valueOf(cal.get(Calendar.DAY_OF_WEEK)));
            today.setDate(String.valueOf(cal.get(Calendar.DATE)));
            today.setFromTime("00:00");
            today.setToTime("00:00");
            today.setTimestamp(TimeUtil.getMillisecondsFromString(today.getYear(), today.getMonth(), today.getDate(), today.getToTime()));
        } else {
            while(cursor.moveToNext()) {
                Log.i(TAG, "cursor not null");
                today = Util.makeTodayInstance(cursor);
            }
        }

        tvTodayDate.setText(today.getYear().concat(".").concat(TimeUtil.getMonth(today.getTimestamp())).concat(".").concat(TimeUtil.getDate(today.getTimestamp())));
        setWorkTime(today);
    }

    private void setWorkTime(final WorkDay workDay) {
        tvTotalTime.setVisibility(View.VISIBLE);
        fromToLayout.setVisibility(View.GONE);

        tvTotalTime.setText(TimeUtil.getTotalWorkTime(workDay.getTimestamp(),
                pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false) ? System.currentTimeMillis() :
                        TimeUtil.getMillisecondsFromString(workDay.getYear(), workDay.getMonth(), workDay.getDate(), workDay.getToTime())));

        tvTotalTime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setFromToWorkTime(workDay);
            }
        });
    }

    private void setFromToWorkTime(final WorkDay workDay) {
        tvTotalTime.setVisibility(View.GONE);
        fromToLayout.setVisibility(View.VISIBLE);
        tvFromTime.setText(workDay.getFromTime());
        tvToTime.setText(pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false) ? TimeUtil.getTime(System.currentTimeMillis()) : workDay.getToTime());

        fromToLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWorkTime(workDay);
            }
        });
    }
}
