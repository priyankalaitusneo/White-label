package com.mippay.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalCheckStatusResponse {
    private String orderId;
    private String status;
    private String statusCode;
    private String message;
}