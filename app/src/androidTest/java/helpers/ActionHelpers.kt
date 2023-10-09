package com.privateinternetaccess.android.helpers

import androidx.test.uiautomator.UiObject
import com.privateinternetaccess.android.core.BaseUiAutomatorClass.Companion.defaultTimeOut
import com.privateinternetaccess.android.screens.steps.SideMenuStepObjects
import com.privateinternetaccess.android.screens.steps.SignInStepObjects

object ActionHelpers {

    fun clickIfExists(primaryUiObject : UiObject, secondaryUiObj: UiObject? = null) {
        if (primaryUiObject.exists()) {
            (secondaryUiObj ?: primaryUiObject).clickAndWaitForNewWindow(defaultTimeOut)
        }
    }

    fun <T> inputTextInField(field: UiObject, data: T? = null) {
        field.clearTextField()
        field.click()
        field.text = data?.toString() ?: ""
    }

    fun userLoginSuccess() {
        SignInStepObjects().allowNotifications()
        SignInStepObjects().reachSignInScreen()
        SignInStepObjects().enterCredentials()
        SignInStepObjects().clickOnLoginButton()
        SignInStepObjects().allowVpnProfileCreation()
    }

    fun goToSettings(primarySettingsSection : UiObject, secondarySettingsSection : UiObject? = null ) {
        SideMenuStepObjects().hamburgerMenu()
        SideMenuStepObjects().settings()

        if (primarySettingsSection.exists())
        {
            primarySettingsSection.click()
        }
        if (secondarySettingsSection != null)
        {
            secondarySettingsSection.click()
        }
    }
}