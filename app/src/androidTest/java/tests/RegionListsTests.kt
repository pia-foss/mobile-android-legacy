package com.privateinternetaccess.android.tests

import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.helpers.ActionHelpers.successfulLogin
import com.privateinternetaccess.android.screens.objects.MainScreenPageObjects
import com.privateinternetaccess.android.screens.objects.RegionListsPageObjects
import com.privateinternetaccess.android.screens.steps.RegionListsStepObjects
import org.junit.Test
import org.junit.Assert

class RegionListsTests : BaseUiAutomatorClass() {

    val mainScreenPageObjects = MainScreenPageObjects()
    val regionListsStepObjects = RegionListsStepObjects()
    val regionListsPageObjects = RegionListsPageObjects()

    @Test
    fun searchValidRegion () {
        successfulLogin()
        mainScreenPageObjects.currentRegion.click()
        regionListsStepObjects.regionInput("Belgium")
        assert(regionListsPageObjects.regionName.text.equals("Belgium"))
    }

    @Test
    fun searchInvalidRegion () {
        successfulLogin()
        mainScreenPageObjects.currentRegion.click()
        regionListsStepObjects.regionInput("invalidRegion")
        assert(regionListsPageObjects.regionNoResultImage.exists())
    }

    @Test
    fun connectToRegion() {
        successfulLogin()
        mainScreenPageObjects.currentRegion.click()
        val selectedRegion = regionListsStepObjects.selectFromVisibleRegionName()

        val conditions = listOf(
            {mainScreenPageObjects.regionNameSelected.text.equals(selectedRegion)},
            {mainScreenPageObjects.connectButton.contentDescription.equals("VPN Connected")},
        )
        for (condition in conditions) {
            Assert.assertTrue(condition())
        }
    }
}