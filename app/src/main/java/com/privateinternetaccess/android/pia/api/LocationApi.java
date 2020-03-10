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


import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.model.response.LocationResponse;
import com.privateinternetaccess.android.pia.utils.DLog;

import java.io.IOException;
import java.net.URL;

import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Grabs the user's IP location.
 *
 * Created by hfrede on 8/18/17.
 */

public class LocationApi extends PiaApi {

    public LocationApi() {
        super();
    }

    /**
     * Grabs the login information to send for you.
     *
     * Calls {@link LocationApi#getLocation(Context, String, String)}
     *
     * @param context
     * @return {@link LocationResponse}
     */
    public LocationResponse getLocation(Context context){
        String username = PiaPrefHandler.getLogin(context);
        String password = PiaPrefHandler.getSavedPassword(context);
        LocationResponse ip = getLocation(context, username, password);
        return ip;
    }

    /**
     * Base getLocation api call
     *
     * @param username
     * @param password
     * @return {@link LocationResponse}
     */
    public LocationResponse getLocation(Context context, String username, String password) {
        LocationResponse ip = new LocationResponse();
        try {
            URL url = getClientURL(context, "geo");

            Request request = new Request.Builder()
                    .header("User-Agent", PiaApi.ANDROID_HTTP_CLIENT)
                    .addHeader("Authorization", Credentials.basic(username, password))
                    .url(url).build();
            Response httpResponse = getOkHttpClient().newCall(request).execute();

            String body = httpResponse.body().string();
            DLog.d("LocationTask","body = " + body);

            if(!TextUtils.isEmpty(body)) {
                ip.parse(body);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ip;
    }
}
