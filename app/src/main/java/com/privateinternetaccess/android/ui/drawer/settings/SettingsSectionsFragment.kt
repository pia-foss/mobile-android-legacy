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
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.privateinternetaccess.android.PIAApplication
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.databinding.FragmentSettingsSectionsBinding
import com.privateinternetaccess.android.model.states.VPNProtocol
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler


class SettingsSectionsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsSectionsBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsSectionsBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            if (PIAApplication.isAndroidTV(it)) {
                binding.automationSettings.isGone = true
                binding.automationSummarySetting.isGone = true
            }
            (it as SettingsActivity).setTitle(R.string.menu_settings)
            applyPersistedStateToUi(it)
            prepareClickListeners(it)
        }
    }

    // region private
    private fun prepareClickListeners(context: Context) {
        binding.developerSettings.setOnClickListener {
            showDeveloperActivity(context)
        }

        binding.generalSettings.setOnClickListener {
            (context as SettingsActivity).showFragment(SettingsSectionGeneralFragment())
        }

        binding.protocolsSettings.setOnClickListener {
            (context as SettingsActivity).showFragment(SettingsSectionProtocolFragment())
        }

        binding.networkSettings.setOnClickListener {
            (context as SettingsActivity).showFragment(SettingsSectionNetworkFragment())
        }

        binding.privacySettings.setOnClickListener {
            (context as SettingsActivity).showFragment(SettingsSectionPrivacyFragment())
        }

        binding.automationSettings.setOnClickListener {
            (context as SettingsActivity).showFragment(SettingsSectionAutomationFragment())
        }

        binding.obfuscationSettings.setOnClickListener {
            (context as SettingsActivity).showFragment(SettingsSectionObfuscationFragment())
        }

        binding.helpSettings.setOnClickListener {
            (context as SettingsActivity).showFragment(SettingsSectionHelpFragment())
        }
    }

    private fun showDeveloperActivity(context: Context) {
        startActivity(Intent(context, DeveloperActivity::class.java))
        activity?.overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
    }

    private fun applyPersistedStateToUi(context: Context) {
        binding.protocolSummarySetting.text = PiaPrefHandler.getProtocol(context)
        binding.automationSummarySetting.text = if (PiaPrefHandler.isNetworkManagementEnabled(context)) {
            getString(R.string.enabled)
        } else {
            getString(R.string.disabled)
        }

        binding.developerSettings.visibility = if(PIAApplication.isRelease()){
            View.GONE
        } else {
            View.VISIBLE
        }

        if (PiaPrefHandler.isFeatureActive(context, PiaPrefHandler.DISABLE_NMT_FEATURE_FLAG)) {
            binding.automationSettings.visibility = View.GONE
        }

        when (VPNProtocol.Protocol.valueOf(PiaPrefHandler.getProtocol(context))) {
            VPNProtocol.Protocol.OpenVPN ->
                binding.obfuscationSettings.visibility = View.VISIBLE
            VPNProtocol.Protocol.WireGuard ->
                binding.obfuscationSettings.visibility = View.GONE
        }
    }
    // endregion
}