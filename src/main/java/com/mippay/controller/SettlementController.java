package com.mippay.controller;

import com.mippay.dto.Client.*;
import com.mippay.service.SettlementService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("/api/settlements")
@CrossOrigin(origins = "*")
public class SettlementController {

    private static final Logger logger = LoggerFactory.getLogger(SettlementController.class);

    @Autowired
    private SettlementService settlementService;

    //  Unsettled Merchants 
    
    @GetMapping("/unsettled-merchants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUnsettledMerchants() {
        logger.info("API Called: GET /api/settlements/unsettled-merchants");
        return settlementService.getAllUnsettledMerchants();
    }

    // unsettled merchants with date range filter
     
    @GetMapping("/unsettled-merchants/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUnsettledMerchantsByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        logger.info("API Called: GET /api/settlements/unsettled-merchants/filter - fromDate: {}, toDate: {}", 
                   fromDate, toDate);
        return settlementService.getUnsettledMerchantsByDateRange(fromDate, toDate);
    }

    // Get specific merchant's unsettled amount
    
    @GetMapping("/merchant/{userId}/unsettled-amount")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMerchantUnsettledAmount(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        logger.info("API Called: GET /api/settlements/merchant/{}/unsettled-amount - fromDate: {}, toDate: {}", 
                   userId, fromDate, toDate);
        return settlementService.getMerchantUnsettledAmount(userId, fromDate, toDate);
    }

    //  Settlement Initiation ==========

    // Initiate a new settlement
    
    @PostMapping("/initiate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> initiateSettlement(
            @Valid @RequestBody SettlementRequestDTO request,
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        // Set dates coming from query params into DTO
        request.setFromDate(fromDate);
        request.setToDate(toDate);

        logger.info("API Called: POST /api/settlements/initiate - userId: {}, method: {}, amount: {}, fromDate: {}, toDate: {}", 
                request.getUserId(), 
                request.getSettlementMethod(), 
                request.getSettlementAmount(),
                fromDate,
                toDate);

        return settlementService.initiateSettlement(request);
    }


    // ========== Settlement Management ==========

    // Get settlement details by ID
    
    @GetMapping("/{settlementId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSettlementDetails(@PathVariable String settlementId) {
        logger.info("API Called: GET /api/settlements/{}", settlementId);
        return settlementService.getSettlementDetails(settlementId);
    }

    // Edit existing settlement
   
    @PutMapping("/{settlementId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> editSettlement(
            @PathVariable String settlementId,
            @Valid @RequestBody SettlementEditRequestDTO editRequest) {
        
        logger.info("API Called: PUT /api/settlements/{}/edit", settlementId);
        return settlementService.editSettlement(settlementId, editRequest);
    }

    // Get settlement history with filters
    
    @PostMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSettlementHistory(@RequestBody SettlementHistoryFilterDTO filterDTO) {
        logger.info("API Called: POST /api/settlements/history - filters: {}", filterDTO);
        return settlementService.getSettlementHistory(filterDTO);
    }

    // Get all settlements by merchant
    
    @GetMapping("/merchant/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSettlementsByMerchant(@PathVariable String userId) {
        logger.info("API Called: GET /api/settlements/merchant/{}", userId);
        return settlementService.getSettlementsByMerchant(userId);
    }

    // Get all pending settlements
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingSettlements() {
        logger.info("API Called: GET /api/settlements/pending");
        return settlementService.getPendingSettlements();
    }

    
    // Manually complete a settlement for testing 
    
    @PutMapping("/{settlementId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> completeSettlement(@PathVariable String settlementId) {
        logger.info("API Called: PUT /api/settlements/{}/complete", settlementId);
        return settlementService.completeSettlement(settlementId);
    }

    // ========== Statistics & Reports ==========

    //  Get settlement statistics
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSettlementStatistics() {
        logger.info("API Called: GET /api/settlements/statistics");
        return settlementService.getSettlementStatistics();
    }

    // Get merchant settlement summary
     
    @GetMapping("/merchant/{userId}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMerchantSettlementSummary(@PathVariable String userId) {
        logger.info("API Called: GET /api/settlements/merchant/{}/summary", userId);
        return settlementService.getMerchantSettlementSummary(userId);
    }

    // Validate settlement before initiating
   
    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> validateSettlement(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam Double amount) {
        
        logger.info("API Called: POST /api/settlements/validate - userId: {}, amount: {}", userId, amount);
        return settlementService.validateSettlement(userId, fromDate, toDate, amount);
    }

    
}