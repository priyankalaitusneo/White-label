package com.mippay.controller;

import com.mippay.dto.Admin.PayinDto;
import com.mippay.dto.Client.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.mippay.entity.Client.Client;
import com.mippay.entity.Client.PayinRecords;
import com.mippay.entity.Client.WebhookUrl;

import com.mippay.helper.Generator;
import com.mippay.repository.Admin.UserRepository;
import com.mippay.repository.Client.ClientRepository;

import com.mippay.service.ClientService;
//import com.mippay.service.TrexoService;
import com.mippay.service.WalletService;

import com.mippay.serviceImpl.Client.ClientServiceImpl;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/payment/client")
public class ClientControlller {

	private static final Logger logger = LoggerFactory.getLogger(ClientControlller.class);

	@Autowired
	private ClientService clientService;
	@Autowired
	private ClientRepository clientRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ClientServiceImpl clientServiceimpl;

	@Autowired
	private WalletService walletService;
	
	
	
	 @Value("${phonepe.webhook.username}")
	    private String webhookUsername;

	    @Value("${phonepe.webhook.password}")
	    private String webhookPassword;
//
//	@Autowired
//	private TrexoService trexoService;

	private ResponseEntity<Map<String, Object>> internalErrorResponse(String where, Exception ex) {
		logger.error("Internal error in {} : {}", where, ex.getMessage(), ex);
		Map<String, Object> resp = new HashMap<>();
		resp.put("success", false);
		resp.put("message", "Internal server error");
		resp.put("errorCode", "INTERNAL_ERROR");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
	}

	@GetMapping("/client/{clientId}")
	public ResponseEntity<?> clientByClientId(@PathVariable String clientId) {
		logger.info("Start: clientByClientId - clientId={}", clientId);
		try {
			ResponseEntity<?> response = this.clientService.clientByClientId(clientId);
			logger.info("Success: clientByClientId - clientId={}", clientId);
			return response;
		} catch (Exception ex) {
			return internalErrorResponse("clientByClientId", ex);
		}
	}

	@PostMapping("/initiatePrefund")
	public ResponseEntity<?> createPrefundRequest(@RequestBody PrefundDto request) {
		logger.info("Start: createPrefundRequest - clientId(if present)={}", request == null ? "null" : request);
		try {
			ResponseEntity<?> response = clientService.createPrefundRequest(request);
			logger.info("Success: createPrefundRequest");
			return response;
		} catch (Exception ex) {
			return internalErrorResponse("createPrefundRequest", ex);
		}
	}

