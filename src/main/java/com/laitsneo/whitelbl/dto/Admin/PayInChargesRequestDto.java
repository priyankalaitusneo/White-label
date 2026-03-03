package com.laitsneo.whitelbl.dto.Admin;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayInChargesRequestDto {

	@NotBlank(message = "userId is required")
	private String userId;

	@NotNull(message = "fromRange is required")
	@PositiveOrZero(message = "fromRange must be >= 0")
	private Long fromRange;

	@NotNull(message = "toRange is required")
	@Positive(message = "toRange must be > 0")
	private Long toRange;

	@NotBlank(message = "chargesType is required")
	private String chargesType;

	@NotNull(message = "chargesAmount is required")
	@Positive(message = "chargesAmount must be > 0")
	private Double chargesAmount;
}