package com.laitsneo.mipPay.dto.Admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorsDTO {

    private String id;

    @NotBlank(message = "Vendor name is required")
    private String vendorName;

    @NotBlank(message = "API is required")
    private String api;

    @NotNull(message = "Charges are required")
    private BigDecimal charges;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String status;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;
}