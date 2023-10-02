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
}