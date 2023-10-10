package com.privateinternetaccess.android.screens.objects

import com.privateinternetaccess.android.helpers.LocatorHelper

class MainScreenPageObjects {

    val connectButton = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/connection_background")
    val hamburgerMenu = LocatorHelper.findByContentDesc("Open")
}