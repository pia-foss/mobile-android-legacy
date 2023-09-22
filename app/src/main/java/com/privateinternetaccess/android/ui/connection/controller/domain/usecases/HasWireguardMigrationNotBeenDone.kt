package com.privateinternetaccess.android.ui.connection.controller.domain.usecases

import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerError
import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerErrorCode
import com.privateinternetaccess.android.ui.connection.controller.data.externals.IPersistence

class HasWireguardMigrationNotBeenDone(
    private val persistence: IPersistence
): IHasWireguardMigrationNotBeenDone {

    // region IWireguardMigrationHasNotBeenDone
    override fun invoke(): Result<Unit> =
            if(persistence.hasWireguardMigrationNotBeenDone()) {
                Result.success(Unit)
            } else {
                Result.failure(WireguardMigrationControllerError(
                        code = WireguardMigrationControllerErrorCode.WIREGUARD_MIGRATION_HAS_BEEN_DONE
                ))
            }
    // endregion
}