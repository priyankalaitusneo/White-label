package com.mippay.serviceImpl.Admin;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.util.stream.Collectors;
import org.springframework.http.ContentDisposition;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.mippay.dto.Admin.HoldReportDto;
import com.mippay.dto.Admin.LienReportDto;
import com.mippay.dto.Admin.PayinReportDTO;
import com.mippay.dto.Admin.PayoutReportDTO;
import com.mippay.dto.Admin.PipesReportDTO;

import com.mippay.entity.Client.Client;
import com.mippay.entity.Client.PayinRecords;

import com.mippay.repository.Admin.SwitchingRepository;
import com.mippay.repository.Client.ClientRepository;
import com.mippay.repository.Client.LienRepository;
import com.mippay.repository.Client.PayinRecordRepository;
import com.mippay.repository.Client.PayoutRepository;
import com.mippay.repository.Client.SettlementRecordRepository;

import com.mippay.response.AdminSettlementHistoryResponseDTO;
import com.mippay.response.AdminSettlementReportResponseDTO;

import com.mippay.service.ReportService;


@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
	

    @Autowired
    private PayinRecordRepository payinRecordRepository;

	@Autowired
    private SwitchingRepository switchingRepository;
	
	 @Autowired
	 private PayoutRepository payoutRepository;

	 @Autowired
	  private  LienRepository lienRepo;
	 
	 @Autowired
	    private  ClientRepository clientRepo;
	 
	 @Autowired
	 private SettlementRecordRepository settlementRecordRepository;

   
    private PipesReportDTO mapToPipesReportDTO(Object[] row) {
        String pipeName = (String) row[0];
        Long totalTransactions = ((Number) row[1]).longValue();
        Long successful = ((Number) row[2]).longValue();
        Long failed = ((Number) row[3]).longValue();
        Double totalAmount = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;

        // Calculate success rate
        Double successRate = 0.0;
        if (totalTransactions > 0) {
            successRate = BigDecimal.valueOf((successful * 100.0) / totalTransactions)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return PipesReportDTO.builder()
                .pipeName(pipeName)
                .totalTransactions(totalTransactions)
                .successful(successful)
                .failed(failed)
                .totalAmount(totalAmount)
                .successRate(successRate)
                .build();
    }


//    @Override
//    public List<PayinReportDTO> getPayinReport(String merchantId, String status, String txnId,
//                                                LocalDate fromDate, LocalDate toDate) {
//        try {
//            log.info("Service: Fetching payin report with filters - merchantId: {}, status: {}, txnId: {}, fromDate: {}, toDate: {}",
//                    merchantId, status, txnId, fromDate, toDate);
//
//            List<Object[]> results = payinRecordRepository.getPayinReport(
//                    merchantId, status, txnId, fromDate, toDate);
//
//            List<PayinReportDTO> reports = results.stream()
//                    .map(this::mapToPayinReportDTO)
//                    .collect(Collectors.toList());
//
//            log.info("Service: Successfully mapped {} payin records to DTOs", reports.size());
//            return reports;
//
//        } catch (Exception e) {
//            log.error("Service: Error in getPayinReport: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to fetch payin report", e);
//        }
//    }
    @Override
    public Map<String, Object> getPayinReport(
            String merchantId,
            String status,
            String txnId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {

        int offset = page * size;

        long totalRecords =
                payinRecordRepository.getPayinReportCount(
                        merchantId, status, txnId, fromDate, toDate
                );

        int totalPages = (int) Math.ceil((double) totalRecords / size);

        List<Map<String, Object>> data =
                payinRecordRepository.getPayinReport(
                        merchantId, status, txnId, fromDate, toDate, offset, size
                );

        Map<String, Object> summary =
                payinRecordRepository.getPayinSummaryCounts(
                        merchantId, txnId, fromDate, toDate
                );

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page);
        pagination.put("pageSize", size);
        pagination.put("totalRecords", totalRecords);
        pagination.put("totalPages", totalPages);
        pagination.put("hasNext", page + 1 < totalPages);
        pagination.put("hasPrevious", page > 0);

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("pagination", pagination);
        response.put("summary", summary);

        return response;
    }




    private PayinReportDTO mapToPayinReportDTO(Map<String, Object> row) {
        return PayinReportDTO.builder()
                .txnId((String) row.get("trxnid"))
                .customerName((String) row.get("name"))
                .status((String) row.get("status"))
                .method((String) row.get("payment_method"))
                .amount(row.get("amount") != null
                        ? ((Number) row.get("amount")).doubleValue()
                        : null)
                .date(row.get("created_date") != null
                        ? ((java.sql.Timestamp) row.get("created_date")).toLocalDateTime()
                        : null)
                .build();
    }

   
    @Override
    public Map<String, Object> getPayoutReport(
            String merchantId,
            String status,
            String txnId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {

        int offset = page * size;

        long totalRecords =
                payoutRepository.getPayoutReportCount(
                        merchantId, status, txnId, fromDate, toDate
                );

        int totalPages = (int) Math.ceil((double) totalRecords / size);

        List<Map<String, Object>> data =
                payoutRepository.getPayoutReport(
                        merchantId, status, txnId, fromDate, toDate, offset, size
                );

        Map<String, Object> summary =
                payoutRepository.getPayoutSummaryCounts(
                        merchantId, txnId, fromDate, toDate
                );

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page);
        pagination.put("pageSize", size);
        pagination.put("totalRecords", totalRecords);
        pagination.put("totalPages", totalPages);
        pagination.put("hasNext", page + 1 < totalPages);
        pagination.put("hasPrevious", page > 0);

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("pagination", pagination);
        response.put("summary", summary);

        return response;
    }

    private PayoutReportDTO mapToPayoutReportDTO(Object[] row) {
        return PayoutReportDTO.builder()
                .txnId((String) row[0])
                .customerName((String) row[1])
                .status((String) row[2])
                .method((String) row[3])
                .amount((Double) row[4])
                .date(row[5] != null ? ((java.sql.Timestamp) row[5]).toLocalDateTime() : null)
                .build();
    }

  

    @Override
    public List<HoldReportDto> getHoldReports(String merchantId, String status, String txnId,
                                              String pipe, LocalDate fromDate, LocalDate toDate) {

        log.info("Fetching Hold Reports with filters: merchantId={}, status={}, txnId={}, pipe={}",
                merchantId, status, txnId, pipe);

        List<PayinRecords> list = payinRecordRepository.filterHoldReports(
                merchantId, status, txnId, pipe, fromDate, toDate
        );

        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private HoldReportDto mapToDto(PayinRecords r) {

        Client merchant = clientRepo.findById(r.getUserId()).get();

        HoldReportDto dto = new HoldReportDto();
        dto.setTransactionId(r.getTrxnid());
        dto.setMerchantName(merchant.getName());
        dto.setAmount(r.getAmount());
        dto.setHoldDate(r.getCreatedDate().toLocalDate());
        dto.setReason(r.getHoldReason());
        dto.setStatus(r.getHoldStatus());

        if ("RELEASED".equalsIgnoreCase(r.getHoldStatus())) {
            dto.setReleaseDate(r.getUpdatedDate().toLocalDate());
        }

        return dto;
    }

  

    @Override
    public List<LienReportDto> getLienReports(String userId, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching Lien Reports: userId={}, from={}, to={}", userId, fromDate, toDate);
        List<Object[]> rows = lienRepo.filterLienReports(userId, fromDate, toDate);
        return rows.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private LienReportDto mapToDto(Object[] row) {
        LienReportDto dto = new LienReportDto();
        dto.setMerchantName((String) row[4]);
        dto.setLienAmount(Double.valueOf(row[1].toString()));
        dto.setReason((String) row[2]);
        Timestamp created = (Timestamp) row[3];
        dto.setLienDate(created.toLocalDateTime().toLocalDate());
        String status = (String) row[5];
        dto.setStatus(status);
        Timestamp releaseDate = (Timestamp) row[6];
        if (releaseDate != null) {
            dto.setReleaseDate(releaseDate.toLocalDateTime().toLocalDate());
        }
        return dto;
    }


    @Override
    public Page<PipesReportDTO> getPipesReport(
            String pipeName,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("switchedPipe").ascending());

        Page<Object[]> result = switchingRepository.getPipeSummary(
                pipeName, fromDate, toDate, pageable);

        return result.map(row -> {
            String pipe = (String) row[0];
            Long total = (Long) row[1];
            Long success = (Long) row[2];
            Long failed = (Long) row[3];

            double successRate = total == 0 ? 0 :
                    (success * 100.0) / total;

            return new PipesReportDTO(
                    pipe,
                    total,
                    success,
                    failed,
                    0.0,              // totalAmount (plug later if needed)
                    successRate
            );
        });
    }



	@Override
	public List<AdminSettlementHistoryResponseDTO> getSettlementReport(
	        String merchantId, String status, LocalDateTime fromDate, LocalDateTime toDate) {
	    
	    try {
	        log.info("Fetching settlement report - merchantId: {}, status: {}, fromDate: {}, toDate: {}",
	                merchantId, status, fromDate, toDate);

	        List<Object[]> results = settlementRecordRepository.getSettlementReport(
	                merchantId, status, fromDate, toDate);

	        List<AdminSettlementHistoryResponseDTO> reports = results.stream()
	                .map(this::mapToSettlementReportDTO)
	                .collect(Collectors.toList());

	        log.info("Successfully fetched {} settlement records", reports.size());
	        return reports;

	    } catch (Exception e) {
	        log.error("Error in getSettlementReport: {}", e.getMessage(), e);
	        throw new RuntimeException("Failed to fetch settlement report", e);
	    }
	}

	private AdminSettlementHistoryResponseDTO mapToSettlementReportDTO(Object[] row) {
	    
		LocalDateTime actualSettlementDate =
		        row[0] != null ? (LocalDateTime) row[0] : null;
	    String userId = (String) row[1];
	    String merchantName = (String) row[2];
	    Double settlementAmount = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
	    String settlementMethod = (String) row[4];
	    
	    // From Account fields
	    String fromAccountHolder = (String) row[5];
	    String fromAccountNumber = (String) row[6];
	    String fromBankName = (String) row[7];
	    String fromIfscCode = (String) row[8];
	    
	    // To Merchant Account fields
	    String toAccountHolder = (String) row[9];
	    String toAccountNumber = (String) row[10];
	    String toBankName = (String) row[11];
	    String toIfscCode = (String) row[12];
	    
	    String utrNumber = (String) row[13];
	    String settlementStatus = (String) row[14];
	    String failureReason = (String) row[15];
	    
	    // Build combined strings
	    String fromAccount = buildFromAccount(settlementMethod, fromAccountHolder, 
	            fromAccountNumber, fromBankName, fromIfscCode);
	    
	    String toMerchantAccount = buildToMerchantAccount(toAccountHolder, 
	            toAccountNumber, toBankName, toIfscCode);
	    
	    return AdminSettlementHistoryResponseDTO.builder()
	            .dateTime(actualSettlementDate)
	            .merchantId(userId)
	            .merchantName(merchantName)
	            .settleAmount(settlementAmount)
	            .fromAccount(fromAccount)
	            .toMerchantAccount(toMerchantAccount)
	            .method(settlementMethod)
	            .utr(utrNumber)
	            .status(settlementStatus)
	            .reason(failureReason)
	            .build();
	}

	private String buildFromAccount(String method, String holder, String accountNumber, 
	                                String bankName, String ifscCode) {
	    if ("WALLET".equalsIgnoreCase(method)) {
	        return (holder != null ? holder + " " : "") + "Wallet";
	    } else if ("BANK".equalsIgnoreCase(method)) {
	        if (bankName != null && accountNumber != null && accountNumber.length() >= 4) {
	            String last4 = accountNumber.substring(accountNumber.length() - 4);
	            return bankName + " - " + last4;
	        } else if (bankName != null) {
	            return bankName;
	        }
	    }
	    return "-";
	}

	private String buildToMerchantAccount(String holder, String accountNumber, 
	                                     String bankName, String ifscCode) {
	    if (bankName != null && accountNumber != null && accountNumber.length() >= 4) {
	        String last4 = accountNumber.substring(accountNumber.length() - 4);
	        return bankName + " - " + last4;
	    } else if (bankName != null && accountNumber != null) {
	        return bankName + " - " + accountNumber;
	    } else if (bankName != null) {
	        return bankName;
	    } else if (accountNumber != null) {
	        return "Account - " + accountNumber;
	    }
	    return "-";
	}
	
	// Settllemts Reports

	@Override
	public List<AdminSettlementReportResponseDTO> getSettlementReportAdmin(String merchantId, String status,
			String pipe, LocalDateTime fromDate, LocalDateTime toDate) {
		// TODO Auto-generated method stub
	        
	        try {
	            log.info("Fetching settlement report - merchantId: {}, status: {}, pipe: {}, fromDate: {}, toDate: {}",
	                    merchantId, status, pipe, fromDate, toDate);

	            // Call the new repository query
	            List<Object[]> results = settlementRecordRepository.getSettlementReportData(
	                    merchantId, status, pipe, fromDate, toDate);

	            // Map to DTO
	            List<AdminSettlementReportResponseDTO> reports = results.stream()
	                    .map(this::mapToSettlementReportAdminDTO)
	                    .collect(Collectors.toList());

	            log.info("Successfully fetched {} settlement records", reports.size());
	            return reports;

	        } catch (Exception e) {
	            log.error("Error in getSettlementReport: {}", e.getMessage(), e);
	            throw new RuntimeException("Failed to fetch settlement report", e);
	        }
	    }

	    /**
	     * Map Object[] to AdminSettlementReportResponseDTO
	     */
	    private AdminSettlementReportResponseDTO mapToSettlementReportAdminDTO(Object[] row) {
	        String settlementId = (String) row[0];
	        String merchantName = (String) row[1];
	        Double settlementAmount = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
	        LocalDateTime actualSettlementDate = row[3] != null ? (LocalDateTime) row[3] : null;
	        String toBankName = (String) row[4];
	        String settlementStatus = (String) row[5];
	        String settlementMethod = (String) row[6];
	        String utrNumber = (String) row[7];
	        
	        // For WALLET settlements, show "Wallet" as bank name
	        String bankName = toBankName;
	        if ("WALLET".equalsIgnoreCase(settlementMethod)) {
	            bankName = "Wallet";
	        } else if (toBankName == null || toBankName.trim().isEmpty()) {
	            bankName = "-";
	        }
	        
	        return AdminSettlementReportResponseDTO.builder()
	                .settlementId(settlementId)
	                .merchantName(merchantName)
	                .amount(settlementAmount)
	                .settlementDate(actualSettlementDate)
	                .bankName(bankName)
	                .status(settlementStatus)
	                .method(settlementMethod)
	                .utr(utrNumber)
	                .build();
	    }


		@Override
		public Map<String, Object> getPayoutReport(String merchantId, String status, String txnId, LocalDate fromDate,
				LocalDate toDate) {
			// TODO Auto-generated method stub
			return null;
		}


		
	
		@Override
		public ResponseEntity<?> downloadAdminPayinReportExcel(
		        String merchantId,
		        String status,
		        String orderId,
		        LocalDate fromDate,
		        LocalDate toDate
		) throws Exception {
			 merchantId = normalize(merchantId);
			    status     = normalize(status);
			    orderId    = normalize(orderId);

			    log.info(
			        "ADMIN PAYIN EXCEL | merchantId={}, status={}, orderId={}, fromDate={}, toDate={}",
			        merchantId, status, orderId, fromDate, toDate
			    );

			    List<Map<String, Object>> records =
			            payinRecordRepository.getPayinReportForExcel(
			                    merchantId, status, orderId, fromDate, toDate
			            );


		    Workbook workbook = new XSSFWorkbook();
		    Sheet sheet = workbook.createSheet("Admin Payin Report");

		    /* ================= HEADER ================= */

		    String[] columns = {
		            "User ID", "Name", "Email", "Mobile", "Address",
		            "Transaction ID", "Order ID", "UTR", "PG ID",
		            "Amount", "Charges", "GST Charges", "Total Charges", "Final Amount",
		            "Status", "Status Code", "Refund Status", "Settlement Status",
		            "Current Balance", "Updated Balance",
		            "Transaction Time", "Created Date", "Updated Date"
		    };

		    Row header = sheet.createRow(0);
		    for (int i = 0; i < columns.length; i++) {
		        header.createCell(i).setCellValue(columns[i]);
		    }

		    /* ================= DATA ================= */

		    DateTimeFormatter formatter =
		            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

		    int rowNum = 1;
		    for (Map<String, Object> r : records) {

		        Row row = sheet.createRow(rowNum++);
		        int c = 0;

		        row.createCell(c++).setCellValue(nvl(r.get("user_id")));
		        row.createCell(c++).setCellValue(nvl(r.get("name")));
		        row.createCell(c++).setCellValue(nvl(r.get("email")));
		        row.createCell(c++).setCellValue(nvl(r.get("mobile")));
		        row.createCell(c++).setCellValue(nvl(r.get("address")));

		        row.createCell(c++).setCellValue(nvl(r.get("trxnid")));
		        row.createCell(c++).setCellValue(nvl(r.get("order_id")));
		        row.createCell(c++).setCellValue(nvl(r.get("utr")));
		        row.createCell(c++).setCellValue(nvl(r.get("pg_id")));

//		        row.createCell(c++).setCellValue(nvl(r.get("payment_method")));
//		        row.createCell(c++).setCellValue(nvl(r.get("transfer_mode")));
//		        row.createCell(c++).setCellValue(nvl(r.get("ifsc")));

		        row.createCell(c++).setCellValue(nvlDouble(r.get("amount")));
		        row.createCell(c++).setCellValue(nvlDouble(r.get("charges")));
		        row.createCell(c++).setCellValue(nvlDouble(r.get("gst_charges")));
		        row.createCell(c++).setCellValue(nvlDouble(r.get("total_charges")));
		        row.createCell(c++).setCellValue(nvlDouble(r.get("final_amount")));

		        row.createCell(c++).setCellValue(nvl(r.get("status")));
		        row.createCell(c++).setCellValue(nvl(r.get("status_code")));
		        row.createCell(c++).setCellValue(nvl(r.get("refund_status")));
		        row.createCell(c++).setCellValue(nvl(r.get("settlement_status")));

		        row.createCell(c++).setCellValue(nvlDouble(r.get("current_balance")));
		        row.createCell(c++).setCellValue(nvlDouble(r.get("updated_balance")));

		        row.createCell(c++).setCellValue(nvl(r.get("time_stamp")));
		        row.createCell(c++).setCellValue(formatDate(r.get("created_date"), formatter));
		        row.createCell(c++).setCellValue(formatDate(r.get("updated_date"), formatter));
		    }

		    for (int i = 0; i < columns.length; i++) {
		        sheet.autoSizeColumn(i);
		    }

		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    workbook.write(out);
		    workbook.close();

		    HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(
		            MediaType.parseMediaType(
		                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
		            )
		    );
		    headers.setContentDisposition(
		            ContentDisposition.attachment()
		                    .filename("admin-payin-report.xlsx")
		                    .build()
		    );

		    return ResponseEntity.ok()
		            .headers(headers)
		            .body(out.toByteArray());
		}


		
	
		private String nvl(Object val) {
		    return val != null ? String.valueOf(val) : "-";
		}

		private double nvlDouble(Object val) {
		    return val != null ? Double.parseDouble(val.toString()) : 0.0;
		}

		private String formatDate(Object val, DateTimeFormatter formatter) {
		    if (val == null) return "-";

		    if (val instanceof LocalDateTime ldt) {
		        return ldt.format(formatter);
		    }

		    if (val instanceof java.sql.Timestamp ts) {
		        return ts.toLocalDateTime().format(formatter);
		    }

		    return val.toString(); // fallback (never crashes)
		}


		private String normalize(String val) {
		    return (val == null || val.trim().isEmpty()) ? null : val.trim();
		}

	

}


