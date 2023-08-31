package com.privateinternetaccess.android.ui.connection.controller

interface IWireguardMigrationController {
    operator fun invoke(): Result<Unit>
}