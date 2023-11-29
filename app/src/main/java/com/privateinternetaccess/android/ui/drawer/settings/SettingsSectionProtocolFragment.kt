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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.privateinternetaccess.android.R
import com.privateinternetaccess.android.databinding.FragmentSettingsSectionProtocolBinding
import com.privateinternetaccess.android.model.states.VPNProtocol
import com.privateinternetaccess.android.pia.handlers.PIAServerHandler
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler
import com.privateinternetaccess.android.pia.utils.Toaster
import com.privateinternetaccess.android.pia.vpn.PiaOvpnConfig.OVPN_HANDSHAKE
import com.privateinternetaccess.android.ui.DialogFactory
import com.privateinternetaccess.android.ui.adapters.SettingsAdapter
import com.privateinternetaccess.android.wireguard.backend.GoBackend.WG_HANDSHAKE


class SettingsSectionProtocolFragment : Fragment() {

    private lateinit var binding: FragmentSettingsSectionProtocolBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsSectionProtocolBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            (it as SettingsActivity).setTitle(R.string.menu_settings_protocols)
            (it as SettingsFragmentsEvents).showOrbotDialogIfNeeded(it)
            applyPersistedStateToUi(it)
            prepareClickListeners(it)
        }
    }

    // region private
    private fun prepareClickListeners(context: Context) {
        binding.protocolSetting.setOnClickListener {
            updateProtocolSetting(context)
        }

        binding.transportSetting.setOnClickListener {
            updateTransportSetting(context)
        }

        binding.dataEncryptionSetting.setOnClickListener {
            updateDataEncryptionSetting(context)
        }

        binding.remotePortSetting.setOnClickListener {
            updateRemotePortSetting(context)
        }

        binding.localPortSetting.setOnClickListener {
            updateLocalPortSetting(context)
        }

        binding.smallPacketsSetting.setOnClickListener {
            updateSmallPacketsSetting(context)
        }
    }

    private fun updateProtocolSetting(context: Context) {
        showProtocolDialog(context)
    }

    private fun updateTransportSetting(context: Context) {
        showProtocolTransportDialog(context)
    }

    private fun updateDataEncryptionSetting(context: Context) {
        showDataEncryptionDialog(context)
    }

    private fun updateRemotePortSetting(context: Context) {
        showRemotePortDialog(context)
    }

    private fun updateLocalPortSetting(context: Context) {
        showLocalPortDialog(context)
    }

    private fun updateSmallPacketsSetting(context: Context) {
        val switch = binding.smallPacketsSwitchSetting
        when (VPNProtocol.Protocol.valueOf(PiaPrefHandler.getProtocol(context))) {
            VPNProtocol.Protocol.OpenVPN -> {
                PiaPrefHandler.setOvpnSmallPacketSizeEnabled(context, !switch.isChecked)
            }
            VPNProtocol.Protocol.WireGuard -> {
                PiaPrefHandler.setWireguardSmallPacketSizeEnabled(context, !switch.isChecked)
            }
        }
        (context as SettingsFragmentsEvents).showReconnectDialogIfNeeded(context)
        applyPersistedStateToUi(context)
    }

    private fun applyPersistedStateToUi(context: Context) {
        binding.protocolSummarySetting.text = PiaPrefHandler.getProtocol(context)
        binding.transportSummarySetting.text = PiaPrefHandler.getProtocolTransport(context)
        binding.dataEncryptionSummarySetting.text = PiaPrefHandler.getDataCipher(context)
        binding.remotePortSummarySetting.text = PiaPrefHandler.getRemotePort(context)
        binding.localPortSummarySetting.text = PiaPrefHandler.getLocalPort(context)
        when (VPNProtocol.Protocol.valueOf(PiaPrefHandler.getProtocol(context))) {
            VPNProtocol.Protocol.OpenVPN -> {
                binding.smallPacketsSwitchSetting.isChecked =
                    PiaPrefHandler.getOvpnSmallPacketSizeEnabled(context)
                binding.handshakeSummarySetting.text = OVPN_HANDSHAKE
                binding.dataEncryptionSetting.visibility = View.VISIBLE
                binding.remotePortSetting.visibility = View.VISIBLE
                binding.localPortSetting.visibility = View.VISIBLE
                binding.transportSetting.visibility = View.VISIBLE
            }
            VPNProtocol.Protocol.WireGuard -> {
                binding.smallPacketsSwitchSetting.isChecked =
                    PiaPrefHandler.getWireguardSmallPacketSizeEnabled(context)
                binding.handshakeSummarySetting.text = WG_HANDSHAKE
                binding.dataEncryptionSetting.visibility = View.GONE
                binding.remotePortSetting.visibility = View.GONE
                binding.localPortSetting.visibility = View.GONE
                binding.transportSetting.visibility = View.GONE
            }
        }
    }

    private fun showProtocolDialog(context: Context) {
        val protocols =
            VPNProtocol.Protocol.values().map { protocol -> protocol.name }.toTypedArray()
        val adapter = SettingsAdapter(context).apply {
            setOptions(protocols)
            setDisplayNames(protocols)
            selected = PiaPrefHandler.getProtocol(context)
        }
        val selectionCallback = { dialogInterface: DialogInterface, _: Int ->
            val targetProtocol = VPNProtocol.Protocol.valueOf(protocols[adapter.selectedIndex])
            (context as SettingsFragmentsEvents).showReconnectDialogIfNeeded(
                context,
                targetProtocol
            ) {
                applyPersistedStateToUi(context)
                dialogInterface.dismiss()
            }
        }

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.settings_protocol)
        builder.setSingleChoiceItems(
            adapter,
            protocols.indexOf(PiaPrefHandler.getProtocol(requireContext()))
        ) { _, which ->
            adapter.selected = protocols[which]
            adapter.notifyDataSetChanged()
        }
        builder.setPositiveButton(R.string.save, selectionCallback)
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    private fun showProtocolTransportDialog(context: Context) {
        val protocolTransportList = resources.getStringArray(R.array.protocol_transport)
        val adapter = SettingsAdapter(context).apply {
            setOptions(protocolTransportList)
            setDisplayNames(protocolTransportList)
            selected = PiaPrefHandler.getProtocolTransport(context)
        }

        val selectionCallback = { dialogInterface: DialogInterface, _: Int ->
            val target = protocolTransportList[adapter.selectedIndex]
            PiaPrefHandler.setProtocolTransport(
                context,
                target
            )
            PiaPrefHandler.resetRemotePort(context)
            (context as SettingsFragmentsEvents).showOrbotDialogIfNeeded(context)
            (context as SettingsFragmentsEvents).showReconnectDialogIfNeeded(context)
            applyPersistedStateToUi(context)
            dialogInterface.dismiss()
        }

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.transport)
        builder.setCancelable(true)
        builder.setSingleChoiceItems(
            adapter,
            protocolTransportList.indexOf(PiaPrefHandler.getProtocol(requireContext()))
        ) { _, which ->
            adapter.selected = protocolTransportList[which]
            adapter.notifyDataSetChanged()
        }
        builder.setPositiveButton(R.string.save, selectionCallback)
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    private fun showDataEncryptionDialog(context: Context) {
        val values = resources.getStringArray(R.array.ciphers_values)
        val adapter = SettingsAdapter(context).apply {
            setOptions(values)
            setDisplayNames(values)
            selected = PiaPrefHandler.getDataCipher(context)
        }

        val selectionCallback = { dialogInterface: DialogInterface, _: Int ->
            val selected = values[adapter.selectedIndex]
            PiaPrefHandler.setDataCipher(
                context,
                selected
            )
            (context as SettingsFragmentsEvents).showReconnectDialogIfNeeded(context)
            applyPersistedStateToUi(context)
            dialogInterface.dismiss()
        }

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.data_encyrption)
        builder.setCancelable(true)
        builder.setSingleChoiceItems(
            adapter,
            values.indexOf(PiaPrefHandler.getDataCipher(context))
        ) { _, which ->
            adapter.selected = values[which]
            adapter.notifyDataSetChanged()
        }
        builder.setPositiveButton(R.string.save, selectionCallback)
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    private fun showRemotePortDialog(context: Context) {
        val supportedProtocols = resources.getStringArray(R.array.protocol_transport)
        val options = when {
            PiaPrefHandler.getProtocolTransport(context) == supportedProtocols[0] ->
                PIAServerHandler.getInstance(context).info.udpPorts.map { port ->
                    port.toString()
                }.toMutableList()
            PiaPrefHandler.getProtocolTransport(context) == supportedProtocols[1] ->
                PIAServerHandler.getInstance(context).info.tcpPorts.map { port ->
                    port.toString()
                }.toMutableList()
            else ->
                throw IllegalArgumentException("Unsupported")
        }
        options.add(0, PiaPrefHandler.DEFAULT_AUTO_PORT)
        val adapter = SettingsAdapter(context).apply {
            setOptions(options.toTypedArray())
            setDisplayNames(options.toTypedArray())
            selected = PiaPrefHandler.getRemotePort(context)
        }

        val selectionCallback = { dialogInterface: DialogInterface, _: Int ->
            val selected = options[adapter.selectedIndex]
            PiaPrefHandler.setRemotePort(context, selected)
            (context as SettingsFragmentsEvents).showReconnectDialogIfNeeded(context)
            applyPersistedStateToUi(context)
            dialogInterface.dismiss()
        }

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.remote_port)
        builder.setCancelable(true)
        builder.setSingleChoiceItems(
            adapter,
            options.indexOf(PiaPrefHandler.getRemotePort(context))
        ) { _, which ->
            adapter.selected = options[which]
            adapter.notifyDataSetChanged()
        }
        builder.setPositiveButton(R.string.save, selectionCallback)
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    private fun showLocalPortDialog(context: Context) {
        val factory = DialogFactory(context)
        val dialog = factory.buildDialog()
        factory.addTextBox()
        factory.setHeader(getString(R.string.local_port_titla))
        factory.setEditHint(getString(R.string.settings_lport_hint))

        if (PiaPrefHandler.getLocalPort(context) != PiaPrefHandler.DEFAULT_AUTO_PORT) {
            factory.editText = PiaPrefHandler.getLocalPort(context)
        }

        factory.setPositiveButton(getString(R.string.save)) { _ ->
            factory.editText?.let { localPort ->
                try {
                    val range = PiaPrefHandler.LPORT_MIN_RANGE..PiaPrefHandler.LPORT_MAX_RANGE
                    if (localPort.toInt() in range) {
                        PiaPrefHandler.setLocalPort(context, localPort)
                        (context as SettingsFragmentsEvents).showReconnectDialogIfNeeded(context)
                        applyPersistedStateToUi(context)
                        dialog.dismiss()
                    } else {
                        Toaster.s(context, getString(R.string.settings_port_number_restriction))
                    }
                } catch (e: Exception) {
                    factory.editText = ""
                    Toaster.s(context, getString(R.string.settings_port_number_restriction))
                }
            }
        }

        factory.setNeutralButton(getString(R.string.default_base)) {
            PiaPrefHandler.resetLocalPort(context)
            (context as SettingsFragmentsEvents).showReconnectDialogIfNeeded(context)
            applyPersistedStateToUi(context)
            dialog.dismiss()
        }

        factory.setNegativeButton(getString(R.string.cancel)) {
            dialog.dismiss()
        }
        dialog.show()
    }
    // endregion
}