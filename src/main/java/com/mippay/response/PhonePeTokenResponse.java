package com.mippay.response;


import lombok.Data;

@Data
public class PhonePeTokenResponse {
    private String access_token;
    private Long expires_at;
    private String token_type;
}

