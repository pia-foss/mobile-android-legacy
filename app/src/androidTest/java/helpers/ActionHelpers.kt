package com.privateinternetaccess.android.helpers

import androidx.test.uiautomator.UiObject

object ActionHelpers {

    fun clickIfExists(primaryUiObject : UiObject, secondaryUiObj: UiObject? = null) {
        if (primaryUiObject.exists()) {
            (secondaryUiObj ?: primaryUiObject).click()
        }
    }

    fun <T> inputTextInField(field: UiObject, data: T? = null) {
        field.clearTextField()
        field.click()
        field.text = data?.toString() ?: ""
    }
}