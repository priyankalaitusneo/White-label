package com.laitsneo.mipPay.dto.Client;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bank Transfer Details DTO
 * Contains complete bank transfer information for settlements
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTransferDetailsDTO {

    @JsonProperty("utrNumber")
    @NotBlank(message = "UTR number is required for bank transfer")
    @Size(min = 10, max = 50, message = "UTR number must be between 10 and 50 characters")
    private String utrNumber;

    @JsonProperty("fromAccount")
    @Valid
    private AccountDetailsDTO fromAccount;

    @JsonProperty("toAccount")
    @Valid
    private AccountDetailsDTO toAccount;

    /**
     * Nested DTO for Account Details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDetailsDTO {

        @JsonProperty("accountHolderName")
        @NotBlank(message = "Account holder name is required")
        @Size(max = 255, message = "Account holder name cannot exceed 255 characters")
        private String accountHolderName;

        @JsonProperty("accountNumber")
        @NotBlank(message = "Account number is required")
        @Pattern(regexp = "^[0-9]{9,18}$", message = "Account number must be 9-18 digits")
        private String accountNumber;

        @JsonProperty("bankName")
        @NotBlank(message = "Bank name is required")
        @Size(max = 255, message = "Bank name cannot exceed 255 characters")
        private String bankName;

        @JsonProperty("ifscCode")
        @NotBlank(message = "IFSC code is required")
        @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format")
        private String ifscCode;
    }
}