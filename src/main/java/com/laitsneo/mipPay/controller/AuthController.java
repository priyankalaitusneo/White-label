package com.laitsneo.mipPay.controller;

import com.laitsneo.mipPay.dto.Admin.ResponseDto;
import com.laitsneo.mipPay.dto.Client.LoginRequest;
import com.laitsneo.mipPay.dto.Client.LoginResponse;
import com.laitsneo.mipPay.entity.Admin.User;
import com.laitsneo.mipPay.entity.Client.Client;
import com.laitsneo.mipPay.repository.Admin.UserRepository;
import com.laitsneo.mipPay.repository.Client.ClientRepository;
import com.laitsneo.mipPay.service.AdminRoleService;
import com.laitsneo.mipPay.service.AdminService;
import com.laitsneo.mipPay.service.ClientService;
import com.laitsneo.mipPay.service.RoleService;
import com.laitsneo.mipPay.util.JWTHelper;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payment/auth")
//@CrossOrigin(origins = "*")
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
  
//  
//    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
//    	logger.info("POST /login → Login request received for username: {}", request.getUsername());
//        /* authenticate the username and password */
//        String authentication = this.doAuthenticate(request.getUsername(), request.getPassword());
//        logger.info("POST /login → Authentication result for {}: {}", request.getUsername(), authentication);
//        if (authentication.equals("Authenticated")) {
//            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
//            logger.info("POST /login → UserDetails loaded for {}", request.getUsername());
//            String adminId = this.adminRepository.findByEmail(userDetails.getUsername()).get().getAdminId();
//            logger.info("POST /login → AdminId fetched: {}", adminId);
//            /* Generate jwt token */
//            String token = this.jwtHelper.generateToken(userDetails, adminId);
//            logger.info("POST /login → JWT token generated for adminId: {}", adminId);
//            /* Get UserDetails by email */
//            User admin = this.adminService.getAdminByEmail(request.getUsername());
//            logger.info("POST /login → Admin entity fetched for {}: {}", request.getUsername(), admin);
//            List<String> roleIds = this.adminRoleService.getRoleByAdminId(admin.getAdminId());
//            logger.info("POST /login → RoleIds fetched for {}: {}", admin.getAdminId(), roleIds);
//            List<Map<String, Object>> roles = this.roleService.getRolesByRolesId(roleIds);
//            logger.info("POST /login → Roles resolved: {}", roles);
//            // Cookie creation
//            Cookie cookie = new Cookie("admin-jwt", token);
//            cookie.setHttpOnly(false);
//            cookie.setSecure(false);
//            cookie.setPath("/");
//            cookie.setMaxAge(24 * 60 * 60);
//            cookie.setAttribute("SameSite", "None");
//            response.addCookie(cookie);
//            logger.info("POST /login → JWT cookie added to response for user: {}", admin.getAdminId());
//            LoginResponse resp = LoginResponse.builder()
//                    .email(request.getUsername())
//                    .userId(admin.getAdminId())
//                    .role(roles)
//                    .build();
//            logger.info("POST /login → Login successful for {}, response: {}", request.getUsername(), resp);
//            return ResponseEntity.ok(resp);
//        } else {
//        	logger.warn("POST /login → Authentication failed for {}", request.getUsername());
//            return ResponseEntity.badRequest().body("Invalid username or password !!");
//        }
//    }
 


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
                .token(adminToken)   
                .role(roles)
                .build();

        logger.info("POST /login → Login successful for {}", request.getUsername());

        return ResponseEntity.ok(resp);
    }


    private String doAuthenticate(String username, String password) {
        logger.info("Authenticating user: {}", username);
        /* generating an authentication token with isAuthenticated() = false */
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        try {
            /* authenticating the token using AuthenticationManager */
            authenticationManager.authenticate(authenticationToken);
            logger.info("Authentication successful for user: {}", username);
            return "Authenticated";
        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for user: {} → Invalid credentials", username);
            return "Invalid username or password !!";
        }
    }


    /////////////// Api for forget Password OTP ///////////////
//    @PostMapping("/send-otp")
//    public ResponseEntity<ResponseDto> forgetPassword(@RequestParam String email){
//        String resellerResp = this.adminService.forgetPassword(email);
//        ResponseDto response = ResponseDto.builder().response(resellerResp).status("OK").statusCode(200).build();
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }

    /////////////// Verify otp for Forger Password ///////////////
