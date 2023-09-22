package com.privateinternetaccess.android.pia.migration.mocks

import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerError
import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerErrorCode
import com.privateinternetaccess.android.ui.connection.controller.domain.usecases.IIsOpenVpnUserConfigurationSameAsDefault

class IsOpenVpnUserConfigurationSameAsDefaultMock(
    private val shouldSucceed: Boolean
): IIsOpenVpnUserConfigurationSameAsDefault {

    // region IIsOpenVpnUserConfigurationSameAsDefault
    override fun invoke(): Result<Unit> {
        return if (shouldSucceed) {
            Result.success(Unit)
        } else {
            Result.failure(WireguardMigrationControllerError(
                code = WireguardMigrationControllerErrorCode.OPENVPN_DEFAULT_AND_USER_CONFIGURATION_ARE_DIFFERENT
            ))
        }
    }
    // endregion
}