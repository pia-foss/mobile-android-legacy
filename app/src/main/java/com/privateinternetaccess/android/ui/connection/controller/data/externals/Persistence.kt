package com.privateinternetaccess.android.ui.connection.controller.data.externals

import android.content.Context
import com.privateinternetaccess.android.model.states.VPNProtocol
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler

class Persistence(private val context: Context): IPersistence {

    // region IPersistence
    override fun isSelectedProtocoltOpenVpn(): Boolean =
        VPNProtocol.activeProtocol(context) == VPNProtocol.Protocol.OpenVPN

    override fun hasWireguardMigrationNotBeenDone(): Boolean =
        !PiaPrefHandler.isWireguardMigrationDone(context)

    override fun setWireguardAsSelectedProtocol(): Result<Unit> {
        PiaPrefHandler.setProtocol(context, VPNProtocol.Protocol.WireGuard)
        return Result.success(Unit)
    }

    override fun markWireguardMigrationAsDone(): Result<Unit> {
        PiaPrefHandler.setWireguardMigrationAsDone(context)
        return Result.success(Unit)
    }
    // endregion
}