package com.privateinternetaccess.android.screens.steps

import com.privateinternetaccess.android.helpers.ActionHelpers.inputTextInField
import com.privateinternetaccess.android.screens.objects.DedicatedIPPageObjects
import com.privateinternetaccess.android.core.BaseUiAutomatorClass.Companion.defaultTimeOut


class DedicatedIPStepObjects {

    val dedicatedIPPageObjects = DedicatedIPPageObjects()

    fun enterDedicatedIPToken(DIPToken : String) {
        inputTextInField(dedicatedIPPageObjects.dedicatedIPField, DIPToken)
        dedicatedIPPageObjects.activateButton.click()
        dedicatedIPPageObjects.activateButton.waitForExists(defaultTimeOut)
    }
}