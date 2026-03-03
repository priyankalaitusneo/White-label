package com.laitsneo.whitelbl.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for Settlement Report - Maps exactly to UI columns
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementReportResponseDTO {
    
    // Settlement ID
    private String settlementId;
    
    // Merchant Name
    private String merchantName;
    
    // Amount (as BigDecimal for precision)
    private BigDecimal amount;
    
    // Settlement Date
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate settlementDate;
    
    // Bank Name
    private String bankName;
    
    // Status (COMPLETED, PENDING, FAILED)
    private String status;
    
    // Additional fields for enhanced reporting (optional)
    private String userId;
    private String settlementMethod; // WALLET or BANK
    private String utrNumber; // For bank transfers
    private String pipeId; // Payment gateway/pipe identifier
}