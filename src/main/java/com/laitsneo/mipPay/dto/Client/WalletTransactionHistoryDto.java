package com.laitsneo.mipPay.dto.Client;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public class WalletTransactionHistoryDto {
	    private String transactionId;
	    private String type;              // "PAYIN" or "PAYOUT"
	    private BigDecimal amount;
	    private LocalDateTime date;
	    private String status;
	}

