package com.laitsneo.mipPay.dto.Client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


 
//  Used to display merchants with their unsettled funds
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementListResponseDTO {

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("merchantName")
    private String merchantName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @JsonProperty("unsettledFund")
    private Double unsettledFund;

    @JsonProperty("status")
    private String status; // UNSETTLED or SETTLED

    @JsonProperty("totalTransactions")
    private Long totalTransactions;

    @JsonProperty("lastTransactionDate")
    private LocalDate lastTransactionDate;

    @JsonProperty("accountStatus")
    private String accountStatus; // From Client.status

    // For filtering/sorting
    @JsonProperty("fromDate")
    private LocalDate fromDate;

    @JsonProperty("toDate")
    private LocalDate toDate;
}