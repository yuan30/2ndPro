package com.example.negativeion.model;

import com.google.gson.annotations.SerializedName;

public class Temperature2Model {

    @SerializedName("temperature")
    private String temperatureValue;

    @SerializedName("time")
    private String timeValue;
    @SerializedName("TId")
    private String tId;

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

    public String getTId() {
        return tId;
    }

    public void setTId(String tId) {
        this.tId = tId;
    }
}
