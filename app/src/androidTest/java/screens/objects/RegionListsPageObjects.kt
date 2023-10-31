package com.privateinternetaccess.android.screens.objects

import com.privateinternetaccess.android.helpers.LocatorHelper

class RegionListsPageObjects {
    val searchField = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/search")
    val regionName = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/list_server_name")
    val regionNoResultImage = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/server_select_no_results")
}