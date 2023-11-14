package com.privateinternetaccess.android.screens.objects

import com.privateinternetaccess.android.helpers.LocatorHelper

class MainScreenPageObjects {

    val connectButton = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/connection_background")
    val hamburgerMenu = LocatorHelper.findByContentDesc("Open")
    val quickConnectFlag1 = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/quick_server_flag_1")
    val quickConnectText1 = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/quick_server_name_1")
    val currentRegion = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/fragment_connect_flag_area")
    val regionNameSelected = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/fragment_connect_server_name")

}