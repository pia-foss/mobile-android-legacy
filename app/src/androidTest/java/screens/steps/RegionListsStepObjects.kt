package com.privateinternetaccess.android.screens.steps

import androidx.test.uiautomator.By
import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import com.privateinternetaccess.android.helpers.ActionHelpers.inputTextInField
import com.privateinternetaccess.android.screens.objects.RegionListsPageObjects
import com.privateinternetaccess.android.helpers.LocatorHelper.findByText

class RegionListsStepObjects : BaseUiAutomatorClass() {
    private val serverListPageObjects = RegionListsPageObjects()

    fun regionInput (region : String) {
        inputTextInField(serverListPageObjects.searchField, region)
    }

    /* This function will select a random region within the range of the list  we extracted.
        This is to make our test less flaky, instead of relying on a hard coded region name since
        region lists depends on where you are currently located.
     */

    fun selectFromVisibleRegionName() : String {
        val visibleTextElements = device.findObjects(By.clazz("android.widget.TextView").res("com.privateinternetaccess.android:id/list_server_name"))
        val visibleTextList = mutableListOf<String>()

        for(textElement in visibleTextElements) {
            val text = textElement.text
            if(text != null) visibleTextList.add(text)
        }

        val listSize = visibleTextList.size
        val randomIndex = kotlin.random.Random.nextInt(listSize)
        findByText(visibleTextList[randomIndex]).clickAndWaitForNewWindow(defaultTimeOut)

        return visibleTextList[randomIndex]
    }
}