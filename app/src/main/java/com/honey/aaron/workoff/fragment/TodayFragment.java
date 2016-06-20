package com.honey.aaron.workoff.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.honey.aaron.workoff.R;
import com.honey.aaron.workoff.model.WorkDay;
import com.honey.aaron.workoff.util.TimeSharedPreferences;
import com.honey.aaron.workoff.util.TimeUtil;
import com.honey.aaron.workoff.util.Util;
import com.tekle.oss.android.animation.AnimationFactory;

import java.util.Calendar;

public class TodayFragment extends BaseFragment {
    public static TodayFragment todayFragment;

    // views
    private TextView tvTodayDate;
    private ViewAnimator timeViewAnimator;
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
        timeViewAnimator = (ViewAnimator) view.findViewById(R.id.viewFlipper);
        tvFromToTime = (TextView) view.findViewById(R.id.tv_from_to_time);
        tvTotalTime = (TextView) view.findViewById(R.id.tv_today_work_time);

        initLayout();
        return view;
    }

    // 오늘 데이터 생성 로직
    public void initLayout() {
        // 오늘 날짜를 가져옴
        Calendar cal = Calendar.getInstance();

        Cursor cursor = sqlHelper.select(TimeUtil.getYear(cal.getTimeInMillis()), TimeUtil.getMonth(cal.getTimeInMillis()), null, TimeUtil.getDate(cal.getTimeInMillis()));
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
        timeViewAnimator.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AnimationFactory.flipTransition(timeViewAnimator, AnimationFactory.FlipDirection.LEFT_RIGHT);
            }
        });
        tvTotalTime.setText(today.getFromTimestamp() == 0 ? "00:00" : TimeUtil.getTotalWorkTime(today.getFromTimestamp(),
                pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false) ? System.currentTimeMillis() : today.getToTimestamp()));
        tvFromToTime.setText(String.format(getString(R.string.daily_work_time), "".equals(today.getFromTime()) || today.getFromTime() == null ? "00:00" : today.getFromTime(),
                pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false) ? TimeUtil.getTime(System.currentTimeMillis()) :
                        ("".equals(today.getToTime()) || today.getToTime() == null ? "00:00" : today.getToTime())));
    }
}
