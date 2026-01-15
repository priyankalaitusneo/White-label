package com.laitsneo.mipPay.response;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSettlementHistoryResponseDTO {
 
 // Date & Time (actualSettlementDate)
 private LocalDateTime dateTime;
 
 // Merchant ID (userId)
 private String merchantId;
 
 // Merchant Name
 private String merchantName;
 
 // Settlement Amount
 private Double settleAmount;
 
 // From (Wallet/Account) - Combined format
 private String fromAccount;
 
 // To (Merchant Account) - Combined format
 private String toMerchantAccount;
 
 // Method (WALLET or BANK)
 private String method;
 
 // UTR Number
 private String utr;
 
 // Status (PENDING, COMPLETED, CANCELLED)
 private String status;
 
 // Reason if Failed (optional)
 private String reason;
}