package com.privateinternetaccess.android.ui.drawer.settings

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
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.databinding.FragmentSettingsSectionAutomationBinding
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.services.AutomationService
import com.privateinternetaccess.android.ui.drawer.TrustedWifiActivity


class SettingsSectionAutomationFragment : Fragment() {

    private lateinit var binding: FragmentSettingsSectionAutomationBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsSectionAutomationBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            (it as SettingsActivity).setTitle(R.string.menu_settings_automation)
            applyPersistedStateToUi(it)
            prepareClickListeners(it)
        }
    }

    // region private
    private fun prepareClickListeners(context: Context) {
        binding.automationSetting.setOnClickListener {
            updateAutomationSettingIfNeeded(context)
        }

        binding.manageAutomationSetting.setOnClickListener {
            startActivity(Intent(context, TrustedWifiActivity::class.java))
        }
    }

    private fun updateAutomationSettingIfNeeded(context: Context) {
        val isAutomationBeingEnabled = !binding.automationSwitchSetting.isChecked

        // If the user is enabling the feature but we don't have the permissions.
        // Take the user to the permission screen.
        if (isAutomationBeingEnabled && !TrustedWifiActivity.hasTheRequiredPermissions(context)) {
            startActivity(Intent(context, TrustedWifiActivity::class.java))
            return
        }

        PiaPrefHandler.setNetworkManagementEnabled(context, isAutomationBeingEnabled);
        applyPersistedStateToUi(context)
        updateAutomationService(context)
    }

    private fun applyPersistedStateToUi(context: Context) {
        binding.automationSwitchSetting.isChecked = PiaPrefHandler.isNetworkManagementEnabled(context)
        binding.manageAutomationSetting.visibility = if (PiaPrefHandler.isNetworkManagementEnabled(context)) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun updateAutomationService(context: Context) {
        if (PiaPrefHandler.isNetworkManagementEnabled(context)) {
            AutomationService.start(context)
        } else {
            AutomationService.stop(context)
        }
    }
    // endregion
}