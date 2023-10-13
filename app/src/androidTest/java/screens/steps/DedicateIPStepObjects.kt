package com.privateinternetaccess.android.screens.steps

import com.privateinternetaccess.android.BuildConfig
import com.privateinternetaccess.android.helpers.ActionHelpers.inputTextInField
import com.privateinternetaccess.android.screens.objects.DedicateIPPageObjects

class DedicateIPStepObjects {

    val dedicateIPPageObjects = DedicateIPPageObjects()

    fun enterDedicateIP(DIPToken : String = BuildConfig.PIA_VALID_DIP_TOKEN ) {
        inputTextInField(dedicateIPPageObjects.dedicateIPField, DIPToken)
    }

    fun activateButton() {
        dedicateIPPageObjects.activateButton.click()
        dedicateIPPageObjects.activateButton.waitForExists(5000)
    }

}