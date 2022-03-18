package com.privateinternetaccess.android.pia.kpi

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

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.privateinternetaccess.android.BuildConfig
import com.privateinternetaccess.android.PIAApplication
import com.privateinternetaccess.android.model.states.VPNProtocol
import com.privateinternetaccess.android.pia.api.PiaApi.ANDROID_HTTP_CLIENT
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.model.events.VpnStateEvent
import com.privateinternetaccess.android.pia.providers.ModuleClientStateProvider
import com.privateinternetaccess.android.pia.utils.DLog
import com.privateinternetaccess.kpi.*
import de.blinkt.openvpn.core.ConnectionStatus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class KPIManager {

    private enum class KPIConnectionStatus {
        NOT_CONNECTED,
        STARTED,
        CONNECTING,
        RECONNECTING,
        FAILED,
        CONNECTED
    }

    companion object {
        private const val TAG = "KPIManager"
        private const val STAGING_EVENT_TOKEN = "3bd9fa1b7d7ae30b6d119e335afdcfa7"
        private const val PRODUCTION_EVENT_TOKEN = "d5fe3babe96d218323dafe20a1981e4e"
        public val sharedInstance = KPIManager()
    }

    private var kpi: KPIAPI? = null
    private var kpiConnectionStatus = KPIConnectionStatus.NOT_CONNECTED
    private var kpiConnectionSource = KPIConnectionSource.AUTOMATIC

    // State machine to validate transitions. We can't rely solely on the VPN events as there are
    // several cases of over-reporting.
    private var kpiLastConnectionEvent: KPIConnectionEvent? = null
    private val kpiStateMachine = mapOf(
        KPIConnectionEvent.VPN_CONNECTION_ATTEMPT to listOf(
            KPIConnectionEvent.VPN_CONNECTION_CANCELLED,
            KPIConnectionEvent.VPN_CONNECTION_ESTABLISHED
        ),
        KPIConnectionEvent.VPN_CONNECTION_ESTABLISHED to listOf(
            KPIConnectionEvent.VPN_CONNECTION_ATTEMPT
        ),
        KPIConnectionEvent.VPN_CONNECTION_CANCELLED to listOf(
            KPIConnectionEvent.VPN_CONNECTION_ATTEMPT
        )
    )

    fun start() {
        prepareModuleIfNeeded()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        kpi?.start()
    }

    fun stop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        kpi?.stop()
    }

    fun flush() {
        kpi?.flush { error ->
            error?.let {
                DLog.d(TAG, "There was an error flushing KPI events $it")
            }
        }
    }

    fun recentEvents(callback: (List<String>) -> Unit) {
        if (kpi == null) {
            callback(emptyList())
            return
        }

        kpi?.recentEvents(callback)
    }

    fun setConnectionSource(connectionSource: KPIConnectionSource) {
        kpiConnectionSource = connectionSource
    }

    fun shouldStartKpi(context: Context) =
        PiaPrefHandler.isKpiShareConnectionEventsEnabled(context)

    @Subscribe
    fun vpnStateEvent(event: VpnStateEvent?) {
        val eventType = event?.level ?: return
        val targetStatus = eventType.mapToKPIConnectionStatus()
        if (kpiConnectionStatus == targetStatus) {
            return
        }

        when (targetStatus) {
            KPIConnectionStatus.STARTED,
            KPIConnectionStatus.RECONNECTING,
            KPIConnectionStatus.FAILED -> {
                // Do nothing
            }
            KPIConnectionStatus.NOT_CONNECTED -> {
                // If the target status is not_connected and we were previous connecting.
                // Treat this as a cancelled event.
                if (kpiConnectionStatus == KPIConnectionStatus.STARTED ||
                    kpiConnectionStatus == KPIConnectionStatus.CONNECTING
                ) {
                    submitConnectionEventIfPossible(
                        connectionEvent = KPIConnectionEvent.VPN_CONNECTION_CANCELLED
                    )
                }
            }
            KPIConnectionStatus.CONNECTING -> {
                submitConnectionEventIfPossible(
                    connectionEvent = KPIConnectionEvent.VPN_CONNECTION_ATTEMPT,
                    connectionSource = kpiConnectionSource
                )
            }
            KPIConnectionStatus.CONNECTED -> {
                // If the target status is connected and we were previous connecting.
                // Treat this as a established event.
                if (kpiConnectionStatus == KPIConnectionStatus.CONNECTING) {
                    submitConnectionEventIfPossible(
                        connectionEvent = KPIConnectionEvent.VPN_CONNECTION_ESTABLISHED
                    )
                }
            }
        }
        kpiConnectionStatus = targetStatus
    }

    // region private
    private fun prepareModuleIfNeeded() {
        if (kpi != null) {
            return
        }

        val clientStateProvider = ModuleClientStateProvider(PIAApplication.get().baseContext)
        kpi = KPIBuilder()
            .setKPIClientStateProvider(clientStateProvider)
            .setKPIFlushEventMode(KPISendEventsMode.PER_BATCH)
            .setCertificate(clientStateProvider.certificate)
            .setAppVersion(BuildConfig.VERSION_NAME)
            .build()
    }

    private fun submitConnectionEventIfPossible(
        connectionEvent: KPIConnectionEvent,
        connectionSource: KPIConnectionSource = kpiConnectionSource
    ) {
        if (!isConnectionEventTransitionValid(connectionEvent)) {
            DLog.e(TAG, "Invalid event transition $kpiLastConnectionEvent -> $connectionEvent")
            return
        }

        kpiLastConnectionEvent = connectionEvent
        val activeProtocol = activeProtocol()
        if (activeProtocol == null) {
            DLog.e(TAG, "Unknown active protocol")
            return
        }
        // we would like to only send events when the app is foreground
        if (ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.RESUMED && connectionSource == KPIConnectionSource.MANUAL) {
            DLog.d(TAG, "submitConnectionEventIfPossible $connectionEvent")
            val event = KPIClientEvent(
                eventName = connectionEvent,
                eventProperties = KPIClientEvent.EventProperties(
                    connectionSource = connectionSource,
                    preRelease = false,
                    userAgent = ANDROID_HTTP_CLIENT,
                    vpnProtocol = activeProtocol
                ),
                eventToken = eventToken()
            )
            kpi?.submit(event) { error ->
                error?.let {
                    DLog.d(TAG, "There was an error submitting a KPI event $it")
                }
            }
        }
    }

    private fun ConnectionStatus.mapToKPIConnectionStatus(): KPIConnectionStatus =
        when (this) {
            ConnectionStatus.LEVEL_VPNPAUSED,
            ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT,
            ConnectionStatus.UNKNOWN_LEVEL -> {
                // Do nothing. Return known state.
                kpiConnectionStatus
            }
            ConnectionStatus.LEVEL_NONETWORK,
            ConnectionStatus.LEVEL_AUTH_FAILED -> {
                KPIConnectionStatus.FAILED
            }
            ConnectionStatus.LEVEL_NOTCONNECTED -> {
                KPIConnectionStatus.NOT_CONNECTED
            }
            ConnectionStatus.LEVEL_START -> {
                KPIConnectionStatus.STARTED
            }
            ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET,
            ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED -> {

                // Based on the known `kpiConnectionStatus` state.
                // Decide the target state for these two vpn states.
                when (kpiConnectionStatus) {
                    KPIConnectionStatus.CONNECTING,
                    KPIConnectionStatus.FAILED -> {
                        // Do nothing. Return known state.
                        kpiConnectionStatus
                    }
                    KPIConnectionStatus.NOT_CONNECTED,
                    KPIConnectionStatus.STARTED -> {
                        // If the known state is an initial one. Mark this as connecting.
                        KPIConnectionStatus.CONNECTING
                    }
                    KPIConnectionStatus.RECONNECTING,
                    KPIConnectionStatus.CONNECTED -> {
                        // If the known state is connected. Mark this as reconnecting.
                        KPIConnectionStatus.RECONNECTING
                    }
                }
            }
            ConnectionStatus.LEVEL_CONNECTED -> {
                KPIConnectionStatus.CONNECTED
            }
        }

    private fun isConnectionEventTransitionValid(connectionEvent: KPIConnectionEvent) =
        kpiLastConnectionEvent?.let {
            kpiStateMachine[it]?.contains(connectionEvent)
        } ?: true

    private fun activeProtocol() =
        when (VPNProtocol.activeProtocol(PIAApplication.get().baseContext)) {
            VPNProtocol.Protocol.OpenVPN -> KPIVpnProtocol.OPENVPN
            VPNProtocol.Protocol.WireGuard -> KPIVpnProtocol.WIREGUARD
            null -> null
        }

    private fun eventToken(): String =
        if (PiaPrefHandler.useStaging(PIAApplication.get().baseContext)) {
            STAGING_EVENT_TOKEN
        } else {
            PRODUCTION_EVENT_TOKEN
        }
    // endregion
}
