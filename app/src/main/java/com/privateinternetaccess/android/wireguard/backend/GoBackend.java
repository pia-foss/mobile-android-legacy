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

package com.privateinternetaccess.android.wireguard.backend;

import static com.privateinternetaccess.android.pia.api.PiaApi.GEN4_MACE_ENABLED_DNS;
import static de.blinkt.openvpn.core.OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.collection.ArraySet;

import com.privateinternetaccess.android.BuildConfig;
import com.privateinternetaccess.android.PIAApplication;
import com.privateinternetaccess.android.PIAOpenVPNTunnelLibrary;
import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.model.events.VPNTrafficDataPointEvent;
import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.api.PIACertPinningAPI;
import com.privateinternetaccess.android.pia.api.PiaApi;
import com.privateinternetaccess.android.pia.handlers.PIAServerHandler;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.model.events.VpnStateEvent;
import com.privateinternetaccess.android.pia.providers.VPNFallbackEndpointProvider;
import com.privateinternetaccess.android.pia.receivers.PortForwardingReceiver;
import com.privateinternetaccess.android.pia.utils.DLog;
import com.privateinternetaccess.android.pia.utils.Prefs;
import com.privateinternetaccess.android.tunnel.PIAVpnStatus;
import com.privateinternetaccess.android.tunnel.PortForwardingStatus;
import com.privateinternetaccess.android.ui.connection.MainActivity;
import com.privateinternetaccess.android.ui.notifications.PIANotifications;
import com.privateinternetaccess.android.utils.DedicatedIpUtils;
import com.privateinternetaccess.android.utils.SnoozeUtils;
import com.privateinternetaccess.android.wireguard.config.Config;
import com.privateinternetaccess.android.wireguard.config.InetNetwork;
import com.privateinternetaccess.android.wireguard.config.Interface;
import com.privateinternetaccess.android.wireguard.config.Peer;
import com.privateinternetaccess.android.wireguard.crypto.Key;
import com.privateinternetaccess.android.wireguard.crypto.KeyFormatException;
import com.privateinternetaccess.android.wireguard.crypto.KeyPair;
import com.privateinternetaccess.android.wireguard.model.Tunnel;
import com.privateinternetaccess.android.wireguard.model.Tunnel.State;
import com.privateinternetaccess.android.wireguard.model.Tunnel.Statistics;
import com.privateinternetaccess.android.wireguard.util.SharedLibraryLoader;
import com.privateinternetaccess.core.model.PIAServer;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.blinkt.openvpn.core.ConnectionStatus;
import kotlin.Pair;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class GoBackend implements Backend {
    private String IPV4_PUBLIC_NETWORKS =
            "0.0.0.0/5, 8.0.0.0/7, 11.0.0.0/8, 12.0.0.0/6, 16.0.0.0/4, 32.0.0.0/3, " +
                    "64.0.0.0/2, 128.0.0.0/3, 160.0.0.0/5, 168.0.0.0/6, 172.0.0.0/12, " +
                    "172.32.0.0/11, 172.64.0.0/10, 172.128.0.0/9, 173.0.0.0/8, 174.0.0.0/7, " +
                    "176.0.0.0/4, 192.0.0.0/9, 192.128.0.0/11, 192.160.0.0/13, 192.169.0.0/16, " +
                    "192.170.0.0/15, 192.172.0.0/14, 192.176.0.0/12, 192.192.0.0/10, " +
                    "193.0.0.0/8, 194.0.0.0/7, 196.0.0.0/6, 200.0.0.0/5, 208.0.0.0/4";

    private static final String TAG = "WireGuard/" + GoBackend.class.getSimpleName();
    private static GhettoCompletableFuture<VpnService> vpnService = new GhettoCompletableFuture<>();
    private static final long USAGE_INTERVAL_MS  = 4000;
    private static final long RECONNECT_INITIAL_DELAY_MS = 5000;
    private static final long RECONNECT_RETRY_INTERVAL_MS = 30000;
    @Nullable private static AlwaysOnCallback alwaysOnCallback;
    public static final String WG_HANDSHAKE = "Noise_IK";

    private final Context context;
    @Nullable private Tunnel currentTunnel;
    private int currentTunnelHandle = -1;
    public boolean isConnecting = false;

    private AlarmManager alarmManager;
    private PendingIntent portForwardingIntent;

    public GoBackend(final Context context) {
        SharedLibraryLoader.loadSharedLibrary(context, "wg-go");
        this.context = context;
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    private static native String wgGetConfig(int handle);

    private static native int wgGetSocketV4(int handle);

    private static native int wgGetSocketV6(int handle);

    private static native void wgTurnOff(int handle);

    private static native int wgTurnOn(String ifName, int tunFd, String settings);

    private static native String wgVersion();

    static public State lastState = State.DOWN;

    public static void setAlwaysOnCallback(final AlwaysOnCallback cb) {
        alwaysOnCallback = cb;
    }

    @Override
    public Set<String> getRunningTunnelNames() {
        if (currentTunnel != null) {
            final Set<String> runningTunnels = new ArraySet<>();
            runningTunnels.add(currentTunnel.getName());
            return runningTunnels;
        }
        return Collections.emptySet();
    }

    @Override
    public State getState(final Tunnel tunnel) {
        return currentTunnel == tunnel ? State.UP : State.DOWN;
    }

    @Override
    public Statistics getStatistics(final Tunnel tunnel) {
        final Statistics stats = new Statistics();
        if (tunnel != currentTunnel) {
            return stats;
        }
        final String config = wgGetConfig(currentTunnelHandle);

        if (config == null) {
            return stats;
        }

        Key key = null;
        long rx = 0, tx = 0;
        for (final String line : config.split("\\n")) {
            if (line.startsWith("public_key=")) {
                if (key != null)
                    stats.add(key, rx, tx);
                rx = 0;
                tx = 0;
                try {
                    key = Key.fromHex(line.substring(11));
                } catch (final KeyFormatException ignored) {
                    key = null;
                }
            } else if (line.startsWith("rx_bytes=")) {
                if (key == null)
                    continue;
                try {
                    rx = Long.parseLong(line.substring(9));
                } catch (final NumberFormatException ignored) {
                    rx = 0;
                }
            } else if (line.startsWith("tx_bytes=")) {
                if (key == null)
                    continue;
                try {
                    tx = Long.parseLong(line.substring(9));
                } catch (final NumberFormatException ignored) {
                    tx = 0;
                }
            }
        }
        if (key != null)
            stats.add(key, rx, tx);
        return stats;
    }

    @Override
    public String getVersion() {
        return wgVersion();
    }

    @Override
    public State setState(
            VPNFallbackEndpointProvider.VPNEndpoint endpoint,
            final Tunnel tunnel,
            State state
    ) throws Exception {
        final State originalState = getState(tunnel);
        if (state == State.TOGGLE)
            state = originalState == State.UP ? State.DOWN : State.UP;
        if (state == originalState)
            return originalState;
        if (state == State.UP && currentTunnel != null)
            throw new IllegalStateException("Only one userspace tunnel can run at a time");
        Log.d(TAG, "Changing tunnel " + tunnel.getName() + " to state " + state);
        setStateInternal(endpoint, tunnel, tunnel.getConfig(), state);
        return getState(tunnel);
    }

    private void setStateInternal(
            VPNFallbackEndpointProvider.VPNEndpoint endpoint,
            final Tunnel tunnel,
            @Nullable final Config config,
            final State state
    ) throws Exception {
        PiaPrefHandler.clearLastIPVPN(context);
        if (state == State.UP) {
            Log.i(TAG, "Bringing tunnel up");

            Objects.requireNonNull(config, "Trying to bring up a tunnel with no config");

            if (VpnService.prepare(context) != null)
                throw new Exception("VPN service not authorized by user");

            if (currentTunnelHandle != -1) {
                Log.w(TAG, "Tunnel already up");
                return;
            }

            if (!vpnService.isDone()) {
                try {
                    startVpnService();
                } catch (SecurityException | IllegalStateException exception) {
                    DLog.w("Wireguard", "Error starting VPN service " + exception);
                    throw exception;
                }
            }

            final VpnService service;
            try {
                DLog.d("Wireguard", "Waiting for service");
                service = vpnService.get(2, TimeUnit.SECONDS);
            } catch (final TimeoutException e) {
                DLog.w("Wireguard", "Waiting for service reached the timeout " + e);
                throw new Exception("Unable to start Android VPN service", e);
            }

            // Android expect us to invoke `startForeground` after starting the service.
            // So, let's do it as soon as possible. If an error happens below.
            // The state will not be set as UP.
            service.showNotification("Connected", "Connected", ConnectionStatus.LEVEL_CONNECTED);

            // Representing any potential error when setting up the VPN interface below.
            Error error = null;

            // Build config
            final String goConfig = config.toWgUserspaceString();

            // Create the vpn tunnel with android API
            final VpnService.Builder builder = service.getBuilder();
            builder.setSession(tunnel.getName());

            setDisallowedApps(builder);

            final Intent configureIntent = new Intent(context, MainActivity.class);
            configureIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            int flags;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags = PendingIntent.FLAG_IMMUTABLE;
            } else {
                flags = 0;
            }
            builder.setConfigureIntent(PendingIntent.getActivity(context, 0, configureIntent, flags));

            for (final String excludedApplication : config.getInterface().getExcludedApplications())
                builder.addDisallowedApplication(excludedApplication);

            for (final InetNetwork addr : config.getInterface().getAddresses())
                builder.addAddress(addr.getAddress(), addr.getMask());

            for (final InetAddress addr : config.getInterface().getDnsServers()) {
                DLog.d("Wireguard", "DNS: " + addr.getHostAddress());
                builder.addDnsServer(addr.getHostAddress());
            }

            for (final Peer peer : config.getPeers()) {
                for (final InetNetwork addr : peer.getAllowedIps())
                    builder.addRoute(addr.getAddress(), addr.getMask());
            }

            builder.setMtu(config.getInterface().getMtu().orElse(1280));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                builder.setMetered(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                service.setUnderlyingNetworks(null);

            builder.setBlocking(true);

            // Create the VPN interface with the builder we have prepared.
            try {
                ParcelFileDescriptor tun = builder.establish();
                if (tun == null) {
                    error = new Error("TUN_CREATION_ERROR");
                } else {
                    Log.d(TAG, "Go backend v" + wgVersion());
                    currentTunnelHandle = wgTurnOn(tunnel.getName(), tun.detachFd(), goConfig);
                    if (currentTunnelHandle < 0) {
                        error = new Error("GO_ACTIVATION_ERROR_CODE");
                    }
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
                error = new Error("Error preparing the VPN interface " + e);
            }

            // If an error happened when creating the VPN interface or preparing the tunnel above.
            // Avoid announcing the connected state below or setting up the last know state as UP.
            // And, hide the service notification.
            if (error != null) {
                DLog.w("Wireguard", "Error preparing the VPN interface " + error);
                service.hideNotification();
                return;
            }

            currentTunnel = tunnel;
            service.protect(wgGetSocketV4(currentTunnelHandle));
            service.protect(wgGetSocketV6(currentTunnelHandle));
            VpnService.backend = this;

            //PIA Specific up logic
            service.setActiveTunnel(endpoint, currentTunnel);

            // Announce the connected state
            VpnStateEvent event = new VpnStateEvent(
                    "CONNECT",
                    "Wireguard Connect",
                    R.string.connected,
                    ConnectionStatus.LEVEL_CONNECTED
            );
            EventBus.getDefault().postSticky(event);
            SnoozeUtils.resumeVpn(context, false);

            // Persist the gateway for port forwarding purposes.
            // This needs to happen before starting the port forwarding.
            if (currentTunnel.getConfig() != null) {
                PiaPrefHandler.setGatewayEndpoint(context, currentTunnel.getConfig().getInterface().getGateway());
            }

            // Start port forwarding if enabled.
            // This needs tp happen after persisting the tunnel's gateway.
            if (PiaPrefHandler.isPortForwardingEnabled(context)) {
                startPortForwarding();
            }

            lastState = state;
            isConnecting = false;
        } else {
            disconnect();
        }
    }

    public void disconnect() throws Exception {
        Log.i(TAG, "Bringing tunnel down");

        if (currentTunnelHandle == -1) {
            Log.w(TAG, "Tunnel already down");
            return;
        }

        wgTurnOff(currentTunnelHandle);
        currentTunnel = null;
        currentTunnelHandle = -1;

        //PIA Specific down logic
        VpnService.activeTunnel = null;

        VpnStateEvent event = new VpnStateEvent(
                "CONNECT",
                "Wireguard Connect",
                R.string.state_exiting,
                ConnectionStatus.LEVEL_NOTCONNECTED
        );
        EventBus.getDefault().postSticky(event);

        PiaPrefHandler.clearGatewayEndpoint(context);
        clearPortForwarding();

        lastState = State.DOWN;
        isConnecting = false;

        try {
            VpnService service = vpnService.get(2, TimeUnit.SECONDS);
            service.hideNotification();
            VpnService.backend = null;
            vpnService = vpnService.newIncompleteFuture();
        } catch (final TimeoutException e) {
            throw new Exception("Unable to start Android VPN service", e);
        }
    }

    private void startVpnService() throws SecurityException, IllegalStateException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(new Intent(context, VpnService.class));
        else
            context.startService(new Intent(context, VpnService.class));
    }

    private void clearPortForwarding() {
        PiaPrefHandler.clearGatewayEndpoint(context);
        PIAVpnStatus.setPortForwardingStatus(PortForwardingStatus.NO_PORTFWD, "");
        PiaPrefHandler.clearBindPortForwardInformation(context);
        if (portForwardingIntent == null) {
            return;
        }
        alarmManager.cancel(portForwardingIntent);
        portForwardingIntent = null;
    }

    private void startPortForwarding() {
        PIAVpnStatus.setPortForwardingStatus(
                PortForwardingStatus.REQUESTING,
                context.getString(R.string.requestingportfw)
        );
        if (portForwardingIntent != null) {
            return;
        }

        Intent intent = new Intent(context, PortForwardingReceiver.class);
        int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT;
        } else {
            flags = PendingIntent.FLAG_CANCEL_CURRENT;
        }
        portForwardingIntent = PendingIntent.getBroadcast(
                context, 0, intent, flags
        );
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                0,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                portForwardingIntent
        );
    }

    public interface AlwaysOnCallback {
        void alwaysOnTriggered();
    }

    public void startVpn(VPNFallbackEndpointProvider.VPNEndpoint endpoint) {
        if (PIAApplication.wireguardTunnel != null &&
                !PiaPrefHandler.hasDnsChanged(context)) {
            if (PIAApplication.wireguardServer != null &&
                    !endpoint.getKey().equals(PIAApplication.wireguardServer.getKey())) {
                startWireguardService(endpoint);
            }
            else {
                try {
                    isConnecting = true;
                    setState(endpoint, PIAApplication.wireguardTunnel, State.UP);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            startWireguardService(endpoint);
        }
    }

    public void stopVpn() {
        stopVpn(false);
    }

    public void stopVpn(boolean killTunnel) {
        if (PIAApplication.wireguardTunnel != null) {
            try {
                setState(null, PIAApplication.wireguardTunnel, State.DOWN);

                if (killTunnel) {
                    PIAApplication.wireguardTunnel = null;
                    currentTunnel = null;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void cancel() {
        VpnStateEvent event = new VpnStateEvent("CONNECT", "Wireguard Cancel",
                de.blinkt.openvpn.R.string.state_waitconnectretry, ConnectionStatus.LEVEL_NOTCONNECTED);
        EventBus.getDefault().postSticky(event);
        lastState = State.DOWN;
        isConnecting = false;
    }

    public boolean isActive() {
        if (lastState == State.UP || isConnecting) {
            return true;
        }

        return false;
    }

    private void startWireguardService(VPNFallbackEndpointProvider.VPNEndpoint endpoint) {
        PIAServerHandler handler = PIAServerHandler.getInstance(context);
        final PIAServer server = handler.getSelectedRegion(context, false);

        PiaPrefHandler.setDnsChanged(context, false);

        if (PIAApplication.wireguardTunnel != null &&
                PIAApplication.wireguardServer != null &&
                !PIAApplication.wireguardServer.getKey().equals(endpoint.getKey())) {
            try {
                setState(endpoint, PIAApplication.wireguardTunnel, State.DOWN);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        // The condition above is tearing down an active connection if this method is called
        // while connected. Thus, it is important to report the connecting after that condition.
        EventBus.getDefault().postSticky(new VpnStateEvent(
                "CONNECT",
                "Wireguard Connecting",
                R.string.connecting,
                ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET
        ));

        if (endpoint.getEndpoint() == null || endpoint.getEndpoint().length() <= 0) {
            VpnStateEvent event = new VpnStateEvent(
                    "CONNECT",
                    "Wireguard Failed",
                    R.string.failed_connect_status,
                    ConnectionStatus.LEVEL_NONETWORK
            );
            EventBus.getDefault().postSticky(event);
            return;
        }

        String[] splitPort = endpoint.getEndpoint().split(":");
        String host = splitPort[0];
        int port = Integer.parseInt(splitPort[1]);

        final KeyPair wgKeyPair = new KeyPair();
        isConnecting = true;

        try {
            String vpnToken = PIAFactory.getInstance().getAccount(context).vpnToken();
            if (TextUtils.isEmpty(vpnToken)) {
                VpnStateEvent event = new VpnStateEvent(
                        "CONNECT",
                        "Wireguard Connect",
                        R.string.failed_connect_status,
                        ConnectionStatus.LEVEL_NONETWORK
                );
                EventBus.getDefault().postSticky(event);
                return;
            }

            if (server.isDedicatedIp()) {
                vpnToken = "dedicated_ip_" + server.getDipToken() + "_" + DedicatedIpUtils.randomAlphaNumeric(8);
            }

            HttpUrl httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(host)
                    .port(port)
                    .addPathSegment("addKey")
                    .addQueryParameter("pubkey", wgKeyPair.getPublicKey().toBase64())
                    .addQueryParameter("pt", vpnToken)
                    .build();

            Request request = new Request.Builder().url(httpUrl).build();
            PIACertPinningAPI piaApi = new PIACertPinningAPI();

            // Set the endpoints/cn for the selected protocol before the request
            List<Pair<String, String>> endpointCommonNames = new ArrayList<>();
            endpointCommonNames.add(new Pair<>(host, endpoint.getCommonName()));
            piaApi.setKnownEndpointCommonName(endpointCommonNames);

            OkHttpClient client = piaApi.getOkHttpClient();
            DLog.d("Wireguard", "httpUrl: " + httpUrl.toString());
            DLog.d("Wireguard", "Attempting call");
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    DLog.d("Wireguard", "Failed call");
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    int status = response.code();
                    DLog.d("Wireguard", "status = " + status);
                    String res = response.body().string();
                    DLog.d("Wireguard", "body = " + res);

                    try {
                        JSONObject jsonResponse = new JSONObject(res);
                        Config.Builder wgConfigBuilder = new Config.Builder();
                        wgConfigBuilder.addPeer(Peer.parse(generatePeer(jsonResponse)));
                        wgConfigBuilder.setInterface(Interface.parse(generateInterface(wgKeyPair, jsonResponse)));

                        Tunnel testTunnel = new Tunnel("PIATunnel", wgConfigBuilder.build(), State.DOWN);
                        PIAApplication.wireguardTunnel = testTunnel;
                        PIAApplication.wireguardServer = endpoint.getWireguardServer();
                        setState(endpoint, testTunnel, State.UP);
                    }
                    catch (Throwable e) {
                        e.printStackTrace();
                        EventBus.getDefault().postSticky(
                                new VpnStateEvent(
                                        "CONNECT",
                                        "Wireguard Connect",
                                        R.string.failed_connect_status,
                                        ConnectionStatus.LEVEL_NONETWORK
                                )
                        );
                        try {
                            disconnect();
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
            VpnStateEvent event = new VpnStateEvent(
                    "CONNECT",
                    "Wireguard Connect",
                    R.string.failed_connect_status,
                    ConnectionStatus.LEVEL_NONETWORK
            );
            EventBus.getDefault().postSticky(event);
        }
    }

    private List<String> generateInterface(KeyPair keys, JSONObject response) throws JSONException {
        List<String> wgSettings = new ArrayList<>();
        Object dns = response.getJSONArray("dns_servers").get(0);

        wgSettings.add("privatekey=" + keys.getPrivateKey().toBase64());

        if (PiaPrefHandler.isCustomDnsSelected(context)) {
            String customPrimaryDns = PiaPrefHandler.getPrimaryDns(context);
            if (TextUtils.isEmpty(customPrimaryDns)) {
                customPrimaryDns = dns.toString();
            }
            wgSettings.add("dns=" + customPrimaryDns);
        }
        else {
            if (PiaPrefHandler.isMaceEnabled(context) && !BuildConfig.FLAVOR_store.equals("playstore")) {
                dns = GEN4_MACE_ENABLED_DNS;
            }
            wgSettings.add("dns=" + dns);
        }

        if (PiaPrefHandler.getWireguardSmallPacketSizeEnabled(context)) {
            wgSettings.add("mtu=1280");
        } else {
            wgSettings.add("mtu=1420");
        }

        wgSettings.add("address=" + response.getString("peer_ip"));
        wgSettings.add("gateway=" + response.getString("server_vip"));

        return wgSettings;
    }

    private List<String> generatePeer(JSONObject response) throws JSONException {
        List<String> wgSettings = new ArrayList<>();

        wgSettings.add("publickey=" + response.getString("server_key"));
        wgSettings.add("endpoint=" + response.getString("server_ip") + ":" + response.getString("server_port"));
        wgSettings.add("persistentkeepalive=25");

        if (PiaPrefHandler.isAllowLocalLanEnabled(context)) {
            if (PiaPrefHandler.isMaceEnabled(context)) {
                wgSettings.add("allowedips=" + getAllowedIps(GEN4_MACE_ENABLED_DNS));
            }
            else {
                wgSettings.add("allowedips=" + getAllowedIps(response.getJSONArray("dns_servers").get(0).toString()));
            }
        } else {
            wgSettings.add("allowedips=0.0.0.0/0");
        }

        return wgSettings;
    }

    private String getAllowedIps(String dnsServer) {
        if (dnsServer != null) {
            return IPV4_PUBLIC_NETWORKS + "," + dnsServer;
        }
        else {
            return IPV4_PUBLIC_NETWORKS + "10.0.0.240/29";
        }
    }

    private void setDisallowedApps(VpnService.Builder builder) {
        Prefs prefs = new Prefs(context);
        boolean atLeastOneAllowedApp = false;

        HashSet<String> allowedAppsVpn = new HashSet<>(PiaPrefHandler.getVpnExcludedApps(context));
        boolean allowedAppsVpnAreDisallowed = !prefs.getBoolean(PiaPrefHandler.VPN_PER_APP_ARE_ALLOWED);

        if (!allowedAppsVpnAreDisallowed)
            allowedAppsVpn.add(context.getPackageName());

        for (String pkg : allowedAppsVpn) {
            try {
                if (allowedAppsVpnAreDisallowed) {
                    builder.addDisallowedApplication(pkg);
                } else {
                    builder.addAllowedApplication(pkg);
                    atLeastOneAllowedApp = true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                allowedAppsVpn.remove(pkg);
            }
        }

        if (!allowedAppsVpnAreDisallowed && !atLeastOneAllowedApp) {
            try {
                builder.addAllowedApplication(context.getPackageName());
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // TODO: When we finally drop API 21 and move to API 24, delete this and replace with the ordinary CompletableFuture.
    public static final class GhettoCompletableFuture<V> {
        private final LinkedBlockingQueue<V> completion = new LinkedBlockingQueue<>(1);
        private final FutureTask<V> result = new FutureTask<>(completion::peek);

        public boolean complete(final V value) {
            if (value == null)
                return false;

            final boolean offered = completion.offer(value);
            if (offered)
                result.run();
            return offered;
        }

        public V get() throws ExecutionException, InterruptedException {
            return result.get();
        }

        public V get(final long timeout, final TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
            return result.get(timeout, unit);
        }

        public boolean isDone() {
            return !completion.isEmpty();
        }

        public GhettoCompletableFuture<V> newIncompleteFuture() {
            return new GhettoCompletableFuture<>();
        }
    }

    public static class VpnService extends android.net.VpnService {
        public Builder getBuilder() {
            return new Builder();
        }
        public static Tunnel activeTunnel;
        @Nullable public static GoBackend backend;

        private Handler usageHandler;
        private Runnable usageRunnable;

        static private Handler reconnectHandler;
        static private Runnable reconnectRunnable;

        private Statistics prevStats;

        private int staleCount = 0;

        @Override
        public void onCreate() {
            DLog.d("Wireguard", "Service created");
            vpnService.complete(this);
            super.onCreate();
        }

        @Override
        public void onDestroy() {
            if (backend != null) {
                try {
                    backend.disconnect();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            super.onDestroy();
        }

        @Override
        public int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
            vpnService.complete(this);

            if (intent == null || intent.getComponent() == null || !intent.getComponent().getPackageName().equals(getPackageName())) {
                Log.d(TAG, "Service started by Always-on VPN feature");
                if (alwaysOnCallback != null)
                    alwaysOnCallback.alwaysOnTriggered();
            }

            return super.onStartCommand(intent, flags, startId);
        }

        private void setActiveTunnel(VPNFallbackEndpointProvider.VPNEndpoint endpoint, Tunnel tunnel) {
            activeTunnel = tunnel;

            if (reconnectHandler != null) {
                if (reconnectRunnable != null) {
                    reconnectHandler.removeCallbacks(reconnectRunnable);
                }

                reconnectHandler = null;
            }

            if (activeTunnel != null) {
                startUsageMonitor(endpoint);
            }
            else {
                stopUsageMonitor();
            }
        }

        private void stopUsageMonitor() {
            if (usageRunnable != null)
                usageHandler.removeCallbacks(usageRunnable);

            usageRunnable = null;
            usageHandler = null;
        }

        private void startUsageMonitor(VPNFallbackEndpointProvider.VPNEndpoint endpoint) {
            usageRunnable = new Runnable() {
                @Override
                public void run() {
                    if (backend == null || activeTunnel == null) {
                        return;
                    }

                    Statistics stats = backend.getStatistics(activeTunnel);

                    if (prevStats == null) {
                        prevStats = stats;
                    }

                    VPNTrafficDataPointEvent traffic = new VPNTrafficDataPointEvent(
                            stats.totalRx(),
                            stats.totalTx(),
                            stats.totalRx() - prevStats.totalRx(),
                            stats.totalTx() - prevStats.totalTx());

                    if (stats.totalRx() - prevStats.totalRx() == 0) {
                        staleCount += 1;

                        if (staleCount > 3) {
                            Request request = new Request.Builder().url("http://" + endpoint.getEndpoint()).
                                    build();

                            PiaApi piaApi = new PiaApi();
                            OkHttpClient client = piaApi.getOkHttpClient();
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {

                                }
                            });
                        }

                        if (staleCount > 5) {
                            staleCount = 0;
                            startReconnect(endpoint);

                            prevStats = null;
                            return;
                        }
                    }
                    else {
                        staleCount = 0;
                    }

                    EventBus.getDefault().postSticky(traffic);

                    if (usageHandler != null) {
                        usageHandler.postDelayed(usageRunnable, USAGE_INTERVAL_MS);
                    }

                    prevStats = stats;
                }
            };

            usageHandler = new Handler(Looper.getMainLooper());
            usageHandler.postDelayed(usageRunnable, USAGE_INTERVAL_MS);
        }

        private void startReconnect(VPNFallbackEndpointProvider.VPNEndpoint endpoint) {
            final GoBackend reconnectBackend = backend;
            if (reconnectBackend != null && activeTunnel != null) {
                try {
                    reconnectBackend.setState(endpoint, activeTunnel, State.DOWN);
                    PIAApplication.wireguardTunnel = null;
                    stopUsageMonitor();

                    reconnectHandler = new Handler(Looper.getMainLooper());
                    reconnectRunnable = () -> {
                        reconnectBackend.startVpn(endpoint);
                        reconnectHandler.postDelayed(reconnectRunnable, RECONNECT_RETRY_INTERVAL_MS);
                    };
                    reconnectHandler.postDelayed(reconnectRunnable, RECONNECT_INITIAL_DELAY_MS);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private int getIconByConnectionStatus(ConnectionStatus level) {
            return PIAOpenVPNTunnelLibrary.mNotifications.getIconByConnectionStatus(level);
        }

        private int getColorByConnectionStatus(ConnectionStatus level) {
            return PIAOpenVPNTunnelLibrary.mNotifications.getColorByConnectionStatus(this, level);
        }

        public void hideNotification() {
            stopUsageMonitor();
            stopForeground(true);
            stopSelf();
        }

        public void showNotification(
                final String msg,
                String tickerText,
                ConnectionStatus status
        ) {
            PIAServer server = PIAApplication.wireguardServer;
            String contentTitle = "";
            if (server != null) {
                contentTitle =
                        getString(de.blinkt.openvpn.R.string.notifcation_title, server.getName());
            }
            int notificationId = NOTIFICATION_CHANNEL_NEWSTATUS_ID.hashCode();
            int flags;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags = PendingIntent.FLAG_IMMUTABLE;
            } else {
                flags = 0;
            }
            Notification notification = PIANotifications.Companion.getSharedInstance().showNotification(
                    this,
                    notificationId,
                    NOTIFICATION_CHANNEL_NEWSTATUS_ID,
                    contentTitle,
                    msg,
                    tickerText,
                    getIconByConnectionStatus(status),
                    getColorByConnectionStatus(status),
                    true,
                    PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), flags)
            );

            startForeground(notificationId, notification);
        }
    }
}
