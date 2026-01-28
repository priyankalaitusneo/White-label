package com.mippay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mippay.dto.Admin.PayinDto;
import com.mippay.response.PhonePeOrderStatusResponse;
import com.mippay.service.ClientService;
import com.mippay.service.PhonePeAuthService;
import com.mippay.serviceImpl.Client.ClientServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/phonepe")
public class PhonePeTestController {

    private final com.mippay.service.PhonePeAuthService phonePeAuthService;
    
    @Autowired
    private  ClientServiceImpl clientService;
    
    private ClientService clientServiceInterface;

    public PhonePeTestController(PhonePeAuthService phonePeAuthService) {
        this.phonePeAuthService = phonePeAuthService;
    }

    @GetMapping("/token")
    public ResponseEntity<?> getToken() {
        return ResponseEntity.ok(phonePeAuthService.getAccessToken());
    }
    
    @GetMapping("/order-status/{merchantOrderId}")
    public ResponseEntity<?> checkOrderStatus(
            @PathVariable String merchantOrderId,
            @RequestParam(defaultValue = "false") boolean details,
            @RequestParam(defaultValue = "false") boolean errorContext
    ) {
        PhonePeOrderStatusResponse response =
        		clientService.checkStatus(
                        merchantOrderId,
                        details,
                        errorContext
                );
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/payment")
    public ResponseEntity<?> paymentPayin(
            @Valid @RequestBody PayinDto data,
            @RequestHeader("Client-Id") String clientId,
            @RequestHeader("Client-SecretId") String clientSecretId,
            HttpServletRequest request) throws Exception {

        return clientServiceInterface.paymentPayinPhonepe(
                data,
                clientId,
                clientSecretId,
                request
        );
    }
}
