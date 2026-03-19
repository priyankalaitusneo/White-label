package com.laitsneo.whitelbl.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.laitsneo.whitelbl.dto.Admin.PayinReportDTO;
import com.laitsneo.whitelbl.dto.Admin.PayoutReportDTO;
import com.laitsneo.whitelbl.dto.Admin.PipesReportDTO;
import com.laitsneo.whitelbl.dto.Admin.SettlementReportRequestDTO;
import com.laitsneo.whitelbl.response.AdminSettlementHistoryResponseDTO;
import com.laitsneo.whitelbl.response.AdminSettlementReportResponseDTO;
import com.laitsneo.whitelbl.response.PayinReportResponse;


public interface ReportService {

	Page<PipesReportDTO> getPipesReport(String pipeName, LocalDate fromDate, LocalDate toDate, int page, int size);

//	List<PayinReportDTO> getPayinReport(String merchantId, String status, String txnId, LocalDate fromDate,
//			LocalDate toDate);

	

	Object getHoldReports(String merchantId, String status, String txnId, String pipe, LocalDate fromDate,
			LocalDate toDate);

	Object getLienReports(String userId, LocalDate fromDate, LocalDate toDate);


    List<AdminSettlementHistoryResponseDTO> getSettlementReport(
    String merchantId, 
    String status, 
    LocalDateTime fromDate, 
    LocalDateTime toDate
);

//	List<AdminSettlementReportResponseDTO> getSettlementReportAdmin(String merchantId, String status, String pipe,
//			LocalDateTime fromDate, LocalDateTime toDate);

	 Map<String, Object> getPayinReport(String merchantId, String status, String txnId, LocalDate fromDate,
			LocalDate toDate, int page, int size);

	 Map<String, Object> getPayoutReport(String merchantId, String status, String txnId, LocalDate fromDate,
			LocalDate toDate);

	 Map<String, Object> getPayoutReport(String merchantId, String status, String txnId, LocalDate fromDate,
			LocalDate toDate, int page, int size);

	 Map<String, Object> getSettlementReportAdminCount(String merchantId, String status, String pipe,
			LocalDateTime fromDate, LocalDateTime toDate);

	
    

}
