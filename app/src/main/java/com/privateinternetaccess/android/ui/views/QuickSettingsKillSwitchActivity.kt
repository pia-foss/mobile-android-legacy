package com.privateinternetaccess.android.ui.views

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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.ui.drawer.settings.SettingsBlockConnectionsFragment
import com.privateinternetaccess.android.ui.superclasses.BaseActivity


class QuickSettingsKillSwitchActivity : BaseActivity() {

    companion object {
        public fun open(context: Context) {
            context.startActivity(Intent(context, QuickSettingsKillSwitchActivity::class.java))
            (context as Activity).overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secondary)
        bootstrapActivity()
        showFragment()
    }

    // region private
    private fun bootstrapActivity() {
        initHeader(true, true)
        title = getString(R.string.menu_settings_privacy)
        setBackground()
        setSecondaryGreenBackground()
    }

    private fun showFragment() {
        supportFragmentManager.beginTransaction().replace(
            R.id.activity_secondary_container,
            SettingsBlockConnectionsFragment()
        ).commit()
    }
    // endregion
}