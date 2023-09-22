package com.privateinternetaccess.android.ui.connection.controller

import com.privateinternetaccess.android.ui.connection.controller.domain.usecases.*

class WireguardMigrationController(
        private val isSelectedProtocolOpenVpn: IIsSelectedProtocolOpenVpn,
        private val hasWireguardMigrationNotBeenDone: IHasWireguardMigrationNotBeenDone,
        private val isOpenVpnUserConfigurationSameAsDefault: IIsOpenVpnUserConfigurationSameAsDefault,
        private val setWireguardAsSelectedProtocol: ISetWireguardAsSelectedProtocol,
        private val markWireguardMigrationAsDone: IMarkWireguardMigrationAsDone
): IWireguardMigrationController {

    // region IWireguardMigrationController
    override fun invoke(): Result<Unit> =
            isSelectedProtocolOpenVpn().fold(
                    onSuccess = {
                        return hasWireguardMigrationNotBeenDone()
                                .mapCatching {
                                    isOpenVpnUserConfigurationSameAsDefault().getOrThrow()
                                }
                                .mapCatching {
                                    setWireguardAsSelectedProtocol().getOrThrow()
                                }
                                .mapCatching {
                                    markWireguardMigrationAsDone().getOrThrow()
                                }
                    },
                    onFailure = {
                        markWireguardMigrationAsDone()
                        return Result.failure(it)
                    }
            )
    // endregion
}