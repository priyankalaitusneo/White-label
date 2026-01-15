package com.laitsneo.mipPay.dto.Admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LockedFundsReportDTO {

	private Long lockId;
    private String transactionId;
    private String userId;
    private String merchantName;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime holdDate;
    private LocalDateTime releaseDate;
    private String status;
}
