package com.laitsneo.mipPay.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Request DTO for Settlement Report filtering
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementReportRequestDTO {
    
    // Filter by Status: COMPLETED, PENDING, FAILED
    private String status;
    
    // Filter by Pipe/Gateway ID
    private String pipeId;
    
    // Date range filters
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
    
    // Pagination (optional)
    private Integer page = 0;
    private Integer size = 50;
    
    // Sorting
    private String sortBy = "settlementDate";
    private String sortDirection = "DESC";
}