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

package com.privateinternetaccess.android.pia.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;

import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.utils.KeyStoreUtils;

import org.json.JSONArray;

import java.util.HashSet;
import java.util.Set;

import static com.privateinternetaccess.android.pia.handlers.PIAServerHandler.SELECTEDREGION_deprecated;
import static com.privateinternetaccess.android.pia.handlers.PiaPrefHandler.BLOCK_LOCAL_LAN_deprecated;
import static com.privateinternetaccess.android.pia.handlers.PiaPrefHandler.HIDE_INAPP_MESSAGES_deprecated;
import static com.privateinternetaccess.android.pia.handlers.PiaPrefHandler.USE_TCP_deprecated;


/**
 * Created by half47 on 6/9/16.
 *
 * quick method to handle preferences
 */
public class Prefs {

    private final String ENCRYPTED_SUFFIX = "_ENC";
    private final String STRING_SET_SUFFIX = "_V2";

    private static KeyStoreUtils keyStoreUtils;
    private MultiPreferences preferences;
    private SharedPreferences oldPreferences;
    private String[] privacySensitiveKeys = new String[]{
            PiaPrefHandler.TOKEN,
            PiaPrefHandler.EMAIL,
            PiaPrefHandler.PURCHASING_EMAIL,
            PiaPrefHandler.SUBSCRIPTION_EMAIL,
            PiaPrefHandler.TRIAL_EMAIL,
            PiaPrefHandler.TRIAL_EMAIL_TEMP,
            PiaPrefHandler.DIP_TOKENS,
            PiaPrefHandler.GEN4_LAST_SERVER_BODY,
            PiaPrefHandler.GEN4_QUICK_CONNECT_LIST,
            PiaPrefHandler.FAVORITE_REGIONS,
            PiaPrefHandler.SELECTED_REGION,
    };

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void setKeyStoreUtils(KeyStoreUtils ksUtils) {
        keyStoreUtils = ksUtils;
    }

    /**
     * Sets up using the pref file PiaServerDatabase.PREFNAME
     */
    public Prefs(Context context){ setUp(context, PiaPrefHandler.PREFNAME); }

    /**
     * Sets up using the pref file name you sent in filename
     *
     */
    public Prefs(Context context, String fileName){
        setUp(context, fileName);
    }

    private void setUp(Context context, String fileName){
        preferences = new MultiPreferences(fileName, context.getContentResolver());
        oldPreferences = context.getSharedPreferences(fileName, 0);
        prepareKeyStoreUtils(context);
        migrateProtocolTransport(context);
        migrateSelectedRegion();
        migrateBlockLocalLan();
        migrateInAppMessages();
        migrateFavorites();
    }

    private void prepareKeyStoreUtils(Context context) {
        if (keyStoreUtils == null) {
            keyStoreUtils = new KeyStoreUtils(context, preferences);
        }
    }

    private void migrateProtocolTransport(Context context) {
        boolean wasRemovedAlready = !preferences.containsBoolean(USE_TCP_deprecated);
        if (wasRemovedAlready) {
            return;
        }

        boolean usesTCP = get(USE_TCP_deprecated, false);
        remove(USE_TCP_deprecated);

        String transport = context.getResources().getStringArray(R.array.protocol_transport)[0];
        if (usesTCP) {
            transport = context.getResources().getStringArray(R.array.protocol_transport)[1];
        }
        set(PiaPrefHandler.PROTOCOL_TRANSPORT, transport);
    }

    private void migrateSelectedRegion() {
        String oldSelectedRegion = get(SELECTEDREGION_deprecated, "");
        remove(SELECTEDREGION_deprecated);
        if (TextUtils.isEmpty(oldSelectedRegion)) {
            return;
        }

        set(PiaPrefHandler.SELECTED_REGION, oldSelectedRegion);
    }

    private void migrateBlockLocalLan() {
        boolean wasRemovedAlready = !preferences.containsBoolean(BLOCK_LOCAL_LAN_deprecated);
        if (wasRemovedAlready) {
            return;
        }

        boolean blockLocalLan = get(BLOCK_LOCAL_LAN_deprecated, true);
        remove(BLOCK_LOCAL_LAN_deprecated);
        set(PiaPrefHandler.ALLOW_LOCAL_LAN, !blockLocalLan);
    }

    private void migrateInAppMessages() {
        boolean wasRemovedAlready = !preferences.containsBoolean(HIDE_INAPP_MESSAGES_deprecated);
        if (wasRemovedAlready) {
            return;
        }

        boolean hideInAppMessages = get(HIDE_INAPP_MESSAGES_deprecated, false);
        remove(HIDE_INAPP_MESSAGES_deprecated);
        set(PiaPrefHandler.SHOW_INAPP_MESSAGES, !hideInAppMessages);
    }

