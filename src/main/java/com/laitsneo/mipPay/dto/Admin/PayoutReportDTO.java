package com.laitsneo.mipPay.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutReportDTO {
    private String txnId;
    private String customerName;
    private String status;
    private String method;
    private Double amount;
    private LocalDateTime date;
}
