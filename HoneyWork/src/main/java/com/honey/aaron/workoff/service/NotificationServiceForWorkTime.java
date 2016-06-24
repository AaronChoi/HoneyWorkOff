package com.honey.aaron.workoff.service;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.honey.aaron.workoff.receiver.BroadcastReceiverForWorkTime;

import java.util.ArrayList;
import java.util.List;

public class NotificationServiceForWorkTime extends NotificationListenerService {
    private static final String TAG = NotificationServiceForWorkTime.class.getSimpleName();
    private static final String EMM_PACKAGE_NAME = "com.sds.emm.client";
    private static final String MDM_PACKAGE_NAME = "com.sds.mobile.mdm.client";

    private static List<String> OBSERVE_PACKAGE = new ArrayList<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!TextUtils.equals(intent.getStringExtra("call_from"), "MainActivity")) {
            return super.onStartCommand(intent, flags, startId);
        }

        Log.d(TAG, "startCommand() method start... ");
        OBSERVE_PACKAGE.add(EMM_PACKAGE_NAME);
        OBSERVE_PACKAGE.add(MDM_PACKAGE_NAME);

        for(StatusBarNotification sbn : getActiveNotifications()){
            if(OBSERVE_PACKAGE.contains(sbn.getPackageName())) {
                Log.d(TAG, "Working flag is false but, emm or mdm package is enabled.");
                sendBroadcast(new Intent().setAction(BroadcastReceiverForWorkTime.START_CAL_TIME));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification event) {
        if(EMM_PACKAGE_NAME.equals(event.getPackageName()) || MDM_PACKAGE_NAME.equals(event.getPackageName())) {
            // broadcast receiver 로 워크타임 계산 시작
            sendBroadcast(new Intent().setAction(BroadcastReceiverForWorkTime.START_CAL_TIME));
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification event) {
        if(EMM_PACKAGE_NAME.equals(event.getPackageName()) || MDM_PACKAGE_NAME.equals(event.getPackageName())) {
            // broadcast receiver 로 워크타임 계산 중지
            sendBroadcast(new Intent().setAction(BroadcastReceiverForWorkTime.STOP_CAL_TIME));
        }
    }
}
