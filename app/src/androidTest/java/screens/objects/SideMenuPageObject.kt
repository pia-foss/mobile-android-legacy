package com.privateinternetaccess.android.screens.objects //why do we declare this instead of just screens.object when we start createed the file?

import com.privateinternetaccess.android.helpers.LocatorHelper


class SideMenuPageObject {

    val hamburgerMenu = LocatorHelper.findByContentDesc("Open")
}