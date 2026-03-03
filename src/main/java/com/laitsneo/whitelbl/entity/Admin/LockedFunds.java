package com.laitsneo.whitelbl.entity.Admin;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "locked_funds")
@Data
public class LockedFunds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "merchant_name", length = 255)
    private String merchantName;

    @Column(name = "amount_locked", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountLocked;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "locked_date", nullable = false)
    private LocalDateTime lockedDate;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
        if (lockedDate == null) {
            lockedDate = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}