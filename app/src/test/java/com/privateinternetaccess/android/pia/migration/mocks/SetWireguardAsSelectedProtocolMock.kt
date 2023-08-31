package com.privateinternetaccess.android.pia.migration.mocks

import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerError
import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerErrorCode
import com.privateinternetaccess.android.ui.connection.controller.domain.usecases.ISetWireguardAsSelectedProtocol

class SetWireguardAsSelectedProtocolMock(
    private val shouldSucceed: Boolean
): ISetWireguardAsSelectedProtocol {

    // region ISetWireguardAsSelectedProtocol
    override fun invoke(): Result<Unit> {
        return if (shouldSucceed) {
            Result.success(Unit)
        } else {
            Result.failure(WireguardMigrationControllerError(
                    code = WireguardMigrationControllerErrorCode.SET_WIREGUARD_AS_SELECTED_PROTOCOL_FAILED
            ))
        }
    }
    // endregion
}