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

package com.privateinternetaccess.android.pia.model.events;

import de.blinkt.openvpn.core.ConnectionStatus;

/**
 * Created by half47 on 10/11/16.
 */

public class VpnStateEvent {

    String state;
    String logmessage;
    int localizedResId;
    public ConnectionStatus level;

    public VpnStateEvent(String state, String logmessage, int localizedResId, ConnectionStatus level) {
        this.state = state;
        this.logmessage = logmessage;
        this.localizedResId = localizedResId;
        this.level = level;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLogmessage() {
        return logmessage;
    }

    public void setLogmessage(String logmessage) {
        this.logmessage = logmessage;
    }

    public int getLocalizedResId() {
        return localizedResId;
    }

    public void setLocalizedResId(int localizedResId) {
        this.localizedResId = localizedResId;
    }

    public ConnectionStatus getLevel() {
        return level;
    }

    public void setLevel(ConnectionStatus level) {
        this.level = level;
    }
}
