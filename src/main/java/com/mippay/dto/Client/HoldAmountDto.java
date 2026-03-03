package com.mippay.dto.Client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HoldAmountDto {
	
	
	 
	    @NotBlank(message = "Order ID is required")
	    private String orderId;
	 
	    @NotBlank(message = "User ID is required")
	    private String userId;
	 
	    @NotNull(message = "Hold amount is required")
	    @Positive(message = "Hold amount must be positive")
	    private Double holdAmount;
	 
	    @NotBlank(message = "Hold reason is required")
	    private String holdReason;
}
