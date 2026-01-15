package com.laitsneo.mipPay.entity.Client;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LienHistory {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private int id;
    private String reference;
    private String amount;
    private String userId;
    private String status;

    @CreatedDate
    private Date timestamp = new Date();
}
