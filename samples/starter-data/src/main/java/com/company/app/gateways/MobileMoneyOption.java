package com.company.app.gateways;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MobileMoneyOption {

    private String phoneNumber;
    private String country;
    private String network;
    private String last4;
    private String fingerprint;

}
