package com.laitsneo.whitelbl.serviceImpl.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.laitsneo.whitelbl.dto.Admin.DashboardRequestDTO;
import com.laitsneo.whitelbl.response.ClientDashboardResponseDTO;
import com.laitsneo.whitelbl.response.DashboardResponseDTO;
import com.laitsneo.whitelbl.service.DashboardService;
import com.laitsneo.whitelbl.repository.Client.PayinRecordRepository;
import com.laitsneo.whitelbl.repository.Client.PayoutRepository;
import com.laitsneo.whitelbl.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {
	
	@Autowired
	private  PayinRecordRepository payinRecordRepository;
	
	@Autowired
    private  PayoutRepository payoutRepository;

	@Override
    public DashboardResponseDTO getPayinDashboard(DashboardRequestDTO request) {
        log.info("Fetching Payin Dashboard Data");
        log.info("Filters - FromDate: {}, ToDate: {}, VendorId: {}", 
                request.getFromDate(), request.getToDate(), request.getVendorId());

        try {
            LocalDateTime fromDateTime = request.getFromDate() != null 
                ? request.getFromDate().atStartOfDay() 
                : LocalDateTime.of(2000, 1, 1, 0, 0);
            
            LocalDateTime toDateTime = request.getToDate() != null 
                ? request.getToDate().atTime(LocalTime.MAX) 
                : LocalDateTime.now();

            log.info("DateTime Range - From: {}, To: {}", fromDateTime, toDateTime);

            Map<String, Object> dashboardData = payinRecordRepository.getPayinDashboardData(
                    fromDateTime, 
                    toDateTime, 
                    request.getVendorId()
            );

            log.info("Query Executed Successfully");
            log.info("Raw Data: {}", dashboardData);
            // Extract and convert data
            BigDecimal successAmount = convertToBigDecimal(dashboardData.get("successAmount"));
            Long successCount = convertToLong(dashboardData.get("successCount"));
            
            BigDecimal pendingAmount = convertToBigDecimal(dashboardData.get("pendingAmount"));
            Long pendingCount = convertToLong(dashboardData.get("pendingCount"));
            
            BigDecimal failedAmount = convertToBigDecimal(dashboardData.get("failedAmount"));
            Long failedCount = convertToLong(dashboardData.get("failedCount"));
            
            Long totalCount = convertToLong(dashboardData.get("totalCount"));

            log.info("Success: Amount={}, Count={}", successAmount, successCount);
            log.info("Pending: Amount={}, Count={}", pendingAmount, pendingCount);
            log.info("Failed: Amount={}, Count={}", failedAmount, failedCount);
            log.info("Total Transactions: {}", totalCount);

            // Calculate percentages
            BigDecimal successPercentage = calculatePercentage(successCount, totalCount);
            BigDecimal pendingPercentage = calculatePercentage(pendingCount, totalCount);
            BigDecimal overallSuccessRatio = calculatePercentage(successCount, totalCount);

            log.info("Success %: {}, Pending %: {}, Overall Success Ratio: {}", 
                    successPercentage, pendingPercentage, overallSuccessRatio);

            DashboardResponseDTO response = DashboardResponseDTO.builder()
                    .successAmount(successAmount)
                    .successPercentage(successPercentage)
                    .pendingAmount(pendingAmount)
                    .pendingPercentage(pendingPercentage)
                    .failedAmount(failedAmount)
                    .failedCount(failedCount)
                    .overallSuccessRatio(overallSuccessRatio)
                    .build();

            log.info("Payin Dashboard Response Prepared Successfully");
            return response;

        } catch (Exception e) {
            log.error("Error fetching Payin Dashboard: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch Payin dashboard data: " + e.getMessage());
        }
    }
	
	 @Override
	    public DashboardResponseDTO getPayoutDashboard(DashboardRequestDTO request) {
	        log.info("Fetching Payout Dashboard Data");
	        log.info("Filters - FromDate: {}, ToDate: {}, VendorId: {}", 
	                request.getFromDate(), request.getToDate(), request.getVendorId());

	        try {
	            // Convert LocalDate to LocalDateTime
	            LocalDateTime fromDateTime = request.getFromDate() != null 
	                ? request.getFromDate().atStartOfDay() 
	                : LocalDateTime.of(2000, 1, 1, 0, 0);
	            
	            LocalDateTime toDateTime = request.getToDate() != null 
	                ? request.getToDate().atTime(LocalTime.MAX) 
	                : LocalDateTime.now();

	            log.info("DateTime Range - From: {}, To: {}", fromDateTime, toDateTime);

	            // Get aggregated data from repository
	            Map<String, Object> dashboardData = payoutRepository.getPayoutDashboardData(
	                    fromDateTime, 
	                    toDateTime, 
	                    request.getVendorId()
	            );

	            log.info("Query Executed Successfully");
	            log.info("Raw Data: {}", dashboardData);

	            // Extract and convert data
	            BigDecimal successAmount = convertToBigDecimal(dashboardData.get("successAmount"));
	            Long successCount = convertToLong(dashboardData.get("successCount"));
	            
	            BigDecimal pendingAmount = convertToBigDecimal(dashboardData.get("pendingAmount"));
	            Long pendingCount = convertToLong(dashboardData.get("pendingCount"));
	            
	            BigDecimal failedAmount = convertToBigDecimal(dashboardData.get("failedAmount"));
	            Long failedCount = convertToLong(dashboardData.get("failedCount"));
	            
	            Long totalCount = convertToLong(dashboardData.get("totalCount"));

	            log.info("Success: Amount={}, Count={}", successAmount, successCount);
	            log.info("Pending: Amount={}, Count={}", pendingAmount, pendingCount);
	            log.info("Failed: Amount={}, Count={}", failedAmount, failedCount);
	            log.info("Total Transactions: {}", totalCount);

	            // Calculate percentages
	            BigDecimal successPercentage = calculatePercentage(successCount, totalCount);
	            BigDecimal pendingPercentage = calculatePercentage(pendingCount, totalCount);
	            BigDecimal overallSuccessRatio = calculatePercentage(successCount, totalCount);

	            log.info("Success %: {}, Pending %: {}, Overall Success Ratio: {}", 
	                    successPercentage, pendingPercentage, overallSuccessRatio);

	            DashboardResponseDTO response = DashboardResponseDTO.builder()
	                    .successAmount(successAmount)
	                    .successPercentage(successPercentage)
	                    .pendingAmount(pendingAmount)
	                    .pendingPercentage(pendingPercentage)
	                    .failedAmount(failedAmount)
	                    .failedCount(failedCount)
	                    .overallSuccessRatio(overallSuccessRatio)
	                    .build();

	            log.info("Payout Dashboard Response Prepared Successfully");
	            return response;

	        } catch (Exception e) {
	            log.error("Error fetching Payout Dashboard: {}", e.getMessage(), e);
	            throw new RuntimeException("Failed to fetch Payout dashboard data: " + e.getMessage());
	        }
	    }
	
	
	
	private BigDecimal calculatePercentage(Long count, Long total) {
        if (total == null || total == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal countDecimal = BigDecimal.valueOf(count != null ? count : 0);
        BigDecimal totalDecimal = BigDecimal.valueOf(total);
        return countDecimal.multiply(BigDecimal.valueOf(100))
                .divide(totalDecimal, 2, RoundingMode.HALF_UP);
    }
	
	 private BigDecimal convertToBigDecimal(Object value) {
	        if (value == null) {
	            return BigDecimal.ZERO;
	        }
	        if (value instanceof BigDecimal) {
	            return (BigDecimal) value;
	        }
	        if (value instanceof Double) {
	            return BigDecimal.valueOf((Double) value);
	        }
	        if (value instanceof Long) {
	            return BigDecimal.valueOf((Long) value);
	        }
	        if (value instanceof Integer) {
	            return BigDecimal.valueOf((Integer) value);
	        }
	        return BigDecimal.ZERO;
	    }
	 
	 private Long convertToLong(Object value) {
	        if (value == null) {
	            return 0L;
	        }
	        if (value instanceof Long) {
	            return (Long) value;
	        }
	        if (value instanceof Integer) {
	            return ((Integer) value).longValue();
	        }
	        if (value instanceof BigDecimal) {
	            return ((BigDecimal) value).longValue();
	        }
	        return 0L;
	    }

	// == CLIENT DASHBOARD IMPLEMENTATION 

	
		@Override
		public ClientDashboardResponseDTO getClientPayinDashboard(String userId, LocalDate fromDate, LocalDate toDate) {
			log.info("========== CLIENT PAYIN DASHBOARD ==========");
			log.info("Fetching Client Payin Dashboard for userId: {}", userId);
			log.info("Date Range - From: {}, To: {}", fromDate, toDate);

			try {
				//  STEP 1: Get Today's Summary 
				log.info("Fetching today's payin summary for client...");
				Map<String, Object> todaySummary = payinRecordRepository.getClientPayinTodaySummary(userId);
				log.info("Today's Summary Retrieved: {}", todaySummary);

				BigDecimal totalTodayAmount = convertToBigDecimal(todaySummary.get("totalAmount"));
				BigDecimal successAmount = convertToBigDecimal(todaySummary.get("successAmount"));
				Long successCount = convertToLong(todaySummary.get("successCount"));
				
				BigDecimal pendingAmount = convertToBigDecimal(todaySummary.get("pendingAmount"));
				Long pendingCount = convertToLong(todaySummary.get("pendingCount"));
				
				BigDecimal failedAmount = convertToBigDecimal(todaySummary.get("failedAmount"));
				Long failedCount = convertToLong(todaySummary.get("failedCount"));
				
				Long totalCount = convertToLong(todaySummary.get("totalCount"));

				// Calculate success percentage
				BigDecimal successPercentage = calculatePercentage(successCount, totalCount);

				log.info("Today's Metrics - Total: {}, Success: {}, Pending: {}, Failed: {}", 
						totalTodayAmount, successAmount, pendingAmount, failedAmount);

				//  STEP 2: Get Yearly Overview 
				log.info("Fetching yearly payin overview for client...");
				
				// Set date range for yearly data
				LocalDateTime fromDateTime = fromDate != null 
					? fromDate.atStartOfDay() 
					: LocalDate.now().withDayOfYear(1).atStartOfDay(); // Start of current year
				
				LocalDateTime toDateTime = toDate != null 
					? toDate.atTime(LocalTime.MAX) 
					: LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear()).atTime(LocalTime.MAX); // End of current year

				log.info("Yearly Overview DateTime Range - From: {}, To: {}", fromDateTime, toDateTime);

				List<Map<String, Object>> yearlyData = payinRecordRepository.getClientPayinYearlyOverview(
						userId, fromDateTime, toDateTime);
				
				log.info("Yearly Data Retrieved: {} months", yearlyData.size());

				// Convert to monthly transaction data
				List<ClientDashboardResponseDTO.MonthlyTransactionData> monthlyOverview = yearlyData.stream()
						.map(data -> {
							Integer monthNum = convertToInteger(data.get("month"));
							String monthName = (String) data.get("monthName");
							
							return ClientDashboardResponseDTO.MonthlyTransactionData.builder()
									.month(monthName != null ? monthName.substring(0, 3).toUpperCase() : "")
									.monthNumber(monthNum)
									.successCount(convertToLong(data.get("successCount")))
									.pendingCount(convertToLong(data.get("pendingCount")))
									.failedCount(convertToLong(data.get("failedCount")))
									.totalCount(convertToLong(data.get("totalCount")))
									.build();
						})
						.toList();

				log.info("Monthly Overview Processed: {} entries", monthlyOverview.size());

				// STEP 3: Build Response 
				ClientDashboardResponseDTO response = ClientDashboardResponseDTO.builder()
						.totalTodayAmount(totalTodayAmount)
						.successfulTodayAmount(successAmount)
						.successfulTodayCount(successCount)
						.pendingTodayAmount(pendingAmount)
						.pendingTodayCount(pendingCount)
						.failedTodayAmount(failedAmount)
						.failedTodayCount(failedCount)
						.totalTodayCount(totalCount)
						.successPercentage(successPercentage)
						.yearlyOverview(monthlyOverview)
						.build();

				log.info("Client Payin Dashboard Response Prepared Successfully");
				log.info("==========================================");
				return response;

			} catch (Exception e) {
				log.error("Error fetching Client Payin Dashboard: {}", e.getMessage(), e);
				throw new RuntimeException("Failed to fetch Client Payin dashboard: " + e.getMessage());
			}
		}

		// CLIENT DASHBOARD: Get Payout Dashboard for specific client
		
		@Override
		public ClientDashboardResponseDTO getClientPayoutDashboard(String userId, LocalDate fromDate, LocalDate toDate) {
			log.info("========== CLIENT PAYOUT DASHBOARD ==========");
			log.info("Fetching Client Payout Dashboard for userId: {}", userId);
			log.info("Date Range - From: {}, To: {}", fromDate, toDate);

			try {
				// ========== STEP 1: Get Today's Summary ==========
				log.info("Fetching today's payout summary for client...");
				Map<String, Object> todaySummary = payoutRepository.getClientPayoutTodaySummary(userId);
				log.info("Today's Summary Retrieved: {}", todaySummary);

				BigDecimal totalTodayAmount = convertToBigDecimal(todaySummary.get("totalAmount"));
				BigDecimal successAmount = convertToBigDecimal(todaySummary.get("successAmount"));
				Long successCount = convertToLong(todaySummary.get("successCount"));
				
				BigDecimal pendingAmount = convertToBigDecimal(todaySummary.get("pendingAmount"));
				Long pendingCount = convertToLong(todaySummary.get("pendingCount"));
				
				BigDecimal failedAmount = convertToBigDecimal(todaySummary.get("failedAmount"));
				Long failedCount = convertToLong(todaySummary.get("failedCount"));
				
				Long totalCount = convertToLong(todaySummary.get("totalCount"));

				// Calculate success percentage
				BigDecimal successPercentage = calculatePercentage(successCount, totalCount);

				log.info("Today's Metrics - Total: {}, Success: {}, Pending: {}, Failed: {}", 
						totalTodayAmount, successAmount, pendingAmount, failedAmount);

				// ========== STEP 2: Get Yearly Overview ==========
				log.info("Fetching yearly payout overview for client...");
				
				// Set date range for yearly data
				LocalDateTime fromDateTime = fromDate != null 
					? fromDate.atStartOfDay() 
					: LocalDate.now().withDayOfYear(1).atStartOfDay(); // Start of current year
				
				LocalDateTime toDateTime = toDate != null 
					? toDate.atTime(LocalTime.MAX) 
					: LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear()).atTime(LocalTime.MAX); // End of current year

				log.info("Yearly Overview DateTime Range - From: {}, To: {}", fromDateTime, toDateTime);

				List<Map<String, Object>> yearlyData = payoutRepository.getClientPayoutYearlyOverview(
						userId, fromDateTime, toDateTime);
				
				log.info("Yearly Data Retrieved: {} months", yearlyData.size());

				// Convert to monthly transaction data
				List<ClientDashboardResponseDTO.MonthlyTransactionData> monthlyOverview = yearlyData.stream()
						.map(data -> {
							Integer monthNum = convertToInteger(data.get("month"));
							String monthName = (String) data.get("monthName");
							
							return ClientDashboardResponseDTO.MonthlyTransactionData.builder()
									.month(monthName != null ? monthName.substring(0, 3).toUpperCase() : "")
									.monthNumber(monthNum)
									.successCount(convertToLong(data.get("successCount")))
									.pendingCount(convertToLong(data.get("pendingCount")))
									.failedCount(convertToLong(data.get("failedCount")))
									.totalCount(convertToLong(data.get("totalCount")))
									.build();
						})
						.toList();

				log.info("Monthly Overview Processed: {} entries", monthlyOverview.size());

				//  STEP 3: Build Response 
				ClientDashboardResponseDTO response = ClientDashboardResponseDTO.builder()
						.totalTodayAmount(totalTodayAmount)
						.successfulTodayAmount(successAmount)
						.successfulTodayCount(successCount)
						.pendingTodayAmount(pendingAmount)
						.pendingTodayCount(pendingCount)
						.failedTodayAmount(failedAmount)
						.failedTodayCount(failedCount)
						.totalTodayCount(totalCount)
						.successPercentage(successPercentage)
						.yearlyOverview(monthlyOverview)
						.build();

				log.info("Client Payout Dashboard Response Prepared Successfully");
				log.info("==========================================");
				return response;

			} catch (Exception e) {
				log.error("Error fetching Client Payout Dashboard: {}", e.getMessage(), e);
				throw new RuntimeException("Failed to fetch Client Payout dashboard: " + e.getMessage());
			}
		}

		// HELPER METHOD: Convert Object to Integer
		 
		private Integer convertToInteger(Object value) {
			if (value == null) {
				return 0;
			}
			if (value instanceof Integer) {
				return (Integer) value;
			}
			if (value instanceof Long) {
				return ((Long) value).intValue();
			}
			if (value instanceof BigDecimal) {
				return ((BigDecimal) value).intValue();
			}
			return 0;
		}

}
