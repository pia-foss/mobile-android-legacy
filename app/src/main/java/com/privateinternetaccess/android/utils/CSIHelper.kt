package com.privateinternetaccess.android.utils

import android.content.Context
import android.os.Build
import android.text.TextUtils
import com.privateinternetaccess.android.model.listModel.PIALogItem
import com.privateinternetaccess.android.model.states.VPNProtocol
import com.privateinternetaccess.android.pia.api.PiaApi
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler.PREFNAME
import com.privateinternetaccess.android.pia.handlers.ThemeHandler
import com.privateinternetaccess.android.pia.utils.PIAVpnUtils.openVpnLogs
import com.privateinternetaccess.android.pia.utils.PIAVpnUtils.wireguardLogs
import com.privateinternetaccess.android.pia.utils.Prefs
import com.privateinternetaccess.android.pia.vpn.PiaOvpnConfig
import com.privateinternetaccess.android.wireguard.backend.GoBackend
import com.privateinternetaccess.csi.ICSIProvider
import com.privateinternetaccess.csi.ProviderType
import com.privateinternetaccess.csi.ReportType
import com.privateinternetaccess.regions.RegionsUtils.parse
import com.privateinternetaccess.regions.RegionsUtils.stringify
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import java.util.*


class CSIHelper(private val context: Context) {

    companion object {
        const val CSI_TEAM_IDENTIFIER = "pia_android"
        private const val CSI_APPLICATION_INFORMATION_FILENAME = "application_information"
        private const val CSI_DEVICE_INFORMATION_FILENAME = "device_information"
        private const val CSI_LAST_KNOWN_EXCEPTION_FILENAME = "last_known_exception"
        private const val CSI_PROTOCOL_INFORMATION_FILENAME = "protocol_information"
        private const val CSI_REGION_INFORMATION_FILENAME = "regions_information"
        private const val CSI_USER_SETTINGS_FILENAME = "user_settings"
    }

