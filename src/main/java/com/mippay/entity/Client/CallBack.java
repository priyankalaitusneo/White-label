package com.mippay.entity.Client;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallBack {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private int id;
    private String code;
    private String organizationId;
    private String transactionId;
    private String orderId;
    private String description;
    private String utr;
    private String ledger;
    private String paymentType;
    private String amount;
    private String transactionFees;
    private String payableAmount;
    private String status;
}
