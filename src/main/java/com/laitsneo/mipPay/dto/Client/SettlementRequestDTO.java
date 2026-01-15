package com.laitsneo.mipPay.dto.Client;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Settlement Request DTO
 * Used when admin initiates a new settlement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementRequestDTO {

    @JsonProperty("userId")
    @NotBlank(message = "Merchant user ID is required")
    @Size(max = 50, message = "User ID cannot exceed 50 characters")
    private String userId;

    @JsonProperty("fromDate")
 //   @NotNull(message = "From date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @JsonProperty("toDate")
  //  @NotNull(message = "To date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    @JsonProperty("settlementAmount")
    @NotNull(message = "Settlement amount is required")
    @DecimalMin(value = "0.01", message = "Settlement amount must be greater than 0")
    private Double settlementAmount;

    @JsonProperty("settlementMethod")
    @NotBlank(message = "Settlement method is required")
    @Pattern(regexp = "^(WALLET|BANK)$", message = "Settlement method must be either WALLET or BANK")
    private String settlementMethod;

    @JsonProperty("bankDetails")
    @Valid
    private BankTransferDetailsDTO bankDetails;

    @JsonProperty("remarks")
    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters")
    private String remarks;

    @JsonProperty("initiatedBy")
    @Size(max = 100, message = "Initiated by cannot exceed 100 characters")
    private String initiatedBy;

    /**
     * Custom validation for date range
     */
    @AssertTrue(message = "To date must be greater than or equal to from date")
    public boolean isValidDateRange() {
        if (fromDate == null || toDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return !toDate.isBefore(fromDate);
    }

    /**
     * Custom validation for bank details when method is BANK
     */
    @AssertTrue(message = "Bank details are required when settlement method is BANK")
    public boolean isValidBankDetails() {
        if ("BANK".equalsIgnoreCase(settlementMethod)) {
            return bankDetails != null 
                   && bankDetails.getFromAccount() != null 
                   && bankDetails.getToAccount() != null
                   && bankDetails.getUtrNumber() != null;
        }
        return true;
    }
}