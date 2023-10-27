package com.privateinternetaccess.android.tests

import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLogin
import com.privateinternetaccess.android.helpers.ActionHelpers.goToSideMenu
import com.privateinternetaccess.android.screens.objects.DedicatedIPPageObjects
import com.privateinternetaccess.android.screens.objects.SideMenuPageObjects
import com.privateinternetaccess.android.screens.steps.DedicatedIPStepObjects
import com.privateinternetaccess.android.BuildConfig
import org.junit.Test
import org.junit.Assert

class DedicatedIPTests : BaseUiAutomatorClass() {

    private val dedicatedIPStepObjects = DedicatedIPStepObjects()
    private val sideMenuPageObjects = SideMenuPageObjects()

    @Test
    fun oneDIPTokenAccepted() {
        successfulLogin()
        goToSideMenu(sideMenuPageObjects.dedicatedIP)
        dedicatedIPStepObjects.enterDedicatedIPToken(BuildConfig.PIA_VALID_DIP_TOKEN )
        val conditions = listOf(
            { !DedicatedIPPageObjects().dedicatedIPField.exists() },
            { DedicatedIPPageObjects().serverFlag.exists() },
            { DedicatedIPPageObjects().serverName.exists() },
            { DedicatedIPPageObjects().serverIPAddress.exists() }
        )
        for (condition in conditions) {
            Assert.assertTrue(condition())
        }
    }

    @Test
    fun invalidDIPTokenValidation() {
        successfulLogin()
        goToSideMenu(sideMenuPageObjects.dedicatedIP)
        dedicatedIPStepObjects.enterDedicatedIPToken("invalidToken")
        DedicatedIPPageObjects().toastErrorMessage?.let { assert(it.exists()) }
    }
}