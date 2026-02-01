package com.mippay.serviceImpl.Admin;

import com.mippay.dto.Admin.PayInChargesRequestDto;
import com.mippay.dto.Admin.PrefundApprovalDto;
import com.mippay.dto.Admin.PrefundRejectDto;
import com.mippay.dto.Admin.UpdateChargesDto;
import com.mippay.dto.Admin.VendorsDTO;

import com.mippay.dto.Client.ClientResponseDto;
import com.mippay.dto.Client.ResponseDto;

import com.mippay.entity.Admin.Charges;
import com.mippay.entity.Admin.PayInCharges;
import com.mippay.entity.Admin.User;
import com.mippay.entity.Admin.Vendors;

import com.mippay.entity.Client.*;

import com.mippay.exception.CustomBadRequestException;
import com.mippay.exception.CustomDuplicateEntryException;
import com.mippay.exception.CustomMethodArgumentNotValidException;

import com.mippay.helper.Generator;

import com.mippay.repository.Admin.ChargesRepository;
import com.mippay.repository.Admin.PayInChargesRepository;
import com.mippay.repository.Admin.UserRepository;
import com.mippay.repository.Admin.VendorsRepository;

import com.mippay.repository.Client.*;

import com.mippay.response.PayInChargesResponseDto;
import com.mippay.service.AdminService;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

	Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

	  @Autowired
	    private PayInChargesRepository payInChargesRepository;
	  
	@Autowired
	private ChargesRepository chargesRepository;
	@Autowired
	private ClientRepository clientRepository;
	@Autowired
	private PrefundRequestRepository prefundRequestRepository;
	@Autowired
	private Generator generator;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository adminRepository;
	@Autowired
	private AuthenticationRepository authRepository;
	@Autowired
	private PayoutRepository payoutRepository;
	@Autowired
	private WebhookRepository webhookRepository;
	@Autowired
	private IpRepository ipRepository;
	@Autowired
	private LienRepository lienRepository;
	@Autowired
	private LienHistoryRepository lienHistoryRepository;
	
	@Autowired
	private VendorsRepository vendorsRepository;

	@Override
	public String createAdmin(User request) {
		logger.info("createAdmin() → Starting admin creation for email: {}", request.getEmail());
		String adminId = generator.generateAdminId();
		logger.info("createAdmin() → Generated adminId: {}", adminId);
		String encryptPass = passwordEncoder.encode(request.getPassword());
		request.setPassword(encryptPass);
		request.setAdminId(adminId);
		logger.info("createAdmin() → Admin object after setting ID & encrypted password: {}", request);
		try {
			this.adminRepository.save(request);
			logger.info("createAdmin() → Admin saved successfully in database for adminId: {}", adminId);
			return "Admin onboarded successfully with admin id: " + adminId;
		} catch (CustomBadRequestException exception) {
			logger.error("createAdmin() → CustomBadRequestException: {}", exception.getMessage());
			throw new CustomBadRequestException(exception.getMessage());
		} catch (CustomMethodArgumentNotValidException e) {
			logger.error("createAdmin() → CustomMethodArgumentNotValidException: {}", e.getMessage());
			throw new CustomMethodArgumentNotValidException(e.getMessage());
		} catch (Exception exception) {
			logger.error("createAdmin() → Exception occurred: {}", exception.getMessage());
			if (exception.getMessage().contains("Duplicate entry")) {
				logger.warn("createAdmin() → Duplicate admin entry detected for email: {}", request.getEmail());
				return "Admin is already registered with the given email id";
			}
			throw new CustomDuplicateEntryException("Duplicate Entry");
		}
	}

	public ResponseEntity<?> setCharges(Charges data, HttpServletRequest req) {
		logger.info("setCharges() → Request received to set charges for clientId: {}, range: {} - {}", data.getUserId(),
				data.getFromRange(), data.getToRange());
		List<Charges> charges = this.chargesRepository.fetchByClientIdAndRange(data.getUserId(), data.getFromRange(),
				data.getToRange());
		logger.info("setCharges() → Existing charges found: {}", charges);
		if (charges.size() > 0) {
			logger.warn("setCharges() → Duplicate charge range detected for clientId: {}", data.getUserId());
			return ResponseEntity.badRequest().body("Charges already set for given clientId with same amount range..!");
		} else {
			this.chargesRepository.save(data);
			logger.info("setCharges() → Charges saved successfully for clientId: {}", data.getUserId());
			return ResponseEntity.ok("Charges set successfully..!");
		}
	}

	@Override
	public ResponseEntity<?> updateCharges(UpdateChargesDto data, HttpServletRequest req) {
		logger.info("updateCharges() → Request received to update charges for slNo: {}", data.getSlNo());
		Optional<Charges> charges = Optional.ofNullable(this.chargesRepository.fetchBySlNo(data.getSlNo()));
		if (charges.isPresent()) {
			logger.info("updateCharges() → Existing Charges found: {}", charges.get());
			this.chargesRepository.updateChargesBySlno(data.getChargesType(), data.getCharges(), data.getSlNo());
			logger.info("updateCharges() → Charges updated successfully for slNo: {}", data.getSlNo());
			return ResponseEntity.ok("Charges updated successfully..!");
		} else {
			logger.warn("updateCharges() → No charges found for slNo: {}", data.getSlNo());
			return ResponseEntity.badRequest().body("Please provide valid SlNo..!");
		}
	}

	@Override
	public ResponseEntity<?> getAllCharges() {
		logger.info("getAllCharges() → Fetching all charges");
		Map<String, Object> map = new HashMap<>();
		List<Map<String, Object>> chargeslist = this.chargesRepository.findAllCharges();
		logger.info("getAllCharges() → Number of charge records found: {}", chargeslist.size());
		if (chargeslist.isEmpty()) {
			logger.warn("getAllCharges() → No charges found");
			map.put("stauts", "Success");
			map.put("statusCode", "200");
			map.put("data", "No data found..!");
		} else {
			logger.info("getAllCharges() → Returning {} charge records", chargeslist.size());
			map.put("stauts", "Success");
			map.put("statusCode", "200");
			map.put("data", chargeslist);
		}
		return ResponseEntity.ok(map);
	}

	@Override
	public ResponseEntity<?> deleteChargesBySlNo(int slNo) {
		logger.info("deleteChargesBySlNo() → Request received to delete charges for slNo: {}", slNo);
		Optional<Charges> charges = Optional.ofNullable(this.chargesRepository.fetchBySlNo(slNo));
		if (charges.isPresent()) {
			logger.info("deleteChargesBySlNo() → Charges found for slNo: {}, proceeding with deletion", slNo);
			this.chargesRepository.deleteBySlno(slNo);
			ResponseDto response = ResponseDto.builder().status("OK").message("Success").data("Deleted Successfully..!")
					.build();
			logger.info("deleteChargesBySlNo() → Deletion successful for slNo: {}", slNo);
			return ResponseEntity.ok(response);
		} else {
			logger.warn("deleteChargesBySlNo() → No charges found for slNo: {}", slNo);
			ResponseDto response = ResponseDto.builder().status("Bad_Request").message("Error")
					.data("No data found for given slNo..!").build();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}

	@Override
	public User getAdminByEmail(String username) {
		logger.info("getAdminByEmail() → Fetching admin by email: {}", username);
		User admin = this.adminRepository.findByEmail(username).get();
		logger.info("getAdminByEmail() → Admin found for email: {}", username);
		return admin;
	}

	@Override
	public ResponseEntity<?> prefundHistory(int page, int size) {

	    logger.info("prefundHistory() → Fetching prefund request history with pagination");

	    Pageable pageable = PageRequest.of(page, size);
	    Page<Map<String, Object>> pageResult =
	            this.prefundRequestRepository.findAllList(pageable);

	    logger.info("prefundHistory() → {} records found", pageResult.getTotalElements());

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
	            .data("No data present in the list..!")
	            .build();

	    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
	}


	@Override
	public ResponseEntity<?> profileByUserId(String userId) {
		logger.info("profileByUserId() → Fetching admin profile for userId: {}", userId);
		Optional<User> user = this.adminRepository.findByUserId(userId);
		if (user.isPresent()) {
			logger.info("profileByUserId() → Admin found for userId: {}", userId);
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(user.get()).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("profileByUserId() → No admin found for userId: {}", userId);
			ResponseDto response = ResponseDto.builder().status("BAD_CREDS").message("ERROR")
					.data("Please enter valid admin Id..!").build();
			return ResponseEntity.badRequest().body(response);
		}
	}

	@Override
	public ResponseEntity<?> clientListAndWallets() {
		logger.info("clientListAndWallets() → Fetching client list with wallet information");
		List<Map<String, Object>> walletsList = this.adminRepository.walletsListWithName();
		logger.info("clientListAndWallets() → {} wallet records found", walletsList.size());
		if (walletsList.size() > 0) {
			logger.info("clientListAndWallets() → Returning wallet list data");
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(walletsList).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("clientListAndWallets() → No wallet data available");
			ResponseDto response = ResponseDto.builder().status("NO_CONTENT").message("SUCCESS")
					.data("No data available..!").build();
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}
	}

	@Override
	public ResponseEntity<?> updateStatusByUserId(Map<String, Object> userId) {
		logger.info("updateStatusByUserId() → Request received: {}", userId);
		String clientId = userId.get("clientId").toString();
		String status = userId.get("status").toString();
		logger.info("updateStatusByUserId() → Fetching client for clientId: {}", clientId);
		Optional<Client> client = this.clientRepository.findByUserId(clientId);
		if (client.isPresent()) {
			logger.info("updateStatusByUserId() → Client found. Updating status to: {}", status);
			this.clientRepository.updateStatus(status, clientId);
			logger.info("updateStatusByUserId() → Status updated successfully for clientId: {}", clientId);
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data("Updated successfully..!")
					.build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("updateStatusByUserId() → No client found for clientId: {}", clientId);
			ResponseDto response = ResponseDto.builder().status("OK").message("BAD_REQUEST")
					.data("Please provide valid clientId..!").build();
			return ResponseEntity.badRequest().body(response);
		}
	}

	@Override
	public ResponseEntity<?> deleteClient(String clientId) {
		logger.info("deleteClient() → Request received to delete client with clientId: {}", clientId);
		Optional<Client> client = this.clientRepository.findByUserId(clientId);
		if (client.isPresent()) {
			logger.info("deleteClient() → Client found. Proceeding with deletion for clientId: {}", clientId);
			this.clientRepository.deleteByClientId(clientId);
			logger.info("deleteClient() → Client deleted successfully for clientId: {}", clientId);
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data("Deleted successfully..!")
					.build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("deleteClient() → No client found for clientId: {}", clientId);
			ResponseDto response = ResponseDto.builder().status("OK").message("BAD_REQUEST")
					.data("Please provide valid clientId..!").build();
			return ResponseEntity.badRequest().body(response);
		}
	}


	@Override
	public ResponseEntity<?> allTransactions() {
		logger.info("allTransactions() → Fetching all payout transactions");
		List<PayoutRecords> transactions = this.payoutRepository.findAll();
		logger.info("allTransactions() → {} transactions found", transactions.size());
		if (transactions.size() > 0) {
			logger.info("allTransactions() → Returning transaction list");
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(transactions).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("allTransactions() → No transaction records found");
			ResponseDto response = ResponseDto.builder().status("OK").message("NO_CONTENT").data("No records found..!")
					.build();
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
		}
	}



	@Override
	public ResponseEntity<?> allTrasactionCountAndAmount() {
		logger.info("allTrasactionCountAndAmount() → Fetching transaction counts and amounts");
		Map<String, Object> map = new HashMap<>();
		String successCount = "0", successAmount = "0";
		String pendingAmount = "0", pendingCount = "0";
		String failAmount = "0", failCount = "0";
		List<Map<String, Object>> records = this.payoutRepository.transactionCountAndAmountToday();
		logger.info("allTrasactionCountAndAmount() → {} records found", records.size());
		if (records.size() > 0) {
			for (int i = 0; i < records.size(); i++) {
				Map<String, Object> value = records.get(i);
				String status = value.get("status").toString();
				logger.info("allTrasactionCountAndAmount() → Processing record {} with status: {}", i + 1, status);
				if (status.equals("SUCCESS")) {
					successCount = value.get("count").toString();
					successAmount = value.get("amount").toString();
					logger.info("SUCCESS → count: {}, amount: {}", successCount, successAmount);
				} else if (status.equals("PENDING")) {
					pendingCount = value.get("count").toString();
					pendingAmount = value.get("amount").toString();
					logger.info("PENDING → count: {}, amount: {}", pendingCount, pendingAmount);
				} else {
					failCount = value.get("count").toString();
					failAmount = value.get("amount").toString();
					logger.info("FAILED → count: {}, amount: {}", failCount, failAmount);
				}
			}
			map.put("successCount", successCount);
			map.put("successAmount", successAmount);
			map.put("pendingCount", pendingCount);
			map.put("pendingAmount", pendingAmount);
			map.put("failCount", failCount);
			map.put("failAmount", failAmount);
			logger.info("allTrasactionCountAndAmount() → Final aggregated map: {}", map);
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(map).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("allTrasactionCountAndAmount() → No transaction records found, returning zeros");
			map.put("successCount", successCount);
			map.put("successAmount", successAmount);
			map.put("pendingCount", pendingCount);
			map.put("pendingAmount", pendingAmount);
			map.put("failCount", failCount);
			map.put("failAmount", failAmount);
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(map).build();
			return ResponseEntity.ok(response);
		}
	}

	@Override
	public ResponseEntity<?> filterByUtr(String utr) {
		logger.info("filterByUtr() → Request received to filter transaction by UTR: {}", utr);
		Optional<PayoutRecords> records = this.payoutRepository.findByUtr(utr);
		if (records.isPresent()) {
			logger.info("filterByUtr() → Record found for UTR: {}", utr);
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(records.get()).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("filterByUtr() → No record found for UTR: {}", utr);
			ResponseDto response = ResponseDto.builder().status("NO_CONTENT").message("ERROR")
					.data("No records found for the given Utr..!").build();
			return ResponseEntity.badRequest().body(response);
		}
	}

	@Override
	public ResponseEntity<?> filterByTransactionId(String transactionId) {
		logger.info("filterByTransactionId() → Request received for transactionId: {}", transactionId);
		Optional<PayoutRecords> records = this.payoutRepository.findByTransactionId(transactionId);
		if (records.isPresent()) {
			logger.info("filterByTransactionId() → Record found for transactionId: {}", transactionId);
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(records.get()).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("filterByTransactionId() → No record found for transactionId: {}", transactionId);
			ResponseDto response = ResponseDto.builder().status("NO_CONTENT").message("ERROR")
					.data("No records found for the given TransactionId..!").build();
			return ResponseEntity.badRequest().body(response);
		}
	}

	@Override
	public ResponseEntity<?> prefundFilter(Map<String, Object> data) {
		logger.info("prefundFilter() → Request received: {}", data);
		String fromDate = data.get("fromDate").toString();
		String toDate = data.get("toDate").toString();
		logger.info("prefundFilter() → Filtering prefund records from {} to {}", fromDate, toDate);
		List<Map<String, Object>> list = this.prefundRequestRepository.prefundFilterByDate(fromDate, toDate);
		logger.info("prefundFilter() → {} records found", list.size());
		if (list.size() > 0) {
			logger.info("prefundFilter() → Returning filtered prefund records");
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("prefundFilter() → No records found for date range {} to {}", fromDate, toDate);
			ResponseDto response = ResponseDto.builder().status("BAD_REQUEST").message("ERROR")
					.data("No records found for the given date....!").build();
			return ResponseEntity.badRequest().body(response);
		}
	}

	@Override
	public ResponseEntity<?> webhookList() {
		logger.info("webhookList() → Fetching all webhook logs");
		List<Map<String, Object>> list = this.webhookRepository.findAllWebhookList();
		logger.info("webhookList() → {} webhook records found", list.size());
		if (list.size() > 0) {
			logger.info("webhookList() → Returning webhook records");
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("webhookList() → No webhook records found");
			ResponseDto response = ResponseDto.builder().status("NO_CONTENCT") // kept exactly as your original code
					.message("SUCCESS").data("No records found..!").build();
			return ResponseEntity.ok(response);
		}
	}

	@Override
	public ResponseEntity<?> ipAddressList() {
		logger.info("ipAddressList() → Fetching IP address list");
		List<Map<String, Object>> list = this.ipRepository.findAllIpList();
		logger.info("ipAddressList() → {} IP address records found", list.size());
		if (list.size() > 0) {
			logger.info("ipAddressList() → Returning IP address list");
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("ipAddressList() → No IP address records found");
			ResponseDto response = ResponseDto.builder().status("NO_CONTENCT") // kept exactly as in your code
					.message("SUCCESS").data("No records found..!").build();
			return ResponseEntity.ok(response);
		}
	}

	@Override
	public ResponseEntity<?> lienAmountList() {
		logger.info("lienAmountList() → Fetching lien amount list");
		List<Map<String, Object>> list = this.lienRepository.findAllLienAmountList();
		logger.info("lienAmountList() → {} lien amount records found", list.size());
		if (list.size() > 0) {
			logger.info("lienAmountList() → Returning lien amount records");
			ResponseDto response = ResponseDto.builder().status("OK").message("SUCCESS").data(list).build();
			return ResponseEntity.ok(response);
		} else {
			logger.warn("lienAmountList() → No lien amount records found");
			ResponseDto response = ResponseDto.builder().status("NO_CONTENCT") // kept exactly as in your code
					.message("SUCCESS").data("No records found..!").build();
			return ResponseEntity.ok(response);
		}
	}

	@Override
	public ResponseEntity<?> addLienForPrefundList(LienHistory data) {
		logger.info("addLienForPrefundList() → Request received: {}", data);
		Optional<PrefundRequest> prefund = this.prefundRequestRepository.findByReference(data.getReference());
		if (prefund.isPresent()) {
			logger.info("addLienForPrefundList() → Prefund record found for reference: {}", data.getReference());
			data.setUserId(prefund.get().getUserId());
			data.setAmount(String.valueOf(prefund.get().getAmount()));
			String prefundStatus = prefund.get().getStatus();
			String lienStatus = prefund.get().getLienStatus();
			String actionStatus = data.getStatus();
			logger.info("Prefund Status: {}, Lien Status: {}, Action: {}", prefundStatus, lienStatus, actionStatus);
			// Case 1: First-time lien hold
			if (prefundStatus.equals("APPROVED") && (lienStatus == null || !lienStatus.equals("HOLDED"))
					&& actionStatus.equals("Hold as Lien")) {
				logger.info("addLienForPrefundList() → Holding lien amount for userId: {}", prefund.get().getUserId());
				this.deductClientWallet(prefund.get().getAmount(), prefund.get().getUserId());
				this.prefundRequestRepository.updateLienStatus("HOLDED", data.getReference());
			}
			// Case 2: Already HOLDED but trying to hold again
			if (prefundStatus.equals("APPROVED") && lienStatus != null && lienStatus.equals("HOLDED")
					&& actionStatus.equals("Hold as Lien")) {
				logger.warn("addLienForPrefundList() → Reference already HOLDED: {}", data.getReference());
				ResponseDto response = ResponseDto.builder().status("ERROR")
						.data("Given reference already added as lien..!").message("BAD_REQUEST").build();
				return ResponseEntity.badRequest().body(response);
			}
			// Case 3: Releasing lien
			if (prefundStatus.equals("APPROVED") && lienStatus != null && lienStatus.equals("HOLDED")
					&& actionStatus.equals("Remove from Lien")) {
				logger.info("addLienForPrefundList() → Releasing lien for reference: {}", data.getReference());
				this.refundClient(prefund.get().getAmount(), prefund.get().getUserId());
				this.prefundRequestRepository.updateLienStatus("RELEASED", data.getReference());
			}
			// Case 4: Prefund still PENDING but lien hold attempted
			if (prefundStatus.equals("PENDING") && actionStatus.equals("Hold as Lien")) {
				logger.warn("addLienForPrefundList() → Cannot hold lien for PENDING prefund: {}", data.getReference());
				ResponseDto response = ResponseDto.builder().status("ERROR")
						.data("Can't hold amount for lien, still it's not approved..!").message("BAD_REQUEST").build();
				return ResponseEntity.badRequest().body(response);
			}
			// Save lien history
			logger.info("addLienForPrefundList() → Saving lien history entry");
			this.lienHistoryRepository.save(data);
			ResponseDto response = ResponseDto.builder().status("SUCCESS").data("Updated successfully..!").message("OK")
					.build();
			logger.info("addLienForPrefundList() → Lien update successful for reference: {}", data.getReference());
			return ResponseEntity.ok(response);
		} else {
			logger.warn("addLienForPrefundList() → No prefund record found for reference: {}", data.getReference());
			ResponseDto response = ResponseDto.builder().status("ERROR").data("Please provide valid reference..!")
					.message("BAD_REQUEST").build();
			return ResponseEntity.badRequest().body(response);
		}
	}

	private void refundClient(BigDecimal amount, String userId) {
		logger.info("refundClient() → Refunding wallet for userId: {}", userId);
		int update = this.clientRepository.updateWallet(amount.doubleValue(), userId);
		if (update == 0) {
			logger.warn("refundClient() → Wallet refund FAILED for userId: {}, amount: {}", userId, amount);
		}
		BigDecimal updatedBalance = BigDecimal.valueOf(clientRepository.getWalletBalance(userId));
		double oldBal = updatedBalance.doubleValue() - amount.doubleValue();
		logger.info("refundClient() → Old Balance for userId {}: {}", userId, oldBal);
		BigDecimal newBal = updatedBalance;
		logger.info("refundClient() → New Balance for userId {}: {}", userId, newBal);
		logger.info("refundClient() → Wallet refunded successfully for userId: {}", userId);
	}

	private void deductClientWallet(BigDecimal amount, String userId) {
		logger.info("deductClientWallet() → Deduction of Wallet for userId: {}", userId);
		int updated = this.clientRepository.updateBalance1(userId, amount.doubleValue());
		if (updated == 0) {
			logger.warn("deductClientWallet() → Wallet deduction FAILED for userId: {}, amount: {}", userId, amount);
		}
		BigDecimal updatedBalance = BigDecimal.valueOf(clientRepository.getWalletBalance(userId));
		double oldBal = updatedBalance.doubleValue() + amount.doubleValue();
		logger.info("deductClientWallet() → Old Balance for userId {}: {}", userId, oldBal);
		BigDecimal newBal = updatedBalance;
		logger.info("deductClientWallet() → New Balance for userId {}: {}", userId, newBal);
		logger.info("deductClientWallet() → Wallet updated for userId: {}", userId);
		Optional<Client> client = this.clientRepository.findByUserId(userId);
		Double wallet = client.get().getAccountBal().doubleValue();
		logger.info("deductClientWallet() → Current wallet balance from DB for userId {}: {}", userId, wallet);
		Double newBalance = wallet - amount.doubleValue();
		logger.info("deductClientWallet() → Recalculated new balance for userId {}: {}", userId, newBalance);
		this.clientRepository.updateBalance(userId, newBalance);
		logger.info("deductClientWallet() → Wallet deducted successfully for userId: {}", userId);
	}

	@Override
	public ResponseEntity<?> approvePrefundRequest(PrefundApprovalDto approvalDto) {
		logger.info("approvePrefundRequest() → Request received: {}", approvalDto);
		try {
			// 1. Find the prefund request by reference and userId
			logger.info("approvePrefundRequest() → Searching prefund by Reference: {} and UserId: {}",
					approvalDto.getReference(), approvalDto.getUserId());
			Optional<PrefundRequest> prefundOptional = prefundRequestRepository
					.findByReferenceAndUserId(approvalDto.getReference(), approvalDto.getUserId());
			if (prefundOptional.isEmpty()) {
				logger.warn("approvePrefundRequest() → Prefund NOT FOUND for reference: {} and userId: {}",
						approvalDto.getReference(), approvalDto.getUserId());
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Prefund request not found with reference: "
						+ approvalDto.getReference() + " and userId: " + approvalDto.getUserId());
			}
			PrefundRequest prefundRequest = prefundOptional.get();
			logger.info("approvePrefundRequest() → Prefund found. Current Status: {}", prefundRequest.getStatus());
			// 2. Check if request is already processed
			if (!"PENDING".equalsIgnoreCase(prefundRequest.getStatus())) {
				logger.warn("approvePrefundRequest() → Prefund already processed. Status: {}",
						prefundRequest.getStatus());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("Prefund request is already processed with status: " + prefundRequest.getStatus());
			}
			// 3. Update the status
			prefundRequest.setStatus(approvalDto.getStatus().toUpperCase());
			prefundRequest.setApproveBy(approvalDto.getApproveBy());
			logger.info("approvePrefundRequest() → Updating status to {}", approvalDto.getStatus().toUpperCase());
			if ("APPROVED".equalsIgnoreCase(approvalDto.getStatus())) {
				logger.info("approvePrefundRequest() → Processing APPROVAL for reference: {}",
						approvalDto.getReference());
				prefundRequest.setApprovedDate(
						approvalDto.getApprovedDate() != null ? approvalDto.getApprovedDate() : LocalDateTime.now());
				// ******* WALLET UPDATE STARTS HERE *******
				double currentBalance = clientRepository.getWalletBalance(approvalDto.getUserId());
				logger.info("approvePrefundRequest() → Current wallet balance for user {}: {}", approvalDto.getUserId(),
						currentBalance);
				double updatedBalance = currentBalance + prefundRequest.getAmount().doubleValue();
				logger.info("approvePrefundRequest() → Updated wallet balance: {}", updatedBalance);
				clientRepository.updateBalance(approvalDto.getUserId(), updatedBalance);
				logger.info("approvePrefundRequest() → Wallet balance updated in database");
			} else if ("REJECTED".equalsIgnoreCase(approvalDto.getStatus())) {
				logger.info("approvePrefundRequest() → Processing REJECTION for reference: {}",
						approvalDto.getReference());
				prefundRequest.setApprovedDate(LocalDateTime.now());
			}
			// 5. Save the updated request
			PrefundRequest updatedRequest = prefundRequestRepository.save(prefundRequest);
			logger.info("approvePrefundRequest() → Prefund updated successfully. New Status: {}",
					updatedRequest.getStatus());
			// 6. Return success response
			logger.info("approvePrefundRequest() → Returning success response");
			return ResponseEntity.ok()
					.body("Prefund request " + approvalDto.getStatus().toLowerCase() + " successfully. " + "Reference: "
							+ updatedRequest.getReference() + ", Status: " + updatedRequest.getStatus());
		} catch (Exception e) {
			logger.error("approvePrefundRequest() → Exception occurred: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error processing prefund request: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<?> getAllClients() {
		logger.info("getAllClients() → Fetching all client records");
		try {
			// Fetch all clients from the database
			List<Client> clients = clientRepository.findAll();
			logger.info("getAllClients() → {} clients found", clients.size());
//			// Convert Client entities to ClientResponseDto
//			List<ClientResponseDto> clientResponseList = clients.stream().map(this::convertToDto)
//					.collect(Collectors.toList());
//			logger.info("getAllClients() → Returning client response list");
			return ResponseEntity.ok(clients);
		} catch (Exception e) {
			logger.error("getAllClients() → Error fetching all clients: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	// Helper method to convert Client entity to ClientResponseDto
	private ClientResponseDto convertToDto(Client client) {
		logger.info("convertToDto() → Converting Client entity to DTO for userId: {}", client.getUserId());
		ClientResponseDto dto = new ClientResponseDto();
		dto.setUserId(client.getUserId());
		dto.setName(client.getName());
		dto.setEmail(client.getEmail());
		dto.setMobileNum(client.getMobileNum());
		dto.setStatus(client.getStatus());
		dto.setAccountNum(client.getAccountNum());
		dto.setIfscCode(client.getIfscCode());
		dto.setGst(client.getGst());
		dto.setCin(client.getCin());
		dto.setAccountBal(client.getAccountBal());
		dto.setMerchantType(client.getMerchantType());
		dto.setCreatedDate(client.getCreatedDate());
		dto.setUpdatedDate(client.getUpdatedDate());
		
		logger.info("convertToDto() → DTO conversion completed for userId: {}", client.getUserId());
		return dto;
	}

	
	@Override
	public ResponseEntity<?> addPayInCharges(PayInChargesRequestDto dto) {
		logger.info("addPayInCharges() → Request received for userId: {}, range: {} - {}, chargesType: {}, charges: {}",
				dto.getUserId(), dto.getFromRange(), dto.getToRange(), dto.getChargesType(), dto.getChargesAmount());

		try {
			// Validate input
			if (dto.getFromRange() > dto.getToRange()) {
				logger.warn("addPayInCharges() → Validation failed: fromRange {} > toRange {}", 
						dto.getFromRange(), dto.getToRange());
				ResponseDto response = ResponseDto.builder()
						.status("BAD_REQUEST")
						.message("ERROR")
						.data("From range must be less than or equal to To range")
						.build();
				return ResponseEntity.badRequest().body(response);
			}

			// Check for overlapping ranges for the same user
			boolean overlaps = payInChargesRepository.existsOverlap(dto.getUserId(), 
					dto.getFromRange(), dto.getToRange());
			if (overlaps) {
				logger.warn("addPayInCharges() → Overlapping range detected for userId: {}, range: {} - {}",
						dto.getUserId(), dto.getFromRange(), dto.getToRange());
				ResponseDto response = ResponseDto.builder()
						.status("BAD_REQUEST")
						.message("ERROR")
						.data("PayIn Charges already exist for given userId with overlapping amount range")
						.build();
				return ResponseEntity.badRequest().body(response);
			}

			// Create new PayInCharges entity
			PayInCharges payInCharges = new PayInCharges();
			payInCharges.setUserId(dto.getUserId());
			payInCharges.setFromRange(dto.getFromRange());
			payInCharges.setToRange(dto.getToRange());
			payInCharges.setChargesType(dto.getChargesType());
			payInCharges.setChargesAmount(dto.getChargesAmount());

			// Save to database
			PayInCharges savedCharges = payInChargesRepository.save(payInCharges);
			logger.info("addPayInCharges() → PayInCharges saved successfully with ID: {}", savedCharges.getId());

			PayInChargesResponseDto responseDto = convertToResponseDto(savedCharges);
			ResponseDto response = ResponseDto.builder()
					.status("OK")
					.message("SUCCESS")
					.data(responseDto)
					.build();

			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (Exception e) {
			logger.error("addPayInCharges() → Exception occurred: {}", e.getMessage(), e);
			ResponseDto response = ResponseDto.builder()
					.status("INTERNAL_SERVER_ERROR")
					.message("ERROR")
					.data("Failed to add PayIn Charges: " + e.getMessage())
					.build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	public ResponseEntity<?> updatePayInCharges(Long id, PayInChargesRequestDto dto) {
		logger.info("updatePayInCharges() → Request received for ID: {}, userId: {}, range: {} - {}, chargesType: {}, charges: {}",
				id, dto.getUserId(), dto.getFromRange(), dto.getToRange(), dto.getChargesType(), dto.getChargesAmount());

		try {
			// Validate input
			if (dto.getFromRange() > dto.getToRange()) {
				logger.warn("updatePayInCharges() → Validation failed: fromRange {} > toRange {}", 
						dto.getFromRange(), dto.getToRange());
				ResponseDto response = ResponseDto.builder()
						.status("BAD_REQUEST")
						.message("ERROR")
						.data("From range must be less than or equal to To range")
						.build();
				return ResponseEntity.badRequest().body(response);
			}

			// Check if record exists
			Optional<PayInCharges> existingCharges = payInChargesRepository.findById(id);
			if (existingCharges.isEmpty()) {
				logger.warn("updatePayInCharges() → PayInCharges not found with ID: {}", id);
				ResponseDto response = ResponseDto.builder()
						.status("NOT_FOUND")
						.message("ERROR")
						.data("PayIn Charges not found with ID: " + id)
						.build();
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}

			// Check for overlapping ranges (excluding current record)
			boolean overlaps = payInChargesRepository.existsOverlapExcluding(dto.getUserId(), 
					dto.getFromRange(), dto.getToRange(), id);
			if (overlaps) {
				logger.warn("updatePayInCharges() → Overlapping range detected for userId: {}, range: {} - {}",
						dto.getUserId(), dto.getFromRange(), dto.getToRange());
				ResponseDto response = ResponseDto.builder()
						.status("BAD_REQUEST")
						.message("ERROR")
						.data("PayIn Charges already exist for given userId with overlapping amount range")
						.build();
				return ResponseEntity.badRequest().body(response);
			}

			// Update the entity
			PayInCharges payInCharges = existingCharges.get();
			payInCharges.setUserId(dto.getUserId());
			payInCharges.setFromRange(dto.getFromRange());
			payInCharges.setToRange(dto.getToRange());
			payInCharges.setChargesType(dto.getChargesType());
			payInCharges.setChargesAmount(dto.getChargesAmount());

			// Save updated entity
			PayInCharges updatedCharges = payInChargesRepository.save(payInCharges);
			logger.info("updatePayInCharges() → PayInCharges updated successfully for ID: {}", id);

			PayInChargesResponseDto responseDto = convertToResponseDto(updatedCharges);
			ResponseDto response = ResponseDto.builder()
					.status("OK")
					.message("SUCCESS")
					.data(responseDto)
					.build();
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			logger.error("updatePayInCharges() → Exception occurred: {}", e.getMessage(), e);
			ResponseDto response = ResponseDto.builder()
					.status("INTERNAL_SERVER_ERROR")
					.message("ERROR")
					.data("Failed to update PayIn Charges: " + e.getMessage())
					.build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	public ResponseEntity<?> getPayInChargesByUser(String userId) {
		logger.info("getPayInChargesByUser() → Fetching PayInCharges for userId: {}", userId);

		try {
			List<PayInCharges> chargesList = payInChargesRepository.findByUserIdOrderByFromRangeAsc(userId);
			logger.info("getPayInChargesByUser() → {} charges found for userId: {}", chargesList.size(), userId);

			List<PayInChargesResponseDto> responseDtos = chargesList.stream()
					.map(this::convertToResponseDto)
					.collect(Collectors.toList());

			if (responseDtos.isEmpty()) {
				logger.warn("getPayInChargesByUser() → No charges found for userId: {}", userId);
				ResponseDto response = ResponseDto.builder()
						.status("NO_CONTENT")
						.message("SUCCESS")
						.data("No PayIn charges found for given userId")
						.build();
				return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
			}

			ResponseDto response = ResponseDto.builder()
					.status("OK")
					.message("SUCCESS")
					.data(responseDtos)
					.build();
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.error("getPayInChargesByUser() → Exception occurred: {}", e.getMessage(), e);
			ResponseDto response = ResponseDto.builder()
					.status("INTERNAL_SERVER_ERROR")
					.message("ERROR")
					.data("Failed to fetch PayIn charges: " + e.getMessage())
					.build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	public ResponseEntity<?> deletePayInCharges(Long id) {
		logger.info("deletePayInCharges() → Request received to delete PayInCharges with ID: {}", id);

		try {
			// Check if record exists
			Optional<PayInCharges> charges = payInChargesRepository.findById(id);
			if (charges.isEmpty()) {
				logger.warn("deletePayInCharges() → PayInCharges not found with ID: {}", id);
				ResponseDto response = ResponseDto.builder()
						.status("NOT_FOUND")
						.message("ERROR")
						.data("PayIn Charges not found with ID: " + id)
						.build();
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}

			// Delete the record
			payInChargesRepository.deleteById(id);
			logger.info("deletePayInCharges() → PayInCharges deleted successfully with ID: {}", id);

			ResponseDto response = ResponseDto.builder()
					.status("OK")
					.message("SUCCESS")
					.data("PayIn Charges deleted successfully")
					.build();
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.error("deletePayInCharges() → Exception occurred: {}", e.getMessage(), e);
			ResponseDto response = ResponseDto.builder()
					.status("INTERNAL_SERVER_ERROR")
					.message("ERROR")
					.data("Failed to delete PayIn Charges: " + e.getMessage())
					.build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@Override
	public ResponseEntity<?> getAllPayInCharges() {
		logger.info("getAllPayInCharges() → Fetching all PayInCharges");

		try {
			List<Map<String, Object>> chargesList = payInChargesRepository.findAllPayInChargesWithUserDetails();
			logger.info("getAllPayInCharges() → {} PayInCharges records found", chargesList.size());

			if (chargesList.isEmpty()) {
				logger.warn("getAllPayInCharges() → No PayInCharges records found");
				ResponseDto response = ResponseDto.builder()
						.status("NO_CONTENT")
						.message("SUCCESS")
						.data("No PayIn charges data found")
						.build();
				return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
			}

			ResponseDto response = ResponseDto.builder()
					.status("OK")
					.message("SUCCESS")
					.data(chargesList)
					.build();
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.error("getAllPayInCharges() → Exception occurred: {}", e.getMessage(), e);
			ResponseDto response = ResponseDto.builder()
					.status("INTERNAL_SERVER_ERROR")
					.message("ERROR")
					.data("Failed to fetch all PayIn charges: " + e.getMessage())
					.build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	private PayInChargesResponseDto convertToResponseDto(PayInCharges payInCharges) {
		logger.debug("convertToResponseDto() → Converting PayInCharges to DTO for ID: {}", payInCharges.getId());
		return new PayInChargesResponseDto(
				payInCharges.getId(),
				payInCharges.getUserId(),
				payInCharges.getFromRange(),
				payInCharges.getToRange(),
				payInCharges.getChargesType(),
				payInCharges.getChargesAmount(),
				payInCharges.getCreatedDate(),
				payInCharges.getUpdatedDate()
		);
	}

	@Override
	public ResponseEntity<?> approvedPrefundHistory(int page, int size) {

	    logger.info("approvedPrefundHistory() → Fetching APPROVED prefund requests with pagination");

	    Pageable pageable = PageRequest.of(page, size);
	    Page<Map<String, Object>> pageResult =
	            this.prefundRequestRepository.findByStatus("APPROVED", pageable);

	    logger.info("approvedPrefundHistory() → {} approved prefund records found",
	            pageResult.getTotalElements());

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

	    logger.warn("approvedPrefundHistory() → No approved prefund records found");

	    ResponseDto response = ResponseDto.builder()
	            .status("OK")
	            .message("SUCCESS")
	            .data("No approved prefund records found")
	            .build();

	    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
	}


	@Override
	public Page<Map<String, Object>> getPayinMerchants(String search, Pageable pageable) {

	    if (search != null && !search.isBlank()) {
	        search = "%" + search.trim() + "%";
	    } else {
	        search = null;
	    }

	    return clientRepository.findAllPayinMerchants(search, pageable);
	}


    @Override
    public Object getMerchantDetailsById(String merchantId) {

        logger.debug("Fetching merchant details for {}", merchantId);

        Map<String, Object> merchant =
                clientRepository.findMerchantDetailsById(merchantId);

        if (merchant == null || merchant.isEmpty()) {
            throw new RuntimeException("Merchant not found");
        }

        return merchant;
    }

    @Override
    public Page<Map<String, Object>> getPayoutMerchants(String search, Pageable pageable) {

        if (search != null && !search.isBlank()) {
            search = "%" + search.trim() + "%";
        } else {
            search = null;
        }

        return clientRepository.findAllPayoutMerchants(search, pageable);
    }

    // PAYOUT MERCHANT DETAILS
    @Override
    public Object getPayoutMerchantDetailsById(String merchantId) {

        logger.debug("Fetching payout merchant details for {}", merchantId);

        Map<String, Object> merchant =
                clientRepository.findPayoutMerchantDetailsById(merchantId);

        if (merchant == null || merchant.isEmpty()) {
            throw new RuntimeException("Payout merchant not found");
        }

        return merchant;
    }

	@Override
	public List<PayInChargesResponseDto> getChargesByUser(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PayInChargesResponseDto> getAllChargesForPayIn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<?> rejectPrefundRequest(PrefundRejectDto prefundRejectDto) {

	    logger.info("rejectPrefundRequest() → Request received: {}", prefundRejectDto);

	    try {
	        // 1. Fetch prefund request
	        Optional<PrefundRequest> prefundOptional =
	                prefundRequestRepository.findByReferenceAndUserId(
	                        prefundRejectDto.getReference(),
	                        prefundRejectDto.getUserId()
	                );

	        if (prefundOptional.isEmpty()) {
	            logger.warn("rejectPrefundRequest() → Prefund NOT FOUND for reference: {}, userId: {}",
	                    prefundRejectDto.getReference(), prefundRejectDto.getUserId());

	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("Prefund request not found");
	        }

	        PrefundRequest prefundRequest = prefundOptional.get();

	        logger.info("rejectPrefundRequest() → Current status: {}", prefundRequest.getStatus());

	        // 2. Validate status (ONLY PENDING allowed)
	        if (!"PENDING".equalsIgnoreCase(prefundRequest.getStatus())) {
	            logger.warn("rejectPrefundRequest() → Already processed. Status: {}",
	                    prefundRequest.getStatus());

	            return ResponseEntity.badRequest()
	                    .body("Prefund request already processed with status: " + prefundRequest.getStatus());
	        }

	        // 3. Validate remarks (mandatory)
	        if (prefundRejectDto.getRemarks() == null ||
	            prefundRejectDto.getRemarks().trim().isEmpty()) {

	            logger.warn("rejectPrefundRequest() → Rejection remarks missing");

	            return ResponseEntity.badRequest()
	                    .body("Rejection reason is mandatory");
	        }

	        // 4. Update rejection details
	        prefundRequest.setStatus("REJECTED");
	        prefundRequest.setRemarks(prefundRejectDto.getRemarks());
	        prefundRequest.setApproveBy(prefundRejectDto.getApproveBy());
	        prefundRequest.setApprovedDate(LocalDateTime.now());

	        // 5. Save
	        prefundRequestRepository.save(prefundRequest);

	        logger.info("rejectPrefundRequest() → Prefund rejected successfully. Reference: {}",
	                prefundRequest.getReference());

	        return ResponseEntity.ok(
	                "Prefund request rejected successfully. Reference: " + prefundRequest.getReference()
	        );

	    } catch (Exception e) {
	        logger.error("rejectPrefundRequest() → Exception occurred", e);

	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Error rejecting prefund request: " + e.getMessage());
	    }
	}

	@Override
	public ResponseEntity<?> rejectedPrefundHistory(int page, int size) {

	    logger.info("rejectedPrefundHistory() → Fetching REJECTED prefund requests with pagination");

	    Pageable pageable = PageRequest.of(page, size);
	    Page<Map<String, Object>> pageResult =
	            this.prefundRequestRepository.findByStatus("REJECTED", pageable);

	    logger.info("rejectedPrefundHistory() → {} rejected prefund records found",
	            pageResult.getTotalElements());

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

	    logger.warn("rejectedPrefundHistory() → No rejected prefund records found");

	    ResponseDto response = ResponseDto.builder()
	            .status("OK")
	            .message("SUCCESS")
	            .data("No rejected prefund records found")
	            .build();

	    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
	}


	@Override
	public ResponseEntity<?> getPrefundReports(
	        String merchantId,
	        String status,
	        LocalDate fromDate,
	        LocalDate toDate,
	        int page,
	        int size
	) {

	    logger.info("getPrefundReports() → Fetching prefund reports with pagination");

	    Pageable pageable = PageRequest.of(page, size);

	    Page<Map<String, Object>> pageResult =
	            prefundRequestRepository.getPrefundReports(
	                    merchantId,
	                    status,
	                    fromDate,
	                    toDate,
	                    pageable
	            );

	    logger.info("getPrefundReports() → {} records found", pageResult.getTotalElements());

	    if (pageResult.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
	                ResponseDto.builder()
	                        .status("OK")
	                        .message("SUCCESS")
	                        .data("No prefund data found")
	                        .build()
	        );
	    }

	    Map<String, Object> responseData = new HashMap<>();
	    responseData.put("content", pageResult.getContent());
	    responseData.put("currentPage", pageResult.getNumber());
	    responseData.put("pageSize", pageResult.getSize());
	    responseData.put("totalElements", pageResult.getTotalElements());
	    responseData.put("totalPages", pageResult.getTotalPages());

	    return ResponseEntity.ok(
	            ResponseDto.builder()
	                    .status("OK")
	                    .message("SUCCESS")
	                    .data(responseData)
	                    .build()
	    );
	}

	@Override
    public ResponseEntity<?> createVendor(VendorsDTO vendorsDTO) {
        logger.info("createVendor() → Request received: {}", vendorsDTO);
        
        try {
            Optional<Vendors> existingVendor = vendorsRepository.findByVendorName(vendorsDTO.getVendorName());
            if (existingVendor.isPresent()) {
            	logger.warn("createVendor() → Vendor name already exists: {}", vendorsDTO.getVendorName());
                ResponseDto response = ResponseDto.builder()
                        .status("BAD_REQUEST")
                        .message("ERROR")
                        .data("Vendor with name '" + vendorsDTO.getVendorName() + "' already exists")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

           

            Vendors vendor = new Vendors();
            vendor.setVendorName(vendorsDTO.getVendorName());
            vendor.setApi(vendorsDTO.getApi());
            vendor.setCharges(vendorsDTO.getCharges());
            vendor.setAmount(vendorsDTO.getAmount());
            vendor.setStatus("Active");

            Vendors savedVendor = vendorsRepository.save(vendor);
            logger.info("createVendor() → Vendor created successfully with ID: {}", savedVendor.getId());

            VendorsDTO responseDTO = convertToDTO(savedVendor);
            ResponseDto response = ResponseDto.builder()
                    .status("OK")
                    .message("SUCCESS")
                    .data(responseDTO)
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
        	logger.error("createVendor() → Exception occurred: {}", e.getMessage());
            ResponseDto response = ResponseDto.builder()
                    .status("INTERNAL_SERVER_ERROR")
                    .message("ERROR")
                    .data("Failed to create vendor: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    public ResponseEntity<List<VendorsDTO>> getAllVendors() {
    	logger.info("getAllVendors() → Fetching all vendors");
        
        try {
            List<Vendors> vendors = vendorsRepository.findAll();
            logger.info("getAllVendors() → {} vendors found", vendors.size());

            List<VendorsDTO> vendorDTOs = vendors.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(vendorDTOs);
            
        } catch (Exception e) {
        	logger.error("getAllVendors() → Exception occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public ResponseEntity<?> updateVendor(String id, VendorsDTO vendorsDTO) {
    	logger.info("updateVendor() → Request received for ID: {}, Data: {}", id, vendorsDTO);
        
        try {
            Optional<Vendors> vendorOptional = vendorsRepository.findById(id);
            if (vendorOptional.isEmpty()) {
            	logger.warn("updateVendor() → Vendor not found with ID: {}", id);
                ResponseDto response = ResponseDto.builder()
                        .status("NOT_FOUND")
                        .message("ERROR")
                        .data("Vendor not found with ID: " + id)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Optional<Vendors> existingVendor = vendorsRepository.findByVendorNameAndIdNot(vendorsDTO.getVendorName(), id);
            if (existingVendor.isPresent()) {
            	logger.warn("updateVendor() → Vendor name already exists: {}", vendorsDTO.getVendorName());
                ResponseDto response = ResponseDto.builder()
                        .status("BAD_REQUEST")
                        .message("ERROR")
                        .data("Vendor with name '" + vendorsDTO.getVendorName() + "' already exists")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Vendors vendor = vendorOptional.get();
            vendor.setVendorName(vendorsDTO.getVendorName());
            vendor.setApi(vendorsDTO.getApi());
            vendor.setCharges(vendorsDTO.getCharges());
            vendor.setAmount(vendorsDTO.getAmount());

            Vendors updatedVendor = vendorsRepository.save(vendor);
            logger.info("updateVendor() → Vendor updated successfully with ID: {}", updatedVendor.getId());

            VendorsDTO responseDTO = convertToDTO(updatedVendor);
            ResponseDto response = ResponseDto.builder()
                    .status("OK")
                    .message("SUCCESS")
                    .data(responseDTO)
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
        	logger.error("updateVendor() → Exception occurred: {}", e.getMessage());
            ResponseDto response = ResponseDto.builder()
                    .status("INTERNAL_SERVER_ERROR")
                    .message("ERROR")
                    .data("Failed to update vendor: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    public ResponseEntity<?> deleteVendor(String id) {
    	logger.info("deleteVendor() → Request received for ID: {}", id);
        
        try {
            Optional<Vendors> vendorOptional = vendorsRepository.findById(id);
            if (vendorOptional.isEmpty()) {
            	logger.warn("deleteVendor() → Vendor not found with ID: {}", id);
                ResponseDto response = ResponseDto.builder()
                        .status("NOT_FOUND")
                        .message("ERROR")
                        .data("Vendor not found with ID: " + id)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            vendorsRepository.deleteById(id);
            logger.info("deleteVendor() → Vendor deleted successfully with ID: {}", id);

            ResponseDto response = ResponseDto.builder()
                    .status("OK")
                    .message("SUCCESS")
                    .data("Vendor deleted successfully")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
        	logger.error("deleteVendor() → Exception occurred: {}", e.getMessage());
            ResponseDto response = ResponseDto.builder()
                    .status("INTERNAL_SERVER_ERROR")
                    .message("ERROR")
                    .data("Failed to delete vendor: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    public ResponseEntity<?> updateVendorStatus(Map<String, Object> requestBody) {
    	logger.info("updateVendorStatus() → Request received: {}", requestBody);
        
        try {
            if (!requestBody.containsKey("id") || !requestBody.containsKey("status")) {
            	logger.warn("updateVendorStatus() → Missing required fields (id or status)");
                ResponseDto response = ResponseDto.builder()
                        .status("BAD_REQUEST")
                        .message("ERROR")
                        .data("Both 'id' and 'status' are required in request body")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            String id = requestBody.get("id").toString();
            String status = requestBody.get("status").toString();

            List<String> allowedStatuses = Arrays.asList("Active", "Inactive", "Deactive");
            if (!allowedStatuses.contains(status)) {
            	logger.warn("updateVendorStatus() → Invalid status value: {}", status);
                ResponseDto response = ResponseDto.builder()
                        .status("BAD_REQUEST")
                        .message("ERROR")
                        .data("Invalid status. Allowed values: Active, Inactive, Deactive")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Optional<Vendors> vendorOptional = vendorsRepository.findById(id);
            if (vendorOptional.isEmpty()) {
            	logger.warn("updateVendorStatus() → Vendor not found with ID: {}", id);
                ResponseDto response = ResponseDto.builder()
                        .status("NOT_FOUND")
                        .message("ERROR")
                        .data("Vendor not found with ID: " + id)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            int updated = vendorsRepository.updateVendorStatus(id, status);
            
            if (updated > 0) {
            	logger.info("updateVendorStatus() → Vendor status updated to {} for ID: {}", status, id);
                
                Vendors vendor = vendorOptional.get();
                vendor.setStatus(status);
                
                VendorsDTO responseDTO = convertToDTO(vendor);
                ResponseDto response = ResponseDto.builder()
                        .status("OK")
                        .message("SUCCESS")
                        .data(responseDTO)
                        .build();
                
                return ResponseEntity.ok(response);
            } else {
            	logger.warn("updateVendorStatus() → Failed to update status for ID: {}", id);
                ResponseDto response = ResponseDto.builder()
                        .status("BAD_REQUEST")
                        .message("ERROR")
                        .data("Failed to update vendor status")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
        	logger.error("updateVendorStatus() → Exception occurred: {}", e.getMessage());
            ResponseDto response = ResponseDto.builder()
                    .status("INTERNAL_SERVER_ERROR")
                    .message("ERROR")
                    .data("Failed to update vendor status: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    public ResponseEntity<?> validateVendorAmountLimit(String vendorId, double payoutAmount) {
    	logger.info("validateVendorAmountLimit() → Validating vendor: {}, payout amount: {}", vendorId, payoutAmount);
        
        try {
            Optional<Vendors> vendorOptional = vendorsRepository.findById(vendorId);
            if (vendorOptional.isEmpty()) {
            	logger.warn("validateVendorAmountLimit() → Vendor not found with ID: {}", vendorId);
                ResponseDto response = ResponseDto.builder()
                        .status("NOT_FOUND")
                        .message("ERROR")
                        .data("Vendor not found with ID: " + vendorId)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Vendors vendor = vendorOptional.get();
            Double vendorAmountLimit = vendor.getAmount().doubleValue();
            logger.info("validateVendorAmountLimit() → Vendor amount limit: {}", vendorAmountLimit);

            Double totalSuccessAmount = payoutRepository.getTotalSuccessAmountByVendor(vendorId);
            if (totalSuccessAmount == null) {
                totalSuccessAmount = 0.0;
            }
            logger.info("validateVendorAmountLimit() → Total SUCCESS payout amount for vendor {}: {}", vendorId, totalSuccessAmount);

            double remainingLimit = vendorAmountLimit - totalSuccessAmount;
            logger.info("validateVendorAmountLimit() → Remaining limit: {}", remainingLimit);

            if (totalSuccessAmount + payoutAmount > vendorAmountLimit) {
            	logger.warn("validateVendorAmountLimit() → Payout limit exceeded for vendor: {}", vendorId);
                ResponseDto response = ResponseDto.builder()
                        .status("BAD_REQUEST")
                        .message("ERROR")
                        .data("Payout limit exceeded. Vendor has used " + totalSuccessAmount + 
                              " out of " + vendorAmountLimit + ". Cannot process payout of " + payoutAmount)
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            logger.info("validateVendorAmountLimit() → Validation successful for vendor: {}", vendorId);
            ResponseDto response = ResponseDto.builder()
                    .status("OK")
                    .message("SUCCESS")
                    .data("Vendor amount limit validation passed")
                    .build();
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
        	logger.error("validateVendorAmountLimit() → Exception occurred: {}", e.getMessage());
            ResponseDto response = ResponseDto.builder()
                    .status("INTERNAL_SERVER_ERROR")
                    .message("ERROR")
                    .data("Failed to validate vendor amount limit: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private VendorsDTO convertToDTO(Vendors vendor) {
        return VendorsDTO.builder()
                .id(vendor.getId())
                .vendorName(vendor.getVendorName())
                .api(vendor.getApi())
                .charges(vendor.getCharges())
                .amount(vendor.getAmount())
                .status(vendor.getStatus())
                .createdDate(vendor.getCreatedDate())
                .updatedDate(vendor.getUpdatedDate())
                .build();
    }

    @Override
    public ResponseEntity<?> allTransactionCountAndAmountYearMonthWise() {

        logger.info("Fetching year-month wise transaction counts and amounts");

        List<Map<String, Object>> records =
                payoutRepository.transactionCountAndAmountYearMonthWise();

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();

        for (Map<String, Object> row : records) {

            String year  = row.get("year").toString();
            String month = row.get("month").toString();
            String key   = year + "-" + String.format("%02d", Integer.parseInt(month));

            result.putIfAbsent(key, new HashMap<>());

            Map<String, Object> data = result.get(key);

            // defaults
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

        return ResponseEntity.ok(
            ResponseDto.builder()
                .status("OK")
                .message("SUCCESS")
                .data(result)
                .build()
        );
    }

    @Override
    public ResponseEntity<?> allTrasactionCountAndAmountOverall() {

        logger.info("Fetching OVERALL admin transaction summary");

        Map<String, Object> map = new HashMap<>();

        String successCount = "0", successAmount = "0";
        String pendingCount = "0", pendingAmount = "0";
        String failCount    = "0", failAmount    = "0";

        List<Map<String, Object>> records =
                payoutRepository.transactionCountAndAmountOverall();

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

        map.put("successCount", successCount);
        map.put("successAmount", successAmount);
        map.put("pendingCount", pendingCount);
        map.put("pendingAmount", pendingAmount);
        map.put("failCount", failCount);
        map.put("failAmount", failAmount);

        return ResponseEntity.ok(
            ResponseDto.builder()
                .status("OK")
                .message("SUCCESS")
                .data(map)
                .build()
        );
    }

    @Override
    public ResponseEntity<?> payinWebhookList() {

        logger.info("payinWebhookList() → Fetching PAYIN webhooks");

        List<Map<String, Object>> list =
                webhookRepository.findAllPayinWebhookList();

        logger.info("payinWebhookList() → {} records found", list.size());

        if (!list.isEmpty()) {
            return ResponseEntity.ok(
                    ResponseDto.builder()
                            .status("OK")
                            .message("SUCCESS")
                            .data(list)
                            .build()
            );
        }

        return ResponseEntity.ok(
                ResponseDto.builder()
                        .status("NO_CONTENT")
                        .message("SUCCESS")
                        .data("No PAYIN webhook records found")
                        .build()
        );
    }

    @Override
    public ResponseEntity<?> payoutWebhookList() {

        logger.info("payoutWebhookList() → Fetching PAYOUT webhooks");

        List<Map<String, Object>> list =
                webhookRepository.findAllPayoutWebhookList();

        logger.info("payoutWebhookList() → {} records found", list.size());

        if (!list.isEmpty()) {
            return ResponseEntity.ok(
                    ResponseDto.builder()
                            .status("OK")
                            .message("SUCCESS")
                            .data(list)
                            .build()
            );
        }

        return ResponseEntity.ok(
                ResponseDto.builder()
                        .status("NO_CONTENT")
                        .message("SUCCESS")
                        .data("No PAYOUT webhook records found")
                        .build()
        );
    }



	

	






}
