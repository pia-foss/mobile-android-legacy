package com.privateinternetaccess.android.helpers

import androidx.test.uiautomator.UiObject
import com.privateinternetaccess.android.core.BaseUiAutomatorClass.Companion.defaultTimeOut

object ActionHelpers {

    fun clickIfExists(primaryUiObject : UiObject, secondaryUiObj: UiObject? = null) {
        if (primaryUiObject.exists()) {
            (secondaryUiObj ?: primaryUiObject).clickAndWaitForNewWindow(defaultTimeOut)
        }
    }

    fun <T> inputTextInField(field: UiObject, data: T? = null) {
        field.clearTextField()
        field.click()
        field.text = data?.toString() ?: ""
    }
}