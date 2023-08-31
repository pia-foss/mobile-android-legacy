package com.privateinternetaccess.android.ui.connection.controller.domain.usecases

interface IHasWireguardMigrationNotBeenDone {
    operator fun invoke(): Result<Unit>
}