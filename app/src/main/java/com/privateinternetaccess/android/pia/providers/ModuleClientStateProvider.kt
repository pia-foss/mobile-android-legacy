package com.privateinternetaccess.android.pia.providers

import android.content.Context
import com.privateinternetaccess.account.IAccountEndpointProvider
import com.privateinternetaccess.account.AccountEndpoint
import com.privateinternetaccess.android.BuildConfig
import com.privateinternetaccess.android.PIAApplication
import com.privateinternetaccess.android.pia.PIAFactory
import com.privateinternetaccess.android.pia.handlers.PIAServerHandler
import com.privateinternetaccess.android.pia.handlers.PIAServerHandler.ServerSortingType
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.utils.DLog
import com.privateinternetaccess.regions.IRegionEndpointProvider
import com.privateinternetaccess.regions.RegionEndpoint
import com.privateinternetaccess.core.model.PIAServer
import com.privateinternetaccess.csi.CSIEndpoint
import com.privateinternetaccess.csi.IEndPointProvider
import com.privateinternetaccess.kpi.KPIClientStateProvider
import com.privateinternetaccess.kpi.KPIEndpoint
import java.io.BufferedReader
import kotlin.random.Random


class ModuleClientStateProvider(
        private val context: Context
)  : IAccountEndpointProvider, IRegionEndpointProvider, IEndPointProvider, KPIClientStateProvider {

    companion object {
        private const val TAG = "ModuleClientStateProvider"
        private const val MAX_META_ENDPOINTS = 2
        private const val GATEWAY = "10.0.0.1"
        private const val ACCOUNT_BASE_ROOT_DOMAIN = "privateinternetaccess.com"
        private const val ACCOUNT_PROXY_ROOT_DOMAIN = "piaproxy.net"
        private const val REGION_BASE_ENDPOINT = "serverlist.piaservers.net"
        private const val CSI_BASE_ENDPOINT = "csi.supreme.tools"

        public val CERTIFICATE =
            PIAApplication.getRSA4096Certificate().bufferedReader().use(BufferedReader::readText)
    }

    public val certificate =
        PIAApplication.getRSA4096Certificate(context).bufferedReader().use(BufferedReader::readText)

    // region IAccountEndpointProvider
    override fun accountEndpoints(): List<AccountEndpoint> {
        val endpoints = mutableListOf<AccountEndpoint>()
        for (metaEndpoint in metaEndpoints()) {
            endpoints.add(AccountEndpoint(
                metaEndpoint.endpoint,
                metaEndpoint.isProxy,
                metaEndpoint.usePinnedCertificate,
                metaEndpoint.certificateCommonName)
            )
        }
        endpoints.add(
            AccountEndpoint(
                ACCOUNT_BASE_ROOT_DOMAIN,
                isProxy = false,
                usePinnedCertificate = false,
                certificateCommonName = null
            )
        )
        endpoints.add(
            AccountEndpoint(
                ACCOUNT_PROXY_ROOT_DOMAIN,
                isProxy = true,
                usePinnedCertificate = false,
                certificateCommonName = null
            )
        )

        if (PiaPrefHandler.useStaging(context)) {
            val stagingHost = if (PiaPrefHandler.getStagingServer(context).isNullOrEmpty()) {
                BuildConfig.STAGEINGHOST
            } else {
                PiaPrefHandler.getStagingServer(context)
            }
            endpoints.clear()
            endpoints.add(
                AccountEndpoint(
                    stagingHost.replace("https://", "").replace("http://", ""),
                    isProxy = false,
                    usePinnedCertificate = false,
                    certificateCommonName = null
                )
            )
        }
        return endpoints
    }
    // endregion

    // region RegionClientStateProvider
    override fun regionEndpoints(): List<RegionEndpoint> {
        val endpoints = mutableListOf<RegionEndpoint>()
        for (metaEndpoint in metaEndpoints()) {
            endpoints.add(RegionEndpoint(
                metaEndpoint.endpoint,
                metaEndpoint.isProxy,
                metaEndpoint.usePinnedCertificate,
                metaEndpoint.certificateCommonName)
            )
        }
        endpoints.add(
            RegionEndpoint(
                REGION_BASE_ENDPOINT,
                isProxy = false,
                usePinnedCertificate = false,
                certificateCommonName = null
            )
        )
        return endpoints
    }
    // endregion

    // region IEndPointProvider
    override val endpoints: List<CSIEndpoint>
        get() = listOf(CSIEndpoint(CSI_BASE_ENDPOINT, false, false, null))
    // endregion

    // region KPIClientStateProvider
    override fun kpiEndpoints(): List<KPIEndpoint> {
        val endpoints = mutableListOf<KPIEndpoint>()
        endpoints.add(
            KPIEndpoint(
                ACCOUNT_BASE_ROOT_DOMAIN,
                isProxy = false,
                usePinnedCertificate = false,
                certificateCommonName = null
            )
        )
        endpoints.add(
            KPIEndpoint(
                ACCOUNT_PROXY_ROOT_DOMAIN,
                isProxy = true,
                usePinnedCertificate = false,
                certificateCommonName = null
            )
        )

        if (PiaPrefHandler.useStaging(context)) {
            val stagingHost = if (PiaPrefHandler.getStagingServer(context).isNullOrEmpty()) {
                BuildConfig.STAGEINGHOST
            } else {
                PiaPrefHandler.getStagingServer(context)
            }
            endpoints.clear()
            endpoints.add(
                KPIEndpoint(
                    stagingHost.replace("https://", "").replace("http://", ""),
                    isProxy = false,
                    usePinnedCertificate = false,
                    certificateCommonName = null
                )
            )
        }
        return endpoints
    }

    override fun kpiAuthToken(): String {
        return PIAFactory.getInstance().getAccount(context).vpnToken() ?: ""
    }
    // endregion

    // region private
    private fun metaEndpoints(): List<GenericEndpoint> {
        val endpoints = mutableListOf<GenericEndpoint>()
        if (PiaPrefHandler.isStopUsingMetaServersEnabled(context)) {
            DLog.d(TAG, "Meta endpoints disabled on developer option")
            return endpoints
        }

        val serverHandler = PIAServerHandler.getInstance(context)
        val selectedRegion = serverHandler.getSelectedRegion(context, false)
        val vpnConnected = PIAFactory.getInstance().getVPN(context).isVPNActive

        // If the VPN is connected. Add the meta gateway and return.
        if (vpnConnected) {
            val selectedEndpoints = selectedRegion.endpoints[PIAServer.Protocol.META]
            if (!selectedEndpoints.isNullOrEmpty()) {
                endpoints.add(
                    GenericEndpoint(
                        GATEWAY,
                        isProxy = true,
                        usePinnedCertificate = true,
                        certificateCommonName = selectedEndpoints.first().cn
                    )
                )
            }
            return endpoints
        }

        // Get the list of known regions sorted by latency.
        val sortedLatencyRegions = serverHandler.getServers(context, ServerSortingType.LATENCY)
        if (sortedLatencyRegions.isNullOrEmpty()) {
            return endpoints
        }

        // Filter out invalid latencies. e.g. nil, zero, etc.
        val regionsWithValidLatency = sortedLatencyRegions.filterNot {
            it.latency.isNullOrEmpty() || it.latency == "0"
        }.toMutableList()

        // If there were no regions with valid latencies yet or less than what we need to. Pick random.
        if (regionsWithValidLatency.isEmpty() || regionsWithValidLatency.size < MAX_META_ENDPOINTS) {

            // Starting from 2 because the automatic/selected region occupies one slot of the max.
            for ( i in 2..MAX_META_ENDPOINTS) {
                val region = sortedLatencyRegions[Random.nextInt(0, sortedLatencyRegions.size)]
                regionsWithValidLatency.add(region)
            }
        }

        // Add the selected region.
        regionsWithValidLatency.add(0, selectedRegion)

        // Add the MAX_META_ENDPOINTS regions with the lowest latencies.
        for (region in regionsWithValidLatency.subList(0, MAX_META_ENDPOINTS)) {
            // We want different meta regions. Provide just one meta per region region.
            val selectedEndpoint = region.endpoints[PIAServer.Protocol.META]?.first()
            if (selectedEndpoint != null) {
                endpoints.add(
                    GenericEndpoint(
                        selectedEndpoint.ip,
                        isProxy = true,
                        usePinnedCertificate = true,
                        certificateCommonName = selectedEndpoint.cn
                    )
                )
            }
        }
        return endpoints
    }

    private data class GenericEndpoint(
        val endpoint: String,
        val isProxy: Boolean,
        val usePinnedCertificate: Boolean,
        val certificateCommonName: String?
    )
    // endregion
}