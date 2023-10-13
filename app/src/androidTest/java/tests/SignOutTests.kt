package com.privateinternetaccess.android.tests

import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLogin
import com.privateinternetaccess.android.helpers.ActionHelpers.goToSettings
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLogout
import com.privateinternetaccess.android.helpers.ActionHelpers.returnOnMainScreen
import com.privateinternetaccess.android.screens.objects.ProtocolPageObjects
import com.privateinternetaccess.android.screens.steps.DedicateIPStepObjects
import com.privateinternetaccess.android.screens.steps.MainScreenStepObjects
import com.privateinternetaccess.android.screens.steps.ProtocolStepObjects
import com.privateinternetaccess.android.screens.steps.SideMenuStepObjects
import com.privateinternetaccess.android.screens.objects.DedicateIPPageObjects

import org.junit.Test

class SignOutTests : BaseUiAutomatorClass() {

    private val protocolPageObjects = ProtocolPageObjects()
    private val protocolStepObjects = ProtocolStepObjects()
    private val mainScreenStepObjects = MainScreenStepObjects()
    private val sideMenuStepObjects = SideMenuStepObjects()
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
        mainScreenStepObjects.clickOnHamburgerMenu()
        sideMenuStepObjects.clickOnDedicatedIP()
        dedicateIPStepObjects.enterDedicateIP()
        dedicateIPStepObjects.activateButton()
        assert(DedicateIPPageObjects().serverName.exists())
    }
}