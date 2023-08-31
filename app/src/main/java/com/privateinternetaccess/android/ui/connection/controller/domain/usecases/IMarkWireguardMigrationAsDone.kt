package com.privateinternetaccess.android.ui.connection.controller.domain.usecases

interface IMarkWireguardMigrationAsDone {
    operator fun invoke(): Result<Unit>
}