    private void migrateFavorites() {
        Set<String> oldFavorites = get(PiaPrefHandler.FAVORITES_deprecated + STRING_SET_SUFFIX, new HashSet<>());
        remove(PiaPrefHandler.FAVORITES_deprecated + STRING_SET_SUFFIX);
        if (oldFavorites.isEmpty()) {
            return;
        }

        JSONArray array = new JSONArray();
        for (String favorite : oldFavorites) {
            array.put(favorite);
        }
        set(PiaPrefHandler.FAVORITE_REGIONS, array.toString());
    }

    /**
     * quick factory method.
     *
     */
    public static Prefs with(Context context){
        return new Prefs(context);
    }

    /**
     * quick factory method with added ability to change file name.
     *
     */
    public static Prefs with(Context context, String fileName){
        return new Prefs(context, fileName);
    }

    /**
     * remove any item from stored preferences
     *
     */
    public void remove(String key){
        if (isPrivacySensitiveKey(key)) {
            preferences.removePreference(key + ENCRYPTED_SUFFIX);
        }

        preferences.removePreference(key);
    }

    // getters with defaults

    public String get(String key, String defaultValue){
        // If we are dealing with a privacy sensitive key. Try accessing the encrypted version of it
        // using the ENCRYPTED_SUFFIX. Otherwise default to legacy.
        if (isPrivacySensitiveKey(key)) {
            String value = preferences.getString(key + ENCRYPTED_SUFFIX, defaultValue);
            if (value != null && !TextUtils.isEmpty(value) && !value.equals(defaultValue)) {
                String decryptedValue = keyStoreUtils.decrypt(value);
                if (decryptedValue == null) {
                    // If decryption failed. It is most likely because of invalid backed up data
                    // and it would keep failing. Remove it.
                    preferences.removePreference(key);
                    decryptedValue = defaultValue;
                }
                return decryptedValue;
            }
        }

        return preferences.getString(key, defaultValue);
    }

    public boolean get(String key, boolean defaultValue){
        return preferences.getBoolean(key, defaultValue);
    }

    public int get(String key, int defaultValue){
        return preferences.getInt(key, defaultValue);
    }

    public long get(String key, long defaultValue){
        return preferences.getLong(key, defaultValue);
    }

    public Set<String> get(String key, Set<String> defaultValue){
        // TODO: Condition to be removed once 3.7.0 is officially released
        if (oldPreferences.getAll().get(key) != null && (oldPreferences.getAll().get(key) instanceof String)) {
            String value = oldPreferences.getString(key, null);
            if (!TextUtils.isEmpty(value)) {
                return PrefsUtils.INSTANCE.parseSet(value);
            }
        }

        // If there is a MultiPreferences value persisted. It takes priority.
        String multiPreferencesValue = preferences.getString(key + STRING_SET_SUFFIX, null);
        if (multiPreferencesValue != null) {
            return PrefsUtils.INSTANCE.parseSet(multiPreferencesValue);
        }

        // Otherwise use the legacy value on SharedPreferences
        return oldPreferences.getStringSet(key, defaultValue);
    }

    // quick getters

    /**
     *
     * @return string value or null
     */
    public String getString(String key){
        String defaultValue = null;
        return get(key, defaultValue);
    }

    /**
     *
     * @return int value or 0
     */
    public int getInt(String key){
        return get(key, 0);
    }

    /**
     *
     * @return boolean value or false
     */
    public boolean getBoolean(String key){
        return get(key, false);
    }

    /**
     *
     * @return long value or 0l
     */
    public long getLong(String key){
        return get(key, 0L);
    }

    /**
     *
     * @return set or empty set
     */
    public Set<String> getStringSet(String key){
        return get(key, new HashSet<String>());
    }

    // Setters

    public void set(String key, String value) {
        if (isPrivacySensitiveKey(key)) {
            // Remove old non-encrypted key if needed
            preferences.removePreference(key);

            key = key + ENCRYPTED_SUFFIX;
            if (value != null) {
                value = keyStoreUtils.encrypt(value);
            }
        }
        preferences.setString(key, value);
    }

    public void set(String key, boolean value){
        preferences.setBoolean(key, value);
    }

    public void set(String key, int value){
        preferences.setInt(key, value);
    }

    public void set(String key, long value){
        preferences.setLong(key, value);
    }

    public void set(String key, Set<String> value){
        preferences.setString(key + STRING_SET_SUFFIX, PrefsUtils.INSTANCE.stringifySet(value));
    }

    public Boolean isPrivacySensitiveKey(String key) {
        for (String privacySensitiveKey : privacySensitiveKeys) {
            if (privacySensitiveKey.equals(key)) {
                return true;
            }
        }
        return false;
    }
}
