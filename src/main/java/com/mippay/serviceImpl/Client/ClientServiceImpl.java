package com.mippay.serviceImpl.Client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mippay.dto.Admin.CollectionHistoryDto;
import com.mippay.dto.Admin.PayinDto;
import com.mippay.dto.Client.*;

import com.mippay.entity.Admin.Charges;
import com.mippay.entity.Admin.LockedFunds;
import com.mippay.entity.Admin.PayInCharges;
import com.mippay.entity.Admin.User;
import com.mippay.entity.Client.*;

import com.mippay.helper.AES256EncryptionGSM;
import com.mippay.helper.AESEncryptor;
import com.mippay.helper.Generator;
import com.mippay.helper.IpFetching;

import com.mippay.repository.Admin.ChargesRepository;
import com.mippay.repository.Admin.LockedFundsRepository;
import com.mippay.repository.Admin.PayInChargesRepository;
import com.mippay.repository.Admin.UserRepository;
import com.mippay.repository.Client.*;

import com.mippay.response.LocalCheckStatusResponse;
import com.mippay.response.PhonePeOrderStatusResponse;
import com.mippay.service.ClientService;
import com.mippay.service.EmailService;
import com.mippay.service.PhonePeAuthService;
import com.mippay.util.JWTHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {
	DecimalFormat df = new DecimalFormat("0.00");

	private static final Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);

	@Autowired
	private ClientRepository clientRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AuthenticationRepository authRepository;
	@Autowired
	private PayoutRepository payoutRepository;
	@Autowired
	private ChargesRepository chargesRepository;
	@Autowired
	private JWTHelper jwtUtil;
	@Autowired
	private PrefundRequestRepository prefundRequestRepository;
	@Autowired
	private Generator generator;
	@Autowired
	private CallBackRepository callBackRepository;
	@Autowired
	private EmailService emailService;
	@Autowired
	private WebhookRepository webhookRepository;
	@Autowired
	private IpRepository ipRepository;
	@Autowired
	private IpFetching ipFetching;
	@Autowired
	private LienRepository lienRepository;
	@Autowired
	private LienHistoryRepository lienHistoryRepository;

	@Autowired
	private PayinRecordRepository payinRepository;

	@Autowired
	private PayInChargesRepository payInChargesRepository;

	@Autowired
	private SupportTicketRepository ticketRepo;

	// Token expiration in milliseconds (24 hours)
	private final long TOKEN_EXPIRATION_MS = 86400000L;

	@Autowired
	private LockedFundsRepository lockedFundsRepository;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private  PhonePeAuthService authService;

    @Value("${phonepe.order-status-url}")
    private String orderStatusBaseUrl;

   
    @Autowired
    private PhonePeAuthService phonePeAuthService;

    private static final String PHONEPE_PAY_URL =
            "https://api-preprod.phonepe.com/apis/pg-sandbox/checkout/v2/pay";

    private static final String PHONEPE_STATUS_URL =
            "https://api-preprod.phonepe.com/apis/pg-sandbox/checkout/v2/order/";
    

	@Override
	public ResponseEntity<?> editProfile(String userId, ClientEditProfileDto editProfileDto) {
		Map<String, Object> response = new HashMap<>();

		try {
			logger.info("Starting profile edit for user ID: {}", userId);

			// Find existing client
			Optional<Client> clientOpt = clientRepository.findByUserId(userId);
			if (clientOpt.isEmpty()) {
				logger.warn("Client not found for user ID: {}", userId);
				response.put("success", false);
				response.put("message", "Client not found");
				response.put("errorCode", "CLIENT_NOT_FOUND");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}

			Client client = clientOpt.get();
			boolean isUpdated = false;
			boolean emailChanged = false;
			String oldEmail = client.getEmail();
			String newEmail = null;

			// Update name if provided
			if (editProfileDto.getName() != null && !editProfileDto.getName().trim().isEmpty()) {
				String newName = editProfileDto.getName().trim();
				if (!newName.equals(client.getName())) {
					client.setName(newName);
					isUpdated = true;
					logger.info("Name updated for user ID: {}", userId);
				}
			}

			// Check and update email if provided
			if (editProfileDto.getEmail() != null && !editProfileDto.getEmail().trim().isEmpty()) {
				newEmail = editProfileDto.getEmail().toLowerCase().trim();

				if (!newEmail.equals(client.getEmail())) {
					if (clientRepository.existsByEmailAndUserIdNot(newEmail, userId)) {
						logger.warn("Duplicate email attempted for user ID: {} - Email: {}", userId, newEmail);
						response.put("success", false);
						response.put("message", "Email already exists");
						response.put("errorCode", "DUPLICATE_EMAIL");
						return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
					}

					if (userRepository.existsByEmail(newEmail)) {
						logger.warn("Email already exists in Users table for user ID: {} - Email: {}", userId,
								newEmail);
						response.put("success", false);
						response.put("message", "Email already exists");
						response.put("errorCode", "DUPLICATE_EMAIL_USER");
						return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
					}

					client.setEmail(newEmail);
					isUpdated = true;
					emailChanged = true;
					logger.info("Email updated for user ID: {} from {} to {}", userId, oldEmail, newEmail);
				}
			}

			// Check and update mobile number if provided
			if (editProfileDto.getMobileNum() != null && !editProfileDto.getMobileNum().trim().isEmpty()) {
				String newMobileNum = editProfileDto.getMobileNum().trim();

				if (!newMobileNum.equals(client.getMobileNum())) {
					if (clientRepository.existsByMobileNumAndUserIdNot(newMobileNum, userId)) {
						logger.warn("Duplicate mobile number attempted for user ID: {} - Mobile: {}", userId,
								newMobileNum);
						response.put("success", false);
						response.put("message", "Mobile number already exists");
						response.put("errorCode", "DUPLICATE_MOBILE");
						return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
					}
					client.setMobileNum(newMobileNum);
					isUpdated = true;
					logger.info("Mobile number updated for user ID: {}", userId);
				}
			}

			// Update password if provided
			if (editProfileDto.getPassword() != null && !editProfileDto.getPassword().trim().isEmpty()) {
				String encodedPassword = passwordEncoder.encode(editProfileDto.getPassword());
				client.setPassword(encodedPassword);
				isUpdated = true;
				logger.info("Password updated for user ID: {}", userId);

				// Sync password in Users table also
				String lookupEmail = emailChanged ? oldEmail : client.getEmail();
				Optional<User> userOpt = userRepository.findByEmail(lookupEmail);

				if (userOpt.isPresent()) {
					User user = userOpt.get();
					user.setPassword(encodedPassword);
					userRepository.save(user);
					logger.info("User table password updated for email: {}", client.getEmail());
				} else {
					logger.warn("User not found in Users table for syncing password - email: {}", client.getEmail());
				}
			}

			if (!isUpdated) {
				response.put("success", true);
				response.put("message", "No changes detected");
				response.put("data", buildClientResponseData(client));
				return ResponseEntity.ok(response);
			}

			Client updatedClient = clientRepository.save(client);

			// Update Users table if email changed
			if (emailChanged) {
				Optional<User> userOpt = userRepository.findByEmail(oldEmail);
				if (userOpt.isPresent()) {
					User user = userOpt.get();
					user.setEmail(newEmail);
					userRepository.save(user);
					logger.info("User table email updated from {} to {} for user ID: {}", oldEmail, newEmail, userId);
				} else {
					logger.warn("User not found with email {} for user ID: {}", oldEmail, userId);
				}
			}

			logger.info("Profile updated successfully for user ID: {}", userId);

			response.put("success", true);
			response.put("message",
					"Profile updated successfully" + (emailChanged ? ". Please use new email for next login." : ""));
			response.put("data", buildClientResponseData(updatedClient));

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.error("Error during profile update for user ID: {}", userId, e);
			response.put("success", false);
			response.put("message", "Internal server error during profile update");
			response.put("errorCode", "INTERNAL_ERROR");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	public ResponseEntity<?> paymentPayout(PayoutDto data, String clientId, String clientSecretId,
			HttpServletRequest req) throws Exception {
		String orderId = Generator.generateRandomTranId(8);
		data.setOrderId(orderId);
		// Authentication of client
		Boolean authenticated = this.isAuthenticated(clientId, clientSecretId, data.getUserId());
		System.out.println("Is Authenticated: " + (authenticated ? "success" : "failed"));
		/*********** checking user_id in client Records *******/
		Optional<Client> client = this.clientRepository.findByUserId(data.getUserId());
		if (client.isEmpty()) {
			ResponseDto resp = ResponseDto.builder().message("Error").status("BAD_REQUEST")
					.data("Please enter valid client-Id").build();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
		}

		/************* Checking for Ip whitelisted or not **********/
		String ip = ipFetching.getClientIP(req);
		System.out.println("ip: " + ip);
		Optional<IpAddress> ipentity = this.ipRepository.findByUserId(data.getUserId());
		if (ipentity.isEmpty() || !ip.equals(ipentity.get().getIpAddress())) {
			System.out.println("inside IP not Matched" + ipentity);
			ResponseDto resp = ResponseDto.builder().status("BAD_REQUEST").message("ERROR")
					.data("Ip not whitelisted..!").build();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
		}
		System.out.println("Inside Ip: " + ipentity.get());
		/*********** checking client_id, client_secret and Active/inActive **********/
		if (!authenticated) {
			ResponseDto resp = ResponseDto.builder().message("Error").status("BAD_REQUEST")
					.data("Authentication failed").build();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
		}

		String pgId = authRepository.getPgIdByClientId(data.getUserId());
		System.out.println("pgId: " + pgId);
		if (pgId == null || pgId.isEmpty()) {
			ResponseDto resp = ResponseDto.builder().message("Error").status("Internal processing error")
					.data("Internal processing error").build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
		}

		PayoutRecords deductionProcess = new PayoutRecords();
		/*************** Checking for Duplicate Cust_uniq_ref ***************/
		Optional<PayoutRecords> trxnId = Optional.ofNullable(this.payoutRepository.findByOrderId(data.getOrderId()));
		if (trxnId.isPresent()) {
			ResponseDto resp = ResponseDto.builder().status("BAD_REQUEST").message("Error")
					.data("Duplicate Order_id, Please try again").build();
			return ResponseEntity.badRequest().body(resp);
		}

		// Charges calculation
		Map<String, Object> calculation = this.payoutChargesCalculations(data);
		System.out.println("Calculations: " + calculation);
		if (calculation.get("charges").equals(0)) {
			ResponseDto resp = ResponseDto.builder().status("BAD_REQUEST").message("Error")
					.data("Please contact admin to set the charges..!").build();
			return ResponseEntity.badRequest().body(resp);
		}

		// Deduction Process
		synchronized (this) {
			deductionProcess = this.msPayoutDeductionProcess(data, calculation, pgId);
			System.out.println("deductionProcess: " + deductionProcess);

			if (deductionProcess.getStatus().equals("Insufficient Funds..!")) {
				ResponseDto resp = ResponseDto.builder().status("BAD_REQUEST").message("Error")
						.data("Insufficient Funds..!").build();
				return ResponseEntity.badRequest().body(resp);
			}
		}
		JSONObject response = this.payoutImplementation(data, deductionProcess);
		System.out.println("response: " + response);
		if (response.get("message").equals("Transfer initiated successfully")) {
			deductionProcess.setStatus("PENDING");
			JSONObject json2 = response.getJSONObject("data");
			this.payoutRepository.updateTransactionId(json2.get("transaction_id").toString(),
					response.get("timestamp").toString(), data.getOrderId());
		}
		if (response.get("message").equals("Insufficient balance")) {
			deductionProcess.setStatus("FAILED");
			this.refundClient(data.getOrderId());
			System.out.println("Refund completed successfully..!");
			this.payoutRepository.updateStatus("FAILED", "TXNF", "Refund Completed", null, data.getOrderId());
			System.out.println("Status Updated as Failed in Payout Table");
		}
		return ResponseEntity.ok(deductionProcess);
	}

	private JSONObject payoutImplementation(PayoutDto data, PayoutRecords deductionProcess) throws Exception {

		logger.info("Starting payoutImplementation for orderId: {}", data.getOrderId());

		Optional<Client> client = this.clientRepository.findByUserId(data.getUserId());
		if (client.isEmpty()) {
			logger.error("Client not found for userId: {}", data.getUserId());
			throw new Exception("Client not found");
		}

		// ---------- SKIPPING THIRD-PARTY API CALL ----------
		logger.warn("Third-party payout API is DISABLED. Proceeding with local DB save only.");

		// Save payout record into DB (your existing repository)
		deductionProcess.setOrderId(data.getOrderId());
		deductionProcess.setUserId(data.getUserId());
		deductionProcess.setAmount(Double.parseDouble(data.getAmount()));
		deductionProcess.setStatus("PENDING"); // or "PENDING", based on your logic
		deductionProcess.setTransferMode(data.getTransferMode());
		deductionProcess.setCreatedDate(LocalDateTime.now());

		payoutRepository.save(deductionProcess);

		logger.info("Payout record saved locally for orderId: {}", data.getOrderId());

		// Return mock JSON response so your flow does NOT break
		JSONObject mockResponse = new JSONObject();
		mockResponse.put("status", true);
		mockResponse.put("message", "Payout simulated successfully. No API call made.");
		mockResponse.put("order_id", data.getOrderId());
		mockResponse.put("amount", data.getAmount());

		return mockResponse;
	}

	@Transactional
	private PayoutRecords msPayoutDeductionProcess(PayoutDto data, Map<String, Object> calculation, String pgId) {

		logger.info("Starting msPayoutDeductionProcess for orderId: {}, userId: {}", data.getOrderId(),
				data.getUserId());

		PayoutRecords clientPayoutResponse = new PayoutRecords();
		DecimalFormat df = new DecimalFormat("###.##");

		String mode = data.getTransferMode();
		if (mode == null) {
			mode = "IMPS";
			logger.debug("Transfer mode not provided, defaulting to IMPS.");
		}

		BigDecimal totalAmount = new BigDecimal(calculation.get("finalAmount").toString()).setScale(2,
				RoundingMode.HALF_UP);

		BigDecimal charges = new BigDecimal(calculation.get("charges").toString()).setScale(2, RoundingMode.HALF_UP);

		logger.info("Total amount to deduct: {}, Charges: {}", totalAmount, charges);

		int updatedRows = this.clientRepository.updateBalance1(data.getUserId(), totalAmount.doubleValue());

		if (updatedRows == 0) {
			logger.warn("Insufficient funds for userId: {} | Required: {}", data.getUserId(), totalAmount);

			clientPayoutResponse.setStatus("Insufficient Funds..!");
			return clientPayoutResponse;
		}

		BigDecimal updatedBalance = BigDecimal.valueOf(clientRepository.getWalletBalance(data.getUserId()));
		logger.debug("Updated wallet balance for userId {}: {}", data.getUserId(), updatedBalance);

		BigDecimal currentBalance = updatedBalance.add(totalAmount);
		logger.debug("Calculated current balance for userId {}: {}", data.getUserId(), currentBalance);

		// Set Payout Record Fields
		clientPayoutResponse.setUserId(data.getUserId());
		clientPayoutResponse.setCurrentBalance(currentBalance.doubleValue());
		clientPayoutResponse.setAmount(Double.parseDouble(data.getAmount()));
		clientPayoutResponse.setAccNumber(data.getBankAccount());
		clientPayoutResponse.setOrderId(data.getOrderId());
		clientPayoutResponse.setName(data.getName());
		clientPayoutResponse.setNumber(data.getPhone());
		clientPayoutResponse.setIfsc(data.getIfsc());
		clientPayoutResponse.setEmail(data.getEmail());
		clientPayoutResponse.setTransferMode(mode);

		clientPayoutResponse.setCharges(charges.doubleValue());
		clientPayoutResponse.setGstCharges(Double.parseDouble(df.format(calculation.get("gstCharges"))));
		clientPayoutResponse.setStatus("INPROGRESS");
		clientPayoutResponse.setStatusCode("TXNP");
		clientPayoutResponse.setPgId(pgId);
		clientPayoutResponse.setFinalAmount(Double.parseDouble(df.format(totalAmount)));
		clientPayoutResponse.setUpdatedBalance(Double.parseDouble(df.format(updatedBalance)));

		logger.info("Saving payout record for orderId: {} | userId: {}", data.getOrderId(), data.getUserId());

		this.payoutRepository.save(clientPayoutResponse);

		logger.info("Payout record saved successfully for orderId: {}", data.getOrderId());

		return clientPayoutResponse;
	}

	private Map<String, Object> payoutChargesCalculations(PayoutDto data) {

		logger.info("Starting payoutChargesCalculations for userId: {}, amount: {}", data.getUserId(),
				data.getAmount());

		Map<String, Object> map = new HashMap<>();
		double gstPercent = 18; // not used currently – log kept for reference

		Double amount = Double.parseDouble(data.getAmount());

		List<Charges> cltCharges = this.chargesRepository.fetchByClientIdAndRange(data.getUserId(), amount, amount);

		logger.debug("Fetched charges list for userId {}: {}", data.getUserId(), cltCharges);

		// No charges found
		if (cltCharges.isEmpty()) {
			logger.warn("No charges configured for userId: {} within amount range: {}", data.getUserId(), amount);

			map.put("gstCharges", 0);
			map.put("finalAmount", 0);
			map.put("charges", 0);
			map.put("totalCharges", 0);
			return map;
		}

		Charges first = cltCharges.get(0);
		logger.debug("Applicable charge configuration: {}", first);

		double calcCharges = 0.00;

		if ("Amount".equalsIgnoreCase(first.getChargesType())) {
			calcCharges = first.getCharges();
			logger.debug("Charge type: Amount | Charges applied: {}", calcCharges);
		}

		if ("Percentage".equalsIgnoreCase(first.getChargesType())) {
			calcCharges = (first.getCharges() / 100) * amount;
			logger.debug("Charge type: Percentage | Rate: {}% | Charges applied: {}", first.getCharges(), calcCharges);
		}

		double gstCharges = 0.00; // You have not yet applied GST, so keeping this static
		double finalAmount = calcCharges + amount + gstCharges;

		logger.info("Charges calculated for userId {}: amount={}, charges={}, gst={}, finalAmount={}", data.getUserId(),
				amount, calcCharges, gstCharges, finalAmount);

		map.put("gstCharges", gstCharges);
		map.put("finalAmount", finalAmount);
		map.put("charges", calcCharges);
		map.put("totalCharges", gstCharges + calcCharges);

		return map;
	}

	public Boolean isAuthenticated(String clientId, String clientSecret, String userId) {
		logger.info("Authenticating request for userId: {}", userId);
		Authentication records = this.authRepository.findByCredientials(clientId, clientSecret, userId);
		Optional<Client> client = clientRepository.findByUserId(userId);
		if (client.isEmpty()) {
			logger.warn("Authentication failed: No client found for userId {}", userId);
			return false;
		}
		logger.debug("Client record found: {}", client.get());
		String status = client.get().getStatus();
		if (records != null && "ACTIVE".equalsIgnoreCase(status)) {
			logger.info("Authentication successful for userId {}", userId);
			return true;
		}
		logger.warn("Authentication failed for userId {} | Status: {} | Credentials Found: {}", userId, status,
				(records != null));
		return false;
	}

	private Map<String, Object> buildClientResponseData(Client client) {
		logger.info("Building client response data for userId: {}", client.getUserId());

		Map<String, Object> clientData = new HashMap<>();
		clientData.put("userId", client.getUserId());
		clientData.put("name", client.getName());
		clientData.put("email", client.getEmail());
		clientData.put("mobileNum", client.getMobileNum());
		clientData.put("status", client.getStatus());
		clientData.put("accountNum", client.getAccountNum());
		clientData.put("ifscCode", client.getIfscCode());
		clientData.put("gst", client.getGst());
		clientData.put("cin", client.getCin());
		clientData.put("accountBal", client.getAccountBal());
		clientData.put("createdDate", client.getCreatedDate());
		clientData.put("updatedDate", client.getUpdatedDate());
		logger.debug("Client response data built: {}", clientData);
		return clientData;
	}

	@Override
	public ResponseEntity<?> getAllPayoutRecordsReport(LocalDate fromDate, LocalDate toDate) {
		Map<String, Object> response = new HashMap<>();
		try {
			logger.info("Fetching all payout records report from {} to {}", fromDate, toDate);
			if (fromDate.isAfter(toDate)) {
				response.put("success", false);
				response.put("message", "From date cannot be after to date");
				response.put("errorCode", "INVALID_DATE_RANGE");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
			LocalDateTime fromDateTime = fromDate.atStartOfDay();
			LocalDateTime toDateTime = toDate.atTime(23, 59, 59);
			List<PayoutRecords> payoutRecords = payoutRepository.findAllPayoutRecordsBetweenDates(fromDateTime,
					toDateTime);
			List<Map<String, Object>> reportData = payoutRecords.stream().map(this::buildPayoutRecordResponseData)
					.collect(Collectors.toList());
			response.put("success", true);
			response.put("message", "Payout records report fetched successfully");
			response.put("data", reportData);
			response.put("fromDate", fromDate.toString());
			response.put("toDate", toDate.toString());
			response.put("totalRecords", reportData.size());
			logger.info("Successfully fetched {} payout records for date range {} to {}", reportData.size(), fromDate,
					toDate);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			logger.error("Error fetching all payout records report from {} to {}", fromDate, toDate, e);
			response.put("success", false);
			response.put("message", "Internal server error while fetching payout records report");
			response.put("errorCode", "INTERNAL_ERROR");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	public ResponseEntity<?> getPayoutRecordsByUserIdReport(LocalDate fromDate, LocalDate toDate, String userId) {
		Map<String, Object> response = new HashMap<>();
		try {
			logger.info("Fetching payout records report for userId {} from {} to {}", userId, fromDate, toDate);
			if (userId == null || userId.trim().isEmpty()) {
				response.put("success", false);
				response.put("message", "User ID is required");
				response.put("errorCode", "MISSING_USER_ID");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
			Optional<Client> clientOpt = clientRepository.findByUserId(userId);
			if (clientOpt.isEmpty()) {
				response.put("success", false);
				response.put("message", "Client not found for the provided user ID");
				response.put("errorCode", "CLIENT_NOT_FOUND");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}
			if (fromDate.isAfter(toDate)) {
				response.put("success", false);
				response.put("message", "From date cannot be after to date");
				response.put("errorCode", "INVALID_DATE_RANGE");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
			LocalDateTime fromDateTime = fromDate.atStartOfDay();
			LocalDateTime toDateTime = toDate.atTime(23, 59, 59);
			List<PayoutRecords> payoutRecords = payoutRepository.findPayoutRecordsByUserIdAndDateRange(userId,
					fromDateTime, toDateTime);
			List<Map<String, Object>> reportData = payoutRecords.stream().map(this::buildPayoutRecordResponseData)
					.collect(Collectors.toList());
			Client client = clientOpt.get();
			Map<String, Object> clientInfo = new HashMap<>();
			clientInfo.put("userId", client.getUserId());
			clientInfo.put("name", client.getName());
			clientInfo.put("email", client.getEmail());
			clientInfo.put("mobileNum", client.getMobileNum());
			clientInfo.put("status", client.getStatus());
			clientInfo.put("currentBalance", client.getAccountBal());
			response.put("success", true);
			response.put("message", "Payout records report fetched successfully for user");
			response.put("data", reportData);
			response.put("clientInfo", clientInfo);
			response.put("fromDate", fromDate.toString());
			response.put("toDate", toDate.toString());
			response.put("totalRecords", reportData.size());
			logger.info("Successfully fetched {} payout records for userId {} for date range {} to {}",
					reportData.size(), userId, fromDate, toDate);
			return ResponseEntity.ok(response);
		} catch (NumberFormatException e) {
			logger.error("Invalid userId format: {}", userId, e);
			response.put("success", false);
			response.put("message", "Invalid user ID format");
			response.put("errorCode", "INVALID_USER_ID_FORMAT");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		} catch (Exception e) {
			logger.error("Error fetching payout records report for userId {} from {} to {}", userId, fromDate, toDate,
					e);
			response.put("success", false);
			response.put("message", "Internal server error while fetching payout records report");
			response.put("errorCode", "INTERNAL_ERROR");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	public ResponseEntity<?> getChargesByUserId(String userId) {
		logger.info("Fetching charges for userId: {}", userId);
		Map<String, Object> map = new HashMap<>();
		try {
			List<Charges> chargesList = this.chargesRepository.fetchByUserId(userId);
			logger.debug("Charges fetched for userId {}: {}", userId, chargesList);
			map.put("status", "Success");
			map.put("statusCode", "200");

			if (chargesList.isEmpty()) {
				logger.warn("No charges found for userId: {}", userId);
				map.put("data", "No data found..!");
			} else {
				map.put("data", chargesList);
			}
			return ResponseEntity.ok(map);

		} catch (Exception ex) {
			logger.error("Error while fetching charges for userId {}: {}", userId, ex.getMessage(), ex);
			map.put("status", "Failed");
			map.put("statusCode", "500");
			map.put("data", "Internal Server Error");
			return ResponseEntity.status(500).body(map);
		}
	}

	@Override
	public ResponseEntity<?> createPrefundRequest(PrefundDto request) {

		logger.info("Received prefund request for userId: {}, reference: {}", request.getUserId(),
				request.getReference());

		try {

			// 1️⃣ Reference duplication check
			if (request.getReference() != null && !request.getReference().isBlank()) {

				Optional<PrefundRequest> existing = prefundRequestRepository.findByReference(request.getReference());

				if (existing.isPresent()) {
					logger.warn("Prefund request rejected: Reference already exists: {}", request.getReference());

					return ResponseEntity.badRequest().body("Reference already exists: " + request.getReference());
				}
			}

			String clientName = clientRepository.findClientNameByUserId(request.getUserId());

			if (clientName == null) {
				logger.warn("Prefund request rejected: Client not found | userId={}", request.getUserId());

				return ResponseEntity.badRequest().body("Client not found for userId: " + request.getUserId());
			}

			// 3️⃣ Create PrefundRequest entity
			PrefundRequest prefundRequest = new PrefundRequest();
			prefundRequest.setAmount(request.getAmount());
			prefundRequest.setUserId(request.getUserId());
			prefundRequest.setReference(request.getReference());
			prefundRequest.setStatus("PENDING");
			prefundRequest.setRequestedDate(LocalDateTime.now());
			prefundRequest.setApprovedDate(null);
			prefundRequest.setAdminAccNum(request.getFromAccount());
			prefundRequest.setClientAccNum(request.getToAccount());
			prefundRequest.setPaymentMethod(request.getPaymentMethod());
			prefundRequest.setAdminIfsc(request.getAdminIfsc());

			prefundRequest.setName(clientName);

			// 4️⃣ Save
			prefundRequestRepository.save(prefundRequest);

			logger.info("Prefund request created successfully | userId={}, name={}, reference={}", request.getUserId(),
					clientName, request.getReference());

			return ResponseEntity.ok("Prefund Request sent successfully..!");

		} catch (Exception ex) {

			logger.error("Error while creating prefund request | userId={} | reason={}", request.getUserId(),
					ex.getMessage(), ex);

			return ResponseEntity.status(500).body("Something went wrong while creating the prefund request.");
		}
	}

	@Override
	public String clientOnboard(Client data) {

		logger.info("Starting client onboarding process for email: {}", data.getEmail());

		try {
			// Generate client ID
			String clientId = this.generator.generateClientId();
			String encPass = this.passwordEncoder.encode(data.getPassword());

			data.setUserId(clientId);
			data.setPassword(encPass);

			logger.debug("Generated clientId: {} for email: {}", clientId, data.getEmail());

			// Check if email already exists in Client or Admin tables
			Optional<Client> existingClient = this.clientRepository.findByEmail(data.getEmail());
			Optional<User> existingAdmin = this.userRepository.findByEmail(data.getEmail());

			if (existingClient.isPresent() || existingAdmin.isPresent()) {
				logger.warn("Onboarding failed: Email already exists in system: {}", data.getEmail());
				return "Given email is already present";
			}
			this.clientRepository.save(data);
			logger.info("Client onboarded successfully | userId: {}, email: {}", clientId, data.getEmail());
			logger.debug("Saved Client entity: {}", data);
			return "client onboarded successfully..!";
		} catch (Exception ex) {
			logger.error("Error during client onboarding for email {}: {}", data.getEmail(), ex.getMessage(), ex);
			return "Something went wrong during onboarding.";
		}
	}

	private void sendCallBackToClient(CallBack value) {

		RestTemplate restTemplate = new RestTemplate();
		String clientId = this.payoutRepository.findByOrderId(value.getOrderId()).getUserId();
		logger.info("Preparing to send callback to client. orderId={}, clientId={}", value.getOrderId(), clientId);
		String status = null, statusCode = null, refundStatus = null;
		if (value.getStatus().equals("COMPLETED") || value.getStatus().equals("SUCCESS")) {
			status = "SUCCESS";
			statusCode = "TXNS";
		} else if (value.getStatus().equals("FAILED_REVERSED")) {
			status = "FAILED";
			statusCode = "TXNF";
			refundStatus = "Refund Completed";
		} else {
			status = "PENDING";
			statusCode = "TXNP";
		}
		if (!clientId.isBlank() || !clientId.isEmpty() || clientId != null) {
			String url = this.webhookRepository.findByUserIdAndUrl(clientId).get().getUrl();
			logger.info("Client webhook URL resolved: {}", url);
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");
			Map<String, String> map = new HashMap<>();
			map.put("transactionId", value.getTransactionId());
			map.put("orderId", value.getOrderId());
			map.put("description", value.getDescription());
			map.put("utr", value.getUtr());
			map.put("paymentType", value.getPaymentType());
			map.put("amount", value.getAmount());
			map.put("status", status);
			map.put("statusCode", statusCode);
			map.put("refundStatus", refundStatus);
			logger.debug("Callback payload: {}", map);
			HttpEntity entity = new HttpEntity(map, headers);
			try {
				ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
				logger.info("Callback sent successfully for orderId {} | Response: {}", value.getOrderId(),
						response.getBody());
			} catch (HttpClientErrorException e) {
				logger.error("Error while sending callback for orderId {} | ResponseBody: {}", value.getOrderId(),
						e.getResponseBodyAsString());
				JSONObject jsonErr = new JSONObject(e.getResponseBodyAsString());
				logger.error("Parsed error response: {}", jsonErr);
				logger.error("Failed to send callback for clientId {}", clientId);
			}
		}
	}

	private void refundClient(Object orderId) {
		Optional<PayoutRecords> payout = Optional.ofNullable(this.payoutRepository.findByOrderId(orderId.toString()));
		Optional<Client> client = this.clientRepository.findByUserId(payout.get().getUserId());
		synchronized (this) {
			if (client.isPresent()) {
				double amount = payout.get().getFinalAmount();
				logger.info("Initiating refund for userId={} | amount={} | orderId={}", payout.get().getUserId(),
						amount, orderId);
				int updated = this.clientRepository.updateWallet(amount, payout.get().getUserId());
				if (updated == 0) {
					logger.error("Wallet refund FAILED for userId={} | amount={}", payout.get().getUserId(), amount);
				}
				BigDecimal updatedBalance = BigDecimal
						.valueOf(clientRepository.getWalletBalance(payout.get().getUserId()));
				double oldBal = updatedBalance.doubleValue() - amount;
				logger.debug("Old Wallet Balance for userId {}: {}", payout.get().getUserId(), oldBal);
				BigDecimal newBal = updatedBalance;
				logger.debug("New Wallet Balance for userId {}: {}", payout.get().getUserId(), newBal);
				logger.info("Wallet refund COMPLETED for userId={} | amount={}", payout.get().getUserId(), amount);
			}
		}
	}

	@Override
	public ResponseEntity<?> prefundListByClientId(String clientId, int page, int size) {

	    logger.info("Fetching prefund request list for clientId: {}, page={}, size={}",
	            clientId, page, size);

	    Pageable pageable = PageRequest.of(page, size, Sort.by("requested_date").descending());

	    Page<Map<String, Object>> listPage =
	            this.prefundRequestRepository.findByClientIdWithPagination(clientId, pageable);

	    if (listPage.hasContent()) {

	        logger.info("Prefund records found for clientId: {} | Count: {}",
	                clientId, listPage.getNumberOfElements());

	        ResponseDto response = ResponseDto.builder()
	                .status("OK")
	                .message("SUCCESS")
	                .data(listPage.getContent())
	                .build();

	        return ResponseEntity.ok(response);
	    } else {

	        logger.warn("No prefund records found for clientId: {}", clientId);

	        ResponseDto response = ResponseDto.builder()
	                .status("Ok")
	                .message("SUCCESS")
	                .data("No data present in the list..!")
	                .build();

	        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
	    }
	}


	@Override
	public ResponseEntity<?> transactionRecordsByClientId(String clientId, int page, int size) {

	    logger.info("Fetching transaction records for clientId: {}, page={}, size={}",
	            clientId, page, size);

	    Pageable pageable = PageRequest.of(
	            page,
	            size,
	            Sort.by(Sort.Direction.DESC, "sl_no")   
	    );

	    Page<PayoutRecords> transactionsPage =
	            payoutRepository.findByClientIForClient(clientId, pageable);

	    if (transactionsPage.hasContent()) {

	        logger.info("Transaction records found for clientId: {} | Count: {}",
	                clientId, transactionsPage.getNumberOfElements());

	        ResponseDto response = ResponseDto.builder()
	                .status("OK")
	                .message("SUCCESS")
	                .data(transactionsPage.getContent())
	                .build();

	        return ResponseEntity.ok(response);
	    } else {

	        logger.warn("No transaction records found for clientId: {}", clientId);

	        ResponseDto response = ResponseDto.builder()
	                .status("OK")
	                .message("NO_CONTENT")
	                .data("No records found..!")
	                .build();

	        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
	    }
	}

	@Override
	public ResponseEntity<?> allTrasactionCountAndAmount(String clientId) {
		logger.info("Fetching transaction count & amount summary for clientId: {}", clientId);
		Map<String, Object> map = new HashMap<>();
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String formatted = sdf.format(date);
		System.out.println("date: " + formatted);
		String successCount = "0", successAmount = "0", pendingAmount = "0", pendingCount = "0", failAmount = "0",
				failCount = "0";
		List<Map<String, Object>> records = this.payoutRepository.transactionCountAndAmounByClientIdt(clientId,
				formatted);
		logger.debug("Raw transaction summary records for clientId {}: {}", clientId, records);
		if (records.size() > 0) {
			for (int i = 0; i < records.size(); i++) {
				Map<String, Object> value = records.get(i);
				String status = value.get("status").toString();
				logger.debug("Processing status={} summary: {}", status, value);
				if (status.equals("SUCCESS")) {
					successCount = value.get("count").toString();
					successAmount = value.get("amount").toString();
				} else if (status.equals("PENDING")) {
					pendingCount = value.get("count").toString();
					pendingAmount = value.get("amount").toString();
				} else {
					failCount = value.get("count").toString();
					failAmount = value.get("amount").toString();
				}
			}
			map.put("successCount", successCount);
			map.put("successAmount", successAmount);
			map.put("pendingCount", pendingCount);
			map.put("pendingAmount", pendingAmount);
			map.put("failCount", failCount);
			map.put("failAmount", failAmount);
			logger.info("Transaction summary generated for clientId {}: {}", clientId, map);
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(map).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("No transaction summary found for clientId: {}", clientId);
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(map).build();
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}
	}

	@Override
	public ResponseEntity<?> allTransactionCountAndAmountClientYearMonth(String clientId) {

	    logger.info("Fetching client year-month wise transaction summary | clientId={}", clientId);

	    List<Map<String, Object>> records =
	            payoutRepository.transactionCountAndAmountClientYearMonth(clientId);

	    Map<String, Map<String, Object>> result = new LinkedHashMap<>();

	    for (Map<String, Object> row : records) {

	        String year  = row.get("year").toString();
	        String month = row.get("month").toString();
	        String key   = year + "-" + String.format("%02d", Integer.parseInt(month));

	        result.putIfAbsent(key, new HashMap<>());

	        Map<String, Object> data = result.get(key);

	        // default values
	        data.putIfAbsent("successCount", "0");
	        data.putIfAbsent("successAmount", "0");
	        data.putIfAbsent("pendingCount", "0");
	        data.putIfAbsent("pendingAmount", "0");
	        data.putIfAbsent("failCount", "0");
	        data.putIfAbsent("failAmount", "0");

	        String status = row.get("status").toString();

	        if ("SUCCESS".equals(status)) {
	            data.put("successCount", row.get("count").toString());
	            data.put("successAmount", row.get("amount").toString());
	        } else if ("PENDING".equals(status)) {
	            data.put("pendingCount", row.get("count").toString());
	            data.put("pendingAmount", row.get("amount").toString());
	        } else {
	            data.put("failCount", row.get("count").toString());
	            data.put("failAmount", row.get("amount").toString());
	        }
	    }

	    ResponseDto response = ResponseDto.builder()
	            .status("OK")
	            .message("SUCCESS")
	            .data(result)
	            .build();

	    return ResponseEntity.ok(response);
	}

	
	@Override
	public ResponseEntity<?> sendOtp(String email) throws Exception {
		logger.info("Received OTP request for email: {}", email);
		Optional<Client> client = clientRepository.findByEmail(email);
		String otp = Generator.generateRandomTranId(6);
		logger.debug("Generated OTP: {}", otp); // safe because you are emailing it anyway
		if (client.isPresent()) {
			logger.info("Client found for email: {}. Sending OTP...", email);
			this.emailService.changePassword(email, otp);
			this.clientRepository.saveOtpInClient(AESEncryptor.encryptOtp(otp), email);
			logger.info("OTP sent & saved successfully for email: {}", email);
			return ResponseEntity.ok("Otp sent successfully");
		}
		logger.warn("OTP request failed — No client found for email: {}", email);
		return ResponseEntity.badRequest().body("Please enter the registered email..!");
	}

	@Override
	public ResponseEntity<?> verifyOtp(EmailOtpDto emailOtpDto) throws Exception {
		logger.info("Verifying OTP for email: {}", emailOtpDto.getEmail());
		Optional<Client> client = this.clientRepository.findByEmail(emailOtpDto.getEmail());
		if (client.isPresent()) {
			logger.debug("Client found for email: {}", emailOtpDto.getEmail());
			String decryptedOtp = AESEncryptor.decryptOtp(client.get().getOtp());
			logger.debug("Decrypted OTP from DB for email {}: {}", emailOtpDto.getEmail(), decryptedOtp);
			if (emailOtpDto.getOtp().equals(decryptedOtp)) {
				logger.info("OTP verified successfully for email: {}", emailOtpDto.getEmail());
				String encPass = this.passwordEncoder.encode(emailOtpDto.getPassword());
				this.clientRepository.updatePassword(encPass, emailOtpDto.getEmail());
				logger.info("Password updated successfully for email: {}", emailOtpDto.getEmail());
				return ResponseEntity.ok("Otp verified and Password updated successfully..!!");
			}
			logger.warn("Invalid OTP provided for email: {}", emailOtpDto.getEmail());
		} else {
			logger.warn("OTP verification failed — no client found for email: {}", emailOtpDto.getEmail());
		}
		return ResponseEntity.badRequest().body("Invalid otp !!");
	}

	@Override
	public ResponseEntity<?> changePassword(EmailOtpDto emailOtpDto) {
		logger.info("Password change request received for email: {}", emailOtpDto.getEmail());
		Optional<Client> client = this.clientRepository.findByEmail(emailOtpDto.getEmail());
		if (client.isPresent()) {
			logger.info("Client found for email: {}. Updating password...", emailOtpDto.getEmail());
			String encPass = this.passwordEncoder.encode(emailOtpDto.getPassword());
			this.clientRepository.updatePassword(encPass, emailOtpDto.getEmail());
			logger.info("Password updated successfully for email: {}", emailOtpDto.getEmail());
			return ResponseEntity.ok("Password updated successfully..!!");
		}
		logger.warn("Password change failed — invalid emailId provided: {}", emailOtpDto.getEmail());
		return ResponseEntity.badRequest().body("Invalid emailId !!");
	}

	@Override
	public ResponseEntity<?> prefundFilterByClientId(Map<String, Object> data) {
		String fromDate = data.get("fromDate").toString();
		String toDate = data.get("toDate").toString();
		String clientId = data.get("clientId").toString();
		logger.info("Filtering prefund requests for clientId={} | fromDate={} | toDate={}", clientId, fromDate, toDate);
		List<Map<String, Object>> list = this.prefundRequestRepository.prefundFilterByDateAndClientId(fromDate, toDate,
				clientId);
		logger.debug("Prefund filtered result size: {} | data: {}", list.size(), list);
		if (list.size() > 0) {
			logger.info("Prefund records found for clientId={} in the given date range.", clientId);
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("No prefund records found for clientId={} in date range {} to {}", clientId, fromDate, toDate);
			ResponseDto response = ResponseDto.builder().status("BAD_REQUEST").message("ERROR")
					.data("No records found for the given date....!").build();
			return ResponseEntity.badRequest().body(response);
		}
	}

	@Override
	public ResponseEntity<?> dailyCountAndAmount() {
		logger.info("Fetching transaction count & amount summary: {}");
		Map<String, Object> map = new HashMap<>();
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String formatted = sdf.format(date);
		System.out.println("date: " + formatted);
		String successCount = "0", successAmount = "0", pendingAmount = "0", pendingCount = "0", inprogressCount = "0",
				inprogressAmount = "0", failAmount = "0", failCount = "0";
		List<Map<String, Object>> records = this.payoutRepository.transactionCountAndAmoun(formatted);
		logger.debug("Raw transaction summary records: {}", records);
		if (records.size() > 0) {
			for (int i = 0; i < records.size(); i++) {
				Map<String, Object> value = records.get(i);
				String status = value.get("status").toString();
				logger.debug("Processing status={} summary: {}", status, value);
				if (status.equals("SUCCESS")) {
					successCount = value.get("count").toString();
					successAmount = value.get("amount").toString();
				} else if (status.equals("PENDING")) {
					pendingCount = value.get("count").toString();
					pendingAmount = value.get("amount").toString();
				} else if (status.equals("INPROGRESS")) {
					inprogressCount = value.get("count").toString();
					inprogressAmount = value.get("amount").toString();
				} else {
					failCount = value.get("count").toString();
					failAmount = value.get("amount").toString();
				}
			}
			map.put("successCount", successCount);
			map.put("successAmount", successAmount);
			map.put("pendingCount", pendingCount);
			map.put("pendingAmount", pendingAmount);
			map.put("failCount", failCount);
			map.put("failAmount", failAmount);
			map.put("inprogressCount", inprogressCount);
			map.put("inprogressAmount", inprogressAmount);
			map.put("totalTransactionCount", successCount + pendingCount + failCount + inprogressCount);
			logger.info("Transaction summary generated for clientId: {}", map);
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(map).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("No transaction summary found for clientId: {}");
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(map).build();
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}
	}

	@Override
	public ResponseEntity<?> payoutFilterByClientId(PayoutFilterByCLientId data) {
		logger.info("Starting payoutFilterByClientId request: {}", data);
		if ((data.getUtr() != null || !data.getUtr().isEmpty())
				&& (data.getTransactionId().isEmpty() || data.getTransactionId().isBlank())
				&& (data.getStatus().isBlank() || data.getStatus().isEmpty())
				&& (data.getTransferMode().isBlank() || data.getTransferMode().isEmpty())
				&& (data.getFromDate().isBlank() || data.getFromDate().isEmpty())) {
			logger.info("Filter matched: ONLY UTR present");
			Optional<PayoutRecords> list = this.payoutRepository.findByUtr(data.getUtr());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list.get()).build();
			return ResponseEntity.ok(response);
		}

		if ((data.getUtr().isEmpty() || data.getUtr().isBlank())
				&& (data.getTransactionId() != null || !data.getTransactionId().isEmpty())
				&& (data.getStatus().isBlank() || data.getStatus().isEmpty())
				&& (data.getTransferMode().isBlank() || data.getTransferMode().isEmpty())
				&& (data.getFromDate().isBlank() || data.getFromDate().isEmpty())) {
			logger.info("Filter matched: ONLY TransactionId present");
			Optional<PayoutRecords> list = this.payoutRepository.findByTransactionId(data.getTransactionId());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list.get()).build();
			return ResponseEntity.ok(response);
		}
		if ((data.getUtr().isEmpty() || !data.getUtr().isBlank())
				&& (data.getTransactionId().isBlank() || data.getTransactionId().isEmpty())
				&& (data.getStatus() != null || !data.getStatus().isEmpty())
				&& (data.getTransferMode().isBlank() || data.getTransferMode().isEmpty())
				&& (data.getFromDate().isBlank() || data.getFromDate().isEmpty())) {
			logger.info("Filter matched: ONLY Status present");
			List<PayoutRecords> list = this.payoutRepository.findByClientIdAndStatus(data.getClientId(),
					data.getStatus());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		}

		if ((data.getUtr().isEmpty() || !data.getUtr().isBlank())
				&& (data.getTransactionId().isBlank() || data.getTransactionId().isEmpty())
				&& (data.getStatus().isBlank() || data.getStatus().isEmpty())
				&& (data.getTransferMode() != null || !data.getTransferMode().isEmpty())
				&& (data.getFromDate().isBlank() || data.getFromDate().isEmpty())) {
			logger.info("Filter matched: ONLY TransferMode present");
			List<PayoutRecords> list = this.payoutRepository.findByClientIdAndMode(data.getClientId(),
					data.getTransferMode());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		}

		if ((data.getUtr().isEmpty() || data.getUtr().isBlank())
				&& (data.getTransactionId().isEmpty() || data.getTransactionId().isBlank())
				&& (data.getStatus() != null || !data.getStatus().isEmpty())
				&& (data.getTransferMode().isBlank() || data.getTransferMode().isEmpty())
				&& (data.getFromDate() != null || !data.getFromDate().isEmpty())) {
			logger.info("Filter matched: Status + Date present");
			List<PayoutRecords> list = this.payoutRepository.findByClientIdStatusAndDate(data.getClientId(),
					data.getStatus(), data.getFromDate(), data.getToDate());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		}
		if ((data.getUtr().isEmpty() || data.getUtr().isBlank())
				&& (data.getTransactionId().isEmpty() || data.getTransactionId().isBlank())
				&& (data.getStatus() != null || !data.getStatus().isEmpty())
				&& (data.getTransferMode() != null || !data.getTransferMode().isEmpty())
				&& (data.getFromDate().isBlank() || data.getFromDate().isEmpty())) {
			logger.info("Filter matched: Status + TransferMode present");
			List<PayoutRecords> list = this.payoutRepository.findByClientIdStatusAndMode(data.getClientId(),
					data.getStatus(), data.getTransferMode());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		}
		if ((data.getUtr().isEmpty() || data.getUtr().isBlank())
				&& (data.getTransactionId().isEmpty() || data.getTransactionId().isBlank())
				&& (data.getStatus() != null || !data.getStatus().isEmpty())
				&& (data.getTransferMode() != null || !data.getTransferMode().isEmpty())
				&& (data.getFromDate() != null || !data.getFromDate().isEmpty())) {
			logger.info("Filter matched: Status + Mode + Date present");
			List<PayoutRecords> list = this.payoutRepository.findByClientIdStatusModeAndDate(data.getClientId(),
					data.getStatus(), data.getFromDate(), data.getToDate(), data.getTransferMode());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		}
		if ((data.getUtr().isEmpty() || data.getUtr().isBlank())
				&& (data.getTransactionId().isEmpty() || data.getTransactionId().isBlank())
				&& (data.getStatus().isBlank() || data.getStatus().isEmpty())
				&& (data.getTransferMode() != null || !data.getTransferMode().isEmpty())
				&& (data.getFromDate() != null || !data.getFromDate().isEmpty())) {
			logger.info("Filter matched: TransferMode + Date present");
			List<PayoutRecords> list = this.payoutRepository.findByClientIdModeAndDate(data.getClientId(),
					data.getTransferMode(), data.getFromDate(), data.getToDate());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		}
		logger.warn("No filter condition matched — applying fallback ALL-FILTER.");
		List<PayoutRecords> list = this.payoutRepository.filterByAll(data.getUtr(), data.getTransactionId(),
				data.getFromDate(), data.getToDate(), data.getClientId(), data.getStatus(), data.getTransferMode());
		ResponseDto response = ResponseDto.builder().status("BAD_REQUEST").message("ERROR")
				.data("No records found for the given date....!").build();
		return ResponseEntity.badRequest().body(response);
	}

	@Override
	public ResponseEntity<?> addWebhook(WebhookUrl data) {
		logger.info("Webhook add request received for userId={} | url={}", data.getUserId(), data.getUrl());
		Optional<WebhookUrl> records = this.webhookRepository.findByUserIdAndUrl(data.getUserId());
		if (records.isPresent()) {
			logger.warn("Webhook add failed — URL already exists for userId={}", data.getUserId());
			ResponseDto response = ResponseDto.builder().status("CONFLICT").message("ERROR")
					.data("Same url is exist for the given userId,,!").build();
			return ResponseEntity.badRequest().body(response);
		} else {
			this.webhookRepository.save(data);
			logger.info("Webhook URL saved successfully for userId={}", data.getUserId());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS")
					.data("webhook url saved successfully..!").build();
			return ResponseEntity.ok(response);
		}
	}

	@Override
	public ResponseEntity<?> updateWebhook(WebhookUrl data) {

	    logger.info(
	        "Webhook update request | userId={} | url={} | type={}",
	        data.getUserId(),
	        data.getUrl(),
	        data.getWebhooktype()
	    );

	    Optional<WebhookUrl> records =
	            webhookRepository.findByUserIdAndUrl(data.getUserId());

	    if (records.isEmpty()) {

	        logger.warn("No webhook found for userId={}", data.getUserId());

	        return ResponseEntity.badRequest().body(
	                ResponseDto.builder()
	                        .status("BAD_REQUEST")
	                        .message("ERROR")
	                        .data("No records found for the given userId")
	                        .build()
	        );
	    }

	    webhookRepository.updateURl(
	            data.getUserId(),
	            data.getUrl(),
	            data.getWebhooktype()
	    );

	    logger.info("Webhook updated successfully for userId={}", data.getUserId());

	    return ResponseEntity.ok(
	            ResponseDto.builder()
	                    .status("OK")
	                    .message("SUCCESS")
	                    .data("Webhook updated successfully")
	                    .build()
	    );
	}


	@Override
	public ResponseEntity<?> webhookByClientId(String clientId) {
		logger.info("Fetching webhook details for clientId={}", clientId);
		Optional<WebhookUrl> records = this.webhookRepository.findByUserIdAndUrl(clientId);
		if (records.isPresent()) {
			logger.info("Webhook record found for clientId={}", clientId);
			logger.debug("Webhook details: {}", records.get());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(records.get()).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("No webhook record found for clientId={}", clientId);
			ResponseDto response = ResponseDto.builder().status("BAD_REQUEST").message("ERROR")
					.data("No records found for the given clientId..!").build();
			return ResponseEntity.badRequest().body(response);
		}
	}

	@Override
	public ResponseEntity<?> addIpAddress(IpAddress data) {

	    logger.info("Add IP request received for userId={} | ip={}", 
	                data.getUserId(), data.getIpAddress());

	    this.ipRepository.save(data);

	    logger.info("IP address added successfully for userId={}", data.getUserId());

	    ResponseDto response = ResponseDto.builder()
	            .status("OK")
	            .message("SUCCESS")
	            .data("IpAddress added successfully..!")
	            .build();

	    return ResponseEntity.ok(response);
	}


	@Override
	public ResponseEntity<?> updateIpAddress(IpAddress data) {

	    logger.info("IP update request received for userId={} | newIp={}", 
	                data.getUserId(), data.getIpAddress());

	    this.ipRepository.updateIp(
	            data.getUserId(),
	            data.getIpAddress()
	    );

	    logger.info("IP address updated successfully for userId={}", data.getUserId());

	    ResponseDto response = ResponseDto.builder()
	            .status("OK")
	            .message("SUCCESS")
	            .data("IpAddress updated successfully..!")
	            .build();

	    return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<?> ipAddressByClientId(String clientId) {
		logger.info("Fetching IP address for clientId={}", clientId);
		Optional<IpAddress> records = this.ipRepository.findByUserId(clientId);
		if (records.isPresent()) {
			logger.info("IP address record found for clientId={}", clientId);
			logger.debug("IP Details: {}", records.get());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(records.get()).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("No IP address record found for clientId={}", clientId);
			ResponseDto response = ResponseDto.builder().status("BAD_REQUEST").message("ERROR")
					.data("No records found for the given clientId..!").build();
			return ResponseEntity.badRequest().body(response);
		}
	}

	@Override
	public ResponseEntity<?> updateMerchant(ClientEditProfileDto data) {
		logger.info("Update merchant request received for userId={}", data.getUserId());
		Optional<Client> client = this.clientRepository.findByUserId(data.getUserId());
		if (client.isPresent()) {
			logger.info("Client found for userId={} — proceeding with update", data.getUserId());
			String encPass = this.passwordEncoder.encode(data.getPassword());
			this.clientRepository.updateClient(data.getEmail(), encPass, data.getName(), data.getMobileNum(),
					data.getUserId());
			logger.info("Merchant profile updated successfully for userId={}", data.getUserId());
			ResponseDto response = ResponseDto.builder().message("SUCCESS").status("OK").data("Updated successfully..!")
					.build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("Merchant update failed — no client found for userId={}", data.getUserId());
			ResponseDto response = ResponseDto.builder().message("ERROR").status("BAD_REQUEST")
					.data("No records found for the given ClientId..!").build();
			return ResponseEntity.badRequest().body(response);
		}
	}

//	@Override
//	@Transactional
//	public ResponseEntity<?> addLienAmount(LienAmount data) {
//
//	    logger.info("Add LienAmount request received for userId={} | amount={}",
//	            data.getUserId(), data.getAmount());
//
//	    Optional<LienAmount> records =
//	            this.lienRepository.findByUserId(data.getUserId());
//
//	    if (records.isPresent()) {
//	        logger.warn("LienAmount entry already exists for userId={}", data.getUserId());
//
//	        ResponseDto response = ResponseDto.builder()
//	                .status("CONFLICT")
//	                .message("ERROR")
//	                .data("LienAmount already exists for the given userId")
//	                .build();
//
//	        return ResponseEntity.badRequest().body(response);
//	    }
//
//	    // 1️⃣ Save lien amount
//	    this.lienRepository.save(data);
//	    logger.info("LienAmount saved successfully for userId={}", data.getUserId());
//
//	    // 2️⃣ Update prefund lien status
//	    int updated =
//	            prefundRequestRepository.updateLienStatusByUserId(
//	                    data.getUserId(),
//	                    "LIEN_APPLIED"
//	            );
//
//	    logger.info("Prefund lien_status updated for userId={} | rowsAffected={}",
//	            data.getUserId(), updated);
//
//	    ResponseDto response = ResponseDto.builder()
//	            .status("OK")
//	            .message("SUCCESS")
//	            .data("LienAmount added and prefund lien status updated successfully")
//	            .build();
//
//	    return ResponseEntity.ok(response);
//	}

	@Override
	public ResponseEntity<?> updateLienAmount(LienAmount data) {
		logger.info("Update LienAmount request received for userId={} | newAmount={}", data.getUserId(),
				data.getAmount());
		Optional<LienAmount> records = this.lienRepository.findByUserId(data.getUserId());
		if (records.isPresent()) {
			logger.info("LienAmount record found for userId={} — updating amount", data.getUserId());
			this.lienRepository.updateAmount(data.getUserId(), data.getAmount());
			logger.info("LienAmount updated successfully for userId={}", data.getUserId());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS")
					.data("LienAmount updated successfully..!").build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("Update failed — no LienAmount record found for userId={}", data.getUserId());
			ResponseDto response = ResponseDto.builder().status("BAD_REQUEST").message("ERROR")
					.data("No records found for the given userId..!").build();
			return ResponseEntity.badRequest().body(response);
		}
	}

	@Override
	public ResponseEntity<?> walletDashboardByClientId(String clientId) {
		logger.info("Fetching wallet dashboard details for clientId={}", clientId);
		Map<String, Object> map = new HashMap<>();
		Optional<Client> balance = this.clientRepository.findByUserId(clientId);
		Optional<LienAmount> lien = this.lienRepository.findByUserId(clientId);
		double available = 0.00;
		if (balance.isPresent()) {
			logger.info("Client balance found for clientId={}", clientId);
			logger.debug("Balance details: {}", balance.get());
			if (lien.isPresent()) {
				logger.info("Lien amount found for clientId={}", clientId);
				logger.debug("Lien details: {}", lien.get());
				available = Double.parseDouble(String.valueOf(balance.get().getAccountBal())) - lien.get().getAmount();
				if (available < 0) {
					available = 0.00;
				}
				map.put("availableBalance", available);
				map.put("lienAmount", lien.get().getAmount());
				map.put("totalBalance", balance.get().getAccountBal());
				logger.info("Wallet dashboard computed for clientId={}: {}", clientId, map);
				ResponseDto response = ResponseDto.builder().message("SUCCESS").data(map).status("OK").build();
				return ResponseEntity.ok(response);
			} else {
				logger.info("No lien amount found for clientId={}. Using full balance as available.", clientId);
				map.put("availableBalance", balance.get().getAccountBal());
				map.put("lienAmount", 0.00);
				map.put("totalBalance", balance.get().getAccountBal());
				ResponseDto response = ResponseDto.builder().message("SUCCESS").data(map).status("OK").build();
				return ResponseEntity.ok(response);
			}
		} else {
			logger.warn("Invalid clientId={} — no client balance found.", clientId);
			ResponseDto response = ResponseDto.builder().message("ERROR").data("Please provide valid userId..!")
					.status("BAD_REQUEST").build();
			return ResponseEntity.ok(response);
		}
	}

	@Override
	public ResponseEntity<?> filterByOrderId(String orderId) {
		logger.info("Filtering payout record by orderId={}", orderId);
		Optional<PayoutRecords> data = Optional.ofNullable(this.payoutRepository.findByOrderId(orderId));
		if (data.isPresent()) {
			logger.info("Record found for orderId={}", orderId);
			logger.debug("Record details: {}", data.get());
			ResponseDto response = ResponseDto.builder().message("SUCCESS").data(data.get()).status("OK").build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("No payout record found for orderId={}", orderId);
			ResponseDto response = ResponseDto.builder().message("ERROR").data("Please provide valid orderId..!")
					.status("BAD_REQUEST").build();
			return ResponseEntity.badRequest().body(response);
		}
	}

	@Override
	public ResponseEntity<?> lienHistory(String clientId) {
		logger.info("Fetching lien history for clientId={}", clientId);
		List<LienHistory> list = this.lienHistoryRepository.findByClientId(clientId);
		if (list.size() > 0) {
			logger.info("Lien history found for clientId={} | count={}", clientId, list.size());
			logger.debug("Lien history details: {}", list);
			ResponseDto response = ResponseDto.builder().message("SUCCESS").status("OK").data(list).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("No lien history found for clientId={}", clientId);
			ResponseDto response = ResponseDto.builder().message("ERROR").status("BAD_REQUEST")
					.data("No data found for the given clientId..!").build();
			return ResponseEntity.badRequest().body(response);
		}
	}

	// Helper method to build payout record response data
	private Map<String, Object> buildPayoutRecordResponseData(PayoutRecords record) {
		logger.info("Building payout record response map for recordId={}", record.getSlNo());
		logger.debug("Payout record input data: {}", record);
		Map<String, Object> data = new HashMap<>();
		data.put("slNo", record.getSlNo());
		data.put("userId", record.getUserId());
		data.put("name", record.getName());
		data.put("email", record.getEmail());
		data.put("number", record.getNumber());
		data.put("accNumber", record.getAccNumber());
		data.put("ifsc", record.getIfsc());
		data.put("charges", record.getCharges());
		data.put("gstCharges", record.getGstCharges());
		data.put("amount", record.getAmount());
		data.put("finalAmount", record.getFinalAmount());
		data.put("status", record.getStatus());
		data.put("statusCode", record.getStatusCode());
		data.put("utr", record.getUtr());
		data.put("refundStatus", record.getRefundStatus());
		data.put("currentBalance", record.getCurrentBalance());
		data.put("updatedBalance", record.getUpdatedBalance());
		data.put("transferMode", record.getTransferMode());
		data.put("trxnId", record.getOrderId());
		data.put("pgId", record.getPgId());
		data.put("errorMsg", record.getErrorMsg());
		data.put("createdDate", record.getCreatedDate());
		data.put("updatedDate", record.getUpdatedDate());
		logger.debug("Final payout record response map generated: {}", data);
		return data;
	}

	private String generateUniqueReference() {
		logger.info("Generating unique reference number...");
		String prefix = "REF";
		String timestamp = String.valueOf(System.currentTimeMillis());
		String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		String reference = prefix + timestamp + uniqueId;
		logger.debug("Generated unique reference: {}", reference);
		return reference;
	}

	public LocalCheckStatusResponse testCheckStatusLocal(String orderId) {
		logger.info("Running local test check-status simulation for orderId={}", orderId);
		PayoutRecords record = payoutRepository.findByOrderId(orderId);
		logger.debug("Fetched payout record: {}", record);
		String simulatedCode;
		String message;
		if (orderId.endsWith("1")) {
			logger.info("Simulating SUCCESS state for orderId={}", orderId);
			simulatedCode = "00";
			record.setStatus("SUCCESS");
			message = "Operation completed successfully";
		} else if (orderId.endsWith("2")) {
			logger.info("Simulating PENDING state for orderId={}", orderId);
			simulatedCode = "01";
			record.setStatus("PENDING");
			message = "Operation is pending";
		} else {
			logger.info("Simulating FAILED state for orderId={}", orderId);
			simulatedCode = "99";
			record.setStatus("FAILED");
			message = "Testing Fail State";
		}
		record.setStatusCode(simulatedCode);
		record.setErrorMsg(message);
		record.setUpdatedDate(LocalDateTime.now().toString());
		payoutRepository.save(record);
		logger.info("Updated local test record for orderId={} with status={} and statusCode={}", orderId,
				record.getStatus(), simulatedCode);
		logger.debug("Final saved record: {}", record);
		return new LocalCheckStatusResponse(orderId, record.getStatus(), simulatedCode, message);
	}

	@Override
	public Client getClientByEmail(String username) {
		return this.clientRepository.findByEmail(username).get();
	}

	@Override
	public ResponseEntity<?> clientByClientId(String clientId) {
		Optional<Client> client = this.clientRepository.findByUserId(clientId);
		if (client.isPresent()) {
			return ResponseEntity.ok(client.get());
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No data present for the given Client-Id..!");
		}
	}

	@Override
	public String saveCallBack(Map<String, Object> request) {
        logger.info("callBack recieved from PayG : {}", request);
		System.out.println("CallBack: " + request);
		return "SUCCESS";
	}

	@Override
	public ResponseEntity<?> trasactionCountAndAmountByDate(Map<String, Object> data) {
		String date = null, clientId = null;
		if (data.containsKey("date")) {
			date = data.get("date").toString();
		}
		if (data.containsKey("clientId")) {
			clientId = data.get("clientId").toString();
		}
		Map<String, Object> map = new HashMap<>();
		if (date != null && clientId != null) {
			String successCount = "0", successAmount = "0", pendingAmount = "0", pendingCount = "0", failAmount = "0",
					failCount = "0";
			List<Map<String, Object>> records = this.payoutRepository.transactionCountAndAmounByClientIdt(clientId,
					date);
			System.out.println("size: " + records.size());

			if (records.size() > 0) {
				for (int i = 0; i < records.size(); i++) {
					Map<String, Object> value = records.get(i);
					if (value.get("status").equals("SUCCESS")) {
						successCount = value.get("count").toString();
						successAmount = value.get("amount").toString();
					} else if (value.get("status").equals("PENDING")) {
						pendingCount = value.get("count").toString();
						pendingAmount = value.get("amount").toString();
					} else {
						failCount = value.get("count").toString();
						failAmount = value.get("amount").toString();
					}
				}
				map.put("successCount", successCount);
				map.put("successAmount", successAmount);
				map.put("pendingCount", pendingCount);
				map.put("pendingAmount", pendingAmount);
				map.put("failCount", failCount);
				map.put("failAmount", failAmount);
				ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(map).build();
				return ResponseEntity.ok(response);
			} else {
				ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(map).build();
				return ResponseEntity.ok(response);
			}
		}
		ResponseDto response = ResponseDto.builder().status("BAD_REQUEST").message("ERROR")
				.data("Please provide valid data..!").build();
		return ResponseEntity.badRequest().body(response);
	}

	@Override
	public ResponseEntity<?> payoutFilter(PayoutFilterByCLientId data) {
		if ((data.getUtr() != null || !data.getUtr().isEmpty())
				&& (data.getTransactionId().isEmpty() || data.getTransactionId().isBlank())
				&& (data.getStatus().isEmpty() || data.getStatus().isBlank())
				&& (data.getTransferMode().isEmpty() || data.getTransferMode().isBlank())
				&& (data.getFromDate().isEmpty() || data.getFromDate().isBlank())) {
			System.out.println("only utr is present.......");
			Optional<PayoutRecords> list = this.payoutRepository.findByUtr(data.getUtr());
			if (list.isEmpty()) {
				ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data("No records found..!")
						.build();
				return ResponseEntity.ok(response);
			}
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list.get()).build();
			return ResponseEntity.ok(response);
		}
		if ((data.getUtr().isEmpty() || data.getUtr().isBlank())
				&& (data.getTransactionId() != null || !data.getTransactionId().isEmpty())
				&& (data.getStatus().isBlank() || data.getStatus().isEmpty())
				&& (data.getTransferMode().isBlank() || data.getTransferMode().isEmpty())
				&& (data.getFromDate().isBlank() || data.getFromDate().isEmpty())) {
			System.out.println("only transactionId is present ..........");
			Optional<PayoutRecords> list = this.payoutRepository.findByTransactionId(data.getTransactionId());
			if (list.isEmpty()) {
				ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data("No records found..!")
						.build();
				return ResponseEntity.ok(response);
			}
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list.get()).build();
			return ResponseEntity.ok(response);
		}
		if ((data.getUtr().isEmpty() || !data.getUtr().isBlank())
				&& (data.getTransactionId().isBlank() || data.getTransactionId().isEmpty())
				&& (data.getStatus() != null || !data.getStatus().isEmpty())
				&& (data.getTransferMode().isBlank() || data.getTransferMode().isEmpty())
				&& (data.getFromDate().isBlank() || data.getFromDate().isEmpty())) {
			System.out.println("only Status is present ..........");
			List<PayoutRecords> list = this.payoutRepository.findByStatus(data.getStatus());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		}
		if ((data.getUtr().isEmpty() || !data.getUtr().isBlank())
				&& (data.getTransactionId().isBlank() || data.getTransactionId().isEmpty())
				&& (data.getStatus().isBlank() || data.getStatus().isEmpty())
				&& (data.getTransferMode() != null || !data.getTransferMode().isEmpty())
				&& (data.getFromDate().isBlank() || data.getFromDate().isEmpty())) {
			System.out.println("only TransferMode is present ..........");
			List<PayoutRecords> list = this.payoutRepository.findByMode(data.getTransferMode());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		}
		if ((data.getStatus().isBlank() || data.getStatus().isEmpty())
				&& (data.getTransferMode().isBlank() || data.getTransferMode().isEmpty())
				&& (data.getTransactionId().isEmpty() || data.getTransactionId().isBlank())
				&& (data.getFromDate() != null || !data.getFromDate().isEmpty())) {
			System.out.println(" Only Date are present ..........");
			List<PayoutRecords> list = this.payoutRepository.findByDate(data.getFromDate(), data.getToDate());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		}
		if ((data.getUtr().isEmpty() || data.getUtr().isBlank())
				&& (data.getTransactionId().isEmpty() || data.getTransactionId().isBlank())
				&& (data.getStatus() != null || !data.getStatus().isEmpty())
				&& (data.getTransferMode().isBlank() || data.getTransferMode().isEmpty())
				&& (data.getFromDate() != null || !data.getFromDate().isEmpty())) {
			System.out.println(" Status and Date are present ..........");
			List<PayoutRecords> list = this.payoutRepository.findByStatusAndDate(data.getStatus(), data.getFromDate(),
					data.getToDate());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		}
		if ((data.getUtr().isEmpty() || data.getUtr().isBlank())
				&& (data.getTransactionId().isEmpty() || data.getTransactionId().isBlank())
				&& (data.getStatus() != null || !data.getStatus().isEmpty())
				&& (data.getTransferMode() != null || !data.getTransferMode().isEmpty())
				&& (data.getFromDate().isBlank() || data.getFromDate().isEmpty())) {
			System.out.println(" Status and TransferMode are present ..........");
			List<PayoutRecords> list = this.payoutRepository.findByStatusAndMode(data.getStatus(),
					data.getTransferMode());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		}
		if ((data.getUtr().isEmpty() || data.getUtr().isBlank())
				&& (data.getTransactionId().isEmpty() || data.getTransactionId().isBlank())
				&& (data.getStatus() != null || !data.getStatus().isEmpty())
				&& (data.getTransferMode() != null || !data.getTransferMode().isEmpty())
				&& (data.getFromDate() != null || !data.getFromDate().isEmpty())) {
			System.out.println(" Status, Mode and Date are present ..........");
			List<PayoutRecords> list = this.payoutRepository.findByStatusModeAndDate(data.getStatus(),
					data.getFromDate(), data.getToDate(), data.getTransferMode());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		}
		if ((data.getUtr().isEmpty() || data.getUtr().isBlank())
				&& (data.getTransactionId().isEmpty() || data.getTransactionId().isBlank())
				&& (data.getStatus().isBlank() || data.getStatus().isEmpty())
				&& (data.getTransferMode() != null || !data.getTransferMode().isEmpty())
				&& (data.getFromDate() != null || !data.getFromDate().isEmpty())) {
			System.out.println(" transferMode and Date are present ..........");
			List<PayoutRecords> list = this.payoutRepository.findByModeAndDate(data.getTransferMode(),
					data.getFromDate(), data.getToDate());
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		} else {
			List<PayoutRecords> list = this.payoutRepository.filterByAll(data.getUtr(), data.getTransactionId(),
					data.getFromDate(), data.getToDate(), data.getClientId(), data.getStatus(), data.getTransferMode());
			ResponseDto response = ResponseDto.builder().status("BAD_REQUEST").message("ERROR")
					.data("No records found for the given date....!").build();
			return ResponseEntity.badRequest().body(response);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<?> paymentPayin(PayinDto data, String clientId, String clientSecretId, HttpServletRequest req) throws Exception {
	    logger.info("PayIn request initiated | userId={} | orderId={}", data.getUserId(), data.getOrderId());
	    System.out.println("PayIn START | userId=" + data.getUserId()
	            + " | orderId=" + data.getOrderId());

        double amount = Double.parseDouble(data.getAmount());
        if (amount < 100 ){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseDto.builder().message("ERROR")
                    .status("BAD_REQUEST").data("Amount should be greater than 100 ").build());
        }

        // --------------------------------------------------------
        // 1) AUTHENTICATION
        // --------------------------------------------------------
        Boolean authenticated = this.isAuthenticated(clientId, clientSecretId, data.getUserId());
        if (!authenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseDto.builder().message("ERROR")
                    .status("UNAUTHORIZED").data("Authentication failed").build());
        }

        // --------------------------------------------------------
        // 2) CLIENT CHECK
        // --------------------------------------------------------
        Optional<Client> clt = this.clientRepository.findByUserId(data.getUserId());
        if (clt.isEmpty()) {
            return ResponseEntity.badRequest().body(ResponseDto.builder().message("Error").status("BAD_REQUEST")
                    .data("Invalid client user-id").build());
        }

        // --------------------------------------------------------
        // 3) CLIENT ACTIVE CHECK
        // --------------------------------------------------------
        if (!isClientActive(clt.get())) {
            return ResponseEntity.badRequest().body(
                    ResponseDto.builder().message("Error").status("BAD_REQUEST").data("Client is inactive").build());
        }

        // --------------------------------------------------------
        // 4) IP WHITELIST CHECK
        // --------------------------------------------------------
        String ip = ipFetching.getClientIP(req);
        System.out.println("ip: "+ ip);
        Optional<IpAddress> ipRow = this.ipRepository.findByUserIdAndIp(data.getUserId(), ip);
        if (ipRow.isEmpty() || !ip.equals(ipRow.get().getIpAddress())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ResponseDto.builder().message("ERROR").status("BAD_REQUEST").data("IP not whitelisted").build());
        }

        // --------------------------------------------------------
        // 5) DUPLICATE ORDER-ID CHECK
        // --------------------------------------------------------
        if (data.getOrderId() != null && !data.getOrderId().isBlank()) {

            PayinRecords exist = this.payinRepository.findByOrderId(data.getOrderId());
            if (exist != null) {
                return ResponseEntity.badRequest().body(
                        ResponseDto.builder().message("Error").status("BAD_REQUEST").data("Duplicate OrderId").build());
            }

        }

        // --------------------------------------------------------
        // 6) CHARGES CALCULATION
        // --------------------------------------------------------
        Map<String, Object> calc = this.payinChargesCalculations(data);

        if (calc.get("charges") == null || toBigDecimal(calc.get("charges")).compareTo(BigDecimal.ZERO) == 0) {

            return ResponseEntity.badRequest().body(ResponseDto.builder().message("Error").status("BAD_REQUEST")
                    .data("Charges not configured. Contact admin").build());
        }

        // --------------------------------------------------------
        // 7) WALLET ADD PROCESS (LIKE DEDUCTION IN PAYOUT)
        // --------------------------------------------------------
        PayinRecords savedRecord;
        PayinResponseDto responseDto;

        synchronized (this) {
            savedRecord = this.msPayinAdditionProcess(data, calc);

            if (!"PENDING".equals(savedRecord.getStatus())) {
                return ResponseEntity.badRequest().body(
                        ResponseDto.builder().message("Error").status("FAILED").data("Wallet update failed").build());
            }
        }
        // --------------------------------------------------------
        // 8) CALLING PAYIN METHOD BASED ON PGID
        // --------------------------------------------------------

        ResponseEntity<?> payinResp = this.buckBoxPayin(data);

        System.out.println("calling payin pipe inside the main flow");
        logger.info("payin response recieved:  {}", payinResp);

//        String resp = payinResp.getBody().toString();
//        JSONObject json = new JSONObject(resp);
//        logger.info("json response: {}", json);
//
//        JSONObject paymentData = json.getJSONObject("payment_data");
//        System.out.println("paymentData: "+paymentData);
//
//        JSONObject extendedData = paymentData.getJSONObject("extended_data");
//        System.out.println("extendedData: "+extendedData);
//
//        String upiUrl = extendedData.getString("qr_code_content");

        responseDto = this.payinResponseGenerate(data,calc,savedRecord);
        responseDto.setRedirect_url("upiUrl");
        // --------------------------------------------------------
        // 9) RETURN SUCCESS RESPONSE
        // --------------------------------------------------------
        return ResponseEntity.ok(responseDto);
	}

    private PayinResponseDto payinResponseGenerate(PayinDto data, Map<String, Object> calc, PayinRecords savedRecords) {
        PayinResponseDto r = new PayinResponseDto();

        BigDecimal amount = safeBig(data.getAmount());
        BigDecimal charges = toBigDecimal(calc.get("charges"));
        BigDecimal gst = toBigDecimal(calc.get("gstCharges"));

        r.setOrderId(data.getOrderId());
        r.setUserId(data.getUserId());
        r.setName(nz(data.getName()));
        r.setEmail(nz(data.getEmail()));
        r.setPhone(nz(data.getMobile()));
        r.setAddress(nz(data.getAddress()));
        r.setAmount(String.valueOf(amount.doubleValue()));
        r.setCharges(String.valueOf(charges.doubleValue()));
        r.setGstCharges(String.valueOf(gst.doubleValue()));
        r.setStatus("PENDING");
        r.setStatusCode("TXNP");
        r.setCreatedDate(String.valueOf(savedRecords.getCreatedDate()));
        r.setUpdatedDate(String.valueOf(savedRecords.getUpdatedDate()));
        return r;
    }


    private Map<String, Object> buildErrorResponse(String status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", status);
        error.put("message", "Error");
        error.put("data", message);
        error.put("timestamp", LocalDateTime.now().toString());
        return error;
    }

    private Map<String, Object> buildSuccessResponse(PayinRecords record, String redirectUrl) {

        Map<String, Object> response = new HashMap<>();

        response.put("name", record.getName());
        response.put("email", record.getEmail());
        response.put("phone", record.getMobile());
        response.put("address", record.getAddress());
        response.put("amount", String.valueOf(record.getAmount()));
        response.put("orderId", record.getOrderId());
        response.put("redirect_url", redirectUrl);
        response.put("status", record.getStatus());
        response.put("statusCode", record.getStatusCode());
        response.put("createdDate",
                record.getCreatedDate() != null
                        ? record.getCreatedDate().toString()
                        : null);

        response.put("updatedDate",
                record.getUpdatedDate() != null
                        ? record.getUpdatedDate().toString()
                        : null);

        response.put("charges", String.valueOf(record.getCharges()));
        response.put("gstCharges", String.valueOf(record.getGstCharges()));
        response.put("userId", record.getUserId());

        return response;
    }


    // CHARGES CALCULATION
    private Map<String, Object> payinChargesCalculations(PayinDto data) {

        Map<String, Object> map = new HashMap<>();

        BigDecimal amount = safeBig(data.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            map.put("configured", false);
            map.put("error", "Invalid amount");
            return map;
        }

        PayInCharges ch = payInChargesRepository
                .findApplicableCharges(data.getUserId(), amount.doubleValue());

        if (ch == null) {
            map.put("configured", false);
            map.put("error", "Charges not configured");
            return map;
        }

        BigDecimal charges;
        BigDecimal chargeValue = BigDecimal.valueOf(ch.getChargesAmount());
        if ("PERCENTAGE".equalsIgnoreCase(ch.getChargesType())
                || "%".equals(ch.getChargesType())) {

            charges = amount
                    .multiply(chargeValue)
                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        } else {
            charges = chargeValue;
        }

        BigDecimal gst = charges
                .multiply(BigDecimal.valueOf(18))
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        BigDecimal netAmount = amount.subtract(charges.add(gst));

        if (netAmount.compareTo(BigDecimal.ZERO) <= 0) {
            map.put("configured", false);
            map.put("error", "Net amount invalid");
            return map;
        }

        map.put("configured", true);
        map.put("amount", amount.setScale(2, RoundingMode.HALF_UP));
        map.put("charges", charges.setScale(2, RoundingMode.HALF_UP));
        map.put("gstCharges", gst.setScale(2, RoundingMode.HALF_UP));
        map.put("netAmount", netAmount.setScale(2, RoundingMode.HALF_UP));
        return map;
    }

//	    // CHARGES CALCULATION
//	 private Map<String, Object> payinChargesCalculations(PayinDto data) {
//
//		    Map<String, Object> map = new HashMap<>();
//
//		    BigDecimal amount = safeBig(data.getAmount());
//
//		    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
//		        map.put("configured", false);
//		        map.put("error", "Invalid amount");
//		        return map;
//		    }
//
//		    PayInCharges ch = payInChargesRepository
//		            .findApplicableCharges(data.getUserId(), amount.doubleValue());
//
//		    if (ch == null) {
//		        map.put("configured", false);
//		        map.put("error", "Charges not configured");
//		        return map;
//		    }
//
//		    BigDecimal charges;
//		    BigDecimal chargesAmount = BigDecimal.valueOf(ch.getChargesAmount());
//
//		    if ("PERCENTAGE".equalsIgnoreCase(ch.getChargesType())) {
//		        charges = amount.multiply(chargesAmount)
//		                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
//		    } else {
//		        charges = chargesAmount; 
//		    }
//
//		    BigDecimal gst = charges.multiply(BigDecimal.valueOf(18))
//		            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
//
//		    BigDecimal netAmount = amount.subtract(charges.add(gst));
//
//		    if (netAmount.compareTo(BigDecimal.ZERO) <= 0) {
//		        map.put("configured", false);
//		        map.put("error", "Net amount invalid");
//		        return map;
//		    }
//
//		    map.put("configured", true);
//		    map.put("amount", amount);
//		    map.put("charges", charges);
//		    map.put("gstCharges", gst);
//		    map.put("netAmount", netAmount);
//
//		    return map;
//		}
	 private ResponseEntity<String> phonePeCreatePayment(PayinDto data) {

		    RestTemplate restTemplate = new RestTemplate();
		    String accessToken = phonePeAuthService.getAccessToken();

		    Map<String, Object> body = new HashMap<>();
		    body.put("merchantOrderId", data.getOrderId());
		    body.put("amount", Long.parseLong(data.getAmount()) * 100); // ₹ → paisa
		    body.put("expireAfter", 1200);
		    body.put("metaInfo", Map.of(
		            "udf1", data.getUserId(),
		            "udf2", data.getEmail(),
		            "udf3", data.getMobile()
		    ));
		    body.put("paymentFlow", Map.of(
		            "type", "PG_CHECKOUT",
		            "merchantUrls", Map.of("redirectUrl", data.getRedirectRoute()),
		            "paymentModeConfig", Map.of(
		                    "enabledPaymentModes",
		                    List.of(Map.of("type", "UPI_INTENT"))
		            )
		    ));

		    HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.APPLICATION_JSON);
		    headers.set("Authorization", "O-Bearer " + accessToken);

		    return restTemplate.exchange(
		            PHONEPE_PAY_URL,
		            HttpMethod.POST,
		            new HttpEntity<>(body, headers),
		            String.class
		    );
		}


	    // WALLET UPDATE
	    @Transactional
	    private PayinRecords msPayinAdditionProcess(PayinDto data, Map<String, Object> calc) {

	        PayinRecords r = new PayinRecords();

	        BigDecimal amount = toBigDecimal(calc.get("amount"));
	        BigDecimal charges = toBigDecimal(calc.get("charges"));
	        BigDecimal gst = toBigDecimal(calc.get("gstCharges"));
	        BigDecimal netAmount = toBigDecimal(calc.get("netAmount"));

	        BigDecimal currentWallet =
	                BigDecimal.valueOf(clientRepository.getWalletBalance(data.getUserId()));

	        BigDecimal updatedWallet = currentWallet.add(netAmount);

	        int updated = clientRepository
	                .updateWalletBalance(updatedWallet.doubleValue(), data.getUserId());

	        if (updated == 0) {
	            r.setStatus("FAILED");
	            return r;
	        }

	        r.setOrderId(data.getOrderId());
	        r.setUserId(data.getUserId());

	        r.setName(nz(data.getName()));
	        r.setEmail(nz(data.getEmail()));
	        r.setMobile(nz(data.getMobile()));
	        r.setAddress(nz(data.getAddress()));
	        r.setPaymentMethod(nz(data.getPaymentMethod()));
	        r.setAccNumber(nz(data.getAccountNo()));
	        r.setNumber(nz(data.getMobile()));

	        r.setAmount(amount.doubleValue());
	        r.setCharges(charges.doubleValue());
	        r.setGstCharges(gst.doubleValue());
	        r.setTotalCharges(charges.add(gst).doubleValue());
	        r.setFinalAmount(netAmount.doubleValue());

	        r.setCurrentWalet(currentWallet.doubleValue());
	        r.setCurrentBalance(currentWallet.doubleValue());
	        r.setUpdatedBalance(updatedWallet.doubleValue());
	        r.setSettlementStatus("PENDING");
	        r.setStatus("PENDING");
	        r.setStatusCode("TXNP");
	        r.setTimeStamp(LocalDateTime.now().toString());
	        payinRepository.save(r);
	        return r;
	    }

	    private Map<String, Object> buildPayinResponse(PayinRecords r) {

	        Map<String, Object> resp = new HashMap<>();

	        resp.put("orderId", r.getOrderId());
	        resp.put("status", r.getStatus());
	        resp.put("statusCode", r.getStatusCode());
	        resp.put("amount", r.getAmount());
	        resp.put("charges", r.getCharges());
	        resp.put("gstCharges", r.getGstCharges());
	        resp.put("totalCharges", r.getTotalCharges());
	        resp.put("finalAmount", r.getFinalAmount());
	        resp.put("userId", r.getUserId());
	        resp.put("settlementStatus", r.getSettlementStatus());
	        resp.put("timeStamp", r.getTimeStamp());

	        return resp;
	    }

	    
	    // UTIL METHODS
	    private BigDecimal safeBig(String s) {
	        try {
	            return new BigDecimal(s);
	        } catch (Exception e) {
	            return BigDecimal.ZERO;
	        }
	    }

	    private BigDecimal toBigDecimal(Object o) {
	        if (o instanceof BigDecimal) return (BigDecimal) o;
	        if (o instanceof Number) return BigDecimal.valueOf(((Number) o).doubleValue());
	        return safeBig(o.toString());
	    }

	    private boolean isClientActive(Client c) {
	        try {
	            return "ACTIVE".equalsIgnoreCase(c.getStatus());
	        } catch (Exception e) {
	            return false;
	        }
	    }

	    private String nz(String s) {
	        return s == null ? "" : s;
	    }

	   



	

	

	@Override
	public String holdAmount(String userId, String txnId) {
		PayinRecords rec = payinRepository.findByTrxnidAndUserId(txnId, userId);
		rec.setSettlementStatus("HOLD");
		payinRepository.save(rec);
		return "Amount held successfully";
	}

	@Override
	public List<CollectionHistoryDto> getHistory(String userId, String fromDate, String toDate, String utr,
			String txnId) {
		// TODO Auto-generated method stub
		return null;
	}

	// PayIn Reports - Get all PayIn records by date range (ADMIN)

	@Override
	public ResponseEntity<?> getAllPayinRecordsReport(LocalDate fromDate, LocalDate toDate) {
		Map<String, Object> response = new HashMap<>();

		try {
			logger.info("ADMIN: Fetching all payin records report from {} to {}", fromDate, toDate);

			// Validate dates
			if (fromDate.isAfter(toDate)) {
				response.put("success", false);
				response.put("message", "From date cannot be after to date");
				response.put("errorCode", "INVALID_DATE_RANGE");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			// Convert LocalDate to LocalDateTime
			LocalDateTime fromDateTime = fromDate.atStartOfDay();
			LocalDateTime toDateTime = toDate.atTime(23, 59, 59);

			// Fetch records from repository
			List<PayinRecords> payinRecords = payinRepository.findAllPayinRecordsBetweenDates(fromDateTime, toDateTime);

			// Return only PayinRecords entity data
			response.put("success", true);
			response.put("message", "PayIn records fetched successfully");
			response.put("fromDate", fromDate.toString());
			response.put("toDate", toDate.toString());
			response.put("totalRecords", payinRecords.size());
			response.put("data", payinRecords);

			logger.info("Successfully fetched {} payin records for date range {} to {}", payinRecords.size(), fromDate,
					toDate);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.error("Error fetching all payin records report from {} to {}", fromDate, toDate, e);
			response.put("success", false);
			response.put("message", "Internal server error while fetching payin records report");
			response.put("errorCode", "INTERNAL_ERROR");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	public ResponseEntity<?> payinTransactionRecordsByClientId(String clientId, int page, int size) {

	    logger.info("Fetching pay-in transaction records for clientId={}, page={}, size={}",
	            clientId, page, size);

	    Pageable pageable = PageRequest.of(page, size, Sort.by("created_date").descending());

	    Page<PayinRecords> transactionsPage =
	            payinRepository.findByClientIdWithPagination(clientId, pageable);

	    if (transactionsPage.hasContent()) {

	        logger.info("Pay-in records found | clientId={} | count={}",
	                clientId, transactionsPage.getNumberOfElements());

	        ResponseDto response = ResponseDto.builder()
	                .status("OK")
	                .message("SUCCESS")
	                .data(transactionsPage.getContent())
	                .build();

	        return ResponseEntity.ok(response);

	    } else {

	        logger.warn("No pay-in records found for clientId={}", clientId);

	        ResponseDto response = ResponseDto.builder()
	                .status("OK")
	                .message("NO_CONTENT")
	                .data("No pay-in transaction records found")
	                .build();

	        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
	    }
	}


	@Override
	public ResponseEntity<?> getPayoutReportsByUserId(
	        String userId,
	        String status,
	        String paymentMethod,
	        LocalDate fromDate,
	        LocalDate toDate,
	        int page,
	        int size
	) {

	    logger.info("Fetching payout records for userId={}, page={}, size={}", userId, page, size);

	    int offset = page * size;

	    List<PayoutRecords> records =
	            payoutRepository.findAllPayoutByUserId(
	                    userId, status, paymentMethod, fromDate, toDate, offset, size
	            );

	    if (records != null && !records.isEmpty()) {

	        logger.info("Payout records found | userId={} | count={}", userId, records.size());

	        ResponseDto response = ResponseDto.builder()
	                .status("OK")
	                .message("SUCCESS")
	                .data(records)
	                .build();

	        return ResponseEntity.ok(response);
	    }

	    logger.warn("No payout records found | userId={}", userId);

	    ResponseDto response = ResponseDto.builder()
	            .status("OK")
	            .message("NO_CONTENT")
	            .data("No payout records found for given filters")
	            .build();

	    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
	}


	@Override
	public ResponseEntity<?> approvedPrefundHistory(String userId, int page, int size) {

	    logger.info(
	        "approvedPrefundHistory() → Fetching APPROVED prefund | userId={}, page={}, size={}",
	        userId, page, size
	    );

	    int offset = page * size;

	    List<Map<String, Object>> list =
	            prefundRequestRepository.findByStatusAndUserId(
	                    "APPROVED", userId, offset, size
	            );

	    if (!list.isEmpty()) {
	        return ResponseEntity.ok(
	                ResponseDto.builder()
	                        .status("OK")
	                        .message("SUCCESS")
	                        .data(list)
	                        .build()
	        );
	    }

	    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
	            ResponseDto.builder()
	                    .status("OK")
	                    .message("SUCCESS")
	                    .data("No approved prefund records found")
	                    .build()
	    );
	}


	@Override
	public ResponseEntity<?> rejectedPrefundHistory(String userId, int page, int size) {

	    logger.info(
	        "rejectedPrefundHistory() → Fetching REJECTED prefund | userId={}, page={}, size={}",
	        userId, page, size
	    );

	    int offset = page * size;

	    List<Map<String, Object>> list =
	            prefundRequestRepository.findByStatusAndUserId(
	                    "REJECTED", userId, offset, size
	            );

	    if (!list.isEmpty()) {
	        return ResponseEntity.ok(
	                ResponseDto.builder()
	                        .status("OK")
	                        .message("SUCCESS")
	                        .data(list)
	                        .build()
	        );
	    }

	    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
	            ResponseDto.builder()
	                    .status("OK")
	                    .message("SUCCESS")
	                    .data("No rejected prefund records found")
	                    .build()
	    );
	}


	public ResponseEntity<?> raiseTicket(String userId, SupportTicketRequestDTO request) {

		SupportTicket ticket = new SupportTicket();
		ticket.setUserId(userId);
		ticket.setClientName(request.getClientName());
		ticket.setSubject(request.getSubject());
		ticket.setClientEmail(request.getClientEmail());
		ticket.setDescription(request.getDescription());

		SupportTicket saved = ticketRepo.save(ticket);

		emailService.changePassword("ps328104@gmail.com",
				"New Ticket Raised\n\n" + "Ticket ID: " + saved.getTicketId() + "\n" + "User ID: " + userId + "\n"
						+ "Subject: " + request.getSubject() + "\n\n" + request.getDescription());

		return ResponseEntity.ok(Map.of("status", "SUCCESS", "ticketId", saved.getTicketId()));
	}

	@Override
	public CollectionHistoryDto getDetails(String userId, String txnId) {

		PayinRecords rec = payinRepository.findByTrxnidAndUserId(txnId, userId);

		return new CollectionHistoryDto(rec.getTrxnid(), rec.getName(), rec.getUtr(), rec.getStatus(),
				rec.getPaymentMethod(), String.format("₹%.2f", rec.getAmount()), rec.getCreatedDate().toString());
	}

	@Override
	public String refundAmount(String userId, String txnId) {
		PayinRecords rec = payinRepository.findByTrxnidAndUserId(txnId, userId);
		rec.setSettlementStatus("REFUND");
		rec.setStatus("REFUNDED");
		payinRepository.save(rec);
		return "Refund initiated";
	}

	private Map<String, Object> buildpayInRecordResponseData(PayoutRecords record) {
		logger.info("Building payout record response map for recordId={}", record.getSlNo());
		logger.debug("Payout record input data: {}", record);
		Map<String, Object> data = new HashMap<>();
		data.put("slNo", record.getSlNo());
		data.put("userId", record.getUserId());
		data.put("name", record.getName());
		data.put("email", record.getEmail());
		data.put("number", record.getNumber());
		data.put("accNumber", record.getAccNumber());
		data.put("ifsc", record.getIfsc());
		data.put("charges", record.getCharges());
		data.put("gstCharges", record.getGstCharges());
		data.put("amount", record.getAmount());
		data.put("finalAmount", record.getFinalAmount());
		data.put("status", record.getStatus());
		data.put("statusCode", record.getStatusCode());
		data.put("utr", record.getUtr());
		data.put("refundStatus", record.getRefundStatus());
		data.put("currentBalance", record.getCurrentBalance());
		data.put("updatedBalance", record.getUpdatedBalance());
		data.put("transferMode", record.getTransferMode());
		data.put("trxnId", record.getOrderId());
		data.put("pgId", record.getPgId());
		data.put("errorMsg", record.getErrorMsg());
		data.put("createdDate", record.getCreatedDate());
		data.put("updatedDate", record.getUpdatedDate());
		logger.debug("Final payout record response map generated: {}", data);
		return data;
	}

// Hold Amount - Admin holds specific amount from PayIn transaction

	@Override
	@Transactional
	public ResponseEntity<?> holdPayinAmount(@Valid HoldAmountDto holdAmountDto) {
		Map<String, Object> response = new HashMap<>();

		try {
			logger.info("Hold amount request received for orderId: {}, userId: {}, holdAmount: {}",
					holdAmountDto.getOrderId(), holdAmountDto.getUserId(), holdAmountDto.getHoldAmount());

			// Step 1: Validate PayIn transaction exists
			PayinRecords payinRecord = payinRepository.findByOrderId(holdAmountDto.getOrderId());
			if (payinRecord == null) {
				logger.warn("PayIn transaction not found for orderId: {}", holdAmountDto.getOrderId());
				response.put("success", false);
				response.put("message", "PayIn transaction not found");
				response.put("errorCode", "TRANSACTION_NOT_FOUND");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}

			// Step 2: Validate userId matches
			if (!payinRecord.getUserId().equals(holdAmountDto.getUserId())) {
				logger.warn("UserId mismatch for orderId: {}. Expected: {}, Got: {}", holdAmountDto.getOrderId(),
						payinRecord.getUserId(), holdAmountDto.getUserId());
				response.put("success", false);
				response.put("message", "User ID does not match transaction");
				response.put("errorCode", "USER_ID_MISMATCH");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			// Step 3: Validate transaction status is SUCCESS
			if (!"SUCCESS".equalsIgnoreCase(payinRecord.getStatus())) {
				logger.warn("Cannot hold amount - transaction status is not SUCCESS. Status: {}",
						payinRecord.getStatus());
				response.put("success", false);
				response.put("message", "Can only hold amount from successful transactions");
				response.put("errorCode", "INVALID_TRANSACTION_STATUS");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			// Step 4: Check if already held
			if ("ACTIVE".equalsIgnoreCase(payinRecord.getHoldStatus())) {
				logger.warn("Amount already on hold for orderId: {}. Current hold: {}", holdAmountDto.getOrderId(),
						payinRecord.getHoldAmount());
				response.put("success", false);
				response.put("message", "Amount is already on hold for this transaction");
				response.put("errorCode", "ALREADY_ON_HOLD");
				response.put("currentHoldAmount", payinRecord.getHoldAmount());
				response.put("currentHoldReason", payinRecord.getHoldReason());
				return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
			}

			// Step 5: Calculate current final amount (after charges)
			// finalAmount = amount - charges - gstCharges
			BigDecimal originalAmount = BigDecimal.valueOf(payinRecord.getAmount());
			BigDecimal charges = BigDecimal.valueOf(payinRecord.getCharges());
			BigDecimal gstCharges = BigDecimal.valueOf(payinRecord.getGstCharges());
			BigDecimal currentFinalAmount = originalAmount.subtract(charges).subtract(gstCharges);

			// Step 6: Validate hold amount
			BigDecimal holdAmount = BigDecimal.valueOf(holdAmountDto.getHoldAmount());
			if (holdAmount.compareTo(currentFinalAmount) > 0) {
				logger.warn("Hold amount {} exceeds available final amount {} for orderId: {}", holdAmount,
						currentFinalAmount, holdAmountDto.getOrderId());
				response.put("success", false);
				response.put("message", "Hold amount cannot exceed final amount");
				response.put("errorCode", "HOLD_AMOUNT_EXCEEDS_FINAL_AMOUNT");
				response.put("availableFinalAmount", currentFinalAmount.doubleValue());
				response.put("requestedHoldAmount", holdAmount.doubleValue());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			// Step 7: Calculate new final amount after hold
			BigDecimal newFinalAmount = currentFinalAmount.subtract(holdAmount);

			logger.info(
					"Hold amount calculation for orderId {}: originalAmount={}, charges={}, gst={}, "
							+ "currentFinalAmount={}, holdAmount={}, newFinalAmount={}",
					holdAmountDto.getOrderId(), originalAmount, charges, gstCharges, currentFinalAmount, holdAmount,
					newFinalAmount);

			// Step 8: Deduct hold amount from merchant's wallet
			Optional<Client> clientOpt = clientRepository.findByUserId(holdAmountDto.getUserId());
			if (clientOpt.isEmpty()) {
				logger.error("Client not found for userId: {}", holdAmountDto.getUserId());
				response.put("success", false);
				response.put("message", "Client not found");
				response.put("errorCode", "CLIENT_NOT_FOUND");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}

			Client client = clientOpt.get();
			BigDecimal currentWalletBalance = BigDecimal.valueOf(client.getWalletBalance());

			// Validate sufficient balance
			if (currentWalletBalance.compareTo(holdAmount) < 0) {
				logger.warn("Insufficient wallet balance for userId: {}. Available: {}, Required: {}",
						holdAmountDto.getUserId(), currentWalletBalance, holdAmount);
				response.put("success", false);
				response.put("message", "Insufficient wallet balance to hold amount");
				response.put("errorCode", "INSUFFICIENT_BALANCE");
				response.put("availableBalance", currentWalletBalance.doubleValue());
				response.put("requiredAmount", holdAmount.doubleValue());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}

			// Deduct from wallet
			BigDecimal newWalletBalance = currentWalletBalance.subtract(holdAmount);
			int walletUpdated = clientRepository.updateWalletBalance(newWalletBalance.doubleValue(),
					holdAmountDto.getUserId());

			if (walletUpdated == 0) {
				logger.error("Failed to update wallet balance for userId: {}", holdAmountDto.getUserId());
				response.put("success", false);
				response.put("message", "Failed to update wallet balance");
				response.put("errorCode", "WALLET_UPDATE_FAILED");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}

			logger.info("Wallet balance updated for userId: {}. Old: {}, New: {}", holdAmountDto.getUserId(),
					currentWalletBalance, newWalletBalance);

			// Step 9: Update PayIn record with hold details
			int recordsUpdated = payinRepository.updateHoldAmount(holdAmountDto.getOrderId(), holdAmount.doubleValue(),
					holdAmountDto.getHoldReason(), "ACTIVE", newFinalAmount.doubleValue(), LocalDateTime.now());

			if (recordsUpdated == 0) {
				// Rollback wallet deduction
				clientRepository.updateWalletBalance(currentWalletBalance.doubleValue(), holdAmountDto.getUserId());

				logger.error("Failed to update hold amount in PayIn record for orderId: {}",
						holdAmountDto.getOrderId());
				response.put("success", false);
				response.put("message", "Failed to update hold amount");
				response.put("errorCode", "HOLD_UPDATE_FAILED");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
			}

			// Step 10: Prepare success response
			Map<String, Object> holdDetails = new HashMap<>();
			holdDetails.put("orderId", holdAmountDto.getOrderId());
			holdDetails.put("userId", holdAmountDto.getUserId());
			holdDetails.put("originalAmount", originalAmount.doubleValue());
			holdDetails.put("charges", charges.doubleValue());
			holdDetails.put("gstCharges", gstCharges.doubleValue());
			holdDetails.put("previousFinalAmount", currentFinalAmount.doubleValue());
			holdDetails.put("holdAmount", holdAmount.doubleValue());
			holdDetails.put("holdReason", holdAmountDto.getHoldReason());
			holdDetails.put("newFinalAmount", newFinalAmount.doubleValue());
			holdDetails.put("holdStatus", "ACTIVE");
			holdDetails.put("previousWalletBalance", currentWalletBalance.doubleValue());
			holdDetails.put("newWalletBalance", newWalletBalance.doubleValue());
			holdDetails.put("holdDate", LocalDateTime.now().toString());

			response.put("success", true);
			response.put("message", "Amount held successfully and deducted from wallet");
			response.put("data", holdDetails);

			logger.info(
					"Hold amount processed successfully for orderId: {}. HoldAmount: {}, NewFinalAmount: {}, NewWalletBalance: {}",
					holdAmountDto.getOrderId(), holdAmount, newFinalAmount, newWalletBalance);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.error("Error processing hold amount for orderId: {}", holdAmountDto.getOrderId(), e);
			response.put("success", false);
			response.put("message", "Internal server error while processing hold amount");
			response.put("errorCode", "INTERNAL_ERROR");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	@Transactional
	public LienResponseDTO addLienAmount(LienAmountDTO lienAmountDTO) {

		String userId = lienAmountDTO.getUserId();
		String description = lienAmountDTO.getDescription();

		try {
			logger.info("LIEN_ADD_INITIATED | userId={}", userId);

			// Step 1: Validate user exists in clients table
			long clientCount = lienRepository.countClientByUserId(userId);
			if (clientCount == 0) {
				logger.error("LIEN_ADD_FAILED | userId={} | reason=USER_NOT_FOUND", userId);
				return new LienResponseDTO(false, "User does not exist in clients table");
			}

			// Step 2: Check if active lien already exists
			Optional<LienAmount> existingLien = lienRepository.findByUserId(userId);
			if (existingLien.isPresent()) {
				logger.error("LIEN_ADD_FAILED | userId={} | reason=DUPLICATE_LIEN", userId);
				return new LienResponseDTO(false, "Active lien already exists for this user");
			}

			// Step 3: Calculate total approved prefund amount
			BigDecimal totalApprovedAmount = prefundRequestRepository.getTotalApprovedAmount(userId);

			logger.info("TOTAL_APPROVED_AMOUNT_CALCULATED | userId={} | amount={}", userId, totalApprovedAmount);

			// 🔒 Mandatory business validation
			if (totalApprovedAmount == null || totalApprovedAmount.compareTo(BigDecimal.ZERO) <= 0) {

				logger.error("LIEN_ADD_FAILED | userId={} | reason=NO_LIENABLE_AMOUNT | totalApprovedAmount={}", userId,
						totalApprovedAmount);

				return new LienResponseDTO(false, "No approved amount available for lien");
			}

			// Step 4: Insert into lien_amount table
			LienAmount lienAmount = new LienAmount();
			lienAmount.setUserId(userId);
			lienAmount.setAmount(totalApprovedAmount.doubleValue());
			lienAmount.setDescription(description);
			lienAmount.setCreatedDate(new Date());

			LienAmount savedLien = lienRepository.save(lienAmount);

			logger.info("LIEN_AMOUNT_INSERTED | userId={} | lienId={} | amount={}", userId, savedLien.getId(),
					savedLien.getAmount());

			// Step 5: Insert into lien_history table
			LienHistory lienHistory = new LienHistory();
			lienHistory.setUserId(userId);
			lienHistory.setAmount(totalApprovedAmount.toPlainString());
			lienHistory.setStatus("LIEN_APPLIED");
			lienHistory.setReference(null);
			lienHistory.setTimestamp(new Date());

			LienHistory savedHistory = lienHistoryRepository.save(lienHistory);

			logger.info("LIEN_HISTORY_INSERTED | userId={} | historyId={} | status=LIEN_APPLIED", userId,
					savedHistory.getId());

			// Step 6: Update prefundrequests.lien_status
			int updatedRows = prefundRequestRepository.updateLienStatusForApprovedRequests(userId, "LIEN_APPLIED");

			logger.info("PREFUND_STATUS_UPDATED | userId={} | rowsAffected={} | newStatus=LIEN_APPLIED", userId,
					updatedRows);

			logger.info("LIEN_ADDED_SUCCESSFULLY | userId={} | amount={}", userId, totalApprovedAmount);

			return new LienResponseDTO(true, "Lien amount added successfully", savedLien);

		} catch (Exception e) {
			logger.error("LIEN_ADD_FAILED | userId={} | error={}", userId, e.getMessage(), e);
			throw new RuntimeException("Failed to add lien amount", e);
		}
	}

	@Override
	@Transactional
	public LienResponseDTO deleteLienAmount(String userId) {
		try {
			logger.info("LIEN_DELETE_INITIATED | userId={}", userId);

			// Step 1: Validate active lien exists
			Optional<LienAmount> existingLien = lienRepository.findByUserId(userId);
			if (existingLien.isEmpty()) {
				logger.error("LIEN_DELETE_FAILED | userId={} | reason=NO_ACTIVE_LIEN", userId);
				return new LienResponseDTO(false, "No active lien found for this user");
			}

			// Step 2: Fetch the existing lien amount for history logging
			LienAmount lienAmount = existingLien.get();
			Double amount = lienAmount.getAmount();
			logger.info("ACTIVE_LIEN_FOUND | userId={} | lienId={} | amount={}", userId, lienAmount.getId(), amount);

			// Step 3: Delete from lien_amount table
			lienRepository.deleteLien(userId);
			logger.info("LIEN_AMOUNT_DELETED | userId={} | amount={}", userId, amount);

			// Step 4: Insert into lien_history table
			LienHistory lienHistory = new LienHistory();
			lienHistory.setUserId(userId);
			lienHistory.setAmount(String.valueOf(amount));
			lienHistory.setStatus("RELEASED");
			lienHistory.setReference(null); // As per your requirement
			lienHistory.setTimestamp(new Date());

			LienHistory savedHistory = lienHistoryRepository.save(lienHistory);
			logger.info("LIEN_HISTORY_INSERTED | userId={} | historyId={} | status=RELEASED", userId,
					savedHistory.getId());

			// Step 5: Update prefundrequests.lien_status for all LIEN_APPLIED requests
			int updatedRows = prefundRequestRepository.updateLienStatusForAppliedRequests(userId, "LIEN_DELETED");
			logger.info("PREFUND_STATUS_UPDATED | userId={} | rowsAffected={} | newStatus=LIEN_DELETED", userId,
					updatedRows);

			logger.info("LIEN_DELETED | userId={} | amount={} | SUCCESS", userId, amount);
			return new LienResponseDTO(true, "Lien amount deleted successfully");

		} catch (Exception e) {
			logger.error("LIEN_DELETE_FAILED | userId={} | error={}", userId, e.getMessage(), e);
			throw new RuntimeException("Failed to delete lien amount: " + e.getMessage(), e);
		}
	}

	@Override
	public ResponseEntity<?> getPayinReportsByUserId(
	        String userId,
	        String status,
	        String paymentMethod,
	        LocalDate fromDate,
	        LocalDate toDate,
	        int page,
	        int size
	) {

	    logger.info("Fetching payin records for userId={}, page={}, size={}", userId, page, size);

	    int offset = page * size;

	    List<PayinRecords> records =
	            payinRepository.findAllPayinByUserId(
	                    userId, status, paymentMethod, fromDate, toDate, offset, size
	            );

	    if (records != null && !records.isEmpty()) {

	        logger.info("Payin records found | userId={} | count={}", userId, records.size());

	        ResponseDto response = ResponseDto.builder()
	                .status("OK")
	                .message("SUCCESS")
	                .data(records)
	                .build();

	        return ResponseEntity.ok(response);
	    }

	    logger.warn("No payin records found | userId={}", userId);

	    ResponseDto response = ResponseDto.builder()
	            .status("OK")
	            .message(records.isEmpty() ? "NO_DATA" : "SUCCESS")
	            .data(records)
	            .build();

	    return ResponseEntity.ok(response);

	}


	@Override
	public ResponseEntity<?> prefundHistory(String userId) {

		logger.info("prefundHistory() → Fetching prefund history | userId={}", userId);

		List<Map<String, Object>> list = prefundRequestRepository.findPendingListByUserId(userId);

		logger.info("prefundHistory() → {} prefund records found", list.size());

		if (!list.isEmpty()) {
			return ResponseEntity.ok(ResponseDto.builder().status("OK").message("SUCCESS").data(list).build());
		}

		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
				ResponseDto.builder().status("OK").message("SUCCESS").data("No data present in the list..!").build());
	}

	// Get Payin Wallet Summary - Returns total amount sum and all payin
	// transactions for a merchant

	@Override
	public ResponseEntity<?> getPayinWalletSummary(String userId) {
		Map<String, Object> response = new HashMap<>();

		try {
			logger.info("Fetching payin wallet summary for userId: {}", userId);

			// Step 1: Validate user exists
			Optional<Client> clientOpt = clientRepository.findByUserId(userId);
			if (clientOpt.isEmpty()) {
				logger.warn("Client not found for userId: {}", userId);
				response.put("success", false);
				response.put("message", "Client not found");
				response.put("errorCode", "CLIENT_NOT_FOUND");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}

			// Step 2: Fetch all payin records for this user (all statuses)
			List<PayinRecords> payinRecords = payinRepository.findByClientId(userId);

			if (payinRecords == null || payinRecords.isEmpty()) {
				logger.info("No payin transactions found for userId: {}", userId);
				response.put("success", true);
				response.put("message", "No payin transactions found");
				response.put("totalAmount", 0.0);
				response.put("transactionCount", 0);
				response.put("data", new ArrayList<>());
				return ResponseEntity.ok(response);
			}

			// Step 3: Calculate total amount
			double totalAmount = payinRecords.stream().mapToDouble(PayinRecords::getAmount).sum();

			// Step 4: Build transaction list
			List<Map<String, Object>> transactions = new ArrayList<>();
			for (PayinRecords record : payinRecords) {
				Map<String, Object> txn = new HashMap<>();
				txn.put("txnId", record.getTrxnid() != null ? record.getTrxnid() : "");
				txn.put("customerName", record.getName() != null ? record.getName() : "");
				txn.put("status", record.getStatus() != null ? record.getStatus() : "");
				txn.put("method", record.getPaymentMethod() != null ? record.getPaymentMethod() : "");
				txn.put("amount", record.getAmount());
				txn.put("date", record.getCreatedDate() != null ? record.getCreatedDate().toString() : "");
				transactions.add(txn);
			}

			// Step 5: Prepare success response
			response.put("success", true);
			response.put("message", "Payin wallet summary fetched successfully");
			response.put("totalAmount", totalAmount);
			response.put("transactionCount", payinRecords.size());
			response.put("data", transactions);

			logger.info("Payin wallet summary fetched successfully for userId: {} | Total: {} | Count: {}", userId,
					totalAmount, payinRecords.size());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.error("Error fetching payin wallet summary for userId: {}", userId, e);
			response.put("success", false);
			response.put("message", "Internal server error while fetching payin wallet summary");
			response.put("errorCode", "INTERNAL_ERROR");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	//Get Payout Wallet Summary - Returns total amount sum and all payout
	  //

	@Override
	public ResponseEntity<?> getPayoutWalletSummary(String userId) {

	    Map<String, Object> response = new HashMap<>();

	    try {
	        logger.info("Fetching payout wallet summary for userId: {}", userId);

	        // 1. Validate client exists
	        Optional<Client> clientOpt = clientRepository.findByUserId(userId);
	        if (clientOpt.isEmpty()) {
	            response.put("success", false);
	            response.put("message", "Client not found");
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	        }

	        Client client = clientOpt.get();

	        // 2. Validate merchant type = PAYOUT
	        if (!"PAYOUT".equalsIgnoreCase(client.getMerchantType())) {
	            response.put("success", false);
	            response.put("message", "Merchant is not a PAYOUT type");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	        }

	        // 3. Get account balance (original balance)
	        BigDecimal accountBal = client.getAccountBal();
	        if (accountBal == null) {
	            accountBal = BigDecimal.ZERO;
	        }

	        // 4. Get total locked amount
	        BigDecimal totalLockedAmount =
	                lockedFundsRepository.getTotalLockedAmountByUserId(userId);

	        if (totalLockedAmount == null) {
	            totalLockedAmount = BigDecimal.ZERO;
	        }

	        // 5. Calculate available balance (runtime only)
	        BigDecimal availableBalance = accountBal.subtract(totalLockedAmount);

	        // 6. Do NOT allow negative wallet
	        if (availableBalance.compareTo(BigDecimal.ZERO) < 0) {
	            response.put("success", false);
	            response.put("message", "Insufficient available balance");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	        }

	        // 7. Success response (NO transactions, NO prefund, NO count)
	        response.put("success", true);
	        response.put("message", "Payout wallet summary fetched successfully");
	        response.put("totalAmount", availableBalance);

	        logger.info(
	                "Payout wallet summary | userId={} | accountBal={} | lockedAmount={} | availableBalance={}",
	                userId, accountBal, totalLockedAmount, availableBalance
	        );

	        return ResponseEntity.ok(response);

	    } catch (Exception e) {
	        logger.error("Error fetching payout wallet summary for userId: {}", userId, e);
	        response.put("success", false);
	        response.put("message", "Internal server error while fetching payout wallet summary");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	/**
	 * Get Locked Funds Summary - Returns total locked amount and all locked fund
	 * records for a merchant
	 */
	@Override
	public ResponseEntity<?> getLockedFundsSummary(String userId) {
		Map<String, Object> response = new HashMap<>();

		try {
			logger.info("Fetching locked funds summary for userId: {}", userId);

			// Step 1: Validate user exists
			Optional<Client> clientOpt = clientRepository.findByUserId(userId);
			if (clientOpt.isEmpty()) {
				logger.warn("Client not found for userId: {}", userId);
				response.put("success", false);
				response.put("message", "Client not found");
				response.put("errorCode", "CLIENT_NOT_FOUND");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}

			// Step 2: Fetch all locked funds for this user
			List<LockedFunds> lockedFunds = lockedFundsRepository.findByUserId(userId);

			if (lockedFunds == null || lockedFunds.isEmpty()) {
				logger.info("No locked funds found for userId: {}", userId);
				response.put("success", true);
				response.put("message", "No locked funds found");
				response.put("totalLockedAmount", 0.0);
				response.put("lockedFundsCount", 0);
				response.put("data", new ArrayList<>());
				return ResponseEntity.ok(response);
			}

			// Step 3: Calculate total locked amount
			BigDecimal totalLockedAmount = lockedFunds.stream().map(LockedFunds::getAmountLocked)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			// Step 4: Build locked funds list
			List<Map<String, Object>> lockedFundsList = new ArrayList<>();
			for (LockedFunds fund : lockedFunds) {
				Map<String, Object> item = new HashMap<>();
				item.put("id", fund.getId());
				item.put("customerName", fund.getMerchantName() != null ? fund.getMerchantName() : "");
				item.put("amount", fund.getAmountLocked() != null ? fund.getAmountLocked().doubleValue() : 0.0);
				item.put("date", fund.getLockedDate() != null ? fund.getLockedDate().toString() : "");
				lockedFundsList.add(item);
			}

			// Step 5: Prepare success response
			response.put("success", true);
			response.put("message", "Locked funds summary fetched successfully");
			response.put("totalLockedAmount", totalLockedAmount.doubleValue());
			response.put("lockedFundsCount", lockedFunds.size());
			response.put("data", lockedFundsList);

			logger.info("Locked funds summary fetched successfully for userId: {} | Total: {} | Count: {}", userId,
					totalLockedAmount, lockedFunds.size());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.error("Error fetching locked funds summary for userId: {}", userId, e);
			response.put("success", false);
			response.put("message", "Internal server error while fetching locked funds summary");
			response.put("errorCode", "INTERNAL_ERROR");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	public ResponseEntity<?> prefundHistoryAll(String userId, int page, int size) {

	    logger.info(
	        "prefundHistoryAll() → Fetching ALL prefund history | userId={}, page={}, size={}",
	        userId, page, size
	    );

	    int offset = page * size;

	    List<Map<String, Object>> list =
	            prefundRequestRepository.findAllByUserId(
	                    userId, offset, size
	            );

	    logger.info(
	        "prefundHistoryAll() → {} prefund records found (page result)",
	        list.size()
	    );

	    if (!list.isEmpty()) {
	        return ResponseEntity.ok(
	                ResponseDto.builder()
	                        .status("OK")
	                        .message("SUCCESS")
	                        .data(list)
	                        .build()
	        );
	    }

	    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
	            ResponseDto.builder()
	                    .status("OK")
	                    .message("SUCCESS")
	                    .data("No prefund records found")
	                    .build()
	    );
	}

	@Override
	public ResponseEntity<?> lienAmountListByUserId(String userId) {

		logger.info("lienAmountListByUserId() → Fetching lien amount list | userId={}", userId);

		List<Map<String, Object>> list = this.lienRepository.findLienAmountListByUserId(userId);

		logger.info("lienAmountListByUserId() → {} records found", list.size());

		if (!list.isEmpty()) {
			return ResponseEntity.ok(ResponseDto.builder().status("OK").message("SUCCESS").data(list).build());
		}

		logger.warn("lienAmountListByUserId() → No records found | userId={}", userId);

		return ResponseEntity.ok(ResponseDto.builder().status("NO_CONTENCT") // kept same as your style
				.message("SUCCESS").data("No records found..!").build());
	}

	@Override
	public ResponseEntity<?> prefundHistory(String userId, int page, int size) {

	    logger.info(
	        "prefundHistory(client) → Fetching prefund history | userId={}, page={}, size={}",
	        userId, page, size
	    );

	    Pageable pageable = PageRequest.of(page, size);
	    Page<Map<String, Object>> pageResult =
	            prefundRequestRepository.findPendingPrefundByUserId(userId, pageable);

	    logger.info(
	        "prefundHistory(client) → {} records found",
	        pageResult.getTotalElements()
	    );

	    if (!pageResult.isEmpty()) {

	        Map<String, Object> responseData = new HashMap<>();
	        responseData.put("content", pageResult.getContent());
	        responseData.put("currentPage", pageResult.getNumber());
	        responseData.put("pageSize", pageResult.getSize());
	        responseData.put("totalElements", pageResult.getTotalElements());
	        responseData.put("totalPages", pageResult.getTotalPages());

	        ResponseDto response = ResponseDto.builder()
	                .status("OK")
	                .message("SUCCESS")
	                .data(responseData)
	                .build();

	        return ResponseEntity.ok(response);
	    }

	    ResponseDto response = ResponseDto.builder()
	            .status("OK")
	            .message("SUCCESS")
	            .data("No prefund records found for this user")
	            .build();

	    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
	}


	@Override
	public ResponseEntity<?> allTrasactionCountAndAmountOverall(String clientId) {

	    logger.info("Fetching OVERALL transaction summary for clientId: {}", clientId);

	    Map<String, Object> map = new HashMap<>();

	    String successCount = "0", successAmount = "0";
	    String pendingCount = "0", pendingAmount = "0";
	    String failCount    = "0", failAmount    = "0";

	    List<Map<String, Object>> records =
	            payoutRepository.transactionCountAndAmountOverallByClient(clientId);

	    logger.debug("Overall transaction summary records for clientId {}: {}", clientId, records);

	    if (!records.isEmpty()) {

	        for (Map<String, Object> value : records) {

	            String status = value.get("status").toString();

	            if ("SUCCESS".equals(status)) {
	                successCount  = value.get("count").toString();
	                successAmount = value.get("amount").toString();
	            } else if ("PENDING".equals(status)) {
	                pendingCount  = value.get("count").toString();
	                pendingAmount = value.get("amount").toString();
	            } else {
	                failCount  = value.get("count").toString();
	                failAmount = value.get("amount").toString();
	            }
	        }
	    }

	    map.put("successCount", successCount);
	    map.put("successAmount", successAmount);
	    map.put("pendingCount", pendingCount);
	    map.put("pendingAmount", pendingAmount);
	    map.put("failCount", failCount);
	    map.put("failAmount", failAmount);

	    logger.info("OVERALL transaction summary for clientId {}: {}", clientId, map);

	    ResponseDto response = ResponseDto.builder()
	            .status("OK")
	            .message("SUCCESS")
	            .data(map)
	            .build();

	    return ResponseEntity.ok(response);
	}


    @Override
    public ResponseEntity<?> payGorderCreate(PayinDto data) {
        String url = "https://apiv2.payg.in/payment/api/order/createIntent";

        RestTemplate restTemplate = new RestTemplate();
        Map<String,Object> UserDefinedData = new HashMap<>();
        UserDefinedData.put("UserDefined1", "");

        Map<String,Object> TransactionData = new HashMap<>();
        TransactionData.put("AcceptedPaymentTypes", "");
        TransactionData.put("PaymentType", "UPIINTENT");
        TransactionData.put("SurchargeType", "");
        TransactionData.put("SurchargeValue", "");
        TransactionData.put("RefTransactionId", "");
        TransactionData.put("IndustrySpecificationCode", "");
        TransactionData.put("PartialPaymentOption", "");

        Map<String,Object> OrderAmountData = new HashMap<>();
        TransactionData.put("AmountTypeDesc", "2");
        TransactionData.put("Amount", "2");

        Map<String,Object> CustomerData = new HashMap<>();
        CustomerData.put("CustomerId", "");
        CustomerData.put("CustomerNotes", "");
        CustomerData.put("FirstName", data.getName());
        CustomerData.put("LastName", "");
        CustomerData.put("MobileNo", data.getMobile());
        CustomerData.put("Email", data.getEmail());
        CustomerData.put("EmailReceipt", true);
        CustomerData.put("BillingAddress", data.getAddress());
        CustomerData.put("BillingCity", "");
        CustomerData.put("BillingState", "");
        CustomerData.put("BillingCountry", "");
        CustomerData.put("BillingZipCode", "");
        CustomerData.put("ShippingFirstName", "");
        CustomerData.put("ShippingLastName", "");
        CustomerData.put("ShippingAddress", "");
        CustomerData.put("ShippingCity", "");
        CustomerData.put("ShippingState", "");
        CustomerData.put("ShippingCountry", "");
        CustomerData.put("ShippingZipCode", "");
        CustomerData.put("ShippingMobileNo", "");

        Map<String,Object> IntegrationData = new HashMap<>();
        IntegrationData.put("UserName", "");
        IntegrationData.put("Source", "");
        IntegrationData.put("IntegrationType", "");
        IntegrationData.put("HashData", "");
        IntegrationData.put("PlatformId", "");

        Map<String,Object> map = new HashMap<>();
        map.put("MID", "408000147774040");
        map.put("UniqueRequestId", data.getOrderId());
        map.put("UserDefinedData", UserDefinedData);
        map.put("RequestDateTime", "");
        map.put("RedirectUrl", "");
        map.put("TransactionData", TransactionData);
        map.put("OrderAmount", "20");
        map.put("OrderAmountData", OrderAmountData);
        map.put("CustomerData", CustomerData);
        map.put("IntegrationData", IntegrationData);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Basic "+Generator.base64encodedHeaders());

        HttpEntity entity = new HttpEntity(map, headers);
        System.out.println("entity: "+ entity);

        try{
            ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            System.out.println("response: " + response.getBody());
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            logger.error("PayG Payin API error for orderId: {} | Status: {} | Body: {}", data.getOrderId(),
                    e.getStatusCode(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

   

    public PhonePeOrderStatusResponse checkStatus(
            String merchantOrderId,
            boolean details,
            boolean errorContext
    ) {

        String token = authService.getAccessToken();

        String url = orderStatusBaseUrl + "/" + merchantOrderId +
                "/status?details=" + details +
                "&errorContext=" + errorContext;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "O-Bearer " + token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<PhonePeOrderStatusResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        request,
                        PhonePeOrderStatusResponse.class
                );

        return response.getBody();
    }


    /* --------------------------------------------------
       PAYIN CREATE
    -------------------------------------------------- */

    @Override
    public ResponseEntity<?> paymentPayinPhonepe(
            PayinDto data,
            String clientId,
            String clientSecretId,
            HttpServletRequest request) throws Exception {

        // ---------- BASIC VALIDATION ----------
        if (data.getOrderId() == null || data.getOrderId().isBlank()) {
        	return ResponseEntity.badRequest()
        	        .body(ResponseDto.builder()
        	                .status("BAD_REQUEST")
        	                .message("Error")
        	                .data("OrderId is mandatory")
        	                .build());
        }

        if (payinRepository.findByOrderId(data.getOrderId()) != null) {
        	return ResponseEntity.badRequest()
        	        .body(ResponseDto.builder()
        	                .status("BAD_REQUEST")
        	                .message("Error")
        	                .data("OrderId is mandatory")
        	                .build());
        }

        // ---------- CHARGES ----------
        Map<String, Object> calc = payinChargesCalculations(data);
        if (!Boolean.TRUE.equals(calc.get("configured"))) {
        	return ResponseEntity.badRequest()
        	        .body(ResponseDto.builder()
        	                .status("BAD_REQUEST")
        	                .message("Error")
        	                .data("OrderId is mandatory")
        	                .build());
        }

        // ---------- CALL PHONEPE ----------
        ResponseEntity<String> phonePeResp = callPhonePe(data);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(phonePeResp.getBody());

        String state = root.path("data").path("state").asText();
        String redirectUrl = root.path("data").path("redirectUrl").asText("");

        // ---------- SAVE ONLY IF PENDING ----------
        if ("PENDING".equalsIgnoreCase(state)) {
            savePendingPayin(data, calc);
        }

        // ---------- BUILD API RESPONSE ----------
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", data.getOrderId());
        response.put("status", state);
        response.put("statusCode", "TXNP");
        response.put("name", data.getName());
        response.put("email", data.getEmail());
        response.put("mobile", data.getMobile());
        response.put("address", data.getAddress());
        response.put("paymentMethod", data.getPaymentMethod());
        response.put("amount", calc.get("amount").toString());
        response.put("charges", calc.get("charges").toString());
        response.put("gstCharges", calc.get("gstCharges").toString());
        response.put("totalCharges",
                toBigDecimal(calc.get("charges"))
                        .add(toBigDecimal(calc.get("gstCharges")))
                        .toString());
        response.put("finalAmount", calc.get("netAmount").toString());
        response.put("redirectRoute", redirectUrl);
        response.put("userId", data.getUserId());
        response.put("createdDate", LocalDateTime.now().toString());
        response.put("updatedDate", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }


    private ResponseEntity<String> callPhonePe(PayinDto data) {

        RestTemplate restTemplate = new RestTemplate();
        String accessToken = phonePeAuthService.getAccessToken();

        Map<String, Object> metaInfo = new HashMap<>();
        metaInfo.put("udf1", data.getUserId());
        metaInfo.put("udf2", data.getEmail());
        metaInfo.put("udf3", data.getMobile());

        Map<String, Object> merchantUrls = Map.of(
                "redirectUrl",
                "https://example.com/phonepe/callback?orderId=" + data.getOrderId()
        );

        Map<String, Object> body = new HashMap<>();
        body.put("merchantOrderId", data.getOrderId());
        body.put("amount", Long.parseLong(data.getAmount()) * 100); // RUPEES → PAISA
        body.put("expireAfter", 1200);
        body.put("metaInfo", metaInfo);
        body.put("paymentFlow", Map.of(
                "type", "PG_CHECKOUT",
                "merchantUrls", merchantUrls,
                "paymentModeConfig", Map.of(
                        "enabledPaymentModes",
                        List.of(Map.of("type", "UPI_INTENT"))
                )
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "O-Bearer " + accessToken);

        return restTemplate.exchange(
                PHONEPE_PAY_URL,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );
    }
    @Transactional
    private void savePendingPayin(PayinDto data, Map<String, Object> calc) {

        PayinRecords r = new PayinRecords();

        r.setOrderId(data.getOrderId());
        r.setUserId(data.getUserId());
        r.setName(nz(data.getName()));
        r.setEmail(nz(data.getEmail()));
        r.setMobile(nz(data.getMobile()));
        r.setAddress(nz(data.getAddress()));
        r.setPaymentMethod(nz(data.getPaymentMethod()));

        r.setAmount(toBigDecimal(calc.get("amount")).doubleValue());
        r.setCharges(toBigDecimal(calc.get("charges")).doubleValue());
        r.setGstCharges(toBigDecimal(calc.get("gstCharges")).doubleValue());
        r.setTotalCharges(
                toBigDecimal(calc.get("charges"))
                        .add(toBigDecimal(calc.get("gstCharges")))
                        .doubleValue()
        );
        r.setFinalAmount(toBigDecimal(calc.get("netAmount")).doubleValue());

        r.setStatus("PENDING");
        r.setSettlementStatus("PENDING");
        r.setStatusCode("TXNP");
        r.setTimeStamp(LocalDateTime.now().toString());

        payinRepository.save(r);
    }

    @Override
    public String savePhonePeCallBack(Map<String, Object> request) {

        System.out.println("PhonePe CallBack: " + request);

        /* =========================
           EXTRACT PAYLOAD
        ========================= */
        Map<String, Object> payload =
                (Map<String, Object>) request.get("payload");

        if (payload == null) {
            logger.warn("Invalid PhonePe callback: payload missing");
            return "SUCCESS"; // Always ack
        }

        String orderId = payload.get("merchantOrderId").toString();
        String state   = payload.get("state").toString();

        System.out.println("OrderId: " + orderId);
        System.out.println("State: " + state);

        /* =========================
           PAYMENT DETAILS
        ========================= */
        String utr = null;

        if (payload.containsKey("paymentDetails")) {
            Object detailsObj = payload.get("paymentDetails");
            if (detailsObj instanceof List) {
                List<Map<String, Object>> details =
                        (List<Map<String, Object>>) detailsObj;

                if (!details.isEmpty()) {
                    Map<String, Object> first = details.get(0);
                    if (first.containsKey("transactionId")) {
                        utr = first.get("transactionId").toString();
                    }
                }
            }
        }

        /* =========================
           STATUS MAPPING (SAME AS OLD)
        ========================= */
        String status;
        String statusCode;

        if ("COMPLETED".equalsIgnoreCase(state)) {
            status = "SUCCESS";
            statusCode = "TXNS";
        } else if ("FAILED".equalsIgnoreCase(state)) {
            status = "FAILED";
            statusCode = "TXNF";
        } else {
            status = "PENDING";
            statusCode = "TXNP";
        }

        System.out.println("Mapped status=" + status + ", code=" + statusCode);

        /* =========================
           UPDATE DB
        ========================= */
        Optional<PayinRecords> payin =
                Optional.ofNullable(payinRepository.findByOrderId(orderId));

        if (payin.isPresent()) {
            if ("SUCCESS".equals(status)) {
                payinRepository.updateStatusByOrderId(
                        "SUCCESS",
                        statusCode,
                        utr,
                        orderId
                );
            } else if ("FAILED".equals(status)) {
                payinRepository.updateStatusByOrderId(
                        "FAILED",
                        statusCode,
                        utr,
                        orderId
                );
            }
        }

        /* =========================
           SEND CALLBACK TO MERCHANT
        ========================= */
        if (payin.isPresent()) {

            String client = payin.get().getUserId();
            WebhookUrl web =
                    webhookRepository.findByUserIdAndType(client, "PAYIN");

            if (web != null) {
                RestTemplate restTemplate = new RestTemplate();

                Map<String, Object> callBackRequest = new HashMap<>();
                callBackRequest.put("orderId", orderId);
                callBackRequest.put("utr", utr);
                callBackRequest.put("paymentMethod", payin.get().getPaymentMethod());
                callBackRequest.put("amount", payload.get("amount").toString());
                callBackRequest.put("status", status);
                callBackRequest.put("statusCode", statusCode);
                callBackRequest.put("refundStatus", "");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<?> entity =
                        new HttpEntity<>(callBackRequest, headers);

                logger.info("Sending callback to merchant: {}", entity);

                ResponseEntity<String> response =
                        restTemplate.exchange(
                                web.getUrl(),
                                HttpMethod.POST,
                                entity,
                                String.class
                        );

                logger.info("Merchant callback response: {}", response.getBody());
            }
        }

        return "SUCCESS";
    }

    
    @Override
    public String handlePhonePeWebhook(Map<String, Object> request) {

        System.out.println("PhonePe webhook payload: " + request);

        String event = (String) request.get("event");
        Map<String, Object> payload =
                (Map<String, Object>) request.get("payload");

        if (payload == null) {
            return "SUCCESS";
        }

        String orderId = payload.get("merchantOrderId").toString();
        String state = payload.get("state").toString();

        String utr = null;
        if (payload.containsKey("paymentDetails")) {
            List<Map<String, Object>> paymentDetails =
                    (List<Map<String, Object>>) payload.get("paymentDetails");

            if (!paymentDetails.isEmpty()) {
                Map<String, Object> payment = paymentDetails.get(0);
                if (payment.containsKey("transactionId")) {
                    utr = payment.get("transactionId").toString();
                }
            }
        }

        // Map PhonePe -> internal
        String status;
        String statusCode;

        if ("COMPLETED".equalsIgnoreCase(state)) {
            status = "SUCCESS";
            statusCode = "TXNS";
        } else if ("FAILED".equalsIgnoreCase(state)) {
            status = "FAILED";
            statusCode = "TXNF";
        } else {
            status = "PENDING";
            statusCode = "TXNP";
        }

        Optional<PayinRecords> payin =
                Optional.ofNullable(payinRepository.findByOrderId(orderId));

        if (payin.isPresent()) {

            // Idempotency: do not re-update SUCCESS
            if (!"SUCCESS".equals(payin.get().getStatus())) {

                payinRepository.updateStatusByOrderId(
                        status,
                        statusCode,
                        utr,
                        orderId
                );
            }

            // Notify YOUR merchant (same as reference)
            WebhookUrl web =
                    webhookRepository.findByUserIdAndType(
                            payin.get().getUserId(), "PAYIN");

            if (web != null) {

                Map<String, Object> clientCallback = new HashMap<>();
                clientCallback.put("orderId", orderId);
                clientCallback.put("utr", utr);
                clientCallback.put("amount", payin.get().getAmount());
                clientCallback.put("status", status);
                clientCallback.put("statusCode", statusCode);
                clientCallback.put("refundStatus", "");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<?> entity =
                        new HttpEntity<>(clientCallback, headers);

                new RestTemplate().exchange(
                        web.getUrl(),
                        HttpMethod.POST,
                        entity,
                        String.class
                );
            }
        }

        // ALWAYS acknowledge PhonePe
        return "SUCCESS";
    }


    public ResponseEntity<?> buckBoxPayin(PayinDto data) throws Exception {
        String url = "https://payin-staging.bustto.com/api/merchant/external/transaction/payin";
//        String url = "https://payin.bustto.com/api/merchant/external/transaction/payin";
        RestTemplate restTemplate = new RestTemplate();

        Map<String,Object> delivery = new HashMap<>();
        delivery.put("recipient_name", data.getName());
        delivery.put("recipient_email",data.getEmail() );
        delivery.put("recipient_phone_number", "+91"+data.getPhone());
        delivery.put("user_id",data.getOrderId());

        Map<String,Object> request = new HashMap<>();
        request.put("amount", data.getAmount());
        request.put("external_order_id",data.getOrderId() );
//        request.put("success_url", "google.com");
//        request.put("failure_url", "google.com");
        request.put("delivery_details", delivery);
//        request.put("payment_mode", data.getTransferMode());

        System.out.println("request: "+ request);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Key", "3B5qKMWGFXYGlKfvXP1baZNrIc2VJDNk4WBcGS300QqvbuCAOxPl");
        headers.set("Authorization", "Bearer "+ Generator.generateBuckBoxToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        String encRequest = AES256EncryptionGSM.encryptPayload(request);
        System.out.println("encRequest: "+encRequest);

        Map<String, String> finalBody = new HashMap<>();
        finalBody.put("request", encRequest);

        HttpEntity<Map<String,String>> entity = new HttpEntity<>(finalBody, headers);
        System.out.println("entity: "+entity);

        try{
            ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            System.out.println("response: "+response.getBody());

            JSONObject resp1 = new JSONObject(response.getBody().toString());
            System.out.println("resp1: "+resp1);

            String decResponse = AES256EncryptionGSM.decryptPayload(resp1.get("response").toString()).toString();
            System.out.println("Decrypted response: " + decResponse);

            return ResponseEntity.ok(decResponse);
        }catch (HttpClientErrorException | HttpServerErrorException ex){
            String errorBody = ex.getResponseBodyAsString();

            System.out.println("HTTP Status: " + ex.getStatusCode());
            System.out.println("Raw error response: " + errorBody);

            String encryptedResponse = objectMapper.readTree(errorBody).get("response").asText();
            System.out.println("encryptedResponse: "+encryptedResponse );

            String decRespons = AES256EncryptionGSM.decryptPayload(encryptedResponse).toString();
            System.out.println("decResponse: "+ decRespons);
            return ResponseEntity.badRequest().body(decRespons);
        }

    }


}