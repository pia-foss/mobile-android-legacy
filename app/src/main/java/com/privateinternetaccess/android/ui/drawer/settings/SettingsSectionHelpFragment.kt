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
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.privateinternetaccess.android.BuildConfig
import com.privateinternetaccess.android.PIAApplication
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.databinding.FragmentSettingsSectionHelpBinding
import com.privateinternetaccess.android.pia.PIAFactory
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.kpi.KPIManager
import com.privateinternetaccess.android.pia.model.enums.RequestResponseStatus
import com.privateinternetaccess.android.ui.connection.CallingCardActivity
import com.privateinternetaccess.android.ui.drawer.KPIShareEventsDetailsActivity
import com.privateinternetaccess.android.ui.drawer.VpnLogActivity
import com.privateinternetaccess.android.ui.drawer.KPIShareEventsMoreFragment
import com.privateinternetaccess.android.ui.rating.Rating


class SettingsSectionHelpFragment : Fragment() {

    private lateinit var binding: FragmentSettingsSectionHelpBinding
    private val shareEventsMoreFragment = KPIShareEventsMoreFragment()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsSectionHelpBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            (context as SettingsActivity).setTitle(R.string.menu_settings_help)
            applyPersistedStateToUi(it)
            prepareClickListeners(it)
        }
    }

    // region private
    private fun prepareClickListeners(context: Context) {
        binding.versionSetting.setOnClickListener {
            updateVersionSetting(context)
        }

        binding.kpiShareConnectionsSetting.setOnClickListener {
            updateKpiShareConnectionsSetting(context)
        }

        binding.kpiViewSharedEventsSetting.setOnClickListener {
            showKpiSharedEventsActivity(context)
        }

        binding.kpiFindOutMoreSetting.setOnClickListener {
            showKpiFindOutMoreFragment()
        }

        binding.viewVpnDebugLogSetting.setOnClickListener {
            updateViewVpnDebugLogSetting(context)
        }

        binding.sendDebugLogInformationSetting.setOnClickListener {
            updateSendDebugLogInformationSetting(context)
        }

        binding.latestNewsSetting.setOnClickListener {
            updateLatestNewsSetting(context)
        }
    }

    private fun updateVersionSetting(context: Context) {
        if (BuildConfig.FLAVOR_store == "playstore" && !PIAApplication.isAndroidTV(context)) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(Rating.APP_URL)
            }
            startActivity(intent)
        }
    }

    private fun updateKpiShareConnectionsSetting(context: Context) {
        val switch = binding.kpiShareConnectionsSwitchSetting
        PiaPrefHandler.setKpiShareConnectionEventsEnabled(context, !switch.isChecked)
        if (PiaPrefHandler.isKpiShareConnectionEventsEnabled(context)) {
            KPIManager.sharedInstance.start()
        } else {
            KPIManager.sharedInstance.stop()
        }
        applyPersistedStateToUi(context)
    }

    private fun showKpiSharedEventsActivity(context: Context) {
        KPIShareEventsDetailsActivity.open(context)
        activity?.overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
    }

    private fun showKpiFindOutMoreFragment() {
        shareEventsMoreFragment.show(
            requireActivity().supportFragmentManager,
            shareEventsMoreFragment.tag
        )
    }

    private fun updateViewVpnDebugLogSetting(context: Context) {
        VpnLogActivity.open(context)
        activity?.overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
    }

    private fun updateSendDebugLogInformationSetting(context: Context) {
        sendDebugLogInformation(context)
    }

    private fun updateLatestNewsSetting(context: Context) {
        CallingCardActivity.open(context, false)
    }

    private fun applyPersistedStateToUi(context: Context) {
        val versionSummary = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        binding.versionSummarySetting.text = versionSummary

        val kpiShareConnEventsEnabled = PiaPrefHandler.isKpiShareConnectionEventsEnabled(context)
        binding.kpiShareConnectionsSwitchSetting.isChecked = kpiShareConnEventsEnabled
        binding.kpiViewSharedEventsSetting.visibility = if (kpiShareConnEventsEnabled) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun sendDebugLogInformation(context: Context) {
        binding.sendDebugLogInformationSetting.isEnabled = false
        binding.sendDebugLogInformationProgressBarSetting.visibility = View.VISIBLE
        PIAFactory.getInstance().getAccount(context).sendDebugReport { reportIdentifier, status ->
            binding.sendDebugLogInformationSetting.isEnabled = true
            binding.sendDebugLogInformationProgressBarSetting.visibility = View.GONE
            if (reportIdentifier == null && status != RequestResponseStatus.SUCCEEDED) {
                Toast.makeText(
                    context,
                    getString(R.string.failure_sending_log, status.toString()),
                    Toast.LENGTH_LONG
                ).show()
                return@sendDebugReport
            }

            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.log_send_done_title)
            builder.setMessage(getString(R.string.log_send_done_msg, reportIdentifier))
            builder.setPositiveButton(getString(android.R.string.ok), null)
            builder.create().show()
        }
    }
    // endregion
}