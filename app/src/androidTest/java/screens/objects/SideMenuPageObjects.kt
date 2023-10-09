package com.privateinternetaccess.android.screens.objects //why do we declare this instead of just screens.object when we start createed the file?

import com.privateinternetaccess.android.helpers.LocatorHelper


class SideMenuPageObject {

    val hamburgerMenu = LocatorHelper.findByContentDesc("Open")
    val settings = LocatorHelper.findByText("Settings")
    val general = LocatorHelper.findByText("General")
    val toggleLaunchOnSystemStartUp = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/connectOnBootSwitchSetting")
    val backButton = LocatorHelper.findByContentDesc("Navigate up")
    val logout = LocatorHelper.findByText("Log out")
    val logoutConfirm = LocatorHelper.findByResourceId("android:id/button1")


}