package com.privateinternetaccess.android.tests

import android.view.WindowInsets.Side
import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLogin
import com.privateinternetaccess.android.helpers.ActionHelpers.goToSettings
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLogout
import com.privateinternetaccess.android.helpers.ActionHelpers.returnOnMainScreen
import com.privateinternetaccess.android.screens.objects.ProtocolPageObjects
import com.privateinternetaccess.android.screens.steps.ProtocolStepObjects
import org.junit.Test

class SignOutTests : BaseUiAutomatorClass() {

    private val protocolPageObjects = ProtocolPageObjects()
    private val protocolStepObjects = ProtocolStepObjects()

    @Test
    fun validatingDefaultProtocolSettingsAfterLogout() {
        successfulLogin()
        goToSettings(protocolPageObjects.protocolSettings, protocolPageObjects.protocolSelection)
        protocolStepObjects.selectOpenVPN()
        returnOnMainScreen()
        successfulLogout()
        successfulLogin()
        goToSettings(protocolPageObjects.protocolSettings)
        assert(protocolPageObjects.wireGuard.exists())
    }
}