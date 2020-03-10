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

import com.privateinternetaccess.android.pia.handlers.PiaPrefHandler;
import com.privateinternetaccess.android.pia.model.exceptions.PortForwardingError;
import com.privateinternetaccess.android.pia.model.response.PortForwardResponse;
import com.privateinternetaccess.android.pia.utils.DLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Calls the server to request and acquire portforwarding and the port used.
 *
 * Created by hfrede on 8/18/17.
 */

public class PortForwardApi extends PiaApi {


    public PortForwardApi() {
        super();
    }

    /**
     * Grabs the port the server decides to give us.
     *
     * Must be used when connected to the VPN.
     *
     * @param c
     * @return {@link PortForwardResponse}
     * @throws IOException
     * @throws PortForwardingError
     */
    public PortForwardResponse getPort(Context c) throws IOException, PortForwardingError
    {
        int port = -1;
        String clientId = "";
        try {
            clientId = PiaPrefHandler.getClientUniqueId(c);
        } catch (Exception e) {
            clientId = "testing";
        }
        String portforwarduri = "http://209.222.18.222:2000/?client_id=" + clientId;
        DLog.i("Portforwarding", "START");
        try {
            Request request = new Request.Builder()
                    .url(portforwarduri)
                    .addHeader("User-Agent", PiaApi.ANDROID_HTTP_CLIENT)
                    .build();

            Response response = getOkHttpClient().newCall(request).execute();

            String body = response.body().string();
            DLog.d("PortForward", "request done " + response.code() + " response = " + body);

            if (!(response.code()== 200))
                throw new PortForwardingError("Http Status: " + response.message());

            JSONObject res;
            res = new JSONObject(body);
            if(res.has("port"))
                port = res.optInt("port");
            else if(res.has("error")){
                throw new PortForwardingError(res.optString("error"));
            }
            DLog.d("PortForward", "reader done " + port);
        } catch ( IllegalStateException e) {
            e.printStackTrace();
            DLog.e("Portforward", "illegalstate");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DLog.d("Portforward", "done = " + port);
        if (port == -1)
            throw new PortForwardingError("No port number returned by API.");

        return new PortForwardResponse(port);
    }

}
