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

import com.privateinternetaccess.android.pia.model.exceptions.HttpResponseError;
import com.privateinternetaccess.android.pia.model.response.ReportResponse;
import com.privateinternetaccess.android.pia.utils.DLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Sends in the debug logs to our systems for our support to view.
 *
 * Created by hfrede on 6/13/17.
 */

public class ReportingApi extends PiaApi {

    private Context context;

    public ReportingApi(Context context) {
        super();
        this.context = context;
    }

    /**
     * Creates a alpha numerical number, send it to the server and returns it if the server responded.
     *
     * @param reports
     * @return {@link ReportResponse}
     * @throws IOException
     */
    public ReportResponse sendReport(String reports[]) throws IOException {
        Random random = new Random();
        DLog.d("ReportingAPI", Arrays.toString(reports));

        StringBuilder sep = new StringBuilder();
        String alphabet = "0123456789ABCDEF";

        for (int i = 0; i < 50; i++) {
            sep.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 5; i++){
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        sep.append("\n");

        // worst case assumpiton
        int outlen = 1000;
        for (String r : reports)
            outlen += r.length() + sep.length();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(outlen);
        outputStream.write(sep.toString().getBytes());

        compress(outputStream, String.format(Locale.US, "debug_id\n%s\n", sb.toString()).getBytes());

        for (String report : reports) {
            outputStream.write(sep.toString().getBytes());
            compress(outputStream, report.getBytes());
        }

        String postdata = "bigdata=" + URLEncoder.encode(outputStream.toString("ISO-8859-1"), "ISO-8859-1");

        Request request = new Request.Builder().url(getBaseURL(context) + "vpninfo/debug_log")
                .header("User-Agent", PiaApi.ANDROID_HTTP_CLIENT)
                .post(MultipartBody.create(MediaType.parse("application/x-www-form-urlencoded"), postdata))
                .build();

        Response res = getOkHttpClient().newCall(request).execute();

        String respMessage = res.message();
        int status = res.code();

        DLog.d("ReportingAPI", "id = " + sb.toString() + " status = " + status + " body = " + respMessage);

        if (status != 200)
            throw new HttpResponseError(status, respMessage);
        return new ReportResponse(sb.toString());
    }
}
