package com.mippay.controller;

import com.mippay.dto.Admin.ResponseDto;
import com.mippay.dto.Client.ClientLoginResponse;
import com.mippay.dto.Client.LoginRequest;
import com.mippay.dto.Client.LoginResponse;

import com.mippay.entity.Admin.User;
import com.mippay.entity.Client.Client;

import com.mippay.repository.Admin.UserRepository;
import com.mippay.repository.Client.ClientRepository;

import com.mippay.service.AdminRoleService;
import com.mippay.service.AdminService;
import com.mippay.service.ClientService;
import com.mippay.service.PhonePeAuthService;
import com.mippay.service.RoleService;

import com.mippay.util.JWTHelper;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payment/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    private AdminService adminService;
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    JWTHelper jwtHelper;
    @Autowired
    RoleService roleService;
    @Autowired
    private AdminRoleService adminRoleService;
    @Autowired
    ClientService clientService;
    @Autowired
    UserRepository adminRepository;
    @Autowired
    ClientRepository clientRepository;
    
    
    @Autowired
    private PhonePeAuthService phonePeAuthService;


    /////////////// API to create new admin ///////////////
    @PostMapping("/create_admin")
    public ResponseEntity<ResponseDto> onboardingReseller(@Valid @RequestBody User request) {
    	logger.info("POST /create_admin -> Request: {}", request);
        String resp = this.adminService.createAdmin(request);
        logger.info("POST /create_admin -> Service Response: {}", resp);
        ResponseDto responseDto = ResponseDto.builder()
                .response(resp)
                .status("CREATED")
                .statusCode(201)
                .build();
        logger.info("POST /create_admin -> Final Response: {}", responseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }


    /////////////// API to login admin ///////////////
    @PostMapping("/admin-login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    	logger.info("POST /login → Login request received for username: {}", request.getUsername());
        /* authenticate the username and password */
        String authentication = this.doAuthenticate(request.getUsername(), request.getPassword());
        logger.info("POST /login → Authentication result for {}: {}", request.getUsername(), authentication);
        if (authentication.equals("Authenticated")) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            logger.info("POST /login → UserDetails loaded for {}", request.getUsername());
            String adminId = this.adminRepository.findByEmail(userDetails.getUsername()).get().getAdminId();
            logger.info("POST /login → AdminId fetched: {}", adminId);
            /* Generate jwt token */
            String token = this.jwtHelper.generateToken(userDetails, adminId);
            logger.info("POST /login → JWT token generated for adminId: {}", adminId);
            /* Get UserDetails by email */
            User admin = this.adminService.getAdminByEmail(request.getUsername());
            logger.info("POST /login → Admin entity fetched for {}: {}", request.getUsername(), admin);
            List<String> roleIds = this.adminRoleService.getRoleByAdminId(admin.getAdminId());
            logger.info("POST /login → RoleIds fetched for {}: {}", admin.getAdminId(), roleIds);
            List<Map<String, Object>> roles = this.roleService.getRolesByRolesId(roleIds);
            logger.info("POST /login → Roles resolved: {}", roles);
            // Cookie creation
            Cookie cookie = new Cookie("admin-jwt", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            cookie.setAttribute("SameSite", "None");
            response.addCookie(cookie);
            logger.info("POST /login → JWT cookie added to response for user: {}", admin.getAdminId());
            LoginResponse resp = LoginResponse.builder()
                    .email(request.getUsername())
                    .userId(admin.getAdminId())
                    .role(roles)
                    .build();
            logger.info("POST /login → Login successful for {}, response: {}", request.getUsername(), resp);
            return ResponseEntity.ok(resp);
        } else {
        	logger.warn("POST /login → Authentication failed for {}", request.getUsername());
            return ResponseEntity.badRequest().body("Invalid username or password !!");
        }
    }
 


    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        logger.info("POST /login → Login request received for username: {}", request.getUsername());

        String authentication = this.doAuthenticate(
                request.getUsername(),
                request.getPassword()
        );

        if (!"Authenticated".equals(authentication)) {
            logger.warn("POST /login → Authentication failed for {}", request.getUsername());
            return ResponseEntity.badRequest()
                    .body("Invalid username or password !!");
        }

        // Load user details
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.getUsername());

        // Fetch adminId
        String adminId = adminRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Admin not found"))
                .getAdminId();

        // Generate JWT ONCE
        String token = jwtHelper.generateToken(userDetails, adminId);

        // Fetch admin & roles
        User admin = adminService.getAdminByEmail(request.getUsername());
        List<String> roleIds =
                adminRoleService.getRoleByAdminId(admin.getAdminId());
        List<Map<String, Object>> roles =
                roleService.getRolesByRolesId(roleIds);

       
        String adminToken = "admin-jwt:" + token;

        // Build response
        LoginResponse resp = LoginResponse.builder()
                .email(request.getUsername())
                .userId(admin.getAdminId())
