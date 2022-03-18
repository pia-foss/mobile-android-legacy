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

package com.privateinternetaccess.android.ui.drawer;

import static android.app.Activity.RESULT_OK;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static com.privateinternetaccess.android.ui.drawer.ServerListActivity.RESULT_SERVER_CHANGED;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse.DedicatedIPInformation;
import com.privateinternetaccess.android.PIAApplication;
import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.model.events.ServerClickedEvent;
import com.privateinternetaccess.android.model.events.SeverListUpdateEvent;
import com.privateinternetaccess.android.model.events.TVAppBarExpandEvent;
import com.privateinternetaccess.android.model.listModel.ServerItem;
import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.handlers.PIAServerHandler;
import com.privateinternetaccess.android.pia.handlers.PIAServerHandler.ServerSortingType;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.handlers.ThemeHandler;
import com.privateinternetaccess.android.pia.interfaces.IVPN;
import com.privateinternetaccess.android.pia.model.events.VpnStateEvent;
import com.privateinternetaccess.android.pia.utils.DLog;
import com.privateinternetaccess.android.pia.utils.NetworkConnectionListener;
import com.privateinternetaccess.android.pia.utils.NetworkReceiver;
import com.privateinternetaccess.android.pia.utils.Prefs;
import com.privateinternetaccess.android.pia.utils.Toaster;
import com.privateinternetaccess.android.tunnel.PIAVpnStatus;
import com.privateinternetaccess.android.ui.adapters.ServerListAdapter;
import com.privateinternetaccess.android.ui.tv.views.ServerSelectionItemDecoration;
import com.privateinternetaccess.android.utils.DedicatedIpUtils;
import com.privateinternetaccess.core.model.PIAServer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ServerListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, NetworkConnectionListener {

    private static final String SERVER_SEARCH_KEY = "server_search";

    @Nullable
    @BindView(R.id.appbar)
    AppBarLayout appBar;
    @BindView(R.id.server_list_refresh_layout)
    SwipeRefreshLayout rvServerRefreshLayout;
    @BindView(android.R.id.list)
    RecyclerView rvServerList;

    @Nullable
    @BindView(R.id.server_select_no_results)
    ImageView ivNoResults;
    @Nullable
    @BindView(R.id.cancel_icon)
    ImageView ivCancelSearch;
    @Nullable
    @BindView(R.id.search)
    EditText etSearchBar;

    @Nullable
    @BindView(R.id.connect_server_list_progress_bar)
    View progressBar;
    @Nullable
    @BindView(R.id.header_icon_button)
    AppCompatImageView ivIconButton;

    private ServerListAdapter mAdapter;
    PIAServerHandler mHandler;

    private GridLayoutManager mLayoutManager;
    private final BroadcastReceiver receiver = new NetworkReceiver(this::isConnected);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(PIAApplication.isAndroidTV(getContext()) ?
                        R.layout.activity_tv_secondary_list : R.layout.fragment_select_server,
                container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!PIAApplication.isAndroidTV(getContext())) {
            ServerListActivity serverActivity = (ServerListActivity) getActivity();
            serverActivity.initAppBar();

            etSearchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    DLog.d("ServerListFragment", "Text updated");
                    searchRegions();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            ivCancelSearch.setOnClickListener(view1 -> etSearchBar.setText(""));

            ivCancelSearch.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requireContext().registerReceiver(receiver, new IntentFilter(CONNECTIVITY_ACTION));
        EventBus.getDefault().register(this);
        initView(getContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(receiver);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRefresh() {
        Context context = getContext();
        Function1<Error, Unit> completionCallback = error -> {
            rvServerRefreshLayout.setRefreshing(false);
            if (error == null) {
                mAdapter.itemsUpdated(getServerItems(context, getRegionsFilterSelection(context)));
            }
            return Unit.INSTANCE;
        };

        if (PIAFactory.getInstance().getVPN(context).isVPNActive()) {
            DLog.e("ServerListFragment", "Error when updating latencies. Connected to the VPN.");
            Toaster.l(context, getString(R.string.error_pinging_while_connected));
            completionCallback.invoke(new Error("Connected to the VPN"));
            return;
        }

        // Clear old latencies to indicate the user we are updating them
        List<ServerItem> serverItems = getServerItems(context, getRegionsFilterSelection(context));
        for (ServerItem serverItem : serverItems) {
            serverItem.setLatency(null);
        }
        mAdapter.itemsUpdated(serverItems);

        PIAServerHandler.getInstance(context).triggerFetchServers(error -> {
            // Stop the spinner after fetching the servers.
            rvServerRefreshLayout.setRefreshing(false);
            PIAServerHandler.getInstance(context).triggerLatenciesUpdate(completionCallback);
            return null;
        });
    }

    private void initView(Context context) {
        int selectedPosition = setUpAdapter(context);
        setAdapterPosition(selectedPosition);

        if (ivIconButton != null) {
            ivIconButton.setVisibility(View.GONE);
        }

        handleServerListUpdateState(context, PIAServerHandler.getServerListFetchState());
        searchRegions();
    }

    private void searchRegions() {
        if (!PIAApplication.isAndroidTV(getContext())) {
            ivCancelSearch.setVisibility(etSearchBar.getText().toString().length() == 0 ?
                    View.GONE : View.VISIBLE);

            if (mAdapter != null) {
                if (mAdapter.applySearch(etSearchBar.getText().toString()) == 0) {
                    ivNoResults.setVisibility(View.VISIBLE);
                } else {
                    ivNoResults.setVisibility(View.GONE);
                }
            }
        }
    }

    private void setAdapterPosition(int selectedPosition) {
        if (selectedPosition != -1) {
            if (!PIAApplication.isAndroidTV(getContext())) {
                rvServerList.scrollToPosition(selectedPosition);
                mAdapter.setmSelectedItem(selectedPosition);
            } else {
                mAdapter.setmConnectedItem(selectedPosition);
            }
        }
    }

    public int setUpAdapter(Context context, boolean reset, PIAServerHandler.ServerSortingType... types) {
        mHandler = PIAServerHandler.getInstance(getContext());
        DLog.d("ServerListFragment", "Types: " + types);

        List<ServerItem> serverItems = getServerItems(context, types);
        PIAServer selectedServer = mHandler.getSelectedRegion(context, true);
        int selectedPosition = -1;
        if (reset) {

            // Set a scroll position for non-DIP servers. DIP servers are always on top.
            // No need to set a position as we default to top.
            if (selectedServer != null && !selectedServer.isDedicatedIp()) {
                for (int idx = 0; idx < serverItems.size(); idx++) {
                    ServerItem serverItem = serverItems.get(idx);
                    if (serverItem.getKey().equals(selectedServer.getKey()) &&
                            serverItem.getIso().equals(selectedServer.getIso()) &&
                            serverItem.getFlagId() == mHandler.getFlagResource(selectedServer.getIso())) {
                        selectedPosition = idx;
                        break;
                    }
                }
            }

            //set up adapter
            mAdapter = new ServerListAdapter(serverItems, getActivity());
            mAdapter.setSearchMode(true);

            mLayoutManager = new GridLayoutManager(context, getResources().getInteger(R.integer.server_selection_grid_size));
            //Required on some versions to correctly set span count
            mLayoutManager.setSpanCount(getResources().getInteger(R.integer.server_selection_grid_size));
            mLayoutManager.requestLayout();
            mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            if (PIAApplication.isAndroidTV(context)) {
                mAdapter.setSearchMode(false);
                mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (position == 0)
                            return mLayoutManager.getSpanCount();

                        return 1;
                    }
                });
            }

            rvServerList.setLayoutManager(mLayoutManager);
            rvServerList.setHasFixedSize(true);
            rvServerList.addItemDecoration(getItemDecoration());
            rvServerList.setAdapter(mAdapter);
            rvServerRefreshLayout.setOnRefreshListener(this);
            ThemeHandler.Theme theme = ThemeHandler.getPrefTheme(getActivity());
            switch (theme) {
                case DAY:
                    rvServerRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white);
                    rvServerRefreshLayout.setColorSchemeResources(R.color.green);
                    break;
                case NIGHT:
                    rvServerRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.dark);
                    rvServerRefreshLayout.setColorSchemeResources(R.color.greendark20);
                    break;
            }
        } else {
            for (int i = 0; i < serverItems.size(); i++) {
                ServerItem item = serverItems.get(i);
                if (i == 0) {
                    item.setSelected(mHandler.isSelectedRegionAuto(context));
                } else {
                    if (selectedServer != null)
                        item.setSelected(item.getName().equals(selectedServer.getName()));
                    else {
                        item.setSelected(false);
                    }
                }
                serverItems.set(i, item);
            }
            mAdapter.notifyDataSetChanged();
        }
        return selectedPosition;
    }

    public ServerSortingType getSortedServerSortingTypeForId(Integer id) {
        ServerSortingType selectedSortingType = PIAServerHandler.ServerSortingType.NAME;
        for (ServerSortingType serverSortingType : ServerSortingType.values()) {
            if (serverSortingType.name().hashCode() == id) {
                selectedSortingType = serverSortingType;
                break;
            }
        }
        return selectedSortingType;
    }

    private ServerSortingType getRegionsFilterSelection(Context context) {
        String selectedServerSortingTypeName = Prefs.with(context).get(
                PiaPrefHandler.REGION_PREFERRED_SORTING,
                ServerSortingType.LATENCY.name()
        );
        return getSortedServerSortingTypeForId(selectedServerSortingTypeName.hashCode());
    }

    private int setUpAdapter(Context context) {
        ServerSortingType sortingType = getRegionsFilterSelection(context);
        int adapter = setUpAdapter(context, true, ServerSortingType.NAME);
        switch (sortingType) {
            case NAME:
                adapter = setUpAdapter(context, true, ServerSortingType.NAME);
                break;
            case LATENCY:
                adapter = setUpAdapter(context, true, ServerSortingType.LATENCY);
                break;
            case FAVORITES:
                adapter = setUpAdapter(context, true, ServerSortingType.NAME, ServerSortingType.FAVORITES);
                break;
        }
        return adapter;
    }

    private List<ServerItem> getServerItems(Context context, PIAServerHandler.ServerSortingType... types) {
        List<ServerItem> items = new ArrayList<>();
        PIAServer selectedServer = mHandler.getSelectedRegion(context, true);

        //add search option
        if (PIAApplication.isAndroidTV(context)) {
            items.add(new ServerItem(SERVER_SEARCH_KEY,
                    R.drawable.ic_search_tv,
                    getString(R.string.server_search),
                    "",
                    false,
                    true,
                    false,
                    false,
                    "",
                    false
            ));
        }

        //add auto option
        items.add(new ServerItem("",
                R.drawable.flag_world,
                context.getString(R.string.automatic_server_selection_main),
                "",
                mHandler.isSelectedRegionAuto(context),
                true,
                false,
                false,
                "",
                false
        ));

        //Add dedicated IPs
        if (PiaPrefHandler.getDedicatedIps(context).size() > 0) {
            List<DedicatedIPInformation> ipList = PiaPrefHandler.getDedicatedIps(context);

            for (int i = 0; i < ipList.size(); i++) {
                DedicatedIPInformation dip = ipList.get(i);
                PIAServer ps = DedicatedIpUtils.serverForDip(dip, context);
                if (ps == null) {
                    continue;
                }

                ServerItem item = new ServerItem(
                        dip.getIp(),
                        mHandler.getFlagResource(ps.getIso()),
                        ps.getName(),
                        ps.getIso(),
                        ps == selectedServer,
                        ps.isAllowsPF(),
                        ps.isGeo(),
                        false,
                        "",
                        ps.isDedicatedIp()
                );
                items.add(item);
            }
        }

        //Add other options
        for (PIAServer ps : mHandler.getServers(context, types)) {
            if (!PiaPrefHandler.isGeoServersEnabled(context) && ps.isGeo()) {
                continue;
            }

            items.add(new ServerItem(
                    ps.getKey(),
                    mHandler.getFlagResource(ps.getIso()),
                    ps.getName(),
                    ps.getIso(),
                    ps == selectedServer,
                    ps.isAllowsPF(),
                    ps.isGeo(),
                    ps.isOffline(),
                    ps.getLatency(),
                    ps.isDedicatedIp()
            ));
        }
        return items;
    }

    private RecyclerView.ItemDecoration getItemDecoration() {
        int spanCount = getResources().getInteger(R.integer.server_selection_grid_size);
        int spacing = 3;

        return new ServerSelectionItemDecoration(spanCount, spacing, 1);
    }

    private void handleServerListUpdateState(
            Context context,
            SeverListUpdateEvent.ServerListUpdateState event
    ) {
        if (context == null) {
            return;
        }

        switch (event) {
            case STARTED:
                // TV Layout specific
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                break;
            case FETCH_SERVERS_FINISHED:
            case GEN4_PING_SERVERS_FINISHED:
                // TV Layout specific
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                mAdapter.itemsUpdated(getServerItems(context, getRegionsFilterSelection(context)));
                break;
        }
    }

    @Subscribe
    public void serverListUpdateEvent(SeverListUpdateEvent event) {
        Context context = getContext();
        if (context == null) {
            return;
        }
        handleServerListUpdateState(context, event.getState());
    }

    @Subscribe
    public void updateState(final VpnStateEvent event) {
        if (PIAApplication.isAndroidTV(getContext()) && mAdapter != null) {
            mAdapter.handleVpnState(event);
        }
    }

    @Subscribe
    public void serverClicked(ServerClickedEvent event) {
        Context context = getContext();
        if (context == null) {
            return;
        }

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        String currentRegionKey = "";
        PIAServerHandler handler = PIAServerHandler.getInstance(context);

        List<DedicatedIPInformation> ipList = PiaPrefHandler.getDedicatedIps(context);
        for (DedicatedIPInformation dip : ipList) {
            if (dip.getIp() != null && dip.getIp().equals(event.getRegionKey())) {
                currentRegionKey = dip.getIp();
                break;
            }
        }

        if (TextUtils.isEmpty(currentRegionKey)) {
            for (PIAServer ps : handler.getServers().values()) {
                if (ps.getName().equals(event.getName())) {
                    currentRegionKey = ps.getKey();
                    break;
                }
            }
        }

        // Try also with the key of the objects
        if (TextUtils.isEmpty(currentRegionKey)) {
            for (PIAServer ps : handler.getServers().values()) {
                if (event.getId() == ps.getKey().hashCode()) {
                    currentRegionKey = ps.getKey();
                    break;
                }
            }
        }

        String previousRegionKey = "";
        PIAServer previousRegion = PIAVpnStatus.getLastConnectedRegion();
        if (previousRegion != null) {
            previousRegionKey =
                    previousRegion.isDedicatedIp() ?
                            previousRegion.getDedicatedIp() :
                            previousRegion.getKey();
        }

        if (!PIAApplication.isAndroidTV(context)) {
            activity.setResult(RESULT_OK);
            if (!currentRegionKey.equals(previousRegionKey)) {
                activity.setResult(RESULT_SERVER_CHANGED);
            }
            activity.overridePendingTransition(R.anim.right_to_left_exit, R.anim.left_to_right_exit);
            activity.finish();
        } else {
            IVPN vpn = PIAFactory.getInstance().getVPN(context);
            if (!currentRegionKey.equals(previousRegionKey) || !vpn.isVPNActive()) {
                vpn.start();
            }
        }
    }

    @Subscribe
    public void onAppBarEventRecieve(TVAppBarExpandEvent event) {
        if (!event.isOpen()) {
            appBar.setExpanded(false, true);
        } else {
            appBar.setExpanded(true, true);
        }
    }

    @Override
    public void isConnected(boolean isConnected) {
        mAdapter.setEnabled(isConnected);
    }
}
