package com.mippay.dto.Admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChargesDto {

    private int slNo;
    @NotBlank(message = "UserId is required")
    private String userId;
    @NotBlank(message = "ChargesType is required")
    private String chargesType;
    @NotNull(message = "charges is required")
    private Double charges;
}
