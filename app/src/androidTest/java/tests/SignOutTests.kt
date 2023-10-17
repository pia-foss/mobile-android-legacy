package com.privateinternetaccess.android.tests

import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLogin
import com.privateinternetaccess.android.helpers.ActionHelpers.goToSettings
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLogout
import com.privateinternetaccess.android.helpers.ActionHelpers.returnOnMainScreen
import com.privateinternetaccess.android.helpers.ActionHelpers.goToSideMenu
import com.privateinternetaccess.android.screens.objects.*
import com.privateinternetaccess.android.screens.steps.*

import org.junit.Test

class SignOutTests : BaseUiAutomatorClass() {

    private val protocolPageObjects = ProtocolPageObjects()
    private val protocolStepObjects = ProtocolStepObjects()
    private val sideMenuPageObjects = SideMenuPageObjects()
    private val dedicateIPStepObjects = DedicateIPStepObjects()

    @Test
    fun validateDefaultProtocolSettingsAtLogout() {
        successfulLogin()
        goToSettings(protocolPageObjects.protocolSettings, protocolPageObjects.protocolSelection)
        protocolStepObjects.selectOpenVPN()
        returnOnMainScreen()
        successfulLogout()
        successfulLogin()
        goToSettings(protocolPageObjects.protocolSettings)
        assert(protocolPageObjects.wireGuard.exists())
    }

    @Test
    fun validateDIPSettingsAtLogout() {
        successfulLogin()
        goToSideMenu(sideMenuPageObjects.dedicateIP)
        dedicateIPStepObjects.enterDedicatedIP()
        returnOnMainScreen()
        successfulLogout()
        successfulLogin()
        goToSideMenu(sideMenuPageObjects.dedicateIP)

        val conditions = listOf(
            { DedicateIPPageObjects().dedicateIPField.text.equals("Paste your token here") },
            { !DedicateIPPageObjects().serverFlag.exists() },
            { !DedicateIPPageObjects().serverName.exists() },
            { !DedicateIPPageObjects().serverIPAddress.exists() }
        )

        for (condition in conditions) {
            assert(condition())
        }

    }
}