package com.privateinternetaccess.android.helpers

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector

object LocatorHelper {

    private val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun findByResourceId(id: String, instance: Int = 0): UiObject {
        return device.findObject(UiSelector().resourceId(id).instance(instance))
    }

    fun findByContentDesc(description: String): UiObject {
        return device.findObject(UiSelector().description(description))
    }

    fun findByText(text : String) : UiObject {
        return device.findObject(UiSelector().text(text))
    }

    fun selectFromRadioButton(text : String) : UiObject {
        return device.findObject(UiSelector().textMatches(text))
    }
}