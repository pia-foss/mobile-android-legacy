package com.privateinternetaccess.android.pia.providers

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler

object DnsProvider {

    private const val MACE_DNS = "10.0.0.241"
    private const val PIA_DNS = "10.0.0.242"

    fun getTargetDns(context: Context, defaultDns: String? = null): Pair<String, String?> {
        var primaryDns: String = defaultDns ?: PIA_DNS
        var secondaryDns: String? = null

        if (PiaPrefHandler.isMaceEnabled(context)) {
            primaryDns = MACE_DNS
        } else if (PiaPrefHandler.isCustomDnsSelected(context)) {
            PiaPrefHandler.getPrimaryDns(context)?.let {
                primaryDns = it
            }
            PiaPrefHandler.getSecondaryDns(context)?.let {
                secondaryDns = it
            }
        } else if (
            PiaPrefHandler.isSystemDnsResolverSelected(context) &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        ) {
            val connectivityManager: ConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            connectivityManager.getLinkProperties(network)?.dnsServers?.let { dnsServers ->
                dnsServers.firstOrNull()?.hostAddress?.let {
                    primaryDns = it
                }
                if (dnsServers.size > 1) {
                    dnsServers.lastOrNull()?.hostAddress?.let {
                        secondaryDns = it
                    }
                }
            }
        }

        return Pair(primaryDns, secondaryDns)
    }
}