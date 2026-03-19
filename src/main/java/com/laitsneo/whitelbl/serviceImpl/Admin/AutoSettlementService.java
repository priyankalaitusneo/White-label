package com.laitsneo.whitelbl.serviceImpl.Admin;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.laitsneo.whitelbl.entity.Client.SettlementRecord;
import com.laitsneo.whitelbl.repository.Client.ClientRepository;
import com.laitsneo.whitelbl.repository.Client.PayinRecordRepository;
import com.laitsneo.whitelbl.repository.Client.SettlementRecordRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoSettlementService {
	
    private final PayinRecordRepository payinRepo;
    private final SettlementRecordRepository settlementRepo;
    private final ClientRepository clientRepo;
    
    @Scheduled(cron = "0 0 20 * * *") // Runs daily at 8PM
    @Transactional
    public void runAutoSettlement() {
        
        log.info("============================================");
        log.info("STARTING AUTO SETTLEMENT PROCESS");
        log.info("============================================");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime today8PM = now.toLocalDate().atTime(20, 0);
            LocalDateTime fromDateTime = today8PM.minusDays(1);
            LocalDateTime toDateTime = today8PM;
            
            LocalDate fromDate = fromDateTime.toLocalDate();
            LocalDate toDate = toDateTime.toLocalDate();
            
            Timestamp fromTimestamp = Timestamp.valueOf(fromDateTime);
            Timestamp toTimestamp = Timestamp.valueOf(toDateTime);
            
            log.info("Settlement Window:");
            log.info("  FROM: {} ({})", fromDateTime, fromTimestamp);
            log.info("  TO:   {} ({})", toDateTime, toTimestamp);
            log.info("--------------------------------------------");
            
            List<String> userIds = payinRepo.findDistinctUserIds(fromTimestamp, toTimestamp);
            
            if (userIds == null || userIds.isEmpty()) {
                log.warn("No users found with transactions in settlement window");
                log.info("SETTLEMENT COMPLETED - No records to settle");
                return;
            }
            
            log.info("Found {} unique users with transactions", userIds.size());
            log.info("Users: {}", userIds);
            log.info("============================================");
            
            int totalSettled = 0;
            int totalSkipped = 0;
            int totalFailed = 0;
            
            for (String userId : userIds) {
                try {
                    boolean settled = processUserSettlement(userId, fromTimestamp, toTimestamp, fromDate, toDate);
                    if (settled) {
                        totalSettled++;
                    } else {
                        totalSkipped++;
                    }
                } catch (Exception e) {
                    totalFailed++;
                    log.error("Failed to settle for user: {}. Error: {}", userId, e.getMessage(), e);
                }
            }
            
            log.info("============================================");
            log.info("SETTLEMENT SUMMARY:");
            log.info("  Total Users Processed: {}", userIds.size());
            log.info("  Successfully Settled:  {}", totalSettled);
            log.info("  Skipped (No Amount):   {}", totalSkipped);
            log.info("  Failed:                {}", totalFailed);
            log.info("============================================");
            log.info("AUTO SETTLEMENT PROCESS COMPLETED");
            log.info("============================================");
            
        } catch (Exception e) {
            log.error("CRITICAL ERROR in Auto Settlement Process: {}", e.getMessage(), e);
            throw new RuntimeException("Auto Settlement Failed: " + e.getMessage(), e);
        }
    }
    
    private boolean processUserSettlement(
            String userId, 
            Timestamp fromTimestamp, 
            Timestamp toTimestamp,
            LocalDate fromDate,
            LocalDate toDate) {
        
        log.info("Processing settlement for User: {}", userId);
        
        try {
            // Check duplicate
            boolean duplicateExists = settlementRepo.existsByUserIdAndFromDateAndToDate(userId, fromDate, toDate);
            if (duplicateExists) {
                log.warn("  ⚠ DUPLICATE: Settlement already exists for user {} from {} to {}", 
                    userId, fromDate, toDate);
                return false;
            }
            
            // Calculate amount
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fromString = fromTimestamp.toLocalDateTime().format(formatter);
            String toString = toTimestamp.toLocalDateTime().format(formatter);
            
            Double unsettledAmount = payinRepo.getTotalUnsettledAmount(userId, fromString, toString);
            log.info("  Total Unsettled Amount: {}", unsettledAmount);
            
            if (unsettledAmount == null || unsettledAmount <= 0) {
                log.info("  ⏭ SKIPPED: No unsettled amount for user {}", userId);
                return false;
            }
            
            LocalDateTime now = LocalDateTime.now();
            
            SettlementRecord settlementRecord = new SettlementRecord();
            settlementRecord.setUserId(userId);
            settlementRecord.setSettlementAmount(unsettledAmount);
            settlementRecord.setTotalUnsettledAmount(unsettledAmount);
            settlementRecord.setFromDate(fromDate);
            settlementRecord.setToDate(toDate);
            settlementRecord.setSettlementMethod("BANK");
            settlementRecord.setInitiatedDate(now);
            settlementRecord.setScheduledSettlementDate(now);
            settlementRecord.setActualSettlementDate(now);
            settlementRecord.setStatus("SETTLED");
            settlementRecord.setSettlementStatus("COMPLETED");
                        SettlementRecord savedRecord = settlementRepo.save(settlementRecord);
            log.info("Settlement Record Created: ID={}, Amount={}, Status={}", 
                savedRecord.getSettlementId(), unsettledAmount, savedRecord.getStatus());
            
            int updatedRows = payinRepo.markSettled(userId, fromString, toString);
            log.info("Updated {} payin records to SETTLED status", updatedRows);
            
            if (updatedRows == 0) {
                log.warn("WARNING: No payin records were updated for user {}", userId);
            }
            
            log.info("SUCCESS: Settlement completed for user {} - Amount: {}", userId, unsettledAmount);
            return true;
            
        } catch (Exception e) {
            log.error("ERROR: Failed to process settlement for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Settlement processing failed for user: " + userId, e);
        }
    }
}