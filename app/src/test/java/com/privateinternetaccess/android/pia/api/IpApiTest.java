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

import android.test.mock.MockContext;

import com.privateinternetaccess.android.pia.model.response.IPResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import okhttp3.ResponseBody;
import okhttp3.mock.MockInterceptor;
import okhttp3.mock.Rule;

import static okhttp3.mock.MediaTypes.MEDIATYPE_JSON;

@RunWith(MockitoJUnitRunner.class)
public class IpApiTest {

    IpApi api;

    @Mock
    MockContext context;

    MockInterceptor interceptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        interceptor = new MockInterceptor();
        PiaApi.setInterceptor(interceptor);

        api = new IpApi(context);
    }

    @Test
    public void getIPAddress_not200() {
        interceptor.addRule(new Rule.Builder()
                .get()
                .url("https://www.privateinternetaccess.com/api/client/status")
                .respond(401));

        IPResponse response = api.getIPAddress();
        Assert.assertTrue(response == null);
    }

    @Test
    public void getIPAddress_200_emptyJson() {
        interceptor.reset();
        interceptor.addRule(new Rule.Builder()
                .get()
                .url("https://www.privateinternetaccess.com/api/client/status")
                .respond(200)
                .body(ResponseBody.create(MEDIATYPE_JSON, "{}")));

        IPResponse response = api.getIPAddress();
        Assert.assertTrue(response.getPair() != null);
    }

    @Test
    public void getIPAddress_200_notEmptyJson() {
        interceptor.reset();
        interceptor.addRule(new Rule.Builder()
                .get()
                .url("https://www.privateinternetaccess.com/api/client/status")
                .respond(200)
                .body(ResponseBody.create(MEDIATYPE_JSON, "{\"connected\": true, \"ip\": \"0.0.0.0\"}")));

        IPResponse response = api.getIPAddress();
        Assert.assertTrue(response != null);
    }

    @Test
    public void getIPAddress_200_correctJson() {
        interceptor.reset();
        interceptor.addRule(new Rule.Builder()
                .get()
                .url("https://www.privateinternetaccess.com/api/client/status")
                .respond(200)
                .body(ResponseBody.create(MEDIATYPE_JSON, "{\"connected\": true, \"ip\": \"0.0.0.0\"}")));

        IPResponse response = api.getIPAddress();
        Assert.assertTrue(response.getPair().first);
        Assert.assertTrue(response.getPair().second.equals("0.0.0.0"));
    }

    @Test
    public void getIPAddress_200_correctJsonTwo() {
        interceptor.reset();
        interceptor.addRule(new Rule.Builder()
                .get()
                .url("https://www.privateinternetaccess.com/api/client/status")
                .respond(200)
                .body(ResponseBody.create(MEDIATYPE_JSON, "{\"connected\": false, \"ip\": \"0.0.0.1\"}")));

        IPResponse response = api.getIPAddress();
        Assert.assertFalse(response.getPair().first);
        Assert.assertTrue(response.getPair().second.equals("0.0.0.1"));
    }

    @After
    public void tearDown() throws Exception {
        PiaApi.setInterceptor(null);
    }
}
