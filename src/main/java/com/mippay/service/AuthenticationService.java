package com.mippay.service;



import java.util.List;

import com.mippay.dto.Client.AuthenticationResponseDto;

public interface AuthenticationService {

    /**
     * Create authentication credentials for a client
     * @param userId - The user ID from Client entity
     * @return AuthenticationResponseDto with generated credentials
     */
    AuthenticationResponseDto createAuthentication(String userId);

    /**
     * Get authentication details by userId
     * @param userId - The user ID
     * @return AuthenticationResponseDto with existing credentials
     */
    AuthenticationResponseDto getAuthenticationByUserId(String userId);

    /**
     * Update authentication credentials (regenerates clientId and clientSecret)
     * @param userId - The user ID
     * @return AuthenticationResponseDto with new credentials
     */
    AuthenticationResponseDto updateAuthentication(String userId);

    /**
     * Delete authentication credentials
     * @param userId - The user ID
     */
    void deleteAuthentication(String userId);

    /**
     * Get all authentication records
     * @return List of all AuthenticationResponseDto
     */
    List<AuthenticationResponseDto> getAllAuthentications();
}
