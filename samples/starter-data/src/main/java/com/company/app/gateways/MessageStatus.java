package com.company.app.gateways;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MessageStatus {
    @JsonProperty("pending")
    PENDING,
    @JsonProperty("sent")
    SENT,
    @JsonProperty("delivered")
    DELIVERED,
    @JsonProperty("failed")
    FAILED;

    public String toSting() {
        return this.name().toLowerCase();
    }
}
