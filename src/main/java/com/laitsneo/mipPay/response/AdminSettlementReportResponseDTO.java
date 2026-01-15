package com.laitsneo.mipPay.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSettlementReportResponseDTO {
    
    private String settlementId;
    private String merchantName;
    private Double amount;
    private LocalDateTime settlementDate;
    private String bankName;  // Merchant's bank (to_bank_name)
    private String status;    // PENDING, COMPLETED, CANCELLED
    private String method;    // WALLET or BANK
    private String utr;       // UTR number for bank transfers
}