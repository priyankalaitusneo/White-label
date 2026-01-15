package com.laitsneo.mipPay.controller;

import com.laitsneo.mipPay.dto.Client.AuthenticationResponseDto;
import com.laitsneo.mipPay.service.AuthenticationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class AuthenticationController {
	
    Logger logger = LoggerFactory.getLogger(AuthenticationController.class);


    @Autowired
    private AuthenticationService authenticationService;


    @PostMapping("/client/create/{userId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> createAuthentication(@PathVariable String userId) {
    	logger.info("POST /client/create/{} → Creating authentication credentials", userId);
        try {
            AuthenticationResponseDto response = authenticationService.createAuthentication(userId);
            logger.info("POST /client/create/{} → Created successfully: {}", userId, response);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Authentication credentials created successfully");
            result.put("data", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
        	logger.error("POST /client/create/{} → Error: {}", userId, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    @GetMapping("/client/get/{userId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> getAuthenticationByUserId(@PathVariable String userId) {
    	logger.info("GET /client/get/{} → Fetching authentication details", userId);
        try {
            AuthenticationResponseDto response = authenticationService.getAuthenticationByUserId(userId);
            logger.info("GET /client/get/{} → Retrieved successfully", userId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Authentication details retrieved successfully");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
        	logger.error("GET /client/get/{} → Error: {}", userId, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @PutMapping("/client/update/{userId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> updateAuthentication(@PathVariable String userId) {
    	logger.info("PUT /client/update/{} → Updating authentication credentials", userId);
        try {
            AuthenticationResponseDto response = authenticationService.updateAuthentication(userId);
            logger.info("PUT /client/update/{} → Updated successfully", userId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Authentication credentials updated successfully");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
        	logger.error("PUT /client/update/{} → Error: {}", userId, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @DeleteMapping("/client/delete/{userId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> deleteAuthentication(@PathVariable String userId) {
    	logger.info("DELETE /client/delete/{} → Deleting authentication credentials", userId);
        try {
            authenticationService.deleteAuthentication(userId);
            logger.info("DELETE /client/delete/{} → Deleted successfully", userId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Authentication credentials deleted successfully");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
        	logger.error("DELETE /client/delete/{} → Error: {}", userId, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllAuthentications() {
    	logger.info("GET /admin/all → Fetching all authentication records");
        try {
            var authentications = authenticationService.getAllAuthentications();
            logger.info("GET /admin/all → Retrieved {} records", authentications.size());
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "All authentication records retrieved successfully");
            result.put("data", authentications);
            result.put("count", authentications.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
        	logger.error("GET /admin/all → Error retrieving records: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve authentication records");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }


}