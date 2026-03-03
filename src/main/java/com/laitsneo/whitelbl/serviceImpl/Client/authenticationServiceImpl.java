package com.laitsneo.whitelbl.serviceImpl.Client;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.laitsneo.whitelbl.dto.Client.AuthenticationResponseDto;
import com.laitsneo.whitelbl.entity.Client.Authentication;
import com.laitsneo.whitelbl.entity.Client.Client;
import com.laitsneo.whitelbl.repository.Client.AuthenticationRepository;
import com.laitsneo.whitelbl.repository.Client.ClientRepository;
import com.laitsneo.whitelbl.service.AuthenticationService;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class authenticationServiceImpl implements AuthenticationService {
	
    Logger logger = LoggerFactory.getLogger(authenticationServiceImpl.class);


    @Autowired
    private AuthenticationRepository authenticationRepository;

    @Autowired
    private ClientRepository clientRepository;
    
    @Override
    @Transactional
    public AuthenticationResponseDto createAuthentication(String userId) {
    	logger.info("createAuthentication() → Started for userId: {}", userId);
        // 1. Fetch client
        Client client = clientRepository.findByUserId(userId)
                .orElseThrow(() -> {
                	logger.warn("createAuthentication() → Client not found for userId: {}", userId);
                    return new RuntimeException("Client not found for userId: " + userId);
                });
        logger.info("createAuthentication() → Client fetched: {}", client.getUserId());
        // 2. Check if authentication already exists
        if (authenticationRepository.findByUserId(userId).isPresent()) {
        	logger.warn("createAuthentication() → Authentication already exists for userId: {}", userId);
            throw new RuntimeException("Authentication credentials already exist for userId: " + userId);
        }
        // 3. Create new authentication
        Authentication authentication = new Authentication(
                userId,
                client.getName(),
                generateClientId(),
                generateClientSecret()
        );
        authentication.setPgId("PG_001");
        logger.info("createAuthentication() → Generated credentials for userId: {}", userId);
        try {
            // 4. Save in DB
            Authentication savedAuth = authenticationRepository.save(authentication);
            logger.info("createAuthentication() → Authentication saved successfully for userId: {}", userId);
            // 5. Convert to DTO and return
            AuthenticationResponseDto dto = convertToDto(savedAuth);
            logger.info("createAuthentication() → Returning DTO for userId: {}", userId);
            return dto;
        } catch (DataIntegrityViolationException e) {
        	logger.error("createAuthentication() → Duplicate clientId generated for userId: {} | Error: {}", userId, e.getMessage());
            throw new RuntimeException(
                    "Failed to create authentication. Duplicate clientId generated. Please try again."
            );
        }
    }


    @Override
    public AuthenticationResponseDto getAuthenticationByUserId(String userId) {
    	logger.info("getAuthenticationByUserId() → Fetching authentication for userId: {}", userId);
        Authentication auth = authenticationRepository.findByUserId(userId)
                .orElseThrow(() -> {
                	logger.warn("getAuthenticationByUserId() → Authentication not found for userId: {}", userId);
                    return new RuntimeException("Authentication not found for userId: " + userId);
                });
        logger.info("getAuthenticationByUserId() → Authentication found for userId: {}", userId);
        return convertToDto(auth);
    }


    @Override
    @Transactional
    public AuthenticationResponseDto updateAuthentication(String userId) {
    	logger.info("updateAuthentication() → Started for userId: {}", userId);
        // 1. Fetch existing authentication
        Authentication authentication = authenticationRepository.findByUserId(userId)
                .orElseThrow(() -> {
                	logger.warn("updateAuthentication() → Authentication not found for userId: {}", userId);
                    return new RuntimeException("Authentication not found for userId: " + userId);
                });
        logger.info("updateAuthentication() → Existing authentication found for userId: {}", userId);
        // 2. Fetch client to ensure it still exists
        Client client = clientRepository.findByUserId(userId)
                .orElseThrow(() -> {
                	logger.warn("updateAuthentication() → Client not found for userId: {}", userId);
                    return new RuntimeException("Client not found for userId: " + userId);
                });
        logger.info("updateAuthentication() → Client found: {}", client.getUserId());
        // 3. Update clientId and clientSecret (regenerate both)
        authentication.setClientId(generateClientId());
        authentication.setClientSecret(generateClientSecret());
        authentication.setClientName(client.getName()); // Update client name in case it changed
        logger.info("updateAuthentication() → Credentials regenerated for userId: {}", userId);
        try {
            // 4. Save updated authentication
            Authentication updatedAuth = authenticationRepository.save(authentication);
            logger.info("updateAuthentication() → Authentication updated successfully for userId: {}", userId);
            // 5. Convert to DTO and return
            AuthenticationResponseDto dto = convertToDto(updatedAuth);
            logger.info("updateAuthentication() → Returning updated DTO for userId: {}", userId);
            return dto;
        } catch (DataIntegrityViolationException e) {
        	logger.error(
                    "updateAuthentication() → Duplicate clientId generated during update for userId: {} | Error: {}",
                    userId, e.getMessage()
            );
            throw new RuntimeException(
                    "Failed to update authentication. Duplicate clientId generated. Please try again."
            );
        }
    }

    @Override
    @Transactional
    public void deleteAuthentication(String userId) {
    	logger.info("deleteAuthentication() → Started for userId: {}", userId);
        // 1. Fetch authentication to delete
        Authentication authentication = authenticationRepository.findByUserId(userId)
                .orElseThrow(() -> {
                	logger.warn("deleteAuthentication() → Authentication not found for userId: {}", userId);
                    return new RuntimeException("Authentication not found for userId: " + userId);
                });
        logger.info("deleteAuthentication() → Authentication found, deleting for userId: {}", userId);
        // 2. Delete the authentication record
        authenticationRepository.delete(authentication);
        logger.info("deleteAuthentication() → Authentication deleted successfully for userId: {}", userId);
    }


    @Override
    public List<AuthenticationResponseDto> getAllAuthentications() {
    	logger.info("getAllAuthentications() → Fetching all authentication records");
        List<Authentication> authentications = authenticationRepository.findAll();
        logger.info("getAllAuthentications() → {} records found", authentications.size());
        return authentications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    /**
     * Generate secure client ID (CID_[8 chars])
     * Checks for uniqueness to avoid duplicates
     */
    private String generateClientId() {
    	logger.info("generateClientId() → Generating new clientId");
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        String clientId;
        // Generate until we get a unique clientId
        do {
            StringBuilder sb = new StringBuilder("CID_");
            for (int i = 0; i < 8; i++) {
                sb.append(characters.charAt(random.nextInt(characters.length())));
            }
            clientId = sb.toString();
            logger.info("generateClientId() → Generated candidate: {}", clientId);
        } while (authenticationRepository.existsByClientId(clientId));
        logger.info("generateClientId() → Unique clientId generated: {}", clientId);
        return clientId;
    }


    /**
     * Generate secure client secret (32 chars)
     */
    private String generateClientSecret() {
    	logger.info("generateClientSecret() → Generating new clientSecret");
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder clientSecret = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            clientSecret.append(characters.charAt(random.nextInt(characters.length())));
        }
        logger.info("generateClientSecret() → Client secret generated successfully");
        return clientSecret.toString();
    }


    /**
     * Convert Authentication entity to DTO
     */
    private AuthenticationResponseDto convertToDto(Authentication authentication) {
    	logger.info("convertToDto() → Converting entity to DTO for userId: {}", authentication.getUserId());
        return new AuthenticationResponseDto(
                authentication.getUserId(),
                authentication.getClientName(),
                authentication.getClientId(),
                authentication.getClientSecret(),
                authentication.getCreatedDate(),
                authentication.getUpdatedDate()
        );
    }

}