    init {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            PiaPrefHandler.setLastKnownException(context, throwable.stackTraceToString())
        }
    }

    val protocolInformationProvider = object : ICSIProvider {
        override val filename: String
            get() = CSI_PROTOCOL_INFORMATION_FILENAME
        override val isPersistedData: Boolean
            get() = false
        override val providerType: ProviderType
            get() = ProviderType.PROTOCOL_INFORMATION
        override val reportType: ReportType
            get() = ReportType.DIAGNOSTIC
        override val value: String
            get() = getProtocolInformation()
    }

    val regionInformationProvider = object : ICSIProvider {
        override val filename: String
            get() = CSI_REGION_INFORMATION_FILENAME
        override val isPersistedData: Boolean
            get() = true
        override val providerType: ProviderType
            get() = ProviderType.REGION_INFORMATION
        override val reportType: ReportType
            get() = ReportType.DIAGNOSTIC
        override val value: String
            get() = getRegionsInformation()
    }

    val userSettingsProvider = object : ICSIProvider {
        override val filename: String
            get() = CSI_USER_SETTINGS_FILENAME
        override val isPersistedData: Boolean
            get() = true
        override val providerType: ProviderType
            get() = ProviderType.USER_SETTINGS
        override val reportType: ReportType
            get() = ReportType.DIAGNOSTIC
        override val value: String
            get() = getUserSettings()
    }

    val lastKnownExceptionProvider = object : ICSIProvider {
        override val filename: String
            get() = CSI_LAST_KNOWN_EXCEPTION_FILENAME
        override val isPersistedData: Boolean
            get() = true
        override val providerType: ProviderType
            get() = ProviderType.LAST_KNOWN_EXCEPTION
        override val reportType: ReportType
            get() = ReportType.CRASH
        override val value: String
            get() = getLastKnownException()
    }

    val applicationInformationProvider = object : ICSIProvider {
        override val filename: String
            get() = CSI_APPLICATION_INFORMATION_FILENAME
        override val isPersistedData: Boolean
            get() = false
        override val providerType: ProviderType
            get() = ProviderType.APPLICATION_INFORMATION
        override val reportType: ReportType
            get() = ReportType.DIAGNOSTIC
        override val value: String
            get() = getApplicationInformation()
    }

    val deviceInformationProvider = object : ICSIProvider {
        override val filename: String
            get() = CSI_DEVICE_INFORMATION_FILENAME
        override val isPersistedData: Boolean
            get() = false
        override val providerType: ProviderType
            get() = ProviderType.DEVICE_INFORMATION
        override val reportType: ReportType
            get() = ReportType.DIAGNOSTIC
        override val value: String
            get() = getDeviceInformation()
    }

    // region private
    private fun getProtocolInformation(): String {
        val activeProtocol = VPNProtocol.activeProtocol(context)
        val prefs = Prefs.with(context)
        val sb = StringBuilder()
        sb.append("~~ Connection Settings ~~\n")
        sb.append("Connection Type: ${PiaPrefHandler.getProtocolTransport(context)}\n")
        sb.append("Port Forwarding: ${PiaPrefHandler.isPortForwardingEnabled(context)}\n")
        sb.append("Remote Port: ${PiaPrefHandler.getRemotePort(context)}\n")
        sb.append("Local Port: ${PiaPrefHandler.getLocalPort(context)}\n")
        sb.append("OVPN Use Small Packets: ${PiaPrefHandler.getOvpnSmallPacketSizeEnabled(context)}\n")
        sb.append("Wireguard Use Small Packets: ${PiaPrefHandler.getWireguardSmallPacketSizeEnabled(context)}\n")
        sb.append("Protocol: ${activeProtocol.name}\n")
        sb.append("\n~~ Proxy Settings ~~\n\n")
        sb.append("Proxy Enabled: ${PiaPrefHandler.isConnectViaProxyEnabled(context)}\n")
        sb.append("Proxy App: ${PiaPrefHandler.getProxyApp(context)}\n")
        sb.append("Proxy Port: ${PiaPrefHandler.getProxyPort(context)}\n")
        sb.append("\n~~ Blocking Settings ~~\n\n")
        sb.append("MACE: ${PiaPrefHandler.isMaceEnabled(context)}\n")
        sb.append("Killswitch: ${PiaPrefHandler.isKillswitchEnabled(context)}\n")
        sb.append("Ipv6 Blocking: ${PiaPrefHandler.isBlockIpv6Enabled(context)}\n")
        sb.append("Allow Local Network: ${PiaPrefHandler.isAllowLocalLanEnabled(context)}\n")
        sb.append("\n~~ Encryption Settings ~~\n\n")

        PiaPrefHandler.getDataCipher(context)?.let {
            sb.append("Data Encryption: $it\n")
            sb.append(
                "Data Authentication: " + if (it.toLowerCase(
                        Locale.ENGLISH
                    ).contains("gcm")
                ) PiaPrefHandler.getDataAuthentication(context) else ""
            ).append("\n")
        }

        sb.append("OpenVPN Handshake: ${PiaOvpnConfig.OVPN_HANDSHAKE}\n")
        sb.append("Wireguard Handshake: ${GoBackend.WG_HANDSHAKE}\n")
        sb.append("\n~~ App Settings ~~\n\n")
        sb.append("1 click connect: ${prefs[PiaPrefHandler.AUTOCONNECT, false]}\n")
        sb.append("Connect on Boot: ${prefs[PiaPrefHandler.AUTOSTART, false]}\n")
        sb.append("Connect on App Updated: ${prefs[PiaPrefHandler.CONNECT_ON_APP_UPDATED, false]}\n")
        sb.append("Haptic Feedback: ${prefs[PiaPrefHandler.HAPTIC_FEEDBACK, true]}\n")
        sb.append("Dark theme: ${prefs[ThemeHandler.PREF_THEME, false]}\n")
        sb.append("\n~~~~~ End User Settings ~~~~~\n")
        sb.append("\n~~ VPN Logs ~~\n\n")
        when (activeProtocol) {
            VPNProtocol.Protocol.OpenVPN -> {
                val items: List<PIALogItem> = openVpnLogs(context)
                for (item in items) {
                    sb.append("${item.timeString}: ${item.message}\n")
                }
            }
            VPNProtocol.Protocol.WireGuard -> {
                val items: List<PIALogItem> = wireguardLogs()
                for (item in items) {
                    sb.append("${item.timeString}: ${item.message}\n")
                }
            }
            else -> {
                sb.append("Unsupported protocol logs\n")
            }
        }
        sb.append("\n~~~~~ End VPN Logs ~~~~~\n\n")
        return redactIPsFromString(sb.toString())
    }

    private fun getRegionsInformation(): String {
        val lastServerBody = PiaPrefHandler.lastServerBody(context)
        if (TextUtils.isEmpty(lastServerBody)) {
            return "Unknown"
        }

        val redactedRegions: ArrayList<VpnRegionsResponse.Region> = ArrayList<VpnRegionsResponse.Region>()
        val (groups, regions) = parse(lastServerBody)
        for ((id, name, country, dns, geo, offline, latitude, longitude, autoRegion, portForward, proxy) in regions) {
            redactedRegions.add(
                VpnRegionsResponse.Region(
                    id,
                    name,
                    country,
                    dns,
                    geo,
                    offline,
                    latitude,
                    longitude,
                    autoRegion,
                    portForward,
                    proxy, emptyMap()
                )
            )
        }
        val redactedResponse = VpnRegionsResponse(
            groups,
            redactedRegions
        )
        return redactIPsFromString(stringify(redactedResponse))
    }

    private fun getUserSettings(): String {
        var userSettings = ""
        val wantedSettings = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE).all.filterNot {
            it.key ==  PiaPrefHandler.LAST_KNOWN_EXCEPTION_KEY
        }
        for ((key, value) in wantedSettings) {
            userSettings = "$userSettings\n$key: $value"
        }
        return redactIPsFromString(userSettings)
    }

    private fun getLastKnownException(): String {
        return PiaPrefHandler.getLastKnownException(context)
    }

    private fun getApplicationInformation(): String {
        return PiaApi.ANDROID_HTTP_CLIENT
    }

    private fun getDeviceInformation(): String {
        val sb = StringBuilder()
        sb.append("OS Version: ${System.getProperty("os.version")}(${Build.VERSION.INCREMENTAL}").append("\n")
        sb.append("API Level: ${Build.VERSION.SDK_INT}").append("\n")
        sb.append("Device: ${Build.DEVICE}").append("\n")
        sb.append("Product: ${Build.PRODUCT}").append("\n")
        sb.append("Model: ${Build.MODEL}").append("\n")
        return sb.toString()
    }

    private fun redactIPsFromString(redact: String): String {
        return redact.replace("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b".toRegex(), "REDACTED")
    } // endregion
}