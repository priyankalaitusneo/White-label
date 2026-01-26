package com.mippay.serviceImpl.Admin;

import com.mippay.dto.Admin.SwitchingDTO;

import com.mippay.entity.Admin.Switching;
import com.mippay.entity.Admin.Vendors;
import com.mippay.entity.Client.Client;

import com.mippay.repository.Admin.SwitchingRepository;
import com.mippay.repository.Admin.VendorsRepository;
import com.mippay.repository.Client.ClientRepository;

import com.mippay.service.SwitchingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SwitchingServiceImpl implements SwitchingService {

    Logger logger = LoggerFactory.getLogger(SwitchingServiceImpl.class);

    @Autowired
    private SwitchingRepository switchingRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private VendorsRepository vendorsRepository;

    // ==================== PAYIN SWITCHING ====================

    @Override
    @Transactional
    public Map<String, Object> switchAllPayinMerchants(SwitchingDTO request) {
        logger.info("switchAllPayinMerchants() → Started for vendorName: {}", request.getVendorName());

        // 1. Validate vendor exists
        Vendors vendor = vendorsRepository.findByVendorName(request.getVendorName())
                .orElseThrow(() -> {
                    logger.error("Vendor not found: {}", request.getVendorName());
                    return new RuntimeException("Vendor not found: " + request.getVendorName());
                });

        // 2. Check vendor status
        if (!"Active".equalsIgnoreCase(vendor.getStatus())) {
            logger.error("Vendor is not active: {}", request.getVendorName());
            throw new RuntimeException("Vendor is not active: " + request.getVendorName());
        }

        // 3. Get all PAYIN clients only
        List<Client> payinClients = clientRepository.findAll().stream()
                .filter(client -> "PAYIN".equalsIgnoreCase(client.getMerchantType()))
                .collect(Collectors.toList());

        if (payinClients.isEmpty()) {
            logger.error("No PAYIN merchants found in the system");
            throw new RuntimeException("No PAYIN merchants found in the system");
        }

        logger.info("Found {} PAYIN merchants to process", payinClients.size());

        // 4. Get current admin email
        String adminEmail = getCurrentAdminEmail();

        // 5. Check each merchant and create logs only for those not already on this vendor
        List<Switching> switchingLogs = new ArrayList<>();
        List<Map<String, String>> skippedMerchants = new ArrayList<>();

        for (Client client : payinClients) {
            // Check if merchant is already using this vendor
            Switching latestLog = switchingRepository.findLatestByMerchantIdAndType(
                    client.getUserId(), "PAYIN");

            if (latestLog != null && vendor.getVendorName().equalsIgnoreCase(latestLog.getSwitchedPipe())) {
                // Merchant already on this vendor - SKIP
                Map<String, String> skippedInfo = new HashMap<>();
                skippedInfo.put("merchantId", client.getUserId());
                skippedInfo.put("merchantName", client.getName());
                skippedInfo.put("reason", "Already using " + vendor.getVendorName());
                skippedMerchants.add(skippedInfo);
                logger.info("Skipping merchant {} - already using {}", 
                          client.getUserId(), vendor.getVendorName());
            } else {
                // Create new switching log
                Switching log = new Switching(
                        client.getUserId(),
                        client.getName(),
                        "PAYIN",
                        vendor.getVendorName(),
                        adminEmail
                );
                switchingLogs.add(log);
            }
        }

        // 6. Check if all merchants were skipped
        if (switchingLogs.isEmpty()) {
            logger.warn("All PAYIN merchants are already using {}", vendor.getVendorName());
            throw new RuntimeException("All PAYIN merchants are already using " + vendor.getVendorName() + 
                                     ". No switching needed.");
        }

        // 7. Save switching logs for merchants that need switching
        switchingRepository.saveAll(switchingLogs);
        logger.info("Switched {} PAYIN merchants to vendor: {}", switchingLogs.size(), vendor.getVendorName());

        // 8. Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Switching completed for PAYIN merchants");
        response.put("vendorName", vendor.getVendorName());
        response.put("merchantType", "PAYIN");
        response.put("totalMerchantsSwitched", switchingLogs.size());
        response.put("totalMerchantsSkipped", skippedMerchants.size());

        if (!skippedMerchants.isEmpty()) {
            response.put("skippedMerchants", skippedMerchants);
            response.put("info", skippedMerchants.size() + " merchant(s) were already using " + 
                               vendor.getVendorName());
        }

        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> switchSelectedPayinMerchants(String vendorName, List<String> merchantIds) {
        logger.info("switchSelectedPayinMerchants() → Started for vendorName: {} with {} merchants",
                vendorName, merchantIds.size());

        // 1. Validate input
        if (merchantIds == null || merchantIds.isEmpty()) {
            logger.error("Merchant IDs list is empty");
            throw new RuntimeException("Merchant IDs list cannot be empty");
        }

        // 2. Validate vendor exists
        Vendors vendor = vendorsRepository.findByVendorName(vendorName)
                .orElseThrow(() -> {
                    logger.error("Vendor not found: {}", vendorName);
                    return new RuntimeException("Vendor not found: " + vendorName);
                });

        // 3. Check vendor status
        if (!"Active".equalsIgnoreCase(vendor.getStatus())) {
            logger.error("Vendor is not active: {}", vendorName);
            throw new RuntimeException("Vendor is not active: " + vendorName);
        }

        // 4. Validate merchant IDs exist and are PAYIN type
        List<Client> selectedPayinClients = new ArrayList<>();
        List<String> notFoundMerchants = new ArrayList<>();
        List<String> wrongTypeMerchants = new ArrayList<>();

        for (String merchantId : merchantIds) {
            Optional<Client> clientOpt = clientRepository.findByUserId(merchantId);
            if (clientOpt.isPresent()) {
                Client client = clientOpt.get();
                if ("PAYIN".equalsIgnoreCase(client.getMerchantType())) {
                    selectedPayinClients.add(client);
                } else {
                    wrongTypeMerchants.add(merchantId);
                    logger.warn("Merchant {} is not a PAYIN merchant (type: {})", 
                              merchantId, client.getMerchantType());
                }
            } else {
                notFoundMerchants.add(merchantId);
            }
        }

        if (selectedPayinClients.isEmpty()) {
            logger.error("None of the provided merchant IDs are valid PAYIN merchants");
            throw new RuntimeException("None of the provided merchant IDs are valid PAYIN merchants");
        }

        // 5. Get current admin email
        String adminEmail = getCurrentAdminEmail();

        // 6. Check each merchant and create logs only for those not already on this vendor
        List<Switching> switchingLogs = new ArrayList<>();
        List<Map<String, String>> skippedMerchants = new ArrayList<>();

        for (Client client : selectedPayinClients) {
            // Check if merchant is already using this vendor
            Switching latestLog = switchingRepository.findLatestByMerchantIdAndType(
                    client.getUserId(), "PAYIN");

            if (latestLog != null && vendor.getVendorName().equalsIgnoreCase(latestLog.getSwitchedPipe())) {
                // Merchant already on this vendor - SKIP
                Map<String, String> skippedInfo = new HashMap<>();
                skippedInfo.put("merchantId", client.getUserId());
                skippedInfo.put("merchantName", client.getName());
                skippedInfo.put("reason", "Already using " + vendor.getVendorName());
                skippedMerchants.add(skippedInfo);
                logger.info("Skipping merchant {} - already using {}", 
                          client.getUserId(), vendor.getVendorName());
            } else {
                // Create new switching log
                Switching log = new Switching(
                        client.getUserId(),
                        client.getName(),
                        "PAYIN",
                        vendor.getVendorName(),
                        adminEmail
                );
                switchingLogs.add(log);
            }
        }

        // 7. Check if all valid merchants were skipped
        if (switchingLogs.isEmpty()) {
            logger.warn("All selected PAYIN merchants are already using {}", vendor.getVendorName());
            throw new RuntimeException("All selected PAYIN merchants are already using " + 
                                     vendor.getVendorName() + ". No switching needed.");
        }

        // 8. Save switching logs
        switchingRepository.saveAll(switchingLogs);
        logger.info("Switched {} PAYIN merchants to vendor: {}", switchingLogs.size(), vendor.getVendorName());

        // 9. Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Switching completed for selected PAYIN merchants");
        response.put("vendorName", vendor.getVendorName());
        response.put("merchantType", "PAYIN");
        response.put("totalMerchantsSwitched", switchingLogs.size());
        response.put("totalMerchantsSkipped", skippedMerchants.size());

        // Add warnings for invalid merchants
        if (!notFoundMerchants.isEmpty() || !wrongTypeMerchants.isEmpty() || !skippedMerchants.isEmpty()) {
            List<String> warnings = new ArrayList<>();
            
            if (!notFoundMerchants.isEmpty()) {
                warnings.add(notFoundMerchants.size() + " merchant(s) not found");
                response.put("notFoundMerchants", notFoundMerchants);
            }
            if (!wrongTypeMerchants.isEmpty()) {
                warnings.add(wrongTypeMerchants.size() + " merchant(s) are not PAYIN type");
                response.put("wrongTypeMerchants", wrongTypeMerchants);
            }
            if (!skippedMerchants.isEmpty()) {
                warnings.add(skippedMerchants.size() + " merchant(s) already using " + vendor.getVendorName());
                response.put("skippedMerchants", skippedMerchants);
            }
            
            response.put("warning", String.join(", ", warnings));
        }

        return response;
    }

    @Override
    public List<Switching> getPayinSwitchingLogs() {
        logger.info("getPayinSwitchingLogs() → Fetching all PAYIN switching logs");
        List<Switching> logs = switchingRepository.findPayinLogsOrderByDateTimeDesc();
        logger.info("Found {} PAYIN switching log records", logs.size());
        return logs;
    }

    // ==================== PAYOUT SWITCHING ====================

    @Override
    @Transactional
    public Map<String, Object> switchAllPayoutMerchants(SwitchingDTO request) {
        logger.info("switchAllPayoutMerchants() → Started for vendorName: {}", request.getVendorName());

        // 1. Validate vendor exists
        Vendors vendor = vendorsRepository.findByVendorName(request.getVendorName())
                .orElseThrow(() -> {
                    logger.error("Vendor not found: {}", request.getVendorName());
                    return new RuntimeException("Vendor not found: " + request.getVendorName());
                });

        // 2. Check vendor status
        if (!"Active".equalsIgnoreCase(vendor.getStatus())) {
            logger.error("Vendor is not active: {}", request.getVendorName());
            throw new RuntimeException("Vendor is not active: " + request.getVendorName());
        }

        // 3. Get all PAYOUT clients only
        List<Client> payoutClients = clientRepository.findAll().stream()
                .filter(client -> "PAYOUT".equalsIgnoreCase(client.getMerchantType()))
                .collect(Collectors.toList());

        if (payoutClients.isEmpty()) {
            logger.error("No PAYOUT merchants found in the system");
            throw new RuntimeException("No PAYOUT merchants found in the system");
        }

        logger.info("Found {} PAYOUT merchants to process", payoutClients.size());

        // 4. Get current admin email
        String adminEmail = getCurrentAdminEmail();

        // 5. Check each merchant and create logs only for those not already on this vendor
        List<Switching> switchingLogs = new ArrayList<>();
        List<Map<String, String>> skippedMerchants = new ArrayList<>();

        for (Client client : payoutClients) {
            // Check if merchant is already using this vendor
            Switching latestLog = switchingRepository.findLatestByMerchantIdAndType(
                    client.getUserId(), "PAYOUT");

            if (latestLog != null && vendor.getVendorName().equalsIgnoreCase(latestLog.getSwitchedPipe())) {
                // Merchant already on this vendor - SKIP
                Map<String, String> skippedInfo = new HashMap<>();
                skippedInfo.put("merchantId", client.getUserId());
                skippedInfo.put("merchantName", client.getName());
                skippedInfo.put("reason", "Already using " + vendor.getVendorName());
                skippedMerchants.add(skippedInfo);
                logger.info("Skipping merchant {} - already using {}", 
                          client.getUserId(), vendor.getVendorName());
            } else {
                // Create new switching log
                Switching log = new Switching(
                        client.getUserId(),
                        client.getName(),
                        "PAYOUT",
                        vendor.getVendorName(),
                        adminEmail
                );
                switchingLogs.add(log);
            }
        }

        // 6. Check if all merchants were skipped
        if (switchingLogs.isEmpty()) {
            logger.warn("All PAYOUT merchants are already using {}", vendor.getVendorName());
            throw new RuntimeException("All PAYOUT merchants are already using " + vendor.getVendorName() + 
                                     ". No switching needed.");
        }

        // 7. Save switching logs for merchants that need switching
        switchingRepository.saveAll(switchingLogs);
        logger.info("Switched {} PAYOUT merchants to vendor: {}", switchingLogs.size(), vendor.getVendorName());

        // 8. Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Switching completed for PAYOUT merchants");
        response.put("vendorName", vendor.getVendorName());
        response.put("merchantType", "PAYOUT");
        response.put("totalMerchantsSwitched", switchingLogs.size());
        response.put("totalMerchantsSkipped", skippedMerchants.size());

        if (!skippedMerchants.isEmpty()) {
            response.put("skippedMerchants", skippedMerchants);
            response.put("info", skippedMerchants.size() + " merchant(s) were already using " + 
                               vendor.getVendorName());
        }

        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> switchSelectedPayoutMerchants(String vendorName, List<String> merchantIds) {
        logger.info("switchSelectedPayoutMerchants() → Started for vendorName: {} with {} merchants",
                vendorName, merchantIds.size());

        // 1. Validate input
        if (merchantIds == null || merchantIds.isEmpty()) {
            logger.error("Merchant IDs list is empty");
            throw new RuntimeException("Merchant IDs list cannot be empty");
        }

        // 2. Validate vendor exists
        Vendors vendor = vendorsRepository.findByVendorName(vendorName)
                .orElseThrow(() -> {
                    logger.error("Vendor not found: {}", vendorName);
                    return new RuntimeException("Vendor not found: " + vendorName);
                });

        // 3. Check vendor status
        if (!"Active".equalsIgnoreCase(vendor.getStatus())) {
            logger.error("Vendor is not active: {}", vendorName);
            throw new RuntimeException("Vendor is not active: " + vendorName);
        }

        // 4. Validate merchant IDs exist and are PAYOUT type
        List<Client> selectedPayoutClients = new ArrayList<>();
        List<String> notFoundMerchants = new ArrayList<>();
        List<String> wrongTypeMerchants = new ArrayList<>();

        for (String merchantId : merchantIds) {
            Optional<Client> clientOpt = clientRepository.findByUserId(merchantId);
            if (clientOpt.isPresent()) {
                Client client = clientOpt.get();
                if ("PAYOUT".equalsIgnoreCase(client.getMerchantType())) {
                    selectedPayoutClients.add(client);
                } else {
                    wrongTypeMerchants.add(merchantId);
                    logger.warn("Merchant {} is not a PAYOUT merchant (type: {})", 
                              merchantId, client.getMerchantType());
                }
            } else {
                notFoundMerchants.add(merchantId);
            }
        }

        if (selectedPayoutClients.isEmpty()) {
            logger.error("None of the provided merchant IDs are valid PAYOUT merchants");
            throw new RuntimeException("None of the provided merchant IDs are valid PAYOUT merchants");
        }

        // 5. Get current admin email
        String adminEmail = getCurrentAdminEmail();

        // 6. Check each merchant and create logs only for those not already on this vendor
        List<Switching> switchingLogs = new ArrayList<>();
        List<Map<String, String>> skippedMerchants = new ArrayList<>();

        for (Client client : selectedPayoutClients) {
            // Check if merchant is already using this vendor
            Switching latestLog = switchingRepository.findLatestByMerchantIdAndType(
                    client.getUserId(), "PAYOUT");

            if (latestLog != null && vendor.getVendorName().equalsIgnoreCase(latestLog.getSwitchedPipe())) {
                // Merchant already on this vendor - SKIP
                Map<String, String> skippedInfo = new HashMap<>();
                skippedInfo.put("merchantId", client.getUserId());
                skippedInfo.put("merchantName", client.getName());
                skippedInfo.put("reason", "Already using " + vendor.getVendorName());
                skippedMerchants.add(skippedInfo);
                logger.info("Skipping merchant {} - already using {}", 
                          client.getUserId(), vendor.getVendorName());
            } else {
                // Create new switching log
                Switching log = new Switching(
                        client.getUserId(),
                        client.getName(),
                        "PAYOUT",
                        vendor.getVendorName(),
                        adminEmail
                );
                switchingLogs.add(log);
            }
        }

        // 7. Check if all valid merchants were skipped
        if (switchingLogs.isEmpty()) {
            logger.warn("All selected PAYOUT merchants are already using {}", vendor.getVendorName());
            throw new RuntimeException("All selected PAYOUT merchants are already using " + 
                                     vendor.getVendorName() + ". No switching needed.");
        }

        // 8. Save switching logs
        switchingRepository.saveAll(switchingLogs);
        logger.info("Switched {} PAYOUT merchants to vendor: {}", switchingLogs.size(), vendor.getVendorName());

        // 9. Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Switching completed for selected PAYOUT merchants");
        response.put("vendorName", vendor.getVendorName());
        response.put("merchantType", "PAYOUT");
        response.put("totalMerchantsSwitched", switchingLogs.size());
        response.put("totalMerchantsSkipped", skippedMerchants.size());

        // Add warnings for invalid merchants
        if (!notFoundMerchants.isEmpty() || !wrongTypeMerchants.isEmpty() || !skippedMerchants.isEmpty()) {
            List<String> warnings = new ArrayList<>();
            
            if (!notFoundMerchants.isEmpty()) {
                warnings.add(notFoundMerchants.size() + " merchant(s) not found");
                response.put("notFoundMerchants", notFoundMerchants);
            }
            if (!wrongTypeMerchants.isEmpty()) {
                warnings.add(wrongTypeMerchants.size() + " merchant(s) are not PAYOUT type");
                response.put("wrongTypeMerchants", wrongTypeMerchants);
            }
            if (!skippedMerchants.isEmpty()) {
                warnings.add(skippedMerchants.size() + " merchant(s) already using " + vendor.getVendorName());
                response.put("skippedMerchants", skippedMerchants);
            }
            
            response.put("warning", String.join(", ", warnings));
        }

        return response;
    }

    @Override
    public List<Switching> getPayoutSwitchingLogs() {
        logger.info("getPayoutSwitchingLogs() → Fetching all PAYOUT switching logs");
        List<Switching> logs = switchingRepository.findPayoutLogsOrderByDateTimeDesc();
        logger.info("Found {} PAYOUT switching log records", logs.size());
        return logs;
    }

    // ==================== COMMON ====================

    @Override
    public List<Vendors> getAllActiveVendors() {
        logger.info("getAllActiveVendors() → Fetching all active vendors");
        List<Vendors> allVendors = vendorsRepository.findAll();

        // Filter only active vendors
        List<Vendors> activeVendors = allVendors.stream()
                .filter(v -> "Active".equalsIgnoreCase(v.getStatus()))
                .collect(Collectors.toList());

        logger.info("Found {} active vendors", activeVendors.size());
        return activeVendors;
    }

    /**
     * Get current logged-in admin email from Spring Security context
     */
    private String getCurrentAdminEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                logger.info("Current admin email: {}", email);
                return email;
            }
            logger.warn("No authenticated admin found, using 'System'");
            return "System";
        } catch (Exception e) {
            logger.error("Error getting admin email: {}", e.getMessage());
            return "System";
        }
    }
}