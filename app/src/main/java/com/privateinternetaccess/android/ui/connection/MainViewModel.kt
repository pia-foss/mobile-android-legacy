package com.privateinternetaccess.android.ui.connection

import android.content.Context
import androidx.lifecycle.ViewModel
import com.privateinternetaccess.android.pia.utils.DLog
import com.privateinternetaccess.android.ui.connection.controller.IWireguardMigrationController
import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationController
import com.privateinternetaccess.android.ui.connection.controller.data.externals.IOpenVpnConfiguration
import com.privateinternetaccess.android.ui.connection.controller.data.externals.IPersistence
import com.privateinternetaccess.android.ui.connection.controller.data.externals.OpenVpnConfiguration
import com.privateinternetaccess.android.ui.connection.controller.data.externals.Persistence
import com.privateinternetaccess.android.ui.connection.controller.domain.usecases.*

class MainViewModel : ViewModel() {

    private val TAG = "MainViewModel"

    fun migrateToWireguard(context: Context) {
        val persistence: IPersistence = Persistence(context)
        val openVpnConfiguration: IOpenVpnConfiguration = OpenVpnConfiguration(context)
        val wireguardMigrationController: IWireguardMigrationController = WireguardMigrationController(
                IsSelectedProtocolOpenVpn(persistence),
                HasWireguardMigrationNotBeenDone(persistence),
                IsOpenVpnUserConfigurationSameAsDefault(openVpnConfiguration),
                SetWireguardAsSelectedProtocol(persistence),
                MarkWireguardMigrationAsDone(persistence)
        )
        val result = wireguardMigrationController()
        DLog.d(TAG, "migrateToWireguard result: $result")
    }
}