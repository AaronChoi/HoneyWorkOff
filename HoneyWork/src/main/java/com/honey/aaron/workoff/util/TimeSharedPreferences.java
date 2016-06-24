package com.honey.aaron.workoff.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class TimeSharedPreferences {
    private static final String PREF_NAME = "com.honey.aaron.workoff.pref";

    public final static String PREF_IS_WORKING = "PREF_IS_WORKING";

    private final Context mContext;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public TimeSharedPreferences(Context ctx) {
        this.mContext = ctx;
    }

    public void put(String key, String value) {
        pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void put(String key, boolean value) {
        pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        editor = pref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void put(String key, long value) {
        pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        editor = pref.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public String getValue(String key, String defValue) {
        pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);

        try {
            return pref.getString(key, defValue);
        } catch (Exception e) {
            return defValue;
        }
    }

    public boolean getValue(String key, boolean defValue) {
        pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);

        try {
            return pref.getBoolean(key, defValue);
        } catch (Exception e) {
            return defValue;
        }
    }

    public long getValue(String key, long defValue) {
        pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);

        try {
            return pref.getLong(key, defValue);
        } catch (Exception e) {
            return defValue;
        }
    }
}
