package com.brightsky.medicabdriver.firebasemessaging;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DriverBookingMessagingRequestModel {
    @SerializedName("token")
    @Expose
    private String token;

    @SerializedName("txId")
    @Expose
    private String txId;

    @SerializedName("driverkey")
    @Expose
    private String driverKey;

    public void setToken(String token) {
        this.token = token;
    }

    public String getDriverKey() {
        return driverKey;
    }

    public String getTxId() {
        return txId;
    }

    public void setDriverKey(String driverKey) {
        this.driverKey = driverKey;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getToken() {
        return token;
    }
}