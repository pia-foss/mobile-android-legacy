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

package com.privateinternetaccess.android.ui.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.model.events.SettingsUpdateEvent;
import com.privateinternetaccess.android.pia.services.AutomationService;
import com.privateinternetaccess.android.ui.drawer.TrustedWifiActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class QuickSettingsView extends FrameLayout {

    public enum QuickSettings {
        SETTING_KILL_SWITCH,
        SETTING_NETWORK,
        SETTING_BROWSER
    }

    @BindView(R.id.quick_settings_icons_layout) LinearLayout lIcons;

    public QuickSettingsView(Context context) {
        super(context);
        init(context);
    }

    public QuickSettingsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public QuickSettingsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        inflate(context, R.layout.view_quick_settings, this);
        ButterKnife.bind(this, this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
        setupStates();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }


    @OnClick(R.id.quick_settings_layout)
    public void onTileClicked() {
        Intent i = new Intent(getContext(), QuickSettingsSettings.class);
        getContext().startActivity(i);
    }

    public void onKillSwitchClicked() {
        QuickSettingsKillSwitchActivity.Companion.open(getContext());
    }

    public void onBrowserClicked() {
        Intent launchIntent = getContext().getPackageManager().getLaunchIntentForPackage("nu.tommie.inbrowser");
        if (launchIntent != null) {
            String url = "https://www.privateinternetaccess.com";
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.setData(Uri.parse(url));
            getContext().startActivity(launchIntent);
        }
        else {
            launchIntent = new Intent(Intent.ACTION_VIEW);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.setData(Uri.parse("market://details?id=" + "nu.tommie.inbrowser"));

            //silently fail if Google Play Store isn't installed.
            if (launchIntent.resolveActivity(getContext().getPackageManager()) != null) {
                getContext().startActivity(launchIntent);
            }
        }
    }

    public void onNetworkClicked(Context context) {
        boolean isAutomationBeingEnabled = !PiaPrefHandler.isNetworkManagementEnabled(context);

        // If the user is enabling the feature but we don't have the permissions.
        // Take the user to the permission screen.
        if (isAutomationBeingEnabled && !TrustedWifiActivity.hasTheRequiredPermissions(context)) {
            context.startActivity(new Intent(getContext(), TrustedWifiActivity.class));
            return;
        }

        PiaPrefHandler.setNetworkManagementEnabled(context, isAutomationBeingEnabled);
        updateAutomationService(context);
        setupStates();
    }

    @Subscribe
    public void settingsUpdate(SettingsUpdateEvent event) {
        setupStates();
    }

    private void updateAutomationService(Context context) {
        if (PiaPrefHandler.isNetworkManagementEnabled(context)) {
            AutomationService.Companion.start(context);
        } else {
            AutomationService.Companion.stop(context);
        }
    }

    private void setupStates() {
        lIcons.removeAllViews();

        prepareKillSwitchWidget();
        prepareNetworkManagementToolWidget();
        preparePrivateBrowserWidget();
    }

    private void prepareNetworkManagementToolWidget() {
        Context context = getContext();
        if (PiaPrefHandler.getQuickSettingsNetwork(context) && !PiaPrefHandler.isFeatureActive(context, PiaPrefHandler.DISABLE_NMT_FEATURE_FLAG)) {
            View view = getView(QuickSettings.SETTING_NETWORK, PiaPrefHandler.isNetworkManagementEnabled(context));
            lIcons.addView(view);
            view.setOnClickListener(view12 -> onNetworkClicked(context));
        }
    }

    private void prepareKillSwitchWidget() {
        if (PiaPrefHandler.getQuickSettingsKillswitch(getContext())) {
            View view = getView(QuickSettings.SETTING_KILL_SWITCH, false);
            lIcons.addView(view);
            view.setOnClickListener(view1 -> onKillSwitchClicked());
        }
    }

    private void preparePrivateBrowserWidget() {
        if (PiaPrefHandler.getQuickSettingsPrivateBrowser(getContext())) {
            View view = getView(QuickSettings.SETTING_BROWSER, false);
            lIcons.addView(view);
            view.setOnClickListener(view13 -> onBrowserClicked());
        }
    }

    private View getView(QuickSettings viewType, boolean isActive) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_quick_settings_icon, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.weight = 1;

        view.setLayoutParams(lp);
        AppCompatImageView iconImage = view.findViewById(R.id.quick_settings_icon_image);

        TextView iconText = view.findViewById(R.id.quick_settings_icon_text);
        switch(viewType) {
            case SETTING_BROWSER:
                iconImage.setImageResource(R.drawable.ic_private_browser);
                iconText.setText(R.string.quick_settings_private_browser);
                break;
            case SETTING_NETWORK:
                iconImage.setImageResource(isActive ? R.drawable.ic_network_management_active :
                        R.drawable.ic_network_management_inactive);
                iconText.setText(R.string.menu_settings_automation);
                break;
            case SETTING_KILL_SWITCH:
                iconImage.setImageResource(R.drawable.ic_killswitch);
                iconText.setText(R.string.killswitch);
                break;

        }
        iconText.setVisibility(View.VISIBLE);
        return view;
    }
}
