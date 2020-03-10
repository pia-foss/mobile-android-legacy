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

import com.privateinternetaccess.android.pia.model.PIAAccountData;
import com.privateinternetaccess.android.pia.model.enums.LoginResponseStatus;

import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

/**
 * lrStatus shows you how you should handle this response.
 *
 * showExpire is to be used so the backend can decide when to show the expiration area.
 *
 * Created by half47 on 11/14/16.
 */

public class LoginResponse extends PIAAccountData {

    private LoginResponseStatus lrStatus;

    public LoginResponse() {
        active = true;
    }

    public void parse(JSONObject object){
        try {
            setActive(object.optBoolean("active"));
            setPlan(object.optString("plan"));
            setExpiration_time(object.optLong("expiration_time") * 1000);
            setEmail(object.optString("email"));
            setExpired(object.optBoolean("expired"));
            setShowExpire(object.optBoolean("expire_alert"));
            setRenewable(object.optBoolean("renewable"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "Type: %s, Expiry: %s, expired: %s, showExpire: %s, active: %s, renewable: %s", plan,
                new Date(expiration_time * 1000).toString(), expired, showExpire + "", active + "", renewable + "");
    }

    public LoginResponseStatus getStatus() {
        return lrStatus;
    }

    public void setLrStatus(LoginResponseStatus lrStatus) {
        this.lrStatus = lrStatus;
    }

}