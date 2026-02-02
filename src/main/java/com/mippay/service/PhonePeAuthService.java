package com.mippay.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.mippay.response.PhonePeOrderStatusResponse;
import com.mippay.response.PhonePeTokenResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class PhonePeAuthService {

    @Value("${phonepe.client-id}")
    private String clientId;

    @Value("${phonepe.client-secret}")
    private String clientSecret;

    @Value("${phonepe.client-version}")
    private Integer clientVersion;

    @Value("${phonepe.token-url}")
    private String tokenUrl;

    private PhonePeTokenResponse cachedToken;

    public synchronized String getAccessToken() {

        // Reuse token if valid
        if (cachedToken != null &&
                cachedToken.getExpires_at() > Instant.now().getEpochSecond()) {
            return cachedToken.getAccess_token();
        }

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("client_version", String.valueOf(clientVersion));
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<PhonePeTokenResponse> response =
                restTemplate.postForEntity(
                        tokenUrl,
                        request,
                        PhonePeTokenResponse.class
                );

        cachedToken = response.getBody();
        return cachedToken.getAccess_token();
    }

    
    private static final String USERNAME = "olivia19";
    private static final String PASSWORD = "olivia123";

    public boolean verifyAuthorization(String authHeader) {
    	
    	System.out.println(authHeader+"------------");

        if (authHeader == null || !authHeader.startsWith("SHA256")) {
            return false;
        }

        String expectedHash = sha256(USERNAME +":"+PASSWORD);
        String receivedHash = authHeader.replace("SHA256", "").trim();
System.out.println(receivedHash+"--------------expected"+expectedHash);
        return expectedHash.equalsIgnoreCase(receivedHash);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return null;
        }
    }
}
