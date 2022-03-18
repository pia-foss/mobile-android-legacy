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

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.privateinternetaccess.android.PIAApplication;
import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.handlers.PIAServerHandler;
import com.privateinternetaccess.android.pia.model.events.VpnStateEvent;
import com.privateinternetaccess.android.pia.utils.NetworkConnectionListener;
import com.privateinternetaccess.android.pia.utils.NetworkReceiver;
import com.privateinternetaccess.android.wireguard.backend.GoBackend;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.blinkt.openvpn.core.ConnectionStatus;

/**
 * Created by hfrede on 12/18/17.
 */

public class ConnectionSlider extends FrameLayout implements NetworkConnectionListener {

    public static GoBackend wireguard;
    public static final String TAG = "ConnectionSlider";

    @BindView(R.id.connection_background) AppCompatImageView background;
    @Nullable @BindView(R.id.connection_focused) AppCompatImageView focusedImageView;
    @Nullable @BindView(R.id.connect_progress) ProgressBar progressBar;

    private final BroadcastReceiver receiver = new NetworkReceiver(this::isConnected);

    private static final long TAP_DELAY = 750;
    private long lastTap = 0L;

    public ConnectionSlider(Context context) {
        super(context);
        init(context);
    }

    public ConnectionSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ConnectionSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    public void init(Context context){
        context.registerReceiver(receiver, new IntentFilter(CONNECTIVITY_ACTION));

        if (wireguard == null) {
            wireguard = PIAApplication.getWireguard();
        }

        inflate(context, R.layout.view_connection_slider, this);
        ButterKnife.bind(this, this);

        setEnabled(true);

        updateState();
    }

    private void toggleVPN() {
        if (lastTap + TAP_DELAY > System.currentTimeMillis()) {
            return;
        }

        lastTap = System.currentTimeMillis();

        boolean isVpnDisconnected = !PIAFactory.getInstance().getVPN(getContext()).isVPNActive();
        if (isVpnDisconnected && PIAServerHandler.getInstance(getContext()).getServers().size() == 0) {
            EventBus.getDefault().postSticky(
                    new VpnStateEvent(
                            "CONNECT",
                            "No regions available",
                            R.string.failed_connect_status,
                            ConnectionStatus.LEVEL_NONETWORK
                    )
            );
            return;
        }

        if(isVpnDisconnected) {
            PIAFactory.getInstance().getVPN(getContext()).start(true);
        } else {
            PIAFactory.getInstance().getVPN(getContext()).stop(true);
        }
    }

    public void updateState(){
        ConnectionStatus status =
                EventBus.getDefault().getStickyEvent(VpnStateEvent.class).getLevel();
        switch (status) {
            case LEVEL_CONNECTED:
                background.setContentDescription(getContext().getString(R.string.wg_connected));
                background.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_connection_on)
                );
                break;
            case LEVEL_NOTCONNECTED:
                background.setContentDescription(getContext().getString(R.string.tap_to_connect));
                background.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_connection_off)
                );
                break;
            case LEVEL_NONETWORK:
            case LEVEL_AUTH_FAILED:
                background.setContentDescription(getContext().getString(R.string.api_check_failure_title));
                background.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_connection_error)
                );
                break;
            case LEVEL_VPNPAUSED:
            case UNKNOWN_LEVEL:
            case LEVEL_WAITING_FOR_USER_INPUT:
            case LEVEL_START:
            case LEVEL_CONNECTING_NO_SERVER_REPLY_YET:
            case LEVEL_CONNECTING_SERVER_REPLIED:
                background.setContentDescription(getContext().getString(R.string.wg_connecting));
                background.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_connection_connecting)
                );
                break;
        }
    }

    public void animateFocus(boolean focused) {
        // Android TV only
        if (focusedImageView == null) {
            return;
        }

        if (focused) {
            focusedImageView.setVisibility(View.VISIBLE);
        } else {
            focusedImageView.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateState(VpnStateEvent event) {
        updateState();
    }

    @Override
    protected void detachViewFromParent(View child) {
        super.detachViewFromParent(child);
        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void isConnected(boolean isConnected) {
        if (isConnected) {
            updateState();
            background.setOnClickListener(view -> toggleVPN());
        } else {
            background.setOnClickListener(null);
            background.setImageDrawable(
                    getResources().getDrawable(R.drawable.ic_connection_error)
            );
        }
    }
}
