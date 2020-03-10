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

package com.privateinternetaccess.android.pia.handlers;

import android.content.Context;

import com.privateinternetaccess.android.PIAKillSwitchStatus;
import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.interfaces.IVPN;
import com.privateinternetaccess.android.pia.model.events.VpnStateEvent;
import com.privateinternetaccess.android.pia.utils.DLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Vector;

import de.blinkt.openvpn.core.ConnectionStatus;

public class RestartHandler {

    private IVPN vpn;
    private Vector<String> changes;
    private boolean restart;


    public RestartHandler() {
        EventBus.getDefault().register(this);
        changes = new Vector<>();
    }

    public void restartVPN(Context context){
        restart = true;
        vpn = PIAFactory.getInstance().getVPN(context);
        vpn.stop();
    }

    @Subscribe
    public void onStateReceived(VpnStateEvent event){
        if(event.getLevel() == ConnectionStatus.LEVEL_NOTCONNECTED){
            if(restart) {
                if(vpn.isKillswitchActive())
                    vpn.stopKillswitch();
                vpn.start();
            }
            restart = false;
            DLog.d("RestartHandler","restart = " + restart);
            vpn = null;
            changes.clear();
            EventBus.getDefault().unregister(this);
        }
    }

    public Vector<String> getChanges() {
        return changes;
    }

    public void setChanges(Vector<String> changes) {
        this.changes = changes;
    }
}
