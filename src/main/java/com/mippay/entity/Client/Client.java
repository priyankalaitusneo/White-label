package com.mippay.entity.Client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "clients")
@Data

public class Client implements UserDetails {

    @Id
    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;   // Random 5–6 digit number (set manually in @PrePersist)

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "mobile_num", nullable = false, length = 20)
    private String mobileNum;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "account_num", length = 50)
    private String accountNum;

    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "gst", length = 15)
    private String gst;

    @Column(name = "cin", length = 21)
    private String cin;

    private String otp;
    
    
    @Column(name = "merchant_type", length = 20)
    private String merchantType;
    
    @Column(name = "account_bal", precision = 19, scale = 2)
    private BigDecimal accountBal;

    private String virtualAccNo;

    @Column(name = "created_date", nullable = false)
    @CreationTimestamp
    private Date createdDate = new Date();

    @Column(name = "updated_date")
    @UpdateTimestamp
    private Date updatedDate = new Date();
    
    private Double walletBalance;
    
    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "pan", length = 10)
    private String pan;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }
}
