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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse.DedicatedIPInformation;
import com.privateinternetaccess.android.PIAApplication;
import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.model.events.ServerClickedEvent;
import com.privateinternetaccess.android.model.listModel.ServerItem;
import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.handlers.PIAServerHandler;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.utils.NetworkConnectionListener;
import com.privateinternetaccess.android.pia.utils.NetworkReceiver;
import com.privateinternetaccess.android.pia.utils.Prefs;
import com.privateinternetaccess.android.ui.connection.MainActivity;
import com.privateinternetaccess.android.utils.DedicatedIpUtils;
import com.privateinternetaccess.core.model.PIAServer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuickConnectView extends FrameLayout implements NetworkConnectionListener {

    @BindView(R.id.quick_server_flag_1) AppCompatImageView ivFlag1;
    @BindView(R.id.quick_server_flag_2) AppCompatImageView ivFlag2;
    @BindView(R.id.quick_server_flag_3) AppCompatImageView ivFlag3;
    @BindView(R.id.quick_server_flag_4) AppCompatImageView ivFlag4;
    @BindView(R.id.quick_server_flag_5) AppCompatImageView ivFlag5;
    @BindView(R.id.quick_server_flag_6) AppCompatImageView ivFlag6;

    @BindView(R.id.quick_server_name_1) AppCompatTextView tvName1;
    @BindView(R.id.quick_server_name_2) AppCompatTextView tvName2;
    @BindView(R.id.quick_server_name_3) AppCompatTextView tvName3;
    @BindView(R.id.quick_server_name_4) AppCompatTextView tvName4;
    @BindView(R.id.quick_server_name_5) AppCompatTextView tvName5;
    @BindView(R.id.quick_server_name_6) AppCompatTextView tvName6;

    @BindView(R.id.quick_dip_1) AppCompatImageView ivDip1;
    @BindView(R.id.quick_dip_2) AppCompatImageView ivDip2;
    @BindView(R.id.quick_dip_3) AppCompatImageView ivDip3;
    @BindView(R.id.quick_dip_4) AppCompatImageView ivDip4;
    @BindView(R.id.quick_dip_5) AppCompatImageView ivDip5;
    @BindView(R.id.quick_dip_6) AppCompatImageView ivDip6;

    @BindView(R.id.quick_server_layout_1) ConstraintLayout lContainer1;
    @BindView(R.id.quick_server_layout_2) ConstraintLayout lContainer2;
    @BindView(R.id.quick_server_layout_3) ConstraintLayout lContainer3;
    @BindView(R.id.quick_server_layout_4) ConstraintLayout lContainer4;
    @BindView(R.id.quick_server_layout_5) ConstraintLayout lContainer5;
    @BindView(R.id.quick_server_layout_6) ConstraintLayout lContainer6;

    private ImageView[] flags;
    private AppCompatTextView[] names;
    private AppCompatImageView[] dips;
    private ServerItem[] servers;
    private ConstraintLayout[] containers;
    private boolean isNetworkAvailable;

    private final BroadcastReceiver receiver = new NetworkReceiver(this::isConnected);

    private static final int  MAX_QUICK_CONNECT_SERVERS= 6;

    public QuickConnectView(Context context) {
        super(context);
        init(context);
    }

    public QuickConnectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public QuickConnectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        context.registerReceiver(receiver, new IntentFilter(CONNECTIVITY_ACTION));
        inflate(context, R.layout.view_quick_connect, this);
        ButterKnife.bind(this, this);

        flags = new ImageView[6];
        names = new AppCompatTextView[6];
        dips = new AppCompatImageView[6];
        servers = new ServerItem[6];
        containers = new ConstraintLayout[6];
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);

        flags[0] = ivFlag1;
        flags[1] = ivFlag2;
        flags[2] = ivFlag3;
        flags[3] = ivFlag4;
        flags[4] = ivFlag5;
        flags[5] = ivFlag6;

        names[0] = tvName1;
        names[1] = tvName2;
        names[2] = tvName3;
        names[3] = tvName4;
        names[4] = tvName5;
        names[5] = tvName6;

        dips[0] = ivDip1;
        dips[1] = ivDip2;
        dips[2] = ivDip3;
        dips[3] = ivDip4;
        dips[4] = ivDip5;
        dips[5] = ivDip6;

        containers[0] = lContainer1;
        containers[1] = lContainer2;
        containers[2] = lContainer3;
        containers[3] = lContainer4;
        containers[4] = lContainer5;
        containers[5] = lContainer6;

        presentServers();
    }

    @Override
    protected void detachViewFromParent(View child) {
        super.detachViewFromParent(child);
        getContext().unregisterReceiver(receiver);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    private PIAServer getServer(String serverIdentifier) {
        if (serverIdentifier == null) {
            return null;
        }

        PIAServerHandler handler = PIAServerHandler.getInstance(getContext());

        // Look for it on the regular list of servers.
        for (PIAServer ps : handler.getServers(getContext(), PIAServerHandler.ServerSortingType.NAME)) {
            if (ps.getKey().equals(serverIdentifier)) {
                return ps;
            }
        }

        // Look for it on the DIP list of servers.
        for (DedicatedIPInformation dip : PiaPrefHandler.getDedicatedIps(getContext())) {
            PIAServer dipServer = DedicatedIpUtils.serverForDip(dip, getContext());
            if (dipServer != null &&
                    dipServer.getDedicatedIp() != null &&
                    dipServer.getDedicatedIp().equals(serverIdentifier)
            ) {
                return dipServer;
            }
        }

        return null;
    }

    private void populateServers() {
        int currentIndex = 0;

        List<ServerItem> validServers = new ArrayList<>();
        String[] savedServers = PiaPrefHandler.getQuickConnectList(getContext());
        PIAServerHandler handler = PIAServerHandler.getInstance(getContext());

        // Prepare the list of servers based on previous saved states.
        for (String serverName : savedServers) {
            PIAServer server = getServer(serverName);
            if (server == null) {
                continue;
            }

            // TODO: Stop using server names as identifiers.
            //  It will drop favourites when jumping languages.
            boolean isFavorite = PiaPrefHandler.isFavorite(getContext(), server.getName());
            if (server.isDedicatedIp()) {
                isFavorite = PiaPrefHandler.isFavorite(getContext(), server.getDedicatedIp());
            }

            if (!isFavorite) {
                String key = server.getKey();
                if (server.isDedicatedIp()) {
                    dips[currentIndex].setVisibility(View.VISIBLE);
                    key = server.getDedicatedIp();
                }
                currentIndex++;
                validServers.add(
                        new ServerItem(
                                key,
                                PIAServerHandler.getInstance(getContext()).getFlagResource(server.getIso()),
                                server.getName(),
                                server.getIso(),
                                false,
                                server.isAllowsPF(),
                                server.isGeo(),
                                server.isOffline(),
                                server.getLatency(),
                                server.isDedicatedIp()
                        )
                );
            }
        }

        // If there are empty spaces after favourites and recent connections.
        // Fill it with low latency endpoints and don't repeat countries.
        if (validServers.size() < MAX_QUICK_CONNECT_SERVERS) {
            List<String> countryIsoAdded = new ArrayList();
            for (PIAServer ps : handler.getServers(getContext(), PIAServerHandler.ServerSortingType.LATENCY)) {
                if (isServerInQuickConnectList(validServers, ps.getKey())) {
                    continue;
                }

                if (countryIsoAdded.contains(ps.getIso())) {
                    continue;
                }

                if (ps.isGeo() && !PiaPrefHandler.isGeoServersEnabled(getContext())) {
                    continue;
                }

                countryIsoAdded.add(ps.getIso());
                validServers.add(
                        new ServerItem(
                                ps.getKey(),
                                PIAServerHandler.getInstance(getContext()).getFlagResource(ps.getIso()),
                                ps.getName(),
                                ps.getIso(),
                                false,
                                ps.isAllowsPF(),
                                ps.isGeo(),
                                ps.isOffline(),
                                ps.getLatency(),
                                ps.isDedicatedIp()
                        )
                );
            }
        }

        for (int i = 0; i < MAX_QUICK_CONNECT_SERVERS; i++) {
            if (i < validServers.size()) {
                servers[i] = validServers.get(i);
            }
            else {
                servers[i] = null;
            }
        }
    }

    private void presentServers() {

        for (AppCompatImageView dip : dips) {
            dip.setVisibility(View.GONE);
        }

        populateServers();

        for (int i = 0; i < servers.length; i++) {
            if (servers[i] == null || servers[i].equals("")) {
                flags[i].setImageResource(R.drawable.ic_map_empty);
                containers[i].setOnClickListener(null);
            }
            else {
                flags[i].setContentDescription(servers[i].getName());
                flags[i].setImageResource(servers[i].getFlagId());
                names[i].setContentDescription(servers[i].getName());
                names[i].setText(servers[i].getIso());

                final String selectedRegion = servers[i].getKey();
                final String selectedRegionName = servers[i].getName();
                containers[i].setOnClickListener(view -> {
                    if (isNetworkAvailable) {
                        PIAServerHandler handler = PIAServerHandler.getInstance(getContext());

                        PIAServer oldRegion = handler.getSelectedRegion(getContext(), true);
                        String oldRegionName = oldRegion != null ? oldRegion.getKey() : "";
                        handler.saveSelectedServer(getContext(), selectedRegion);

                        EventBus.getDefault().post(new ServerClickedEvent(selectedRegionName, selectedRegionName.hashCode(), null));

                        if(!selectedRegion.equals(oldRegionName) ||
                                !PIAFactory.getInstance().getVPN(getContext()).isVPNActive()) {
                            Activity activity = getActivity();

                            if (activity != null && activity instanceof MainActivity) {
                                ((MainActivity)activity).startVPN(true, true);
                            }
                        }
                    }
                });
            }
        }
    }

    private boolean isServerInQuickConnectList(List<ServerItem> quickConnectList, String serverKey) {
        for (ServerItem serverItem : quickConnectList) {
            if (serverItem.getKey() == serverKey) {
                return true;
            }
        }
        return false;
    }

    @Subscribe
    public void onServerSelected(ServerClickedEvent event) {
        presentServers();
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    @Override
    public void isConnected(boolean isConnected) {
        isNetworkAvailable = isConnected;
    }
}
