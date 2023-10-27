package com.privateinternetaccess.android.screens.objects

import com.privateinternetaccess.android.helpers.LocatorHelper
import com.privateinternetaccess.android.helpers.XPathUtil

class DedicatedIPPageObjects {
    val dedicatedIPField = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/snippet_dip_entry_field")
    val activateButton = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/snippet_dip_activate_button")
    val serverFlag = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/list_server_flag")
    val serverName = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/list_server_name")
    val serverIPAddress = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/list_server_ping")
    val toastErrorMessage = XPathUtil.findElementByXPath("//android.widget.Toast[@text='Your token is invalid. Please make sure you have entered the token correctly.']" )
}