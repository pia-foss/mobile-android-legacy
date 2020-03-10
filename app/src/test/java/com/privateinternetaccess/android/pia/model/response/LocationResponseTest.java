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

package com.privateinternetaccess.android.pia.model.response;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class LocationResponseTest {

    private String valid_JSON = "{\"lat\": 38.9072, \"long\": 77.0369, \"country\": \"USA\", \"region\": \"DC\", \"city\":\"Washington, DC\"}";

    @Test
    public void parse_empty() {
        LocationResponse response = new LocationResponse();
        response.parse("");
        Assert.assertTrue(response.getBody() == null);
    }

    @Test
    public void parse_emptyJSON() {
        LocationResponse response = new LocationResponse();
        response.parse("{}");
        Assert.assertTrue(response.getBody() != null);
    }

    @Test
    public void parse_validReturn_city() {
        LocationResponse response = new LocationResponse();
        response.parse(valid_JSON);
        Assert.assertTrue(response.getCity().equals("Washington, DC"));
    }

    @Test
    public void parse_validReturn_country() {
        LocationResponse response = new LocationResponse();
        response.parse(valid_JSON);
        Assert.assertTrue(response.getCountry().equals("USA"));
    }

    @Test
    public void parse_validReturn_region() {
        LocationResponse response = new LocationResponse();
        response.parse(valid_JSON);
        Assert.assertTrue(response.getRegion().equals("DC"));
    }

    @Test
    public void parse_validReturn_lat() {
        LocationResponse response = new LocationResponse();
        response.parse(valid_JSON);
        Assert.assertTrue(response.getLat() == 38.9072);
    }

    @Test
    public void parse_validReturn_long() {
        LocationResponse response = new LocationResponse();
        response.parse(valid_JSON);
        Assert.assertTrue(response.getLon() == 77.0369);
    }

    @Test
    public void parse_validReturn_BodyCheck() {
        LocationResponse response = new LocationResponse();
        response.parse(valid_JSON);
        Assert.assertTrue(response.getBody().equals(valid_JSON));
    }
}