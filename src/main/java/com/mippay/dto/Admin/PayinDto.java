package com.mippay.dto.Admin;

import lombok.Data;

@Data
public class PayinDto {
    private String orderId;
    private String status;
    private String remarks;
    private String name;
    private String phone;
    private String email;
    private String paymentMethod;
    private String charges;
    private String gstCharges;
    private String totalCharges;
    private String accountNo;
    private String mobile;
    private String address;
    private String amount;       // You can convert to BigDecimal if needed
    private String userId;
    private String redirectRoute;     
}
