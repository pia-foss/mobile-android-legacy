package com.privateinternetaccess.android

/*
 *  Copyright (c) 2021 Private Internet Access, Inc.
 *
 *  This file is part of the Private Internet Access Android Client.
 *
 *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with the Private
 *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 */

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.privateinternetaccess.android.pia.PIAFactory
import com.privateinternetaccess.android.pia.handlers.PIAServerHandler
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.kpi.KPIManager
import com.privateinternetaccess.android.pia.model.enums.RequestResponseStatus
import com.privateinternetaccess.android.pia.utils.DLog
import com.privateinternetaccess.android.pia.utils.Prefs


class PIALifecycleObserver(private val context: Context) : LifecycleObserver {

    companion object {
        private const val TAG = "PIALifecycleObserver"
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForeground() {
        DLog.d(TAG, "Application Foregrounded")
        val account = PIAFactory.getInstance().getAccount(context)
        if (account.loggedIn()) {
            PIAServerHandler.startup(context)
            PIAServerHandler.getInstance(context).triggerLatenciesUpdate()
            migrateApiTokenIfNeeded()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onBackground() {
        DLog.d(TAG, "Application Backgrounded")
        KPIManager.sharedInstance.flush()
    }

    // region private
    private fun migrateApiTokenIfNeeded() {
        val oldApiToken = Prefs.with(context).get(PiaPrefHandler.TOKEN, "")
        if (oldApiToken.isNullOrEmpty()) {
            return
        }

        val account = PIAFactory.getInstance().getAccount(context)
        account.migrateApiToken(oldApiToken) { status: RequestResponseStatus ->
            if (status != RequestResponseStatus.SUCCEEDED) {
                DLog.d(TAG, "migrateApiTokenIfNeeded failed")
                return@migrateApiToken
            }

            Prefs.with(context).remove(PiaPrefHandler.TOKEN)
        }
    }
    // endregion
}
