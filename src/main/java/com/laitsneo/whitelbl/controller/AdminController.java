package com.laitsneo.whitelbl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laitsneo.whitelbl.dto.Admin.AdminClientUpdateDto;
import com.laitsneo.whitelbl.dto.Admin.AssignRoleRequestDto;
import com.laitsneo.whitelbl.dto.Admin.CollectionHistoryDto;
import com.laitsneo.whitelbl.dto.Admin.PayInChargesRequestDto;
import com.laitsneo.whitelbl.dto.Admin.PayinDto;
import com.laitsneo.whitelbl.dto.Admin.PayinReportDTO;
import com.laitsneo.whitelbl.dto.Admin.PayinReportRequest;
import com.laitsneo.whitelbl.dto.Admin.PayoutReportDTO;
import com.laitsneo.whitelbl.dto.Admin.PayoutReportRequest;
import com.laitsneo.whitelbl.dto.Admin.PipesReportDTO;
import com.laitsneo.whitelbl.dto.Admin.PrefundApprovalDto;
import com.laitsneo.whitelbl.dto.Admin.PrefundRejectDto;
import com.laitsneo.whitelbl.dto.Admin.PrefundReportRequest;
import com.laitsneo.whitelbl.dto.Admin.ResponseDto;
import com.laitsneo.whitelbl.dto.Admin.SettlementReportRequestDTO;
import com.laitsneo.whitelbl.dto.Admin.TransactionHistoryDTO;
import com.laitsneo.whitelbl.dto.Admin.UpdateChargesDto;
import com.laitsneo.whitelbl.dto.Admin.VendorsDTO;
import com.laitsneo.whitelbl.dto.Client.ClientEditProfileDto;
import com.laitsneo.whitelbl.dto.Client.ClientOnboardDto;
import com.laitsneo.whitelbl.dto.Client.ClientResponseDto;
import com.laitsneo.whitelbl.dto.Client.HoldAmountDto;
import com.laitsneo.whitelbl.dto.Client.LienAmountDTO;
import com.laitsneo.whitelbl.dto.Client.LienResponseDTO;
import com.laitsneo.whitelbl.dto.Client.PayoutFilterByCLientId;
import com.laitsneo.whitelbl.dto.Client.PrefundDto;
import com.laitsneo.whitelbl.dto.Client.WalletDetailResponseDTO;
import com.laitsneo.whitelbl.entity.Admin.AdminRole;
import com.laitsneo.whitelbl.entity.Admin.Charges;
import com.laitsneo.whitelbl.entity.Admin.Role;
import com.laitsneo.whitelbl.entity.Admin.SettlementRuleRequest;
import com.laitsneo.whitelbl.entity.Client.Client;
import com.laitsneo.whitelbl.entity.Client.IpAddress;
import com.laitsneo.whitelbl.entity.Client.LienAmount;
import com.laitsneo.whitelbl.entity.Client.LienHistory;
import com.laitsneo.whitelbl.entity.Client.PayinRecords;
import com.laitsneo.whitelbl.response.AdminRoleResponseDto;
import com.laitsneo.whitelbl.response.AdminSettlementHistoryResponseDTO;
import com.laitsneo.whitelbl.response.AdminSettlementReportResponseDTO;
import com.laitsneo.whitelbl.response.PayInChargesResponseDto;
import com.laitsneo.whitelbl.response.PayinReportResponse;
import com.laitsneo.whitelbl.service.AdminRoleService;
import com.laitsneo.whitelbl.service.ClientService;
import com.laitsneo.whitelbl.service.LockedFundsService;
import com.laitsneo.whitelbl.service.ReportService;
import com.laitsneo.whitelbl.service.RoleService;
import com.laitsneo.whitelbl.service.TrexoService;
import com.laitsneo.whitelbl.service.WalletService;
import com.laitsneo.whitelbl.serviceImpl.Admin.AdminServiceImpl;
import com.laitsneo.whitelbl.serviceImpl.Admin.AutoSettlementService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/payment/admin")
public class AdminController {
	Logger logger = LoggerFactory.getLogger(AdminController.class);

	@Autowired
	private AdminServiceImpl adminService;
	@Autowired
	private ClientService clientService;
	@Autowired
	private RoleService roleService;
	@Autowired
	private AdminRoleService adminRoleService;
	@Autowired
	private TrexoService trexoService;
	
	@Autowired
	private ReportService reportService;

	@Autowired
    private  WalletService walletService;
	
	
	@Autowired
	private AutoSettlementService autoSettlementService;
	
