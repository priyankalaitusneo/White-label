package com.laitsneo.mipPay.serviceImpl.Client;

import com.laitsneo.mipPay.dto.Client.*;
import com.laitsneo.mipPay.entity.Client.Client;
import com.laitsneo.mipPay.entity.Client.PayinRecords;
import com.laitsneo.mipPay.entity.Client.PayoutRecords;
import com.laitsneo.mipPay.repository.Admin.LockedFundsRepository;
import com.laitsneo.mipPay.repository.Client.ClientRepository;
import com.laitsneo.mipPay.repository.Client.PayinRecordRepository;
import com.laitsneo.mipPay.repository.Client.PayoutRepository;
import com.laitsneo.mipPay.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WalletServiceImpl implements WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private LockedFundsRepository lockedFundsRepository;

    @Autowired
    private PayinRecordRepository payinRecordRepository;

    @Autowired
    private PayoutRepository payoutRepository;

    @Override
    public ResponseEntity<?> getAllMerchantsWalletSummary(String search) {
        logger.info("getAllMerchantsWalletSummary() → Request received with search: {}", search);

        try {
            // Fetch all clients or filtered by search
            List<Client> clients;
            
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim() + "%";
                logger.info("getAllMerchantsWalletSummary() → Applying search filter: {}", searchPattern);
                
                clients = clientRepository.findAll().stream()
                    .filter(c -> c.getUserId().toLowerCase().contains(search.toLowerCase()) ||
                                c.getName().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
                
                logger.info("getAllMerchantsWalletSummary() → Found {} merchants matching search", clients.size());
            } else {
                clients = clientRepository.findAll();
                logger.info("getAllMerchantsWalletSummary() → Fetched all {} merchants", clients.size());
            }

            if (clients.isEmpty()) {
                logger.warn("getAllMerchantsWalletSummary() → No merchants found");
                ResponseDto response = ResponseDto.builder()
                        .status("NO_CONTENT")
                        .message("SUCCESS")
                        .data("No merchants found")
                        .build();
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
            }

            // Build merchant wallet summary list
            List<MerchantWalletSummaryDto> summaryList = new ArrayList<>();

            for (Client client : clients) {
                try {
                    String merchantId = client.getUserId();
                    
                    // Get total fund (account balance)
                    BigDecimal totalFund = client.getAccountBal() != null ? 
                        client.getAccountBal() : BigDecimal.ZERO;

                    // Get locked amount for this merchant
                    BigDecimal lockedAmount = lockedFundsRepository.getTotalLockedAmountByUserId(merchantId);
                    if (lockedAmount == null) {
                        lockedAmount = BigDecimal.ZERO;
                    }

                    // Calculate available balance
                    BigDecimal available = totalFund.subtract(lockedAmount);

                    // Get payin count
                    Long payinCount = countPayinTransactions(merchantId);

                    // Get payout count
                    Long payoutCount = countPayoutTransactions(merchantId);

                    MerchantWalletSummaryDto summary = MerchantWalletSummaryDto.builder()
                            .merchantId(merchantId)
                            .merchantName(client.getName())
                            .totalFund(totalFund)
                            .available(available)
                            .locked(lockedAmount)
                            .payinCount(payinCount)
                            .payoutCount(payoutCount)
                            .build();

                    summaryList.add(summary);
                    
                    logger.debug("getAllMerchantsWalletSummary() → Processed merchant: {}, Total: {}, Locked: {}, Available: {}", 
                        merchantId, totalFund, lockedAmount, available);
                        
                } catch (Exception e) {
                    logger.error("getAllMerchantsWalletSummary() → Error processing merchant {}: {}", 
                        client.getUserId(), e.getMessage());
                    // Continue with next merchant
                }
            }

            logger.info("getAllMerchantsWalletSummary() → Successfully processed {} merchants", summaryList.size());

            ResponseDto response = ResponseDto.builder()
                    .status("OK")
                    .message("SUCCESS")
                    .data(summaryList)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("getAllMerchantsWalletSummary() → Exception occurred: {}", e.getMessage(), e);
            ResponseDto response = ResponseDto.builder()
                    .status("INTERNAL_SERVER_ERROR")
                    .message("ERROR")
                    .data("Failed to fetch merchant wallet summary: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    public ResponseEntity<?> getAggregateWalletSummary() {
        logger.info("getAggregateWalletSummary() → Request received");

        try {
            // Fetch all clients
            List<Client> allClients = clientRepository.findAll();
            
            if (allClients.isEmpty()) {
                logger.warn("getAggregateWalletSummary() → No clients found in system");
                
                AggregateWalletSummaryDto emptyDto = AggregateWalletSummaryDto.builder()
                        .totalBalance(BigDecimal.ZERO)
                        .availableBalance(BigDecimal.ZERO)
                        .lockedAmount(BigDecimal.ZERO)
                        .totalMerchants(0L)
                        .build();
                
                ResponseDto response = ResponseDto.builder()
                        .status("OK")
                        .message("SUCCESS")
                        .data(emptyDto)
                        .build();
                        
                return ResponseEntity.ok(response);
            }

            logger.info("getAggregateWalletSummary() → Processing {} merchants", allClients.size());

            // Calculate total balance (sum of all account_bal)
            BigDecimal totalBalance = allClients.stream()
                    .map(client -> client.getAccountBal() != null ? client.getAccountBal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            logger.info("getAggregateWalletSummary() → Total Balance calculated: {}", totalBalance);

            // Calculate total locked amount across all merchants
            BigDecimal totalLockedAmount = BigDecimal.ZERO;
            
            for (Client client : allClients) {
                BigDecimal merchantLocked = lockedFundsRepository.getTotalLockedAmountByUserId(client.getUserId());
                if (merchantLocked != null) {
                    totalLockedAmount = totalLockedAmount.add(merchantLocked);
                }
            }

            logger.info("getAggregateWalletSummary() → Total Locked Amount calculated: {}", totalLockedAmount);

            // Calculate available balance (total - locked)
            BigDecimal availableBalance = totalBalance.subtract(totalLockedAmount);

            logger.info("getAggregateWalletSummary() → Available Balance calculated: {}", availableBalance);

            // Build aggregate summary
            AggregateWalletSummaryDto aggregateDto = AggregateWalletSummaryDto.builder()
                    .totalBalance(totalBalance)
                    .availableBalance(availableBalance)
                    .lockedAmount(totalLockedAmount)
                    .totalMerchants((long) allClients.size())
                    .build();

            logger.info("getAggregateWalletSummary() → Summary completed successfully");

            ResponseDto response = ResponseDto.builder()
                    .status("OK")
                    .message("SUCCESS")
                    .data(aggregateDto)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("getAggregateWalletSummary() → Exception occurred: {}", e.getMessage(), e);
            ResponseDto response = ResponseDto.builder()
                    .status("INTERNAL_SERVER_ERROR")
                    .message("ERROR")
                    .data("Failed to fetch aggregate wallet summary: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    public ResponseEntity<?> getMerchantWalletDetails(String merchantId) {
        logger.info("getMerchantWalletDetails() → Request received for merchantId: {}", merchantId);

        try {
            // Validate merchant exists
            Optional<Client> clientOpt = clientRepository.findByUserId(merchantId);
            
            if (clientOpt.isEmpty()) {
                logger.warn("getMerchantWalletDetails() → Merchant not found: {}", merchantId);
                ResponseDto response = ResponseDto.builder()
                        .status("NOT_FOUND")
                        .message("ERROR")
                        .data("Merchant not found with ID: " + merchantId)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Client client = clientOpt.get();
            logger.info("getMerchantWalletDetails() → Merchant found: {}", client.getName());

            // Get wallet details
            BigDecimal totalFund = client.getAccountBal() != null ? 
                client.getAccountBal() : BigDecimal.ZERO;

            BigDecimal lockedAmount = lockedFundsRepository.getTotalLockedAmountByUserId(merchantId);
            if (lockedAmount == null) {
                lockedAmount = BigDecimal.ZERO;
            }

            BigDecimal availableBalance = totalFund.subtract(lockedAmount);

            logger.info("getMerchantWalletDetails() → Wallet details - Total: {}, Locked: {}, Available: {}", 
                totalFund, lockedAmount, availableBalance);

            // Fetch transaction history from both tables
            List<WalletTransactionHistoryDto> transactionHistory = fetchCombinedTransactionHistory(merchantId);

            logger.info("getMerchantWalletDetails() → Fetched {} transactions", transactionHistory.size());

            // Build response
            WalletDetailResponseDTO detailDto = WalletDetailResponseDTO.builder()
                    .merchantId(merchantId)
                    .merchantName(client.getName())
                    .totalFund(totalFund)
                    .availableBalance(availableBalance)
                    .lockedAmount(lockedAmount)
                    .totalTransactions((long) transactionHistory.size())
                    .transactionHistory(transactionHistory)
                    .build();

            ResponseDto response = ResponseDto.builder()
                    .status("OK")
                    .message("SUCCESS")
                    .data(detailDto)
                    .build();

            logger.info("getMerchantWalletDetails() → Successfully completed for merchantId: {}", merchantId);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("getMerchantWalletDetails() → Exception occurred for merchantId {}: {}", 
                merchantId, e.getMessage(), e);
            ResponseDto response = ResponseDto.builder()
                    .status("INTERNAL_SERVER_ERROR")
                    .message("ERROR")
                    .data("Failed to fetch merchant wallet details: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== Helper Methods ==========

    private Long countPayinTransactions(String merchantId) {
        try {
            List<PayinRecords> payinList = payinRecordRepository.findAll().stream()
                    .filter(p -> merchantId.equals(p.getUserId()))
                    .collect(Collectors.toList());
            return (long) payinList.size();
        } catch (Exception e) {
            logger.error("countPayinTransactions() → Error for merchantId {}: {}", merchantId, e.getMessage());
            return 0L;
        }
    }

    private Long countPayoutTransactions(String merchantId) {
        try {
            List<PayoutRecords> payoutList = payoutRepository.findByClientId(merchantId);
            return payoutList != null ? (long) payoutList.size() : 0L;
        } catch (Exception e) {
            logger.error("countPayoutTransactions() → Error for merchantId {}: {}", merchantId, e.getMessage());
            return 0L;
        }
    }

    private List<WalletTransactionHistoryDto> fetchCombinedTransactionHistory(String merchantId) {
        logger.info("fetchCombinedTransactionHistory() → Fetching transactions for merchantId: {}", merchantId);

        List<WalletTransactionHistoryDto> combinedList = new ArrayList<>();

        try {
            // Fetch payin transactions
            List<PayinRecords> payinList = payinRecordRepository.findAll().stream()
                    .filter(p -> merchantId.equals(p.getUserId()))
                    .collect(Collectors.toList());

            logger.info("fetchCombinedTransactionHistory() → Found {} payin transactions", payinList.size());

            for (PayinRecords payin : payinList) {
                try {
                    WalletTransactionHistoryDto dto = WalletTransactionHistoryDto.builder()
                            .transactionId(payin.getTrxnid() != null ? payin.getTrxnid() : payin.getOrderId())
                            .type("PAYIN")
                            .amount(payin.getAmount() != null ? 
                                BigDecimal.valueOf(payin.getAmount()) : BigDecimal.ZERO)
                            .date(payin.getCreatedDate())
                            .status(payin.getStatus() != null ? payin.getStatus() : "UNKNOWN")
                            .build();
                    combinedList.add(dto);
                } catch (Exception e) {
                    logger.error("fetchCombinedTransactionHistory() → Error processing payin record: {}", 
                        e.getMessage());
                }
            }

            // Fetch payout transactions
            List<PayoutRecords> payoutList = payoutRepository.findByClientId(merchantId);
            
            logger.info("fetchCombinedTransactionHistory() → Found {} payout transactions", 
                payoutList != null ? payoutList.size() : 0);

            if (payoutList != null && !payoutList.isEmpty()) {
                for (PayoutRecords payout : payoutList) {
                    try {
                        WalletTransactionHistoryDto dto = WalletTransactionHistoryDto.builder()
                                .transactionId(payout.getOrderId())
                                .type("PAYOUT")
                                .amount(BigDecimal.valueOf(payout.getAmount()))
                                .date(payout.getCreatedDate())
                                .status(payout.getStatus() != null ? payout.getStatus() : "UNKNOWN")
                                .build();
                        combinedList.add(dto);
                    } catch (Exception e) {
                        logger.error("fetchCombinedTransactionHistory() → Error processing payout record: {}", 
                            e.getMessage());
                    }
                }
            }

            // Sort by date descending (newest first)
            combinedList.sort((t1, t2) -> {
                if (t1.getDate() == null && t2.getDate() == null) return 0;
                if (t1.getDate() == null) return 1;
                if (t2.getDate() == null) return -1;
                return t2.getDate().compareTo(t1.getDate());
            });

            logger.info("fetchCombinedTransactionHistory() → Total combined transactions: {}", combinedList.size());

            // If no transactions found, log it
            if (combinedList.isEmpty()) {
                logger.info("fetchCombinedTransactionHistory() → No transactions found for merchantId: {}", 
                    merchantId);
            }

        } catch (Exception e) {
            logger.error("fetchCombinedTransactionHistory() → Exception occurred: {}", e.getMessage(), e);
        }

        return combinedList;
    }
}