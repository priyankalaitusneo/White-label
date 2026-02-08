package com.mippay.service;



import com.mippay.entity.Client.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;

import com.mippay.dto.Admin.CollectionHistoryDto;
import com.mippay.dto.Admin.PayinDto;

import com.mippay.dto.Client.ClientEditProfileDto;
import com.mippay.dto.Client.EmailOtpDto;
import com.mippay.dto.Client.HoldAmountDto;
import com.mippay.dto.Client.LienAmountDTO;
import com.mippay.dto.Client.LienResponseDTO;
import com.mippay.dto.Client.PayoutDto;
import com.mippay.dto.Client.PayoutFilterByCLientId;
import com.mippay.dto.Client.PrefundDto;
import com.mippay.dto.Client.SupportTicketRequestDTO;

import com.mippay.response.LocalCheckStatusResponse;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ClientService {

    ResponseEntity<?> editProfile(String userId, ClientEditProfileDto editProfileDto);

    ResponseEntity<?> paymentPayout(@Valid PayoutDto data, String clientId, String clientSecretId, HttpServletRequest req) throws Exception;

    // New report methods
    ResponseEntity<?> getAllPayoutRecordsReport(LocalDate fromDate, LocalDate toDate);

    ResponseEntity<?> getPayoutRecordsByUserIdReport(LocalDate fromDate, LocalDate toDate, String userId);

    ResponseEntity<?> getChargesByUserId(String userId);

    ResponseEntity<?> createPrefundRequest(PrefundDto request);

    String clientOnboard(@Valid Client data);

    Client getClientByEmail(@NotBlank(message = "Email is required") @Email(message = "Please provide a valid email") String username);

    ResponseEntity<?> clientByClientId(String clientId);

    String saveCallBack(Map<String,Object> request);

    ResponseEntity<?> prefundListByClientId(String clientId, int page, int size);

    ResponseEntity<?> transactionRecordsByClientId(String clientId, int page, int size);

    ResponseEntity<?> allTrasactionCountAndAmount(String clientId);

    ResponseEntity<?> sendOtp(String mail) throws Exception;

    ResponseEntity<?> verifyOtp(EmailOtpDto emailOtpDto) throws Exception;

    ResponseEntity<?> changePassword(EmailOtpDto emailOtpDto);

   
    ResponseEntity<?> payoutFilterByClientId(PayoutFilterByCLientId data);

    ResponseEntity<?> addWebhook(@Valid WebhookUrl data);

    ResponseEntity<?> updateWebhook(@Valid WebhookUrl data);

    ResponseEntity<?> webhookByClientId(String clientId);

    ResponseEntity<?> addIpAddress(IpAddress data);

    ResponseEntity<?> updateIpAddress(@Valid IpAddress data);

    ResponseEntity<?> ipAddressByClientId(String clientId);

    ResponseEntity<?> updateMerchant(ClientEditProfileDto data);

//    ResponseEntity<?> addLienAmount(@Valid LienAmount data);

    ResponseEntity<?> updateLienAmount(@Valid LienAmount data);

    ResponseEntity<?> walletDashboardByClientId(String clientId);

    ResponseEntity<?> filterByOrderId(String orderId);

    ResponseEntity<?> lienHistory(String clientId);

//    ResponseEntity<?> deleteLienAmount(String userId);

    LocalCheckStatusResponse  testCheckStatusLocal(String orderId);

    ResponseEntity<?> payoutFilter(PayoutFilterByCLientId data);

    ResponseEntity<?> trasactionCountAndAmountByDate(Map<String, Object> data);

	ResponseEntity<?> prefundFilterByClientId(Map<String, Object> data);

    ResponseEntity<?> dailyCountAndAmount();

	ResponseEntity<?> paymentPayin(@Valid PayinDto data, String client_id, String client_secret_id,
			HttpServletRequest req) throws Exception;


	String refundAmount(String userId, String txnId);

	String holdAmount(String userId, String txnId);

	CollectionHistoryDto getDetails(String userId, String txnId);

	List<CollectionHistoryDto> getHistory(String userId, String fromDate, String toDate, String utr, String txnId);

	ResponseEntity<?> getAllPayinRecordsReport(LocalDate fromDate, LocalDate toDate);

	ResponseEntity<?> holdPayinAmount(@Valid HoldAmountDto holdAmountDto);

	LienResponseDTO addLienAmount(LienAmountDTO lienAmountDTO);

	LienResponseDTO deleteLienAmount(String userId);

	ResponseEntity<?> payinTransactionRecordsByClientId(String clientId, int page, int size);

	ResponseEntity<?> getPayinReportsByUserId(String userId, String status, String paymentMethod, LocalDate fromDate,
			LocalDate toDate, int page, int size);

	ResponseEntity<?> getPayoutReportsByUserId(String userId, String status, String paymentMethod, LocalDate fromDate,
			LocalDate toDate, int page, int size);

	ResponseEntity<?> prefundHistory(String userId, int page, int size);

	ResponseEntity<?> approvedPrefundHistory(String userId, int page, int size);

	ResponseEntity<?> rejectedPrefundHistory(String userId, int page, int size);

	ResponseEntity<?> raiseTicket(String userId, SupportTicketRequestDTO request);
	
	// Get Payin Wallet Summary with total amount and transaction list
	 
	ResponseEntity<?> getPayinWalletSummary(String userId);

	// Get Payout Wallet Summary with total amount and transaction list
	 
	ResponseEntity<?> getPayoutWalletSummary(String userId);

	// Get Locked Funds Summary with total locked amount and locked funds list
	 
	ResponseEntity<?> getLockedFundsSummary(String userId);

	ResponseEntity<?> prefundHistoryAll(String userId, int page, int size);

	ResponseEntity<?> lienAmountListByUserId(String userId);

	ResponseEntity<?> prefundHistory(String userId);

	ResponseEntity<?> allTransactionCountAndAmountClientYearMonth(String clientId);

	ResponseEntity<?> allTrasactionCountAndAmountOverall(String clientId);

    ResponseEntity<?> payGorderCreate(PayinDto data);

	ResponseEntity<?> paymentPayinPhonepe(@Valid PayinDto data, String clientId, String clientSecretId,
			HttpServletRequest request) throws Exception;

	String savePhonePeCallBack(Map<String, Object> request);

	String handlePhonePeWebhook(Map<String, Object> request);

    ResponseEntity<?> payinDashboard(String clientId);

    ResponseEntity<?> settlementByClientId(String clientId);

    ResponseEntity<?> settlementListByClientId(String clientId);

//    ResponseEntity<?> buckBoxPayin(PayinRecords data) throws Exception;
}
