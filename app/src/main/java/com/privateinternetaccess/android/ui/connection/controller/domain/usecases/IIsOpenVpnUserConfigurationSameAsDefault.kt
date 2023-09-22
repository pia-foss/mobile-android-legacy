package com.privateinternetaccess.android.ui.connection.controller.domain.usecases

interface IIsOpenVpnUserConfigurationSameAsDefault {
    operator fun invoke(): Result<Unit>
}