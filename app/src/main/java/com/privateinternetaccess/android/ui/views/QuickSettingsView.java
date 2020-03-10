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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.interfaces.IVPN;
import com.privateinternetaccess.android.pia.utils.Prefs;
import com.privateinternetaccess.android.ui.drawer.TrustedWifiActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class QuickSettingsView extends FrameLayout {

    @BindView(R.id.quick_settings_theme_image) AppCompatImageView ivActiveTheme;
    @BindView(R.id.quick_settings_kill_switch_image) AppCompatImageView ivKillSwitch;
    @BindView(R.id.quick_settings_network_image) AppCompatImageView ivNetwork;

    @BindView(R.id.quick_settings_kill_switch_layout) LinearLayout lKillSwitch;
    @BindView(R.id.quick_settings_network_layout) LinearLayout lNetwork;

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
        setupStates();
    }

    @OnClick(R.id.quick_settings_kill_switch_layout)
    public void onKillSwitchClicked() {
        boolean killswitch = PiaPrefHandler.isKillswitchEnabled(getContext());

        if (killswitch) {
            Prefs.with(getContext()).set(PiaPrefHandler.KILLSWITCH, false);

            IVPN vpn = PIAFactory.getInstance().getVPN(getContext());
            if(vpn.isKillswitchActive()){
                vpn.stopKillswitch();
            }
        }
        else {
            Prefs.with(getContext()).set(PiaPrefHandler.KILLSWITCH, true);
        }

        setupStates();
    }

    @OnClick(R.id.quick_settings_network_layout)
    public void onNetworkClicked() {
        boolean networking = PiaPrefHandler.shouldConnectOnWifi(getContext());

        if (networking) {
            Prefs.with(getContext()).set(PiaPrefHandler.TRUST_WIFI, false);
        }
        else {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Prefs.with(getContext()).set(PiaPrefHandler.TRUST_WIFI, true);
            }
            else {
                Intent i = new Intent(getContext(), TrustedWifiActivity.class);
                getContext().startActivity(i);
            }
        }

        setupStates();
    }

    private void setupStates() {
        if (PiaPrefHandler.isKillswitchEnabled(getContext())) {
            ivKillSwitch.setImageResource(R.drawable.ic_kill_switch_active);
        }
        else {
            ivKillSwitch.setImageResource(R.drawable.ic_kill_switch_inactive);
        }

        if (PiaPrefHandler.shouldConnectOnWifi(getContext())) {
            ivNetwork.setImageResource(R.drawable.ic_network_management_active);
        }
        else {
            ivNetwork.setImageResource(R.drawable.ic_network_management_inactive);
        }
    }
}
