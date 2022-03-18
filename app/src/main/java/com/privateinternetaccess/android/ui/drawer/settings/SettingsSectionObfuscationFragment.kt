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
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.databinding.FragmentSettingsSectionObfuscationBinding
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.ui.drawer.AllowedAppsActivity


class SettingsSectionObfuscationFragment : Fragment() {

    companion object {
        private const val PROXY_PORT_HORIZONTAL_PADDING = 40
        private const val PROXY_PORT_VERTICAL_PADDING = 20
        private const val PROXY_PORT_HORIZONTAL_MARGIN = 15
        private const val PROXY_PORT_TEXT_SIZE = 15f
    }

    private lateinit var binding: FragmentSettingsSectionObfuscationBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsSectionObfuscationBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            (it as SettingsActivity).setTitle(R.string.menu_settings_obfuscation)
            (it as SettingsFragmentsEvents).showOrbotDialogIfNeeded(it)
            validateSelectedProxyApp(it)
            applyPersistedStateToUi(it)
            prepareClickListeners(it)
        }
    }

    // region private
    private fun prepareClickListeners(context: Context) {
        binding.connectViaProxySetting.setOnClickListener {
            updateConnectViaProxySetting(context)
        }

        binding.selectedProxyAppSetting.setOnClickListener {
            updateSelectProxyAppSetting(context)
        }

        binding.selectedProxyPortSetting.setOnClickListener {
            updateSelectedProxyPortSetting(context)
        }
    }

    private fun updateConnectViaProxySetting(context: Context) {
        showProxyAppDialogIfNeeded(context)
    }

    private fun updateSelectProxyAppSetting(context: Context) {
        val intent = Intent(context, AllowedAppsActivity::class.java)
        intent.putExtra(AllowedAppsActivity.EXTRA_SELECT_APP, true)
        startActivity(intent)
    }

    private fun updateSelectedProxyPortSetting(context: Context) {
        showSelectProxyPort(context)
    }

    private fun showProxyAppDialogIfNeeded(context: Context) {
        val switch = binding.connectViaProxySwitchSetting

        // If we are disabling it. No need to continue.
        if (PiaPrefHandler.isConnectViaProxyEnabled(context)) {
            PiaPrefHandler.setConnectViaProxyEnabled(context, !switch.isChecked)
            applyPersistedStateToUi(context)
            return
        }

        val proxyApp = PiaPrefHandler.getProxyApp(context)
        if (proxyApp.isNullOrEmpty()) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.enable_proxy_dialog_title)
            builder.setMessage(R.string.enable_proxy_dialog_message)
            builder.setCancelable(false)
            builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
                PiaPrefHandler.setConnectViaProxyEnabled(context, !switch.isChecked)
                applyPersistedStateToUi(context)

                val intent = Intent(context, AllowedAppsActivity::class.java)
                intent.putExtra(AllowedAppsActivity.EXTRA_SELECT_APP, true)
                startActivity(intent)
                dialogInterface.dismiss()
            }
            builder.setNegativeButton(R.string.cancel, null)
            builder.show()
        } else {
            val excludedApps = PiaPrefHandler.getVpnExcludedApps(context).apply {
                add(proxyApp)
            }
            PiaPrefHandler.setVpnExcludedApps(context, excludedApps)
            PiaPrefHandler.setConnectViaProxyEnabled(context, !switch.isChecked)
            applyPersistedStateToUi(context)
        }
    }

    private fun showSelectProxyPort(context: Context) {
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setPadding(
                PROXY_PORT_HORIZONTAL_PADDING,
                PROXY_PORT_VERTICAL_PADDING,
                PROXY_PORT_HORIZONTAL_PADDING,
                PROXY_PORT_VERTICAL_PADDING
        )
        linearLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )

        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ).apply {
            setMargins(PROXY_PORT_HORIZONTAL_MARGIN, 0, PROXY_PORT_HORIZONTAL_MARGIN, 0)
        }
        val editText = EditText(context)
        editText.layoutParams = params
        editText.textSize = PROXY_PORT_TEXT_SIZE
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        editText.setText(PiaPrefHandler.getProxyPort(context))
        linearLayout.addView(editText)

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.preference_proxy_port)
        builder.setView(linearLayout)
        builder.setPositiveButton(R.string.save) { dialogInterface, _ ->
            PiaPrefHandler.setProxyPort(context, editText.text.toString())
            applyPersistedStateToUi(context)
            dialogInterface.dismiss()
        }
        builder.setNeutralButton(R.string.default_base) { dialogInterface, _ ->
            PiaPrefHandler.resetProxyPort(context)
            applyPersistedStateToUi(context)
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    private fun validateSelectedProxyApp(context: Context) {
        if (PiaPrefHandler.getProxyApp(context).isNullOrEmpty()) {
            PiaPrefHandler.resetConnectViaProxyEnabled(context)
            PiaPrefHandler.resetProxyApp(context)
            PiaPrefHandler.resetProxyPort(context)
        }
    }

    private fun applyPersistedStateToUi(context: Context) {
        binding.connectViaProxySwitchSetting.isChecked = PiaPrefHandler.isConnectViaProxyEnabled(context)
        binding.selectedProxyAppSummarySetting.text = PiaPrefHandler.getProxyApp(context)
        binding.selectedProxyPortSummarySetting.text = PiaPrefHandler.getProxyPort(context)
        val targetVisibility = if (PiaPrefHandler.isConnectViaProxyEnabled(context)) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.selectedProxyAppSetting.visibility = targetVisibility
        binding.selectedProxyPortSetting.visibility = targetVisibility
    }
    // endregion
}