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

package com.privateinternetaccess.android.pia.vpn;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.handlers.PIAServerHandler;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.providers.VPNFallbackEndpointProvider;
import com.privateinternetaccess.android.pia.utils.DLog;
import com.privateinternetaccess.android.pia.utils.Prefs;
import com.privateinternetaccess.android.ui.drawer.settings.DeveloperActivity;
import com.privateinternetaccess.android.utils.DedicatedIpUtils;
import com.privateinternetaccess.core.model.PIAServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Locale;
import java.util.Vector;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.Connection;
import de.blinkt.openvpn.core.ProfileManager;

import static com.privateinternetaccess.android.pia.api.PiaApi.GEN4_MACE_ENABLED_DNS;


public class PiaOvpnConfig {

    public static final String DEFAULT_CIPHER = "AES-128-GCM";
    public static final String DEFAULT_AUTH = "SHA256";
    public static final String OVPN_HANDSHAKE = "rsa4096";

    @NonNull
    public static VpnProfile generateVpnProfile(Context context, VPNFallbackEndpointProvider.VPNEndpoint endpoint) throws IOException, ConfigParser.ConfigParseError {
        PIAServer region = PIAServerHandler.getInstance(context).getSelectedRegion(context, false);

        String user = "";
        String pw = "";
        String vpnToken = PIAFactory.getInstance().getAccount(context).vpnToken();
        if (vpnToken != null && !TextUtils.isEmpty(vpnToken)) {
            user = vpnToken.split(":")[0];
            pw = vpnToken.split(":")[1];;
        }

        if (region.isDedicatedIp()) {
            user = "dedicated_ip_" + region.getDipToken() + "_" + DedicatedIpUtils.randomAlphaNumeric(8);
            pw = region.getDedicatedIp();
        }

        ConfigParser cp = new ConfigParser();
        cp.parseConfig(new StringReader(PiaOvpnConfig.generateOpenVPNUserConfiguration(context, user, pw, endpoint)));
        final VpnProfile vp = cp.convertProfile();
        Prefs prefs = new Prefs(context);
        if (vp.checkProfile(context) != R.string.no_error_found)
            DLog.d("PIA", context.getString(vp.checkProfile(context)));

        vp.mName = region.getName();
        vp.mAllowedAppsVpnAreDisallowed = !prefs.getBoolean(PiaPrefHandler.VPN_PER_APP_ARE_ALLOWED);
        vp.mAllowedAppsVpn = new HashSet<>(PiaPrefHandler.getVpnExcludedApps(context));
        /* Always include PIA itself, so the current IP mechanism works */
        if (!vp.mAllowedAppsVpnAreDisallowed)
            vp.mAllowedAppsVpn.add(context.getPackageName());

        DLog.d("MainActivity", "apps disallowed = " + vp.mAllowedAppsVpnAreDisallowed);

        vp.mAllowLocalLAN = PiaPrefHandler.isAllowLocalLanEnabled(context);

        if (prefs.get("proxyisorbot", false))
            for (Connection conn : vp.mConnections)
                conn.mProxyType = Connection.ProxyType.ORBOT;

        vp.mUseCustomConfig = true;
        vp.mCustomConfigOptions += "\nauth-nocache\n";

        // Unlimited number of retries
        vp.mConnectRetryMax = "-1";

        vp.mDNS1 = "";
        vp.mDNS2 = "";

        String dns = PiaPrefHandler.getPrimaryDns(context);
        if (PiaPrefHandler.isMaceEnabled(context)) {
            dns = GEN4_MACE_ENABLED_DNS;
        }
        DLog.d("PiaOvpnConfig", "Custom DNS: " + dns);
        if(!TextUtils.isEmpty(dns)){
            vp.mDNS1 = dns;
            vp.mOverrideDNS = true;
        }

        String secondaryDns = PiaPrefHandler.getSecondaryDns(context);
        if (!TextUtils.isEmpty(secondaryDns)) {
            vp.mDNS2 = secondaryDns;
        }

        if (PiaPrefHandler.getOvpnSmallPacketSizeEnabled(context)) {
            vp.mMssFix = 1350;
        }

        ProfileManager.setTemporaryProfile(context, vp);

        DLog.d("PiaOvpnConfig", "Primary: " + vp.mDNS1);
        DLog.d("PiaOvpnConfig", "Secondary: " + vp.mDNS2);

        return vp;
    }

