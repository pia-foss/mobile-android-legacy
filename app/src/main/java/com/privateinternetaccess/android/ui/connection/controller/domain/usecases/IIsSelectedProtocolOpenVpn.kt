package com.privateinternetaccess.android.ui.connection.controller.domain.usecases

interface IIsSelectedProtocolOpenVpn {
    operator fun invoke(): Result<Unit>
}