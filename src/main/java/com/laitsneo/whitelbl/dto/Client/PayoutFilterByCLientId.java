package com.laitsneo.whitelbl.dto.Client;


import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayoutFilterByCLientId {

    @NotNull(message = "clientId should not be null")
    private String clientId;
    private String utr;
    private String transactionId;
    private String fromDate;
    private String toDate;
    private String transferMode;
    private String status;

}
