package com.laitsneo.whitelbl.dto.Admin;


import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminClientUpdateDto {

    private String name;
    private String email;
    private String mobileNum;
    private String password;

    private String accountNum;
    private String ifscCode;
    private String bankName;
    private String gst;
    private String cin;
    private String merchantType;

    private BigDecimal walletBalance;
    private String status;
}