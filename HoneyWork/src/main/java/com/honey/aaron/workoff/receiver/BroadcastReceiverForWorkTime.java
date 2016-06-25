package com.honey.aaron.workoff.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.honey.aaron.workoff.MyApplication;
import com.honey.aaron.workoff.db.WorkTimeSQLiteHelper;
import com.honey.aaron.workoff.util.TimeSharedPreferences;
import com.honey.aaron.workoff.util.TimeUtil;

import java.util.Calendar;

public class BroadcastReceiverForWorkTime extends BroadcastReceiver {
    public static final String TAG = BroadcastReceiverForWorkTime.class.getSimpleName();

    public static final String START_CAL_TIME = "com.honey.aaron.workoff.START_CAL_TIME";
    public static final String STOP_CAL_TIME = "com.honey.aaron.workoff.STOP_CAL_TIME";

    public static WorkTimeSQLiteHelper sqlHelper;
    public static TimeSharedPreferences pref;

    long result;

    public BroadcastReceiverForWorkTime() {
        super();

        if(pref == null) pref = new TimeSharedPreferences(MyApplication.getInstance());
        if(sqlHelper == null) sqlHelper = new WorkTimeSQLiteHelper(MyApplication.getInstance(), WorkTimeSQLiteHelper.DB_NAME, null, 1);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive() called!");
        String mAction = intent.getAction();
        Calendar calendar = Calendar.getInstance();

        // 오전 6시 이전일 경우 어제의 업무 시간으로 귀속
        if(calendar.get(Calendar.HOUR_OF_DAY) < 6) {
            calendar.add(Calendar.DATE, -1);
        }

        String year = TimeUtil.getYear(calendar.getTimeInMillis());
        String month = TimeUtil.getMonth(calendar.getTimeInMillis());
        String week = TimeUtil.getWeek(calendar.getTimeInMillis());
        String date = TimeUtil.getDate(calendar.getTimeInMillis());
        String day = TimeUtil.getDay(calendar.getTimeInMillis());
        String time = TimeUtil.getTime(calendar.getTimeInMillis());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        /**
         * sql database 입력관련
         */
        // START 일 경우 work time 시작
        if(mAction.equals(START_CAL_TIME)) {
            Cursor cursor = sqlHelper.select(year, month, null, date);
            if(cursor == null || cursor.getCount() == 0) {  // 오늘의 데이터가 없으면 시작시간으로 생성
                result = sqlHelper.insert(year, month, week, date, day, time, null, String.valueOf(calendar.getTimeInMillis()), "0");
                Log.i(TAG, "insert result : " + result);
            } else {
                // 오늘의 데이터가 있는 경우 종료시간을 갱신
                sqlHelper.update(year, month, date, null, time, null, String.valueOf(calendar.getTimeInMillis()));
            }
            pref.put(TimeSharedPreferences.PREF_IS_WORKING, true);
        } else if(mAction.equals(STOP_CAL_TIME)) {  // STOP 일 경우 종료시간 갱신 후 work time 중지
            result = sqlHelper.update(year, month, date, null, time, null, String.valueOf(calendar.getTimeInMillis()));
            Log.i(TAG, "update result : " + result);
            pref.put(TimeSharedPreferences.PREF_IS_WORKING, false);
        }
    }
}
