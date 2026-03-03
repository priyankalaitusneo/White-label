package com.laitsneo.whitelbl.dto.Admin;


import lombok.Data;
import java.math.BigDecimal;

@Data
public class ClientOnboardDto {

    private String name;
    private String email;
    private String mobileNum;
    private String password;

    private String dob;
    private String aadharNo;
    private String pan;

    private String address;
    private String state;
    private String city;
    private String pincode;
    private String houseNumber;
    private String landmark;

    private String merchantType;
    private String accountNum;
    private String ifscCode;
    private String gst;
    private String cin;

    private String scheme;

    private BigDecimal accountBal;
}