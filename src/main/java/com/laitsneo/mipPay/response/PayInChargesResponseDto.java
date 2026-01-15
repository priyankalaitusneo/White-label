package com.laitsneo.mipPay.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayInChargesResponseDto {
    private Long id;
    private String userId;
    private Long fromRange;
    private Long toRange;
    private String chargesType;
    private Double charges;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
