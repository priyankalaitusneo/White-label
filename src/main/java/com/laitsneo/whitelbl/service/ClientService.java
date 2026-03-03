package com.laitsneo.whitelbl.service;



import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import com.laitsneo.whitelbl.dto.Admin.CollectionHistoryDto;
import com.laitsneo.whitelbl.dto.Admin.PayinDto;
import com.laitsneo.whitelbl.dto.Client.ClientEditProfileDto;
import com.laitsneo.whitelbl.dto.Client.ClientOnboardDto;
import com.laitsneo.whitelbl.dto.Client.EmailOtpDto;
import com.laitsneo.whitelbl.dto.Client.HoldAmountDto;
import com.laitsneo.whitelbl.dto.Client.LienAmountDTO;
import com.laitsneo.whitelbl.dto.Client.LienResponseDTO;
import com.laitsneo.whitelbl.dto.Client.PayoutDto;
import com.laitsneo.whitelbl.dto.Client.PayoutFilterByCLientId;
import com.laitsneo.whitelbl.dto.Client.PrefundDto;
import com.laitsneo.whitelbl.dto.Client.SupportTicketRequestDTO;
import com.laitsneo.whitelbl.entity.Client.Client;
import com.laitsneo.whitelbl.entity.Client.IpAddress;
import com.laitsneo.whitelbl.entity.Client.LienAmount;
import com.laitsneo.whitelbl.entity.Client.PayinRecords;
import com.laitsneo.whitelbl.entity.Client.WebhookUrl;
import com.laitsneo.whitelbl.response.LocalCheckStatusResponse;

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

    String clientOnboard(ClientOnboardDto dto,
            MultipartFile aadhaarFront,
            MultipartFile aadhaarBack,
            MultipartFile panCard,
            MultipartFile gstFile,
            MultipartFile shopPhoto,
            MultipartFile profilePhoto);
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

	ResponseEntity<?> createClient(ClientOnboardDto dto, MultipartFile aadharFront, MultipartFile aadharBack,
			MultipartFile panDoc, MultipartFile gstDoc, MultipartFile shopPhoto, MultipartFile profilePhoto);

//	ResponseEntity<?> prefundHistory(String userId);
	
}
