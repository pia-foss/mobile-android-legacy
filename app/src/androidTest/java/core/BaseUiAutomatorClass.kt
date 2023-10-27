package com.privateinternetaccess.android.core

import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Before

open class BaseUiAutomatorClass {

    protected val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private lateinit var context: Context

    private fun startApp(packageName: String, activityName: String? = null) {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = if (activityName == null) {
            context.packageManager.getLaunchIntentForPackage(packageName)
        } else {
            Intent().setClassName(packageName, activityName)
        }
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear out any previous instances
        context.startActivity(intent)
    }

    @Before
    fun setUp() {
        device.pressHome()
        startApp("com.privateinternetaccess.android")
    }

    companion object {
        const val defaultTimeOut = 5000L
    }
}