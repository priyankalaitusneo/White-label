package com.laitsneo.mipPay.dto.Client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDto {

    private Long slNo;
    private String userId;
    private String name;
    private String email;
    private String mobileNum;
    private String status;
    private String accountNum;
    private String ifscCode;
    private String gst;
    private String cin;
    private String merchantType;
    private BigDecimal accountBal;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
