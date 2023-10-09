package com.privateinternetaccess.android.screens.steps

import com.privateinternetaccess.android.screens.objects.SideMenuPageObjects

class SideMenuStepObjects {

    private val sideMenuPageObject = SideMenuPageObjects()

    fun hamburgerMenu()
    {
        sideMenuPageObject.hamburgerMenu.click()
    }

    fun settings()
    {
        sideMenuPageObject.settings.click()
    }

    fun general()
    {
        sideMenuPageObject.general.click()
    }

    fun launchOnSystemStartUp()
    {
        sideMenuPageObject.toggleLaunchOnSystemStartUp.click()
    }

    fun back()
    {
        sideMenuPageObject.backButton.click()
    }

    fun logout()
    {
        sideMenuPageObject.logout.click()
        sideMenuPageObject.logoutConfirm.click()
    }
}
