package com.honey.aaron.workoff.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.honey.aaron.workoff.fragment.TodayFragment;
import com.honey.aaron.workoff.fragment.WeeklyFragment;
import com.honey.aaron.workoff.service.NotificationServiceForWorkTime;
import com.honey.aaron.workoff.util.TimeSharedPreferences;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_SETTING_ACTIVITY = 10000;

    private TimeSharedPreferences pref;
    private Timer timer;

    ViewPagerAdapter pagerAdapter;
    ViewPager pager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = new TimeSharedPreferences(MyApplication.getInstance());
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
                updateFragmentViewForWorkTime();
            }
        }, (60 - Calendar.getInstance().get(Calendar.SECOND)) * 1000, 60000);// 정시 분에 시작

        if (!isContainedInNotificationListeners(getApplicationContext())) {
            makeDialogPopup();
        }
    }

    private void updateFragmentViewForWorkTime() {
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

    // 최초 실행 시 업무중이면 활성화..
    public void checkIsWorkingWhenObservedPackageEnabled() {
        if(!pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false)) {
            Intent intent = new Intent(this, NotificationServiceForWorkTime.class);
            intent.putExtra("call_from", "MainActivity");
            startService(intent);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_SETTING_ACTIVITY) {
            if (!isContainedInNotificationListeners(getApplicationContext())) {
                makeDialogPopup();
            } else {
                checkIsWorkingWhenObservedPackageEnabled();
            }
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() called and update fragment's work time.");
        updateFragmentViewForWorkTime();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() called and cancel timer.");
        timer.cancel();
        super.onDestroy();
    }
}
