package com.privateinternetaccess.android.ui.connection.controller

data class WireguardMigrationControllerError(
        val code: WireguardMigrationControllerErrorCode,
        val error: Error? = null
) : Throwable()