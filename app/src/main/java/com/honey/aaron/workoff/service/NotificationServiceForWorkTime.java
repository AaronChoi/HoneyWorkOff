package com.honey.aaron.workoff.service;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.honey.aaron.workoff.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class NotificationServiceForWorkTime extends NotificationListenerService {
    private static final String TAG = NotificationServiceForWorkTime.class.getSimpleName();
    private static final String EMM_PACKAGE_NAME = "com.sds.emm.client";
    private static final String MDM_PACKAGE_NAME = "com.sds.mobile.mdm.client";

    private static List<String> OBSERVE_PACKAGE = new ArrayList<>();
    static NotificationServiceForWorkTime notiListener;

    public static NotificationServiceForWorkTime getInstance() {
        if(notiListener == null) {
            notiListener = new NotificationServiceForWorkTime();
        }
        OBSERVE_PACKAGE.add(EMM_PACKAGE_NAME);
        OBSERVE_PACKAGE.add(MDM_PACKAGE_NAME);
        return notiListener;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification event) {
        if(EMM_PACKAGE_NAME.equals(event.getPackageName()) || MDM_PACKAGE_NAME.equals(event.getPackageName())) {
            // broadcast receiver 로 워크타임 계산 시작
            sendBroadcast(new Intent().setAction(MainActivity.BroadcastReceiverForWorkTime.START_CAL_TIME));
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification event) {
        if(EMM_PACKAGE_NAME.equals(event.getPackageName()) || MDM_PACKAGE_NAME.equals(event.getPackageName())) {
            // broadcast receiver 로 워크타임 계산 중지
            sendBroadcast(new Intent().setAction(MainActivity.BroadcastReceiverForWorkTime.STOP_CAL_TIME));
        }
    }

    public boolean isActivateObservedPackage() {
        for(StatusBarNotification sbn : notiListener.getActiveNotifications()){
            if(OBSERVE_PACKAGE.contains(sbn.getPackageName())) {
                return true;
            }
        }
        return false;
    }
}
