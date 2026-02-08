//package com.mippay.serviceImpl.Client;
//
//import com.mippay.dto.Client.*;
//
//import com.mippay.entity.Client.Client;
//import com.mippay.entity.Client.PayinRecords;
//import com.mippay.entity.Client.SettlementRecord;
//
//import com.mippay.repository.Client.ClientRepository;
//import com.mippay.repository.Client.PayinRecordRepository;
//import com.mippay.repository.Client.SettlementRecordRepository;
//
//import com.mippay.service.SettlementService;
//
//import jakarta.transaction.Transactional;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//import java.util.stream.Collectors;
//
//
//@Service
//public class SettlementServiceImpl implements SettlementService {
//
//    private static final Logger logger = LoggerFactory.getLogger(SettlementServiceImpl.class);
//
//    @Autowired
//    private SettlementRecordRepository settlementRepository;
//
//    @Autowired
//    private PayinRecordRepository payinRepository;
//
//    @Autowired
//    private ClientRepository clientRepository;
//
//    // Unsettled Merchants List
//
//    @Override
//    public ResponseEntity<?> getAllUnsettledMerchants() {
//        try {
//            logger.info("Fetching all merchants with unsettled funds");
//
//            List<Object[]> results = settlementRepository.findAllMerchantsWithUnsettledFunds();
//
//            if (results.isEmpty()) {
//                logger.info("No merchants found with unsettled funds");
//                return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "No unsettled funds found",
//                    "data", Collections.emptyList()
//                ));
//            }
//
//            List<SettlementListResponseDTO> responseList = results.stream()
//                .map(result -> {
//                    String userId = (String) result[0];
//                    String name = (String) result[1];
//                    Double unsettledAmount = ((Number) result[2]).doubleValue();
//                    Long transactionCount = ((Number) result[3]).longValue();
//
//                    // Get client details
//                    Optional<Client> clientOpt = clientRepository.findByUserId(userId);
//
//                    return SettlementListResponseDTO.builder()
//                        .userId(userId)
//                        .merchantName(name)
//                        .email(clientOpt.map(Client::getEmail).orElse(null))
//                        .mobileNumber(clientOpt.map(Client::getMobileNum).orElse(null))
//                        .unsettledFund(unsettledAmount)
//                        .status("UNSETTLED")
//                        .totalTransactions(transactionCount)
//                        .accountStatus(clientOpt.map(Client::getStatus).orElse("UNKNOWN"))
//                        .build();
//                })
//                .collect(Collectors.toList());
//
//            logger.info("Found {} merchants with unsettled funds", responseList.size());
//
//            return ResponseEntity.ok(Map.of(
//                "success", true,
//                "message", "Unsettled merchants retrieved successfully",
//                "data", responseList,
//                "totalMerchants", responseList.size()
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error fetching unsettled merchants: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                "success", false,
//                "message", "Failed to fetch unsettled merchants",
//                "error", e.getMessage()
//            ));
//        }
//    }
//
//    @Override
//    public ResponseEntity<?> getUnsettledMerchantsByDateRange(LocalDate fromDate, LocalDate toDate) {
//        try {
//            logger.info("Fetching unsettled merchants for date range: {} to {}", fromDate, toDate);
//
//            // Validate date range
//            if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
//                logger.warn("Invalid date range: toDate {} is before fromDate {}", toDate, fromDate);
//                return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "To date must be greater than or equal to from date"
//                ));
//            }
//
//            List<Object[]> results = settlementRepository.findMerchantsWithUnsettledFundsByDateRange(fromDate, toDate);
//
//            if (results.isEmpty()) {
//                logger.info("No merchants found with unsettled funds in date range");
//                return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "No unsettled funds found for the specified date range",
//                    "data", Collections.emptyList(),
//                    "fromDate", fromDate,
//                    "toDate", toDate
//                ));
//            }
//
//            List<SettlementListResponseDTO> responseList = results.stream()
//                .map(result -> {
//                    String userId = (String) result[0];
//                    String name = (String) result[1];
//                    String email = (String) result[2];
//                    String mobile = (String) result[3];
//                    Double unsettledAmount = ((Number) result[4]).doubleValue();
//                    Long transactionCount = ((Number) result[5]).longValue();
//                    LocalDateTime lastTxnDate = (LocalDateTime) result[6];
//
//                    Optional<Client> clientOpt = clientRepository.findByUserId(userId);
//
//                    return SettlementListResponseDTO.builder()
//                        .userId(userId)
//                        .merchantName(name)
//                        .email(email)
//                        .mobileNumber(mobile)
//                        .unsettledFund(unsettledAmount)
//                        .status("UNSETTLED")
//                        .totalTransactions(transactionCount)
//                        .lastTransactionDate(lastTxnDate != null ? lastTxnDate.toLocalDate() : null)
//                        .accountStatus(clientOpt.map(Client::getStatus).orElse("UNKNOWN"))
//                        .fromDate(fromDate)
//                        .toDate(toDate)
//                        .build();
//                })
//                .collect(Collectors.toList());
//
//            logger.info("Found {} merchants with unsettled funds in date range", responseList.size());
//
//            return ResponseEntity.ok(Map.of(
//                "success", true,
//                "message", "Unsettled merchants retrieved successfully",
//                "data", responseList,
//                "totalMerchants", responseList.size(),
//                "fromDate", fromDate,
//                "toDate", toDate
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error fetching unsettled merchants by date range: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                "success", false,
//                "message", "Failed to fetch unsettled merchants",
//                "error", e.getMessage()
//            ));
//        }
//    }
//
//    @Override
//    public ResponseEntity<?> getMerchantUnsettledAmount(String userId, LocalDate fromDate, LocalDate toDate) {
//        try {
//            logger.info("Fetching unsettled amount for merchant: {} from {} to {}", userId, fromDate, toDate);
//
//            // Validate merchant exists
//            Optional<Client> clientOpt = clientRepository.findByUserId(userId);
//            if (clientOpt.isEmpty()) {
//                logger.warn("Merchant not found: {}", userId);
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
//                    "success", false,
//                    "message", "Merchant not found with userId: " + userId
//                ));
//            }
//
//            // Validate date range
//            if (fromDate.isAfter(toDate)) {
//                logger.warn("Invalid date range for merchant {}: toDate {} is before fromDate {}",
//                           userId, toDate, fromDate);
//                return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "To date must be greater than or equal to from date"
//                ));
//            }
//
//            Client client = clientOpt.get();
//            Double unsettledAmount = settlementRepository.calculateUnsettledAmount(userId, fromDate, toDate);
//
//            // Count unsettled transactions
//            List<PayinRecords> unsettledRecords = payinRepository.searchHistory(userId, fromDate, toDate, null, null)
//                .stream()
//                .filter(record -> "SUCCESS".equals(record.getStatus()) &&
//                                 "UNSETTLED".equals(record.getSettlementStatus()))
//                .collect(Collectors.toList());
//
//            logger.info("Merchant {} has ₹{} unsettled amount from {} transactions",
//                       userId, unsettledAmount, unsettledRecords.size());
//
//            SettlementListResponseDTO response = SettlementListResponseDTO.builder()
//                .userId(userId)
//                .merchantName(client.getName())
//                .email(client.getEmail())
//                .mobileNumber(client.getMobileNum())
//                .unsettledFund(unsettledAmount)
//                .status(unsettledAmount > 0 ? "UNSETTLED" : "SETTLED")
//                .totalTransactions((long) unsettledRecords.size())
//                .fromDate(fromDate)
//                .toDate(toDate)
//                .accountStatus(client.getStatus())
//                .build();
//
//            return ResponseEntity.ok(Map.of(
//                "success", true,
//                "message", "Unsettled amount retrieved successfully",
//                "data", response
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error fetching merchant unsettled amount: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                "success", false,
//                "message", "Failed to fetch merchant unsettled amount",
//                "error", e.getMessage()
//            ));
//        }
//    }
//
//    //  Settlement Initiation
//
//    @Override
//    @Transactional
//    public ResponseEntity<?> initiateSettlement(SettlementRequestDTO request) {
////        try {
////            logger.info("Initiating settlement for merchant: {} with amount: {} via method: {}",
////                       request.getUserId(), request.getSettlementAmount(), request.getSettlementMethod());
////
////            // Step 1: Validate merchant exists
////            Optional<Client> clientOpt = clientRepository.findByUserId(request.getUserId());
////            if (clientOpt.isEmpty()) {
////                logger.error("Merchant not found: {}", request.getUserId());
////                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
////                    "success", false,
////                    "message", "Merchant not found with userId: " + request.getUserId()
////                ));
////            }
////
////            Client client = clientOpt.get();
////
////            // Step 2: Validate date range
////            if (request.getFromDate().isAfter(request.getToDate())) {
////                logger.error("Invalid date range: fromDate {} is after toDate {}",
////                           request.getFromDate(), request.getToDate());
////                return ResponseEntity.badRequest().body(Map.of(
////                    "success", false,
////                    "message", "To date must be greater than or equal to from date"
////                ));
////            }
////
////            // Step 3: Calculate unsettled amount
////            Double unsettledAmount = settlementRepository.calculateUnsettledAmount(
////                request.getUserId(), request.getFromDate(), request.getToDate()
////            );
////
////            logger.info("Unsettled amount for merchant {}: ₹{}", request.getUserId(), unsettledAmount);
////
////            if (unsettledAmount == null || unsettledAmount <= 0) {
////                logger.warn("No unsettled amount found for merchant {} in date range", request.getUserId());
////                return ResponseEntity.badRequest().body(Map.of(
////                    "success", false,
////                    "message", "No unsettled amount found for the specified date range"
////                ));
////            }
////
////            // Step 4: Validate settlement amount
////            if (request.getSettlementAmount() > unsettledAmount) {
////                logger.error("Settlement amount ₹{} exceeds unsettled amount ₹{}",
////                           request.getSettlementAmount(), unsettledAmount);
////                return ResponseEntity.badRequest().body(Map.of(
////                    "success", false,
////                    "message", String.format("Settlement amount (₹%.2f) cannot exceed unsettled amount (₹%.2f)",
////                                           request.getSettlementAmount(), unsettledAmount),
////                    "unsettledAmount", unsettledAmount,
////                    "requestedAmount", request.getSettlementAmount()
////                ));
////            }
////
////            // Step 5: Check for overlapping settlements
////            Long overlappingCount = settlementRepository.checkOverlappingSettlements(
////                request.getUserId(), request.getFromDate(), request.getToDate()
////            );
////
////            if (overlappingCount > 0) {
////                logger.error("Found {} overlapping transactions already in settlement process", overlappingCount);
////                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
////                    "success", false,
////                    "message", "Some transactions in this date range are already being settled or have been settled",
////                    "overlappingTransactions", overlappingCount
////                ));
////            }
////
////            // Step 6: Validate bank details for BANK method
////            if ("BANK".equalsIgnoreCase(request.getSettlementMethod())) {
////                if (request.getBankDetails() == null) {
////                    logger.error("Bank details missing for BANK settlement method");
////                    return ResponseEntity.badRequest().body(Map.of(
////                        "success", false,
////                        "message", "Bank details are required for bank transfer settlement"
////                    ));
////                }
////
////                // Check UTR uniqueness
////                if (settlementRepository.existsByUtrNumber(request.getBankDetails().getUtrNumber())) {
////                    logger.error("UTR number already exists: {}", request.getBankDetails().getUtrNumber());
////                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
////                        "success", false,
////                        "message", "UTR number already exists. Please use a unique UTR number."
////                    ));
////                }
////            }
////
////            // Step 7: Create Settlement Record
////            SettlementRecord settlement = new SettlementRecord();
////            settlement.setUserId(request.getUserId());
////            settlement.setMerchantName(client.getName());
//////            settlement.setFromDate(request.getFromDate());
//////            settlement.setToDate(request.getToDate());
//////            settlement.setTotalUnsettledAmount(unsettledAmount);
////            settlement.setSettlementAmount(request.getSettlementAmount());
////            settlement.setSettlementMethod(request.getSettlementMethod().toUpperCase());
////            settlement.setRemarks(request.getRemarks());
////            settlement.setInitiatedBy(request.getInitiatedBy());
////
////            // Set amount based on method
////            if ("WALLET".equalsIgnoreCase(request.getSettlementMethod())) {
////                settlement.setWalletSettlementAmount(request.getSettlementAmount());
////                settlement.setBankSettlementAmount(0.0);
////                logger.info("Settlement will be credited to wallet");
////            } else {
////                settlement.setWalletSettlementAmount(0.0);
////                settlement.setBankSettlementAmount(request.getSettlementAmount());
////
////                // Set bank details
////                BankTransferDetailsDTO bankDetails = request.getBankDetails();
////                settlement.setUtrNumber(bankDetails.getUtrNumber());
////                settlement.setFromAccountHolder(bankDetails.getFromAccount().getAccountHolderName());
////                settlement.setFromAccountNumber(bankDetails.getFromAccount().getAccountNumber());
////                settlement.setFromBankName(bankDetails.getFromAccount().getBankName());
////                settlement.setFromIfscCode(bankDetails.getFromAccount().getIfscCode());
////                settlement.setToAccountHolder(bankDetails.getToAccount().getAccountHolderName());
////                settlement.setToAccountNumber(bankDetails.getToAccount().getAccountNumber());
////                settlement.setToBankName(bankDetails.getToAccount().getBankName());
////                settlement.setToIfscCode(bankDetails.getToAccount().getIfscCode());
////
////                logger.info("Settlement will be processed via bank transfer with UTR: {}",
////                           bankDetails.getUtrNumber());
////            }
////
////            // Step 8: Save settlement record
////            SettlementRecord savedSettlement = settlementRepository.save(settlement);
////            logger.info("Settlement record created with ID: {}", savedSettlement.getSettlementId());
////
////            // Step 9: Update PayinRecords settlement status to IN_PROGRESS
////            updatePayinRecordsStatus(request.getUserId(), request.getFromDate(), request.getToDate(),
////                                    "IN_PROGRESS", request.getSettlementAmount());
////
////            // Step 10: Prepare response
////            SettlementDetailsResponseDTO response = mapToDetailsResponse(savedSettlement, client);
////
////            logger.info("Settlement initiated successfully: {} - Scheduled for: {}",
////                       savedSettlement.getSettlementId(), savedSettlement.getScheduledSettlementDate());
////
////            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
////                "success", true,
////                "message", "Settlement initiated successfully. Will be processed on " +
////                          savedSettlement.getScheduledSettlementDate().toLocalDate(),
////                "data", response
////            ));
////
////        } catch (Exception e) {
////            logger.error("Error initiating settlement: {}", e.getMessage(), e);
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
////                "success", false,
////                "message", "Failed to initiate settlement",
////                "error", e.getMessage()
////            ));
////        }
//        return null;
//    }
//
//    /**
//     * Update PayinRecords settlement status (partial settlement logic)
//     */
//    @Transactional
//    private void updatePayinRecordsStatus(String userId, LocalDate fromDate, LocalDate toDate,
//                                         String status, Double settlementAmount) {
//        try {
//            logger.info("Updating PayinRecords status to {} for merchant: {}", status, userId);
//
//            List<PayinRecords> unsettledRecords = payinRepository.searchHistory(userId, fromDate, toDate, null, null)
//                .stream()
//                .filter(record -> "SUCCESS".equals(record.getStatus()) &&
//                                 "UNSETTLED".equals(record.getSettlementStatus()))
//                .sorted(Comparator.comparing(PayinRecords::getCreatedDate))
//                .collect(Collectors.toList());
//
//            double remainingAmount = settlementAmount;
//            int updatedCount = 0;
//
//            for (PayinRecords record : unsettledRecords) {
//                if (remainingAmount <= 0) break;
//
//                double recordAmount = record.getFinalAmount();
//
//                if (recordAmount <= remainingAmount) {
//                    // Fully settle this record
//                    record.setSettlementStatus(status);
//                    record.setUpdatedDate(LocalDateTime.now());
//                    payinRepository.save(record);
//                    remainingAmount -= recordAmount;
//                    updatedCount++;
//                    logger.debug("Fully marked record {} as {}", record.getOrderId(), status);
//                } else {
//                    // Partial settlement - mark as IN_PROGRESS
//                    record.setSettlementStatus(status);
//                    record.setUpdatedDate(LocalDateTime.now());
//                    payinRepository.save(record);
//                    updatedCount++;
//                    logger.debug("Partially marked record {} as {}", record.getOrderId(), status);
//                    break;
//                }
//            }
//
//            logger.info("Updated {} PayinRecords to status: {}", updatedCount, status);
//
//        } catch (Exception e) {
//            logger.error("Error updating PayinRecords status: {}", e.getMessage(), e);
//            throw e;
//        }
//    }
//
//    /**
//     * Map Settlement Record to Response DTO
//     */
////    private SettlementDetailsResponseDTO mapToDetailsResponse(SettlementRecord settlement, Client client) {
////        SettlementDetailsResponseDTO.BankAccountInfo fromAccount = null;
////        SettlementDetailsResponseDTO.BankAccountInfo toAccount = null;
////
////        if ("BANK".equals(settlement.getSettlementMethod())) {
////            fromAccount = SettlementDetailsResponseDTO.BankAccountInfo.builder()
////                .accountHolderName(settlement.getFromAccountHolder())
////                .accountNumber(settlement.getFromAccountNumber())
////                .bankName(settlement.getFromBankName())
////                .ifscCode(settlement.getFromIfscCode())
////                .build();
////
////            toAccount = SettlementDetailsResponseDTO.BankAccountInfo.builder()
////                .accountHolderName(settlement.getToAccountHolder())
////                .accountNumber(settlement.getToAccountNumber())
////                .bankName(settlement.getToBankName())
////                .ifscCode(settlement.getToIfscCode())
////                .build();
////        }
////
////        long remainingHours = ChronoUnit.HOURS.between(LocalDateTime.now(), settlement.getScheduledSettlementDate());
////
////        return SettlementDetailsResponseDTO.builder()
////            .settlementId(settlement.getSettlementId())
////            .userId(settlement.getUserId())
////            .merchantName(settlement.getMerchantName())
////            .merchantEmail(client.getEmail())
////            .merchantMobile(client.getMobileNum())
////            .fromDate(settlement.getFromDate())
////            .toDate(settlement.getToDate())
////            .totalUnsettledAmount(settlement.getTotalUnsettledAmount())
////            .settlementAmount(settlement.getSettlementAmount())
////            .walletSettlementAmount(settlement.getWalletSettlementAmount())
////            .bankSettlementAmount(settlement.getBankSettlementAmount())
////            .settlementMethod(settlement.getSettlementMethod())
////            .utrNumber(settlement.getUtrNumber())
////            .fromAccount(fromAccount)
////            .toAccount(toAccount)
////            .status(settlement.getStatus())
////            .settlementStatus(settlement.getSettlementStatus())
////            .initiatedDate(settlement.getInitiatedDate())
////            .scheduledSettlementDate(settlement.getScheduledSettlementDate())
////            .actualSettlementDate(settlement.getActualSettlementDate())
////            .createdDate(settlement.getCreatedDate())
////            .updatedDate(settlement.getUpdatedDate())
////            .initiatedBy(settlement.getInitiatedBy())
////            .remarks(settlement.getRemarks())
////            .failureReason(settlement.getFailureReason())
////            .isEditable(settlement.isEditable())
////            .remainingHours(remainingHours > 0 ? remainingHours : 0L)
////            .build();
////    }
//
//
//
//    //  Settlement Management
//
//    @Override
//    public ResponseEntity<?> getSettlementDetails(String settlementId) {
//        try {
//            logger.info("Fetching settlement details for ID: {}", settlementId);
//
//            Optional<SettlementRecord> settlementOpt = settlementRepository.findBySettlementId(settlementId);
//
//            if (settlementOpt.isEmpty()) {
//                logger.warn("Settlement not found: {}", settlementId);
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
//                    "success", false,
//                    "message", "Settlement not found with ID: " + settlementId
//                ));
//            }
//
//            SettlementRecord settlement = settlementOpt.get();
//            Optional<Client> clientOpt = clientRepository.findByUserId(settlement.getUserId());
//
//            if (clientOpt.isEmpty()) {
//                logger.error("Client not found for settlement: {}", settlement.getUserId());
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                    "success", false,
//                    "message", "Client data inconsistency"
//                ));
//            }
//
////            SettlementDetailsResponseDTO response = mapToDetailsResponse(settlement, clientOpt.get());
//
//            logger.info("Settlement details retrieved successfully: {}", settlementId);
//            return ResponseEntity.ok(Map.of(
//                "success", true,
//                "message", "Settlement details retrieved successfully",
////                "data", response
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error fetching settlement details: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                "success", false,
//                "message", "Failed to fetch settlement details",
//                "error", e.getMessage()
//            ));
//        }
//    }
//
//    @Override
//    @Transactional
//    public ResponseEntity<?> editSettlement(String settlementId, SettlementEditRequestDTO editRequest) {
//        try {
//            logger.info("Editing settlement: {}", settlementId);
//
//            // Step 1: Find settlement
//            Optional<SettlementRecord> settlementOpt = settlementRepository.findBySettlementId(settlementId);
//
//            if (settlementOpt.isEmpty()) {
//                logger.warn("Settlement not found: {}", settlementId);
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
//                    "success", false,
//                    "message", "Settlement not found with ID: " + settlementId
//                ));
//            }
//
//            SettlementRecord settlement = settlementOpt.get();
//
//            // Step 2: Check if editable
//            if (!settlement.isEditable()) {
//                logger.warn("Settlement {} is not editable. Status: {}", settlementId, settlement.getStatus());
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
//                    "success", false,
//                    "message", "Settlement cannot be edited. Current status: " + settlement.getStatus()
//                ));
//            }
//
//            // Step 3: Update date range if provided
//            if (editRequest.getFromDate() != null && editRequest.getToDate() != null) {
//                if (editRequest.getToDate().isBefore(editRequest.getFromDate())) {
//                    logger.error("Invalid date range in edit request");
//                    return ResponseEntity.badRequest().body(Map.of(
//                        "success", false,
//                        "message", "To date must be greater than or equal to from date"
//                    ));
//                }
//
//                // Recalculate unsettled amount for new date range
//                Double newUnsettledAmount = settlementRepository.calculateUnsettledAmount(
//                    settlement.getUserId(), editRequest.getFromDate(), editRequest.getToDate()
//                );
//
//                logger.info("New unsettled amount for updated date range: ₹{}", newUnsettledAmount);
//
//                // Check overlapping for new date range
//                Long overlappingCount = settlementRepository.checkOverlappingSettlements(
//                    settlement.getUserId(), editRequest.getFromDate(), editRequest.getToDate()
//                );
//
//                if (overlappingCount > 0) {
//                    logger.error("Found {} overlapping transactions in new date range", overlappingCount);
//                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
//                        "success", false,
//                        "message", "Some transactions in the new date range are already being settled",
//                        "overlappingTransactions", overlappingCount
//                    ));
//                }
//
//                // Revert old PayinRecords
//                updatePayinRecordsStatus(settlement.getUserId(), settlement.getFromDate(),
//                                       settlement.getToDate(), "UNSETTLED", settlement.getSettlementAmount());
//
//                // Update settlement dates
//                settlement.setFromDate(editRequest.getFromDate());
//                settlement.setToDate(editRequest.getToDate());
//                settlement.setTotalUnsettledAmount(newUnsettledAmount);
//
//                logger.info("Date range updated for settlement {}", settlementId);
//            }
//
//            // Step 4: Update settlement amount if provided
//            if (editRequest.getSettlementAmount() != null) {
//                Double currentUnsettled = settlement.getTotalUnsettledAmount();
//
//                if (editRequest.getSettlementAmount() > currentUnsettled) {
//                    logger.error("New settlement amount ₹{} exceeds unsettled amount ₹{}",
//                               editRequest.getSettlementAmount(), currentUnsettled);
//                    return ResponseEntity.badRequest().body(Map.of(
//                        "success", false,
//                        "message", String.format("Settlement amount (₹%.2f) cannot exceed unsettled amount (₹%.2f)",
//                                               editRequest.getSettlementAmount(), currentUnsettled),
//                        "unsettledAmount", currentUnsettled
//                    ));
//                }
//
//                // Update amounts based on method
//                if ("WALLET".equals(settlement.getSettlementMethod())) {
//                    settlement.setWalletSettlementAmount(editRequest.getSettlementAmount());
//                } else {
//                    settlement.setBankSettlementAmount(editRequest.getSettlementAmount());
//                }
//
//                settlement.setSettlementAmount(editRequest.getSettlementAmount());
//                logger.info("Settlement amount updated to ₹{}", editRequest.getSettlementAmount());
//            }
//
//            // Step 5: Update bank details if provided (for BANK method only)
//            if ("BANK".equals(settlement.getSettlementMethod()) && editRequest.getBankDetails() != null) {
//                BankTransferDetailsDTO bankDetails = editRequest.getBankDetails();
//
//                // Check new UTR uniqueness (if changed)
//                if (!settlement.getUtrNumber().equals(bankDetails.getUtrNumber())) {
//                    if (settlementRepository.existsByUtrNumber(bankDetails.getUtrNumber())) {
//                        logger.error("New UTR number already exists: {}", bankDetails.getUtrNumber());
//                        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
//                            "success", false,
//                            "message", "New UTR number already exists"
//                        ));
//                    }
//                    settlement.setUtrNumber(bankDetails.getUtrNumber());
//                }
//
//                // Update from account
//                if (bankDetails.getFromAccount() != null) {
//                    settlement.setFromAccountHolder(bankDetails.getFromAccount().getAccountHolderName());
//                    settlement.setFromAccountNumber(bankDetails.getFromAccount().getAccountNumber());
//                    settlement.setFromBankName(bankDetails.getFromAccount().getBankName());
//                    settlement.setFromIfscCode(bankDetails.getFromAccount().getIfscCode());
//                }
//
//                // Update to account
//                if (bankDetails.getToAccount() != null) {
//                    settlement.setToAccountHolder(bankDetails.getToAccount().getAccountHolderName());
//                    settlement.setToAccountNumber(bankDetails.getToAccount().getAccountNumber());
//                    settlement.setToBankName(bankDetails.getToAccount().getBankName());
//                    settlement.setToIfscCode(bankDetails.getToAccount().getIfscCode());
//                }
//
//                logger.info("Bank details updated for settlement {}", settlementId);
//            }
//
//            // Step 6: Update remarks if provided
//            if (editRequest.getRemarks() != null) {
//                settlement.setRemarks(editRequest.getRemarks());
//            }
//
//            // Step 7: Save updated settlement
//            settlement.setUpdatedDate(LocalDateTime.now());
//            SettlementRecord updatedSettlement = settlementRepository.save(settlement);
//
//            // Step 8: Update PayinRecords with new amount/dates
//            updatePayinRecordsStatus(settlement.getUserId(), settlement.getFromDate(),
//                                   settlement.getToDate(), "IN_PROGRESS", settlement.getSettlementAmount());
//
//            // Step 9: Prepare response
//            Optional<Client> clientOpt = clientRepository.findByUserId(settlement.getUserId());
//            SettlementDetailsResponseDTO response = mapToDetailsResponse(updatedSettlement, clientOpt.get());
//
//            logger.info("Settlement {} updated successfully", settlementId);
//
//            return ResponseEntity.ok(Map.of(
//                "success", true,
//                "message", "Settlement updated successfully",
//                "data", response
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error editing settlement: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                "success", false,
//                "message", "Failed to edit settlement",
//                "error", e.getMessage()
//            ));
//        }
//    }
//
//    @Override
//    public ResponseEntity<?> getSettlementHistory(SettlementHistoryFilterDTO filterDTO) {
//        try {
//            logger.info("Fetching settlement history with filters: {}", filterDTO);
//
//            // Create pageable
//            Sort.Direction direction = "ASC".equalsIgnoreCase(filterDTO.getSortDirection())
//                                      ? Sort.Direction.ASC : Sort.Direction.DESC;
//            Pageable pageable = PageRequest.of(
//                filterDTO.getPage(),
//                filterDTO.getSize(),
//                Sort.by(direction, filterDTO.getSortBy())
//            );
//
//            // Fetch with filters
//            Page<SettlementRecord> settlementPage = settlementRepository.findSettlementHistory(
//                filterDTO.getUserId(),
//                filterDTO.getSettlementId(),
//                filterDTO.getStatus(),
//                filterDTO.getSettlementStatus(),
//                filterDTO.getSettlementMethod(),
//                filterDTO.getFromDate(),
//                filterDTO.getToDate(),
//                filterDTO.getMinAmount(),
//                filterDTO.getMaxAmount(),
//                pageable
//            );
//
//            // Convert to DTOs
//            List<SettlementDetailsResponseDTO> responseList = settlementPage.getContent().stream()
//                .map(settlement -> {
//                    Optional<Client> clientOpt = clientRepository.findByUserId(settlement.getUserId());
//                    return mapToDetailsResponse(settlement, clientOpt.orElse(null));
//                })
//                .collect(Collectors.toList());
//
//            logger.info("Found {} settlements in history", settlementPage.getTotalElements());
//
//            return ResponseEntity.ok(Map.of(
//                "success", true,
//                "message", "Settlement history retrieved successfully",
//                "data", responseList,
//                "page", settlementPage.getNumber(),
//                "size", settlementPage.getSize(),
//                "totalElements", settlementPage.getTotalElements(),
//                "totalPages", settlementPage.getTotalPages()
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error fetching settlement history: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                "success", false,
//                "message", "Failed to fetch settlement history",
//                "error", e.getMessage()
//            ));
//        }
//    }
//
//    @Override
//    public ResponseEntity<?> getSettlementsByMerchant(String userId) {
//        try {
//            logger.info("Fetching all settlements for merchant: {}", userId);
//
//            List<SettlementRecord> settlements = settlementRepository.findByUserId(userId);
//
//            if (settlements.isEmpty()) {
//                logger.info("No settlements found for merchant: {}", userId);
//                return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "No settlements found for this merchant",
//                    "data", Collections.emptyList()
//                ));
//            }
//
//            Optional<Client> clientOpt = clientRepository.findByUserId(userId);
//            Client client = clientOpt.orElse(null);
//
//            List<SettlementDetailsResponseDTO> responseList = settlements.stream()
//                .map(settlement -> mapToDetailsResponse(settlement, client))
//                .collect(Collectors.toList());
//
//            logger.info("Found {} settlements for merchant: {}", settlements.size(), userId);
//
//            return ResponseEntity.ok(Map.of(
//                "success", true,
//                "message", "Settlements retrieved successfully",
//                "data", responseList,
//                "totalSettlements", settlements.size()
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error fetching merchant settlements: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                "success", false,
//                "message", "Failed to fetch merchant settlements",
//                "error", e.getMessage()
//            ));
//        }
//    }
//
//    @Override
//    public ResponseEntity<?> getPendingSettlements() {
//        try {
//            logger.info("Fetching all pending settlements");
//
//            List<SettlementRecord> pendingSettlements = settlementRepository.findByStatus("IN_PROGRESS");
//
//            if (pendingSettlements.isEmpty()) {
//                logger.info("No pending settlements found");
//                return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "No pending settlements",
//                    "data", Collections.emptyList()
//                ));
//            }
//
//            List<SettlementDetailsResponseDTO> responseList = pendingSettlements.stream()
//                .map(settlement -> {
//                    Optional<Client> clientOpt = clientRepository.findByUserId(settlement.getUserId());
//                    return mapToDetailsResponse(settlement, clientOpt.orElse(null));
//                })
//                .collect(Collectors.toList());
//
//            logger.info("Found {} pending settlements", pendingSettlements.size());
//
//            return ResponseEntity.ok(Map.of(
//                "success", true,
//                "message", "Pending settlements retrieved successfully",
//                "data", responseList,
//                "totalPending", pendingSettlements.size()
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error fetching pending settlements: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                "success", false,
//                "message", "Failed to fetch pending settlements",
//                "error", e.getMessage()
//            ));
//        }
//    }
//
//    // ========== Scheduler Methods ==========
//
//    @Override
//    @Transactional
//    public void processReadySettlements() {
//        try {
//            logger.info("Processing ready settlements (T+1 reached)");
//
//            LocalDateTime currentTime = LocalDateTime.now();
//            List<SettlementRecord> readySettlements =
//                settlementRepository.findPendingSettlementsForProcessing(currentTime);
//
//            if (readySettlements.isEmpty()) {
//                logger.info("No settlements ready for processing");
//                return;
//            }
//
//            logger.info("Found {} settlements ready for processing", readySettlements.size());
//
//            for (SettlementRecord settlement : readySettlements) {
//                try {
//                    processIndividualSettlement(settlement);
//                } catch (Exception e) {
//                    logger.error("Failed to process settlement {}: {}",
//                               settlement.getSettlementId(), e.getMessage(), e);
//                    // Mark as failed
//                    settlement.setStatus("FAILED");
//                    settlement.setFailureReason(e.getMessage());
//                    settlement.setUpdatedDate(LocalDateTime.now());
//                    settlementRepository.save(settlement);
//                }
//            }
//
//            logger.info("Completed processing ready settlements");
//
//        } catch (Exception e) {
//            logger.error("Error in processReadySettlements: {}", e.getMessage(), e);
//        }
//    }
//
//    @Transactional
//    private void processIndividualSettlement(SettlementRecord settlement) {
//        logger.info("Processing settlement: {}", settlement.getSettlementId());
//
//        if ("WALLET".equals(settlement.getSettlementMethod())) {
//            // Update wallet balance
//            Optional<Client> clientOpt = clientRepository.findByUserId(settlement.getUserId());
//            if (clientOpt.isPresent()) {
//                Client client = clientOpt.get();
//
//                // Add settlement amount to wallet
//                BigDecimal currentBalance = client.getAccountBal() != null ? client.getAccountBal() : BigDecimal.ZERO;
//                BigDecimal newBalance = currentBalance.add(BigDecimal.valueOf(settlement.getSettlementAmount()));
//
//                client.setAccountBal(newBalance);
//                clientRepository.save(client);
//
//                logger.info("Updated wallet balance for merchant {}: ₹{} -> ₹{}",
//                           settlement.getUserId(), currentBalance, newBalance);
//            } else {
//                throw new RuntimeException("Client not found: " + settlement.getUserId());
//            }
//        } else {
//            // Bank transfer - already processed manually with UTR
//            logger.info("Bank transfer settlement: {}", settlement.getSettlementId());
//        }
//
//        // Update PayinRecords to SETTLED
//        updatePayinRecordsStatus(settlement.getUserId(), settlement.getFromDate(),
//                               settlement.getToDate(), "SETTLED", settlement.getSettlementAmount());
//
//        // Update settlement record
//        settlement.setStatus("SETTLED");
//        settlement.setSettlementStatus("COMPLETED");
//        settlement.setActualSettlementDate(LocalDateTime.now());
//        settlement.setUpdatedDate(LocalDateTime.now());
//        settlementRepository.save(settlement);
//
//        logger.info("Settlement {} processed successfully", settlement.getSettlementId());
//    }
//
//    // manual complete for testing
//    @Override
//    @Transactional
//    public ResponseEntity<?> completeSettlement(String settlementId) {
//        try {
//            logger.info("Manually completing settlement: {}", settlementId);
//
//            Optional<SettlementRecord> settlementOpt = settlementRepository.findBySettlementId(settlementId);
//
//            if (settlementOpt.isEmpty()) {
//                logger.warn("Settlement not found: {}", settlementId);
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
//                    "success", false,
//                    "message", "Settlement not found"
//                ));
//            }
//
//            SettlementRecord settlement = settlementOpt.get();
//
//            if (!"IN_PROGRESS".equals(settlement.getStatus())) {
//                logger.warn("Settlement {} is not in IN_PROGRESS status", settlementId);
//                return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "Settlement is not in IN_PROGRESS status"
//                ));
//            }
//
//            processIndividualSettlement(settlement);
//
//            return ResponseEntity.ok(Map.of(
//                "success", true,
//                "message", "Settlement completed successfully"
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error completing settlement: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                "success", false,
//                "message", "Failed to complete settlement",
//                "error", e.getMessage()
//            ));
//        }
//    }
//
//    // ========== Statistics & Validation ==========
//
//    @Override
//    public ResponseEntity<?> getSettlementStatistics() {
//        try {
//            logger.info("Fetching settlement statistics");
//
//            List<Object[]> statusCounts = settlementRepository.getSettlementCountByStatus();
//            List<Object[]> methodSummary = settlementRepository.getSettlementSummary(
//                LocalDateTime.now().minusDays(30)
//            );
//
//            Map<String, Object> stats = new HashMap<>();
//
//            // Status counts
//            Map<String, Long> statusMap = new HashMap<>();
//            for (Object[] row : statusCounts) {
//                statusMap.put((String) row[0], ((Number) row[1]).longValue());
//            }
//            stats.put("statusCounts", statusMap);
//
//            // Method summary (last 30 days)
//            Map<String, Map<String, Object>> methodMap = new HashMap<>();
//            for (Object[] row : methodSummary) {
//                String method = (String) row[0];
//                Long count = ((Number) row[1]).longValue();
//                Double amount = ((Number) row[2]).doubleValue();
//
//                Map<String, Object> methodData = new HashMap<>();
//                methodData.put("count", count);
//                methodData.put("totalAmount", amount);
//                methodMap.put(method, methodData);
//            }
//            stats.put("last30Days", methodMap);
//
//            logger.info("Settlement statistics retrieved successfully");
//
//            return ResponseEntity.ok(Map.of(
//                "success", true,
//                "message", "Statistics retrieved successfully",
//                "data", stats
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error fetching statistics: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                "success", false,
//                "message", "Failed to fetch statistics",
//                "error", e.getMessage()
//            ));
//        }
//    }
//
//    @Override
//    public ResponseEntity<?> getMerchantSettlementSummary(String userId) {
//        try {
//            logger.info("Fetching settlement summary for merchant: {}", userId);
//
//            Double totalSettled = settlementRepository.getTotalSettledAmount(userId);
//            List<SettlementRecord> allSettlements = settlementRepository.findByUserId(userId);
//
//            Map<String, Object> summary = new HashMap<>();
//            summary.put("totalSettledAmount", totalSettled != null ? totalSettled : 0.0);
//            summary.put("totalSettlements", allSettlements.size());
//            summary.put("pendingSettlements", allSettlements.stream()
//                .filter(s -> "IN_PROGRESS".equals(s.getStatus())).count());
//            summary.put("completedSettlements", allSettlements.stream()
//                .filter(s -> "SETTLED".equals(s.getStatus())).count());
//
//            return ResponseEntity.ok(Map.of(
//                "success", true,
//                "message", "Merchant summary retrieved successfully",
//                "data", summary
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error fetching merchant summary: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                "success", false,
//                "message", "Failed to fetch merchant summary",
//                "error", e.getMessage()
//            ));
//        }
//    }
//
//    @Override
//    public ResponseEntity<?> validateSettlement(String userId, LocalDate fromDate,
//                                               LocalDate toDate, Double amount) {
//        try {
//            logger.info("Validating settlement for merchant: {}", userId);
//
//            Map<String, Object> validation = new HashMap<>();
//            validation.put("valid", true);
//            List<String> errors = new ArrayList<>();
//
//            // Check merchant
//            if (!clientRepository.findByUserId(userId).isPresent()) {
//                errors.add("Merchant not found");
//                validation.put("valid", false);
//            }
//
//            // Check date range
//            if (toDate.isBefore(fromDate)) {
//                errors.add("Invalid date range");
//                validation.put("valid", false);
//            }
//
//            // Check unsettled amount
//            Double unsettled = settlementRepository.calculateUnsettledAmount(userId, fromDate, toDate);
//            if (amount > unsettled) {
//                errors.add("Amount exceeds unsettled fund");
//                validation.put("valid", false);
//            }
//
//            // Check overlapping
//            Long overlapping = settlementRepository.checkOverlappingSettlements(userId, fromDate, toDate);
//            if (overlapping > 0) {
//                errors.add("Overlapping settlements found");
//                validation.put("valid", false);
//            }
//
//            validation.put("errors", errors);
//            validation.put("unsettledAmount", unsettled);
//            validation.put("overlappingCount", overlapping);
//
//            return ResponseEntity.ok(Map.of(
//                "success", true,
//                "data", validation
//            ));
//
//        } catch (Exception e) {
//            logger.error("Error validating settlement: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                "success", false,
//                "message", "Validation failed",
//                "error", e.getMessage()
//            ));
//        }
//    }
//
//}