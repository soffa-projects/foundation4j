package dev.soffa.foundation.data.app.model;

import dev.soffa.foundation.annotation.Store;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Store("__context__")
@AllArgsConstructor
public class PaymentMetric {

    private long time;
    private String id;
    private String tenant;
    private String application;
    private String account;
    private double amount;
    private String status;
    private String paymentMethod;
}
