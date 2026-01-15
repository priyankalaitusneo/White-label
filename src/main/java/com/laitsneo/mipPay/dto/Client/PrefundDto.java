package com.laitsneo.mipPay.dto.Client;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrefundDto {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "User ID is required")
    private String userId;

    private String reference;
    
    private String fromAccount;
    private String toAccount;
    private String paymentMethod;
    private String adminIfsc;

}
