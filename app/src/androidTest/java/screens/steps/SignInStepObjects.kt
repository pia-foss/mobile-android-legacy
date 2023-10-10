package com.privateinternetaccess.android.screens.steps

import com.privateinternetaccess.android.BuildConfig
import com.privateinternetaccess.android.helpers.ActionHelpers.clickIfExists
import com.privateinternetaccess.android.helpers.ActionHelpers.inputTextInField
import com.privateinternetaccess.android.screens.objects.SignInPageObjects
import com.privateinternetaccess.android.core.BaseUiAutomatorClass.Companion.defaultTimeOut

class SignInStepObjects {

    private val signInPageObjects = SignInPageObjects()

    fun allowNotifications() {
        clickIfExists(signInPageObjects.allowNotifications, signInPageObjects.allowNotifications)
    }

    fun reachSignInScreen() {
        signInPageObjects.reachLoginScreenButton.clickAndWaitForNewWindow(defaultTimeOut)
    }

    fun enterCredentials(
        username : String = BuildConfig.PIA_VALID_USERNAME, 
        password : String = BuildConfig.PIA_VALID_PASSWORD
    ) {
        inputTextInField(signInPageObjects.usernameField, username)
        inputTextInField(signInPageObjects.passwordField, password)
    }

    fun clickOnLoginButton() {
        signInPageObjects.loginButton.clickAndWaitForNewWindow(defaultTimeOut)
    }

    fun allowVpnProfileCreation() {
        clickIfExists(signInPageObjects.vpnProfileOkButton)
        clickIfExists(signInPageObjects.androidOkButton)
    }
}