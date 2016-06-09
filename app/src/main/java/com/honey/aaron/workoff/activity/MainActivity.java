package com.honey.aaron.workoff.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.honey.aaron.workoff.R;
import com.honey.aaron.workoff.adapter.ViewPagerAdapter;
import com.honey.aaron.workoff.db.WorkTimeSQLiteHelper;
import com.honey.aaron.workoff.util.TimeSharedPreferences;
import com.honey.aaron.workoff.util.TimeUtil;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_SETTING_ACTIVITY = 10000;

    private static TimeSharedPreferences pref;
    private static WorkTimeSQLiteHelper sqlHelper;

    ViewPagerAdapter pagerAdapter;
    ViewPager pager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sqlHelper = new WorkTimeSQLiteHelper(this, WorkTimeSQLiteHelper.DB_NAME, null, 1);
        pref = new TimeSharedPreferences(this);

        pager = (ViewPager) findViewById(R.id.pager);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setIcon(android.R.drawable.arrow_down_float));
        tabLayout.addTab(tabLayout.newTab().setIcon(android.R.drawable.arrow_down_float));
        tabLayout.addTab(tabLayout.newTab().setIcon(android.R.drawable.arrow_down_float));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (!isContainedInNotificationListeners(getApplicationContext())) {
            makeDialogPopup();
        }
    }

    public void makeDialogPopup() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivityForResult(intent, REQUEST_SETTING_ACTIVITY);
                dialog.dismiss();
            }
        });
        alert.setTitle("알림");
        alert.setMessage("Notification 에 접근하기 위하여 알림 접근 권한을 설정해 주시기 바랍니다.\n확인을 누르면 설정화면으로 이동합니다. ");
        alert.setCancelable(false);
        alert.show();
    }

    public static boolean isContainedInNotificationListeners(Context $context) {
        String enabledListeners = Settings.Secure.getString($context.getContentResolver(), "enabled_notification_listeners");
        return !TextUtils.isEmpty(enabledListeners) && enabledListeners.contains($context.getPackageName());
    }

    public TimeSharedPreferences getSharedPreferences() {
        return pref;
    }

    public WorkTimeSQLiteHelper getSQLHelper() {
        return sqlHelper;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_SETTING_ACTIVITY) {
            if (!isContainedInNotificationListeners(getApplicationContext())) {
                makeDialogPopup();
            }
        }
    }

    public static class BroadcastReceiverForWorkTime extends BroadcastReceiver {
        public static final String START_CAL_TIME = "com.honey.aaron.workoff.START_CAL_TIME";
        public static final String STOP_CAL_TIME = "com.honey.aaron.workoff.STOP_CAL_TIME";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("#########", "onReceive() called!");
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

            /**
             * sql database 입력관련
             */
            // START 일 경우 work time 시작
            if(mAction.equals(START_CAL_TIME)) {
                Cursor cursor = sqlHelper.select(year, month, null, date);
                if(cursor == null || cursor.getCount() == 0) {  // 오늘의 데이터가 없으면 시작시간으로 생성
                    long result = sqlHelper.insert(year, month, week, date, day, time, String.valueOf(calendar.getTimeInMillis()));
                    Log.i("#########", "insert result : " + result);
                } else {
                    // 오늘의 데이터가 있는 경우 종료시간을 갱신
                    sqlHelper.update(year, month, date, time);
                }
                pref.put(TimeSharedPreferences.PREF_IS_WORKING, true);
            } else if(mAction.equals(STOP_CAL_TIME)) {  // STOP 일 경우 work time 중지
                sqlHelper.update(year, month, date, time);
                pref.put(TimeSharedPreferences.PREF_IS_WORKING, false);
            }
        }
    }
}
