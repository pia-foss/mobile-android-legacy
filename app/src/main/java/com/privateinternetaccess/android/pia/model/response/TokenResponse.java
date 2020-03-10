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

import com.privateinternetaccess.android.pia.model.enums.LoginResponseStatus;

import org.json.JSONObject;

public class TokenResponse {

    private String token;

    private LoginResponseStatus lrStatus;

    public TokenResponse() { }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public LoginResponseStatus getStatus() {
        return lrStatus;
    }

    public void setLrStatus(LoginResponseStatus lrStatus) {
        this.lrStatus = lrStatus;
    }

    public void parse(JSONObject json) {
        try {
            setToken(json.optString("token"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
