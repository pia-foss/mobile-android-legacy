package com.privateinternetaccess.android.screens.steps

import com.privateinternetaccess.android.screens.objects.SettingsPageObjects
import com.privateinternetaccess.android.screens.objects.GeneralSettingsPageObjects


class GeneralSettingslStepObjects {

    val generalPageObjects = GeneralSettingsPageObjects()
    val settingsPageObjects = SettingsPageObjects()

    fun launchOnSystemStartUp() {
        generalPageObjects.toggleLaunchOnSystemStartUp.click()
    }

    fun clickOnBackArrow() {
        settingsPageObjects.backArrowButton.click()
    }

}