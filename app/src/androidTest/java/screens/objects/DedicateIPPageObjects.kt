package com.privateinternetaccess.android.screens.objects

import com.privateinternetaccess.android.helpers.LocatorHelper

class DedicateIPPageObjects {
    val dedicateIPField = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/snippet_dip_entry_field")
    val activateButton = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/snippet_dip_activate_button")
    val serverName = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/list_server_name")
}