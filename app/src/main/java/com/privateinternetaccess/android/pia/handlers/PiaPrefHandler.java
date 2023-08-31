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

package com.privateinternetaccess.android.pia.handlers;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.privateinternetaccess.account.model.response.AndroidSubscriptionsInformation;
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse;
import com.privateinternetaccess.account.model.response.InvitesDetailsInformation;
import com.privateinternetaccess.android.BuildConfig;
import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.model.events.TrustedWifiEvent;
import com.privateinternetaccess.android.model.states.VPNProtocol;
import com.privateinternetaccess.android.pia.model.AccountInformation;
import com.privateinternetaccess.android.pia.model.AmazonPurchaseData;
import com.privateinternetaccess.android.pia.model.PurchaseData;
import com.privateinternetaccess.android.pia.model.TrialData;
import com.privateinternetaccess.android.pia.utils.Prefs;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import kotlinx.serialization.json.Json;

/**
 * Gives access to our preferences used to control the vpn's features.
 * <p>
 * Uses our {@link Prefs} class to perform actions.
 * <p>
 * Created by hfrede on 6/13/17.
 */

public class PiaPrefHandler {
    public static final String LAST_API = "lastApi";

    public final static String PREFNAME = "com.privateinternetaccess.android_preferences";

    public static final String LOGINDATA = "MainActivity"; //Relic of the past

    public static final String EXPIRATION_TIME = "expiration_time";
    public static final String PLAN = "plan";
    public static final String EXPIRED = "expired";
    public static final String IS_USER_LOGGED_IN = "isUserLoggedIn";
    public static final String CLIENTUUID = "clientuuid";
    public static final String LOGIN = "login";

    public static final String SUBSCRIPTION_EMAIL = "subscriptionEmail";
    public static final String AVAILABLE_SUBSCRIPTIONS = "availableSubscriptions";
    public static final String HAS_SET_EMAIL = "hasSetEmail";

    public static final String EMAIL = "email";
    @Deprecated
    public static final String TOKEN = "token";
    public static final String LASTEXPIRYNOTIFICATION = "lastexpirynotification";
    public static final String ACTIVE = "active";
    public static final String SHOW_EXPIRE = "showExpire";
    public static final String TIMELEFT = "timeleft";
    public static final String RENEWABLE = "renewable";
    public static final String LAST_IP = "lastIP";
    public static final String LAST_IP_VPN = "lastIPVPN";
    public static final String PORTFORWARDING = "portforwarding";
    private static final String PORTFORWARDING_INFO = "portforwarding_info";

    public static final String SNOOZE_TIME = "lastSnoozeTime";

    public static final String WIDGET_ORDER = "widgetInformation";

    public static final String SELECTED_REGION = "selected_region";
    public static final String FAVORITE_REGIONS = "favoriteRegions";
    public static final String FAVORITES_deprecated = "favoritesSet";

    public static final String AUTOSTART = "autostart";
    public static final String AUTOCONNECT = "autoconnect";
    private static final String TRUSTED_WIFI_LIST = "trustedWifiList";
    public static final String NETWORK_RULES = "networkRules";
    public static final String NETWORK_MANAGEMENT = "networkManagement";

    public static final String PIA_MACE = "pia_mace";

    public static final String GEO_SERVERS_ACTIVE = "geo_servers_active";

    public static final String LAST_SERVER_VERSION = "last_server_version";

    public static final String PROTOCOL_TRANSPORT = "protocolTransport";
    public static final String USE_TCP_deprecated = "useTCP";
    public static final String IPV6 = "blockipv6";
    public static final String OVPN_PACKET_SIZE = "mssfix";
    public static final String WIREGUARD_PACKET_SIZE = "wireguardmssfix";

    public static final String CIPHER = "cipher";
    public static final String AUTH = "auth";

    public static final String FIRST_PERMISSION_REQUEST = "firstPermissionRequest";

    public static final String VPN_PROTOCOL = "vpn_protocol";

    public static final String WIDGET_BACKGROUND_COLOR = "widgetBackgroundColor";
    public static final String WIDGET_TEXT_COLOR = "widgetTextColor";
    public static final String WIDGET_UPLOAD_COLOR = "widgetUploadColor";
    public static final String WIDGET_DOWNLOAD_COLOR = "widgetDownloadColor";
    public static final String WIDGET_RADIUS = "widgetRadius";
    public static final String WIDGET_ALPHA = "widgetImageAlpha";

    public static final String VPN_PER_APP_PACKAGES = "vpn_per_app_packages";
    public static final String VPN_PER_APP_ARE_ALLOWED = "vpn_per_app_are_allowed";

    public static final String PURCHASING_EMAIL = "purchasingEmail";
    public static final String PURCHASING_ORDER_ID = "purchasingOrderId";
    public static final String PURCHASING_TOKEN = "purchasingToken";
    public static final String PURCHASING_SKU = "purchasingProductId";

    public static final String AMAZON_USER_ID = "amazonUserId";
    public static final String AMAZON_RECEIPT_ID = "amazonReceiptId";

    public static final String PURCHASING_TESTING_MODE = "testpurchasing";
    public static final String PURCHASING_TESTING_STATUS = "purchaseTestingStatus";
    public static final String PURCHASING_TESTING_USERNAME = "purchaseTestingUsername";
    public static final String PURCHASING_TESTING_PASSWORD = "purchaseTestingPassword";
    public static final String PURCHASING_TESTING_EXCEPTION = "purchaseTestingException";

    public static final String USE_STAGING = "usestagingbackends";
    public static final String STAGING_SERVER = "stagingServer";

    public static final String KILLSWITCH = "killswitch";
    public static final String HAPTIC_FEEDBACK = "hapticFeedback";
    public static final String GRAPHUNIT = "graphunit";

    public static final String PROXY_ENABLED = "useproxy";
    public static final String PROXY_PORT = "proxyport";
    public static final String PROXY_APP = "excluded_proxy_app";
    public static final String PROXY_ORBOT = "proxyisorbot";

    public static final String BASE_PROXY_PATH = "baseURLProxyPath";

    private static final String PREF_DEBUG_MODE = "developer_mode3";
    private static final String PREF_DEBUG_LEVEL = "debug_level";
    private static final String PREF_DEBUG_STOP_PING_SERVER = "developer_stop_pinging_server";
    private static final String PREF_DEBUG_STOP_USING_META_SERVER = "developer_stop_using_meta_server";

    public static final String DIP_TOKENS = "dip_tokens";

    public static final String HIDE_INAPP_MESSAGES_deprecated = "hide_inapp_messages";
    public static final String SHOW_INAPP_MESSAGES = "show_inapp_messages";

    public static final String LAST_VERSION = "last_version";

    public static final String REGION_PREFERRED_SORTING = "region_preferred_sorting";

