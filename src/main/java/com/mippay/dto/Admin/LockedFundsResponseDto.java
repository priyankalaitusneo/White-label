package com.mippay.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockedFundsResponseDto {

    private Long lockId;
    private String userId;
    private String merchantName;
    private BigDecimal amountLocked;
    private String reason;
    private LocalDateTime lockedDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private BigDecimal currentBalance;
    private String merchantStatus;
}