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

package com.privateinternetaccess.android.ui;

import static com.privateinternetaccess.android.pia.model.enums.RequestResponseStatus.SUCCEEDED;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.privateinternetaccess.android.PIAApplication;
import com.privateinternetaccess.android.PIALifecycleObserver;
import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.interfaces.IAccount;
import com.privateinternetaccess.android.pia.model.enums.RequestResponseStatus;
import com.privateinternetaccess.android.pia.services.AutomationService;
import com.privateinternetaccess.android.pia.utils.DLog;
import com.privateinternetaccess.android.pia.utils.Prefs;
import com.privateinternetaccess.android.ui.connection.MainActivity;
import com.privateinternetaccess.android.ui.connection.MainActivityHandler;
import com.privateinternetaccess.android.ui.connection.VPNPermissionActivity;
import com.privateinternetaccess.android.ui.loginpurchasing.LoginPurchaseActivity;
import com.privateinternetaccess.android.ui.tv.DashboardActivity;
import com.privateinternetaccess.android.utils.DedicatedIpUtils;
import com.privateinternetaccess.android.utils.InAppMessageManager;


public class LauncherActivity extends AppCompatActivity {

    public static final String TAG = "LauncherActivity";
    public static final String USERNAME = "username";
    public static final String TOKEN = "token";
    public static final String HAS_AUTO_STARTED = "hasAutoStarted";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Prefs.with(getApplicationContext()).set(HAS_AUTO_STARTED, false);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new PIALifecycleObserver(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFlags(this);
    }

    private void nextActivityLogic() {
        Intent intent = getIntent();
        DLog.i(TAG, "Starting app");

        final IAccount account = PIAFactory.getInstance().getAccount(this);

        if (intent != null && intent.getData() != null) {
            Uri openUri = intent.getData();
            setIntent(null);

            if (openUri.toString().contains("login")) {
                String url = openUri.toString();
                url = url.replace("piavpn:login?", "piavpn://login/?");
                DLog.i(TAG, "URL: " + url);
                openUri = Uri.parse(url);
            }

            final String username = openUri.getQueryParameter(USERNAME);
            final String token = openUri.getQueryParameter(TOKEN);
            if (token != null) {
                if (!account.loggedIn()) {
                    account.migrateApiToken(token, requestResponseStatus -> {
                        if (requestResponseStatus != SUCCEEDED) {
                            DLog.d(TAG, "migrateApiToken failed");
                            return null;
                        }

                        PiaPrefHandler.setUserIsLoggedIn(LauncherActivity.this, true);
                        launchVPN(LauncherActivity.this);
                        return null;
                    });
                }
            }

            if (username != null) {
                DLog.d(TAG, "Stored = " + PiaPrefHandler.getLogin(getApplicationContext()) + " open = " + username);
                if (!PiaPrefHandler.getLogin(getApplicationContext()).equals(username)) {
                    PiaPrefHandler.setUserIsLoggedIn(getApplicationContext(), false);
                }
            }
        }

        if (account.apiToken() == null && account.vpnToken() == null) {
            account.migrateApiToken(PiaPrefHandler.getAuthToken(this), requestResponseStatus -> {
                if (requestResponseStatus != SUCCEEDED) {
                    DLog.d(TAG, "migrateApiToken failed");
                    launchVPN(this);
                    return null;
                }

                PiaPrefHandler.setUserIsLoggedIn(LauncherActivity.this, true);
                launchVPN(this);
                return null;
            });
        } else {
            launchVPN(this);
        }
    }

    void launchVPN(Context context) {
        Intent intent = new Intent(context, LoginPurchaseActivity.class);
        IAccount account = PIAFactory.getInstance().getAccount(this);
        if (account.loggedIn()) {
            intent = getIntentForLoggedInUser(context);
        }
        startActivity(intent);
    }

    void loadOnLaunch(Context context) {
        DedicatedIpUtils.refreshTokensAndInAppMessages(context);

        if (PIAApplication.isAndroidTV(this)) {
            PIAApplication.validatePreferences(this);
        }

        if (PiaPrefHandler.isShowInAppMessagesEnabled(context)) {
            IAccount account = PIAFactory.getInstance().getAccount(context);
            account.message((message, response) -> {
                if (response == RequestResponseStatus.SUCCEEDED) {
                    InAppMessageManager.queueRemoteMessage(message);
                }
                return null;
            });
        }
    }

    private void loadFlags(Context context) {
        PIAFactory.getInstance().getAccount(context).featureFlags((flags, responseStatus) -> {
            if (responseStatus != SUCCEEDED) {
                DLog.i(TAG, "Failed to fetch feature flags");
            }

            if (flags == null) {
                DLog.i(TAG, "Invalid feature flags response");
            } else {
                PiaPrefHandler.saveFeatureFlags(context, flags.getFlags());
                if (PiaPrefHandler.isFeatureActive(this, MainActivityHandler.FEATURE_NEW_INITIAL_SCREEN)) {
                    PIAFactory.getInstance()
                            .getAccount(this)
                            .availableSubscriptions((subscriptionsInformation, responseStatus1) -> {
                                        PiaPrefHandler.setAvailableSubscriptions(
                                                this,
                                                subscriptionsInformation
                                        );
                                        return null;
                                    }
                            );
                }
            }

            prepareAutomationOnLaunch(context);
            nextActivityLogic();
            return null;
        });
    }

    private void prepareAutomationOnLaunch(Context context) {
        // If automation was disabled. Make sure to stop the service.
        if (PiaPrefHandler.isAutomationDisabledBySettingOrFeatureFlag(context)) {
            AutomationService.Companion.stop(context);
        }
    }

    private Intent getPlatformMainActivity(Context context) {
        if (PIAApplication.isAndroidTV(context)) {
            return new Intent(this, DashboardActivity.class);
        } else {
            return new Intent(this, MainActivity.class);
        }
    }

    private Intent getIntentForLoggedInUser(Context context) {
        Intent intent = getPlatformMainActivity(context);
        if (VpnService.prepare(context) == null) {
            loadOnLaunch(context);
            if (getIntent() != null) {
                String shortcut = getIntent().getAction();
                if (!TextUtils.isEmpty(shortcut)) {
                    intent.setAction(shortcut);
                }
            }
        } else {
            intent = new Intent(this, VPNPermissionActivity.class);
        }
        return intent;
    }
}