package com.privateinternetaccess.android.ui.connection.controller.data.externals

import android.content.Context
import com.privateinternetaccess.android.pia.providers.VPNFallbackEndpointProvider
import com.privateinternetaccess.android.pia.vpn.PiaOvpnConfig
import com.privateinternetaccess.core.model.PIAServer

class OpenVpnConfiguration(private val context: Context): IOpenVpnConfiguration {

    private val commonTokenUser = "commonTokenUser"
    private val commonTokenPass = "commonTokenPass"
    private val commonEndpoint = VPNFallbackEndpointProvider.VPNEndpoint(
            key = "key",
            name = "name",
            endpoint = "1.1.1.1",
            commonName = "commonName",
            usesVanillaOpenVPN = false,
            wireguardServer = PIAServer(
                key = "key",
                name = "name",
                iso = "iso",
                dns = "8.8.8.8",
                latency = null,
                endpoints = emptyMap(),
                latitude = null,
                longitude = null,
                isGeo = false,
                isOffline = false,
                isAllowsPF = false,
                dipToken = null,
                dedicatedIp = null
            )
    )

    // region IOpenVpnConfiguration
    override fun getUserDefinedConfiguration(): Result<String> =
            Result.success(
                    PiaOvpnConfig.generateOpenVPNUserConfiguration(
                            context,
                            commonTokenUser,
                            commonTokenPass,
                            commonEndpoint
                    )
            )



    override fun getApplicationDefaultConfiguration(): Result<String> =
            Result.success(
                    PiaOvpnConfig.generateOpenVPNAppDefaultConfiguration(
                            context,
                            commonTokenUser,
                            commonTokenPass,
                            commonEndpoint
                    )
            )
    // endregion
}