package com.laitsneo.whitelbl.dto.Client;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailOtpDto {

    @NotNull(message = "Otp cannot be empty")
    private String otp;
    @NotNull(message = "Email cannot be empty")
    private String email;
    @NotNull(message = "Passord cannot be empty")
    private String password;
}
