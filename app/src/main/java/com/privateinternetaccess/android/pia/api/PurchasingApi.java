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
import android.util.JsonWriter;

import com.privateinternetaccess.android.pia.model.PurchasingTestingData;
import com.privateinternetaccess.android.pia.model.exceptions.HttpResponseError;
import com.privateinternetaccess.android.pia.model.response.PurchasingResponse;
import com.privateinternetaccess.android.pia.model.response.SubscriptionAvailableResponse;
import com.privateinternetaccess.android.pia.utils.DLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Handles the purchasing and account creation from the server.
 *
 * Created by hfrede on 6/13/17.
 */

public class PurchasingApi extends PiaApi {

    private Context context;
    private static String TAG = "PurchasingAPI";

    public PurchasingApi(Context context) {
        super();
        this.context = context;
    }

    /**
     * Queries PIA for active Android subscription IDs
     *
     * @return
     */
    public SubscriptionAvailableResponse findSubscriptions() {
        SubscriptionAvailableResponse res = new SubscriptionAvailableResponse();
        String baseUrl = getBaseURL(context);

        try {
            Request request = new Request.Builder()
                    .url(baseUrl + "api/client/android")
                    .header("User-Agent", ANDROID_HTTP_CLIENT)
                    .build();
            Response response = getOkHttpClient().newCall(request).execute();

            if(response.isSuccessful()){
                String body = response.body().string();
                DLog.d(TAG, body);
                parseSubscriptionResponse(body, res);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    /**
     * Signs the user up with our service sending the IAP data with it.
     *
     * PurchasingResponse comes back with an exception. That is to tell what failed. responsenumber is the response code.
     *
     * @param email
     * @param order_id
     * @param token
     * @param branchInfo
     * @return {@link PurchasingResponse}
     * @throws IOException
     */
    public PurchasingResponse signUp(String email, String order_id, String token, String sku,
                                            JSONObject branchInfo, PurchasingTestingData testing, int timeout) throws IOException {
        PurchasingResponse response = new PurchasingResponse();
        if(!testing.isTesting()) {
            try {
                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.connectTimeout(timeout, TimeUnit.SECONDS);
                builder.readTimeout(timeout, TimeUnit.SECONDS);
                builder.writeTimeout(timeout, TimeUnit.SECONDS);
                if(INTERCEPTOR != null)
                    builder.addInterceptor(INTERCEPTOR);
                OkHttpClient client = builder.build();

                DLog.d("SignUp", "branchInfo = " + branchInfo);
                JSONObject json = createIAPJSON(order_id, token, sku, email, branchInfo, ANDROID_VERSION);
                RequestBody body = RequestBody.create(JSON, json.toString());

                Request request = new Request.Builder()
                        .header("User-Agent", PiaApi.ANDROID_HTTP_CLIENT)
                        .post(body)
                        .url(getBaseURL(context) + "api/client/signup").build();

                Response httpResponse = client.newCall(request).execute();
                String respBody = httpResponse.body().string();
                response.setResponse(respBody);
                int status = httpResponse.code();
                response.setResponseNumber(status);

                DLog.d("PIASignIn", "status = " + status + " url = " + getBaseURL(context) + "api/client/signup" + " json = " + json.toString());
                if (status == 200) {

                    DLog.i("PIA", "Loaded info from google play store. Parsing json..");
                    DLog.d("PIA", "JSON response: " + respBody);


                    JSONObject obj = new JSONObject(respBody);
                    response.setUsername(obj.optString("username"));
                    response.setPassword(obj.optString("password"));
                    if (response.getPassword().equals("null"))
                        response.setPassword(null);

                    DLog.d("PIA API", "signin = " + response.getPassword() + " " + response.getUsername() + " isValid = " + !"success".equals(status));
                    DLog.d("PIA API", "status = " + obj.optString("status"));

                } else {
                    response.setResponse(respBody);
                    response.setException(new HttpResponseError(status, respBody));
                }
            } catch (MalformedURLException ignored) {

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            response.setUsername(testing.getUsername());
            response.setPassword(testing.getPassword());
            response.setResponseNumber(testing.getResponseCode());
            if(!TextUtils.isEmpty(testing.getException())) {
                response.setException(new NullPointerException());
                response.setResponse(testing.getException());
            }
        }
        return response;
    }

    /**
     * Signs the user up with our service sending the IAP data with it.
     *
     * PurchasingResponse comes back with an exception. That is to tell what failed. responsenumber is the response code.
     *
     * @param email
     * @param order_id
     * @param token
     * @param product_id
     * @param branchInfo
     * @return {@link PurchasingResponse}
     * @throws IOException
     */
    public PurchasingResponse signUp(String email, String order_id, String token, String product_id,
                                            JSONObject branchInfo, PurchasingTestingData testing) throws IOException {
        PurchasingResponse response = new PurchasingResponse();
        if(!testing.isTesting()) {
            try {
                DLog.d("SignUp", "branchInfo = " + branchInfo);
                JSONObject json = createIAPJSON(order_id, token, product_id, email, branchInfo, ANDROID_VERSION);
                RequestBody body = RequestBody.create(JSON, json.toString());

                Request request = new Request.Builder()
                        .header("User-Agent", PiaApi.ANDROID_HTTP_CLIENT)
                        .post(body)
                        .url(getClientURL(context,"payment")).build();

                Response httpResponse = getOkHttpClient().newCall(request).execute();
                String respBody = httpResponse.body().string();
                response.setResponse(respBody);
                int status = httpResponse.code();
                response.setResponseNumber(status);

                DLog.d("PIASignIn", "status = " + status + " url = " + getBaseURL(context) + "api/client/signup" + " json = " + json.toString());
                if (status == 200) {

                    DLog.i("PIA", "Loaded info from google play store. Parsing json..");
                    DLog.d("PIA", "JSON response: " + respBody);


                    JSONObject obj = new JSONObject(respBody);
                    response.setUsername(obj.optString("username"));
                    response.setPassword(obj.optString("password"));
                    if (response.getPassword().equals("null"))
                        response.setPassword(null);

                    DLog.d("PIA API", "signin = " + response.getPassword() + " " + response.getUsername() + " isValid = " + !"success".equals(status));
                    DLog.d("PIA API", "status = " + obj.optString("status"));

                } else {
                    response.setException(new HttpResponseError(status, respBody));
                }
            } catch (MalformedURLException ignored) {

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            response.setUsername(testing.getUsername());
            response.setPassword(testing.getPassword());
            response.setResponseNumber(testing.getResponseCode());
            if(!TextUtils.isEmpty(testing.getException())) {
                response.setException(new NullPointerException());
                response.setResponse(testing.getException());
            }
        }
        return response;
    }

    /**
     *
     * This might not be required at all on Android since subscriptions are handled on Google's side.
     *
     * @param username
     * @param password
     * @param order_id
     * @param token
     * @param product_id
     * @return
     */
    public boolean renewSubscription(String username, String password, String order_id, String token, String product_id) {
        try {
            setAuthenticatorUP(username, password);

            JSONObject json = createIAPJSON(order_id, token, product_id, null, null, ANDROID_VERSION);
            RequestBody body = RequestBody.create(JSON, json.toString());

            Request request = new Request.Builder()
                    .header("User-Agent", PiaApi.ANDROID_HTTP_CLIENT)
                    .post(body)
                    .url(getClientURL(context, "payment")).build();

            Response response = getOkHttpClient().newCall(request).execute();

            String respMessage = response.message();
            int status = response.code();
            DLog.d("renewSubscription", "status = " + status + " respMessage = " + respMessage);

            cleanAuthenticator();
            if (status == 200)
                return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }
        return false;
    }

    /*
    POST /api/client/signup
    {
        store: 'google_play',
        receipt: {
          order_id: 'xxx',
          token: 'xxx',
          product_id: 'xxx',
          coupon: 'xxx'
         },
        email: 'xxx'
    }
  */

    /* Difference to

   /* RENEW API */
    /*
{
  store: 'google_play',
  receipt: {
    order_id: 'xxx',
    token: 'xxx',
    product_id: 'xxx',
    coupon: 'xxx'
  }
}

*/

    private static void writeIAPJson(String order_id, String token, String product_id, HttpURLConnection urlConnection, String email, String campaign) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
        writer.beginObject();
        writer.name("store");
        writer.value("google_play");

        writer.name("receipt");

        writer.beginObject();
        writer.name("order_id");
        writer.value(order_id);

        writer.name("token");
        writer.value(token);

        writer.name("product_id");
        writer.value(product_id);

        String coupon = getCoupon();
        if (coupon != null) {
            writer.name("coupon");
            writer.value(coupon);
        } else if (!TextUtils.isEmpty(campaign)) {
            writer.name("coupon");
            writer.value(campaign);
        }
        writer.endObject();
        if (email != null) {
            writer.name("email");
            writer.value(email);
        }
        writer.endObject();

        writer.close();
    }

    static private String getCoupon() {
        Properties configuration = new Properties();
        try {
            configuration.load(new FileInputStream("/system/build.prop"));
        } catch (IOException e) {
            return null;
        }
        return configuration.getProperty("pia.affiliate_code");
    }

    /**
     * Parses server response from subscription list endpoint. Return true if successful, false
     * if there are any errors parsing the response
     * @param body
     * @param response
     * @return
     */
    private static boolean parseSubscriptionResponse(String body, SubscriptionAvailableResponse response) {
        try {
            JSONObject obj = new JSONObject(body);
            DLog.d(TAG, body);

            if (!obj.getString("status").equals("success")) {
                return false;
            }

            JSONArray subs = obj.getJSONArray("available_products");

            for (int i = 0; i < subs.length(); i++) {
                JSONObject sub = subs.getJSONObject(i);

                if (!sub.getBoolean("legacy")) {
                    if (sub.getString("plan").equals(SubscriptionAvailableResponse.MONTHLY_KEY)) {
                        response.setActiveMonthlySubscription(sub.getString("id"));
                    }
                    else if (sub.getString("plan").equals(SubscriptionAvailableResponse.YEARLY_KEY)) {
                        response.setActiveYearlySubscription(sub.getString("id"));
                    }
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static JSONObject createIAPJSON(String order_id, String token, String product_id, String email, JSONObject branchInfo, String version) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.putOpt("store", "google_play");
        obj.putOpt("client_version", version);

        JSONObject receipt = new JSONObject();
        receipt.putOpt("order_id", order_id);
        receipt.putOpt("token", token);
        receipt.putOpt("product_id", product_id);
        obj.putOpt("receipt", receipt);

        obj.putOpt("email", email);


        JSONObject marketing = branchInfo;
        if (marketing == null)
            marketing = new JSONObject();

        obj.putOpt("marketing", marketing);

        DLog.d("createIAPJSON", obj.toString());

        return obj;
    }
}