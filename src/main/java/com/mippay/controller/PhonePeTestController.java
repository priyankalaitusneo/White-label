package com.mippay.controller;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

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
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${phonepe.webhook.username}")
    private String webhookUsername;

    @Value("${phonepe.webhook.password}")
    private String webhookPassword;
    
	private static final Logger logger = LoggerFactory.getLogger(ClientControlller.class);


    
    @Autowired
    private  ClientServiceImpl clientService;
    

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

        return clientService.paymentPayinPhonepe(
                data,
                clientId,
                clientSecretId,
                request
        );
    }
    
    @GetMapping("/callback")
    public ResponseEntity<String> phonePeCallback(
            @RequestParam String orderId) {

        System.out.println("PHONEPE CALLBACK RECEIVED");
        System.out.println("OrderId = " + orderId);

        return ResponseEntity.ok(
                "Payment received successfully for orderId=" + orderId
        );
    }
    
    @PostMapping("/callback")
    public String phonePeCallBack(@RequestBody Map<String, Object> request) {
        logger.info("POST /phonepe/callback → Callback request received: {}", request);

        String response = clientService.savePhonePeCallBack(request);

        logger.info("POST /phonepe/callback → Callback service response: {}", response);
        return response;
    }

}
