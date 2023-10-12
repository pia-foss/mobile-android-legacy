package com.privateinternetaccess.android.screens.steps

import com.privateinternetaccess.android.screens.objects.SideMenuPageObjects

class SideMenuStepObjects {

    private val sideMenuPageObjects = SideMenuPageObjects()

    fun clickOnDedicatedIP(){
        sideMenuPageObjects.dedicateIP.click()
    }

    fun clickOnSettings() {
        sideMenuPageObjects.settings.click()
    }

    fun clickOnLogout() {
        sideMenuPageObjects.logout.click()
        sideMenuPageObjects.logoutConfirm.click()
    }
}
