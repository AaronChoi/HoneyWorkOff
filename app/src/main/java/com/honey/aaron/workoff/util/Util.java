package com.honey.aaron.workoff.util;

import android.database.Cursor;
import android.util.Log;

import com.honey.aaron.workoff.db.WorkTimeSQLiteHelper;
import com.honey.aaron.workoff.model.WorkDay;

public class Util {
    private static final String TAG = Util.class.getSimpleName();

    static WorkDay day;

    public static WorkDay makeTodayInstance(Cursor cursor) {
        day = new WorkDay();
        day.setYear(cursor.getString(cursor.getColumnIndex(WorkTimeSQLiteHelper.COLUMN_YEAR)));
        day.setMonth(cursor.getString(cursor.getColumnIndex(WorkTimeSQLiteHelper.COLUMN_MONTH)));
        day.setWeek(cursor.getString(cursor.getColumnIndex(WorkTimeSQLiteHelper.COLUMN_WEEK)));
        day.setDate(cursor.getString(cursor.getColumnIndex(WorkTimeSQLiteHelper.COLUMN_DATE)));
        day.setDay(cursor.getString(cursor.getColumnIndex(WorkTimeSQLiteHelper.COLUMN_DAY)));
        day.setFromTime(cursor.getString(cursor.getColumnIndex(WorkTimeSQLiteHelper.COLUMN_FROM_TIME)));
        day.setToTime(cursor.getString(cursor.getColumnIndex(WorkTimeSQLiteHelper.COLUMN_TO_TIME)));
        day.setTimestamp(Long.parseLong(cursor.getString(cursor.getColumnIndex(WorkTimeSQLiteHelper.COLUMN_TIMESTAMP))));
        Log.i(TAG, "year : " + day.getYear() +
                "month : " + day.getMonth() +
                "week : " + day.getWeek() +
                "date : " + day.getDate() +
                "day : " + day.getDay() +
                "from_time : " + day.getFromTime() +
                "to_time : " + day.getToTime() +
                "timestamp : " + day.getTimestamp());

        return day;
    }
}