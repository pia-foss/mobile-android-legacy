package com.privateinternetaccess.android.pia.migration.utils

import android.content.Context
import com.privateinternetaccess.android.ui.connection.controller.data.externals.IOpenVpnConfiguration
import com.privateinternetaccess.android.ui.connection.controller.data.externals.IPersistence
import com.privateinternetaccess.android.ui.connection.controller.data.externals.OpenVpnConfiguration
import com.privateinternetaccess.android.ui.connection.controller.data.externals.Persistence

internal object GivenExternal {

    fun persistence(
        context: Context
    ): IPersistence =
            Persistence(context = context)

    fun openVpnConfiguration(
        context: Context
    ): IOpenVpnConfiguration =
            OpenVpnConfiguration(context = context)
}