package com.laitsneo.mipPay.entity.Client;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity

public class PayoutRecords {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int slNo;
    private String userId;
    private String name;
    private String email;
    private String number;
    private String  accNumber;
    private String ifsc;
    private double charges;
    private double gstCharges;
    private double amount;
    private double finalAmount;
    private String status;
    private String statusCode;
    private String utr;
    private String refundStatus;
    private double currentBalance;
    private double updatedBalance;
    private String transferMode;
    private String orderId;
    private String pgId;
    private String errorMsg;
    private String timeStamp;
    private String updatedDate;
    private String transactionId;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
}
