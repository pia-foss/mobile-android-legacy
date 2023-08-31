package com.privateinternetaccess.android.ui.connection.controller.domain.usecases

interface ISetWireguardAsSelectedProtocol {
    operator fun invoke(): Result<Unit>
}