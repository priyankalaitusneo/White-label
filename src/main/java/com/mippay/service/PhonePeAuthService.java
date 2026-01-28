package com.mippay.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.mippay.response.PhonePeOrderStatusResponse;
import com.mippay.response.PhonePeTokenResponse;

import java.time.Instant;

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

}
