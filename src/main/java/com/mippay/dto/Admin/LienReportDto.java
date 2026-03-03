package com.mippay.dto.Admin;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LienReportDto {
    private String merchantName;
    private Double lienAmount;
    private LocalDate lienDate;
    private String reason;
    private LocalDate releaseDate;
    private String status;
}
