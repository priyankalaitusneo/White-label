package com.laitsneo.whitelbl.entity.Admin;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Entity
@Table(name = "settlement_rule_slot")
@Data
public class SettlementRuleSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "time_slot")
    private LocalTime timeSlot;
}