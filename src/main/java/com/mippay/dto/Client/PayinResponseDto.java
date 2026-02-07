package com.mippay.dto.Client;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayinResponseDto {

    private String name;
    private String email;
    private String phone;
    private String address;
    private String amount;
    private String orderId;
    private String redirect_url;
    private String status;
    private String statusCode;
    private String createdDate;
    private String updatedDate;
    private String charges;
    private String gstCharges;
    private String userId;
}
