package com.example.negativeion.model;

import com.google.gson.annotations.SerializedName;

public class HumidityModel {

    @SerializedName("Humidity")
    private String humidityValue = null;

    @SerializedName("Time")
    private String timeValue = null;

    public String getHumidityValue() {
        return humidityValue;
    }

    public void setHumidityValue(String humidityValue) {
        this.humidityValue = humidityValue;
    }

    public String getTimeValue() {
        return timeValue;
    }

    public void setTimeValue(String timeValue) {
        this.timeValue = timeValue;
    }
}
