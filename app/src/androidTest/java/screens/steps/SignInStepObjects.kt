package com.privateinternetaccess.android.screens.steps

import com.privateinternetaccess.android.helpers.ActionHelpers.clickIfExists
import com.privateinternetaccess.android.helpers.ActionHelpers.inputTextInField
import com.privateinternetaccess.android.screens.objects.SignInPageObjects
import com.privateinternetaccess.android.core.BaseUiAutomatorClass.Companion.defaultTimeOut

class SignInStepObjects {

    private val signInPageObjects = SignInPageObjects()

    fun allowNotifications() {
        clickIfExists(signInPageObjects.notificationPrompt, signInPageObjects.allowNotifications)
    }

    fun reachSignInScreen() {
        signInPageObjects.reachLoginScreenButton.clickAndWaitForNewWindow(defaultTimeOut)
    }

    fun enterUsername(username : String?) {
        inputTextInField(signInPageObjects.usernameField, username)
    }

    fun enterPassword(password : String?) {
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