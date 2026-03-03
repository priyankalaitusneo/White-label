package com.laitsneo.whitelbl.dto.Client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.convert.DataSizeUnit;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutDto {

    @NotBlank(message = "name is required")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name should be alphanumeric and can contain spaces.")
    private String name;

    @NotBlank(message = "email is required")
    @Email(message = "Invalid email address.")
    private String email;

    @NotBlank(message = "Phone number should not be null or empty.")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number.")
    private String phone;

    @NotBlank(message = "bankAccount is required")
    @Pattern(regexp = "^[0-9]{9,18}$",
            message = "Bank account number must be 9 to 18 digits")
    private String bankAccount;
    @NotBlank(message = "ifsc is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",message = "Invalid IFSC code format")
    private String ifsc;
    @NotBlank(message = "address is required")
    private String address;
    @NotBlank(message = "amount is required")
    @Pattern(regexp = "^(?!0+(\\.0{1,2})?$)\\d+(\\.\\d{1,2})?$",
            message = "Amount must be a positive number greater than 0")
    private String amount;
    @NotBlank(message = "orderId is required")
    private String orderId;
    private String transferMode;
    private String remarks;
    @NotBlank(message = "userId is required")
    private String userId;
}
