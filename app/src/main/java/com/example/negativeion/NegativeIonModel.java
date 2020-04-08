package com.example.negativeion;

import com.google.gson.annotations.SerializedName;

public class NegativeIonModel {

    @SerializedName("NegativeIon")
    private String negativeIonValue;

    @SerializedName("PM25")
    private String pm25Value;

    @SerializedName("Temperature")
    private String temperatureValue;

    @SerializedName("Humidity")
    private String humidityValue;

    @SerializedName("Time")
    private String timeValue;

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
