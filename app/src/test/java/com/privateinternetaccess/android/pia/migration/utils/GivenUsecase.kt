package com.privateinternetaccess.android.pia.migration.utils

import android.content.Context
import com.privateinternetaccess.android.ui.connection.controller.data.externals.IOpenVpnConfiguration
import com.privateinternetaccess.android.ui.connection.controller.data.externals.IPersistence
import com.privateinternetaccess.android.ui.connection.controller.domain.usecases.*

internal object GivenUsecase {

    fun hasWireguardMigrationNotBeenDone(
        context: Context,
        persistence: IPersistence = GivenExternal.persistence(context = context)
    ): IHasWireguardMigrationNotBeenDone =
            HasWireguardMigrationNotBeenDone(persistence = persistence)

    fun isOpenVpnUserConfigurationSameAsDefault(
        context: Context,
        openVpnConfiguration: IOpenVpnConfiguration = GivenExternal.openVpnConfiguration(context = context)
    ): IIsOpenVpnUserConfigurationSameAsDefault =
            IsOpenVpnUserConfigurationSameAsDefault(openVpnConfiguration = openVpnConfiguration)

    fun isSelectedProtocolOpenVpn(
        context: Context,
        persistence: IPersistence = GivenExternal.persistence(context = context)
    ): IIsSelectedProtocolOpenVpn =
            IsSelectedProtocolOpenVpn(persistence = persistence)

    fun markWireguardMigrationAsDone(
        context: Context,
        persistence: IPersistence = GivenExternal.persistence(context = context)
    ): IMarkWireguardMigrationAsDone =
            MarkWireguardMigrationAsDone(persistence = persistence)

    fun setWireguardAsSelectedProtocol(
        context: Context,
        persistence: IPersistence = GivenExternal.persistence(context = context)
    ): ISetWireguardAsSelectedProtocol =
            SetWireguardAsSelectedProtocol(persistence = persistence)
}