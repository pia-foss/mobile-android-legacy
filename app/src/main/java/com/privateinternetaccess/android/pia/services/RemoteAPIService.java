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

package com.privateinternetaccess.android.pia.services;
/*
 * Command to the service to display a message
 */

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import com.privateinternetaccess.android.tunnel.PIAVpnStatus;


/**
 * Handler of incoming messages from clients.
 */
public class RemoteAPIService extends Service {
    static final int MSG_GET_FWD_STATUS = 1;

    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_FWD_STATUS:
                    sendFWDBroadcast();
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendFWDBroadcast() {
        Intent intent = new Intent();
        intent.setAction("com.privateinternetaccess.com.PORTFORWARD_STATUS");
        intent.putExtra("status", PIAVpnStatus.getFwdStatus());
        intent.putExtra("argument", PIAVpnStatus.getFwdArgument());
        sendBroadcast(intent);
    }


    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}