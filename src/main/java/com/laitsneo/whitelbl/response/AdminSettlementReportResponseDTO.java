package com.laitsneo.whitelbl.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSettlementReportResponseDTO {
	 private String userId;
	    private String name;
	    private String email;
	    private String mobile;

	    private Double totalCharges;
	    private Double totalGstCharges;
	    private Double totalFinalAmount;

	    private long successCount;
	    private LocalDateTime lastTransactionDate;
}