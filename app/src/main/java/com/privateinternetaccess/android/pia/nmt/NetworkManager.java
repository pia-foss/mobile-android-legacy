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

package com.privateinternetaccess.android.pia.nmt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import androidx.annotation.Nullable;

import com.privateinternetaccess.android.pia.nmt.models.NetworkItem;

import java.util.List;


public class NetworkManager {

    public static NetworkItem.NetworkBehavior getBestRule(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            return RulesManager.getBehavior(context, activeNetwork.getType());
        }

        return null;
    }

    public static boolean isConnectedTrustedWifi(Context context) {
        WifiManager wifiManager =
                (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ssid = wifiManager.getConnectionInfo().getSSID();
        String sanitizedSSID = ssid.substring(1, ssid.length() - 1);

        List<String> serializedRules = PrefsHandler.getSerializedNetworkRules(context);
        for (String serializedRule : serializedRules) {
            NetworkItem rule = NetworkItem.fromString(serializedRule);

            if (rule != null) {
                if (rule.networkName.equals(sanitizedSSID))
                    return true;
            }
        }

        return false;
    }

    @Nullable
    public static NetworkItem networkItemForSsid(Context context, String ssid) {
        List<String> serializedResults = PrefsHandler.getSerializedNetworkRules(context);

        for (String item : serializedResults) {
            NetworkItem network = NetworkItem.fromString(item);

            if (network.networkName.equals(ssid)) {
                return network;
            }
        }

        return null;
    }

    public boolean hasBackgroundPermissions() {
        return false;
    }
}
