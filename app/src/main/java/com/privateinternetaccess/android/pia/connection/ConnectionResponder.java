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

package com.privateinternetaccess.android.pia.connection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.privateinternetaccess.android.PIAKillSwitchStatus;
import com.privateinternetaccess.android.PIAOpenVPNTunnelLibrary;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.receivers.PortForwardingReceiver;
import com.privateinternetaccess.android.pia.utils.DLog;
import com.privateinternetaccess.android.tunnel.PIAVpnStatus;
import com.privateinternetaccess.android.tunnel.PortForwardingStatus;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.VpnStatus;

import static de.blinkt.openvpn.core.OpenVpnManagementThread.GATEWAY;

/**
 *
 * Use this to handle all connection features.
 *
 * You can toggle all of these features with {@link com.privateinternetaccess.android.pia.PIABuilder} or {@link PiaPrefHandler} methods.
 *
 *
 * Change log:
 *
 * Created by arne on 10.10.13.
 * Updated by half47 a while later.
 * Completely changed by half47 an even while later.
 * Fixed by arne on 13.6.17.
 * Fixed mace not working by half47 4/18 and documented more.
 *
 *
 */
public class ConnectionResponder implements VpnStatus.StateListener, PIAKillSwitchStatus.KillSwitchStateListener {

    public static final String TAG = "ConnectionResponder";

    private Context context;
    private static ConnectionResponder mInstance;

    private AlarmManager alarmManager;
    private PendingIntent portForwardingIntent;

    static ThreadPoolExecutor executor;
    static BlockingQueue<Runnable> workQueue;

    private static int REQUESTING_PORT_STRING;
    public static boolean VPN_REVOKED;

    private ConnectionResponder(Context c, int resId) {
        context = c;
        REQUESTING_PORT_STRING = resId;
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PIAKillSwitchStatus.addKillSwitchListener(this);
    }

    public static ConnectionResponder initConnection(Context c, int resID) {
        if (mInstance == null)
            mInstance = new ConnectionResponder(c, resID);
        return mInstance;
    }

    @Override
    public void killSwitchUpdate(boolean isInKillSwitch) { }

    @Override
    public void setConnectedVPN(String uuid) { }

    @Override
    public void updateState(String state, String message, int localizedResId, final ConnectionStatus level) {
        // Threading this as the amount of work has bloated overtime.
        new Thread(() -> handleStateChange(state, message, level)).start();
    }

    private synchronized void handleStateChange(String state, String message, ConnectionStatus level) {
        DLog.d(TAG, level + "");
        if (level == ConnectionStatus.LEVEL_CONNECTED) {
            if(executor == null || (executor != null && executor.isShutdown())) {
                int number_of_cores = Runtime.getRuntime().availableProcessors();
                workQueue = new LinkedBlockingQueue<>();
                executor = new ThreadPoolExecutor(number_of_cores, number_of_cores, 30, TimeUnit.SECONDS, workQueue);
            }

            if (state.equals(GATEWAY)) {
                PiaPrefHandler.setGatewayEndpoint(context, message);
            }

            if (PiaPrefHandler.isPortForwardingEnabled(context)) {
                startPortForwarding();
            }

            if(PiaPrefHandler.isKillswitchEnabled(context)){
                PIAOpenVPNTunnelLibrary.mNotifications.stopKillSwitchNotification(context);
            }

            resetRevivalMechanic();
        } else if(level == ConnectionStatus.LEVEL_NOTCONNECTED) {
            DLog.d(TAG, "Not connected Clear");
            PiaPrefHandler.clearLastIPVPN(context);
            PIAVpnStatus.clearOldData();
            cleanupExecutor();
            PiaPrefHandler.setVPNConnecting(context, false);
            clearPortForwarding();
        } else if(level == ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET || level == ConnectionStatus.LEVEL_NONETWORK){
            PiaPrefHandler.setVPNConnecting(context, true);
            PiaPrefHandler.clearLastIPVPN(context);
            VPN_REVOKED = false;
            clearPortForwarding();
        }
    }

    private void resetRevivalMechanic() {
        PiaPrefHandler.setUserEndedConnection(context, false);
        PiaPrefHandler.setVPNConnecting(context, false);
        VPN_REVOKED = false;
    }

    private void cleanupExecutor() {
        try {
            if (executor != null) {
                executor.shutdown();
                try {
                    if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                }
                executor = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPortForwarding() {
        PIAVpnStatus.setPortForwardingStatus(PortForwardingStatus.REQUESTING, context.getString(REQUESTING_PORT_STRING));
        if (portForwardingIntent != null) {
            return;
        }

        Intent intent = new Intent(context, PortForwardingReceiver.class);
        portForwardingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT
        );
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                0,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                portForwardingIntent
        );
    }

    private void clearPortForwarding() {
        PiaPrefHandler.clearGatewayEndpoint(context);
        PIAVpnStatus.setPortForwardingStatus(PortForwardingStatus.NO_PORTFWD, "");
        if (portForwardingIntent == null) {
            return;
        }
        PiaPrefHandler.clearBindPortForwardInformation(context);
        alarmManager.cancel(portForwardingIntent);
        portForwardingIntent = null;
    }
}
