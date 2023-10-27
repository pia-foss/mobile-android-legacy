package com.privateinternetaccess.android.screens.steps

import androidx.test.uiautomator.UiObject
import com.privateinternetaccess.android.screens.objects.MainScreenPageObjects
import com.privateinternetaccess.android.core.BaseUiAutomatorClass.Companion.defaultTimeOut

class MainScreenStepObjects {

    private val mainScreenPageObjects = MainScreenPageObjects()

    fun clickOnHamburgerMenu() {
        mainScreenPageObjects.hamburgerMenu.click()
    }

    fun clickConnect() {
        mainScreenPageObjects.connectButton.clickAndWaitForNewWindow(defaultTimeOut)
    }
}