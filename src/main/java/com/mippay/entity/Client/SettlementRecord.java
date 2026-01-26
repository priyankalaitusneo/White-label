package com.mippay.entity.Client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;



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
    @Column(name = "settlement_id", unique = true, nullable = false, length = 50)
    private String settlementId;

    // Merchant Information
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "merchant_name", length = 255)
    private String merchantName;

    // Date Range for Settlement
    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    // Amount Details
    @Column(name = "total_unsettled_amount")
    private Double totalUnsettledAmount;

    @Column(name = "settlement_amount", nullable = false)
    private Double settlementAmount;

    @Column(name = "wallet_settlement_amount")
    private Double walletSettlementAmount = 0.0;

    @Column(name = "bank_settlement_amount")
    private Double bankSettlementAmount = 0.0;

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

    // Status Fields
    @Column(name = "status", length = 20)
    private String status = "IN_PROGRESS"; // IN_PROGRESS, SETTLED, FAILED

    @Column(name = "settlement_status", length = 20)
    private String settlementStatus = "PENDING"; // PENDING, COMPLETED, CANCELLED

    // Timestamp Fields
    @Column(name = "initiated_date", nullable = false)
    private LocalDateTime initiatedDate;

    @Column(name = "scheduled_settlement_date")
    private LocalDateTime scheduledSettlementDate; // T+1 (48 hours from initiation)

    @Column(name = "actual_settlement_date")
    private LocalDateTime actualSettlementDate;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Admin & Tracking
    @Column(name = "initiated_by", length = 100)
    private String initiatedBy; // Admin user ID or name

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    // Lifecycle Callbacks
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdDate = now;
        this.updatedDate = now;
        this.initiatedDate = now;
        
        // Calculate T+1 settlement date (48 hours from now)
        this.scheduledSettlementDate = now.plusHours(48);
        
        // Generate unique settlement ID
        if (this.settlementId == null || this.settlementId.isEmpty()) {
            this.settlementId = generateSettlementId();
        }
        
        // Set default values
        if (this.status == null) {
            this.status = "IN_PROGRESS";
        }
        if (this.settlementStatus == null) {
            this.settlementStatus = "PENDING";
        }
        if (this.walletSettlementAmount == null) {
            this.walletSettlementAmount = 0.0;
        }
        if (this.bankSettlementAmount == null) {
            this.bankSettlementAmount = 0.0;
        }

        logger.info("New settlement record created: {} for merchant: {} with amount: {}", 
                    this.settlementId, this.userId, this.settlementAmount);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
        logger.info("Settlement record updated: {} - Status: {} - SettlementStatus: {}", 
                    this.settlementId, this.status, this.settlementStatus);
    }

    /**
     * Generate unique settlement ID
     * Format: STL-YYYYMMDD-HHMMSS-XXX (XXX = random 3 digits)
     */
    private String generateSettlementId() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = String.format("%04d%02d%02d-%02d%02d%02d",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                now.getHour(), now.getMinute(), now.getSecond());
        String random = String.format("%03d", (int) (Math.random() * 1000));
        return "STL-" + timestamp + "-" + random;
    }

    /**
     * Check if settlement is editable
     * Only IN_PROGRESS settlements can be edited
     */
    public boolean isEditable() {
        return "IN_PROGRESS".equals(this.status);
    }

    /**
     * Check if settlement is ready for processing
     * Returns true if scheduled date has passed
     */
    public boolean isReadyForSettlement() {
        return this.scheduledSettlementDate != null 
               && LocalDateTime.now().isAfter(this.scheduledSettlementDate)
               && "IN_PROGRESS".equals(this.status);
    }
}