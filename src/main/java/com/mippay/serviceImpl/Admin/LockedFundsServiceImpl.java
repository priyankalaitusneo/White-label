package com.mippay.serviceImpl.Admin;

import com.mippay.dto.Admin.LockedFundsReportDTO;
import com.mippay.dto.Admin.LockedFundsRequestDto;
import com.mippay.dto.Admin.LockedFundsResponseDto;

import com.mippay.dto.Client.ResponseDto;

import com.mippay.entity.Admin.LockedFunds;
import com.mippay.entity.Client.Client;

import com.mippay.repository.Admin.LockedFundsRepository;
import com.mippay.repository.Client.ClientRepository;

import com.mippay.service.LockedFundsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LockedFundsServiceImpl implements LockedFundsService {

    private static final Logger logger = LoggerFactory.getLogger(LockedFundsServiceImpl.class);

    @Autowired
    private LockedFundsRepository lockedFundsRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Override
    @Transactional
    public ResponseEntity<?> lockFunds(LockedFundsRequestDto requestDto) {
        logger.info("lockFunds() → Request received for userId: {}, amount: {}", 
                   requestDto.getUserId(), requestDto.getAmountLocked());

        try {
            // Step 1: Validate client exists
            Optional<Client> clientOpt = clientRepository.findByUserId(requestDto.getUserId());
            if (clientOpt.isEmpty()) {
                logger.warn("lockFunds() → Client not found for userId: {}", requestDto.getUserId());
                ResponseDto response = ResponseDto.builder()
                        .status("NOT_FOUND")
                        .message("ERROR")
                        .data("Client not found with userId: " + requestDto.getUserId())
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Client client = clientOpt.get();
            BigDecimal currentBalance = client.getAccountBal();
            BigDecimal amountToLock = requestDto.getAmountLocked();

            logger.info("lockFunds() → Current balance for userId {}: {}", 
                       requestDto.getUserId(), currentBalance);

            // Step 2: Validate sufficient balance
            // Check against current balance to ensure user has enough funds
            if (currentBalance.compareTo(amountToLock) < 0) {
                logger.warn("lockFunds() → Insufficient balance. Available: {}, Required: {}", 
                           currentBalance, amountToLock);
                ResponseDto response = ResponseDto.builder()
                        .status("BAD_REQUEST")
                        .message("ERROR")
                        .data("Insufficient balance to lock funds. Available: " + currentBalance + 
                              ", Required: " + amountToLock)
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            //  DO NOT deduct from account_bal
            // account_bal remains unchanged - shows original balance
            // Locked amount is tracked ONLY in locked_funds table
            
            logger.info("lockFunds() → Balance remains unchanged at {} (locked amount tracked separately)", 
                       currentBalance);

            // Step 3: Create locked funds record
            LockedFunds lockedFunds = new LockedFunds();
            lockedFunds.setUserId(requestDto.getUserId());
            lockedFunds.setMerchantName(client.getName());
            lockedFunds.setAmountLocked(amountToLock);
            lockedFunds.setReason(requestDto.getReason());
            lockedFunds.setLockedDate(LocalDateTime.now());

            LockedFunds savedRecord = lockedFundsRepository.save(lockedFunds);

            logger.info("lockFunds() → Locked funds record created with ID: {}", savedRecord.getId());

            // Step 4: Prepare response
            // currentBalance shows original balance (unchanged)
            LockedFundsResponseDto responseDto = buildResponseDto(savedRecord, currentBalance, client.getStatus());

            ResponseDto response = ResponseDto.builder()
                    .status("OK")
                    .message("SUCCESS")
                    .data(responseDto)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("lockFunds() → Exception occurred: {}", e.getMessage(), e);
            ResponseDto response = ResponseDto.builder()
                    .status("INTERNAL_SERVER_ERROR")
                    .message("ERROR")
                    .data("Failed to lock funds: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateLockedFunds(Long lockId, LockedFundsRequestDto requestDto) {
        logger.info("updateLockedFunds() → Request received for lockId: {}, userId: {}, newAmount: {}", 
                   lockId, requestDto.getUserId(), requestDto.getAmountLocked());

        try {
            // Step 1: Validate locked funds record exists
            Optional<LockedFunds> lockedFundsOpt = lockedFundsRepository.findById(lockId);
            if (lockedFundsOpt.isEmpty()) {
                logger.warn("updateLockedFunds() → Locked funds record not found with ID: {}", lockId);
                ResponseDto response = ResponseDto.builder()
                        .status("NOT_FOUND")
                        .message("ERROR")
                        .data("Locked funds record not found with ID: " + lockId)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            LockedFunds lockedFunds = lockedFundsOpt.get();

            // Step 2: Validate userId matches
            if (!lockedFunds.getUserId().equals(requestDto.getUserId())) {
                logger.warn("updateLockedFunds() → UserId mismatch. Record userId: {}, Request userId: {}", 
                           lockedFunds.getUserId(), requestDto.getUserId());
                ResponseDto response = ResponseDto.builder()
                        .status("BAD_REQUEST")
                        .message("ERROR")
                        .data("UserId mismatch. Cannot update locked funds for different user.")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Step 3: Get client details
            Optional<Client> clientOpt = clientRepository.findByUserId(requestDto.getUserId());
            if (clientOpt.isEmpty()) {
                logger.error("updateLockedFunds() → Client not found for userId: {}", requestDto.getUserId());
                ResponseDto response = ResponseDto.builder()
                        .status("NOT_FOUND")
                        .message("ERROR")
                        .data("Client not found")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Client client = clientOpt.get();
            BigDecimal currentBalance = client.getAccountBal();
            BigDecimal oldLockedAmount = lockedFunds.getAmountLocked();
            BigDecimal newLockedAmount = requestDto.getAmountLocked();
            BigDecimal difference = newLockedAmount.subtract(oldLockedAmount);

            logger.info("updateLockedFunds() → Current balance: {}, Old locked: {}, New locked: {}, Difference: {}", 
                       currentBalance, oldLockedAmount, newLockedAmount, difference);

            //  DO NOT adjust account_bal
            // Only validate if increasing lock amount that balance is sufficient
            if (difference.compareTo(BigDecimal.ZERO) > 0) {
                // Increasing lock amount - validate sufficient balance exists
                if (currentBalance.compareTo(newLockedAmount) < 0) {
                    logger.warn("updateLockedFunds() → Insufficient balance. Current: {}, New locked amount required: {}", 
                               currentBalance, newLockedAmount);
                    ResponseDto response = ResponseDto.builder()
                            .status("BAD_REQUEST")
                            .message("ERROR")
                            .data("Insufficient balance to increase lock amount. Available: " + currentBalance + 
                                  ", New locked amount: " + newLockedAmount)
                            .build();
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                logger.info("updateLockedFunds() → Increasing locked amount. Balance remains unchanged at {}", 
                           currentBalance);

            } else if (difference.compareTo(BigDecimal.ZERO) < 0) {
                // Decreasing lock amount
                logger.info("updateLockedFunds() → Decreasing locked amount. Balance remains unchanged at {}", 
                           currentBalance);
            } else {
                // No change in amount, only reason update
                logger.info("updateLockedFunds() → No amount change, only updating reason");
            }

            // account_bal is NOT modified - remains as is
            // NO save to clientRepository needed

            // Step 4: Update locked funds record
            lockedFunds.setAmountLocked(newLockedAmount);
            lockedFunds.setReason(requestDto.getReason());
            lockedFunds.setMerchantName(client.getName());

            LockedFunds updatedRecord = lockedFundsRepository.save(lockedFunds);

            logger.info("updateLockedFunds() → Locked funds record updated successfully for lockId: {}", lockId);

            // Step 5: Prepare response
            // currentBalance shows original balance (unchanged)
            LockedFundsResponseDto responseDto = buildResponseDto(
                updatedRecord, 
                currentBalance,
                client.getStatus()
            );

            ResponseDto response = ResponseDto.builder()
                    .status("OK")
                    .message("SUCCESS")
                    .data(responseDto)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("updateLockedFunds() → Exception occurred: {}", e.getMessage(), e);
            ResponseDto response = ResponseDto.builder()
                    .status("INTERNAL_SERVER_ERROR")
                    .message("ERROR")
                    .data("Failed to update locked funds: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteLockedFunds(Long lockId) {
        logger.info("deleteLockedFunds() → Request received to delete lockId: {}", lockId);

        try {
            // Step 1: Validate locked funds record exists
            Optional<LockedFunds> lockedFundsOpt = lockedFundsRepository.findById(lockId);
            if (lockedFundsOpt.isEmpty()) {
                logger.warn("deleteLockedFunds() → Locked funds record not found with ID: {}", lockId);
                ResponseDto response = ResponseDto.builder()
                        .status("NOT_FOUND")
                        .message("ERROR")
                        .data("Locked funds record not found with ID: " + lockId)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            LockedFunds lockedFunds = lockedFundsOpt.get();
            String userId = lockedFunds.getUserId();
            BigDecimal lockedAmount = lockedFunds.getAmountLocked();

            logger.info("deleteLockedFunds() → Found record. UserId: {}, Locked amount: {}", 
                       userId, lockedAmount);

            // Step 2: Get client details (for response only)
            Optional<Client> clientOpt = clientRepository.findByUserId(userId);
            if (clientOpt.isEmpty()) {
                logger.error("deleteLockedFunds() → Client not found for userId: {}", userId);
                ResponseDto response = ResponseDto.builder()
                        .status("NOT_FOUND")
                        .message("ERROR")
                        .data("Client not found")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Client client = clientOpt.get();
            BigDecimal currentBalance = client.getAccountBal();

            // DO NOT refund to account_bal
            // Balance remains unchanged - just delete the locked funds record
            
            logger.info("deleteLockedFunds() → Balance remains unchanged at {} (no refund on delete)", 
                       currentBalance);

            // Step 3: Delete locked funds record (hard delete)
            lockedFundsRepository.deleteById(lockId);

            logger.info("deleteLockedFunds() → Locked funds record deleted successfully. LockId: {}", lockId);

            // Step 4: Prepare response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("lockId", lockId);
            responseData.put("userId", userId);
            responseData.put("deletedLockedAmount", lockedAmount);
            responseData.put("currentBalance", currentBalance); // Unchanged
            responseData.put("message", "Locked funds record deleted successfully");

            ResponseDto response = ResponseDto.builder()
                    .status("OK")
                    .message("SUCCESS")
                    .data(responseData)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("deleteLockedFunds() → Exception occurred: {}", e.getMessage(), e);
            ResponseDto response = ResponseDto.builder()
                    .status("INTERNAL_SERVER_ERROR")
                    .message("ERROR")
                    .data("Failed to delete locked funds: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    public ResponseEntity<?> getAllLockedFunds() {
        logger.info("getAllLockedFunds() → Fetching all locked funds records");

        try {
            List<Object[]> results = lockedFundsRepository.findAllLockedFundsWithClientDetails();

            if (results.isEmpty()) {
                logger.warn("getAllLockedFunds() → No locked funds records found");
                ResponseDto response = ResponseDto.builder()
                        .status("NO_CONTENT")
                        .message("SUCCESS")
                        .data("No locked funds records found")
                        .build();
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
            }

            List<LockedFundsResponseDto> responseDtos = results.stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());

            logger.info("getAllLockedFunds() → {} locked funds records found", responseDtos.size());

            ResponseDto response = ResponseDto.builder()
                    .status("OK")
                    .message("SUCCESS")
                    .data(responseDtos)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("getAllLockedFunds() → Exception occurred: {}", e.getMessage(), e);
            ResponseDto response = ResponseDto.builder()
                    .status("INTERNAL_SERVER_ERROR")
                    .message("ERROR")
                    .data("Failed to fetch locked funds: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    public ResponseEntity<?> getLockedFundsByUserId(String userId) {
        logger.info("getLockedFundsByUserId() → Fetching locked funds for userId: {}", userId);

        try {
            // Validate client exists
            Optional<Client> clientOpt = clientRepository.findByUserId(userId);
            if (clientOpt.isEmpty()) {
                logger.warn("getLockedFundsByUserId() → Client not found for userId: {}", userId);
                ResponseDto response = ResponseDto.builder()
                        .status("NOT_FOUND")
                        .message("ERROR")
                        .data("Client not found with userId: " + userId)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            List<LockedFunds> lockedFundsList = lockedFundsRepository.findByUserId(userId);

            if (lockedFundsList.isEmpty()) {
                logger.warn("getLockedFundsByUserId() → No locked funds found for userId: {}", userId);

                ResponseDto response = ResponseDto.builder()
                        .status("NOT_FOUND")
                        .message("ERROR")
                        .data("No locked funds found for this user")
                        .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Client client = clientOpt.get();
            BigDecimal currentBalance = client.getAccountBal();

            List<LockedFundsResponseDto> responseDtos = lockedFundsList.stream()
                    .map(lf -> buildResponseDto(lf, currentBalance, client.getStatus()))
                    .collect(Collectors.toList());

            // Calculate total locked amount
            BigDecimal totalLocked = lockedFundsList.stream()
                    .map(LockedFunds::getAmountLocked)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userId", userId);
            responseData.put("merchantName", client.getName());
            responseData.put("currentBalance", currentBalance);
            responseData.put("totalLockedAmount", totalLocked);
            responseData.put("lockedFundsCount", lockedFundsList.size());
            responseData.put("lockedFundsList", responseDtos);

            logger.info("getLockedFundsByUserId() → {} locked funds records found for userId: {}", 
                       lockedFundsList.size(), userId);

            ResponseDto response = ResponseDto.builder()
                    .status("OK")
                    .message("SUCCESS")
                    .data(responseData)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("getLockedFundsByUserId() → Exception occurred: {}", e.getMessage(), e);
            ResponseDto response = ResponseDto.builder()
                    .status("INTERNAL_SERVER_ERROR")
                    .message("ERROR")
                    .data("Failed to fetch locked funds: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Helper method to build response DTO
    private LockedFundsResponseDto buildResponseDto(LockedFunds lockedFunds, 
                                                     BigDecimal currentBalance, 
                                                     String merchantStatus) {
        return LockedFundsResponseDto.builder()
                .lockId(lockedFunds.getId())
                .userId(lockedFunds.getUserId())
                .merchantName(lockedFunds.getMerchantName())
                .amountLocked(lockedFunds.getAmountLocked())
                .reason(lockedFunds.getReason())
                .lockedDate(lockedFunds.getLockedDate())
                .createdDate(lockedFunds.getCreatedDate())
                .updatedDate(lockedFunds.getUpdatedDate())
                .currentBalance(currentBalance)
                .merchantStatus(merchantStatus)
                .build();
    }
    
    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return (LocalDateTime) value;
    }

    // Helper method to map query result to DTO
    private LockedFundsResponseDto mapToResponseDto(Object[] result) {
        return LockedFundsResponseDto.builder()
                .lockId(((Number) result[0]).longValue())
                .userId((String) result[1])
                .merchantName((String) result[2])
                .amountLocked((BigDecimal) result[3])
                .reason((String) result[4])
                .lockedDate(toLocalDateTime(result[5]))
                .createdDate(toLocalDateTime(result[6]))
                .updatedDate(toLocalDateTime(result[7]))
                .currentBalance((BigDecimal) result[8])
                .merchantStatus((String) result[9])
                .build();
    }

    public Map<String, Object> getLockedFundsReport(
            String merchantName,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> result =
                lockedFundsRepository.getLockedFundsReport(
                        merchantName, status, fromDate, toDate, pageable
                );

        List<LockedFundsReportDTO> data = result.getContent().stream().map(row -> {
            LockedFundsReportDTO dto = new LockedFundsReportDTO();
            dto.setLockId(((Number) row[0]).longValue());
            dto.setTransactionId((String) row[1]);
            dto.setUserId((String) row[2]);
            dto.setMerchantName((String) row[3]);
            dto.setAmount((BigDecimal) row[4]);
            dto.setReason((String) row[5]);
            dto.setHoldDate(((Timestamp) row[6]).toLocalDateTime());
            dto.setReleaseDate(row[7] != null
                    ? ((Timestamp) row[7]).toLocalDateTime()
                    : null);
            dto.setStatus((String) row[8]);
            return dto;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("records", data);
        response.put("currentPage", result.getNumber());
        response.put("pageSize", result.getSize());
        response.put("totalRecords", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());

        return response;
    }

}