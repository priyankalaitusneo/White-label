package com.laitsneo.whitelbl.dto.Client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDetailResponseDTO {
    private String merchantId;
    private String merchantName;
    private BigDecimal totalFund;
    private BigDecimal availableBalance;
    private BigDecimal lockedAmount;
    private Long totalTransactions;
    private List<WalletTransactionHistoryDto> transactionHistory;
}