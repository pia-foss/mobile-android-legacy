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


import com.privateinternetaccess.android.pia.tasks.RetryPurchasingTask;

/**
 * Created by half47 on 2/5/16.
 */
public class PurchasingResponse {
    String username = null;
    String password = null;
    Exception exception;
    String response;
    int responseNumber;
    private int attempt;

    @Override
    public String toString() {
        return "PurchasingResponse{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", exception=" + exception +
                ", response='" + response + '\'' +
                ", responseNumber=" + responseNumber +
                ", attempt=" + attempt +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getResponseNumber() {
        return responseNumber;
    }

    public void setResponseNumber(int responseNumber) {
        this.responseNumber = responseNumber;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public boolean wasLastAttempt() {
        return attempt == RetryPurchasingTask.MAX_ATTEMPTS-1;
    }

    public int getAttempt() {
        return attempt;
    }
}
