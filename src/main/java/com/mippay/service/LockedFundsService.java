package com.mippay.service;

import com.mippay.dto.Admin.LockedFundsRequestDto;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Map;

public interface LockedFundsService {

    // Lock funds for a specific merchant
    
    ResponseEntity<?> lockFunds(LockedFundsRequestDto requestDto);

    // Update existing locked funds record
    
    ResponseEntity<?> updateLockedFunds(Long lockId, LockedFundsRequestDto requestDto);

    // Delete locked funds record
    
    ResponseEntity<?> deleteLockedFunds(Long lockId);

    // Get all locked funds records with client details
     
    ResponseEntity<?> getAllLockedFunds();

    // Get locked funds by specific merchant/client ID
     
    ResponseEntity<?> getLockedFundsByUserId(String userId);

	Map<String, Object> getLockedFundsReport(String merchantName, String status, LocalDate fromDate, LocalDate toDate,
			int page, int size);
}