package com.privateinternetaccess.android.pia.services

/*
 *  Copyright (c) 2021 Private Internet Access, Inc.
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

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.pia.PIAFactory
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.model.events.VpnStateEvent
import com.privateinternetaccess.android.pia.nmt.NetworkManager
import com.privateinternetaccess.android.pia.nmt.RulesManager
import com.privateinternetaccess.android.pia.nmt.models.NetworkItem
import com.privateinternetaccess.android.pia.utils.DLog
import com.privateinternetaccess.android.ui.connection.MainActivity
import com.privateinternetaccess.android.utils.SnoozeUtils
import de.blinkt.openvpn.core.ConnectionStatus
import de.blinkt.openvpn.core.OpenVPNService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class AutomationService : Service(), RulesManager.RulesChangedListener {

    companion object {
        private const val TAG = "AutomationService"
        private const val ANDROID_CONNECTIVITY_FILTER = "android.net.conn.CONNECTIVITY_CHANGE"
        // Used exclusively by OVPN to show the connection speeds and other stats.
        // It operates on a different channel with a different configuration.
        private val connStatsNotificationId = OpenVPNService.NOTIFICATION_CHANNEL_BG_ID.hashCode()
        // It represents the different connection statuses. e.g. connecting, connected, etc.
        // Used exclusively by WG and transitionally on OVPN as it is replaced by
        // NOTIFICATION_CHANNEL_BG_ID with the connection stats later on.
        private val connStatusNotificationId = OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID.hashCode()
        private lateinit var notificationManager: NotificationManager

        fun start(context: Context) {
            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.applicationContext.startForegroundService(Intent(context, AutomationService::class.java))
            } else {
                context.applicationContext.startService(Intent(context, AutomationService::class.java))
            }
        }

        fun stop(context: Context) {
            context.applicationContext.stopService(Intent(context, AutomationService::class.java))
        }
    }

    private var lastKnownVpnStateEvent: VpnStateEvent? = null

    private val connectivityChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            context?.let {
                val action = intent.action
                if (action == ANDROID_CONNECTIVITY_FILTER) {
                    // User triggered action have priority over Automation rules.
                    // If the connection delta is due to an user action. Ignore it. Return.
                    if (PiaPrefHandler.getAutomationIgnoreConnectionDeltaByUserAction(it)) {
                        PiaPrefHandler.resetAutomationIgnoreConnectionDeltaByUserAction(it)
                        return
                    }
                    evaluateAutomationRulesIfNeeded(it)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        RulesManager.rulesChangedListener = this
        registerReceiver(connectivityChangeReceiver, IntentFilter(ANDROID_CONNECTIVITY_FILTER))
        EventBus.getDefault().register(this)
        startForeground(notificationId(), targetNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        RulesManager.rulesChangedListener = null
        lastKnownVpnStateEvent = null
        unregisterReceiver(connectivityChangeReceiver)
        EventBus.getDefault().unregister(this)
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun updateVpnState(event: VpnStateEvent)
    {
        if (event.level == null) {
            lastKnownVpnStateEvent = null
            return
        }

        // The events are being over reported. Thus, the need for a state to check deltas.
        if (lastKnownVpnStateEvent?.level == event.level) {
            return
        }

        lastKnownVpnStateEvent = event
        when(event.level) {
            ConnectionStatus.LEVEL_VPNPAUSED,
            ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED,
            ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET,
            ConnectionStatus.LEVEL_START,
            ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT -> {
                // Do nothing
            }
            ConnectionStatus.LEVEL_CONNECTED -> {
                // Reset notification with the connection one
                startForeground(notificationId(), targetNotification())
            }
            ConnectionStatus.LEVEL_NONETWORK,
            ConnectionStatus.LEVEL_NOTCONNECTED,
            ConnectionStatus.LEVEL_AUTH_FAILED,
            ConnectionStatus.UNKNOWN_LEVEL -> {
                // Reset notification with the automation one
                startForeground(notificationId(), automationNotification())
            }
        }
    }

    // region RulesChangedListener
    override fun onRulesChanged(context: Context) {
        evaluateAutomationRulesIfNeeded(context)
    }
    // endregion

    // region private
    private fun evaluateAutomationRulesIfNeeded(context: Context) {
        if (PiaPrefHandler.isAutomationDisabledBySettingOrFeatureFlag(context)) {
            DLog.d(TAG, "evaluateAutomationRulesIfNeeded feature disabled.")
            return
        }

        val networkBehavior = NetworkManager.getBestRule(context)
        if (networkBehavior == null) {
            DLog.d(TAG, "evaluateAutomationRulesIfNeeded unknown rule.")
            return
        }

        val vpn = PIAFactory.getInstance().getVPN(context)
        val vpnConnected = vpn.isOpenVPNActive || vpn.isWireguardActive
        val isSnoozeActive = SnoozeUtils.hasActiveAlarm(context)
        if (!vpnConnected && networkBehavior == NetworkItem.NetworkBehavior.ALWAYS_CONNECT && !isSnoozeActive) {
            vpn.start()
        } else if (vpnConnected && networkBehavior == NetworkItem.NetworkBehavior.ALWAYS_DISCONNECT) {
            vpn.stop()
        }
    }

    private fun automationNotification(): Notification {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this,
                    0,
                    notificationIntent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_IMMUTABLE
                    } else {
                        0
                    }
                )
            }

        return NotificationCompat.Builder(this, OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_stat_pia_robot_white)
            .setAutoCancel(true)
            .setContentTitle(getString(R.string.nmt_notification_title))
            .setContentText(getString(R.string.nmt_notification_description))
            .setContentIntent(pendingIntent).build()
    }

    private fun targetNotification(): Notification {
        // By default use the automation one, unless there is one already present.
        // e.g. vpn connection
        var targetNotification = automationNotification()

        // Try to find an existing one.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.activeNotifications.firstOrNull {
                it.id == connStatusNotificationId || it.id == connStatsNotificationId
            }?.notification?.let { notification ->
                targetNotification = notification
            }
        }

        return targetNotification
    }

    private fun notificationId(): Int =
        // By default use connection status one, unless it is ovpn. In which case use the
        // connection stats one as the connection status is transitional and replaced by the stats one.
        if (PIAFactory.getInstance().getVPN(this).isOpenVPNActive) {
            connStatsNotificationId
        } else {
            connStatusNotificationId
        }
    // endregion
}