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

import android.content.SharedPreferences;

import com.privateinternetaccess.android.pia.nmt.models.NetworkItem;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class PrefsHandler {
    private static String PREFS_NAME = "nmt_prefs";

    private static String NETWORK_RULES = "networkRules";

    public static List<String> getSerializedNetworkRules(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        List<String> items = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(prefs.getString(NETWORK_RULES, "[]"));

            for (int i = 0; i < array.length(); i++) {
                items.add(array.getString(i));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return items;
    }

    public static void removeNetworkRule(Context context, NetworkItem networkRule) {
        List<String> rules = getSerializedNetworkRules(context);

        for (int i = 0; i < rules.size(); i++) {
            NetworkItem rule = NetworkItem.fromString(rules.get(i));

            if (rule != null && networkRule.networkName.equals(rule.networkName)) {
                rules.remove(i);
                updateNetworkRules(context, rules);
                return;
            }
        }
    }

    public static void saveNetworkRule(Context context, NetworkItem networkRule) {
        List<String> rules = getSerializedNetworkRules(context);

        String serializedRule = networkRule.toString();

        if (serializedRule == null) {
            return;
        }

        for (int i = 0; i < rules.size(); i++) {
            NetworkItem rule = NetworkItem.fromString(rules.get(i));

            if (rule != null && networkRule.networkName.equals(rule.networkName)) {
                rules.set(i, serializedRule);
                updateNetworkRules(context, rules);
                return;
            }
        }

        rules.add(serializedRule);
        updateNetworkRules(context, rules);
    }

    public static void updateNetworkRules(Context context, List<String> networkRules) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        JSONArray array = new JSONArray();

        for (int i = 0; i < networkRules.size(); i++) {
            array.put(networkRules.get(i));
        }

        editor.putString(NETWORK_RULES, array.toString());
        editor.apply();
    }
}
