package com.mippay.service;

import org.springframework.http.ResponseEntity;

public interface WalletService {

   
    ResponseEntity<?> getAllMerchantsWalletSummary(String search);

    
    ResponseEntity<?> getAggregateWalletSummary();

  
    ResponseEntity<?> getMerchantWalletDetails(String merchantId);
}