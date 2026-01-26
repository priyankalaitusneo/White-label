package com.mippay.service;

import com.mippay.dto.Client.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

// Defines all business operations for settlement management

public interface SettlementService {

    // = Unsettled Merchants List ==========

    
    ResponseEntity<?> getAllUnsettledMerchants();

   
     // Get merchants with unsettled funds filtered by date range
     //@param fromDate Start date (optional)
   
    ResponseEntity<?> getUnsettledMerchantsByDateRange(LocalDate fromDate, LocalDate toDate);

    
     // Get specific merchant's unsettled amount
     //@param userId Merchant user ID
    
    ResponseEntity<?> getMerchantUnsettledAmount(String userId, LocalDate fromDate, LocalDate toDate);

    // Settlement Initiation ==========

    // Initiate a new settlement (Wallet or Bank)
     // @param request Settlement request details
  
    ResponseEntity<?> initiateSettlement(SettlementRequestDTO request);

    // Settlement Management 

    /**
     * Get settlement details by settlement ID
     * @param settlementId Unique settlement identifier
     * @return Complete settlement details
     */
    ResponseEntity<?> getSettlementDetails(String settlementId);

    //Edit existing settlement (only if status = IN_PROGRESS)
    
    ResponseEntity<?> editSettlement(String settlementId, SettlementEditRequestDTO editRequest);

    // Get settlement history with filters
    
    ResponseEntity<?> getSettlementHistory(SettlementHistoryFilterDTO filterDTO);

    // Get all settlements by merchant user ID
   
    ResponseEntity<?> getSettlementsByMerchant(String userId);

    // Get pending settlements (for admin dashboard)
     //@return List of all IN_PROGRESS settlements
     
    ResponseEntity<?> getPendingSettlements();

    //  Scheduler Methods

    // Process all ready settlements (called by scheduler)
   
    void processReadySettlements();

    // Complete a specific settlement
    
    ResponseEntity<?> completeSettlement(String settlementId);

    // ========== Statistics & Reports ==========

    
    ResponseEntity<?> getSettlementStatistics();

    
     //Get merchant settlement summary
     // @param userId Merchant user ID
   
    ResponseEntity<?> getMerchantSettlementSummary(String userId);

    // ========== Validation Methods ==========

    ResponseEntity<?> validateSettlement(String userId, LocalDate fromDate, LocalDate toDate, Double amount);
}