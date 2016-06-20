package com.honey.aaron.workoff.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public static final String COLUMN_FROM_TIMESTAMP = "from_timestamp";
    public static final String COLUMN_TO_TIMESTAMP = "to_timestamp";

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
        String sql = "create table if not exists " + DB_NAME + "(" +
                "_id INTEGER primary key autoincrement, " +
                COLUMN_YEAR + " TEXT, " +
                COLUMN_MONTH + " TEXT, " +
                COLUMN_WEEK + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_DAY + " TEXT, " +
                COLUMN_FROM_TIME + " TEXT, " +
                COLUMN_TO_TIME + " TEXT, " +
                COLUMN_FROM_TIMESTAMP + " TEXT," +
                COLUMN_TO_TIMESTAMP + " TEXT);";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // db = 적용할 db, old = 구버전, new = 신버전
        Log.d("##########", "db upgrade");
        if(newVersion > oldVersion) {
            // column 항목 가져오기
            List<String> columns = GetColumns(db, DB_NAME);
            // 테이블 이름 변경
            db.execSQL("ALTER table " + DB_NAME + " RENAME TO temp_" + DB_NAME);
            // 같은 이름으로 테이블 생성
            onCreate(db);
            // 신규 컬럼에 없는 항목 제거
            List<String> newColumns = GetColumns(db, DB_NAME);
            columns.retainAll(newColumns);
//            for(String newColumn : newColumns) {
//                if(!columns.contains(newColumn)) {
//                    columns.add(newColumn);
//                }
//            }
            Log.d("############", "columns : " + columns.size());
            // 컬럼을 구분자를 통해 모두 연결
            String cols = join(columns, ",");
            Log.d("############", "cols : " + cols);
            // 백업 테이블에서 모든 데이터를 긁어와서 입력
            db.execSQL(String.format("INSERT INTO %s (%s) SELECT %s from temp_%s", DB_NAME, cols, cols, DB_NAME));
            // 백업 테이블 제거
            db.execSQL("drop table if exists temp_" + DB_NAME);
        }
    }

    /**
     * insert data to db
     * @param year 년
     * @param month 월
     * @param week 주
     * @param date 일
     * @param day 요일
     * @param from_time 시작
     * @param from_timestamp unix timestamp millisecond
     */
    public long insert(String year, String month, String week, String date, String day, String from_time, String to_time, String from_timestamp, String to_timestamp) {
        if(from_time == null && to_time == null) return 0;

        db = this.getWritableDatabase();
        values.clear();
        values.put(COLUMN_YEAR, year);
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_WEEK, week);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DAY, day);
        if(from_time != null) values.put(COLUMN_FROM_TIME, from_time);
        if(to_time != null) values.put(COLUMN_TO_TIME, to_time);
        values.put(COLUMN_FROM_TIMESTAMP, from_timestamp);
        values.put(COLUMN_TO_TIMESTAMP, to_timestamp);
        return db.insert(DB_NAME, null, values);
    }

    public long update(String year, String month, String date, String from_time, String to_time, String from_timestamps, String to_timestamps) {
        if(from_time == null && to_time == null) return 0;

        db = this.getWritableDatabase();
        values.clear();
        if(from_time != null) values.put(COLUMN_FROM_TIME, from_time);
        if(to_time != null) values.put(COLUMN_TO_TIME, to_time);
        if(from_timestamps != null) values.put(COLUMN_FROM_TIMESTAMP, from_timestamps);
        if(to_timestamps != null) values.put(COLUMN_TO_TIMESTAMP, to_timestamps);
        return db.update(DB_NAME, values, COLUMN_YEAR + "=? and " + COLUMN_MONTH + "=? and " + COLUMN_DATE + "=?", new String[]{year, month, date});
    }

    public long delete(String year, String month, String date) {
        db = this.getWritableDatabase();
        return db.delete(DB_NAME, COLUMN_YEAR + "=? and " + COLUMN_MONTH + "=? and " + COLUMN_DATE + "=?", new String[]{year, month, date});
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
            where.append(COLUMN_YEAR);
            where.append("=? and ");
            values.add(year);
        }
        if(month != null && !"".equals(month)) {
            where.append(COLUMN_MONTH);
            where.append("=? and ");
            values.add(month);
        }
        if(week != null && !"".equals(week)) {
            where.append(COLUMN_WEEK);
            where.append("=? and ");
            values.add(week);
        }
        if(date != null && !"".equals(date)) {
            where.append(COLUMN_DATE);
            where.append("=?");
            values.add(date);
        } else {
            if(where.length() > 0) where.replace(where.length() - 4, where.length(), "");
        }

        return db.query(DB_NAME, null, where.toString(), values.toArray(new String[values.size()]), null, null, "_id DESC");
    }

    public Cursor selectAll() {
        db = this.getReadableDatabase();
        return db.query(DB_NAME, null, null, null, null, null, null);
    }


    /**
     * These are for DB upgrade
     */
    public static List<String> GetColumns(SQLiteDatabase db, String tableName) {
        List<String> ar = null;
        Cursor c = null;
        try {
            c = db.rawQuery("select * from " + tableName + " limit 1", null);
            if (c != null) {
                ar = new ArrayList<>(Arrays.asList(c.getColumnNames()));
            }
        } catch (Exception e) {
            Log.v(tableName, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
        }
        return ar;
    }

    public static String join(List<String> list, String delim) {
        StringBuilder buf = new StringBuilder();
        int num = list.size();
        for (int i = 0; i < num; i++) {
            if (i != 0)
                buf.append(delim);
            buf.append(list.get(i));
        }
        return buf.toString();
    }
}
