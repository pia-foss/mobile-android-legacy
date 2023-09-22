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
import androidx.fragment.app.Fragment
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.kpi.KPIManager
import com.privateinternetaccess.android.ui.drawer.KPIShareEventsMoreFragment


class KPIShareEventsFragment : Fragment() {

    private var shareEventsMoreTextView: Button? = null
    private var shareEventsAcceptButton: Button? = null
    private var shareEventsDeclineButton: Button? = null
    private val shareEventsMoreFragment = KPIShareEventsMoreFragment()

    var fireOffPurchasing: Boolean = false
    var isTrial: Boolean = false
    var isLoginWithReceipt: Boolean = false

    private var savedFocusedView: Int = 0

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
        prepareFocusListeners()
        savedFocusedView = savedInstanceState?.getInt(SAVED_FOCUSED_VIEW, 0) ?: savedFocusedView
        return view
    }

    override fun onStart() {
        super.onStart()
        restoreFocusedView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVED_FOCUSED_VIEW, savedFocusedView)
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

    private fun prepareFocusListeners() {
        val focusListener = View.OnFocusChangeListener focus@{ view: View, hasFocus: Boolean ->
            if (hasFocus.not()) {
                return@focus
            }
            val id: Int = view.id
            savedFocusedView = when (id) {
                R.id.fragment_kpi_share_events_more -> id
                R.id.fragment_kpi_share_events_accept -> id
                R.id.fragment_kpi_share_events_decline -> id
                else -> 0
            }
        }

        shareEventsMoreTextView?.onFocusChangeListener = focusListener
        shareEventsAcceptButton?.onFocusChangeListener = focusListener
        shareEventsDeclineButton?.onFocusChangeListener = focusListener
    }

    private fun restoreFocusedView() {
        val focusedView: View? = when (savedFocusedView) {
            R.id.fragment_kpi_share_events_more -> shareEventsMoreTextView
            R.id.fragment_kpi_share_events_decline -> shareEventsDeclineButton
            else -> shareEventsAcceptButton
        }
        focusedView?.requestFocus()
    }

    private fun switchToPurchasingProcess() {
        val frag = PurchasingProcessFragment()
        frag.setFirePurchasing(fireOffPurchasing)
        frag.setTrial(isTrial)
        frag.setLoginWithReceipt(isLoginWithReceipt)
        fragmentManager?.beginTransaction()?.add(R.id.container, frag)?.commit()
    }
    // endregion

    companion object {
        private const val SAVED_FOCUSED_VIEW: String = "savedFocusedView"
    }
}
