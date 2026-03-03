package com.mippay.dto.Admin;


import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class PayinReportRequest {
    // Filters
    private String merchantId;
    private String status;
    private String txnId;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
    
    // Pagination
    private Integer page; // Default will be 0
}
