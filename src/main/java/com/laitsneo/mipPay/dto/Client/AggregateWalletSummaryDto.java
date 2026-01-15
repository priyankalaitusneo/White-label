package com.laitsneo.mipPay.dto.Client;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregateWalletSummaryDto {
    private BigDecimal totalBalance;      // Sum of all account_bal
    private BigDecimal availableBalance;  // Sum of all (account_bal - locked)
    private BigDecimal lockedAmount;      // Sum of all locked amounts
    private Long totalMerchants;          // Total number of merchants
}