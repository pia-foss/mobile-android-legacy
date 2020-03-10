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

package com.privateinternetaccess.android.pia.model;

import android.text.TextUtils;

/**
 * Created by hfrede on 8/31/17.
 */

public class PurchasingTestingData {

    private boolean testing;
    private int responseCode;
    private String username;
    private String password;
    private String exception;

    public PurchasingTestingData(boolean testing) {
        this.testing = testing;
    }

    public PurchasingTestingData(boolean testing, int responseCode, String username, String password, String exception) {
        this.testing = testing;
        this.responseCode = responseCode;
        this.username = username;
        this.password = password;
        this.exception = exception;
    }

    public boolean isTesting() {
        return testing;
    }

    /**
     * returns {@link #testing} and if not true, checks if testing emails match
     *
     * @param email
     * @return
     */
    public boolean isTesting(String email) {
        boolean ret = testing;
        if(!testing && !TextUtils.isEmpty(email))
            ret = email.equals("piamobileteamtest@gmail.com");
        return ret;
    }

    public int getResponseCode() {
        return responseCode;
    }

    /**
     * @return {@link #username} if there or p1234567 if empty
     */
    public String getUsername() {
        String ret = username;
        if(TextUtils.isEmpty(ret)) {
           ret = "p1234567";
        }
        return ret;
    }

    /**
     * @return {@link #password} if there or tPassword if empty
     */
    public String getPassword() {
        String ret = password;
        if(TextUtils.isEmpty(ret))
            ret = "tPassword";
        return ret;
    }

    public String getException() {
        return exception;
    }
}
