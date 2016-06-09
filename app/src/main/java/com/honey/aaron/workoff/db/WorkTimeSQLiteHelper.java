package com.honey.aaron.workoff.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class WorkTimeSQLiteHelper extends SQLiteOpenHelper {
    private static SQLiteDatabase db;
    private static ContentValues values = new ContentValues();

    public static final String DB_NAME = "work_time";

    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_WEEK = "week";
    /**
     * 일, 몇일
     */
    public static final String COLUMN_DATE = "date";
    /**
     * 요일
     */
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_FROM_TIME = "from_time";
    public static final String COLUMN_TO_TIME = "to_time";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    /**
     * Constructor
     * @param context The context that is called from where
     * @param name Database name
     * @param factory CursorFactory, set null in common
     * @param version Version of database scheme
     */
    public WorkTimeSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + DB_NAME + "(" +
                "_id INTEGER primary key autoincrement, " +
                "year TEXT, " +
                "month TEXT, " +
                "week TEXT, " +
                "date TEXT, " +
                "day TEXT, " +
                "from_time TEXT, " +
                "to_time TEXT, " +
                "timestamp TEXT);";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // db = 적용할 db, old = 구버전, new = 신버전
        Log.d("##########", "db upgrade");
        db.execSQL("drop table if exists " + DB_NAME);
        onCreate(db);
    }

    /**
     * insert data to db
     * @param year 년
     * @param month 월
     * @param week 주
     * @param date 일
     * @param day 요일
     * @param from_time 시작
     * @param timestamp unix timestamp millisecond
     */
    public long insert(String year, String month, String week, String date, String day, String from_time, String timestamp) {
        db = this.getWritableDatabase();
        values.put("year", year);
        values.put("month", month);
        values.put("week", week);
        values.put("date", date);
        values.put("day", day);
        values.put("from_time", from_time);
        values.put("timestamp", timestamp);
        return db.insert(DB_NAME, null, values);
    }

    public long update(String year, String month, String date, String to_time) {
        db = this.getWritableDatabase();
        values.put("to_time", to_time);
        return db.update(DB_NAME, values, "year=? and month=? and date=?", new String[]{year, month, date});
    }

    public long delete(String year, String month, String date) {
        db = this.getWritableDatabase();
        return db.delete(DB_NAME, "year=? and month=? and date=?", new String[]{year, month, date});
    }

    /**
     * select from where values
     * @param year 년
     * @param month 월
     * @param week 주
     * @param date 일
     * @return Cursor : selected list
     */
    public Cursor select(String year, String month, String week, String date) {
        db = this.getReadableDatabase();
        StringBuilder where = new StringBuilder();
        ArrayList<String> values = new ArrayList<>();

        if(year != null && !"".equals(year)) {
            where.append("year=? and ");
            values.add(year);
        }
        if(month != null && !"".equals(month)) {
            where.append("month=? and ");
            values.add(month);
        }
        if(week != null && !"".equals(week)) {
            where.append("week=? and ");
            values.add(week);
        }
        if(date != null && !"".equals(date)) {
            where.append("date=?");
            values.add(date);
        } else {
            if(where.length() > 0) where.replace(where.length() - 4, where.length(), "");
        }

        return db.query(DB_NAME, null, where.toString(), values.toArray(new String[values.size()]), null, null, "_id");
    }

    public Cursor selectAll() {
        db = this.getReadableDatabase();
        return db.query(DB_NAME, null, null, null, null, null, "_id");
    }
}
