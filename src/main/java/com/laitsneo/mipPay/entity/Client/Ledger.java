package com.laitsneo.mipPay.entity.Client;


import com.laitsneo.mipPay.helper.DateTimeGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Ledger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String userId;
    private String type;
    private String amount;
    private String openingBalance;
    private String closingBalance;
    private String orderId;
    private String description;
    private String date;
    private String timestamp;
    private String updatedDate;
    private String updatedTimestamp;


    public Ledger(String userId, String amount, String orderId, String updatedBalance){
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.orderId = orderId;
        this.openingBalance = String.valueOf(Float.parseFloat(updatedBalance)+Float.parseFloat(amount));
        this.closingBalance = updatedBalance;
        this.description = "amount" + amount + "Rs. has been DEBITED";
        DateTimeGenerator generator = new DateTimeGenerator();
        this.date = generator.fetchDate();
        this.timestamp = generator.fetchTime();
    }
}
