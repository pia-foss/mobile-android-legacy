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

    /* OpenVPN protocol by default has small packets option disabled.
    (i) First test scenario is to validate that OpenVPN can connect while small packet is disabled
    (ii) Second test is to validate that OpenVPN can connect while small packet is enabled.
     */
    @Test
    fun openVPNConnectivityWhenSmallPacketDisabled() {
        successfulLogin()
        goToSettings(protocolPageObjects.protocolSettings, protocolPageObjects.protocolSelection)
        protocolStepObjects.selectOpenVPN()
        returnOnMainScreen()
        mainScreenStepObjects.clickConnect()
        assert(MainScreenPageObjects().connectButton.contentDescription.equals("VPN Connected"))
    }

    @Test
    fun openVPNConnectivityWhenSmallPacketEnabled() {
        successfulLogin()
        goToSettings(protocolPageObjects.protocolSettings, protocolPageObjects.protocolSelection)
        protocolStepObjects.selectOpenVPN()
        protocolPageObjects.smallPacketsSwitchSetting.click()
        returnOnMainScreen()
        mainScreenStepObjects.clickConnect()
        assert(MainScreenPageObjects().connectButton.contentDescription.equals("VPN Connected"))
    }

    /* WireGuard protocol by default has small packets enabled.
    (i) First test scenario is to validate that WireGuard can connect while small packet is disabled.
    (ii) Second test is to validate that WireGuard can connect while small packet is enabled.
     */
    @Test
    fun wireGuardConnectivityWhenSmallPacketDisabled() {
        successfulLogin()
        goToSettings(protocolPageObjects.protocolSettings, protocolPageObjects.protocolSelection)
        protocolStepObjects.selectWireGuard()
        protocolPageObjects.smallPacketsSwitchSetting.click()
        returnOnMainScreen()
        mainScreenStepObjects.clickConnect()
        assert(MainScreenPageObjects().connectButton.contentDescription.equals("VPN Connected"))
    }

    @Test
    fun wireGuardConnectivityWhenSmallPacketEnabled() {
        successfulLogin()
        goToSettings(protocolPageObjects.protocolSettings, protocolPageObjects.protocolSelection)
        protocolStepObjects.selectWireGuard()
        returnOnMainScreen()
        mainScreenStepObjects.clickConnect()
        assert(MainScreenPageObjects().connectButton.contentDescription.equals("VPN Connected"))
    }
}