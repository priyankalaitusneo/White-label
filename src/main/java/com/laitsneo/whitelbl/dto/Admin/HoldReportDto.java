package com.laitsneo.whitelbl.dto.Admin;

import lombok.Data;
import java.time.LocalDate;

@Data
public class HoldReportDto {
    private String transactionId;
    private String merchantName;
    private Double amount;
    private LocalDate holdDate;
    private String reason;
    private LocalDate releaseDate;
    private String status;
}
