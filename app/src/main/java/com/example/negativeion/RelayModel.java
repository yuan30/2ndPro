package com.example.negativeion;

import com.google.gson.annotations.SerializedName;

public class RelayModel {

    @SerializedName("id")
    private String idValue;

    @SerializedName("relay")
    private String relayValue;

    public String getIdValue() {
        return idValue;
    }

    public void setIdValue(String idValue) {
        this.idValue = idValue;
    }

    public String getRelayValue() {
        return relayValue;
    }

    public void setRelayValue(String relayValue) {
        this.relayValue = relayValue;
    }
}
