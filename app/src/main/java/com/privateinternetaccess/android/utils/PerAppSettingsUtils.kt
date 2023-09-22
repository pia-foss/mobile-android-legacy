package com.privateinternetaccess.android.utils

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

object PerAppSettingsUtils {

    fun getInstalledApps(packageManager: PackageManager): List<ApplicationInfo> =
        packageManager.getPackagesHoldingPermissions(
            arrayOf(Manifest.permission.INTERNET),
            0
        ).map {
            it.applicationInfo
        }

    fun containsPackageName(apps: List<ApplicationInfo>, packageName: String): Boolean {
        return apps.find { it.packageName == packageName } != null
    }
}