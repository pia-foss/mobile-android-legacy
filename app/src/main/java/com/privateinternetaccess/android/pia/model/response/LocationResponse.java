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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hfrede on 8/18/17.
 */

public class LocationResponse {

    private String IP;
    private String country;
    private String city;
    private double lat;
    private double lon;
    private String region;
    private String body;

    public void parse(String body){
        JSONObject json;
        try {
            json = new JSONObject(body);
            // {"ip":"216.155.129.59","country":"US","city":"Matawan","region":"New Jersey","lat":40.46520000000001,"lng":-74.2307}
            setIP(json.optString("ip"));
            setLat(json.optDouble("lat"));
            setLon(json.optDouble("long"));
            setCity(json.optString("city"));
            setCountry(json.optString("country"));
            setRegion(json.optString("region"));
            setBody(body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
