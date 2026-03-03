package com.laitsneo.whitelbl.dto.Client;

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
 * Used for editing existing settlements (only IN_PROGRESS status)
 * Allows editing: settlement amount, date range, bank details, remarks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementEditRequestDTO {

    @JsonProperty("fromDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @JsonProperty("toDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    @JsonProperty("settlementAmount")
    @DecimalMin(value = "0.01", message = "Settlement amount must be greater than 0")
    private Double settlementAmount;

    @JsonProperty("bankDetails")
    @Valid
    private BankTransferDetailsDTO bankDetails;

    @JsonProperty("remarks")
    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters")
    private String remarks;

    /**
     * Custom validation for date range
     */
    @AssertTrue(message = "To date must be greater than or equal to from date")
    public boolean isValidDateRange() {
        if (fromDate == null || toDate == null) {
            return true; // Null means no change
        }
        return !toDate.isBefore(fromDate);
    }
}