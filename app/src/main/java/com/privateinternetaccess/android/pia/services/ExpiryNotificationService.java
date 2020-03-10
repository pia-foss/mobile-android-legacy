/*
 *  Copyright (c) 2020 Private Internet Access, Inc.
 *
 *  This file is part of the Private Internet Access Android Client.
 *
 *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with the Private
 *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.privateinternetaccess.android.pia.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.privateinternetaccess.android.BuildConfig;
import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.interfaces.IAccount;
import com.privateinternetaccess.android.pia.model.PIAAccountData;
import com.privateinternetaccess.android.pia.utils.DLog;
import com.privateinternetaccess.android.pia.utils.NotificationHelper;
import com.privateinternetaccess.android.ui.loginpurchasing.LoginPurchaseActivity;

public class ExpiryNotificationService extends Service {
    private static final int SERVICE_NOTIFY_EXPIRY = 7231;
    private static final long DAY_MS = 24 * 3600 * 1000;
    private static final long MONTH_MS = 31 * DAY_MS;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DLog.i("ExpiryNotificationService", "On Start Command");

//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//        Notification.Builder nBuilder = new Notification.Builder(this);
//        nBuilder.setSmallIcon(R.drawable.ic_stat_robot);
        int iconId = R.drawable.ic_stat_pia_robot_white;
//        nBuilder.setAutoCancel(true);
//        nBuilder.setContentTitle(getString(R.string.expiry_notification_title));
        String title = getString(R.string.expiry_notification_title);

        IAccount account = PIAFactory.getInstance().getAccount(this);
        PIAAccountData pai = account.getAccountInfo();

        // We should arm the timers for the next alert
        armReminders(this);

        if (pai.getTimeLeft() > MONTH_MS && !BuildConfig.DEBUG) {
            // More than one moth left?! ignore and rearm timers
            return START_NOT_STICKY;
        }

        // If we have shown a reminder recently don't annoy the user
        if (!PiaPrefHandler.showExpiryNotifcation(this))
            return START_NOT_STICKY;

        String text = null;

        if (pai.getTimeLeft() > 7 * DAY_MS)
            text = getString(R.string.expiry_notification_onnemonth);
        else if (pai.getTimeLeft() > 3 * DAY_MS)
            // More than one day left
            text = getString(R.string.expiry_notification_oneweek);

        else if (pai.getTimeLeft() > DAY_MS)
            // More than one day left
            text = getString(R.string.expirty_notification_threedays);
        else if (pai.getTimeLeft() <= DAY_MS)
            text = getString(R.string.expirty_notification_oneday);


        Intent ni = new Intent(this, LoginPurchaseActivity.class);

        PendingIntent pi = PendingIntent.getActivity(this, 0, ni, 0);
//        nBuilder.setContentIntent(pi);
//        mNotificationManager.notify(SERVICE_NOTIFY_EXPIRY, nBuilder.build());

        NotificationHelper.createNotification(this, SERVICE_NOTIFY_EXPIRY, title, iconId, true, text, pi, NotificationHelper.NOTIFICATION_CHANNEL_ID);

        PiaPrefHandler.setLastExpiryNotifcationShown(this);

        return START_NOT_STICKY;
    }

    public static void armReminders(Context c) {
        DLog.i("ExpiryNotificationService", "Arm Reminders");
        Intent serviceIntent = new Intent(c, ExpiryNotificationService.class);
        PendingIntent pIntent = PendingIntent.getService(c, SERVICE_NOTIFY_EXPIRY, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        // First cancel any armed event
        alarmManager.cancel(pIntent);

        IAccount account = PIAFactory.getInstance().getAccount(c);
        // Check if notifications should be displayed
        PIAAccountData pai = account.getAccountInfo();

        if (account.isLoggedIn() && pai.getExpiration_time() > 0) {

            long timeLeft = pai.getTimeLeft();

            long expirty_time = pai.getExpiration_time() * 1000;

            if (PIAAccountData.PLAN_YEARLY.equals(pai.getPlan()) && timeLeft > MONTH_MS) {
                long wakeTime = expirty_time - MONTH_MS;
                alarmManager.set(AlarmManager.RTC, wakeTime, pIntent);
            } else if (timeLeft > 7 * DAY_MS) {
                // Still more than a week left arm timer for one week expiry
                long wakeTime = expirty_time - 7 * DAY_MS;
                alarmManager.set(AlarmManager.RTC, wakeTime, pIntent);
            } else if (timeLeft > 3 * DAY_MS) {
                long wakeTime = expirty_time - 3 * DAY_MS;
                alarmManager.set(AlarmManager.RTC, wakeTime, pIntent);

            } else if (timeLeft > DAY_MS) {
                long wakeTime = expirty_time - DAY_MS;
                alarmManager.set(AlarmManager.RTC, wakeTime, pIntent);
            }
        }
    }
}
