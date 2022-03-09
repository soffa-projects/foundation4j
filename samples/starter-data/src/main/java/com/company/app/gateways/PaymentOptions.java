package com.company.app.gateways;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOptions {

    //@JsonProperty("mobile_money")
    private MobileMoneyOption mobileMoney;

}
