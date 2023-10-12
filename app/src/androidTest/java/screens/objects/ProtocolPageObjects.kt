package com.privateinternetaccess.android.screens.objects

import androidx.test.uiautomator.UiSelector
import com.privateinternetaccess.android.helpers.LocatorHelper

class ProtocolPageObjects {

    val protocolSettings = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/protocolsSettings")
    val protocolSelection = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/protocolSetting")
    val openVPN = LocatorHelper.selectFromRadioButton("OpenVPN")
    val wireGuard = LocatorHelper.selectFromRadioButton("WireGuard")
    val save = LocatorHelper.findByResourceId("android:id/button1")
}