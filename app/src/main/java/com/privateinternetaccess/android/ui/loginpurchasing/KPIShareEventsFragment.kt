package com.privateinternetaccess.android.ui.loginpurchasing

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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.kpi.KPIManager
import com.privateinternetaccess.android.ui.drawer.KPIShareEventsMoreFragment


class KPIShareEventsFragment : Fragment() {

    private var shareEventsMoreTextView: TextView? = null
    private var shareEventsAcceptButton: Button? = null
    private var shareEventsDeclineButton: Button? = null
    private val shareEventsMoreFragment = KPIShareEventsMoreFragment()

    var fireOffPurchasing: Boolean = false
    var isTrial: Boolean = false
    var isLoginWithReceipt: Boolean = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_kpi_share_events, container, false)
        prepareViews(view)
        prepareFindOutMoreClickListener()
        preparePositiveClickListener()
        prepareNegativeClickListener()
        return view
    }

    override fun onResume() {
        super.onResume()
        prepareViews(view)
        preparePositiveClickListener()
        prepareNegativeClickListener()
    }

    // region private
    private fun prepareViews(view: View?) {
        shareEventsMoreTextView = view?.findViewById(R.id.fragment_kpi_share_events_more)
        shareEventsAcceptButton = view?.findViewById(R.id.fragment_kpi_share_events_accept)
        shareEventsDeclineButton = view?.findViewById(R.id.fragment_kpi_share_events_decline)
    }

    private fun prepareFindOutMoreClickListener() {
        shareEventsMoreTextView?.setOnClickListener {
            shareEventsMoreFragment.show(
                    requireActivity().supportFragmentManager,
                    shareEventsMoreFragment.tag
            )
        }
    }

    private fun preparePositiveClickListener() {
        shareEventsAcceptButton?.setOnClickListener {
            PiaPrefHandler.setKpiShareConnectionEventsEnabled(context, true)
            KPIManager.sharedInstance.start()
            switchToPurchasingProcess()
        }
    }

    private fun prepareNegativeClickListener() {
        shareEventsDeclineButton?.setOnClickListener {
            PiaPrefHandler.setKpiShareConnectionEventsEnabled(context, false)
            KPIManager.sharedInstance.stop()
            switchToPurchasingProcess()
        }
    }

    private fun switchToPurchasingProcess() {
        val frag = PurchasingProcessFragment()
        frag.setFirePurchasing(fireOffPurchasing)
        frag.setTrial(isTrial)
        frag.setLoginWithReceipt(isLoginWithReceipt)
        fragmentManager?.beginTransaction()?.add(R.id.container, frag)?.commit()
    }
    // endregion
}
