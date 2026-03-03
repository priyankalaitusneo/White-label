package com.mippay.controller;

import com.mippay.dto.Admin.LockedFundsRequestDto;
import com.mippay.service.LockedFundsService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/payment/admin/locked-funds")
public class AdminLockedFundsController {

    private static final Logger logger = LoggerFactory.getLogger(AdminLockedFundsController.class);

    @Autowired
    private LockedFundsService lockedFundsService;

    
    @PostMapping("/lock")
    public ResponseEntity<?> lockFunds(@Valid @RequestBody LockedFundsRequestDto requestDto) {
        logger.info("POST /locked-funds/lock → Request: userId={}, amount={}", 
                   requestDto.getUserId(), requestDto.getAmountLocked());
        
        ResponseEntity<?> response = lockedFundsService.lockFunds(requestDto);
        
        logger.info("POST /locked-funds/lock → Response status: {}", response.getStatusCode());
        return response;
    }

    
    @PutMapping("/{lockId}")
    public ResponseEntity<?> updateLockedFunds(
            @PathVariable Long lockId,
            @Valid @RequestBody LockedFundsRequestDto requestDto) {
        
        logger.info("PUT /locked-funds/{} → Request: userId={}, newAmount={}", 
                   lockId, requestDto.getUserId(), requestDto.getAmountLocked());
        
        ResponseEntity<?> response = lockedFundsService.updateLockedFunds(lockId, requestDto);
        
        logger.info("PUT /locked-funds/{} → Response status: {}", lockId, response.getStatusCode());
        return response;
    }

    
    @DeleteMapping("/{lockId}")
    public ResponseEntity<?> deleteLockedFunds(@PathVariable Long lockId) {
        logger.info("DELETE /locked-funds/{} → Request received", lockId);
        
        ResponseEntity<?> response = lockedFundsService.deleteLockedFunds(lockId);
        
        logger.info("DELETE /locked-funds/{} → Response status: {}", lockId, response.getStatusCode());
        return response;
    }

   
    @GetMapping("/all")
    public ResponseEntity<?> getAllLockedFunds() {
        logger.info("GET /locked-funds/all → Request received");
        
        ResponseEntity<?> response = lockedFundsService.getAllLockedFunds();
        
        logger.info("GET /locked-funds/all → Response status: {}", response.getStatusCode());
        return response;
    }

   
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getLockedFundsByUserId(@PathVariable String userId) {
        logger.info("GET /locked-funds/user/{} → Request received", userId);
        
        ResponseEntity<?> response = lockedFundsService.getLockedFundsByUserId(userId);
        
        logger.info("GET /locked-funds/user/{} → Response status: {}", userId, response.getStatusCode());
        return response;
    }
}