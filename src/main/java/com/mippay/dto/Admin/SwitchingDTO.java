package com.mippay.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// DTO for switching all merchants
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwitchingDTO {
    private String vendorName;
}

// DTO for switching selected merchants
@Data
@NoArgsConstructor
@AllArgsConstructor
class SwitchSelectedRequest {
    private String vendorName;
    private List<String> merchantIds;
}

// DTO for switching log response
@Data
@NoArgsConstructor
@AllArgsConstructor
class SwitchingLogResponse {
    private Long id;
    private LocalDate date;
    private LocalTime time;
    private String merchantId;
    private String merchantName;
    private String switchedPipe;
    private String updatedBy;
}

// DTO for vendor response
@Data
@NoArgsConstructor
@AllArgsConstructor
class VendorResponse {
    private Long id;
    private String vendorName;
    private String status;
}