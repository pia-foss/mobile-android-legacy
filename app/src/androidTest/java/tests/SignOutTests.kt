package com.privateinternetaccess.android.tests

import com.privateinternetaccess.android.screens.steps.SideMenuStepObjects
import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.screens.objects.SideMenuPageObjects
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLogin
import com.privateinternetaccess.android.helpers.ActionHelpers.goToSettings
import com.privateinternetaccess.android.screens.objects.SettingsPageObjects
import com.privateinternetaccess.android.screens.steps.GeneralStepObjects
import com.privateinternetaccess.android.screens.steps.MainScreenStepObjects
import org.junit.Test
import scom.privateinternetaccess.android.screens.objects.GeneralPageObjects

class SignOutTests : BaseUiAutomatorClass() {

    private val sideMenuStepObjects = SideMenuStepObjects()
    private val mainScreenStepObjects = MainScreenStepObjects()
    private val settingsPageObjects = SettingsPageObjects()
    private val generalStepObjects = GeneralStepObjects()
    @Test
    fun validatingDefaultSettingsAfterLogout() {
        successfulLogin()
        goToSettings(settingsPageObjects.general)
        generalStepObjects.launchOnSystemStartUp()
        generalStepObjects.clickOnBackArrow()
        generalStepObjects.clickOnBackArrow()
        mainScreenStepObjects.clickOnHamburgerMenu()
        sideMenuStepObjects.clickOnLogout()
        successfulLogin()
        goToSettings(settingsPageObjects.general)
        assert(!GeneralPageObjects().toggleLaunchOnSystemStartUp.isChecked())
    }
}