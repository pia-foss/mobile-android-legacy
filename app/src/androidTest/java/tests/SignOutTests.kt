package com.privateinternetaccess.android.tests

import com.privateinternetaccess.android.screens.steps.SideMenuStepObjects
import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.screens.objects.SideMenuPageObjects
import com.privateinternetaccess.android.helpers.ActionHelpers.userLoginSuccess
import com.privateinternetaccess.android.helpers.ActionHelpers.goToSettings
import org.junit.Before
import org.junit.Test

class SignOutTests : BaseUiAutomatorClass() {

    private val sideMenuStepObjects = SideMenuStepObjects()
    private val sideMenuPageObjects = SideMenuPageObjects()
    @Before
    fun login () {
        userLoginSuccess()
    }

    @Test
    fun logout() {
        goToSettings(sideMenuPageObjects.general)
        sideMenuStepObjects.launchOnSystemStartUp()
        sideMenuStepObjects.back()
        sideMenuStepObjects.back()
        sideMenuStepObjects.hamburgerMenu()
        sideMenuStepObjects.logout()
        userLoginSuccess()
        goToSettings(sideMenuPageObjects.general)
        assert(!SideMenuPageObjects().toggleLaunchOnSystemStartUp.isChecked())
    }
}