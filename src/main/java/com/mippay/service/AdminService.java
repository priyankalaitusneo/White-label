package com.mippay.service;

import com.mippay.entity.Admin.PayInCharges;
import com.mippay.entity.Client.Client;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.mippay.dto.Admin.PayInChargesRequestDto;
import com.mippay.dto.Admin.PrefundApprovalDto;
import com.mippay.dto.Admin.PrefundRejectDto;
import com.mippay.dto.Admin.UpdateChargesDto;
import com.mippay.dto.Admin.VendorsDTO;
import com.mippay.dto.Client.ClientResponseDto;
import com.mippay.entity.Admin.Charges;
import com.mippay.entity.Admin.User;
import com.mippay.entity.Client.LienHistory;
import com.mippay.response.PayInChargesResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AdminService {

    String createAdmin(@Valid User request);

    ResponseEntity<?> setCharges(Charges data, HttpServletRequest req);

    ResponseEntity<?> approvePrefundRequest(PrefundApprovalDto approvalDto);

    ResponseEntity<?> getAllClients();

    ResponseEntity<?> updateCharges(@Valid UpdateChargesDto data, HttpServletRequest req);

    ResponseEntity<?> getAllCharges();

    ResponseEntity<?> deleteChargesBySlNo(int slNo);


    User getAdminByEmail(String username);

//    ResponseEntity<?> prefundHistory();

    ResponseEntity<?> profileByUserId(String userId);

    ResponseEntity<?> clientListAndWallets();

    ResponseEntity<?> updateStatusByUserId(Map<String,Object> userId);

    ResponseEntity<?> deleteClient(String clientId);

    ResponseEntity<?> allTransactions();

    ResponseEntity<?> allTrasactionCountAndAmount();

    ResponseEntity<?> filterByUtr(String utr);

    ResponseEntity<?> filterByTransactionId(String transactionId);

    ResponseEntity<?> prefundFilter(Map<String, Object> data);

    ResponseEntity<?> webhookList();

    ResponseEntity<?> ipAddressList();

    ResponseEntity<?> lienAmountList();

    ResponseEntity<?> addLienForPrefundList(LienHistory data);
    
 // -- Vendor 
    
    ResponseEntity<?> createVendor(VendorsDTO vendorsDTO);
  
    ResponseEntity<List<VendorsDTO>> getAllVendors();

    ResponseEntity<?> updateVendor(String id, VendorsDTO vendorsDTO);

    ResponseEntity<?> deleteVendor(String id);

    ResponseEntity<?> updateVendorStatus(Map<String, Object> requestBody);

   
    ResponseEntity<?> validateVendorAmountLimit(String vendorId, double payoutAmount);

    
    
    List<PayInChargesResponseDto> getChargesByUser(String userId);
    
    
    List<PayInChargesResponseDto> getAllChargesForPayIn();
    
    Page<Map<String, Object>> getPayinMerchants(String search, Pageable pageable);
    
    Object getMerchantDetailsById(String merchantId);

    Page<Map<String, Object>> getPayoutMerchants(String search, Pageable pageable);
    
    Object getPayoutMerchantDetailsById(String merchantId);
    
//    ResponseEntity<?> approvedPrefundHistory();



    ResponseEntity<?> addPayInCharges(PayInCharges dto);

    ResponseEntity<?> updatePayInCharges(Long id, PayInChargesRequestDto dto);

    ResponseEntity<?> getPayInChargesByUser(String userId);

    ResponseEntity<?> deletePayInCharges(Long id);

    ResponseEntity<?> getAllPayInCharges();

	ResponseEntity<?> rejectPrefundRequest(PrefundRejectDto prefundRejectDto);

//	ResponseEntity<?> rejectedPrefundHistory();


	ResponseEntity<?> prefundHistory(int page, int size);

	ResponseEntity<?> approvedPrefundHistory(int page, int size);

	ResponseEntity<?> rejectedPrefundHistory(int page, int size);

	ResponseEntity<?> getPrefundReports(String merchantId, String status, LocalDate fromDate, LocalDate toDate,
			int page, int size);

	ResponseEntity<?> allTransactionCountAndAmountYearMonthWise();

	ResponseEntity<?> allTrasactionCountAndAmountOverall();

	ResponseEntity<?> payinWebhookList();

	ResponseEntity<?> payoutWebhookList();
	
	
}
