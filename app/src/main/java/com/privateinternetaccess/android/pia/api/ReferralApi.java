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

package com.privateinternetaccess.android.pia.api;

import android.content.Context;
import android.text.TextUtils;

import com.privateinternetaccess.android.pia.model.response.InviteResponse;
import com.privateinternetaccess.android.pia.model.response.InvitesResponse;
import com.privateinternetaccess.android.pia.utils.DLog;

import org.json.JSONObject;

import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReferralApi extends PiaApi {
    public static final String TAG = "ReferralAPI";

    private Context context;

    public ReferralApi(Context context) {
        super();
        this.context = context;
    }

    public InvitesResponse getInvites(String token) {
        try {
            InvitesResponse inviteResponse = new InvitesResponse();

            Request request = new Request.Builder().url(getClientURL(context,"invites")).
                    addHeader("Authorization", "Token " + token).
                    addHeader("client_version", ANDROID_HTTP_CLIENT).
                    build();

            Response response = getOkHttpClient().newCall(request).execute();
            int status = response.code();
            String res = response.body().string();
            DLog.d("ReferralApi", "body = " + res);

            if (status == 200) {
                if (!TextUtils.isEmpty(res)) {
                    inviteResponse.parse(new JSONObject(res));
                }
            }


            return inviteResponse;
        }
        catch (Exception e) {
            InvitesResponse invitesResponse = new InvitesResponse();
            invitesResponse.setException(e);

            return invitesResponse;
        }
    }

    public InviteResponse sendInvite(String token, String email, String name) {
        InviteResponse inviteResponse = new InviteResponse();

        try {
            JSONObject json = new JSONObject();
            json.put("invitee_email", email);
            json.put("invitee_name", name);
            json.put("terms_and_conditions", true);

            RequestBody body = RequestBody.create(JSON, json.toString());

            Request request = new Request.Builder()
                    .url(getClientURL(context,"invites"))
                    .post(body)
                    .addHeader("User-Agent", PiaApi.ANDROID_HTTP_CLIENT)
                    .addHeader("Authorization", "Token " + token)
                    .build();

            Response response = getOkHttpClient().newCall(request).execute();
            int status = response.code();
            String res = response.body().string();

            DLog.d("ReferralApi", "body = " + res);

            inviteResponse.setSuccessStatus(status == 200);
        }
        catch (Exception e) {
            e.printStackTrace();
            inviteResponse.setSuccessStatus(false);
        }

        return inviteResponse;
    }
}
