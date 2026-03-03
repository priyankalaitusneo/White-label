package com.mippay.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionHistoryDto {

    private String txnId;
    private String customer;
    private String utr;
    private String status;
    private String method;
    private String amount;
    private String date;
}
