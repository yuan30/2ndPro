package com.example.negativeion.model;

import com.google.gson.annotations.SerializedName;

public class UserAndDeviceModel {
    @SerializedName("DeviceId")
    private String deviceId;

    @SerializedName("DeviceName")
    private String deviceName;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
