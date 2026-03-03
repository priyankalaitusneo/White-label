package com.laitsneo.whitelbl.service;

import com.laitsneo.whitelbl.dto.Admin.SwitchingDTO;
import com.laitsneo.whitelbl.entity.Admin.Switching;
import com.laitsneo.whitelbl.entity.Admin.Vendors;

import java.util.List;
import java.util.Map;

public interface SwitchingService {

    //  PAYIN SWITCHING    
	
    // Switch all PAYIN merchants to a selected vendor
    
    Map<String, Object> switchAllPayinMerchants(SwitchingDTO request);

    // Switch selected PAYIN merchants to a vendor
   
    Map<String, Object> switchSelectedPayinMerchants(String vendorName, List<String> merchantIds);

    // Get all PAYIN switching logs (history)
    
    List<Switching> getPayinSwitchingLogs();

    // PAYOUT SWITCHING 
    
    // Switch all PAYOUT merchants to a selected vendor
    
    Map<String, Object> switchAllPayoutMerchants(SwitchingDTO request);

    // Switch selected PAYOUT merchants to a vendor
     
    Map<String, Object> switchSelectedPayoutMerchants(String vendorName, List<String> merchantIds);

    // Get all PAYOUT switching logs (history)
   
    List<Switching> getPayoutSwitchingLogs();

  
    // Get all active vendors (both PAYIN and PAYOUT)
    
    List<Vendors> getAllActiveVendors();
}