//    @PostMapping("/verify-otp")
//    public ResponseEntity<ResponseDto> verifyOtp(@RequestParam String email, @RequestParam String otp){
//        String resellerResp = this.adminService.verifyOtp(email, otp);
//        System.out.println("resellerResp: "+resellerResp);
//        if(resellerResp.equals("Otp verified successful !!")){
//            ResponseDto response = ResponseDto.builder().response(resellerResp).statusCode(200).status("OK").build();
//            return ResponseEntity.status(HttpStatus.OK).body(response);
//        }else{
//            ResponseDto response = ResponseDto.builder().response(resellerResp).statusCode(400).status("BAD_REQUEST").build();
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
//    }

    /////////////// Api for Change Password ///////////////
//    @PutMapping("/change-password")
//    public ResponseEntity<ResponseDto> changePassword (@RequestParam String email, @RequestParam String password){
//        String resellerResp = this.adminService.changePasswordWithoutLogin(email, password);
//        ResponseDto response = ResponseDto.builder().response(resellerResp).status("OK").statusCode(200).build();
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }

    
       /// Client token in body 
       
    @PostMapping("/client-login")
    public ResponseEntity<?> clientlogin(
            @Valid @RequestBody LoginRequest request) {

        logger.info("POST /client-login → Login request received for username: {}", request.getUsername());

        String authentication = this.doAuthenticate(
                request.getUsername(),
                request.getPassword()
        );

        logger.info("POST /client-login → Authentication result for {}: {}",
                request.getUsername(), authentication);

        if (!"Authenticated".equals(authentication)) {
            logger.warn("POST /client-login → Authentication failed for {}", request.getUsername());
            return ResponseEntity.badRequest()
                    .body("Invalid username or password !!");
        }

        // Load user details
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.getUsername());

        logger.info("POST /client-login → UserDetails loaded for {}", request.getUsername());

        // Fetch clientId
        String clientId = clientRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Client not found"))
                .getUserId();

        logger.info("POST /client-login → ClientId fetched: {}", clientId);

        
        String token = jwtHelper.generateToken(userDetails, clientId);

        
        String clientToken = "client-jwt:" + token;

        // Fetch client entity (needed for response)
        Client client = clientService.getClientByEmail(request.getUsername());

        
        LoginResponse response = LoginResponse.builder()
                .email(request.getUsername())
                .userId(client.getUserId())
                .token(clientToken)   
                .role(null)
                .build();

        logger.info("POST /client-login → Login successful for {}, response: {}",
                request.getUsername(), response);

        return ResponseEntity.ok(response);
    }

    

    /////////////// API to login client ///////////////
//    @PostMapping("/client-login")
//    public ResponseEntity<?> clientlogin(@Valid @RequestBody LoginRequest request, HttpServletResponse resp) {
//    	logger.info("POST /client-login → Login request received for username: {}", request.getUsername());
//        /* authenticate the username and password */
//        String authentication = this.doAuthenticate(request.getUsername(), request.getPassword());
//        logger.info("POST /client-login → Authentication result for {}: {}", request.getUsername(), authentication);
//        if (authentication.equals("Authenticated")) {
//            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
//            logger.info("POST /client-login → UserDetails loaded for {}", request.getUsername());
//            String clientId = this.clientRepository.findByEmail(userDetails.getUsername()).get().getUserId();
//            logger.info("POST /client-login → ClientId fetched: {}", clientId);
//            /* Generate jwt token */
//            String token = this.jwtHelper.generateToken(userDetails, clientId);
//            logger.info("POST /client-login → JWT token generated for clientId: {}", clientId);
//            /* get UserDetails by email using loadUserByUsername method  */
//            Client client = this.clientService.getClientByEmail(request.getUsername());
//            logger.info("POST /client-login → Client entity fetched: {}", client);
//            // Creating cookie
//            Cookie cookie = new Cookie("client-jwt", token);
//            cookie.setHttpOnly(true);
//            cookie.setSecure(true);
//            cookie.setPath("/");
//            cookie.setMaxAge(24 * 60 * 60);
//            cookie.setAttribute("SameSite", "None");
//            resp.addCookie(cookie);
//            logger.info("POST /client-login → JWT cookie added for clientId: {}", client.getUserId());
//            ClientLoginResponse response = ClientLoginResponse.builder()
//                    .email(request.getUsername())
//                    .userId(client.getUserId())
//                    .build();
//            logger.info("POST /client-login → Login successful for {}, Response: {}", request.getUsername(), response);
//            return ResponseEntity.ok(response);
//        } else {
//        	logger.warn("POST /client-login → Authentication failed for {}", request.getUsername());
//            return ResponseEntity.badRequest().body("Invalid username or password !!");
//        }
//    }
//

    @PostMapping("/CallBack")
    public String getChargesByUserId(@RequestBody Map<String, Object> request) {
    	logger.info("POST /CallBack → Callback request received: {}", request);
        String response = this.clientService.saveCallBack(request);
        logger.info("POST /CallBack → Callback service response: {}", response);
        return response;
    }


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


}