    public static final String ALLOW_LOCAL_LAN = "allowLocalLan";
    public static final String BLOCK_LOCAL_LAN_deprecated = "blockLocalLan";
    public static final String TESTING_WEB_VIEW = "testingWebView";
    public static final String TESTING_WEBVIEW_SITE = "testingWebviewSite";
    public static final String TRIAL_EMAIL = "TRIAL_EMAIL";
    public static final String TRIAL_PIN = "TRIAL_PIN";
    public static final String TRIAL_TESTING = "TRIAL_TESTING";
    public static final String TRIAL_TESTING_STATUS = "TRIAL_TESTING_STATUS";
    public static final String TRIAL_TESTING_MESSAGE = "TRIAL_TESTING_MESSAGE";
    public static final String TRIAL_TESTING_USERNAME = "TRIAL_TESTING_USERNAME";
    public static final String TRIAL_TESTING_PASSWORD = "TRIAL_TESTING_PASSWORD";
    public static final String TRIAL_EMAIL_TEMP = "trialEmailTemp";
    public static final String DNS = "DNS";
    public static final String DNS_SECONDARY = "DNS_SECONDARY";
    public static final String DNS_CHANGED = "DNS_CHANGED";
    public static final String CUSTOM_DNS = "CUSTOM_DNS";
    public static final String CUSTOM_SECONDARY_DNS = "CUSTOM_SECONDARY_DNS";
    public static final String CUSTOM_DNS_SELECTED = "CUSTOM_SELECTED";
    public static final String DNS_PREF = "dns_pref";

    public static final String CONNECTION_ENDED = "connectionEndedByUser";

    public static final String QUICK_SETTINGS_KILLSWITCH = "quickSettingsKillswitch";
    public static final String QUICK_SETTINGS_NETWORK = "quickSettingsNetwork";
    public static final String QUICK_SETTING_PRIVATE_BROWSER = "quickSettingsPrivateBrowser";

    public static final String INAPP_MESSAGE_DISMISSED_IDS = "inappMessageDismissedIds";

    public static final String USAGE_BYTE_COUNT = "usageByteCount";
    public static final String USAGE_BYTE_COUNT_OUT = "usageByteCountOut";

    public static final String TESTING_REGION_INITIAL_CONNECTION = "testingRegionInitialConnection";
    public static final String TESTING_REGION_OFFLINE = "testingRegionOffline";
    public static final String TESTING_UPDATER = "testingUpdater";
    public static final String TESTING_UPDATER_SHOW_DIALOG = "testingUpdaterDialog";
    public static final String TESTING_UPDATER_SHOW_NOTIFICATION = "testingUpdaterNotification";
    public static final String TESTING_UPDATER_BUILD = "testingUpdaterBuildVersion";
    public static final String TESTING_UPDATER_INTERVAL = "testingUpdaterInterval";

    public static final String CONNECT_ON_APP_UPDATED = "connectOnAppUpdated";
    public static final String VPN_CONNECTING = "VPNConnecting";

    public static final String GEN4_QUICK_CONNECT_LIST = "gen4QuickConnectList";
    public static final String GEN4_LAST_SERVER_BODY = "GEN4_LAST_SERVER_BODY";
    private static final String GEN4_GATEWAY_ENDPOINT = "gen4_gateway_endpoint";

    private static final String RATING_STATE = "rating_state";

    private static final String INVITES_DETAILS = "invitesDetails";
    private static final String FEATURE_FLAGS = "featureFlags";
    private static final String OVERRIDE_DIP_TOKENS = "overrideDipTokens";

    private static final String KPI_SHARE_CONN_EVENTS = "kpiShareConnectionEvents";
    private static final String KPI_SHARE_CONN_TIME_EVENTS = "kpiShareConnectionTimeEvents";
    public static final String DISABLE_NMT_FEATURE_FLAG = "disable-nmt-enabled";

    public static final String RPORT = "rport";
    public static final String LPORT = "lport";
    public static final String DEFAULT_AUTO_PORT = "auto";
    public static final int LPORT_MIN_RANGE = 1024;
    public static final int LPORT_MAX_RANGE = 65535;

    public static final String LAST_KNOWN_EXCEPTION_KEY = "LAST_KNOWN_EXCEPTION_KEY";
    public static final String SUCCESSFUL_CONNECTIONS = "successfulConnections";
    public static final String IS_SURVEY_DISMISSED = "surveyDismissed";
    public static final String WIREGUARD_MIGRATION_DONE = "WIREGUARD_MIGRATION_DONE";

    public static void setInvitesDetails(
            Context context,
            InvitesDetailsInformation invitesDetailsInformation
    ) {
        Prefs.with(context).set(
                INVITES_DETAILS,
                Json.Default.encodeToString(
                        InvitesDetailsInformation.Companion.serializer(),
                        invitesDetailsInformation
                )
        );
    }

    public static InvitesDetailsInformation invitesDetails(Context context) {
        String persistedValue = Prefs.with(context).getString(INVITES_DETAILS);
        InvitesDetailsInformation invitesDetailsInformation = null;
        if (persistedValue != null) {
            invitesDetailsInformation =
                    Json.Default.decodeFromString(
                            InvitesDetailsInformation.Companion.serializer(),
                            persistedValue
                    );
        }
        return invitesDetailsInformation;
    }

    public static void setAvailableSubscriptions(
            Context context,
            AndroidSubscriptionsInformation subscriptions
    ) {
        Prefs.with(context).set(
                AVAILABLE_SUBSCRIPTIONS,
                Json.Default.encodeToString(
                        AndroidSubscriptionsInformation.Companion.serializer(),
                        subscriptions
                )
        );
    }

    public static AndroidSubscriptionsInformation availableSubscriptions(Context context) {
        String persistedValue = Prefs.with(context).getString(AVAILABLE_SUBSCRIPTIONS);
        AndroidSubscriptionsInformation subscriptionsInformation = null;
        if (persistedValue != null) {
            subscriptionsInformation =
                    Json.Default.decodeFromString(
                            AndroidSubscriptionsInformation.Companion.serializer(),
                            persistedValue
                    );
        }
        return subscriptionsInformation;
    }

    public static void setWireguardMigrationAsDone(Context context) {
        Prefs.with(context).set(WIREGUARD_MIGRATION_DONE, true);
    }

    public static Boolean isWireguardMigrationDone(Context context) {
        return Prefs.with(context).get(WIREGUARD_MIGRATION_DONE, false);
    }

    private static void resetWireguardMigrationFlag(Context context) {
        Prefs.with(context).remove(WIREGUARD_MIGRATION_DONE);
    }

    public static void setLastServerBody(Context context, String value) {
        Prefs.with(context).set(GEN4_LAST_SERVER_BODY, value);
    }

    public static String lastServerBody(Context context) {
        return Prefs.with(context).get(GEN4_LAST_SERVER_BODY, "");
    }

    public static void resetLastServerBody(Context context) {
        Prefs.with(context).remove(GEN4_LAST_SERVER_BODY);
    }

    public static void setStopPingingServersEnabled(Context context, boolean value) {
        Prefs.with(context).set(PREF_DEBUG_STOP_PING_SERVER, value);
    }

    public static boolean isStopPingingServersEnabled(Context context) {
        return Prefs.with(context).get(PREF_DEBUG_STOP_PING_SERVER, false);
    }

