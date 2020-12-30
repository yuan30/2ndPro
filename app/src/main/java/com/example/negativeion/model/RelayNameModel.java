package com.example.negativeion.model;

import com.google.gson.annotations.SerializedName;

public class RelayNameModel {
    @SerializedName("Relay1Name")
    private String relay1Name;

    @SerializedName("Relay2Name")
    private String relay2Name;

    @SerializedName("Relay3Name")
    private String relay3Name;

    @SerializedName("Relay4Name")
    private String relay4Name;

    @SerializedName("Relay5Name")
    private String relay5Name;

    public String getRelay1Name() {
        return relay1Name;
    }

    public String getRelay2Name() {
        return relay2Name;
    }

    public String getRelay3Name() {
        return relay3Name;
    }

    public String getRelay4Name() {
        return relay4Name;
    }

    public String getRelay5Name() {
        return relay5Name;
    }
}
