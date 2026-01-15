package com.laitsneo.mipPay.entity.Admin;


import java.util.Collection;
import java.util.Date;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Builder
public class User implements UserDetails {

    @Id
    private String adminId;
    @NotNull(message = "Email is required !!")
    @Email(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$",message = "Invalid email address")
    @Column(unique = true)
    private String email;
    @NotNull(message = "password field is required !!")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()-+=]).{8,}$",
            message = "Password must contain at least 8 characters, one numeric digit, one uppercase letter, one lowercase letter, and one special symbol")
    private String password;
    @NotNull(message = "name field is required !!")
    @Pattern(regexp = "^[a-zA-Z]+(?:[' -][a-zA-Z]+)*$", message = "Please provide a valid name !!")
    private String name;
    @Pattern(regexp = "[6-9][0-9]{9}", message = "Please provide a valid 10 digit mobile number.")
    private String mobile;
    //    private String status;
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE",nullable = false)
    private Boolean active = true;
    
    @CreationTimestamp
    private Date createdDate = new Date();
    

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}