    public static void setStopUsingMetaServersEnabled(Context context, boolean value) {
        Prefs.with(context).set(PREF_DEBUG_STOP_USING_META_SERVER, value);
    }

    public static boolean isStopUsingMetaServersEnabled(Context context) {
        return Prefs.with(context).get(PREF_DEBUG_STOP_USING_META_SERVER, false);
    }

    public static boolean isPortForwardingEnabled(Context context) {
        return Prefs.with(context).getBoolean(PORTFORWARDING);
    }

    public static void setPortForwardingEnabled(Context context, boolean portForwarding) {
        Prefs.with(context).set(PORTFORWARDING, portForwarding);
    }

    public static void resetPortForwardingEnabled(Context context) {
        Prefs.with(context).remove(PORTFORWARDING);
    }

    public static String getRatingState(Context context) {
        return Prefs.with(context).getString(RATING_STATE);
    }

    public static void setRatingState(Context context, String ratingState) {
        Prefs.with(context).set(RATING_STATE, ratingState);
    }

    public static void setBindPortForwardInformation(Context context, String data) {
        Prefs.with(context).set(PORTFORWARDING_INFO, data);
    }

    public static String getBindPortForwardInformation(Context context) {
        return Prefs.with(context).getString(PORTFORWARDING_INFO);
    }

    public static void clearBindPortForwardInformation(Context context) {
        Prefs.with(context).remove(PORTFORWARDING_INFO);
    }

    public static void setGatewayEndpoint(Context context, String endpoint) {
        Prefs.with(context).set(GEN4_GATEWAY_ENDPOINT, endpoint);
    }

    public static String getGatewayEndpoint(Context context) {
        return Prefs.with(context).getString(GEN4_GATEWAY_ENDPOINT);
    }

    public static void clearGatewayEndpoint(Context context) {
        Prefs.with(context).remove(GEN4_GATEWAY_ENDPOINT);
    }

    public static void setHapticFeedbackEnabled(Context context, boolean value) {
        Prefs.with(context).set(HAPTIC_FEEDBACK, value);
    }

    public static boolean isHapticFeedbackEnabled(Context context) {
        return Prefs.with(context).get(HAPTIC_FEEDBACK, true);
    }

    public static void resetHapticFeedbackEnabled(Context context) {
        Prefs.with(context).remove(HAPTIC_FEEDBACK);
    }

    public static String getGraphUnit(Context context) {
        return Prefs.with(context).get(GRAPHUNIT, "8192");
    }

    public static void setGraphUnit(Context context, String value) {
        Prefs.with(context).set(GRAPHUNIT, value);
    }

    public static int getSelectedProxyPath(Context context) {
        return Prefs.with(context).get(BASE_PROXY_PATH, 0);
    }

    public static void setSelectedProxyPath(Context context, int index) {
        Prefs.with(context).set(BASE_PROXY_PATH, index);
    }

    public static boolean getLocationRequest(Context context) {
        return Prefs.with(context).get(FIRST_PERMISSION_REQUEST, false);
    }

    public static void setLocationRequest(Context context, boolean state) {
        Prefs.with(context).set(FIRST_PERMISSION_REQUEST, state);
    }

    public static int getLastServerVersion(Context context) {
        return Prefs.with(context).get(LAST_SERVER_VERSION, 0);
    }

    public static void setLastServerVersion(Context context, int version) {
        Prefs.with(context).set(LAST_SERVER_VERSION, version);
    }

    public static boolean hasSetEmail(Context context) {
        return Prefs.with(context).get(HAS_SET_EMAIL, false);
    }

    public static void setHasSetEmail(Context context, boolean hasSet) {
        Prefs.with(context).set(HAS_SET_EMAIL, hasSet);
    }

    public static void setLogin(Context context, String user) {
        Prefs prefs = new Prefs(context, LOGINDATA);
        prefs.set(LOGIN, user);
    }

    public static String getLogin(Context context) {
        return Prefs.with(context, LOGINDATA).get(LOGIN, "");
    }

    public static String getEmail(Context context) {
        return Prefs.with(context, LOGINDATA).get(SUBSCRIPTION_EMAIL, "");
    }

    public static void saveEmail(Context context, String email) {
        Prefs.with(context, LOGINDATA).set(SUBSCRIPTION_EMAIL, email);
    }

    public static String getLoginEmail(Context context) {
        return Prefs.with(context, LOGINDATA).get(EMAIL, "");
    }

    public static void saveLoginEmail(Context context, String email) {
        Prefs.with(context, LOGINDATA).set(EMAIL, email);
    }

    public static void saveTrialEmail(Context context, String email) {
        Prefs.with(context).set(TRIAL_EMAIL_TEMP, email);
    }

    public static String getTrialEmail(Context context) {
        return Prefs.with(context).get(TRIAL_EMAIL_TEMP, "");
    }

    @Deprecated
    public static String getAuthToken(Context context) {
        Prefs prefs = new Prefs(context, LOGINDATA);
        return prefs.get(TOKEN, "");
    }

    public static void setUserIsLoggedIn(Context context, boolean isLoggedIn) {
        Prefs.with(context, LOGINDATA).set(IS_USER_LOGGED_IN, isLoggedIn);
    }

    public static boolean isUserLoggedIn(Context context) {
        return Prefs.with(context, LOGINDATA).getBoolean(IS_USER_LOGGED_IN);
    }

    public static void saveLastIP(Context context, String ip) {
        Prefs.with(context).set(LAST_IP, ip);
    }

    public static String getLastIP(Context context) {
        String lastIp = Prefs.with(context).getString(LAST_IP);
        return lastIp == null ? "---" : lastIp;
    }

    public static void saveLastIPVPN(Context context, String ip) {
        Prefs.with(context).set(LAST_IP_VPN, ip);
    }

    public static String getLastIPVPN(Context context) {
        String lastIpVpn = Prefs.with(context).getString(LAST_IP_VPN);
        return lastIpVpn == null ? "---" : lastIpVpn;
    }

    public static void clearLastIPVPN(Context context) {
        Prefs.with(context).remove(LAST_IP_VPN);
    }

    public static void setLastExpiryNotifcationShown(Context context) {
        Prefs.with(context, LOGINDATA).set(LASTEXPIRYNOTIFICATION, System.currentTimeMillis());
    }

    public static long getLastSnoozeTime(Context context) {
        return Prefs.with(context).getLong(SNOOZE_TIME);
    }

    public static void setLastSnoozeTime(Context context, long time) {
        Prefs.with(context).set(SNOOZE_TIME, time);
    }

    public static String getStagingServer(Context context) {
        return Prefs.with(context).get(STAGING_SERVER, "");
    }

    public static void setStagingServer(Context context, String value) {
        Prefs.with(context).set(STAGING_SERVER, value);
    }

    public static void resetStagingServer(Context context) {
        Prefs.with(context).remove(STAGING_SERVER);
    }

