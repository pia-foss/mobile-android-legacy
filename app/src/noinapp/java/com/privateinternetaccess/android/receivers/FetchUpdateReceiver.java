package com.privateinternetaccess.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.privateinternetaccess.android.handlers.UpdateHandler;

public class FetchUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.privateinternetaccess.android.FETCH_UPDATE")) {
            UpdateHandler.checkUpdates(context, UpdateHandler.UpdateDisplayType.SHOW_NOTIFICATION);
        }
    }
}
