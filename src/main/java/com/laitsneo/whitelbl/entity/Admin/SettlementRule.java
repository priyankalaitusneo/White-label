package com.laitsneo.whitelbl.entity.Admin;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_rule")
@Data
public class SettlementRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId; // merchantId

    @Column(name = "slot_type", nullable = false)
    private String slotType; // e.g. T+1, Same Day 1 Time, etc.

    @Column(name = "active")
    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
}

