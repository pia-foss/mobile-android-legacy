package com.privateinternetaccess.android.screens.steps

import com.privateinternetaccess.android.BuildConfig
import com.privateinternetaccess.android.helpers.ActionHelpers.inputTextInField
import com.privateinternetaccess.android.screens.objects.DedicatedIPPageObjects
import com.privateinternetaccess.android.core.BaseUiAutomatorClass.Companion.defaultTimeOut


class DedicatedIPStepObjects {

    val dedicatedIPPageObjects = DedicatedIPPageObjects()

    fun enterDedicatedIP(DIPToken : String = BuildConfig.PIA_VALID_DIP_TOKEN ) {
        inputTextInField(dedicatedIPPageObjects.dedicatedIPField, DIPToken)
        dedicatedIPPageObjects.activateButton.click()
        dedicatedIPPageObjects.activateButton.waitForExists(defaultTimeOut)
    }
}