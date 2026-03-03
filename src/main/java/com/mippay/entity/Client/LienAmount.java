package com.mippay.entity.Client;

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
public class LienAmount {

    @Id
    @GeneratedValue (strategy =  GenerationType.IDENTITY)
    private int id;
    @NotBlank(message = "userId should not be null..!")
    private String userId;
    
    
    private Double amount;
    
    private String description;
    
    @CreatedDate
    private Date createdDate = new Date();
    @UpdateTimestamp
    private Date UpdatedDate ;
}
