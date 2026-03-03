package com.laitsneo.whitelbl.entity.Client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Table(name = "prefundrequests")
@Data
public class PrefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long prefundId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "reference", length = 255, unique = true)
    private String reference;


    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "mobile_num", length = 20)
    private String mobileNum;

    @Column(name = "client_acc_num", length = 50)
    private String clientAccNum;

    @Column(name = "client_ifsc", length = 20)
    private String clientIfsc;

    @Column(name = "admin_acc_num", length = 50)
    private String adminAccNum;

    @Column(name = "admin_ifsc", length = 20)
    private String adminIfsc;

    @Column(name = "status", length = 50)
    private String status;

    private String lienStatus;
    
    @Column(name = "paymentmethod", length = 50)
    private String paymentMethod;

    @Column(name = "requested_date", nullable = false)
    private LocalDateTime requestedDate;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;
    
    @Column(name = "approveby")
    private String approveBy;
    
    private String remarks;

    @PrePersist
    protected void onCreate() {
        if (requestedDate == null) {
            requestedDate = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
    }
}