package com.mippay.dto.Admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PayinDto {
    private String orderId;
    private String status;
    private String remarks;
    private String name;
    private String phone;
    private String email;
    private String paymentMethod;
    private String charges;
    private String gstCharges;
    private String totalCharges;
    private String accountNo;
    private String mobile;
    private String address;
    
    @NotBlank(message = "amount should not be null or empty")
    @Pattern(
        regexp = "^[1-9][0-9]{2,}$",
        message = "amount must be at least 100 paisa"
    )
    private String amount;
      // You can convert to BigDecimal if needed
    private String userId;
    private String redirectRoute;     
}
