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

import com.honey.aaron.workoff.MyApplication;
import com.honey.aaron.workoff.R;
import com.honey.aaron.workoff.adapter.ViewPagerAdapter;
import com.honey.aaron.workoff.db.WorkTimeSQLiteHelper;
import com.honey.aaron.workoff.fragment.TodayFragment;
import com.honey.aaron.workoff.fragment.WeeklyFragment;
import com.honey.aaron.workoff.util.TimeSharedPreferences;
import com.honey.aaron.workoff.util.TimeUtil;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_SETTING_ACTIVITY = 10000;

    private static TimeSharedPreferences pref;
    private static WorkTimeSQLiteHelper sqlHelper;
    private Timer timer;

    ViewPagerAdapter pagerAdapter;
    ViewPager pager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sqlHelper = new WorkTimeSQLiteHelper(MyApplication.getInstance(), WorkTimeSQLiteHelper.DB_NAME, null, 1);
        pref = new TimeSharedPreferences(this);
        timer = new Timer();

        pager = (ViewPager) findViewById(R.id.pager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        if(tabLayout != null) {
            tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_tab_daily_on));
            tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_tab_weekly_off));
            tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_tab_setting_off));
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) tab.setCustomView(R.layout.view_home_tab);
            }
        }

        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0 :
                        tab.setIcon(R.drawable.icon_tab_daily_on);
                        break;
                    case 1 :
                        tab.setIcon(R.drawable.icon_tab_weekly_on);
                        break;
                    case 2 :
                        tab.setIcon(R.drawable.icon_tab_setting_on);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0 :
                        tab.setIcon(R.drawable.icon_tab_daily_off);
                        break;
                    case 1 :
                        tab.setIcon(R.drawable.icon_tab_weekly_off);
                        break;
                    case 2 :
                        tab.setIcon(R.drawable.icon_tab_setting_off);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false)) {
                    Log.d(TAG, "fragment call : " + getSupportFragmentManager().getFragments().size());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    Log.d(TAG, "timer ticking... refresh times...");
                                    ((TodayFragment) pagerAdapter.getItem(0)).initTimeLayout();
                                    ((WeeklyFragment) pagerAdapter.getItem(1)).refreshViews();
                                }
                            });
                        }
                    }).start();
                }
            }
        }, (60 - Calendar.getInstance().get(Calendar.SECOND)) * 1000, 60000);// 정시 분에 시작

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
            sqlHelper = new WorkTimeSQLiteHelper(MyApplication.getInstance(), WorkTimeSQLiteHelper.DB_NAME, null, 1);

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
                    long result = sqlHelper.insert(year, month, week, date, day, time, null, String.valueOf(calendar.getTimeInMillis()), "0");
                    Log.i("#########", "insert result : " + result);
                } else {
                    // 오늘의 데이터가 있는 경우 종료시간을 갱신
                    sqlHelper.update(year, month, date, null, time, null, String.valueOf(calendar.getTimeInMillis()));
                }
                pref.put(TimeSharedPreferences.PREF_IS_WORKING, true);
            } else if(mAction.equals(STOP_CAL_TIME)) {  // STOP 일 경우 종료시간 갱신 후 work time 중지
                sqlHelper.update(year, month, date, null, time, null, String.valueOf(calendar.getTimeInMillis()));
                pref.put(TimeSharedPreferences.PREF_IS_WORKING, false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() called and timer canceled.");
        timer.cancel();
        super.onDestroy();
    }
}
