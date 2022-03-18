package com.privateinternetaccess.android.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.privateinternetaccess.android.BuildConfig;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.utils.DLog;
import com.privateinternetaccess.android.pia.utils.Prefs;

public class UpdateCheckReceiver extends BroadcastReceiver {

    private static int UPDATE_RECEIVER_CODE = 12512;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            setupUpdateCheck(context);
        }
    }

    public static void setupUpdateCheck(Context context) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.privateinternetaccess.android.FETCH_UPDATE");

        long alarmInterval = AlarmManager.INTERVAL_DAY;

        if (BuildConfig.FLAVOR_pia.equals("qa") &&
                PiaPrefHandler.getUpdaterTesting(context) &&
                Prefs.with(context).get(PiaPrefHandler.TESTING_UPDATER_INTERVAL, 0L) != 0) {
            alarmInterval = Prefs.with(context).get(PiaPrefHandler.TESTING_UPDATER_INTERVAL, alarmInterval);
        }

        PendingIntent pendingBroadcast = PendingIntent.getBroadcast(context,
                UPDATE_RECEIVER_CODE, broadcastIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + alarmInterval,
                alarmInterval, pendingBroadcast);
    }

}
