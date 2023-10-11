package com.privateinternetaccess.android.tests

import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLogin
import com.privateinternetaccess.android.helpers.ActionHelpers.goToSettings
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLgout
import com.privateinternetaccess.android.screens.objects.SettingsPageObjects
import com.privateinternetaccess.android.screens.steps.GeneralStepObjects
import com.privateinternetaccess.android.screens.objects.GeneralPageObjects

import org.junit.Test

class SignOutTests : BaseUiAutomatorClass() {

    private val settingsPageObjects = SettingsPageObjects()
    private val generalStepObjects = GeneralStepObjects()
    @Test
    fun validatingDefaultSettingsAfterLogout() {
        successfulLogin()
        goToSettings(settingsPageObjects.general)
        generalStepObjects.launchOnSystemStartUp()
        generalStepObjects.clickOnBackArrow()
        generalStepObjects.clickOnBackArrow()
        successfulLgout()
        successfulLogin()
        goToSettings(settingsPageObjects.general)
        assert(!GeneralPageObjects().toggleLaunchOnSystemStartUp.isChecked())
    }
}