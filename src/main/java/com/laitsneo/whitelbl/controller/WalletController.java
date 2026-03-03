package com.laitsneo.whitelbl.controller;

import com.laitsneo.whitelbl.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/payment/admin/wallet")
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    @Autowired
    private WalletService walletService;

   
    @GetMapping("/merchants")
    public ResponseEntity<?> getAllMerchantsWalletSummary(
            @RequestParam(required = false) String search) {
        
        logger.info("GET /wallet/merchants → Request received with search: {}", search);
        
        ResponseEntity<?> response = walletService.getAllMerchantsWalletSummary(search);
        
        logger.info("GET /wallet/merchants → Response status: {}", response.getStatusCode());
        return response;
    }

   
    @GetMapping("/summary")
    public ResponseEntity<?> getAggregateWalletSummary() {
        
        logger.info("GET /wallet/summary → Request received");
        
        ResponseEntity<?> response = walletService.getAggregateWalletSummary();
        
        logger.info("GET /wallet/summary → Response status: {}", response.getStatusCode());
        return response;
    }

   
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<?> getMerchantWalletDetails(@PathVariable String merchantId) {
        
        logger.info("GET /wallet/merchant/{} → Request received", merchantId);
        
        ResponseEntity<?> response = walletService.getMerchantWalletDetails(merchantId);
        
        logger.info("GET /wallet/merchant/{} → Response status: {}", merchantId, response.getStatusCode());
        return response;
    }
}