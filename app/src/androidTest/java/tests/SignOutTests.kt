package com.privateinternetaccess.android.tests

import com.privateinternetaccess.android.screens.steps.SideMenuStepObjects
import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.screens.objects.SideMenuPageObjects
import com.privateinternetaccess.android.screens.steps.SignInStepObjects
import org.junit.Before
import org.junit.Test

class SignOutTests : BaseUiAutomatorClass() {

    private val stepObjects = SignInStepObjects ()
    private val sideMenuStepObjects = SideMenuStepObjects()

    //pre-requiste
    @Before
    fun login () {
        stepObjects.allowNotifications()
        stepObjects.reachSignInScreen()
        stepObjects.enterCredentials()
        stepObjects.clickOnLoginButton()
        stepObjects.allowVpnProfileCreation()
    }

    @Test
    fun logout() {
        sideMenuStepObjects.hamburgerMenu()
        sideMenuStepObjects.settings()
        sideMenuStepObjects.general()
        sideMenuStepObjects.launchOnSystemStartUp()
//        goToSettings(sideMenuPageObject.general)
        sideMenuStepObjects.back()
        sideMenuStepObjects.back()
        sideMenuStepObjects.hamburgerMenu()
        sideMenuStepObjects.logout()
        //login
        stepObjects.allowNotifications()
        stepObjects.reachSignInScreen()
        stepObjects.enterCredentials()
        stepObjects.clickOnLoginButton()
        stepObjects.allowVpnProfileCreation()
        sideMenuStepObjects.hamburgerMenu()
        sideMenuStepObjects.settings()
        sideMenuStepObjects.general()
//        goToSettings(sideMenuPageObject.general)
        assert(!SideMenuPageObjects().toggleLaunchOnSystemStartUp.isChecked())
    }
}