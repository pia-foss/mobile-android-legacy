package com.privateinternetaccess.android.pia.migration.mocks

import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerError
import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerErrorCode
import com.privateinternetaccess.android.ui.connection.controller.domain.usecases.IIsSelectedProtocolOpenVpn

class IsSelectedProtocolOpenVpnMock(
    private val shouldSucceed: Boolean
): IIsSelectedProtocolOpenVpn {

    // region IIsSelectedProtocolOpenVpn
    override fun invoke(): Result<Unit> {
        return if (shouldSucceed) {
            Result.success(Unit)
        } else {
            Result.failure(WireguardMigrationControllerError(
                    code = WireguardMigrationControllerErrorCode.SELECTED_PROTOCOL_IS_NOT_OPENVPN
            ))
        }
    }
    // endregion
}