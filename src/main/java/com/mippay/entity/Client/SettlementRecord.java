package com.mippay.entity.Client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;


//Supports both Wallet and Bank transfer settlement methods
 //Implements T+1 settlement logic (48 hours delay)
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(exclude = {"fromAccountNumber", "toAccountNumber"}) // Exclude sensitive data from logs
@Table(name = "settlement_records")
public class SettlementRecord {

    private static final Logger logger = LoggerFactory.getLogger(SettlementRecord.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique Settlement Identifier
    @Column(name = "settlement_id", unique = true, length = 50)
    private String settlementId;

    // Merchant Information
    @Column(name = "user_id", length = 50)
    private String userId;
    private String mobile;
    private String merchantName;
    private String email;
    @Column(name = "date")
    private String date;

    @Column(name = "settlement_amount")
    private String settlementAmount;

    // Settlement Method
    @Column(name = "settlement_method", length = 20)
    private String settlementMethod; // WALLET or BANK

    // Bank Transfer Details (For BANK method)
    @Column(name = "utr_number", unique = true, length = 50)
    private String utrNumber;

    // From Account Details (Admin/Platform Account)
    @Column(name = "from_account_holder", length = 255)
    private String fromAccountHolder;

    @Column(name = "from_account_number", length = 50)
    private String fromAccountNumber;

    @Column(name = "from_bank_name", length = 255)
    private String fromBankName;

    @Column(name = "from_ifsc_code", length = 20)
    private String fromIfscCode;

    // To Account Details (Merchant Account)
    @Column(name = "to_account_holder", length = 255)
    private String toAccountHolder;

    @Column(name = "to_account_number", length = 50)
    private String toAccountNumber;

    @Column(name = "to_bank_name", length = 255)
    private String toBankName;

    @Column(name = "to_ifsc_code", length = 20)
    private String toIfscCode;

    @Column(name = "settlement_status", length = 20)
    private String settlementStatus = "PENDING"; // PENDING, COMPLETED, CANCELLED

    // Timestamp Fields
    @Column(name = "initiated_date")
    @CreationTimestamp
    private Date initiatedDate = new Date();

    @Column(name = "settled_date")
    private String SettledDate;

    private String count;
    private String charges;
    private String gst;

}