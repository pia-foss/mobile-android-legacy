package com.privateinternetaccess.android.ui.connection.controller.domain.usecases

import com.privateinternetaccess.android.ui.connection.controller.data.externals.IPersistence

class MarkWireguardMigrationAsDone(
    private val persistence: IPersistence
): IMarkWireguardMigrationAsDone {

    // region IMarkWireguardMigrationAsDone
    override fun invoke(): Result<Unit> =
            persistence.markWireguardMigrationAsDone()
    // endregion
}