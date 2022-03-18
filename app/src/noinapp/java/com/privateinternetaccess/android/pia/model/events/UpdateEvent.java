package com.privateinternetaccess.android.pia.model.events;

public class UpdateEvent {

    int versionCode;

    public UpdateEvent(int version) {
        this.versionCode = version;
    }

    public int getVersion() {
        return versionCode;
    }
}
