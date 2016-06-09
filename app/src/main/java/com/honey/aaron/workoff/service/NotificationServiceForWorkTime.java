package com.honey.aaron.workoff.service;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.honey.aaron.workoff.activity.MainActivity;

public class NotificationServiceForWorkTime extends NotificationListenerService {
    private static final String TAG = NotificationServiceForWorkTime.class.getSimpleName();
    private static final String EMM_PACKAGE_NAME = "com.sds.emm.client";
    private static final String MDM_PACKAGE_NAME = "com.sds.mobile.mdm.client";

    @Override
    public void onNotificationPosted(StatusBarNotification event) {
        if(EMM_PACKAGE_NAME.equals(event.getPackageName()) || MDM_PACKAGE_NAME.equals(event.getPackageName())) {
            // broadcast receiver 로 워크타임 계산 시작
        }
        sendBroadcast(new Intent().setAction(MainActivity.BroadcastReceiverForWorkTime.START_CAL_TIME));
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification event) {
        if(EMM_PACKAGE_NAME.equals(event.getPackageName()) || MDM_PACKAGE_NAME.equals(event.getPackageName())) {
            // broadcast receiver 로 워크타임 계산 중지
            sendBroadcast(new Intent().setAction(MainActivity.BroadcastReceiverForWorkTime.STOP_CAL_TIME));
        }
    }
}
