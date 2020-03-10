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

import com.privateinternetaccess.android.pia.utils.DLog;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import de.blinkt.openvpn.core.OpenVPNService;

/**
 * Created by hfrede on 6/13/17.
 */

public class ProtectedSSLSocketFactory  extends SSLSocketFactory {
    private final SSLSocketFactory mSocketFactory;
    private final OpenVPNService mService;

    public ProtectedSSLSocketFactory(OpenVPNService openVPNService) {
        mService = openVPNService;
        mSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return mSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return mSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        // Do really really evil stuff ...
        Socket newSocket = new Socket();
        newSocket.setTcpNoDelay(true);
        protect(newSocket);
        newSocket.connect(s.getRemoteSocketAddress());
        return mSocketFactory.createSocket(newSocket, host, port, autoClose);
    }

    private void protect(Socket s) {
        if (mService != null)
            DLog.d("PIA", String.format("Protecting socket for IP query (%s)", mService.protect(s) ? "ok" : "failed"));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket s = mSocketFactory.createSocket(host, port);
        protect(s);
        return s;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        Socket s = mSocketFactory.createSocket(host, port, localHost, localPort);
        protect(s);
        return s;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket s = mSocketFactory.createSocket(host, port);
        protect(s);
        return s;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        Socket s = mSocketFactory.createSocket(address, port, localAddress, localPort);
        protect(s);
        return s;
    }
}