/*
 *  Copyright (c) 2020 Private Internet Access, Inc.
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

package com.privateinternetaccess.android.pia.interfaces;

import android.content.Context;

import com.privateinternetaccess.android.pia.PIAFactory;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConnectionStatus;

/**
 * This is how you interact with the VPN service
 *
 * Grab instance of this by {@link PIAFactory#getVPN(Context)}
 *
 * Created by hfrede on 9/6/17.
 */

public interface IVPN {

    /**
     * Starts the VPN with a process of clearing and updating elements related. Use this to handle all of the needed methods.
     *
     * Process:
     * {@link com.privateinternetaccess.android.pia.tasks.FetchIPTask#resetValues(Context)}.
     * Updates {@link de.blinkt.openvpn.core.VpnStatus#updateStateString(String, String, int, ConnectionStatus)}.
     * Generates the VPN config using {@link com.privateinternetaccess.android.pia.vpn.PiaOvpnConfig#generateVpnProfile(Context)}.
     * calls {@link com.privateinternetaccess.android.ui.widgets.WidgetProvider#updateWidget(Context, boolean)} to attach listeners.
     *
     * Starts the VPN using {@link de.blinkt.openvpn.core.VPNLaunchHelper#startOpenVpn(VpnProfile, Context)} using the generated config profile in a thread.
     *
     */
    void start();

    /**
     * Starts the VPN with a process of clearing and updating elements related. Use this to handle if a user has explicitly hit
     * the ConnectionSlider. Any other source should be false.
     *
     * Process:
     * {@link com.privateinternetaccess.android.pia.tasks.FetchIPTask#resetValues(Context)}.
     * Updates {@link de.blinkt.openvpn.core.VpnStatus#updateStateString(String, String, int, ConnectionStatus)}.
     * Generates the VPN config using {@link com.privateinternetaccess.android.pia.vpn.PiaOvpnConfig#generateVpnProfile(Context)}.
     * calls {@link com.privateinternetaccess.android.ui.widgets.WidgetProvider#updateWidget(Context, boolean)} to attach listeners.
     *
     * Starts the VPN using {@link de.blinkt.openvpn.core.VPNLaunchHelper#startOpenVpn(VpnProfile, Context)} using the generated config profile in a thread.
     *
     */
    void start(boolean connectPressed);

    /**
     * If the Vpn is active,
     *
     * Stops the VPN by connecting with {@link de.blinkt.openvpn.core.OpenVPNService} and calling {@link de.blinkt.openvpn.core.IOpenVPNServiceInternal#stopVPN(boolean)}
     */
    void stop();

    /**
     * If the Vpn is active,
     *
     * Stops the VPN. Explicitly specifies whether a user has interacted with ConnectionSlider.
     */
    void stop(boolean disconnectPressed);

    /**
     * It request the stop of the OpenVPN protocol.
     */
    void stopOpenVPN();

    /**
     * It request the stop of the Wireguard protocol.
     *
     * @param disconnectPressed `boolean`. Explicitly specifies whether a user has interacted
     * with the ConnectionSlider.
     */
    void stopWireguard(boolean disconnectPressed);

    /**
     * Pauses the VPN if the VPN is connected.
     *
     * Uses {@link de.blinkt.openvpn.core.OpenVPNService} in an intent with action {@link de.blinkt.openvpn.core.OpenVPNService#PAUSE_VPN}
     *
     */
    void pause();

    /**
     * Resumes the VPN if in paused state.
     *
     * Uses {@link de.blinkt.openvpn.core.OpenVPNService} in an intent with action {@link de.blinkt.openvpn.core.OpenVPNService#RESUME_VPN}
     *
     */
    void resume();

    /**
     * is the killswitch currently active.
     *
     * @return
     */
    boolean isKillswitchActive();

    /**
     * Shut down killswitch if needed.
     *
     */
    void stopKillswitch();

    /**
     *
     * @return if the VPN is connected
     */
    boolean isVPNActive();

    /**
     *
     * @return if OpenVPN is connected
     */
    boolean isOpenVPNActive();

    /**
     *
     * @return if Wireguard is connected
     */
    boolean isWireguardActive();
}