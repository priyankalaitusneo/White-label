package com.laitsneo.whitelbl.entity.Client;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpAddress {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private int Id;
    @NotBlank (message = "userId should not be null..!")
    private String userId;
    @NotBlank (message = "ipAddress should not be null..!")
    private String ipAddress;
    
    private String type;

    @CreatedDate
    private Date createdDate = new Date();
    @UpdateTimestamp
    private Date updatedDate;
}
