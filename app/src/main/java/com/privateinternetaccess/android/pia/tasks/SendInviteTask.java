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

package com.privateinternetaccess.android.pia.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.privateinternetaccess.android.pia.api.ReferralApi;
import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.model.events.SendInviteEvent;
import com.privateinternetaccess.android.pia.model.response.InviteResponse;

import org.greenrobot.eventbus.EventBus;

public class SendInviteTask extends AsyncTask<Void, Void, InviteResponse> {
    private Context context;
    private String inviteeEmail;
    private String inviteeName;

    public SendInviteTask(Context context, String email, String name) {
        this.context = context;
        this.inviteeEmail = email;
        this.inviteeName = name;
    }

    @Override
    protected InviteResponse doInBackground(Void... voids) {
        String token = PiaPrefHandler.getAuthToken(context);

        ReferralApi api = new ReferralApi(context);
        return api.sendInvite(token, inviteeEmail, inviteeName);
    }

    @Override
    protected void onPostExecute(InviteResponse inviteResponse) {
        super.onPostExecute(inviteResponse);
        EventBus.getDefault().postSticky(new SendInviteEvent(inviteResponse));
    }
}
