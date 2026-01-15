package com.laitsneo.mipPay.dto.Client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientEditProfileDto {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
    private String mobileNum;

    private String userId;

    private String password; // Optional - only if client wants to change password


    public boolean hasUpdates() {
        return (name != null && !name.trim().isEmpty()) ||
                (email != null && !email.trim().isEmpty()) ||
                (mobileNum != null && !mobileNum.trim().isEmpty()) ||
                (password != null && !password.trim().isEmpty());
    }

    @Override
    public String toString() {
        return "ClientEditProfileDto{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", mobileNum='" + mobileNum + '\'' +
                ", hasPassword=" + (password != null && !password.isEmpty()) +
                '}';
    }
}
