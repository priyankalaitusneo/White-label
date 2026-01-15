package com.laitsneo.mipPay.controller;

import com.laitsneo.mipPay.dto.Admin.SwitchingDTO;
import com.laitsneo.mipPay.entity.Admin.Switching;
import com.laitsneo.mipPay.entity.Admin.Vendors;
import com.laitsneo.mipPay.service.SwitchingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/switching")
public class SwitchingController {

    Logger logger = LoggerFactory.getLogger(SwitchingController.class);

    @Autowired
    private SwitchingService switchingService;

    //PAYIN SWITCHING 

  
    @PostMapping("/payin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> switchAllPayinMerchants(@RequestBody SwitchingDTO request) {
        logger.info("POST /switching/payin/all → Switching all PAYIN merchants to vendor: {}", 
                   request.getVendorName());

        try {
            // Validate request body
            if (request == null) {
                logger.error("POST /switching/payin/all → Request body is null");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Request body is required");
                error.put("errorCode", "INVALID_REQUEST");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Validate vendor name
            if (request.getVendorName() == null || request.getVendorName().trim().isEmpty()) {
                logger.error("POST /switching/payin/all → Vendor name is missing");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Vendor name is required");
                error.put("errorCode", "MISSING_VENDOR_NAME");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Map<String, Object> response = switchingService.switchAllPayinMerchants(request);
            logger.info("POST /switching/payin/all → Success");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("POST /switching/payin/all → Error: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            
            // Specific error codes
            if (e.getMessage().contains("Vendor not found")) {
                error.put("errorCode", "VENDOR_NOT_FOUND");
            } else if (e.getMessage().contains("not active")) {
                error.put("errorCode", "VENDOR_INACTIVE");
            } else if (e.getMessage().contains("No PAYIN merchants found")) {
                error.put("errorCode", "NO_PAYIN_MERCHANTS");
            } else if (e.getMessage().contains("already using")) {
                error.put("errorCode", "ALL_MERCHANTS_ALREADY_USING_VENDOR");
            } else {
                error.put("errorCode", "SWITCHING_FAILED");
            }
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (Exception e) {
            logger.error("POST /switching/payin/all → Unexpected error: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Internal server error occurred while switching PAYIN merchants");
            error.put("errorCode", "INTERNAL_ERROR");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

   
    @PostMapping("/payin/selected")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> switchSelectedPayinMerchants(@RequestBody Map<String, Object> request) throws ClassCastException {
        logger.info("POST /switching/payin/selected → Switching selected PAYIN merchants");

        try {
            // Validate request body
            if (request == null || request.isEmpty()) {
                logger.error("POST /switching/payin/selected → Request body is empty");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Request body is required");
                error.put("errorCode", "INVALID_REQUEST");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Extract vendor name
            String vendorName = (String) request.get("vendorName");
            if (vendorName == null || vendorName.trim().isEmpty()) {
                logger.error("POST /switching/payin/selected → Vendor name is missing");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Vendor name is required");
                error.put("errorCode", "MISSING_VENDOR_NAME");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Extract merchant IDs
            @SuppressWarnings("unchecked")
            List<String> merchantIds = (List<String>) request.get("merchantIds");
            if (merchantIds == null || merchantIds.isEmpty()) {
                logger.error("POST /switching/payin/selected → Merchant IDs list is missing or empty");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Merchant IDs list is required and cannot be empty");
                error.put("errorCode", "MISSING_MERCHANT_IDS");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Map<String, Object> response = switchingService.switchSelectedPayinMerchants(vendorName, merchantIds);
            logger.info("POST /switching/payin/selected → Success");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("POST /switching/payin/selected → Error: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            
            // Specific error codes
            if (e.getMessage().contains("Vendor not found")) {
                error.put("errorCode", "VENDOR_NOT_FOUND");
            } else if (e.getMessage().contains("not active")) {
                error.put("errorCode", "VENDOR_INACTIVE");
            } else if (e.getMessage().contains("None of the provided merchant IDs")) {
                error.put("errorCode", "NO_VALID_PAYIN_MERCHANTS");
            } else if (e.getMessage().contains("cannot be empty")) {
                error.put("errorCode", "EMPTY_MERCHANT_LIST");
            } else if (e.getMessage().contains("already using")) {
                error.put("errorCode", "ALL_MERCHANTS_ALREADY_USING_VENDOR");
            } else {
                error.put("errorCode", "SWITCHING_FAILED");
            }
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (Exception e) {
            logger.error("POST /switching/payin/selected → Unexpected error: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Internal server error occurred while switching selected PAYIN merchants");
            error.put("errorCode", "INTERNAL_ERROR");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

   
    @GetMapping("/payin/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPayinSwitchingLogs() {
        logger.info("GET /switching/payin/logs → Fetching all PAYIN switching logs");

        try {
            List<Switching> logs = switchingService.getPayinSwitchingLogs();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PAYIN switching logs retrieved successfully");
            response.put("data", logs);
            response.put("count", logs.size());
            response.put("merchantType", "PAYIN");

            logger.info("GET /switching/payin/logs → Retrieved {} logs", logs.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("GET /switching/payin/logs → Error: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve PAYIN switching logs");
            error.put("errorCode", "FETCH_LOGS_FAILED");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    //  PAYOUT SWITCHING 
    
    @PostMapping("/payout/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> switchAllPayoutMerchants(@RequestBody SwitchingDTO request) {
        logger.info("POST /switching/payout/all → Switching all PAYOUT merchants to vendor: {}", 
                   request.getVendorName());

        try {
            // Validate request body
            if (request == null) {
                logger.error("POST /switching/payout/all → Request body is null");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Request body is required");
                error.put("errorCode", "INVALID_REQUEST");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Validate vendor name
            if (request.getVendorName() == null || request.getVendorName().trim().isEmpty()) {
                logger.error("POST /switching/payout/all → Vendor name is missing");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Vendor name is required");
                error.put("errorCode", "MISSING_VENDOR_NAME");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Map<String, Object> response = switchingService.switchAllPayoutMerchants(request);
            logger.info("POST /switching/payout/all → Success");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("POST /switching/payout/all → Error: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            
            // Specific error codes
            if (e.getMessage().contains("Vendor not found")) {
                error.put("errorCode", "VENDOR_NOT_FOUND");
            } else if (e.getMessage().contains("not active")) {
                error.put("errorCode", "VENDOR_INACTIVE");
            } else if (e.getMessage().contains("No PAYOUT merchants found")) {
                error.put("errorCode", "NO_PAYOUT_MERCHANTS");
            } else if (e.getMessage().contains("already using")) {
                error.put("errorCode", "ALL_MERCHANTS_ALREADY_USING_VENDOR");
            } else {
                error.put("errorCode", "SWITCHING_FAILED");
            }
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (Exception e) {
            logger.error("POST /switching/payout/all → Unexpected error: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Internal server error occurred while switching PAYOUT merchants");
            error.put("errorCode", "INTERNAL_ERROR");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

   
    @PostMapping("/payout/selected")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> switchSelectedPayoutMerchants(@RequestBody Map<String, Object> request) throws ClassCastException {
        logger.info("POST /switching/payout/selected → Switching selected PAYOUT merchants");

        try {
            // Validate request body
            if (request == null || request.isEmpty()) {
                logger.error("POST /switching/payout/selected → Request body is empty");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Request body is required");
                error.put("errorCode", "INVALID_REQUEST");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Extract vendor name
            String vendorName = (String) request.get("vendorName");
            if (vendorName == null || vendorName.trim().isEmpty()) {
                logger.error("POST /switching/payout/selected → Vendor name is missing");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Vendor name is required");
                error.put("errorCode", "MISSING_VENDOR_NAME");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Extract merchant IDs
            @SuppressWarnings("unchecked")
            List<String> merchantIds = (List<String>) request.get("merchantIds");
            if (merchantIds == null || merchantIds.isEmpty()) {
                logger.error("POST /switching/payout/selected → Merchant IDs list is missing or empty");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Merchant IDs list is required and cannot be empty");
                error.put("errorCode", "MISSING_MERCHANT_IDS");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Map<String, Object> response = switchingService.switchSelectedPayoutMerchants(vendorName, merchantIds);
            logger.info("POST /switching/payout/selected → Success");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("POST /switching/payout/selected → Error: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            
            // Specific error codes
            if (e.getMessage().contains("Vendor not found")) {
                error.put("errorCode", "VENDOR_NOT_FOUND");
            } else if (e.getMessage().contains("not active")) {
                error.put("errorCode", "VENDOR_INACTIVE");
            } else if (e.getMessage().contains("None of the provided merchant IDs")) {
                error.put("errorCode", "NO_VALID_PAYOUT_MERCHANTS");
            } else if (e.getMessage().contains("cannot be empty")) {
                error.put("errorCode", "EMPTY_MERCHANT_LIST");
            } else if (e.getMessage().contains("already using")) {
                error.put("errorCode", "ALL_MERCHANTS_ALREADY_USING_VENDOR");
            } else {
                error.put("errorCode", "SWITCHING_FAILED");
            }
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (Exception e) {
            logger.error("POST /switching/payout/selected → Unexpected error: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Internal server error occurred while switching selected PAYOUT merchants");
            error.put("errorCode", "INTERNAL_ERROR");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

   
    @GetMapping("/payout/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPayoutSwitchingLogs() {
        logger.info("GET /switching/payout/logs → Fetching all PAYOUT switching logs");

        try {
            List<Switching> logs = switchingService.getPayoutSwitchingLogs();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PAYOUT switching logs retrieved successfully");
            response.put("data", logs);
            response.put("count", logs.size());
            response.put("merchantType", "PAYOUT");

            logger.info("GET /switching/payout/logs → Retrieved {} logs", logs.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("GET /switching/payout/logs → Error: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve PAYOUT switching logs");
            error.put("errorCode", "FETCH_LOGS_FAILED");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== COMMON ENDPOINT ====================

    /**
     * GET /switching/vendors
     * Get all active vendors (used by both PAYIN and PAYOUT)
     */
    @GetMapping("/vendors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllActiveVendors() {
        logger.info("GET /switching/vendors → Fetching all active vendors");

        try {
            List<Vendors> vendors = switchingService.getAllActiveVendors();

            if (vendors.isEmpty()) {
                logger.warn("GET /switching/vendors → No active vendors found");
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "No active vendors found in the system");
                response.put("data", vendors);
                response.put("count", 0);
                return ResponseEntity.ok(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Active vendors retrieved successfully");
            response.put("data", vendors);
            response.put("count", vendors.size());

            logger.info("GET /switching/vendors → Retrieved {} vendors", vendors.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("GET /switching/vendors → Error: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve active vendors");
            error.put("errorCode", "FETCH_VENDORS_FAILED");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}