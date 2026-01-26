package com.mippay.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.mippay.dto.Admin.PipesReportDTO;

import com.mippay.response.AdminSettlementHistoryResponseDTO;
import com.mippay.response.AdminSettlementReportResponseDTO;


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

	List<AdminSettlementReportResponseDTO> getSettlementReportAdmin(String merchantId, String status, String pipe,
			LocalDateTime fromDate, LocalDateTime toDate);

	 Map<String, Object> getPayinReport(String merchantId, String status, String txnId, LocalDate fromDate,
			LocalDate toDate, int page, int size);

	 Map<String, Object> getPayoutReport(String merchantId, String status, String txnId, LocalDate fromDate,
			LocalDate toDate);

	 Map<String, Object> getPayoutReport(String merchantId, String status, String txnId, LocalDate fromDate,
			LocalDate toDate, int page, int size);

	
    

}
