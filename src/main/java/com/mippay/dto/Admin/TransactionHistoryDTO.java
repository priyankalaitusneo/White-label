package com.mippay.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryDTO {
    private String transactionId;
    private String type;  // PAYIN or PAYOUT
    private Double amount;
    private LocalDateTime date;
    private String status;
    private String utr;
    private String orderId;
}
