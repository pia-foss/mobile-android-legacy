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

import com.privateinternetaccess.android.R;
import com.privateinternetaccess.android.pia.PIAFactory;
import com.privateinternetaccess.android.pia.interfaces.IVPN;
import com.privateinternetaccess.android.pia.model.events.ServerPingEvent;
import com.privateinternetaccess.android.ui.widgets.WidgetBaseProvider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.VpnStatus;

public class PingDelayStartHandler {


    private int timeDiff;
    private Context context;

    public PingDelayStartHandler() {
        EventBus.getDefault().register(this);
        timeDiff = PingHandler.PING_TIME_3_DIFFERENCE;
    }

    public void startPings(Context context) {
        this.context = context;
        boolean fired = PingHandler.getInstance(context).fetchPings(timeDiff);
        if(!fired){
            onPingEventReceive(null);
        } else {
            WidgetBaseProvider.updateWidget(context, true);
            VpnStatus.updateStateString("NOPROCESS", "Finding best server", R.string.status_server_ping, ConnectionStatus.LEVEL_NOTCONNECTED);
        }
    }

    @Subscribe
    public void onPingEventReceive(ServerPingEvent response){
        IVPN vpn = PIAFactory.getInstance().getVPN(context);
        vpn.start();
        context = null;
        EventBus.getDefault().unregister(this);
    }

    public void setTimeDiff(int timeDiff) {
        this.timeDiff = timeDiff;
    }
}
