package com.privateinternetaccess.android.screens.steps

import com.privateinternetaccess.android.BuildConfig
import com.privateinternetaccess.android.helpers.ActionHelpers.inputTextInField
import com.privateinternetaccess.android.screens.objects.DedicateIPPageObjects
import com.privateinternetaccess.android.core.BaseUiAutomatorClass.Companion.defaultTimeOut


class DedicateIPStepObjects {

    val dedicateIPPageObjects = DedicateIPPageObjects()

    fun enterDedicatedIP(DIPToken : String = BuildConfig.PIA_VALID_DIP_TOKEN ) {
        inputTextInField(dedicateIPPageObjects.dedicateIPField, DIPToken)
        dedicateIPPageObjects.activateButton.click()
        dedicateIPPageObjects.activateButton.waitForExists(defaultTimeOut)
    }
}