//                .token(adminToken)
                .role(roles)
                .build();

        logger.info("POST /login → Login successful for {}", request.getUsername());

        return ResponseEntity.ok(resp);
    }


    private String doAuthenticate(String username, String password) {
        logger.info("Authenticating user: {}", username);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password);

        try {
            Authentication authentication =
                    authenticationManager.authenticate(authToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.info("Authentication successful for user: {}", username);
            return "Authenticated";

        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for user: {}", username);
            return "Invalid username or password !!";
        }
    }


    @PostMapping("/client-login")
    public ResponseEntity<?> clientlogin(@Valid @RequestBody LoginRequest request, HttpServletResponse resp) {
        logger.info("POST /client-login → Login request received for username: {}", request.getUsername());
        /* authenticate the username and password */
        String authentication = this.doAuthenticate(request.getUsername(), request.getPassword());
        logger.info("POST /client-login → Authentication result for {}: {}", request.getUsername(), authentication);
        if (authentication.equals("Authenticated")) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            logger.info("POST /client-login → UserDetails loaded for {}", request.getUsername());
            String clientId = this.clientRepository.findByEmail(userDetails.getUsername()).get().getUserId();
            logger.info("POST /client-login → ClientId fetched: {}", clientId);
            /* Generate jwt token */
            String token = this.jwtHelper.generateToken(userDetails, clientId);
            logger.info("POST /client-login → JWT token generated for clientId: {}", clientId);
            /* get UserDetails by email using loadUserByUsername method  */
            Client client = this.clientService.getClientByEmail(request.getUsername());
            logger.info("POST /client-login → Client entity fetched: {}", client);
            // Creating cookie
            Cookie cookie = new Cookie("client-jwt", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            cookie.setAttribute("SameSite", "None");
            resp.addCookie(cookie);
            logger.info("POST /client-login → JWT cookie added for clientId: {}", client.getUserId());
            ClientLoginResponse response = ClientLoginResponse.builder()
                    .email(request.getUsername())
                    .userId(client.getUserId())
                    .build();
            logger.info("POST /client-login → Login successful for {}, Response: {}", request.getUsername(), response);
            return ResponseEntity.ok(response);
        } else {
            logger.warn("POST /client-login → Authentication failed for {}", request.getUsername());
            return ResponseEntity.badRequest().body("Invalid username or password !!");
        }
    }
       
//    @PostMapping("/client-login")
//    public ResponseEntity<?> clientlogin(
//            @Valid @RequestBody LoginRequest request) {
//
//        logger.info("POST /client-login → Login request received for username: {}", request.getUsername());
//
//        String authentication = this.doAuthenticate(
//                request.getUsername(),
//                request.getPassword()
//        );
//
//        logger.info("POST /client-login → Authentication result for {}: {}",
//                request.getUsername(), authentication);
//
//        if (!"Authenticated".equals(authentication)) {
//            logger.warn("POST /client-login → Authentication failed for {}", request.getUsername());
//            return ResponseEntity.badRequest()
//                    .body("Invalid username or password !!");
//        }
//
//        // Load user details
//        UserDetails userDetails =
//                userDetailsService.loadUserByUsername(request.getUsername());
//
//        logger.info("POST /client-login → UserDetails loaded for {}", request.getUsername());
//
//        // Fetch clientId
//        String clientId = clientRepository
//                .findByEmail(userDetails.getUsername())
//                .orElseThrow(() -> new RuntimeException("Client not found"))
//                .getUserId();
//
//        logger.info("POST /client-login → ClientId fetched: {}", clientId);
//        String token = jwtHelper.generateToken(userDetails, clientId);
//        String clientToken = "client-jwt:" + token;
//        // Fetch client entity (needed for response)
//        Client client = clientService.getClientByEmail(request.getUsername());
//        LoginResponse response = LoginResponse.builder()
//                .email(request.getUsername())
//                .userId(client.getUserId())
////                .token(clientToken)
//                .role(null)
//                .build();
//        logger.info("POST /client-login → Login successful for {}, response: {}",
//                request.getUsername(), response);
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/CallBack")
//    public String getChargesByUserId(@RequestBody Map<String, Object> request) {
//    	logger.info("POST /CallBack → Callback request received: {}", request);
//        String response = this.clientService.saveCallBack(request);
//        logger.info("POST /CallBack → Callback service response: {}", response);
//        return response;
//    }


    @PostMapping("/admin-logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        logger.info("POST /admin-logout → Admin logout requested");
        Cookie jwtCookie = new Cookie("admin-jwt", null);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setMaxAge(0);
        jwtCookie.setAttribute("SameSite", "None");
        response.addCookie(jwtCookie);
        logger.info("POST /admin-logout → Admin JWT cookie cleared successfully");
        return ResponseEntity.ok("Logged out successfully");
    }


    @PostMapping("/client-logout")
    public ResponseEntity<?> clientLogout(HttpServletResponse response) {
    	logger.info("POST /client-logout → Client logout requested");
        Cookie jwtCookie = new Cookie("client-jwt", null);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setMaxAge(0);
        jwtCookie.setAttribute("SameSite", "None");
        response.addCookie(jwtCookie);
        logger.info("POST /client-logout → Client JWT cookie cleared successfully");
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/phonepe-callback")
  public String phonePeCallback(
          @RequestBody Map<String, Object> request,
          @RequestHeader("Authorization") String authorizationHeader
  ) {

      logger.info("PhonePe Webhook received: {}", request);
      
      System.out.println(request+"-------------"+authorizationHeader);

      // Authentication check (explained below)
      if (!phonePeAuthService.verifyAuthorization(authorizationHeader)) {
          logger.warn("Invalid PhonePe webhook authorization");
          return "UNAUTHORIZED";
      }

      return clientService.handlePhonePeWebhook(request);
  }


  @PostMapping("buckbox-callback")
    public String buckBoxCallBack(@RequestBody Map<String, Object> data){
      System.out.println("Call back recieved from buckBox: "+ data);
      return "SUCCESS";
  }

}