    public static boolean showExpiryNotifcation(Context c) {
        long diffToLastMsg = Math.abs(System.currentTimeMillis() - Prefs.with(c, LOGINDATA).getLong(LASTEXPIRYNOTIFICATION));

        long minIntervalBetweenNotifications = BuildConfig.DEBUG ? 300 * 1000 : 23 * 3600 * 1000;

        return diffToLastMsg > minIntervalBetweenNotifications;
    }

    public static boolean getQuickSettingsNetwork(Context c) {
        return Prefs.with(c).get(QUICK_SETTINGS_NETWORK, true);
    }

    public static void setQuickSettingsNetwork(Context c, boolean shouldShow) {
        Prefs.with(c).set(QUICK_SETTINGS_NETWORK, shouldShow);
    }

    public static boolean getQuickSettingsPrivateBrowser(Context c) {
        return Prefs.with(c).get(QUICK_SETTING_PRIVATE_BROWSER, true);
    }

    public static void setQuickSettingsPrivateBrowser(Context c, boolean shouldShow) {
        Prefs.with(c).set(QUICK_SETTING_PRIVATE_BROWSER, shouldShow);
    }

    public static boolean getQuickSettingsKillswitch(Context c) {
        return Prefs.with(c).get(QUICK_SETTINGS_KILLSWITCH, true);
    }

    public static void setQuickSettingsKillswitch(Context c, boolean shouldShow) {
        Prefs.with(c).set(QUICK_SETTINGS_KILLSWITCH, shouldShow);
    }

    public static void addByteCount(Context c, long bytes) {
        long previousCount = getByteCount(c);
        Prefs.with(c).set(USAGE_BYTE_COUNT, previousCount + bytes);
    }

    public static void setByteCount(Context c, long bytes) {
        Prefs.with(c).set(USAGE_BYTE_COUNT, bytes);
    }

    public static long getByteCount(Context c) {
        return Prefs.with(c).get(USAGE_BYTE_COUNT, 0l);
    }

    public static void addByteCountOut(Context c, long bytes) {
        long previousCount = getByteCount(c);
        Prefs.with(c).set(USAGE_BYTE_COUNT_OUT, previousCount + bytes);
    }

    public static void setByteCountOut(Context c, long bytes) {
        Prefs.with(c).set(USAGE_BYTE_COUNT_OUT, bytes);
    }

    public static long getByteCountOut(Context c) {
        return Prefs.with(c).get(USAGE_BYTE_COUNT_OUT, 0l);
    }

    public static void addQuickConnectItem(Context c, String name) {
        List<String> regions = new ArrayList<>();
        Collections.addAll(regions, getQuickConnectList(c));

        for (int i = 0; i < regions.size(); i++) {
            if (regions.get(i).equals(name)) {
                regions.remove(i);
                break;
            }
        }

        if (regions.size() == 6) {
            regions.remove(5);
        }

        regions.add(0, name);

        String[] items = new String[6];

        for (int i = 0; i < regions.size(); i++) {
            if (regions.get(i) != null)
                items[i] = regions.get(i);
        }

        setQuickConnectList(c, items);
    }

