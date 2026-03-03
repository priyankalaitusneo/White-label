package com.laitsneo.whitelbl.dto.Client;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Complete settlement record information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementDetailsResponseDTO {

    @JsonProperty("settlementId")
    private String settlementId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("merchantName")
    private String merchantName;

    @JsonProperty("merchantEmail")
    private String merchantEmail;

    @JsonProperty("merchantMobile")
    private String merchantMobile;

    // Date Range
    @JsonProperty("fromDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @JsonProperty("toDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    // Amount Details
    @JsonProperty("totalUnsettledAmount")
    private Double totalUnsettledAmount;

    @JsonProperty("settlementAmount")
    private Double settlementAmount;

    @JsonProperty("walletSettlementAmount")
    private Double walletSettlementAmount;

    @JsonProperty("bankSettlementAmount")
    private Double bankSettlementAmount;

    // Settlement Method
    @JsonProperty("settlementMethod")
    private String settlementMethod; // WALLET or BANK

    // Bank Details (if applicable)
    @JsonProperty("utrNumber")
    private String utrNumber;

    @JsonProperty("fromAccount")
    private BankAccountInfo fromAccount;

    @JsonProperty("toAccount")
    private BankAccountInfo toAccount;

    // Status
    @JsonProperty("status")
    private String status; // IN_PROGRESS, SETTLED, FAILED

    @JsonProperty("settlementStatus")
    private String settlementStatus; // PENDING, COMPLETED, CANCELLED

    // Timestamps
    @JsonProperty("initiatedDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime initiatedDate;

    @JsonProperty("scheduledSettlementDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledSettlementDate;

    @JsonProperty("actualSettlementDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualSettlementDate;

    @JsonProperty("createdDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @JsonProperty("updatedDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;

    // Admin & Tracking
    @JsonProperty("initiatedBy")
    private String initiatedBy;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("failureReason")
    private String failureReason;

    // Additional Info
    @JsonProperty("transactionCount")
    private Long transactionCount; // Number of PayinRecords included

    @JsonProperty("isEditable")
    private Boolean isEditable; // Can this settlement be edited?

    @JsonProperty("remainingHours")
    private Long remainingHours; // Hours until scheduled settlement

    /**
     * Nested class for Bank Account Information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankAccountInfo {
        @JsonProperty("accountHolderName")
        private String accountHolderName;

        @JsonProperty("accountNumber")
        private String accountNumber;

        @JsonProperty("bankName")
        private String bankName;

        @JsonProperty("ifscCode")
        private String ifscCode;
    }
}