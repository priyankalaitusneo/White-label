package com.mippay.entity.Admin;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "switching_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Switching {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "time", nullable = false)
    private LocalTime time;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "merchant_name", nullable = false)
    private String merchantName;

    @Column(name = "merchant_type", nullable = false, length = 20)
    private String merchantType;  // PAYIN or PAYOUT

    @Column(name = "switched_pipe", nullable = false)
    private String switchedPipe;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    public Switching(String merchantId, String merchantName, String merchantType, 
                     String switchedPipe, String updatedBy) {
        this.merchantId = merchantId;
        this.merchantName = merchantName;
        this.merchantType = merchantType;
        this.switchedPipe = switchedPipe;
        this.updatedBy = updatedBy;
        this.date = LocalDate.now();
        this.time = LocalTime.now();
    }
}