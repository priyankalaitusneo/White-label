package com.laitsneo.whitelbl.entity.Client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "clients",
        indexes = {
                @Index(name = "idx_email", columnList = "email"),
                @Index(name = "idx_mobile", columnList = "mobile_num")
        })
@Data
public class Client implements UserDetails {

    @Id
    @Column(name = "user_id", nullable = false, unique = true, length = 20)
    private String userId;

    // ======================
    // BASIC DETAILS (UI STEP 1)
    // ======================

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;
    
    private BigDecimal accountBal;

    @Column(name = "mobile_num", nullable = false)
    private String mobileNum;

    @Column(nullable = false)
    private String password;

    private String dob;

    @Column(name = "aadhaar_number")
    private String aadhaarNumber;

    private String pan;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String state;
    private String city;
    private String pincode;
    private String houseNumber;
    private String landmark;

    // ======================
    // ORGANIZATION DETAILS (UI STEP 2)
    // ======================

    private String merchantType;     // PAYIN / PAYOUT / BOTH
    private String accountNum;
    private String ifscCode;
    private String bankName;
    private String gst;
    private String cin;

    // ======================
    // SCHEME (UI STEP 3)
    // ======================

    private String scheme;

    // ======================
    // DOCUMENT PATHS (FILES)
    // ======================

    @Column(columnDefinition = "TEXT")
    private String aadhaarFrontPath;

    @Column(columnDefinition = "TEXT")
    private String aadhaarBackPath;

    @Column(columnDefinition = "TEXT")
    private String panPath;

    @Column(columnDefinition = "TEXT")
    private String gstPath;

    @Column(columnDefinition = "TEXT")
    private String shopPhotoPath;

    @Column(columnDefinition = "TEXT")
    private String profilePhotoPath;

    // ======================
    // WALLET / STATUS
    // ======================

    @Column(precision = 19, scale = 2)
    private BigDecimal walletBalance;

    private String status;   // PENDING / ACTIVE / REJECTED / BLOCKED

    private String virtualAccNo;

    // ======================
    // AUDIT
    // ======================

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // ======================
    // AUTO FIELDS
    // ======================

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdDate = now;
        this.updatedDate = now;

        if (this.walletBalance == null)
            this.walletBalance = BigDecimal.ZERO;

        if (this.status == null)
            this.status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // ======================
    // SPRING SECURITY METHODS
    // ======================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"BLOCKED".equalsIgnoreCase(this.status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }
}