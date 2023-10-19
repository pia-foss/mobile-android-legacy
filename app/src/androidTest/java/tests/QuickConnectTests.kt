package com.privateinternetaccess.android.tests

import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLogin
import com.privateinternetaccess.android.screens.objects.MainScreenPageObjects
import com.privateinternetaccess.android.screens.steps.MainScreenStepObjects
import com.privateinternetaccess.android.helpers.ActionHelpers.clickIfExists
import org.junit.Test

class QuickConnectTests: BaseUiAutomatorClass() {

    private val mainScreenStepObjects = MainScreenStepObjects()
    private val mainScreenPageObjects = MainScreenPageObjects()

    @Test
    fun quickConnectConnectivityWhenFlagIsClicked()
    {
        successfulLogin()
        clickIfExists(mainScreenPageObjects.quickConnectFlag1)
        assert(MainScreenPageObjects().connectButton.contentDescription.equals("VPN Connected"))
    }

    @Test
    fun quickConnectConnectivityWhenTextIsClicked()
    {
        successfulLogin()
        clickIfExists(mainScreenPageObjects.quickConnectText1)
        assert(MainScreenPageObjects().connectButton.contentDescription.equals("VPN Connected"))
    }
}