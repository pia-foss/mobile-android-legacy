package com.privateinternetaccess.android.tests

import com.privateinternetaccess.android.BuildConfig
import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.screens.objects.MainScreenPageObjects
import com.privateinternetaccess.android.screens.objects.SignInPageObjects
import com.privateinternetaccess.android.screens.steps.SignInStepObjects
import org.junit.Test

class SignInTests : BaseUiAutomatorClass() {

    private val stepObjects = SignInStepObjects()

    @Test
    fun successfulLoginWithValidCredentials() {
        stepObjects.allowNotifications()
        stepObjects.reachSignInScreen()
        stepObjects.enterUsername(BuildConfig.PIA_VALID_USERNAME)
        stepObjects.enterPassword(BuildConfig.PIA_VALID_PASSWORD)
        stepObjects.clickOnLoginButton()
        stepObjects.allowVpnProfileCreation()
        assert(MainScreenPageObjects().connectButton.exists())
    }

    @Test
    fun incorrectCredentialsReturnToSplashScreen() {
        stepObjects.allowNotifications()
        stepObjects.reachSignInScreen()
        stepObjects.enterUsername("PLACEHOLDER")
        stepObjects.enterPassword("PLACEHOLDER")
        stepObjects.clickOnLoginButton()
        assert(SignInPageObjects().reachLoginScreenButton.exists())
    }

    @Test
    fun errorMessageIfNoCredentialsAreProvided() {
        stepObjects.allowNotifications()
        stepObjects.reachSignInScreen()
        stepObjects.clickOnLoginButton()
        assert(SignInPageObjects().noUsernameOrPasswordError.exists())
    }
}