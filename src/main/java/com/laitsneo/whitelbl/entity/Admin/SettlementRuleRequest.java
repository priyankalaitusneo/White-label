package com.laitsneo.whitelbl.entity.Admin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class SettlementRuleRequest {
    @NotNull
    private String userId;  // merchant id

    @NotNull
    private String slotType;

    // Optional time slots for manual slot types
    private List<String> timeSlots;
}

