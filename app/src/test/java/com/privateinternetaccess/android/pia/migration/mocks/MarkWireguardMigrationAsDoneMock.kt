package com.privateinternetaccess.android.pia.migration.mocks

import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerError
import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerErrorCode
import com.privateinternetaccess.android.ui.connection.controller.domain.usecases.IMarkWireguardMigrationAsDone

class MarkWireguardMigrationAsDoneMock(
    private val shouldSucceed: Boolean
): IMarkWireguardMigrationAsDone {

    internal var invoked = false

    // region IMarkWireguardMigrationAsDone
    override fun invoke(): Result<Unit> {
        invoked = true
        return if (shouldSucceed) {
            Result.success(Unit)
        } else {
            Result.failure(WireguardMigrationControllerError(
                    code = WireguardMigrationControllerErrorCode.MARK_WIREGUARD_MIGRATION_AS_DONE_FAILED
            ))
        }
    }
    // endregion
}