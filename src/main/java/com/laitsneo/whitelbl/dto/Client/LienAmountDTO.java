package com.laitsneo.whitelbl.dto.Client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LienAmountDTO {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
   
    private Double amount;
    
    private String description;
}