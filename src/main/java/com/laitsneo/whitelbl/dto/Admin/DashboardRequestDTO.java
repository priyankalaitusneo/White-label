package com.laitsneo.whitelbl.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRequestDTO {
    
    private LocalDate fromDate;
    private LocalDate toDate;
    private String vendorId;  // pgId filter
    private String type;      // PAYIN or PAYOUT
    
}