package com.laitsneo.whitelbl.entity.Client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "payinRecords")
public class PayinRecords {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

	// --------- Common User Fields ---------
    private String userId;
    private String name;
    private String email;
    private String mobile;
    private String address;
 
    // --------- Payin Amount Fields ---------
    private Double amount;
    private double charges;
    private double gstCharges;
    private double totalCharges;
    private String paymentMethod;
 
    // --------- Payin Specific ---------
    private String trxnid;
    private String settlementStatus;
    @Transient
    private String redirect_route;
    private String bankRefId;
 
    // --------- Fields added from PayoutRecords ---------
    private String number;              // Same as mobile but added to match payout
    private String accNumber;
    private String ifsc;
    private double payoutCharges;
    private double payoutGstCharges;
    private double finalAmount;
    private String statusCode;
    private String refundStatus;
    private double currentBalance;
    private double updatedBalance;
    private String transferMode;
    private String pgId;
    private String errorMsg;
    private String timeStamp;
    private double currentWalet;
 
    // --------- NEW: Hold Amount Fields ---------
    private Double holdAmount;          // Amount being held
    private String holdReason;          // Reason for holding amount
    private String holdStatus;          // NONE, ACTIVE, RELEASED
 
    // --------- Common Transaction Fields ---------
    private String status;
    private String utr;
    private String orderId;
    // --------- Timestamps ---------
    @Column(nullable = false)
    private LocalDateTime createdDate;
 
    @Column
    private LocalDateTime updatedDate;
 
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        // Initialize hold fields
        if (holdAmount == null) {
            holdAmount = 0.0;
        }
        if (holdStatus == null) {
            holdStatus = "NONE";
        }
        
        // Initialize settlement status
        if (settlementStatus == null) {
            settlementStatus = "UNSETTLED";
        }
    }
 
    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}

