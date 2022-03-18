package com.privateinternetaccess.android.pia.model.response;

public class CheckUpdateResponse {

    private int versionCode;
    private String versionName;

    public CheckUpdateResponse(int code) {
        versionCode = code;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
