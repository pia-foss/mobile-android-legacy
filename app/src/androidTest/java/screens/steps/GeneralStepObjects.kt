package com.privateinternetaccess.android.screens.steps

import com.privateinternetaccess.android.screens.objects.SettingsPageObjects
import com.privateinternetaccess.android.screens.objects.GeneralPageObjects


class GeneralStepObjects {

    val generalPageObjects = GeneralPageObjects()
    val settingsPageObjects = SettingsPageObjects()

    fun launchOnSystemStartUp() {
        generalPageObjects.toggleLaunchOnSystemStartUp.click()
    }

    fun clickOnBackArrow() {
        settingsPageObjects.backArrowButton.click()
    }

}