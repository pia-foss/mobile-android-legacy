package com.privateinternetaccess.android.ui.drawer

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
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.pia.kpi.KPIManager
import com.privateinternetaccess.android.ui.superclasses.BaseActivity


class KPIShareEventsDetailsActivity : BaseActivity() {

    companion object {
        fun open(context: Context) {
            context.startActivity(Intent(context, KPIShareEventsDetailsActivity::class.java))
        }
    }

    private lateinit var kpiShareEventsLinearLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kpi_events_details)
        initHeader(true, true)
        title = getString(R.string.kpi_connection_attempts_stats)
        setBackground()
        setSecondaryGreenBackground()
    }

    override fun onResume() {
        super.onResume()
        populateKnownEvents()
    }

    // region private
    private fun populateKnownEvents() {
        kpiShareEventsLinearLayout = findViewById(R.id.kpiShareEventsLinearLayout)
        KPIManager.sharedInstance.recentEvents { events ->
            val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )

            if (events.isEmpty()) {
                val eventTextView = TextView(this)
                eventTextView.setTextColor(ContextCompat.getColor(this, R.color.grey55))
                eventTextView.layoutParams = params
                eventTextView.gravity = Gravity.CENTER
                eventTextView.text = getString(R.string.kpi_connection_attempts_no_events)
                kpiShareEventsLinearLayout.addView(eventTextView)
            }

            for (event in events) {
                val eventTextView = TextView(this)
                eventTextView.setTextColor(ContextCompat.getColor(this, R.color.grey55))
                eventTextView.layoutParams = params
                eventTextView.text = "$event\n"
                kpiShareEventsLinearLayout.addView(eventTextView)
            }
        }
    }
    // endregion
}
