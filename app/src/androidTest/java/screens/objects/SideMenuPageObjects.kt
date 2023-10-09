package com.privateinternetaccess.android.screens.objects

import com.privateinternetaccess.android.helpers.LocatorHelper


class SideMenuPageObjects {

    val hamburgerMenu = LocatorHelper.findByContentDesc("Open")
    val settings = LocatorHelper.findByText("Settings")
    val general = LocatorHelper.findByText("General")
    val toggleLaunchOnSystemStartUp = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/connectOnBootSwitchSetting")
    val backButton = LocatorHelper.findByContentDesc("Navigate up")
    val logout = LocatorHelper.findByText("Log out")
    val logoutConfirm = LocatorHelper.findByResourceId("android:id/button1")


}