package com.privateinternetaccess.android.ui.connection.controller.domain.usecases

import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerError
import com.privateinternetaccess.android.ui.connection.controller.WireguardMigrationControllerErrorCode
import com.privateinternetaccess.android.ui.connection.controller.data.externals.IPersistence

class IsSelectedProtocolOpenVpn(
    private val persistence: IPersistence
): IIsSelectedProtocolOpenVpn {

    // region IIsSelectedProtocolOpenVpn
    override fun invoke(): Result<Unit> =
            if (persistence.isSelectedProtocoltOpenVpn()) {
                Result.success(Unit)
            } else {
                Result.failure(WireguardMigrationControllerError(
                    code = WireguardMigrationControllerErrorCode.SELECTED_PROTOCOL_IS_NOT_OPENVPN
                ))
            }
    // endregion
}