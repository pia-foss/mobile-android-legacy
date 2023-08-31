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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.databinding.FragmentNoInAppPurchasingBinding
import com.privateinternetaccess.android.ui.features.WebviewActivity


class NoInAppPurchasingFragment : Fragment() {

    companion object {

        fun open(fragmentTransaction: FragmentTransaction) {
            fragmentTransaction.setCustomAnimations(
                R.anim.left_to_right,
                R.anim.right_to_left,
                R.anim.right_to_left_exit,
                R.anim.left_to_right_exit
            )
            fragmentTransaction.replace(R.id.container, NoInAppPurchasingFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

    private lateinit var binding: FragmentNoInAppPurchasingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoInAppPurchasingBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            prepareClickListeners(it)
        }
    }

    // region private
    private fun prepareClickListeners(context: Context) {
        binding.noInAppRenewalButton.setOnClickListener {
            navigateToSubscriptionSite(context)
        }
    }

    private fun navigateToSubscriptionSite(context: Context) {
        val intent = Intent(context.applicationContext, WebviewActivity::class.java)
        intent.putExtra(WebviewActivity.EXTRA_URL, WebviewActivity.SUBSCRIPTION_OVERVIEW_SITE)
        startActivity(intent)
    }
    // endregion
}