    public static String generateOpenVPNUserConfiguration(
            Context context,
            String user,
            String password,
            VPNFallbackEndpointProvider.VPNEndpoint endpoint
    ) throws IOException {
        Prefs prefs = new Prefs(context);

        InputStream conf = context.getAssets().open("vpn.conf");
        InputStreamReader isr = new InputStreamReader(conf);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder config = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null){
            config.append(line).append("\n");
        }

        PIAServerHandler handler = PIAServerHandler.getInstance(context);
        PIAServer ps = handler.getSelectedRegion(context, false);
        String autoserver = endpoint.getEndpoint();

        String[] transports = context.getResources().getStringArray(R.array.protocol_transport);
        boolean useTCP = PiaPrefHandler.getProtocolTransport(context).equals(transports[1]);

        if (ps.isDedicatedIp()) {
            autoserver = ps.getDedicatedIp();
        }

        if (useTCP) {
            config.append("proto tcp\n");
        } else {
            config.append("proto udp\n");
        }

        config.append(getInlineCa(context, OVPN_HANDSHAKE));
        config.append("cipher ").append(prefs.get(PiaPrefHandler.CIPHER, DEFAULT_CIPHER)).append("\n");
        config.append("auth ").append(prefs.get(PiaPrefHandler.AUTH, DEFAULT_AUTH)).append("\n");

        String remoteip = autoserver;
        String remoteport = "";
        if (autoserver.contains(":")) {
            remoteip = autoserver.split(":")[0];
        }

        if (useSignalSettings(endpoint)) {
            config.append("pia-signal-settings\n");
            config.append("ncp-disable\n");
        }

        String rport = PiaPrefHandler.getRemotePort(context);
        if (!rport.equals("") && !rport.equals("auto")) {
            remoteport = rport;
            config.append(String.format(Locale.ENGLISH, "remote %s %s\n", remoteip, remoteport));
        } else {
            Vector<Integer> ports = null;

            if(useTCP) {
                ports = handler.getInfo().getTcpPorts();
            } else {
                ports = handler.getInfo().getUdpPorts();
            }
            remoteport = ports.firstElement().toString();

            config.append(String.format(Locale.ENGLISH, "remote %s %s\n", remoteip, remoteport));

            for(Integer i : ports)
                if(!i.toString().equals(remoteport))
                    config.append(String.format(Locale.ENGLISH, "remote %s %s\n", remoteip, i));

            config.append("connect-timeout 5\n");
            config.append("connect-retry-max 1\n");
        }

        if (PiaPrefHandler.isConnectViaProxyEnabled(context))
            config.append(String.format(Locale.ENGLISH, "socks-proxy 127.0.0.1 %s\n", prefs.get("proxyport", "8080")));

        String lport = PiaPrefHandler.getLocalPort(context);
        if (lport.equals("") || lport.equals(PiaPrefHandler.DEFAULT_AUTO_PORT))
            config.append("nobind\n");
        else
            config.append(String.format("lport %s\nbind\n", lport));


        config.append(String.format("<auth-user-pass>\n%s\n%s\n</auth-user-pass>\n",
                user, password));

        if (PiaPrefHandler.getOvpnSmallPacketSizeEnabled(context))
            config.append("mssfix 1250\n");


