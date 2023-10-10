package com.privateinternetaccess.android.screens.objects

import com.privateinternetaccess.android.helpers.LocatorHelper

class SideMenuPageObjects {

    val settings = LocatorHelper.findByText("Settings")
    val logout = LocatorHelper.findByText("Log out")
    val logoutConfirm = LocatorHelper.findByResourceId("android:id/button1")
}