package com.privateinternetaccess.android.ui.connection.controller.domain.usecases

import com.privateinternetaccess.android.ui.connection.controller.data.externals.IPersistence

class SetWireguardAsSelectedProtocol(
    private val persistence: IPersistence
): ISetWireguardAsSelectedProtocol {

    // region ISetWireguardAsSelectedProtocol
    override fun invoke(): Result<Unit> =
            persistence.setWireguardAsSelectedProtocol()
    // endregion
}