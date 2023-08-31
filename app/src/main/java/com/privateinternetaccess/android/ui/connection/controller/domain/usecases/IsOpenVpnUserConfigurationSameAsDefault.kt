package com.privateinternetaccess.android.ui.connection.controller.domain.usecases

import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerError
import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerErrorCode
import com.privateinternetaccess.android.ui.connection.controller.data.externals.IOpenVpnConfiguration

class IsOpenVpnUserConfigurationSameAsDefault(
    private val openVpnConfiguration: IOpenVpnConfiguration
): IIsOpenVpnUserConfigurationSameAsDefault {

    // region IIsOpenVpnUserConfigurationSameAsDefault
    override fun invoke(): Result<Unit> {
        val userDefinedConfiguration =
                openVpnConfiguration.getUserDefinedConfiguration().getOrThrow()
        val applicationDefaultConfiguration =
                openVpnConfiguration.getApplicationDefaultConfiguration().getOrThrow()

        return if (userDefinedConfiguration == applicationDefaultConfiguration) {
            Result.success(Unit)
        } else {
            Result.failure(WireguardMigrationControllerError(
                code = WireguardMigrationControllerErrorCode.OPENVPN_DEFAULT_AND_USER_CONFIGURATION_ARE_DIFFERENT
            ))
        }
    }
    // endregion
}