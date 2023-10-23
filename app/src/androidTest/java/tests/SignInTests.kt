package com.privateinternetaccess.android.tests

import android.util.Log
import com.privateinternetaccess.android.BuildConfig
import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.core.DataFactory
import com.privateinternetaccess.android.screens.objects.MainScreenPageObjects
import com.privateinternetaccess.android.screens.objects.SignInPageObjects
import com.privateinternetaccess.android.screens.steps.SignInStepObjects
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SignInTests(private val username: String, private val password: String, private val expectedOutcome: String) : BaseUiAutomatorClass() {

    private val stepObjects = SignInStepObjects()

    /**
     * A companion object for the [loginDataDrivenTests] containing a method to provide parameterized test data.
     *
     * This object is responsible for loading test data from JSON and mapping it to an array of usernames and passwords.
     */
    companion object {
        /**
         * @return A collection of arrays, where each array contains a username and a password.
         */
        @JvmStatic
        @Parameterized.Parameters()
        fun data(): Collection<Array<String>> {
            val jsonTestData = """
                [
                    {"username": "${BuildConfig.PIA_VALID_USERNAME}", "password": "${BuildConfig.PIA_VALID_PASSWORD}", "expectedOutcome": "valid"},
                    {"username": "user", "password": "pass", "expectedOutcome": "invalid"},
                    {"username": "", "password": "", "expectedOutcome": "noCredentials"},
                    {"username": "${BuildConfig.PIA_VALID_USERNAME}", "password": "", "expectedOutcome": "noCredentials"},
                    {"username": "", "password": "${BuildConfig.PIA_VALID_PASSWORD}", "expectedOutcome": "noCredentials"}
                ]
            """
            val testData = DataFactory.loadTestDataFromJson(jsonTestData)
            return testData.map { arrayOf(it.username, it.password, it.expectedOutcome) }
        }
    }

    /**
     * A data-driven test that covers the Sign In flows and it's outcomes
     * based on the set of credentials used through an array.
     *
     * Covers and asserts login with a valid account, with invalid credentials,
     * and without providing an username or password, or both.
     *
     */
    @Test
    fun loginDataDrivenTests() {
        stepObjects.allowNotifications()
        stepObjects.reachSignInScreen()
        stepObjects.enterCredentials(username, password)
        stepObjects.clickOnLoginButton()
        stepObjects.allowVpnProfileCreation()

        when (expectedOutcome) {
            "valid" -> {
                Log.i("Login Test", "Test with valid credentials passed")
                assert(MainScreenPageObjects().connectButton.exists())
            }
            "invalid" -> {
                Log.i("Login Test", "Test with invalid credentials passed")
                assert(SignInPageObjects().reachLoginScreenButton.exists())
            }
            "noCredentials" -> {
                Log.i("Login Test", "Test without credentials passed")
                assert(SignInPageObjects().noUsernameOrPasswordError.exists())
            }
        }
    }
}