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

import java.util.Date;

/**
 * Created by hfrede on 9/7/17.
 */

public class PIAAccountData {

    public static final String PLAN_TRIAL = "trial";
    public static final String PLAN_YEARLY = "yearly";
    public static final String PLAN_MONTHLY = "monthly";

    protected String plan;
    protected long expiration_time;
    protected boolean expired;
    protected boolean renewable;
    protected String email;
    protected boolean showExpire;
    protected Exception exception;
    protected boolean active;

    /**
     * @return time left in ms
     */
    public long getTimeLeft() {
        Date expiry = new Date(expiration_time);
        Date now = new Date();
        return expiry.getTime() - now.getTime();
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public long getExpiration_time() {
        return expiration_time;
    }

    public void setExpiration_time(long expiration_time) {
        this.expiration_time = expiration_time;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isRenewable() {
        return renewable;
    }

    public void setRenewable(boolean renewable) {
        this.renewable = renewable;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isShowExpire() {
        return showExpire;
    }

    public void setShowExpire(boolean showExpire) {
        this.showExpire = showExpire;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
