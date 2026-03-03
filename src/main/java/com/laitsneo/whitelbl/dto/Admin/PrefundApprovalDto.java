package com.laitsneo.whitelbl.dto.Admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrefundApprovalDto {

    @NotBlank(message = "Status is required")
    private String status;

    @NotBlank(message = "Reference is required")
    private String reference;

    @NotNull(message = "User ID is required")
    private String userId;

    private LocalDateTime approvedDate;

    // Optional: Add reason for approval/rejection
    private String remarks;
    
    private String approveBy;
}
