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
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.privateinternetaccess.android.PIAApplication
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.model.states.VPNProtocol
import com.privateinternetaccess.android.pia.PIAFactory
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.ui.connection.MainActivity
import com.privateinternetaccess.android.ui.drawer.AllowedAppsActivity
import com.privateinternetaccess.android.ui.superclasses.BaseActivity
import com.privateinternetaccess.android.utils.InAppMessageManager


interface SettingsFragmentsEvents {
    fun showReconnectDialogIfNeeded(
            context: Context,
            targetProtocol: VPNProtocol.Protocol? = null,
            callback: (() -> Unit)? = null
    )
    fun showOrbotDialogIfNeeded(context: Context)
    fun showFragment(fragment: Fragment)
}

class SettingsActivity : BaseActivity(), SettingsFragmentsEvents {

    companion object {
        private const val RECONNECT_DELAY_MS = 1000L

        public fun open(context: Context) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    private val settingsSectionsFragment = SettingsSectionsFragment()
    private var appBar: AppBarLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PIAApplication.isAndroidTV(this)) {
            setContentView(R.layout.activity_tv_secondary)
        } else {
            setContentView(R.layout.activity_secondary)
            initHeader(true, true)
            title = getString(R.string.menu_settings)
            setBackground()
            setSecondaryGreenBackground()
            appBar = findViewById(R.id.appbar)
        }

        supportFragmentManager.beginTransaction().replace(
                R.id.activity_secondary_container,
                settingsSectionsFragment
        ).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            onBackPressed()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        intent.extras?.getString(InAppMessageManager.EXTRA_KEY)?.let {
            handleKey(baseContext, it)
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            setResult(RESULT_OK)
            finish()
            overridePendingTransition(R.anim.right_to_left_exit, R.anim.left_to_right_exit)
        }
    }

    fun applyThemeChange() {
        setResult(MainActivity.THEME_CHANGED)
        finish()
        overridePendingTransition(R.anim.right_to_left_exit, R.anim.left_to_right_exit)
    }

    fun showHideActionBar(show: Boolean) {
        appBar?.setExpanded(show)
    }

    override fun showReconnectDialogIfNeeded(
            context: Context,
            targetProtocol: VPNProtocol.Protocol?,
            callback: (() -> Unit)?
    ) {
        // Eagerly check all protocols in order to avoid possible issues where the user
        // selected `Later` to a protocol change.
        if (!PIAFactory.getInstance().getVPN(context).isOpenVPNActive &&
                !PIAFactory.getInstance().getVPN(context).isWireguardActive
        ) {
            targetProtocol?.let { PiaPrefHandler.setProtocol(context, it) }
            callback?.let { it() }
            return
        }

        val selectionCallback = { dialogInterface: DialogInterface, reconnect: Boolean ->
            if (reconnect) {
                // Eagerly stop all protocols in order to avoid possible issues where the user
                // selected `Later` to a protocol change.
                PIAFactory.getInstance().getVPN(context).stopOpenVPN()
                PIAFactory.getInstance().getVPN(context).stopWireguard(false)
                targetProtocol?.let { PiaPrefHandler.setProtocol(context, it) }

                // Handle the connection on our own only if NMT is disabled. Otherwise, let it handle it.
                if (PiaPrefHandler.isAutomationDisabledBySettingOrFeatureFlag(context)) {

                    // Due to the lack of stopping callbacks. Introduce some delay to allow for
                    // the tearing down of the previous connection.
                    Handler(Looper.getMainLooper()).postDelayed({
                        PIAFactory.getInstance().getVPN(context).start(true)
                    }, RECONNECT_DELAY_MS)
                }
            } else {
                targetProtocol?.let { PiaPrefHandler.setProtocol(context, it) }
            }
            dialogInterface.dismiss()
            callback?.let { it() }
        }

        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle(R.string.menu_settings)
        builder.setMessage(R.string.reconnect_vpn)
        builder.setPositiveButton(R.string.reconnect) { dialogInterface, _ ->
            selectionCallback(dialogInterface, true)
        }
        builder.setNegativeButton(R.string.later) { dialogInterface, _ ->
            selectionCallback(dialogInterface, false)
        }
        builder.show()
    }

    override fun showOrbotDialogIfNeeded(context: Context) {
        val selectedApp = PiaPrefHandler.getProxyApp(context)
        if (selectedApp != AllowedAppsActivity.ORBOT) {
            return
        }

        val transport = PiaPrefHandler.getProtocolTransport(context)
        if (transport == context.resources.getStringArray(R.array.protocol_transport)[1]) {
            return
        }

        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle(R.string.settings_orbot_udp_problem_title)
        builder.setMessage(R.string.settings_orbot_udp_problem_message)
        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            PiaPrefHandler.setProtocolTransport(
                    context,
                    resources.getStringArray(R.array.protocol_transport)[1]
            )
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.dismiss, null)
        builder.show()
    }

    override fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().let { transaction ->
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

    // region private
    private fun handleKey(context: Context?, key: String) {
        context?.let {
            val targetProtocol = when (key) {
                InAppMessageManager.KEY_OVPN ->
                    VPNProtocol.Protocol.OpenVPN
                InAppMessageManager.KEY_WG ->
                    VPNProtocol.Protocol.WireGuard
                else ->
                    throw IllegalArgumentException("Unsupported")
            }
            showReconnectDialogIfNeeded(it, targetProtocol)
        }
    }
    // endregion
}