        // IPv6 kill
        if (PiaPrefHandler.isBlockIpv6Enabled(context)) {
            if (!VpnProfile.mIsOpenVPN22) {
                config.append("ifconfig-ipv6 fd15:53b6:dead::2/64 fd15:53b6:dead::1\n");
                config.append("route-ipv6 ::/0 ::1\n");
            }
            config.append("block-ipv6\n");
        }

        String devConfigurations = prefs.getString(DeveloperActivity.PREF_DEVELOPER_CONFIGURATION);
        DLog.d("PIAOVPNConfig", "dev = " + devConfigurations);
        if (!TextUtils.isEmpty(devConfigurations)) {
            config.append("\n").append(devConfigurations).append("\n");
        }

        return config.toString();
    }

    public static String generateOpenVPNAppDefaultConfiguration(
            Context context,
            String user,
            String password,
            VPNFallbackEndpointProvider.VPNEndpoint endpoint
    ) throws IOException {
        Prefs prefs = new Prefs(context);

        InputStream conf = context.getAssets().open("vpn.conf");
        InputStreamReader isr = new InputStreamReader(conf);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder config = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null){
            config.append(line).append("\n");
        }

        config.append("proto udp\n");
        config.append(getInlineCa(context, OVPN_HANDSHAKE));
        config.append("cipher AES-128-GCM\n");
        config.append("auth SHA256\n");

        PIAServerHandler handler = PIAServerHandler.getInstance(context);
        String autoserver = endpoint.getEndpoint();
        String remoteip = autoserver;
        String remoteport = "";
        if (autoserver.contains(":")) {
            remoteip = autoserver.split(":")[0];
        }

        if (useSignalSettings(endpoint)) {
            config.append("pia-signal-settings\n");
            config.append("ncp-disable\n");
        }


        Vector<Integer> ports = handler.getInfo().getUdpPorts();
        remoteport = ports.firstElement().toString();
        config.append(String.format(Locale.ENGLISH, "remote %s %s\n", remoteip, remoteport));
        for(Integer i : ports)
            if(!i.toString().equals(remoteport))
                config.append(String.format(Locale.ENGLISH, "remote %s %s\n", remoteip, i));

        config.append("connect-timeout 5\n");
        config.append("connect-retry-max 1\n");

        if (PiaPrefHandler.isConnectViaProxyEnabled(context))
            config.append(String.format(Locale.ENGLISH, "socks-proxy 127.0.0.1 %s\n", prefs.get("proxyport", "8080")));

        config.append("nobind\n");
        config.append(String.format("<auth-user-pass>\n%s\n%s\n</auth-user-pass>\n",
                user, password));

        if (PiaPrefHandler.getOvpnSmallPacketSizeEnabled(context))
            config.append("mssfix 1250\n");


        // IPv6 kill
        if (PiaPrefHandler.isBlockIpv6Enabled(context)) {
            if (!VpnProfile.mIsOpenVPN22) {
                config.append("ifconfig-ipv6 fd15:53b6:dead::2/64 fd15:53b6:dead::1\n");
                config.append("route-ipv6 ::/0 ::1\n");
            }
            config.append("block-ipv6\n");
        }

        String devConfigurations = prefs.getString(DeveloperActivity.PREF_DEVELOPER_CONFIGURATION);
        if (!TextUtils.isEmpty(devConfigurations)) {
            config.append("\n").append(devConfigurations).append("\n");
        }

        return config.toString();
    }

    private static String getInlineCa(Context c, String s) throws IOException {
        InputStream conf = c.getAssets().open(s + ".pem");
        InputStreamReader isr = new InputStreamReader(conf);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder cafile = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            cafile.append(line).append("\n");
        }
        return String.format("<ca>\n%s\n</ca>\n", cafile.toString());
    }

    //Temporary function to test different servers
    private static boolean useSignalSettings(VPNFallbackEndpointProvider.VPNEndpoint endpoint) {
        if (endpoint.getUsesVanillaOpenVPN()) {
            return false;
        }

        return true;
    }
}
