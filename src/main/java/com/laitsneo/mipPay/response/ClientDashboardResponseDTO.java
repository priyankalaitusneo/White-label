package com.laitsneo.mipPay.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDashboardResponseDTO {
    
    // ========== TODAY'S SUMMARY ==========
    private BigDecimal totalTodayAmount;           // Total transactions today
    private BigDecimal successfulTodayAmount;      // Successful transactions today
    private BigDecimal failedTodayAmount;          // Failed transactions today
    private BigDecimal pendingTodayAmount;         // Pending transactions today
    
    private Long totalTodayCount;
    private Long successfulTodayCount;
    private Long failedTodayCount;
    private Long pendingTodayCount;
    
    private BigDecimal successPercentage;          // Success rate percentage
    
    //  YEARLY TRANSACTION OVERVIEW 
    private List<MonthlyTransactionData> yearlyOverview;  // Monthly breakdown
    
   
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyTransactionData {
        private String month;              // Month name
        private Integer monthNumber;       // Month number (1-12)
        private Long successCount;         // Success transaction count
        private Long pendingCount;         // Pending transaction count
        private Long failedCount;          // Failed transaction count
        private Long totalCount;           // Total transaction count
    }
}