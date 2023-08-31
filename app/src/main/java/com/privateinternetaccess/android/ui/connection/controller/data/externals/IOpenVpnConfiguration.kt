package com.privateinternetaccess.android.ui.connection.controller.data.externals

interface IOpenVpnConfiguration {

    fun getUserDefinedConfiguration(): Result<String>

    fun getApplicationDefaultConfiguration(): Result<String>
}