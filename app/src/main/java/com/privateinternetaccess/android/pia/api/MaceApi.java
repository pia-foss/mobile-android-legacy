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

import com.privateinternetaccess.android.pia.model.response.MaceResponse;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Starts up Mace for the user when the VPN is connected.
 *
 * Created by hfrede on 8/18/17.
 */

public class MaceApi extends PiaApi {


    public MaceApi() {
        super();
    }

    /**
     * This calls the server and tells the server to use MACE on this connection. Must be called after any reconnection.
     *
     * Use the method while connected to the VPN
     *
     * @return {@link MaceResponse}
     */
    public MaceResponse hitMace(){
        String url = "http://209.222.18.222:1111";
        boolean hitUrl = false;
        try {
            Request request = new Request.Builder()
                    .header("User-Agent", ANDROID_HTTP_CLIENT)
                    .url(url).build();
            Response httpResponse = getOkHttpClient().newCall(request).execute(); // THIS IS GOING TO CREATE A ECONNREFUSED so expect it.
            int code = httpResponse.code();
            hitUrl = true;
        } catch (IOException e) {
        }
        return new MaceResponse(hitUrl);
    }

}
