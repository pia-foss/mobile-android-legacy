package com.privateinternetaccess.android.pia.migration.mocks

import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerError
import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerErrorCode
import com.privateinternetaccess.android.ui.connection.controller.domain.usecases.IHasWireguardMigrationNotBeenDone

class HasWireguardMigrationNotBeenDoneMock(
    private val shouldSucceed: Boolean
): IHasWireguardMigrationNotBeenDone {

    // region IHasWireguardMigrationNotBeenDone
    override fun invoke(): Result<Unit> {
        return if (shouldSucceed) {
            Result.success(Unit)
        } else {
            Result.failure(WireguardMigrationControllerError(
                code = WireguardMigrationControllerErrorCode.WIREGUARD_MIGRATION_HAS_BEEN_DONE
            ))
        }
    }
    // endregion
}