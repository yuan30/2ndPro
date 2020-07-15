package com.example.negativeion.model;

import com.google.gson.annotations.SerializedName;

public class GoogleInfoModel {
    @SerializedName("sub")
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
