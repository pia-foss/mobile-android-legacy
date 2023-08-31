package com.privateinternetaccess.android.pia.migration.utils

import android.content.Context
import com.privateinternetaccess.android.ui.connection.controller.IWireguardMigrationController
import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationController
import com.privateinternetaccess.android.ui.connection.controller.domain.usecases.*

internal object GivenController {

    fun wireguardMigrationController(
        context: Context,
        isSelectedProtocolOpenVpn: IIsSelectedProtocolOpenVpn =
                GivenUsecase.isSelectedProtocolOpenVpn(context = context),
        hasWireguardMigrationNotBeenDone: IHasWireguardMigrationNotBeenDone =
                GivenUsecase.hasWireguardMigrationNotBeenDone(context = context),
        isOpenVpnUserConfigurationSameAsDefault: IIsOpenVpnUserConfigurationSameAsDefault =
                GivenUsecase.isOpenVpnUserConfigurationSameAsDefault(context = context),
        setWireguardAsSelectedProtocol: ISetWireguardAsSelectedProtocol =
                GivenUsecase.setWireguardAsSelectedProtocol(context = context),
        markWireguardMigrationAsDone: IMarkWireguardMigrationAsDone =
                GivenUsecase.markWireguardMigrationAsDone(context = context)
    ): IWireguardMigrationController =
            WireguardMigrationController(
                    isSelectedProtocolOpenVpn = isSelectedProtocolOpenVpn,
                    hasWireguardMigrationNotBeenDone = hasWireguardMigrationNotBeenDone,
                    isOpenVpnUserConfigurationSameAsDefault = isOpenVpnUserConfigurationSameAsDefault,
                    setWireguardAsSelectedProtocol = setWireguardAsSelectedProtocol,
                    markWireguardMigrationAsDone = markWireguardMigrationAsDone
            )
}