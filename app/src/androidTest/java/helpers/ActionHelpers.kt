package com.privateinternetaccess.android.helpers

import androidx.test.uiautomator.UiObject
import com.privateinternetaccess.android.core.BaseUiAutomatorClass.Companion.defaultTimeOut
import com.privateinternetaccess.android.screens.objects.MainScreenPageObjects
import com.privateinternetaccess.android.screens.steps.GeneralSettingslStepObjects
import com.privateinternetaccess.android.screens.steps.SideMenuStepObjects
import com.privateinternetaccess.android.screens.steps.SignInStepObjects
import com.privateinternetaccess.android.screens.steps.MainScreenStepObjects

object ActionHelpers {
    fun clickIfExists(primaryUiObject : UiObject, secondaryUiObj: UiObject? = null) {
        if (primaryUiObject.exists()) {
            (secondaryUiObj ?: primaryUiObject).clickAndWaitForNewWindow(defaultTimeOut)
        }
    }

    fun <T> inputTextInField(field: UiObject, data: T? = null) {
        field.click()
        field.text = data?.toString() ?: ""
    }

    fun successfulLogin() {
        SignInStepObjects().allowNotifications()
        SignInStepObjects().reachSignInScreen()
        SignInStepObjects().enterCredentials()
        SignInStepObjects().clickOnLoginButton()
        SignInStepObjects().allowVpnProfileCreation()
    }

    fun goToSettings(primarySettingsSection : UiObject, secondarySettingsSection : UiObject? = null ) {
        MainScreenStepObjects().clickOnHamburgerMenu()
        SideMenuStepObjects().clickOnSettings()
        primarySettingsSection.click()
        secondarySettingsSection?.click()
    }

    fun successfulLogout()
    {
        MainScreenStepObjects().clickOnHamburgerMenu()
        SideMenuStepObjects().clickOnLogout()
    }

    fun returnOnMainScreen() {
        while (!MainScreenPageObjects().connectButton.exists()) {
            GeneralSettingslStepObjects().clickOnBackArrow()
        }
    }

    fun goToSideMenu(sideMenuOption : UiObject) {
        MainScreenStepObjects().clickOnHamburgerMenu()
        sideMenuOption.click()
    }

}