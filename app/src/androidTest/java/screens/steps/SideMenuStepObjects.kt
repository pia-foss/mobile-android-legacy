package com.privateiinternetaccess.android.screens.steps

import com.privateinternetaccess.android.screens.objects.SideMenuPageObject

class SideMenuStepObjects {

    private val sideMenuPageObject = SideMenuPageObject()

    fun clickOnHamburgerIcon()
    {
        sideMenuPageObject.hamburgerMenu.click()
    }

    fun clickOnSettings()
    {
        sideMenuPageObject.settings.click()
    }

}