    public static String[] getQuickConnectList(Context c) {
        String[] items = new String[6];
        Arrays.fill(items, "");

        try {
            JSONArray array = new JSONArray(Prefs.with(c).get(GEN4_QUICK_CONNECT_LIST, "[]"));
            for (int i = 0; i < array.length(); i++) {
                items[i] = array.getString(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return items;
    }

    public static void setQuickConnectList(Context c, String[] items) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < items.length; i++) {
            array.put(items[i]);
        }
        Prefs.with(c).set(GEN4_QUICK_CONNECT_LIST, array.toString());
    }

    public static List<String> getWidgetOrder(Context context) {
        List<String> items = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(Prefs.with(context).get(WIDGET_ORDER, "[]"));

            for (int i = 0; i < array.length(); i++) {
                items.add(array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return items;
    }

    public static void saveWidgetOrder(Context context, List<String> items) {
        JSONArray array = new JSONArray();

        for (int i = 0; i < items.size(); i++) {
            array.put(items.get(i));
        }

        Prefs.with(context).set(WIDGET_ORDER, array.toString());
    }

    private static void addFavorite(Context context, String serverName) {
        JSONArray array = new JSONArray();
        for (String favorite : getFavorites(context)) {
            array.put(favorite);
        }
        array.put(serverName);
        Prefs.with(context).set(FAVORITE_REGIONS, array.toString());
    }

    public static List<String> getFavorites(Context context) {
        List<String> items = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(Prefs.with(context).get(FAVORITE_REGIONS, "[]"));
            for (int i = 0; i < array.length(); i++) {
                items.add(array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static boolean isFavorite(Context context, String serverName) {
        return getFavorites(context).contains(serverName);
    }

    public static void clearFavorites(Context context) {
        Prefs.with(context).set(FAVORITE_REGIONS, "[]");
    }

    public static void removeFavorite(Context context, String serverName) {
        JSONArray array = new JSONArray();
        for (String favorite : getFavorites(context)) {
            if (favorite.equals(serverName)) {
                continue;
            }
            array.put(favorite);
        }
        Prefs.with(context).set(FAVORITE_REGIONS, array.toString());
    }

    public static void toggleFavorite(Context context, String serverName) {
        if (isFavorite(context, serverName)) {
            removeFavorite(context, serverName);
        } else {
            addFavorite(context, serverName);
        }
    }

    public static Set<String> getFeatureFlags(Context context) {
        return Prefs.with(context).get(FEATURE_FLAGS, new HashSet<>());
    }

    public static boolean isFeatureActive(Context context, String feature) {
        Set<String> features = getFeatureFlags(context);
        return features.contains(feature);
    }

    public static void saveFeatureFlags(Context context, List<String> featureFlags) {
        Set<String> features = new HashSet<>();

        for (String feature : featureFlags)
            features.add(feature);

        Prefs.with(context).set(FEATURE_FLAGS, features);
    }

    public static void addDismissedInAppMessageId(Context context, String id) {
        Set<String> idSet = getDismissedInAppMessageIds(context);
        idSet.add(id);

        Prefs.with(context).set(INAPP_MESSAGE_DISMISSED_IDS, idSet);
    }

    public static void clearDismissedInAppMessageIds(Context context) {
        Prefs.with(context).set(INAPP_MESSAGE_DISMISSED_IDS, new HashSet<>());
    }

    public static Set<String> getDismissedInAppMessageIds(Context context) {
        return Prefs.with(context).getStringSet(INAPP_MESSAGE_DISMISSED_IDS);
    }

    public static void saveAccountInformation(Context context, AccountInformation pai) {
        Prefs prefs = new Prefs(context, LOGINDATA);
        prefs.set(EXPIRATION_TIME, pai.getExpirationTime());
        prefs.set(EXPIRED, pai.getExpired());
        prefs.set(PLAN, pai.getPlan());
        prefs.set(ACTIVE, pai.getActive());
        prefs.set(SHOW_EXPIRE, pai.getShowExpire());
        prefs.set(RENEWABLE, pai.getRenewable());
        prefs.set(LOGIN, pai.getUsername());

        saveEmail(context, pai.getEmail());
    }

    @Nullable
    public static AccountInformation getAccountInformation(Context c) {
        Prefs prefs = new Prefs(c, LOGINDATA);
        String username = prefs.get(LOGIN, "");
        String plan = prefs.get(PLAN, "");
        long expirationTime = prefs.get(EXPIRATION_TIME, -1L);
        if (TextUtils.isEmpty(username) && TextUtils.isEmpty(plan) && expirationTime == -1L) {
            return null;
        }

        return new AccountInformation(
                getEmail(c),
                prefs.get(ACTIVE, true),
                prefs.get(EXPIRED, true),
                prefs.get(RENEWABLE, true),
                prefs.get(SHOW_EXPIRE, false),
                plan,
                expirationTime,
                username
        );

    }

    public static void clearAccountInformation(Context c) {
        Prefs prefs = new Prefs(c, LOGINDATA);
        prefs.remove(EXPIRATION_TIME);
        prefs.remove(PLAN);
        prefs.remove(EXPIRED);
        prefs.remove(ACTIVE);
        prefs.remove(SHOW_EXPIRE);
        prefs.remove(RENEWABLE);
        prefs.remove(LOGIN);
        prefs.remove(TOKEN);
        prefs.set(IS_USER_LOGGED_IN, false);
    }

    public static String getClientUniqueId(Context c) {
        Prefs prefs = new Prefs(c);
        String uuid = prefs.getString(CLIENTUUID);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            prefs.set(CLIENTUUID, uuid);
        }
        return uuid;
    }

    public static void setAutoStart(Context c, boolean autoStart) {
        Prefs.with(c).set(AUTOSTART, autoStart);
    }

    public static boolean doAutoStart(Context c) {
        return Prefs.with(c).getBoolean(AUTOSTART);
    }

    public static void resetAutoStart(Context c) {
        Prefs.with(c).remove(AUTOSTART);
    }

    public static void setAutoConnect(Context c, boolean autoConnect) {
        Prefs.with(c).set(AUTOCONNECT, autoConnect);
    }

    public static boolean doAutoConnect(Context c) {
        return Prefs.with(c).getBoolean(AUTOCONNECT);
    }

    public static void resetAutoConnect(Context c) {
        Prefs.with(c).remove(AUTOCONNECT);
    }

    public static void savePurchasingTask(Context context, String order_id, String token, String sku) {
        Prefs prefs = Prefs.with(context);
        prefs.set(PURCHASING_ORDER_ID, order_id);
        prefs.set(PURCHASING_TOKEN, token);
        prefs.set(PURCHASING_SKU, sku);
    }

    public static void saveAmazonPurchase(Context context, String userId, String receiptId) {
        Prefs prefs = Prefs.with(context);
        prefs.set(AMAZON_USER_ID, userId);
        prefs.set(AMAZON_RECEIPT_ID, receiptId);
    }

    public static AmazonPurchaseData getAmazonPurchaseData(Context context) {
        final Prefs prefs = Prefs.with(context);
        if (prefs.getString(AMAZON_USER_ID) != null && prefs.getString(AMAZON_RECEIPT_ID) != null) {
            return new AmazonPurchaseData(prefs.getString(AMAZON_USER_ID), prefs.getString(AMAZON_RECEIPT_ID));
        } else {
            return null;
        }
    }

    public static PurchaseData getPurchasingData(Context context) {
        String orderId = PiaPrefHandler.getPurchasingOrderId(context);
        String token = PiaPrefHandler.getPurchasingToken(context);
        String productId = PiaPrefHandler.getPurchasingSku(context);

        if (!TextUtils.isEmpty(productId))
            return new PurchaseData(token, productId, orderId);
        else
            return null;
    }

    public static void clearPurchasingInfo(Context context) {
        Prefs prefs = Prefs.with(context);
        prefs.remove(PURCHASING_EMAIL);
        prefs.remove(PURCHASING_ORDER_ID);
        prefs.remove(PURCHASING_TOKEN);
        prefs.remove(PURCHASING_SKU);
    }

    public static boolean isPurchasingProcessDone(Context context) {
        Prefs prefs = Prefs.with(context);
        return TextUtils.isEmpty(prefs.getString(PURCHASING_ORDER_ID));
    }

    public static String getPurchasingEmail(Context context) {
        return Prefs.with(context).getString(PURCHASING_EMAIL);
    }

    public static String getPurchasingOrderId(Context context) {
        return Prefs.with(context).getString(PURCHASING_ORDER_ID);
    }

    public static String getPurchasingToken(Context context) {
        return Prefs.with(context).getString(PURCHASING_TOKEN);
    }

    public static String getPurchasingSku(Context context) {
        return Prefs.with(context).getString(PURCHASING_SKU);
    }

    public static boolean isPurchasingTesting(Context context) {
        return Prefs.with(context).getBoolean(PURCHASING_TESTING_MODE);
    }

    public static void setPurchaseTesting(Context context, boolean testing) {
        Prefs.with(context).set(PURCHASING_TESTING_MODE, testing);
    }

    public static boolean useStaging(Context context) {
        if (BuildConfig.FLAVOR_pia.equals("production"))
            return false;
        else
            return Prefs.with(context).getBoolean(USE_STAGING);
    }

    public static void setUseStaging(Context context, boolean testing) {
        Prefs.with(context).set(USE_STAGING, testing);
    }

    public static boolean overrideDIPTokens(Context context) {
        if (BuildConfig.FLAVOR_pia.equals("production"))
            return false;
        else
            return Prefs.with(context).getBoolean(OVERRIDE_DIP_TOKENS);
    }

    public static void setOverrideDipTokens(Context context, boolean testing) {
        Prefs.with(context).set(OVERRIDE_DIP_TOKENS, testing);
    }

    public static void setPurchaseTestingStatus(Context context, int data) {
        Prefs.with(context).set(PURCHASING_TESTING_STATUS, data);
    }

    public static void setPurchaseTestingUsername(Context context, String data) {
        Prefs.with(context).set(PURCHASING_TESTING_USERNAME, data);
    }

    public static void setPurchaseTestingPassword(Context context, String data) {
        Prefs.with(context).set(PURCHASING_TESTING_PASSWORD, data);
    }

    public static void setPurchaseTestingException(Context context, String data) {
        Prefs.with(context).set(PURCHASING_TESTING_EXCEPTION, data);
    }

    public static boolean getDebugMode(Context context) {
        return Prefs.with(context).getBoolean(PREF_DEBUG_MODE);
    }

    public static void setDebugMode(Context context, boolean debugMode) {
        Prefs.with(context).set(PREF_DEBUG_MODE, debugMode);
    }

    public static int getDebugLevel(Context context) {
        return Prefs.with(context).get(PREF_DEBUG_LEVEL, 1);
    }

    public static void setDebugLevel(Context context, int debugLevel) {
        Prefs.with(context).set(PREF_DEBUG_LEVEL, debugLevel);
    }

    public static boolean getWebviewTesting(Context context) {
        return Prefs.with(context).get(TESTING_WEB_VIEW, false);
    }

    public static void setWebviewTesting(Context context, boolean testing) {
        Prefs.with(context).set(TESTING_WEB_VIEW, testing);
    }

    public static String getWebviewTestingSite(Context context) {
        return Prefs.with(context).get(TESTING_WEBVIEW_SITE, "");
    }

    public static void setWebviewTestingSite(Context context, String site) {
        Prefs.with(context).set(TESTING_WEBVIEW_SITE, site);
    }

    public static boolean getRegionInitialConnectionRandomizerTesting(Context context) {
        return Prefs.with(context).get(TESTING_REGION_INITIAL_CONNECTION, false);
    }

    public static void setRegionInitialConnectionRandomizerTesting(Context context, boolean testing) {
        Prefs.with(context).set(TESTING_REGION_INITIAL_CONNECTION, testing);
    }

    public static boolean getRegionOfflineRandomizerTesting(Context context) {
        return Prefs.with(context).get(TESTING_REGION_OFFLINE, false);
    }

    public static void setRegionOfflineRandomizerTesting(Context context, boolean testing) {
        Prefs.with(context).set(TESTING_REGION_OFFLINE, testing);
    }

    public static boolean getUpdaterTesting(Context context) {
        return Prefs.with(context).get(TESTING_UPDATER, false);
    }

    public static void setUpdaterTesting(Context context, boolean testing) {
        Prefs.with(context).set(TESTING_UPDATER, testing);
    }

    public static TrialData getTempTrialData(Context context) {
        Prefs prefs = Prefs.with(context);
        TrialData data = new TrialData(
                prefs.getString(TRIAL_EMAIL),
                prefs.getString(TRIAL_PIN)
        );
        return data;
    }

    public static void saveTempTrialData(Context context, TrialData data) {
        Prefs prefs = Prefs.with(context);
        prefs.set(TRIAL_EMAIL, data.getEmail());
        prefs.set(TRIAL_PIN, data.getPin());
    }

    public static void cleanTempTrialData(Context context) {
        Prefs prefs = Prefs.with(context);
        prefs.remove(TRIAL_PIN);
        prefs.remove(TRIAL_EMAIL);
    }

    public static boolean isTrialTesting(Context context) {
        return Prefs.with(context).get(TRIAL_TESTING, false);
    }

    public static void setTrialTesting(Context context, boolean testing) {
        Prefs.with(context).set(TRIAL_TESTING, testing);
    }

    public static boolean isConnectionUserEnded(Context context) {
        return Prefs.with(context).get(CONNECTION_ENDED, false);
    }

    public static void setUserEndedConnection(Context context, boolean val) {
        Prefs.with(context).set(CONNECTION_ENDED, val);
    }

    /**
     * if it was ended by the user, reset the value for the next connection
     *
     * @param context
     * @param resetOnYes
     * @return
     */
    public static boolean wasConnectionEndedByUser(Context context, boolean resetOnYes) {
        boolean userEnded = isConnectionUserEnded(context);
        if (resetOnYes && userEnded) {
            setUserEndedConnection(context, false);
        }
        return userEnded;
    }

    public static void clearDedicatedIps(Context context) {
        Prefs.with(context).remove(PiaPrefHandler.DIP_TOKENS);
    }

    public static List<DedicatedIPInformationResponse.DedicatedIPInformation> getDedicatedIps(Context context) {
        List<DedicatedIPInformationResponse.DedicatedIPInformation> ipList = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(Prefs.with(context).get(DIP_TOKENS, "[]"));
            for (int i = 0; i < array.length(); i++) {
                ipList.add(Json.Default.decodeFromString(DedicatedIPInformationResponse.DedicatedIPInformation.Companion.serializer(), array.getString(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ipList;
    }

    public static void addDedicatedIp(Context context, DedicatedIPInformationResponse.DedicatedIPInformation dip) {
        List<DedicatedIPInformationResponse.DedicatedIPInformation> ipList = getDedicatedIps(context);

        for (DedicatedIPInformationResponse.DedicatedIPInformation ip : ipList) {
            if (dip.getDipToken().equals(ip.getDipToken()))
                return;
        }

        ipList.add(dip);
        saveDedicatedIps(context, ipList);
    }

    public static void saveDedicatedIps(Context context, List<DedicatedIPInformationResponse.DedicatedIPInformation> ipList) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < ipList.size(); i++) {
            jsonArray.put(Json.Default.encodeToString(DedicatedIPInformationResponse.DedicatedIPInformation.Companion.serializer(), ipList.get(i)));
        }

        Prefs.with(context).set(DIP_TOKENS, jsonArray.toString());
    }

    public static void removeDedicatedIp(
            Context context,
            DedicatedIPInformationResponse.DedicatedIPInformation dedicatedIPInformation
    ) {
        List<DedicatedIPInformationResponse.DedicatedIPInformation> dips = getDedicatedIps(context);
        for (DedicatedIPInformationResponse.DedicatedIPInformation dip : dips) {
            if (dedicatedIPInformation.getDipToken().equals(dip.getDipToken())) {
                dips.remove(dip);
                break;
            }
        }

        saveDedicatedIps(context, dips);
    }

    public static void clearTrustedNetworks(Context context) {
        Prefs.with(context).remove(PiaPrefHandler.TRUSTED_WIFI_LIST);
        EventBus.getDefault().post(new TrustedWifiEvent());
    }

    public static String getLastVersion(Context context) {
        return Prefs.with(context).get(LAST_VERSION, "");
    }

    public static void setLastVersion(Context context, String version) {
        Prefs.with(context).set(LAST_VERSION, version);
    }

    public static boolean wasVPNConnecting(Context context) {
        return Prefs.with(context).getBoolean(VPN_CONNECTING);
    }

    public static void setVPNConnecting(Context context, boolean value) {
        Prefs.with(context).set(VPN_CONNECTING, value);
    }

    public static boolean isConnectOnAppUpdate(Context context) {
        return Prefs.with(context).get(CONNECT_ON_APP_UPDATED, false);
    }

    public static void setConnectOnAppUpdate(Context context, boolean value) {
        Prefs.with(context).set(CONNECT_ON_APP_UPDATED, value);
    }

    public static void resetConnectOnAppUpdate(Context context) {
        Prefs.with(context).remove(CONNECT_ON_APP_UPDATED);
    }

    public static void setDarkTheme(Context context, boolean value) {
        Prefs.with(context).set(ThemeHandler.PREF_THEME, value);
    }

    public static boolean isDarkTheme(Context context) {
        return Prefs.with(context).get(ThemeHandler.PREF_THEME, false);
    }

    public static void setShowInAppMessagesEnabled(Context context, boolean value) {
        Prefs.with(context).set(SHOW_INAPP_MESSAGES, value);
    }

    public static boolean isShowInAppMessagesEnabled(Context context) {
        return Prefs.with(context).get(SHOW_INAPP_MESSAGES, true);
    }

    public static void resetShowInAppMessagesEnabled(Context context) {
        Prefs.with(context).remove(SHOW_INAPP_MESSAGES);
    }

    public static void setProtocol(Context context, VPNProtocol.Protocol protocol) {
        Prefs.with(context).set(VPN_PROTOCOL, protocol.name());
    }

    public static String getProtocol(Context context) {
        return Prefs.with(context).get(VPN_PROTOCOL, VPNProtocol.Protocol.OpenVPN.name());
    }

    public static void resetProtocol(Context context) {
        Prefs.with(context).remove(VPN_PROTOCOL);
    }

    public static void setProtocolTransport(Context context, String value) {
        Prefs.with(context).set(PROTOCOL_TRANSPORT, value);
    }

    public static String getProtocolTransport(Context context) {
        return Prefs.with(context).get(PROTOCOL_TRANSPORT, context.getResources().getStringArray(R.array.protocol_transport)[0]);
    }

    public static void setDataCipher(Context context, String value) {
        Prefs.with(context).set(CIPHER, value);
    }

    public static String getDataCipher(Context context) {
        return Prefs.with(context).get(CIPHER, context.getResources().getStringArray(R.array.ciphers_values)[0]);
    }

    public static void resetDataCipher(Context context) {
        Prefs.with(context).remove(CIPHER);
    }

    public static void setDataAuthentication(Context context, String value) {
        Prefs.with(context).set(AUTH, value);
    }

    public static String getDataAuthentication(Context context) {
        return Prefs.with(context).get(AUTH, context.getResources().getStringArray(R.array.auth_values)[0]);
    }

    public static void resetDataAuthentication(Context context) {
        Prefs.with(context).remove(AUTH);
    }

    public static void setRemotePort(Context context, String value) {
        Prefs.with(context).set(RPORT, value);
    }

    public static String getRemotePort(Context context) {
        return Prefs.with(context).get(RPORT, DEFAULT_AUTO_PORT);
    }

    public static void resetRemotePort(Context context) {
        Prefs.with(context).remove(RPORT);
    }

    public static void setLocalPort(Context context, String value) {
        Prefs.with(context).set(LPORT, value);
    }

    public static String getLocalPort(Context context) {
        return Prefs.with(context).get(LPORT, DEFAULT_AUTO_PORT);
    }

    public static void resetLocalPort(Context context) {
        Prefs.with(context).remove(LPORT);
    }

    public static void setOvpnSmallPacketSizeEnabled(Context context, boolean value) {
        Prefs.with(context).set(OVPN_PACKET_SIZE, value);
    }

    public static boolean getOvpnSmallPacketSizeEnabled(Context context) {
        return Prefs.with(context).get(OVPN_PACKET_SIZE, context.getResources().getBoolean(R.bool.ovpnusemssfix));
    }

    public static void resetOvpnSmallPacketSizeEnabled(Context context) {
        Prefs.with(context).remove(OVPN_PACKET_SIZE);
    }

    public static void setWireguardSmallPacketSizeEnabled(Context context, boolean value) {
        Prefs.with(context).set(WIREGUARD_PACKET_SIZE, value);
    }

    public static boolean getWireguardSmallPacketSizeEnabled(Context context) {
        return Prefs.with(context).get(WIREGUARD_PACKET_SIZE, context.getResources().getBoolean(R.bool.wireguardusemssfix));
    }

    public static void resetWireguardSmallPacketSizeEnabled(Context context) {
        Prefs.with(context).remove(WIREGUARD_PACKET_SIZE);
    }

    public static void setDnsChanged(Context context, boolean value) {
        Prefs.with(context).set(DNS_CHANGED, value);
    }

    public static boolean hasDnsChanged(Context context) {
        return Prefs.with(context).get(DNS_CHANGED, false);
    }

    public static void setCustomDnsSelected(Context context, boolean value) {
        Prefs.with(context).set(CUSTOM_DNS_SELECTED, value);
    }

    public static boolean isCustomDnsSelected(Context context) {
        return Prefs.with(context).get(CUSTOM_DNS_SELECTED, false);
    }

    public static void resetCustomDnsSelected(Context context) {
        Prefs.with(context).remove(CUSTOM_DNS_SELECTED);
    }

    public static void setPrimaryDns(Context context, String value) {
        Prefs.with(context).set(DNS, value);
    }

    public static String getPrimaryDns(Context context) {
        return Prefs.with(context).get(DNS, "");
    }

    public static void resetPrimaryDns(Context context) {
        Prefs.with(context).remove(DNS);
    }

    public static void setSecondaryDns(Context context, String value) {
        Prefs.with(context).set(DNS_SECONDARY, value);
    }

    public static String getSecondaryDns(Context context) {
        return Prefs.with(context).get(DNS_SECONDARY, "");
    }

    public static void resetSecondaryDns(Context context) {
        Prefs.with(context).remove(DNS_SECONDARY);
    }

    public static void setKillswitchEnabled(Context context, boolean value) {
        Prefs.with(context).set(KILLSWITCH, value);
    }

    public static boolean isKillswitchEnabled(Context context) {
        return Prefs.with(context).getBoolean(KILLSWITCH);
    }

    public static void resetKillswitchEnabled(Context context) {
        Prefs.with(context).remove(KILLSWITCH);
    }

    public static void setGeoServersEnabled(Context context, boolean value) {
        Prefs.with(context).set(GEO_SERVERS_ACTIVE, value);
    }

    public static boolean isGeoServersEnabled(Context context) {
        return Prefs.with(context).get(GEO_SERVERS_ACTIVE, true);
    }

    public static void resetGeoServersEnabled(Context context) {
        Prefs.with(context).remove(GEO_SERVERS_ACTIVE);
    }

    public static void setBlockIpv6Enabled(Context context, boolean value) {
        Prefs.with(context).set(IPV6, value);
    }

    public static boolean isBlockIpv6Enabled(Context context) {
        return Prefs.with(context).get(IPV6, context.getResources().getBoolean(R.bool.useblockipv6));
    }

    public static void resetBlockIpv6Enabled(Context context) {
        Prefs.with(context).remove(IPV6);
    }

    public static void setAllowLocalLanEnabled(Context context, boolean value) {
        Prefs.with(context).set(ALLOW_LOCAL_LAN, value);
    }

    public static boolean isAllowLocalLanEnabled(Context context) {
        return Prefs.with(context).get(ALLOW_LOCAL_LAN, false);
    }

    public static void resetAllowLocalLanEnabled(Context context) {
        Prefs.with(context).remove(ALLOW_LOCAL_LAN);
    }

    public static void setMaceEnabled(Context context, boolean value) {
        Prefs.with(context).set(PIA_MACE, value);
    }

    public static boolean isMaceEnabled(Context context) {
        return Prefs.with(context).getBoolean(PIA_MACE);
    }

    public static void resetMaceEnabled(Context context) {
        Prefs.with(context).remove(PIA_MACE);
    }

    public static boolean isAutomationDisabledBySettingOrFeatureFlag(Context context) {
        return !PiaPrefHandler.isNetworkManagementEnabled(context) ||
                PiaPrefHandler.isFeatureActive(context, PiaPrefHandler.DISABLE_NMT_FEATURE_FLAG);
    }

    public static void setNetworkManagementEnabled(Context context, boolean value) {
        Prefs.with(context).set(NETWORK_MANAGEMENT, value);
    }

    public static boolean isNetworkManagementEnabled(Context context) {
        return Prefs.with(context).getBoolean(NETWORK_MANAGEMENT);
    }

    public static void resetNetworkManagementEnabled(Context context) {
        Prefs.with(context).remove(NETWORK_MANAGEMENT);
    }

    public static void setConnectViaProxyEnabled(Context context, boolean value) {
        Prefs.with(context).set(PROXY_ENABLED, value);
    }

    public static boolean isConnectViaProxyEnabled(Context context) {
        return Prefs.with(context).getBoolean(PROXY_ENABLED);
    }

    public static void resetConnectViaProxyEnabled(Context context) {
        Prefs.with(context).remove(PROXY_ENABLED);
    }

    public static void setProxyApp(Context context, String value) {
        Prefs.with(context).set(PROXY_APP, value);
    }

    public static String getProxyApp(Context context) {
        return Prefs.with(context).get(PROXY_APP, "");
    }

    public static void resetProxyApp(Context context) {
        Prefs.with(context).remove(PROXY_APP);
    }

    public static void setProxyPort(Context context, String value) {
        Prefs.with(context).set(PROXY_PORT, value);
    }

    public static String getProxyPort(Context context) {
        return Prefs.with(context).get(PROXY_PORT, "8080");
    }

    public static void resetProxyPort(Context context) {
        Prefs.with(context).remove(PROXY_PORT);
    }

    public static void setVpnExcludedApps(Context context, Set<String> value) {
        Prefs.with(context).set(VPN_PER_APP_PACKAGES, value);
    }

    public static Set<String> getVpnExcludedApps(Context context) {
        return Prefs.with(context).getStringSet(VPN_PER_APP_PACKAGES);
    }

    public static void resetVpnExcludedApps(Context context) {
        Prefs.with(context).remove(VPN_PER_APP_PACKAGES);
    }

    public static boolean isKpiShareConnectionEventsEnabled(Context context) {
        return Prefs.with(context).get(KPI_SHARE_CONN_EVENTS, false);
    }

    public static void setKpiShareConnectionEventsEnabled(Context context, boolean enabled) {
        Prefs.with(context).set(KPI_SHARE_CONN_EVENTS, enabled);
        Prefs.with(context).set(KPI_SHARE_CONN_TIME_EVENTS, enabled);
    }

    public static void resetKpiShareConnectionEventsEnabled(Context context) {
        Prefs.with(context).remove(KPI_SHARE_CONN_EVENTS);
    }

    public static boolean isShareTimeEventEnabled(Context context) {
        return Prefs.with(context).getBoolean(KPI_SHARE_CONN_TIME_EVENTS);
    }

    public static void resetShareTimeEnabled(Context context) {
        Prefs.with(context).remove(KPI_SHARE_CONN_TIME_EVENTS);
    }

    public static void setLastKnownException(Context context, String value) {
        Prefs.with(context).set(LAST_KNOWN_EXCEPTION_KEY, value);
    }

    public static String getLastKnownException(Context context) {
        return Prefs.with(context).get(LAST_KNOWN_EXCEPTION_KEY, "");
    }

    public static void recordSuccessfulConnection(Context context) {
        Prefs.with(context).set(SUCCESSFUL_CONNECTIONS, Prefs.with(context).get(SUCCESSFUL_CONNECTIONS, 0) + 1);
    }

    public static int getSuccessfulConnections(Context context) {
        if (isSurveyDismissed(context)) {
            return 0;
        } else {
            return Prefs.with(context).getInt(SUCCESSFUL_CONNECTIONS);
        }
    }

    public static boolean isSurveyDismissed(Context context) {
        return Prefs.with(context).get(IS_SURVEY_DISMISSED, false);
    }

    public static void dismissSurvey(Context context) {
        Prefs.with(context).set(IS_SURVEY_DISMISSED, true);
    }

    public static void resetSettings(Context context) {
        Prefs prefs = Prefs.with(context);

        // Connection Area
        prefs.set(PiaPrefHandler.USE_TCP_deprecated, false);
        PiaPrefHandler.resetPortForwardingEnabled(context);
        PiaPrefHandler.resetRemotePort(context);
        PiaPrefHandler.resetLocalPort(context);
        PiaPrefHandler.resetOvpnSmallPacketSizeEnabled(context);
        PiaPrefHandler.resetWireguardSmallPacketSizeEnabled(context);
        PiaPrefHandler.resetGeoServersEnabled(context);
        PiaPrefHandler.resetProxyPort(context);
        PiaPrefHandler.resetProxyApp(context);
        PiaPrefHandler.resetMaceEnabled(context);
        PiaPrefHandler.resetKillswitchEnabled(context);
        PiaPrefHandler.resetWireguardMigrationFlag(context);

        //Blocking
        PiaPrefHandler.resetBlockIpv6Enabled(context);
        PiaPrefHandler.resetAllowLocalLanEnabled(context);

        // Encryption
        PiaPrefHandler.resetDataCipher(context);
        PiaPrefHandler.resetDataAuthentication(context);

        // Protocol
        PiaPrefHandler.resetProtocol(context);

        // Application Settings
        prefs.set(
                PiaPrefHandler.WIDGET_BACKGROUND_COLOR,
                ContextCompat.getColor(context, R.color.widget_background_default)
        );
        prefs.set(
                PiaPrefHandler.WIDGET_TEXT_COLOR,
                ContextCompat.getColor(context, R.color.widget_text_default)
        );
        prefs.set(
                PiaPrefHandler.WIDGET_UPLOAD_COLOR,
                ContextCompat.getColor(context, R.color.widget_upload_default)
        );
        prefs.set(
                PiaPrefHandler.WIDGET_DOWNLOAD_COLOR,
                ContextCompat.getColor(context, R.color.widget_download_default)
        );
        prefs.set(PiaPrefHandler.WIDGET_RADIUS, 8);
        prefs.set(PiaPrefHandler.WIDGET_ALPHA, 100);
        PiaPrefHandler.resetPrimaryDns(context);
        PiaPrefHandler.resetSecondaryDns(context);
        PiaPrefHandler.resetAutoStart(context);
        PiaPrefHandler.resetAutoConnect(context);
        PiaPrefHandler.resetConnectOnAppUpdate(context);
        PiaPrefHandler.resetHapticFeedbackEnabled(context);
        PiaPrefHandler.resetShowInAppMessagesEnabled(context);

        // Per App Settings
        prefs.set(PiaPrefHandler.VPN_PER_APP_ARE_ALLOWED, false);
        PiaPrefHandler.resetVpnExcludedApps(context);

        // In App Messages
        PiaPrefHandler.clearDismissedInAppMessageIds(context);

        PiaPrefHandler.clearFavorites(context);
        PiaPrefHandler.clearTrustedNetworks(context);
        PiaPrefHandler.clearDedicatedIps(context);
        PiaPrefHandler.resetKpiShareConnectionEventsEnabled(context);
        PiaPrefHandler.resetShareTimeEnabled(context);
    }
}