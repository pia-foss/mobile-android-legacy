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
import android.content.DialogInterface
import android.net.InetAddresses
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.databinding.FragmentSettingsSectionNetworkBinding
import com.privateinternetaccess.android.model.states.VPNProtocol
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.ui.DialogFactory
import com.privateinternetaccess.android.ui.adapters.SettingsAdapter


class SettingsSectionNetworkFragment : Fragment() {

    companion object {
        private const val CUSTOM_DNS_HORIZONTAL_PADDING = 40
        private const val CUSTOM_DNS_VERTICAL_PADDING = 20
        private const val CUSTOM_DNS_HORIZONTAL_MARGIN = 15
    }

    private lateinit var binding: FragmentSettingsSectionNetworkBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsSectionNetworkBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            (it as SettingsActivity).setTitle(R.string.menu_settings_network)
            applyPersistedStateToUi(it)
            prepareClickListeners(it)
        }
    }

    // region private
    private fun prepareClickListeners(context: Context) {
        binding.dnsSetting.setOnClickListener {
            showDNSDialog(context)
        }
        binding.portForwardingSetting.setOnClickListener {
            updatePortForwardingSetting(context)
        }
        binding.aggressiveIpv6BlockingSetting.setOnClickListener {
            updateAggressiveIpv6BlockingSetting(context)
        }
        binding.allowLanTrafficSetting.setOnClickListener {
            updateAllowLanTrafficSetting(context)
        }
    }

    private fun updatePortForwardingSetting(context: Context) {
        val switch = binding.portForwardingSwitchSetting
        PiaPrefHandler.setPortForwardingEnabled(context, !switch.isChecked)
        (context as SettingsFragmentsEvents).showReconnectDialogIfNeeded(context)
        applyPersistedStateToUi(context)
    }

    private fun updateAggressiveIpv6BlockingSetting(context: Context) {
        val switch = binding.aggressiveIpv6BlockingSwitchSetting
        PiaPrefHandler.setBlockIpv6Enabled(context, !switch.isChecked)
        (context as SettingsFragmentsEvents).showReconnectDialogIfNeeded(context)
        applyPersistedStateToUi(context)
    }

    private fun updateAllowLanTrafficSetting(context: Context) {
        val switch = binding.allowLanTrafficSwitchSetting
        if (switch.isChecked) {
            PiaPrefHandler.setAllowLocalLanEnabled(context, !switch.isChecked)
            (context as SettingsFragmentsEvents).showReconnectDialogIfNeeded(context)
            applyPersistedStateToUi(context)
        } else {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.pref_block_dialog_title)
            builder.setMessage(R.string.pref_block_dialog_message)
            builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
                PiaPrefHandler.setAllowLocalLanEnabled(context, !switch.isChecked)
                (context as SettingsFragmentsEvents).showReconnectDialogIfNeeded(context)
                applyPersistedStateToUi(context)
                dialogInterface.dismiss()
            }
            builder.setNegativeButton(R.string.cancel, null)
            builder.show()
        }
    }

    private fun showDNSDialog(context: Context) {

        // Prepare the list of options.
        val piaDnsString = getString(R.string.pia_dns)
        val options = mutableListOf(
            piaDnsString,
        )
        val systemDnsResolverString = getString(R.string.system_resolver_dns)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            options.add(systemDnsResolverString)
        }
        val customDnsString = customDnsString(context)
        if (customDnsString.isNotEmpty()) {
            options.add(customDnsString)
        }

        val selectedOption = if (PiaPrefHandler.isCustomDnsSelected(context)) {
            customDnsString
        } else if (PiaPrefHandler.isSystemDnsResolverSelected(context)) {
            systemDnsResolverString
        } else {
            piaDnsString
        }

        // Build the dialog
        val adapter = SettingsAdapter(context).apply {
            setOptions(options.toTypedArray())
            setDisplayNames(options.toTypedArray())
            selected = selectedOption
        }

        val selectionCallback = { dialogInterface: DialogInterface, _: Int ->
            val index = adapter.selectedIndex
            PiaPrefHandler.setDnsChanged(context, true)
            when (index) {
                0 -> {
                    PiaPrefHandler.resetCustomDnsSelected(context)
                    PiaPrefHandler.resetSystemDnsResolverSelected(context)
                }
                1 -> {
                    showDnsWarningDialog(context) {
                        showDnsSystemResolverAllowLanWarningDialogIfNeeded(context) {
                            showMaceWarningDialogIfNeeded(context)
                            applySystemDnsResolverOption(context)
                        }
                    }
                }
                2 -> {
                    PiaPrefHandler.setCustomDnsSelected(context, true)
                    PiaPrefHandler.resetSystemDnsResolverSelected(context)
                    showMaceWarningDialogIfNeeded(context)
                }
                else ->
                    throw IllegalArgumentException("Unsupported")
            }

            (context as SettingsFragmentsEvents).showReconnectDialogIfNeeded(context)
            applyPersistedStateToUi(context)
            dialogInterface.dismiss()
        }

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.dns_pref_header)
        builder.setCancelable(false)
        builder.setSingleChoiceItems(
            adapter,
            adapter.selectedIndex
        ) { _, which ->
            adapter.selected = options[which]
            adapter.notifyDataSetChanged()
        }
        builder.setPositiveButton(R.string.save, selectionCallback)

        // If there is more than the default option
        if (options.size > 1) {
            builder.setNeutralButton(R.string.edit_custom_dns) { dialogInterface, _ ->
                showCustomDnsDialog(context)
                dialogInterface.dismiss()
            }
        } else {
            builder.setNeutralButton(R.string.custom_dns) { dialogInterface, _ ->
                showDnsWarningDialog(context) {
                    showCustomDnsDialog(context)
                }
                dialogInterface.dismiss()
            }
        }

        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    private fun showDnsWarningDialog(
        context: Context,
        acceptedCallback: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.custom_dns)
        builder.setCancelable(false)
        builder.setMessage(R.string.custom_dns_warning_body)
        builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
            acceptedCallback()
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialogInterface, _ ->
            showDNSDialog(context)
            dialogInterface.dismiss()
        }
        builder.show()
    }

    private fun showDnsSystemResolverAllowLanWarningDialogIfNeeded(
        context: Context,
        acceptedCallback: () -> Unit
    ) {
        if (PiaPrefHandler.isAllowLocalLanEnabled(context)) {
            acceptedCallback()
            return
        }

        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.system_resolver_dns))
        builder.setCancelable(false)
        builder.setMessage(getString(R.string.system_resolver_dns_body))
        builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
            acceptedCallback()
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(R.string.cancel)  { dialogInterface, _ ->
            showDNSDialog(context)
            dialogInterface.dismiss()
        }
        builder.show()
    }

    private fun showMaceWarningDialogIfNeeded(context: Context) {
        if (!PiaPrefHandler.isMaceEnabled(context)) {
            return
        }

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.custom_dns)
        builder.setCancelable(false)
        builder.setMessage(R.string.custom_dns_disabling_mace)
        builder.setNegativeButton(R.string.ok, null)
        builder.show()

        // Disable it right away. No need to wait for the dialog dismissal.
        PiaPrefHandler.setMaceEnabled(context, false)
    }

    private fun showCustomDnsDialog(context: Context) {
        val primaryTextLayout = TextInputLayout(context)
        val secondaryTextLayout = TextInputLayout(context)
        val primaryInputEditText = TextInputEditText(context)
        val secondaryInputEditText = TextInputEditText(context)

        val container = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )

        val textLayout = LinearLayout(context)
        textLayout.orientation = LinearLayout.VERTICAL
        textLayout.setPadding(
                CUSTOM_DNS_HORIZONTAL_PADDING,
                CUSTOM_DNS_VERTICAL_PADDING,
                CUSTOM_DNS_HORIZONTAL_PADDING,
                CUSTOM_DNS_VERTICAL_PADDING
        )
        textLayout.layoutParams = container

        val editTextParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )
        editTextParams.setMargins(
                CUSTOM_DNS_HORIZONTAL_MARGIN,
                0,
                CUSTOM_DNS_HORIZONTAL_MARGIN,
                0
        )
        primaryInputEditText.layoutParams = editTextParams
        secondaryInputEditText.layoutParams = editTextParams

        textLayout.addView(primaryTextLayout)
        textLayout.addView(secondaryTextLayout)

        if (PiaPrefHandler.isMaceEnabled(context)) {
            val maceWarningText = TextView(context)
            maceWarningText.setText(R.string.custom_dns_disabling_mace)
            maceWarningText.layoutParams = editTextParams
            textLayout.addView(maceWarningText)
        }

        val primaryDns = PiaPrefHandler.getPrimaryDns(context)
        val secondaryDNS = PiaPrefHandler.getSecondaryDns(context)

        primaryTextLayout.addView(primaryInputEditText)
        secondaryTextLayout.addView(secondaryInputEditText)

        primaryInputEditText.setHint(R.string.custom_primary_dns)
        secondaryInputEditText.setHint(R.string.custom_secondary_dns)

        val factory = DialogFactory(context)
        val dialog = factory.buildDialog()
        factory.setHeader(getString(R.string.custom_dns))
        factory.setBody(textLayout)

        if (!primaryDns.isNullOrEmpty()) {
            primaryInputEditText.setText(primaryDns)
        }

        if (!secondaryDNS.isNullOrEmpty()) {
            secondaryInputEditText.setText(secondaryDNS)
        }

        factory.setPositiveButton(getString(R.string.save)) { _ ->
            val typedCustomPrimaryDns = primaryInputEditText.text.toString()
            val typedCustomSecondaryDns = secondaryInputEditText.text.toString()

            val isValidCustomPrimaryDns = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                InetAddresses.isNumericAddress(typedCustomPrimaryDns)
            } else {
                Patterns.IP_ADDRESS.matcher(typedCustomPrimaryDns).matches()
            }
            val isValidCustomSecondaryDns = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                InetAddresses.isNumericAddress(typedCustomSecondaryDns)
            } else {
                Patterns.IP_ADDRESS.matcher(typedCustomSecondaryDns).matches()
            }

            if (isValidCustomPrimaryDns.not()) {
                primaryInputEditText.error = getString(R.string.custom_primary_dns_invalid)
                return@setPositiveButton
            }

            if (isValidCustomSecondaryDns.not()) {
                secondaryInputEditText.error = getString(R.string.custom_secondary_dns_invalid)
                return@setPositiveButton
            }

            PiaPrefHandler.setPrimaryDns(context, typedCustomPrimaryDns)
            PiaPrefHandler.setSecondaryDns(context, typedCustomSecondaryDns)
            PiaPrefHandler.setCustomDnsSelected(context, true)
            PiaPrefHandler.setMaceEnabled(context, false)
            applyPersistedStateToUi(context)
            dialog.dismiss()
        }
        if (!primaryDns.isNullOrEmpty()) {
            factory.setNeutralButton(getString(R.string.clear)) { _ ->
                PiaPrefHandler.resetPrimaryDns(context)
                PiaPrefHandler.resetSecondaryDns(context)
                PiaPrefHandler.resetCustomDnsSelected(context)
                applyPersistedStateToUi(context)
                dialog.dismiss()
            }
        }
        factory.setNegativeButton(getString(R.string.cancel)) {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun applySystemDnsResolverOption(context: Context) {
        PiaPrefHandler.setCustomDnsSelected(context, false)
        PiaPrefHandler.setAllowLocalLanEnabled(context, true)
        PiaPrefHandler.setSystemDnsResolverSelected(context, true)
        applyPersistedStateToUi(context)
    }

    private fun applyPersistedStateToUi(context: Context) {
        binding.dnsSummarySetting.text = if (PiaPrefHandler.isCustomDnsSelected(context)) {
            customDnsString(context)
        } else if (PiaPrefHandler.isSystemDnsResolverSelected(context)) {
            getString(R.string.system_resolver_dns)
        } else {
            getString(R.string.pia_dns)
        }
        binding.portForwardingSwitchSetting.isChecked = PiaPrefHandler.isPortForwardingEnabled(context)
        binding.aggressiveIpv6BlockingSwitchSetting.isChecked = PiaPrefHandler.isBlockIpv6Enabled(context)
        binding.allowLanTrafficSwitchSetting.isChecked = PiaPrefHandler.isAllowLocalLanEnabled(context)
        when (VPNProtocol.Protocol.valueOf(PiaPrefHandler.getProtocol(context))) {
            VPNProtocol.Protocol.OpenVPN ->
                binding.aggressiveIpv6BlockingSetting.visibility = View.VISIBLE
            VPNProtocol.Protocol.WireGuard ->
                binding.aggressiveIpv6BlockingSetting.visibility = View.GONE
        }
    }

    private fun customDnsString(context: Context): String {
        var result = ""
        val primaryDns = PiaPrefHandler.getPrimaryDns(context)
        if (!primaryDns.isNullOrEmpty()) {
            val secondaryDns = PiaPrefHandler.getSecondaryDns(context)
            result = if (secondaryDns.isNullOrEmpty()) {
                "${getString(R.string.custom_dns)} ($primaryDns)"
            } else {
                "${getString(R.string.custom_dns)} ($primaryDns / $secondaryDns)"
            }
        }
        return result
    }
    // endregion
}