package com.mippay.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mippay.service.PhonePeAuthService;

@RestController
@RequestMapping("/phonepe")
public class PhonePeTestController {

    private final com.mippay.service.PhonePeAuthService phonePeAuthService;

    public PhonePeTestController(PhonePeAuthService phonePeAuthService) {
        this.phonePeAuthService = phonePeAuthService;
    }

    @GetMapping("/token")
    public ResponseEntity<?> getToken() {
        return ResponseEntity.ok(phonePeAuthService.getAccessToken());
    }
}
