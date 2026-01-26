package com.mippay.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipesReportDTO {
	 private String pipeName;
	    private Long totalTransactions;
	    private Long successful;
	    private Long failed;
	    private Double totalAmount;
	    private Double successRate;
}
