package com.example.negativeion;

import com.google.gson.annotations.SerializedName;

public class NegativeIonModel {

    @SerializedName("NegativeIon")
    private String negativeIonValue = null;

    @SerializedName("PM25")
    private String pm25Value = null;

    @SerializedName("Temperature")
    private String temperatureValue = null;

    @SerializedName("Humidity")
    private String humidityValue = null;

    @SerializedName("Time")
    private String timeValue = null;

    public String getNegativeIonValue() {
        return negativeIonValue;
    }

    public void setNegativeIonValue(String negativeIonValue) {
        this.negativeIonValue = negativeIonValue;
    }

    public String getPm25Value() {
        return pm25Value;
    }

    public void setPm25Value(String pm25Value) {
        this.pm25Value = pm25Value;
    }

    public String getTemperatureValue() {
        return temperatureValue;
    }

    public void setTemperatureValue(String temperatureValue) {
        this.temperatureValue = temperatureValue;
    }

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
