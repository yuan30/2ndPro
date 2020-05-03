package com.example.negativeion.model;

import com.google.gson.annotations.SerializedName;

public class TemperatureModel {

    @SerializedName("Temperature")
    private String temperatureValue = null;

    @SerializedName("Time")
    private String timeValue = null;

    public String getTemperatureValue() {
        return temperatureValue;
    }

    public void setTemperatureValue(String temperatureValue) {
        this.temperatureValue = temperatureValue;
    }

    public String getTimeValue() {
        return timeValue;
    }

    public void setTimeValue(String timeValue) {
        this.timeValue = timeValue;
    }
}
