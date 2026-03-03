package com.laitsneo.whitelbl.controller;

import com.laitsneo.whitelbl.dto.Admin.DashboardRequestDTO;
import com.laitsneo.whitelbl.response.ClientDashboardResponseDTO;
import com.laitsneo.whitelbl.response.DashboardResponseDTO;
import com.laitsneo.whitelbl.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboards")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DashboardController {
	
	@Autowired
    private  DashboardService dashboardService;


	
    @GetMapping("/getPayinData")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPayinDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String vendorId) {
        
        log.info("API Request Received");
        log.info("FromDate: {}, ToDate: {}, VendorId: {}", fromDate, toDate, vendorId);

        try {
            DashboardRequestDTO request = new DashboardRequestDTO();
            request.setFromDate(fromDate);
            request.setToDate(toDate);
            request.setVendorId(vendorId);
            request.setType("PAYIN");

            DashboardResponseDTO response = dashboardService.getPayinDashboard(request);

            log.info("Response Generated Successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching Payin dashboard: " + e.getMessage());
        }
    }
    
    @GetMapping("/getPayoutData")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPayoutDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String vendorId) {
        
        log.info("API Request Received");
        log.info("FromDate: {}, ToDate: {}, VendorId: {}", fromDate, toDate, vendorId);

        try {
            DashboardRequestDTO request = new DashboardRequestDTO();
            request.setFromDate(fromDate);
            request.setToDate(toDate);
            request.setVendorId(vendorId);
            request.setType("PAYOUT");

            DashboardResponseDTO response = dashboardService.getPayoutDashboard(request);

            log.info("Response Generated Successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching Payout dashboard: " + e.getMessage());
        }
    }
 // ===CLIENT DASHBOARD ENDPOINTS 
 
 	@GetMapping("/client/getPayinData")
    @PreAuthorize("hasRole('CLIENT')")

 	public ResponseEntity<?> getClientPayinDashboard(
 			@RequestParam(required = true) String userId,
 			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
 			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

 		log.info("========== CLIENT PAYIN DASHBOARD API ==========");
 		log.info("Client Request Received");
 		log.info("UserId: {}, FromDate: {}, ToDate: {}", userId, fromDate, toDate);

 		try {
 			// Validate userId
 			if (userId == null || userId.trim().isEmpty()) {
 				log.error("UserId is required but not provided");
 				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
 						.body("UserId is required");
 			}

 			// Call service method
 			ClientDashboardResponseDTO response = dashboardService.getClientPayinDashboard(userId, fromDate, toDate);

 			log.info("Client Payin Dashboard Response Generated Successfully");
 			log.info("Today's Total: {}, Success: {}, Failed: {}, Pending: {}", 
 					response.getTotalTodayAmount(), 
 					response.getSuccessfulTodayAmount(),
 					response.getFailedTodayAmount(),
 					response.getPendingTodayAmount());
 			log.info("Yearly Overview: {} months data", response.getYearlyOverview().size());
 			log.info("===============================================");

 			return ResponseEntity.ok(response);

 		} catch (Exception e) {
 			log.error("Error in Client Payin Dashboard API: {}", e.getMessage(), e);
 			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
 					.body("Error fetching Client Payin dashboard: " + e.getMessage());
 		}
 	}

 	// CLIENT PAYOUT DASHBOARD ENDPOINT
 
 	@GetMapping("/client/getPayoutData")
 	 @PreAuthorize("hasRole('CLIENT')")
 	public ResponseEntity<?> getClientPayoutDashboard(
 			@RequestParam(required = true) String userId,
 			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
 			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

 		log.info("========== CLIENT PAYOUT DASHBOARD API ==========");
 		log.info("Client Request Received");
 		log.info("UserId: {}, FromDate: {}, ToDate: {}", userId, fromDate, toDate);

 		try {
 			// Validate userId
 			if (userId == null || userId.trim().isEmpty()) {
 				log.error("UserId is required but not provided");
 				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
 						.body("UserId is required");
 			}

 			// Call service method
 			ClientDashboardResponseDTO response = dashboardService.getClientPayoutDashboard(userId, fromDate, toDate);

 			log.info("Client Payout Dashboard Response Generated Successfully");
 			log.info("Today's Total: {}, Success: {}, Failed: {}, Pending: {}", 
 					response.getTotalTodayAmount(), 
 					response.getSuccessfulTodayAmount(),
 					response.getFailedTodayAmount(),
 					response.getPendingTodayAmount());
 			log.info("Yearly Overview: {} months data", response.getYearlyOverview().size());
 			log.info("================================================");

 			return ResponseEntity.ok(response);

 		} catch (Exception e) {
 			log.error("Error in Client Payout Dashboard API: {}", e.getMessage(), e);
 			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
 					.body("Error fetching Client Payout dashboard: " + e.getMessage());
 		}
 	}
    
}
