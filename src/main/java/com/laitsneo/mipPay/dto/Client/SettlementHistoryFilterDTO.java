package com.laitsneo.mipPay.dto.Client;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Used for filtering settlement history with multiple criteria
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementHistoryFilterDTO {

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("settlementId")
    private String settlementId;

    @JsonProperty("fromDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @JsonProperty("toDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    @JsonProperty("status")
    private String status; // IN_PROGRESS, SETTLED, FAILED

    @JsonProperty("settlementStatus")
    private String settlementStatus; // PENDING, COMPLETED, CANCELLED

    @JsonProperty("settlementMethod")
    private String settlementMethod; // WALLET, BANK

    @JsonProperty("minAmount")
    private Double minAmount;

    @JsonProperty("maxAmount")
    private Double maxAmount;

    @JsonProperty("page")
    private Integer page = 0;

    @JsonProperty("size")
    private Integer size = 20;

    @JsonProperty("sortBy")
    private String sortBy = "createdDate"; // Field to sort by

    @JsonProperty("sortDirection")
    private String sortDirection = "DESC"; // ASC or DESC
}