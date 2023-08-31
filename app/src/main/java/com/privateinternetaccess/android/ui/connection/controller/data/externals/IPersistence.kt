package com.privateinternetaccess.android.ui.connection.controller.data.externals

interface IPersistence {

    fun isSelectedProtocoltOpenVpn(): Boolean

    fun hasWireguardMigrationNotBeenDone(): Boolean

    fun setWireguardAsSelectedProtocol(): Result<Unit>

    fun markWireguardMigrationAsDone(): Result<Unit>
}