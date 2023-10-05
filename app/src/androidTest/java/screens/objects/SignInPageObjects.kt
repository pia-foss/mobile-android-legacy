package com.privateinternetaccess.android.screens.objects

import com.privateinternetaccess.android.helpers.LocatorHelper

class SignInPageObjects {

    val notificationPrompt = LocatorHelper.findByResourceId("com.android.permissioncontroller:id/permission_message")
    val allowNotifications = LocatorHelper.findByResourceId("com.android.permissioncontroller:id/permission_allow_button")
    val denyNotifications = LocatorHelper.findByResourceId("com.android.permissioncontroller:id/permission_deny_button")
    val reachLoginScreenButton = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/login")
    val usernameField = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/piaxEditText")
    val passwordField = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/piaxEditText", instance = 1)
    val loginButton = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/fragment_login_button")
    val vpnProfileOkButton = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/activity_vpn_permissions_button")
    val androidOkButton = LocatorHelper.findByResourceId("android:id/button1")

    val loginWithReceipt = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/fragment_login_receipt")

    val subscribeButton = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/subscribe")

    val noUsernameOrPasswordError = LocatorHelper.findByResourceId("com.privateinternetaccess.android:id/piaxEditTextError")
}