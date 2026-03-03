package com.mippay.dto.Admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PrefundRejectDto {

	@NotBlank(message = "Rejection description is required")
	private String remarks;
	
	private String reference;
	private String userId;
	private String approveBy;

}
