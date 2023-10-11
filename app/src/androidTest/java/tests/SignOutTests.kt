package com.privateinternetaccess.android.tests

import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLogin
import com.privateinternetaccess.android.helpers.ActionHelpers.goToSettings
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLgout
import com.privateinternetaccess.android.helpers.ActionHelpers.returnOnMainScreen
import com.privateinternetaccess.android.screens.objects.SettingsPageObjects
import com.privateinternetaccess.android.screens.steps.GeneralStepObjects
import com.privateinternetaccess.android.screens.objects.GeneralPageObjects
import com.privateinternetaccess.android.screens.objects.ProtocolPageObjects
import com.privateinternetaccess.android.screens.steps.ProtocolStepObjects
import org.junit.Test

class SignOutTests : BaseUiAutomatorClass() {

    private val settingsPageObjects = SettingsPageObjects()
    private val generalStepObjects = GeneralStepObjects()
    private val protocolPageObjects = ProtocolPageObjects()

    @Test
    fun validatingDefaultSettingsAfterLogout() {
        successfulLogin()
        goToSettings(protocolPageObjects.protocolSettings, protocolPageObjects.protocolSelection)
        ProtocolStepObjects().clickProtocol()
        generalStepObjects.launchOnSystemStartUp()
//        returnOnMainScreen()
//        successfulLgout()
//        successfulLogin()
//        goToSettings(settingsPageObjects.general)
//        assert(!GeneralPageObjects().toggleLaunchOnSystemStartUp.isChecked())
    }
}