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

package com.privateinternetaccess.android.model.events;

import java.util.Calendar;

public class VPNTrafficDataPointEvent {

    private long in;
    private long out;
    private long diffIn;
    private long diffOut;
    private long timeInMillis;

    public VPNTrafficDataPointEvent(long in, long out, long diffIn, long diffOut) {
        this.in = in;
        this.out = out;
        this.diffIn = diffIn;
        this.diffOut = diffOut;
        this.timeInMillis = Calendar.getInstance().getTimeInMillis();
    }

    public long getIn() {
        return in;
    }

    public void setIn(long in) {
        this.in = in;
    }

    public long getOut() {
        return out;
    }

    public void setOut(long out) {
        this.out = out;
    }

    public long getDiffIn() {
        return diffIn;
    }

    public void setDiffIn(long diffIn) {
        this.diffIn = diffIn;
    }

    public long getDiffOut() {
        return diffOut;
    }

    public void setDiffOut(long diffOut) {
        this.diffOut = diffOut;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }
}
