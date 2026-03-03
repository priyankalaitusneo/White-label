package com.mippay.dto.Client;

import java.time.LocalDateTime;

public class AuthenticationResponseDto {

    private String userId;
    private String clientName;
    private String clientId;
    private String clientSecret;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public AuthenticationResponseDto(String userId, String clientName, String clientId, String clientSecret,
                                     LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.userId = userId;
        this.clientName = clientName;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
