package com.mippay.response;


import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PhonePeOrderStatusResponse {

    private String orderId;
    private String state;
    private Long amount;
    private Long payableAmount;
    private Long feeAmount;
    private Long expireAt;

    private Map<String, Object> metaInfo;
    private List<Map<String, Object>> paymentDetails;
}