	@Autowired
	private LockedFundsService lockedFundsService;
	/////////////// API for client onboard ////////////////
	@PostMapping(value = "/create-client", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> createClient(
	        @RequestPart("data") String data,
	        @RequestPart(required = false) MultipartFile aadhaarFront,
	        @RequestPart(required = false) MultipartFile aadhaarBack,
	        @RequestPart(required = false) MultipartFile panDoc,
	        @RequestPart(required = false) MultipartFile gstDoc,
	        @RequestPart(required = false) MultipartFile shopPhoto,
	        @RequestPart(required = false) MultipartFile profilePhoto
	) throws Exception {

	    ObjectMapper mapper = new ObjectMapper();
	    ClientOnboardDto dto = mapper.readValue(data, ClientOnboardDto.class);

	    return clientService.createClient(
	            dto,
	            aadhaarFront,
	            aadhaarBack,
	            panDoc,
	            gstDoc,
	            shopPhoto,
	            profilePhoto
	    );
	}

	@PostMapping("create-role")
	public ResponseEntity<ResponseDto> createRole(@Valid @RequestBody Role request) {
		logger.info("POST /create-role → Request: {}", request);
		String response = this.roleService.createRole(request);
		logger.info("POST /create-role → Response: {}", response);
		ResponseDto responseDto = ResponseDto.builder().response(response).status("CREATED").statusCode(201).build();
		return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
	}

	@PostMapping("/assign-role")
	public ResponseEntity<ResponseDto> assignRole(@Valid @RequestBody AssignRoleRequestDto request) {
		logger.info("POST /assign-role → Request: {}", request);
		String response = this.adminRoleService.assignRole(request);
		logger.info("POST /assign-role → Response: {}", response);
		ResponseDto responseDto = ResponseDto.builder().response(response).status("CREATED").statusCode(201).build();
		return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
	}
	
	  // EDIT ROLE
    @PutMapping("/update-role/{adminRoleId}")
    public ResponseEntity<ResponseDto> updateRole(
            @PathVariable String adminRoleId,
            @RequestBody AssignRoleRequestDto request) {

        String response = adminRoleService.updateRole(adminRoleId, request);
        return ResponseEntity.ok(
                ResponseDto.builder()
                        .response(response)
                        .status("SUCCESS")
                        .statusCode(200)
                        .build());
    }
    
    @GetMapping("/get-allRoles")
    public ResponseEntity<List<AdminRoleResponseDto>> getAllRoles() {
        return ResponseEntity.ok(adminRoleService.getAllRoles());
    }
    
    // SOFT DELETE
    @DeleteMapping("/delete-role/{adminRoleId}")
    public ResponseEntity<ResponseDto> deleteRole(
            @PathVariable String adminRoleId) {

        String response = adminRoleService.softDeleteRole(adminRoleId);
        return ResponseEntity.ok(
                ResponseDto.builder()
                        .response(response)
                        .status("SUCCESS")
                        .statusCode(200)
                        .build());
    }

	@PostMapping("/setCharges")
	public ResponseEntity<?> setCharges(@Valid @RequestBody Charges data, HttpServletRequest req) {
		logger.info("POST /setCharges → Request: {}", data);
		return this.adminService.setCharges(data, req);
	}

	@GetMapping("/getCharges")
	public ResponseEntity<?> getAllCharges() {
		logger.info("GET /getCharges");
		return this.adminService.getAllCharges();
	}

	@DeleteMapping("/deleteCharges/{slNo}")
	public ResponseEntity<?> deleteCharges(@PathVariable int slNo) {
		logger.info("DELETE /deleteCharges/{}", slNo);
		return this.adminService.deleteChargesBySlNo(slNo);
	}

	@PutMapping("/updateCharges")
	public ResponseEntity<?> updateCharges(@Valid @RequestBody UpdateChargesDto data, HttpServletRequest req) {
		logger.info("PUT /updateCharges → Request: {}", data);
		return this.adminService.updateCharges(data, req);
	}

	@PostMapping("/prefund-request")
	public ResponseEntity<?> approvePrefundRequest(@Valid @RequestBody PrefundApprovalDto approvalDto) {
		logger.info("POST /prefund-request → Request: {}", approvalDto);
		return adminService.approvePrefundRequest(approvalDto);
	}

	@GetMapping("/clients")
	public ResponseEntity<List<ClientResponseDto>> getAllClients() {
		logger.info("GET /clients");
		return adminService.getAllClients();
	}

	@GetMapping("/prefund-history")
	public ResponseEntity<?> prefundHistory(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size
	) {
	    logger.info("GET /prefund-history | page={} | size={}", page, size);
	    return adminService.prefundHistory(page, size);
	}


	@GetMapping("/profile/{userId}")
	public ResponseEntity<?> profileByUserId(@PathVariable String userId) {
		logger.info("GET /profile/{}", userId);
		return adminService.profileByUserId(userId);
	}

	@GetMapping("/clientList-wallet")
	public ResponseEntity<?> clientListAndWallets() {
		logger.info("GET /clientList-wallet");
		return adminService.clientListAndWallets();
	}

	@PutMapping("/update-status")
	public ResponseEntity<?> updateStatusByUserId(@RequestBody Map<String, Object> userId) {
		logger.info("PUT /update-status → Request: {}", userId);
		return adminService.updateStatusByUserId(userId);
	}

	@DeleteMapping("/delete-Client/{clientId}")
	public ResponseEntity<?> deleteClient(@PathVariable String clientId) {
		logger.info("DELETE /delete-Client/{}", clientId);
		return adminService.deleteClient(clientId);
	}

	@GetMapping("/transaction-history")
	public ResponseEntity<?> allTransactions() {
		logger.info("GET /transaction-history");
		return adminService.allTransactions();
	}

	@GetMapping("/transaction-counts")
	public ResponseEntity<?> transactionCountAndAmount() {
		logger.info("GET /transaction-counts");
		return adminService.allTrasactionCountAndAmount();
	}

	@GetMapping("/filterByUtr/{utr}")
	public ResponseEntity<?> filterByUtr(@PathVariable String utr) {
		logger.info("GET /filterByUtr/{}", utr);
		return adminService.filterByUtr(utr);
	}

	@GetMapping("/filterByTransactionId/{transactionId}")
	public ResponseEntity<?> filterByTransactionId(@PathVariable String transactionId) {
		logger.info("GET /filterByTransactionId/{}", transactionId);
		return adminService.filterByTransactionId(transactionId);
	}

	@PostMapping("/prefund-filter")
	public ResponseEntity<?> prefundFilter(@RequestBody Map<String, Object> data) {
		logger.info("POST /prefund-filter → Request: {}", data);
		return adminService.prefundFilter(data);
	}

	@PostMapping("/add-ipAddress")
	public ResponseEntity<?> addIpAddress(@Valid @RequestBody IpAddress data) {
		logger.info("POST /add-ipAddress → Request: {}", data);
		return clientService.addIpAddress(data);
	}

	@PutMapping("/update-ipAddress")
	public ResponseEntity<?> updateIpAddress(@Valid @RequestBody IpAddress data) {
		logger.info("PUT /update-ipAddress → Request: {}", data);
		return clientService.updateIpAddress(data);
	}

	@GetMapping("/ipAddress/{clientId}")
	public ResponseEntity<?> ipAddressByClientId(@PathVariable String clientId) {
		logger.info("GET /ipAddress/{}", clientId);
		return clientService.ipAddressByClientId(clientId);
	}

	 @GetMapping("/webhook-list/payin")
	    public ResponseEntity<?> payinWebhookList() {
	        logger.info("GET /admin/webhook-list/payin");
	        return adminService.payinWebhookList();
	    }

	    @GetMapping("/webhook-list/payout")
	    public ResponseEntity<?> payoutWebhookList() {
	        logger.info("GET /admin/webhook-list/payout");
	        return adminService.payoutWebhookList();
	    }

	@GetMapping("/ipAddress-list")
	public ResponseEntity<?> ipAddressList() {
		logger.info("GET /ipAddress-list");
		return adminService.ipAddressList();
	}

	@PutMapping("/update-merchant")
	public ResponseEntity<?> updateMerchant(@RequestBody ClientEditProfileDto data) {
		logger.info("PUT /update-merchant → Request: {}", data);
		return clientService.updateMerchant(data);
	}

//	@PostMapping("/add-lienAmount")
//	public ResponseEntity<?> addlienAmount(@Valid @RequestBody LienAmount data) {
//		logger.info("POST /add-lienAmount → Request: {}", data);
//		return clientService.addLienAmount(data);
//	}

	@PostMapping("/add-lienAmount")
    public ResponseEntity<LienResponseDTO> addLienAmount(@RequestBody LienAmountDTO lienAmountDTO) {
        log.info("POST /api/lien/add-lienAmount | userId={}", lienAmountDTO.getUserId());
        
        try {
            LienResponseDTO response = clientService.addLienAmount(lienAmountDTO);
            
            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Error adding lien amount: {}", e.getMessage());
            LienResponseDTO errorResponse = new LienResponseDTO(false, "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

	@PutMapping("/update-lienAmount")
	public ResponseEntity<?> updateLienAmount(@Valid @RequestBody LienAmount data) {
		logger.info("PUT /update-lienAmount → Request: {}", data);
		return clientService.updateLienAmount(data);
	}

//	@DeleteMapping("/delete-lienAmount/{userId}")
//	public ResponseEntity<?> deleteLienAmount(@PathVariable String userId) {
//		logger.info("DELETE /delete-lienAmount/{}", userId);
//		return clientService.deleteLienAmount(userId);
//	}
	
	 @DeleteMapping("/delete-lienAmount/{userId}")
	    public ResponseEntity<LienResponseDTO> deleteLienAmount(@PathVariable String userId) {
	        log.info("DELETE /api/lien/delete-lienAmount/{}", userId);
	        
	        try {
	            LienResponseDTO response = clientService.deleteLienAmount(userId);
	            
	            if (response.isSuccess()) {
	                return ResponseEntity.ok(response);
	            } else {
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	            }
	        } catch (Exception e) {
	            log.error("Error deleting lien amount: {}", e.getMessage());
	            LienResponseDTO errorResponse = new LienResponseDTO(false, "Internal server error: " + e.getMessage());
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	        }
	    }

	@GetMapping("/lienAmount-list")
	public ResponseEntity<?> lienAmountList() {
		logger.info("GET /lienAmount-list");
		return adminService.lienAmountList();
	}

	@GetMapping("/statusCheck/{orderId}")
	public ResponseEntity<?> filterByOrderId(@PathVariable String orderId) {
		logger.info("GET /statusCheck/{}", orderId);
		return clientService.filterByOrderId(orderId);
	}

	@PutMapping("/addLien-prefundList")
	public ResponseEntity<?> addLienForPrefundList(@RequestBody LienHistory data) {
		logger.info("PUT /addLien-prefundList → Request: {}", data);
		return adminService.addLienForPrefundList(data);
	}

	@GetMapping("/check/{orderId}")
	public ResponseEntity<?> checkStatus(@PathVariable String orderId) {
		logger.info("GET /check/{}", orderId);
		Map<String, Object> response = trexoService.checkTransaction(orderId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/initiatePrefund")
	public ResponseEntity<?> createPrefundRequest(@RequestBody PrefundDto request) {
		logger.info("POST /PrefundRequest from admin → Request: {}", request);
		ResponseEntity<?> response = clientService.createPrefundRequest(request);
		return response;
	}

	@PostMapping("/payoutFilter")
	public ResponseEntity<?> payoutFilter(@RequestBody PayoutFilterByCLientId data) {
		logger.info("POST /payoutFilter from admin → Request: {}", data);
		return this.clientService.payoutFilter(data);
	}

	@GetMapping("/dailyCountAndAmount")
	public ResponseEntity<?> dailyCountAndAmount() {
		logger.info("GET /dailyCountAndAmount → Request: {}");
		ResponseEntity<?> response = clientService.dailyCountAndAmount();
		return response;
	}
	// VENDOR

	@PostMapping("/createVendor")
    public ResponseEntity<?> createVendor(@Valid @RequestBody VendorsDTO vendorsDTO) {
        logger.info("POST /vendors → Request: {}", vendorsDTO);
        ResponseEntity<?> response = adminService.createVendor(vendorsDTO);
        logger.info("POST /vendors → Response: {}", response.getStatusCode());
        return response;
    }

    
    @GetMapping("/getAllVendor")
    public ResponseEntity<List<VendorsDTO>> getAllVendors() {
        logger.info("GET /vendors");
        ResponseEntity<List<VendorsDTO>> response = adminService.getAllVendors();
        logger.info("GET /vendors → Response: {} vendors found", 
                    response.getBody() != null ? response.getBody().size() : 0);
        return response;
    }

   
    @PutMapping("/updateVendors/{id}")
    public ResponseEntity<?> updateVendor(@PathVariable String id, 
                                          @Valid @RequestBody VendorsDTO vendorsDTO) {
        logger.info("PUT /vendors/{} → Request: {}", id, vendorsDTO);
        ResponseEntity<?> response = adminService.updateVendor(id, vendorsDTO);
        logger.info("PUT /vendors/{} → Response: {}", id, response.getStatusCode());
        return response;
    }

    
    @DeleteMapping("DeleteVendors/{id}")
    public ResponseEntity<?> deleteVendor(@PathVariable String id) {
        logger.info("DELETE /vendors/{}", id);
        ResponseEntity<?> response = adminService.deleteVendor(id);
        logger.info("DELETE /vendors/{} → Response: {}", id, response.getStatusCode());
        return response;
    }

    
    @PatchMapping("/Status")
    public ResponseEntity<?> updateVendorStatus(@RequestBody Map<String, Object> requestBody) {
        logger.info("PATCH /vendors/status → Request: {}", requestBody);
        ResponseEntity<?> response = adminService.updateVendorStatus(requestBody);
        logger.info("PATCH /vendors/status → Response: {}", response.getStatusCode());
        return response;
    }

  
    @PostMapping("/vendorValidate-limit")
    public ResponseEntity<?> validateVendorAmountLimit(@RequestBody Map<String, Object> requestBody) {
        logger.info("POST /vendors/validate-limit → Request: {}", requestBody);
        
        String vendorId = requestBody.get("vendorId").toString();
        double payoutAmount = Double.parseDouble(requestBody.get("payoutAmount").toString());
        
        ResponseEntity<?> response = adminService.validateVendorAmountLimit(vendorId, payoutAmount);
        logger.info("POST /vendors/validate-limit → Response: {}", response.getStatusCode());
        return response;
    }
	//  PAYIN CHARGES 

		@PostMapping("/payinCharges")
		public ResponseEntity<?> addPayInCharges(@Valid @RequestBody PayInChargesRequestDto dto) {
			logger.info("POST /payinCharges → Request: {}", dto);
			ResponseEntity<?> response = adminService.addPayInCharges(dto);
			logger.info("POST /payinCharges → Response Status: {}", response.getStatusCode());
			return response;
		}

		@PutMapping("/payinCharges/{id}")
		public ResponseEntity<?> updatePayInCharges(@PathVariable Long id,
				@Valid @RequestBody PayInChargesRequestDto dto) {
			logger.info("PUT /payinCharges/{} → Request: {}", id, dto);
			ResponseEntity<?> response = adminService.updatePayInCharges(id, dto);
			logger.info("PUT /payinCharges/{} → Response Status: {}", id, response.getStatusCode());
			return response;
		}

		@GetMapping("/payinCharges/user/{userId}")
		public ResponseEntity<?> getPayInChargesByUser(@PathVariable String userId) {
			logger.info("GET /payinCharges/user/{} → Request", userId);
			ResponseEntity<?> response = adminService.getPayInChargesByUser(userId);
			logger.info("GET /payinCharges/user/{} → Response Status: {}", userId, response.getStatusCode());
			return response;
		}

		@DeleteMapping("/payinCharges/{id}")
		public ResponseEntity<?> deletePayInCharges(@PathVariable Long id) {
			logger.info("DELETE /payinCharges/{} → Request", id);
			ResponseEntity<?> response = adminService.deletePayInCharges(id);
			logger.info("DELETE /payinCharges/{} → Response Status: {}", id, response.getStatusCode());
			return response;
		}

		@GetMapping("/payinCharges")
		public ResponseEntity<?> getAllPayInCharges() {
			logger.info("GET /payinCharges → Request");
			ResponseEntity<?> response = adminService.getAllPayInCharges();
			logger.info("GET /payinCharges → Response Status: {}", response.getStatusCode());
			return response;
		}
		
		
	 @GetMapping("/history")
	    public ResponseEntity<?> history(
	            @RequestParam(required = false) String fromDate,
	            @RequestParam(required = false) String toDate,
	            @RequestParam(required = false) String utr,
	            @RequestParam(required = false) String txnId,
	            @RequestHeader("Client-Id") String userId
	    ) {
	        List<CollectionHistoryDto> list =
	        		clientService.getHistory(userId, fromDate, toDate, utr, txnId);

	        return ResponseEntity.ok(list);
	    }

	    @GetMapping("/{txnId}")
	    public ResponseEntity<?> details(
	            @PathVariable String txnId,
	            @RequestHeader("Client-Id") String userId
	    ) {
	        return ResponseEntity.ok(clientService.getDetails(userId, txnId));
	    }
	    
	    @PostMapping("/payin-reports")
	    public ResponseEntity<?> getAllPayinRecordsReport(
	            @RequestParam(value = "fromDate", required = false) 
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

	            @RequestParam(value = "toDate", required = false) 
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

	        System.out.println("fromDate: " + fromDate);
	        System.out.println("toDate: " + toDate);

	        logger.info("ADMIN: Get all payin records report request from {} to {}", fromDate, toDate);

	        try {
	            return clientService.getAllPayinRecordsReport(fromDate, toDate);
	        } catch (Exception e) {
	            logger.error("Error in getAllPayinRecordsReport controller: ", e);

	            Map<String, Object> response = new HashMap<>();
	            response.put("success", false);
	            response.put("message", "Internal server error");
	            response.put("errorCode", "INTERNAL_ERROR");

	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	        }
	    }

	   
	    @PostMapping("/hold-amount")
	    public ResponseEntity<?> holdPayinAmount(@Valid @RequestBody HoldAmountDto holdAmountDto) {
	        logger.info("Hold amount request received for orderId: {}, userId: {}, holdAmount: {}", 
	                   holdAmountDto.getOrderId(), holdAmountDto.getUserId(), holdAmountDto.getHoldAmount());
	        try {
	            return clientService.holdPayinAmount(holdAmountDto);
	        } catch (Exception e) {
	            logger.error("Error in holdPayinAmount controller: ", e);
	            Map<String, Object> response = new HashMap<>();
	            response.put("success", false);
	            response.put("message", "Internal server error");
	            response.put("errorCode", "INTERNAL_ERROR");
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	        }
	    }
	    
//	    Report Section======================
	    @GetMapping("/pipesReport")
	    public ResponseEntity<?> getPipesReport(
	            @RequestParam(required = false) String pipeName,
	            @RequestParam(required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
	            @RequestParam(required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
	            @RequestParam(defaultValue = "0") int page,
	            @RequestParam(defaultValue = "10") int size) {

	        log.info("Pipes Report | pipeName={}, fromDate={}, toDate={}, page={}, size={}",
	                pipeName, fromDate, toDate, page, size);

	        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
	            throw new IllegalArgumentException("fromDate cannot be after toDate");
	        }

	        Page<PipesReportDTO> report =
	                reportService.getPipesReport(pipeName, fromDate, toDate, page, size);

	        Map<String, Object> response = new HashMap<>();
	        response.put("success", true);
	        response.put("data", report.getContent());
	        response.put("currentPage", report.getNumber());
	        response.put("totalItems", report.getTotalElements());
	        response.put("totalPages", report.getTotalPages());

	        return ResponseEntity.ok(response);
	    }
	    
	    
	    @GetMapping("/payinReport")
	    public ResponseEntity<?> getPayinReport(
	            @RequestParam(required = false) String merchantId,
	            @RequestParam(required = false) String status,
	            @RequestParam(required = false) String txnId,
	            @RequestParam(required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
	            @RequestParam(required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
	            @RequestParam int page,
	            @RequestParam int size
	    ) {

	        try {
	            log.info(
	                "Fetching payin report | merchantId={}, status={}, txnId={}, fromDate={}, toDate={}, page={}, size={}",
	                merchantId, status, txnId, fromDate, toDate, page, size
	            );

	            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
	                Map<String, String> error = new HashMap<>();
	                error.put("error", "fromDate cannot be after toDate");
	                return ResponseEntity.badRequest().body(error);
	            }

	            Map<String, Object> reports =
	                    reportService.getPayinReport(
	                            merchantId, status, txnId, fromDate, toDate, page, size
	                    );

	            Map<String, Object> response = new HashMap<>();
	            response.put("success", true);
	            response.put("data", reports);

	            return ResponseEntity.ok(response);

	        } catch (Exception e) {
	            Map<String, String> error = new HashMap<>();
	            error.put("error", "Failed to fetch payin report");
	            error.put("message", e.getMessage());
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	        }
	    }



	    
	    @GetMapping("/payoutReport")
	    public ResponseEntity<?> getPayoutReport(
	            @RequestParam(required = false) String merchantId,
	            @RequestParam(required = false) String status,
	            @RequestParam(required = false) String txnId,
	            @RequestParam(required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
	            @RequestParam(required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
	            @RequestParam int page,
	            @RequestParam int size
	    ) {

	        try {
	            log.info(
	                "Fetching payout report | merchantId={}, status={}, txnId={}, fromDate={}, toDate={}, page={}, size={}",
	                merchantId, status, txnId, fromDate, toDate, page, size
	            );

	            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
	                Map<String, String> error = new HashMap<>();
	                error.put("error", "fromDate cannot be after toDate");
	                return ResponseEntity.badRequest().body(error);
	            }

	            Map<String, Object> reports =
	                    reportService.getPayoutReport(
	                            merchantId, status, txnId, fromDate, toDate, page, size
	                    );

	            Map<String, Object> response = new HashMap<>();
	            response.put("success", true);
	            response.put("data", reports);

	            return ResponseEntity.ok(response);

	        } catch (Exception e) {
	            Map<String, String> error = new HashMap<>();
	            error.put("error", "Failed to fetch payout report");
	            error.put("message", e.getMessage());
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	        }
	    }

	   
	    
	    @GetMapping("/getLienReports")
	    public ResponseEntity<?> getLienReports(
	            @RequestParam(required = false) String userId,
	            @RequestParam(required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
	            @RequestParam(required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
	    ) {
	        log.info("Lien report API triggered.");
	        return ResponseEntity.ok().body(
	        		reportService.getLienReports(userId, fromDate, toDate)
	        );
	    }
	    
	    @GetMapping("/getHoldReports")
	    public ResponseEntity<?> getLockedFundsReport(
	            @RequestParam(required = false) String merchantName,
	            @RequestParam(required = false) String status,
	            @RequestParam(required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
	            @RequestParam(required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
	            @RequestParam int page,
	            @RequestParam int size
	    ) {

	        try {
	            log.info(
	                "Fetching locked funds report | merchantName={}, status={}, fromDate={}, toDate={}, page={}, size={}",
	                merchantName, status, fromDate, toDate, page, size
	            );

	            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
	                Map<String, String> error = new HashMap<>();
	                error.put("error", "fromDate cannot be after toDate");
	                return ResponseEntity.badRequest().body(error);
	            }

	            Map<String, Object> reports =
	            		lockedFundsService.getLockedFundsReport(
	                            merchantName, status, fromDate, toDate, page, size
	                    );

	            Map<String, Object> response = new HashMap<>();
	            response.put("success", true);
	            response.put("data", reports);

	            return ResponseEntity.ok(response);

	        } catch (Exception e) {
	            log.error("Error fetching locked funds report", e);
	            Map<String, String> error = new HashMap<>();
	            error.put("error", "Failed to fetch locked funds report");
	            error.put("message", e.getMessage());
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	        }
	    }

	    
	 
	    // GET ALL PAYIN MERCHANTS
	    @GetMapping("/getAllPayInMerchants")
	    public ResponseEntity<?> getAllPayinMerchants(
	            @RequestParam(required = false) String search,
	            @RequestParam(defaultValue = "0") int page,
	            @RequestParam(defaultValue = "10") int size
	    ) {
	        log.info("ADMIN | Get Payin Merchants | search={}, page={}, size={}", search, page, size);

	        try {
	            Pageable pageable = PageRequest.of(
	                    page,
	                    size,
	                    Sort.by(Sort.Direction.DESC, "created_date")
	            );

	            Page<Map<String, Object>> pageResult =
	                    adminService.getPayinMerchants(search, pageable);

	            // ✅ Build clean & stable response
	            Map<String, Object> response = new HashMap<>();
	            response.put("content", pageResult.getContent());
	            response.put("page", pageResult.getNumber());
	            response.put("size", pageResult.getSize());
	            response.put("totalElements", pageResult.getTotalElements());
	            response.put("totalPages", pageResult.getTotalPages());
	            response.put("last", pageResult.isLast());

	            return ResponseEntity.ok(response);

	        } catch (Exception e) {
	            log.error("Error while fetching payin merchants list", e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
	                    Map.of(
	                            "success", false,
	                            "message", "Failed to fetch payin merchants",
	                            "error", e.getMessage()
	                    )
	            );
	        }
	    }

	    
	    

	    @GetMapping("/getMerchants/{merchantId}")
	    public ResponseEntity<?> getPayinMerchantById(@PathVariable String merchantId) {
	        log.info("ADMIN | Get Payin Merchant Details | merchantId={}", merchantId);
	        try {
	            return ResponseEntity.ok(adminService.getMerchantDetailsById(merchantId));
	        } catch (RuntimeException e) {
	            log.warn("Merchant not found | merchantId={}", merchantId);
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
	                    Map.of(
	                            "success", false,
	                            "message", e.getMessage()
	                    )
	            );
	        } catch (Exception e) {
	            log.error("Error while fetching merchant details", e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
	                    Map.of(
	                            "success", false,
	                            "message", "Failed to fetch merchant details"
	                    )
	            );
	        }
	    }
	    
	    @GetMapping("/getAllPayOutMerchants")
	    public ResponseEntity<?> getAllPayoutMerchants(
	            @RequestParam(required = false) String search,
	            @RequestParam(defaultValue = "0") int page,
	            @RequestParam(defaultValue = "10") int size
	    ) {
	        log.info("ADMIN | Get Payout Merchants | search={}, page={}, size={}", search, page, size);

	        try {
	            Pageable pageable = PageRequest.of(
	                    page,
	                    size,
	                    Sort.by(Sort.Direction.DESC, "created_date")
	            );

	            Page<Map<String, Object>> pageResult =
	                    adminService.getPayoutMerchants(search, pageable);
	            Map<String, Object> response = new HashMap<>();
	            response.put("content", pageResult.getContent());
	            response.put("page", pageResult.getNumber());
	            response.put("size", pageResult.getSize());
	            response.put("totalElements", pageResult.getTotalElements());
	            response.put("totalPages", pageResult.getTotalPages());
	            response.put("last", pageResult.isLast());

	            return ResponseEntity.ok(response);

	        } catch (Exception e) {
	            log.error("Error while fetching payout merchants list", e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
	                    Map.of(
	                            "success", false,
	                            "message", "Failed to fetch payout merchants",
	                            "error", e.getMessage()
	                    )
	            );
	        }
	    }

	    // GET PAYOUT MERCHANT DETAILS
	    @GetMapping("/getAllPayOutMerchants/{merchantId}")
	    public ResponseEntity<?> getPayoutMerchantById(@PathVariable String merchantId) {
	        log.info("ADMIN | Get Payout Merchant Details | merchantId={}", merchantId);
	        try {
	            return ResponseEntity.ok(
	            		adminService.getPayoutMerchantDetailsById(merchantId)
	            );
	        } catch (RuntimeException e) {
	            log.warn("Payout merchant not found | merchantId={}", merchantId);
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
	                    Map.of(
	                            "success", false,
	                            "message", e.getMessage()
	                    )
	            );
	        } catch (Exception e) {
	            log.error("Error while fetching payout merchant details", e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
	                    Map.of(
	                            "success", false,
	                            "message", "Failed to fetch payout merchant details"
	                    )
	            );
	        }
	    }

	    @GetMapping("/approvedprefundList")
	    public ResponseEntity<?> approvedPrefundHistory(
	            @RequestParam(defaultValue = "0") int page,
	            @RequestParam(defaultValue = "10") int size
	    ) {
	        logger.info("GET /prefund-history/approved | page={} | size={}", page, size);
	        return adminService.approvedPrefundHistory(page, size);
	    }

	    
	    @PostMapping("/prefund-reject")
	    public ResponseEntity<?> rejectPrefundRequest(
	            @Valid @RequestBody PrefundRejectDto prefundRejectDto) {

	        logger.info("POST /prefund-request/reject → Request: {}", prefundRejectDto);
	        return adminService.rejectPrefundRequest(prefundRejectDto);
	    }

	    @GetMapping("/rejectedprefundList")
	    public ResponseEntity<?> rejectedPrefundHistory(
	            @RequestParam(defaultValue = "0") int page,
	            @RequestParam(defaultValue = "10") int size
	    ) {
	        logger.info("GET /prefund-history/rejected | page={} | size={}", page, size);
	        return adminService.rejectedPrefundHistory(page, size);
	    }

	    
	    @PostMapping("/prefund-reports")
	    public ResponseEntity<?> getPrefundReports(
	            @RequestBody PrefundReportRequest request
	    ) {
	        logger.info(
	            "POST /prefund-reports → merchantId={}, status={}, fromDate={}, toDate={}, page={}, size={}",
	            request.getMerchantId(),
	            request.getStatus(),
	            request.getFromDate(),
	            request.getToDate(),
	            request.getPage(),
	            request.getSize()
	        );

	        int page = request.getPage() != null ? request.getPage() : 0;
	        int size = request.getSize() != null ? request.getSize() : 10;

	        return adminService.getPrefundReports(
	                request.getMerchantId(),
	                request.getStatus(),
	                request.getFromDate(),
	                request.getToDate(),
	                page,
	                size
	        );
	    }


//	    this  method is for settlement history
	 @GetMapping("/settlementHistory")
	 public ResponseEntity<?> getSettlementReport(
	         @RequestParam(required = false) String merchantId,
	         @RequestParam(required = false) String status,
	         @RequestParam(required = false) 
	         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime fromDate,
	         @RequestParam(required = false) 
	         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime toDate) {
	     
	     try {
	         log.info("GET /settlementReport → merchantId: {}, status: {}, fromDate: {}, toDate: {}",
	                 merchantId, status, fromDate, toDate);

	         // Validate date range
	         if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
	             log.error("Invalid date range: fromDate {} is after toDate {}", fromDate, toDate);
	             return ResponseEntity.badRequest().body(
	                 Map.of("error", "fromDate cannot be after toDate")
	             );
	         }

	         // Validate status if provided
	         if (status != null && !status.isEmpty()) {
	             List<String> validStatuses = Arrays.asList("PENDING", "COMPLETED", "CANCELLED");
	             if (!validStatuses.contains(status.toUpperCase())) {
	                 log.error("Invalid status: {}", status);
	                 return ResponseEntity.badRequest().body(
	                     Map.of("error", "Invalid status. Valid values: PENDING, COMPLETED, CANCELLED")
	                 );
	             }
	         }

	         List<AdminSettlementHistoryResponseDTO> reports = reportService.getSettlementReport(
	                 merchantId, status, fromDate, toDate);

	         log.info("Successfully fetched {} settlement records", reports.size());
	         
	         Map<String, Object> response = new HashMap<>();
	         response.put("success", true);
	         response.put("data", reports);
	         response.put("count", reports.size());
	         
	         return ResponseEntity.ok(response);

	     } catch (Exception e) {
	         log.error("Error fetching settlement report: {}", e.getMessage(), e);
	         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
	             Map.of("success", false, "error", "Failed to fetch settlement report", 
	                    "message", e.getMessage())
	         );
	     }
	 } 
	    
// Settlemets  Reports
	 
	
	 @GetMapping("/settlementReport")
	    public ResponseEntity<?> getSettlementReport(
	            @RequestParam(required = false) String merchantId,
	            @RequestParam(required = false) String status,
	            @RequestParam(required = false) String pipe,
	            @RequestParam(required = false) 
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
	            @RequestParam(required = false) 
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
	        
	        try {
	            log.info("GET /settlementReport → merchantId: {}, status: {}, pipe: {}, fromDate: {}, toDate: {}",
	                    merchantId, status, pipe, fromDate, toDate);

	            // Validate date range
	            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
	                log.error("Invalid date range: fromDate {} is after toDate {}", fromDate, toDate);
	                return ResponseEntity.badRequest().body(
	                    Map.of("success", false, "error", "fromDate cannot be after toDate")
	                );
	            }

	            // Validate status if provided
	            if (status != null && !status.isEmpty()) {
	                List<String> validStatuses = Arrays.asList("PENDING", "COMPLETED", "CANCELLED");
	                if (!validStatuses.contains(status.toUpperCase())) {
	                    log.error("Invalid status: {}", status);
	                    return ResponseEntity.badRequest().body(
	                        Map.of("success", false, "error", "Invalid status. Valid values: PENDING, COMPLETED, CANCELLED")
	                    );
	                }
	            }

	            // Fetch settlement report
	            List<AdminSettlementReportResponseDTO> reports = reportService.getSettlementReportAdmin(
	                    merchantId, status, pipe, fromDate, toDate);

	            log.info("Successfully fetched {} settlement records", reports.size());
	            
	            Map<String, Object> response = new HashMap<>();
	            response.put("success", true);
	            response.put("data", reports);
	            response.put("count", reports.size());
	            
	            return ResponseEntity.ok(response);

	        } catch (Exception e) {
	            log.error("Error fetching settlement report: {}", e.getMessage(), e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
	                Map.of("success", false, "error", "Failed to fetch settlement report", 
	                       "message", e.getMessage())
	            );
	        }
	    }
	 
	 @GetMapping("/transaction-counts/year-month")
	 public ResponseEntity<?> transactionCountAndAmountYearMonthWise() {
	     logger.info("GET /transaction-counts/year-month");
	     return adminService.allTransactionCountAndAmountYearMonthWise();
	 }
	 
	 @GetMapping("/transaction-counts/overall")
	 public ResponseEntity<?> transactionCountAndAmountOverall() {
	     logger.info("GET /transaction-counts/overall");
	     return adminService.allTrasactionCountAndAmountOverall();
	 }

	 
	 @GetMapping("/getPayinHistory")
	    public ResponseEntity<?> getPayinHistory(
	            @RequestParam(required = false) String merchantId,
	            @RequestParam(required = false) String status,
	            @RequestParam(required = false) String txnId,
	            @RequestParam(required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
	            @RequestParam(required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
	            @RequestParam int page,
	            @RequestParam int size
	    ) {

	        try {
	            log.info(
	                "Fetching payin report | merchantId={}, status={}, txnId={}, fromDate={}, toDate={}, page={}, size={}",
	                merchantId, status, txnId, fromDate, toDate, page, size
	            );

	            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
	                Map<String, String> error = new HashMap<>();
	                error.put("error", "fromDate cannot be after toDate");
	                return ResponseEntity.badRequest().body(error);
	            }

	            Map<String, Object> reports =
	                    reportService.getPayinReport(
	                            merchantId, status, txnId, fromDate, toDate, page, size
	                    );

	            Map<String, Object> response = new HashMap<>();
	            response.put("success", true);
	            response.put("data", reports);

	            return ResponseEntity.ok(response);

	        } catch (Exception e) {
	            Map<String, String> error = new HashMap<>();
	            error.put("error", "Failed to fetch payin report");
	            error.put("message", e.getMessage());
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	        }
	    }

	 @PutMapping("/client-update/{userId}")
	 public ResponseEntity<?> updateClientByAdmin(
	         @PathVariable String userId,
	         @Valid @RequestBody AdminClientUpdateDto  dto) {

	     return adminService.updateClientByAdmin(userId, dto);
	 }
	 
	 
	 @PostMapping("/settlementRule")
		public ResponseEntity<?> settlementRule(@RequestBody @Valid SettlementRuleRequest request) {
			logger.info("POST /admin/settlement-rule/create → Request: userId={}, slotType={}, timeSlots={}",
					request.getUserId(), request.getSlotType(), request.getTimeSlots());
			ResponseEntity<?> response = adminService.createRule(request);
			logger.info("POST /admin/settlement-rule/create → Response Status: {}", response.getStatusCode());
			return response;
		}

		@GetMapping("/settlementList")
		public ResponseEntity<?> list() {
			logger.info("GET /admin/settlement-rule/list → Request");
			ResponseEntity<?> response = adminService.getAllRules();
			logger.info("GET /admin/settlement-rule/list → Response Status: {}", response.getStatusCode());
			return response;
		}


		@GetMapping("/run-settlement")
		public ResponseEntity<?> runSettlement() {

			autoSettlementService.runAutoSettlement();

			return ResponseEntity.ok("Settlement cron executed manually");
		}

}