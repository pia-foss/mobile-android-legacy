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

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.privateinternetaccess.android.BuildConfig
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.databinding.FragmentSettingsSectionPrivacyBinding
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler


class SettingsSectionPrivacyFragment : Fragment() {

    private lateinit var binding: FragmentSettingsSectionPrivacyBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsSectionPrivacyBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            (it as SettingsActivity).setTitle(R.string.menu_settings_privacy)
            applyPersistedStateToUi(it)
            prepareClickListeners(it)
        }
    }

    // region private
    private fun prepareClickListeners(context: Context) {
        binding.maceSetting.setOnClickListener {
            updateMaceSetting(context)
        }

        binding.blockConnectionWithoutVpnSetting.setOnClickListener {
            updateBlockConnectionWithoutVpnSetting(context)
        }
    }

    private fun updateMaceSetting(context: Context) {
        val switch = binding.maceSwitchSetting
        if (usingCustomDns(context) && !PiaPrefHandler.isMaceEnabled(context)) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.custom_dns)
            builder.setCancelable(false)
            builder.setMessage(R.string.custom_dns_mace_warning)
            builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
                PiaPrefHandler.resetCustomDnsSelected(context)
                PiaPrefHandler.resetPrimaryDns(context)
                PiaPrefHandler.resetSecondaryDns(context)
                PiaPrefHandler.setDnsChanged(context, true)
                PiaPrefHandler.setMaceEnabled(context, !switch.isChecked)
                applyPersistedStateToUi(context)
                dialogInterface.dismiss()
            }
            builder.setNegativeButton(R.string.cancel, null)
            builder.show()
        } else {
            PiaPrefHandler.setMaceEnabled(context, !switch.isChecked)
            applyPersistedStateToUi(context)
        }
    }

    private fun updateBlockConnectionWithoutVpnSetting(context: Context) {
        (context as SettingsActivity).showFragment(SettingsBlockConnectionsFragment())
    }

    private fun applyPersistedStateToUi(context: Context) {
        binding.maceSwitchSetting.isChecked = PiaPrefHandler.isMaceEnabled(context)
        binding.blockConnectionWithoutVpnSetting.visibility = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.maceSetting.visibility = if (BuildConfig.FLAVOR_store == "playstore") {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun usingCustomDns(context: Context): Boolean =
            !PiaPrefHandler.getPrimaryDns(context).isNullOrEmpty() ||
                    !PiaPrefHandler.getSecondaryDns(context).isNullOrEmpty()
    // endregion
}