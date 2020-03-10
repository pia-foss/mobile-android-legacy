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

package com.privateinternetaccess.android.pia.model.events;

import com.privateinternetaccess.android.pia.model.SkuDetailsObj;

import java.util.Map;

/**
 * Created by hfrede on 12/6/17.
 */

public class PurchasingInfoEvent {

    Map<String, SkuDetailsObj> skuDetailsList;

    public PurchasingInfoEvent(Map<String, SkuDetailsObj> skuDetailsList) {
        this.skuDetailsList = skuDetailsList;
    }

    public Map<String, SkuDetailsObj> getSkuDetailsList() {
        return skuDetailsList;
    }
}
