package com.privateinternetaccess.android.screens.objects

import com.privateinternetaccess.android.helpers.LocatorHelper

class SettingsPageObjects {

    val general = LocatorHelper.findByText("General", 1)
    val backArrowButton = LocatorHelper.findByContentDesc("Navigate up")
}