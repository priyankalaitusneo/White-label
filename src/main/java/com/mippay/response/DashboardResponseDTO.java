package com.mippay.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponseDTO {
    
    private BigDecimal successAmount;
    private BigDecimal successPercentage;
    
    private BigDecimal pendingAmount;
    private BigDecimal pendingPercentage;
    
    private BigDecimal failedAmount;
    private Long failedCount;
    
    private BigDecimal overallSuccessRatio;
    
}