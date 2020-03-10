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

package com.privateinternetaccess.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.interfaces.IVPN;
import com.privateinternetaccess.android.pia.utils.DLog;
import com.privateinternetaccess.android.utils.SnoozeUtils;

public class OnAutoConnectNetworkReceiver extends BroadcastReceiver {

    private static final long CONNECTION_DELAY = 5000;
    private static final long DISCONNECTION_DELAY = 5000;

    @Override
    public void onReceive(final Context context, Intent intent) {
        IVPN vpn = PIAFactory.getInstance().getVPN(context);

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE && PiaPrefHandler.shouldConnectOnCellular(context)
                    && vpn.isVPNActive() && PiaPrefHandler.getLastConnection(context) + CONNECTION_DELAY < System.currentTimeMillis()) {
                vpn.stop();
            }
            else if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI && PiaPrefHandler.shouldConnectOnWifi(context)) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifiManager.getConnectionInfo();

                String ssid  = info.getSSID();
                ssid = ssid.substring(1, ssid.length() - 1);

                if (PiaPrefHandler.getTrustedNetworks(context).contains(ssid)) {
                    if (vpn.isVPNActive() && PiaPrefHandler.getLastConnection(context) + CONNECTION_DELAY < System.currentTimeMillis())
                        vpn.stop();
                }
                else if (!vpn.isVPNActive() &&
                        PiaPrefHandler.getLastDisconnection(context) + DISCONNECTION_DELAY < System.currentTimeMillis() &&
                        !SnoozeUtils.hasActiveAlarm(context)) {
                    DLog.d("NetworkSettings", "Snooze: " + SnoozeUtils.hasActiveAlarm(context));
                    vpn.start();
                }
            }
        }
    }
}
