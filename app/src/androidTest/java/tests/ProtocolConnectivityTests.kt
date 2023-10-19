package com.privateinternetaccess.android.tests

import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.helpers.ActionHelpers.goToSettings
import com.privateinternetaccess.android.helpers.ActionHelpers.returnOnMainScreen
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLogin
import com.privateinternetaccess.android.screens.objects.MainScreenPageObjects
import com.privateinternetaccess.android.screens.objects.ProtocolPageObjects
import com.privateinternetaccess.android.screens.steps.MainScreenStepObjects
import com.privateinternetaccess.android.screens.steps.ProtocolStepObjects
import org.junit.Test

class ProtocolConnectivityTests : BaseUiAutomatorClass() {

    val protocolPageObjects = ProtocolPageObjects()
    val protocolStepObjects = ProtocolStepObjects()
    val mainScreenStepObjects = MainScreenStepObjects()

    @Test
    fun openVPNConnectivity() {
        successfulLogin()
        goToSettings(protocolPageObjects.protocolSettings, protocolPageObjects.protocolSelection)
        protocolStepObjects.selectOpenVPN()
        returnOnMainScreen()
        mainScreenStepObjects.clickConnect()
        assert(MainScreenPageObjects().connectButton.contentDescription.equals("VPN Connected"))
    }

    @Test
    fun wireGuardConnectivity() {
        successfulLogin()
        goToSettings(protocolPageObjects.protocolSettings, protocolPageObjects.protocolSelection)
        protocolStepObjects.selectWireGuard()
        returnOnMainScreen()
        mainScreenStepObjects.clickConnect()
        assert(MainScreenPageObjects().connectButton.contentDescription.equals("VPN Connected"))
    }
}