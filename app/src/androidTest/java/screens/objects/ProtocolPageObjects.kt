package com.privateinternetaccess.android.screens.objects

import androidx.test.uiautomator.UiSelector
import com.privateinternetaccess.android.helpers.LocatorHelper

class ProtocolPageObjects {

    val protocolSettings = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/protocolsSettings")
    val protocolSelection = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/protocolSetting")
    val openVPN = LocatorHelper.selectDropDownList("com.privateinternetaccess.android:id/list_settings_radio", 0)
    val save = LocatorHelper.findByResourceId("android:id/button1")
}