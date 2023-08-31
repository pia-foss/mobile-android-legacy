package com.privateinternetaccess.android.pia.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import com.privateinternetaccess.android.PIAApplication

class NetworkReceiver(private val callback: NetworkConnectionListener) :
    BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == CONNECTIVITY_ACTION) {
            callback.isConnected(PIAApplication.isNetworkAvailable(context))
        }
    }
}