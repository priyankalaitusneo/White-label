package com.laitsneo.whitelbl.dto.Client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientOnboardDto {

	   // step 1
    private String merchantName;
    private String email;
    private String mobileNum;
    private String password;
    private String dob;
    private String aadhaar;
    private String pan;
    private String address;
    private String city;
    private String state;
    private String pincode;
private String houseNumber;
private String landmark;
private String cin;
private String scheme;
    // step 2
private String bankName;
    private String merchantType;
    private String accountNumber;
    private String ifscCode;
    private String gst;

    // scheme
    private String schemeId;
}
