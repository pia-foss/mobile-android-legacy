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

/**
 * Created by hfrede on 1/5/18.
 */

public class PurchaseObj {

    private String orderId;
    private String getPackageName;
    private String productId;
    private long purchaseTime;
    private String purchaseToken;

    public PurchaseObj(String orderId, String getPackageName, String sku, long purchaseTime, String purchaseToken) {
        this.orderId = orderId;
        this.getPackageName = getPackageName;
        this.productId = sku;
        this.purchaseTime = purchaseTime;
        this.purchaseToken = purchaseToken;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getGetPackageName() {
        return getPackageName;
    }

    public void setGetPackageName(String getPackageName) {
        this.getPackageName = getPackageName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public long getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(long purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    public boolean isValid() {
        return orderId != null && purchaseToken != null;
    }
}