	@PutMapping("/edit-profile")
	public ResponseEntity<?> editProfile(@Valid @RequestBody ClientEditProfileDto editProfileDto) {
		Map<String, Object> response = new HashMap<>();

		try {
			// Get authenticated user's email from JWT (for clients, username = email)
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (authentication == null || !authentication.isAuthenticated()) {
				response.put("success", false);
				response.put("message", "User not authenticated");
				response.put("errorCode", "UNAUTHENTICATED");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

			String email = authentication.getName(); // For clients, this is their email
			logger.info("Client edit profile request for email: {}", email);

			// Check if any updates are provided
			if (!editProfileDto.hasUpdates()) {
				response.put("success", false);
				response.put("message", "No data provided for update");
				response.put("errorCode", "NO_UPDATE_DATA");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			// Find client by email directly
			Optional<Client> clientOpt = clientRepository.findByEmail(email);
			if (clientOpt.isEmpty()) {
				response.put("success", false);
				response.put("message", "Client profile not found for email: " + email);
				response.put("errorCode", "PROFILE_NOT_FOUND");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}

			Client client = clientOpt.get();
			String userId = client.getUserId();
			logger.info("Found Client - UserId: {}, Email: {}", userId, client.getEmail());

			// Call service method
			return clientService.editProfile(userId, editProfileDto);

		} catch (Exception e) {
			logger.error("Error in edit-profile controller: ", e);
			response.put("success", false);
			response.put("message", "Internal server error");
			response.put("errorCode", "INTERNAL_ERROR");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/payout-payment")
	public ResponseEntity<?> paymentPayout(@Valid @RequestBody PayoutDto data,
			@RequestHeader("Client-Id") String client_id, @RequestHeader("Client-SecretId") String client_secret_id,
			HttpServletRequest req) throws Exception {
		logger.info("Start: paymentPayout - clientIdHeader={}", client_id);
		try {
			ResponseEntity<?> resp = this.clientService.paymentPayout(data, client_id, client_secret_id, req);
			logger.info("Success: paymentPayout - clientIdHeader={}", client_id);
			return resp;
		} catch (Exception ex) {
			return internalErrorResponse("paymentPayout", ex);
		}
	}

	@GetMapping("/transaction-records/{clientId}")
	public ResponseEntity<?> transactionRecordsByClientId(@PathVariable String clientId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		logger.info("Start: transactionRecordsByClientId - clientId={}, page={}, size={}", clientId, page, size);
		try {
			ResponseEntity<?> resp = clientService.transactionRecordsByClientId(clientId, page, size);
			logger.info("Success: transactionRecordsByClientId - clientId={}", clientId);
			return resp;
		} catch (Exception ex) {
			return internalErrorResponse("transactionRecordsByClientId", ex);
		}
	}

	@GetMapping("/payout-reports")
	public ResponseEntity<?> getAllPayoutRecordsReport(
			@RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

		logger.info("Get all payout records report request from {} to {}", fromDate, toDate);

		try {
			return clientService.getAllPayoutRecordsReport(fromDate, toDate);
		} catch (Exception e) {
			logger.error("Error in getAllPayoutRecordsReport controller: ", e);
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Internal server error");
			response.put("errorCode", "INTERNAL_ERROR");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/payout-reports/user/{userId}")
	public ResponseEntity<?> getPayoutRecordsByUserIdReport(@PathVariable String userId,
			@RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

		logger.info("Get payout records report request for userId {} from {} to {}", userId, fromDate, toDate);

		try {
			return clientService.getPayoutRecordsByUserIdReport(fromDate, toDate, userId);
		} catch (Exception e) {
			logger.error("Error in getPayoutRecordsByUserIdReport controller: ", e);
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Internal server error");
			response.put("errorCode", "INTERNAL_ERROR");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/getCharges/{userId}")
	public ResponseEntity<?> getChargesByUserId(@PathVariable String userId) {
		logger.info("Start: getChargesByUserId - userId={}", userId);
		try {
			ResponseEntity<?> response = this.clientService.getChargesByUserId(userId);
			logger.info("Success: getChargesByUserId - userId={}", userId);
			return response;
		} catch (Exception ex) {
			return internalErrorResponse("getChargesByUserId", ex);
		}
	}

	@GetMapping("/prefund-list/{clientId}")
	public ResponseEntity<?> prefundListByClientId(@PathVariable String clientId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return this.clientService.prefundListByClientId(clientId, page, size);
	}

	@GetMapping("/transaction-countsDaily/{clientId}")
	public ResponseEntity<?> transactionCountAndAmount(@PathVariable String clientId) {
		logger.info("Start: transactionCountAndAmount - clientId={}", clientId);
		try {
			ResponseEntity<?> resp = clientService.allTrasactionCountAndAmount(clientId);
			logger.info("Success: transactionCountAndAmount - clientId={}", clientId);
			return resp;
		} catch (Exception ex) {
			return internalErrorResponse("transactionCountAndAmount", ex);
		}
	}

	@GetMapping("/transaction-counts/{clientId}/year-month")
	public ResponseEntity<?> transactionCountAndAmountClientYearMonth(
	        @PathVariable String clientId) {

	    logger.info("GET /transaction-counts/{}/year-month", clientId);
	    return clientService.allTransactionCountAndAmountClientYearMonth(clientId);
	}

	
	@GetMapping("/transaction-counts")
	public ResponseEntity<?> transactionCountAndAmountByDate(@RequestBody Map<String, Object> data) {
		return clientService.trasactionCountAndAmountByDate(data);
	}

	@PostMapping("/email/{mail}")
	public ResponseEntity<?> sendMail(@PathVariable String mail) throws Exception {
		logger.info("Start: sendMail - mail={}", mail);
		try {
			ResponseEntity<?> resp = clientService.sendOtp(mail);
			logger.info("Success: sendMail - mail={}", mail);
			return resp;
		} catch (Exception ex) {
			return internalErrorResponse("sendMail", ex);
		}
	}

	@PostMapping("/verifyOtp-updatePassword")
	public ResponseEntity<?> verifyOtp(@Valid @RequestBody EmailOtpDto emailOtpDto) throws Exception {
		logger.info("Start: verifyOtp-updatePassword - email={}",
				emailOtpDto == null ? "null" : emailOtpDto.getEmail());
		try {
			ResponseEntity<?> resp = this.clientService.verifyOtp(emailOtpDto);
			logger.info("Success: verifyOtp-updatePassword - email={}",
					emailOtpDto == null ? "null" : emailOtpDto.getEmail());
			return resp;
		} catch (Exception ex) {
			return internalErrorResponse("verifyOtp-updatePassword", ex);
		}
	}

	@PostMapping("/changePassword")
	public ResponseEntity<?> changePassword(@RequestBody EmailOtpDto emailOtpDto) throws Exception {
		logger.info("Start: changePassword - email={}", emailOtpDto == null ? "null" : emailOtpDto.getEmail());
		try {
			ResponseEntity<?> resp = this.clientService.changePassword(emailOtpDto);
			logger.info("Success: changePassword - email={}", emailOtpDto == null ? "null" : emailOtpDto.getEmail());
			return resp;
		} catch (Exception ex) {
			return internalErrorResponse("changePassword", ex);
		}
	}

	@PostMapping("/prefundFilter-ClientId")
	public ResponseEntity<?> prefundFilterByClientId(@RequestBody Map<String, Object> data) {
		logger.info("Start: prefundFilterByClientId - payloadKeys={}", data == null ? 0 : data.keySet());
		try {
			ResponseEntity<?> resp = this.clientService.prefundFilterByClientId(data);
			logger.info("Success: prefundFilterByClientId");
			return resp;
		} catch (Exception ex) {
			return internalErrorResponse("prefundFilterByClientId", ex);
		}
	}

	@PostMapping("/payoutFilter-ClientId")
	public ResponseEntity<?> payoutFilterByClientId(@RequestBody PayoutFilterByCLientId data) {
		logger.info("Start: payoutFilterByClientId - payload present={}", data != null);
		try {
			ResponseEntity<?> resp = this.clientService.payoutFilterByClientId(data);
			logger.info("Success: payoutFilterByClientId");
			return resp;
		} catch (Exception ex) {
			return internalErrorResponse("payoutFilterByClientId", ex);
		}
	}

	@PostMapping("/add-webhook")
	public ResponseEntity<?> addWebhook(@Valid @RequestBody WebhookUrl data) {
		return clientService.addWebhook(data);
	}

	@PutMapping("/update_webhook")
	public ResponseEntity<?> updateWebhook(@Valid @RequestBody WebhookUrl data) {
		logger.info("Start: updateWebhook - clientId(if present)={}", data == null ? "null" : data);
		try {
			ResponseEntity<?> resp = clientService.updateWebhook(data);
			logger.info("Success: updateWebhook - clientId={}", data == null ? "null" : data);
			return resp;
		} catch (Exception ex) {
			return internalErrorResponse("updateWebhook", ex);
		}
	}

	@GetMapping("/webhook/{clientId}")
	public ResponseEntity<?> webhookByClientId(@PathVariable String clientId) {
		logger.info("Start: webhookByClientId - clientId={}", clientId);
		try {
			ResponseEntity<?> resp = clientService.webhookByClientId(clientId);
			logger.info("Success: webhookByClientId - clientId={}", clientId);
			return resp;
		} catch (Exception ex) {
			return internalErrorResponse("webhookByClientId", ex);
		}
	}

	@GetMapping("/wallet-dashboard/{clientId}")
	public ResponseEntity<?> walletDashboardByClientId(@PathVariable String clientId) {
		logger.info("Start: walletDashboardByClientId - clientId={}", clientId);
		try {
			ResponseEntity<?> resp = clientService.walletDashboardByClientId(clientId);
			logger.info("Success: walletDashboardByClientId - clientId={}", clientId);
			return resp;
		} catch (Exception ex) {
			return internalErrorResponse("walletDashboardByClientId", ex);
		}
	}

	@GetMapping("/ipAddress/{clientId}")
	public ResponseEntity<?> ipAddressByClientId(@PathVariable String clientId) {
		return clientService.ipAddressByClientId(clientId);
	}

	@GetMapping("lien-history/{clientId}")
	public ResponseEntity<?> lientHistory(@PathVariable String clientId) {
		logger.info("Start: lientHistory - clientId={}", clientId);
		try {
			ResponseEntity<?> resp = clientService.lienHistory(clientId);
			logger.info("Success: lientHistory - clientId={}", clientId);
			return resp;
		} catch (Exception ex) {
			return internalErrorResponse("lientHistory", ex);
		}
	}

//	@GetMapping("/check/{orderId}")
//	public ResponseEntity<?> checkStatus(@PathVariable String orderId) {
//		logger.info("Start: checkStatus - orderId={}", orderId);
//		try {
//			Map<String, Object> response = trexoService.checkTransaction(orderId);
//			logger.info("Success: checkStatus - orderId={}", orderId);
//			return ResponseEntity.ok(response);
//		} catch (Exception ex) {
//			return internalErrorResponse("checkStatus", ex);
//		}
//	}

	@PostMapping("/payinpayment")
	public ResponseEntity<?> paymentPayin(@Valid @RequestBody PayinDto data,
			@RequestHeader("Client-Id") String client_id, @RequestHeader("Client-SecretId") String client_secret_id,
			HttpServletRequest req) throws Exception {
		System.out.println(data + "---");
		return clientService.paymentPayin(data, client_id, client_secret_id, req);
	}

	@GetMapping("/payin-transaction-records/{clientId}")
	public ResponseEntity<?> payinTransactionRecordsByClientId(@PathVariable String clientId,
			@RequestParam int page, @RequestParam int size) {
		logger.info("Start: payinTransactionRecordsByClientId | clientId={}, page={}, size={}", clientId, page, size);
		try {
			ResponseEntity<?> resp = clientService.payinTransactionRecordsByClientId(clientId, page, size);
			logger.info("Success: payinTransactionRecordsByClientId | clientId={}", clientId);
			return resp;
		} catch (Exception ex) {
			logger.error("Error: payinTransactionRecordsByClientId | clientId={}", clientId, ex);
			return internalErrorResponse("payinTransactionRecordsByClientId", ex);
		}
	}

	@GetMapping("/payin-reports/{userId}")
	public ResponseEntity<?> getPayinReportsByUserId(@PathVariable String userId,
			@RequestParam(required = false) String status, @RequestParam(required = false) String paymentMethod,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestParam int page, @RequestParam int size) {
		logger.info("PAYIN REPORT | userId={}, status={}, method={}, fromDate={}, toDate={}, page={}, size={}", userId,
				status, paymentMethod, fromDate, toDate, page, size);

		try {
			return clientService.getPayinReportsByUserId(userId, status, paymentMethod, fromDate, toDate, page, size);
		} catch (Exception ex) {
			logger.error("Error fetching payin report | userId={}", userId, ex);
			return internalErrorResponse("getPayinReportsByUserId", ex);
		}
	}

	@GetMapping("/payout-reports/{userId}")
	public ResponseEntity<?> getPayoutReportsByUserId(
	        @PathVariable String userId,
	        @RequestParam(required = false) String status,
	        @RequestParam(required = false) String paymentMethod,
	        @RequestParam(required = false)
	        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
	        @RequestParam(required = false)
	        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
	        @RequestParam int page,
	        @RequestParam int size
	) {

	    logger.info(
	        "PAYOUT REPORT | userId={}, status={}, method={}, fromDate={}, toDate={}, page={}, size={}",
	        userId, status, paymentMethod, fromDate, toDate, page, size
	    );

	    try {
	        return clientService.getPayoutReportsByUserId(
	                userId, status, paymentMethod, fromDate, toDate, page, size
	        );
	    } catch (Exception ex) {
	        logger.error("Error fetching payout report | userId={}", userId, ex);
	        return internalErrorResponse("getPayoutReportsByUserId", ex);
	    }
	}


	@GetMapping("/prefund-history/{userId}")
	public ResponseEntity<?> prefundHistory(@PathVariable String userId, @RequestParam int page,
			@RequestParam int size) {
		logger.info("GET /prefund-history | userId={}, page={}, size={}", userId, page, size);
		return clientService.prefundHistory(userId, page, size);
	}

	@GetMapping("/approvedprefundList/{userId}")
	public ResponseEntity<?> approvedPrefundHistory(
	        @PathVariable String userId,
	        @RequestParam int page,
	        @RequestParam int size
	) {
	    logger.info(
	        "GET /prefund-history/approved | userId={}, page={}, size={}",
	        userId, page, size
	    );
	    return clientService.approvedPrefundHistory(userId, page, size);
	}

	@GetMapping("/rejectedprefundList/{userId}")
	public ResponseEntity<?> rejectedPrefundHistory(
	        @PathVariable String userId,
	        @RequestParam int page,
	        @RequestParam int size
	) {
	    logger.info(
	        "GET /prefund-history/rejected | userId={}, page={}, size={}",
	        userId, page, size
	    );
	    return clientService.rejectedPrefundHistory(userId, page, size);
	}


	
	// Get Payin Wallet Summary - Shows total amount and all payin transactions for
	// a user

	@GetMapping("/wallet/payin-summary/{userId}")
	public ResponseEntity<?> getPayinWalletSummary(@PathVariable String userId) {
		logger.info("Fetching payin wallet summary for userId: {}", userId);
		try {
			ResponseEntity<?> response = clientService.getPayinWalletSummary(userId);
			logger.info("Successfully fetched payin wallet summary for userId: {}", userId);
			return response;
		} catch (Exception ex) {
			return internalErrorResponse("getPayinWalletSummary", ex);
		}
	}

	// Get Payout Wallet Summary - Shows total amount and all payout transactions
	// for a user

	@GetMapping("/wallet/payout-summary/{userId}")
	public ResponseEntity<?> getPayoutWalletSummary(@PathVariable String userId) {
		logger.info("Fetching payout wallet summary for userId: {}", userId);
		try {
			ResponseEntity<?> response = clientService.getPayoutWalletSummary(userId);
			logger.info("Successfully fetched payout wallet summary for userId: {}", userId);
			return response;
		} catch (Exception ex) {
			return internalErrorResponse("getPayoutWalletSummary", ex);
		}
	}

	// Get Locked Funds Summary - Shows total locked amount and all locked fund
	// records for a user

	@GetMapping("/wallet/locked-funds-summary/{userId}")
	public ResponseEntity<?> getLockedFundsSummary(@PathVariable String userId) {
		logger.info("Fetching locked funds summary for userId: {}", userId);
		try {
			ResponseEntity<?> response = clientService.getLockedFundsSummary(userId);
			logger.info("Successfully fetched locked funds summary for userId: {}", userId);
			return response;
		} catch (Exception ex) {
			return internalErrorResponse("getLockedFundsSummary", ex);
		}
	}

	@PostMapping("/raise-ticket")
	public ResponseEntity<?> raiseSupportTicket(@RequestHeader("Client-Id") String userId,
			@RequestBody SupportTicketRequestDTO request) {
		return clientService.raiseTicket(userId, request);
	}

	@GetMapping("/prefund-history/all/{userId}")
	public ResponseEntity<?> prefundHistoryAll(
	        @PathVariable String userId,
	        @RequestParam int page,
	        @RequestParam int size
	) {
	    logger.info(
	        "GET /prefund-history/all | userId={}, page={}, size={}",
	        userId, page, size
	    );
	    return clientService.prefundHistoryAll(userId, page, size);
	}

	@GetMapping("/lienAmount-list/{userId}")
	public ResponseEntity<?> lienAmountListByUserId(@PathVariable String userId) {
		logger.info("GET /lienAmount-list/{}", userId);
		return clientService.lienAmountListByUserId(userId);
	}

	@GetMapping("/transaction-countsOverall/{clientId}")
	public ResponseEntity<?> transactionCountAndAmountOverall(
	        @PathVariable String clientId) {
	    logger.info("Start: transactionCountAndAmountOverall - clientId={}", clientId);
	    try {
	        return clientService.allTrasactionCountAndAmountOverall(clientId);
	    } catch (Exception ex) {
	        return internalErrorResponse("transactionCountAndAmountOverall", ex);
	    }
	}

    @PostMapping("/payG-orderCreate")
    public ResponseEntity<?> payGOrderCreate (@RequestBody PayinDto data){
        ResponseEntity<?> response = this.clientService.payGorderCreate(data);
        return response;
    }
  
    /* =======================
    PHONEPE WEBHOOK (S2S)
 ======================= */
 @PostMapping("/webhook")
 public ResponseEntity<String> phonePeWebhook(
         @RequestHeader(value = "Authorization", required = false) String authorization,
         @RequestBody Map<String, Object> body
 ) {
	 
	 System.out.println(authorization+";;;;;;;;;;;;;;;;;;;;;;");

     try {
         /* =======================
            AUTH VERIFICATION
         ======================= */
         if (authorization == null) {
             logger.warn("PhonePe Webhook rejected: Missing Authorization header");
             return ResponseEntity.status(401).body("UNAUTHORIZED");
         }

         String raw = webhookUsername + ":" + webhookPassword;
         String expectedHash = sha256Hex(raw);
         String expectedAuthHeader = "SHA256(" + expectedHash + ")";
System.out.println(expectedAuthHeader+"ghjkjhgf");
         if (!authorization.equals(expectedAuthHeader)) {
             logger.warn("PhonePe Webhook rejected: Invalid Authorization");
             return ResponseEntity.status(401).body("UNAUTHORIZED");
         }

         logger.info("PhonePe Webhook authentication successful");

         /* =======================
            BASIC PAYLOAD CHECK
         ======================= */
         Object eventObj = body.get("event");
         Object payloadObj = body.get("payload");

         if (!(eventObj instanceof String) || !(payloadObj instanceof Map)) {
             logger.warn("PhonePe Webhook ignored: Invalid payload structure");
             return ResponseEntity.ok("IGNORED");
         }

         String event = (String) eventObj;
         Map<String, Object> payload = (Map<String, Object>) payloadObj;

         logger.info("PhonePe Webhook received | event={}", event);

         /* =======================
            EVENT FILTERING
         ======================= */
         if (!"checkout.order.completed".equals(event)) {
             logger.info("PhonePe Webhook ignored | unsupported event={}", event);
             return ResponseEntity.ok("IGNORED");
         }

         /* =======================
            BUSINESS FIELDS (SAFE)
         ======================= */
         String merchantOrderId = String.valueOf(payload.get("merchantOrderId"));
         String state = String.valueOf(payload.get("state"));

         logger.info("PhonePe Order Completed | orderId={} | state={}",
                 merchantOrderId, state);

         
         return ResponseEntity.ok("SUCCESS");

     } catch (Exception ex) {
         logger.error("PhonePe Webhook processing failed", ex);
         return ResponseEntity.status(500).body("ERROR");
     }
 }

 private String sha256Hex(String value) throws Exception {
	    MessageDigest md = MessageDigest.getInstance("SHA-256");
	    byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
	    StringBuilder sb = new StringBuilder();
	    for (byte b : digest) {
	        sb.append(String.format("%02x", b));
	    }
	    return sb.toString();
	}
 

    @GetMapping("token")
    public String generateToken (){
        System.out.println("generate Token");
        String token = Generator.generateBuckBoxToken();
        return token;
    }

    @GetMapping("dashboard-payin/{clientId}")
    public ResponseEntity<?> payinDashboard(@PathVariable String clientId) {
        ResponseEntity<?> response = this.clientService.payinDashboard(clientId);
        return response;
    }

    @GetMapping("settlement-list/{clientId}")
    public ResponseEntity<?> settlementByClientId(@PathVariable String clientId) {
        ResponseEntity<?> response = this.clientService.settlementByClientId(clientId);
        return response;
    }



//    @PostMapping("buckbox-payin")
//    public ResponseEntity<?> buckboxPayin (@RequestBody PayinRecords data) throws Exception {
//        System.out.println("generate Token");
//        ResponseEntity<?> response = this.clientService.buckBoxPayin(data);
//        return response;
//    }
    
    @GetMapping("/payin-reports/{userId}/excel")
    public ResponseEntity<?> downloadPayinReportsExcel(
            @PathVariable String userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        logger.info(
            "PAYIN EXCEL | userId={}, status={}, method={}, fromDate={}, toDate={}",
            userId, status, paymentMethod, fromDate, toDate
        );

        try {
            return clientService.downloadPayinReportsExcel(
                    userId, status, paymentMethod, fromDate, toDate
            );
        } catch (Exception ex) {
            logger.error("Error downloading payin excel | userId={}", userId, ex);
            return internalErrorResponse("downloadPayinReportsExcel", ex);
        }
    }
}
