package com.laitsneo.whitelbl.controller;

import com.laitsneo.whitelbl.repository.Admin.UserRepository;
import com.laitsneo.whitelbl.repository.Client.ClientRepository;
import com.laitsneo.whitelbl.util.JWTHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthVerifyController {
	
    Logger logger = LoggerFactory.getLogger(AuthVerifyController.class);


    @Autowired
    private JWTHelper jwtHelper;

    @Autowired
    private UserRepository adminRepository;

    @Autowired
    private ClientRepository clientRepository;

    private UserDetails buildUser(String username, String password, String role) {
        return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password(password)
                .authorities(role)
                .build();
    }

    private String extractToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }


    //ADMIN VERIFY
    @GetMapping("/payment/admin/verify")
    public ResponseEntity<?> verifyAdmin(@CookieValue(value = "admin-jwt", required = false) String token) {
        //        String token = extractToken(request.getCookies().toString());
    	logger.info("GET /payment/admin/verify → Token received: {}", token);
        if (token == null || token.isEmpty()) {
        	logger.warn("GET /payment/admin/verify → Token missing");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token missing");
        }
        try {
            String username = jwtHelper.getUsernameFromToken(token);
            logger.info("GET /payment/admin/verify → Extracted username from token: {}", username);
            var adminOpt = adminRepository.findByEmail(username);
            if (adminOpt.isEmpty()) {
            	logger.warn("GET /payment/admin/verify → No admin found with email: {}", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not an admin");
            }
            var admin = adminOpt.get();
            logger.info("GET /payment/admin/verify → Admin found: {}", admin.getAdminId());
            UserDetails user = buildUser(admin.getEmail(), admin.getPassword(), "ROLE_ADMIN");
            boolean isValid = jwtHelper.validateToken(token, user);
            logger.info("GET /payment/admin/verify → Token validation result for {}: {}", username, isValid);
            if (!isValid) {
            	logger.warn("GET /payment/admin/verify → Invalid or expired token for {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalid or expired");
            }
            logger.info("GET /payment/admin/verify → Admin verification successful for {}", username);
            return ResponseEntity.ok(admin);
        } catch (Exception e) {
        	logger.error("GET /payment/admin/verify → Exception during token verification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }



 // CLIENT VERIFY //
    @GetMapping("/payment/client/verify")
    public ResponseEntity<?> verifyClient(@CookieValue(value = "client-jwt", required = false) String token) {
        //        String token = extractToken(request.getCookies().toString());
        //        System.out.println("token: "+token);
    	logger.info("GET /payment/client/verify → Token received: {}", token);
        if (token == null || token.isEmpty()) {
        	logger.warn("GET /payment/client/verify → Token missing");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token missing");
        }
        try {
            String username = jwtHelper.getUsernameFromToken(token);
            logger.info("GET /payment/client/verify → Username extracted from token: {}", username);
            var clientOpt = clientRepository.findByEmail(username);
            if (clientOpt.isEmpty()) {
            	logger.warn("GET /payment/client/verify → No client found with email: {}", username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not a client");
            }
            var client = clientOpt.get();
            logger.info("GET /payment/client/verify → Client found: {}", client.getUserId());
            UserDetails user = buildUser(client.getEmail(), client.getPassword(), "ROLE_CLIENT");
            boolean valid = jwtHelper.validateToken(token, user);
            logger.info("GET /payment/client/verify → Token validation result: {}", valid);
            if (!valid) {
            	logger.warn("GET /payment/client/verify → Invalid or expired token for client: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalid or expired");
            }
            logger.info("GET /payment/client/verify → Client verification successful for {}", username);
            return ResponseEntity.ok(client);
        } catch (Exception e) {
        	logger.error("GET /payment/client/verify → Exception occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }

}
