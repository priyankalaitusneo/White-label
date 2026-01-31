package com.mippay.controller;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    
    /* =======================
    WEBHOOK CALLBACK (S2S)
 ======================= */
    @PostMapping("/webhook")
    public ResponseEntity<String> phonePeWebhook(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> body
    ) {
        try {
            String username = "olivia19";
            String password = "olivia123";

            String raw = username + ":" + password;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }

            String expectedAuth = "SHA256(" + hex.toString() + ")";

            System.out.println("RAW STRING      = " + raw);
            System.out.println("EXPECTED AUTH   = " + expectedAuth);
            System.out.println("RECEIVED AUTH   = " + authorization);

            if (authorization == null || !authorization.equals(expectedAuth)) {
                System.out.println("INVALID WEBHOOK AUTH");
                return ResponseEntity.status(401).body("INVALID WEBHOOK AUTH");
            }

            System.out.println("WEBHOOK AUTH VERIFIED");
            System.out.println("PAYLOAD = " + body);

            String event = (String) body.get("event");
            Map<String, Object> payload = (Map<String, Object>) body.get("payload");

            System.out.println("EVENT = " + event);
            System.out.println("MERCHANT ORDER ID = " + payload.get("merchantOrderId"));
            System.out.println("STATE = " + payload.get("state"));

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("ERROR");
        }
    }

 /* =======================
    SHA256 HELPER
 ======================= */
 private String sha256Hex(String value) throws Exception {
     MessageDigest md = MessageDigest.getInstance("SHA-256");
     byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
     StringBuilder sb = new StringBuilder();
     for (byte b : digest) {
         sb.append(String.format("%02x", b));
     }
     return sb.toString();
 }
    
 
}
