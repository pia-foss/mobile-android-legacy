package com.privateinternetaccess.android.pia.migration.mocks

import com.privateinternetaccess.android.ui.connection.controller.data.externals.IOpenVpnConfiguration

class OpenVpnConfigurationMock(
    private val userDefinedConfigurationMock: String,
    private val applicationDefaultConfigurationMock: String
): IOpenVpnConfiguration {

    // region IOpenVpnConfiguration
    override fun getUserDefinedConfiguration(): Result<String> =
            Result.success(userDefinedConfigurationMock)

    override fun getApplicationDefaultConfiguration(): Result<String> =
            Result.success(applicationDefaultConfigurationMock)
    // endregion
}