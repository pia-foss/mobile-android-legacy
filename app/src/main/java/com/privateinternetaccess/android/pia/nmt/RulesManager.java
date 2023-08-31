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
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

import static android.net.ConnectivityManager.TYPE_WIFI;

import com.privateinternetaccess.android.pia.nmt.models.NetworkItem;

public class RulesManager {
    private Context mContext;

    public RulesManager(Context context) {
        this.mContext = context;
    }

    public static RulesChangedListener rulesChangedListener = null;

    public static NetworkItem.NetworkBehavior getBehavior(Context context, int networkType) {
        if (networkType == TYPE_WIFI) {
            return getWiFiBehavior(context);
        } else {
            return getMobileBehaviour(context);
        }
    }

    private static NetworkItem.NetworkBehavior getMobileBehaviour(Context context) {
        List<String> serializedRules = PrefsHandler.getSerializedNetworkRules(context);

        for (String serializedRule : serializedRules) {
            NetworkItem rule = NetworkItem.fromString(serializedRule);

            if (rule != null && rule.isDefaultMobile) {
                return rule.behavior;
            }
        }

        return NetworkItem.NetworkBehavior.RETAIN_STATE;
    }

    private static NetworkItem.NetworkBehavior getWiFiBehavior(Context context) {
        List<String> serializedRules = PrefsHandler.getSerializedNetworkRules(context);
        NetworkItem defaultRule = null;

        WifiManager wifiManager =
                (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ssid = wifiManager.getConnectionInfo().getSSID();
        String sanitizedSSID = ssid.substring(1, ssid.length() - 1);

        for (String serializedRule : serializedRules) {
            NetworkItem rule = NetworkItem.fromString(serializedRule);

            if (rule != null) {
                if (rule.networkName.equals(sanitizedSSID)) {
                    return rule.behavior;
                }

                if (rule.isDefaultOpen) {
                    defaultRule = rule;
                }
            }
        }

        return defaultRule != null ? defaultRule.behavior : NetworkItem.NetworkBehavior.ALWAYS_CONNECT;
    }

    public void addRuleForNetwork(ScanResult network, NetworkItem.NetworkBehavior behavior) {
        NetworkItem newItem = new NetworkItem();
        newItem.type = NetworkItem.NetworkType.WIFI_CUSTOM;
        newItem.behavior = behavior;
        newItem.networkName = network.SSID;

        PrefsHandler.saveNetworkRule(mContext, newItem);

        if (rulesChangedListener != null && mContext != null) {
            rulesChangedListener.onRulesChanged(mContext);
        }
    }

    public void removeNetworkRule(NetworkItem rule) {
        PrefsHandler.removeNetworkRule(mContext, rule);

        if (rulesChangedListener != null && mContext != null) {
            rulesChangedListener.onRulesChanged(mContext);
        }
    }

    public void updateNetworkRule(NetworkItem rule, NetworkItem.NetworkBehavior behavior) {
        if (rule == null) {
            return;
        }

        rule.behavior = behavior;
        PrefsHandler.saveNetworkRule(mContext, rule);

        if (rulesChangedListener != null && mContext != null) {
            rulesChangedListener.onRulesChanged(mContext);
        }
    }

    public List<NetworkItem> getRules() {
        List<String> serializedRules = PrefsHandler.getSerializedNetworkRules(mContext);
        List<NetworkItem> networkList = new ArrayList<>();

        if (serializedRules.size() == 0) {
            networkList.addAll(NetworkItem.defaultList(mContext));
            saveDefaults();
        } else {
            for (int i = 0; i < serializedRules.size(); i++) {
                networkList.add(NetworkItem.fromString(serializedRules.get(i)));
            }
        }

        return networkList;
    }

    private void saveDefaults() {
        List<NetworkItem> rules = NetworkItem.defaultList(mContext);
        List<String> serializedRules = new ArrayList<>();

        for (int i = 0; i < rules.size(); i++) {
            serializedRules.add(rules.get(i).toString());
        }

        PrefsHandler.updateNetworkRules(mContext, serializedRules);
    }

    public interface RulesChangedListener {
        void onRulesChanged(Context context);
    }
}

