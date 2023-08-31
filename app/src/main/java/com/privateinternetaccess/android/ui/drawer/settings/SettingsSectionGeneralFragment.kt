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

import android.app.Activity
import android.app.AlertDialog
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
import com.privateinternetaccess.android.databinding.FragmentSettingsSectionGeneralBinding
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.handlers.ThemeHandler
import com.privateinternetaccess.android.ui.LauncherActivity
import com.privateinternetaccess.android.ui.drawer.WidgetSettingsFragment


class SettingsSectionGeneralFragment : Fragment() {

    private lateinit var binding: FragmentSettingsSectionGeneralBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsSectionGeneralBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            if (PIAApplication.isAndroidTV(it)) {
                binding.widgetSetting.isGone = true
                binding.hapticFeedbackOnConnectSetting.isGone = true
            }
            (it as SettingsActivity).setTitle(R.string.menu_settings_general)
            applyPersistedStateToUi(it)
            prepareClickListeners(it)
        }
    }

    // region private
    private fun prepareClickListeners(context: Context) {
        binding.connectOnBootSetting.setOnClickListener {
            updateConnectOnBootSetting(context)
        }

        binding.connectOnAppStartSetting.setOnClickListener {
            updateConnectOnAppStartSetting(context)
        }

        binding.connectOnAppUpdateSetting.setOnClickListener {
            updateConnectOnAppUpdateSetting(context)
        }

        binding.darkThemeSetting.setOnClickListener {
            updateDarkThemeSetting(context)
        }

        binding.hapticFeedbackOnConnectSetting.setOnClickListener {
            updateHapticFeedbackOnConnectSetting(context)
        }

        binding.widgetSetting.setOnClickListener {
            updateWidgetSetting(context)
        }

        binding.showInAppMessagesSetting.setOnClickListener {
            updateShowInAppMessagesSetting(context)
        }

        binding.showGeoServersSetting.setOnClickListener {
            updateShowGeoServersSetting(context)
        }

        binding.resetSettings.setOnClickListener {
            showResetSettingsDialog(context)
        }
    }

    private fun updateConnectOnBootSetting(context: Context) {
        val switch = binding.connectOnBootSwitchSetting
        PiaPrefHandler.setAutoStart(context, !switch.isChecked)
        applyPersistedStateToUi(context)
    }

    private fun updateConnectOnAppStartSetting(context: Context) {
        val switch = binding.connectOnAppStartSwitchSetting
        PiaPrefHandler.setAutoConnect(context, !switch.isChecked)
        applyPersistedStateToUi(context)
    }

    private fun updateConnectOnAppUpdateSetting(context: Context) {
        val switch = binding.connectOnAppUpdateSwitchSetting
        PiaPrefHandler.setConnectOnAppUpdate(context, !switch.isChecked)
        applyPersistedStateToUi(context)
    }

    private fun updateDarkThemeSetting(context: Context) {
        val switch = binding.darkThemeSwitchSetting
        PiaPrefHandler.setDarkTheme(context, !switch.isChecked)
        applyPersistedStateToUi(context)
        toggleTheme(context)
    }

    private fun updateHapticFeedbackOnConnectSetting(context: Context) {
        val switch = binding.hapticFeedbackOnConnectSwitchSetting
        PiaPrefHandler.setHapticFeedbackEnabled(context, !switch.isChecked)
        applyPersistedStateToUi(context)
    }

    private fun updateWidgetSetting(context: Context) {
        showFragment(WidgetSettingsFragment())
    }

    private fun updateShowInAppMessagesSetting(context: Context) {
        val switch = binding.showInAppMessagesSwitchSetting
        PiaPrefHandler.setShowInAppMessagesEnabled(context, !switch.isChecked)
        applyPersistedStateToUi(context)
    }

    private fun updateShowGeoServersSetting(context: Context) {
        val switch = binding.showGeoServersSwitchSetting
        PiaPrefHandler.setGeoServersEnabled(context, !switch.isChecked)
        applyPersistedStateToUi(context)
    }

    private fun showResetSettingsDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.pref_reset_settings)
        builder.setMessage(R.string.pref_reset_settings_message)
        builder.setPositiveButton(R.string.save) { dialogInterface, _ ->
            PiaPrefHandler.resetSettings(context)
            applyPersistedStateToUi(context)
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    private fun applyPersistedStateToUi(context: Context) {
        binding.connectOnBootSwitchSetting.isChecked = PiaPrefHandler.doAutoStart(context)
        binding.connectOnAppStartSwitchSetting.isChecked = PiaPrefHandler.doAutoConnect(context)
        binding.connectOnAppUpdateSwitchSetting.isChecked = PiaPrefHandler.isConnectOnAppUpdate(context)
        binding.darkThemeSwitchSetting.isChecked = PiaPrefHandler.isDarkTheme(context)
        binding.hapticFeedbackOnConnectSwitchSetting.isChecked = PiaPrefHandler.isHapticFeedbackEnabled(context)
        binding.showInAppMessagesSwitchSetting.isChecked = PiaPrefHandler.isShowInAppMessagesEnabled(context)
        binding.showGeoServersSwitchSetting.isChecked = PiaPrefHandler.isGeoServersEnabled(context)

        if (PIAApplication.isAndroidTV(context)) {
            binding.darkThemeSetting.visibility = View.GONE
        }
    }

    private fun toggleTheme(context: Context) {
        if (PIAApplication.isAmazon()) {
            triggerRebirth(context)
        } else {
            ThemeHandler.setAppTheme((context as SettingsActivity).application)
            context.applyThemeChange()
        }
    }

    private fun triggerRebirth(context: Context) {
        val intent = Intent(context, LauncherActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        if (context is Activity) {
            context.finish()
        }
        Runtime.getRuntime().exit(0)
    }

    private fun showFragment(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()?.let { transaction ->
            transaction.setCustomAnimations(
                    R.anim.left_to_right,
                    R.anim.right_to_left,
                    R.anim.right_to_left_exit,
                    R.anim.left_to_right_exit
            )
            transaction.replace(R.id.activity_secondary_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }
    // endregion
}