package com.laitsneo.whitelbl.service;

import com.laitsneo.whitelbl.dto.Client.AggregateWalletSummaryDto;
import com.laitsneo.whitelbl.dto.Client.MerchantWalletSummaryDto;
import com.laitsneo.whitelbl.dto.Client.WalletDetailResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface WalletService {

   
    ResponseEntity<?> getAllMerchantsWalletSummary(String search);

    
    ResponseEntity<?> getAggregateWalletSummary();

  
    ResponseEntity<?> getMerchantWalletDetails(String merchantId);
}